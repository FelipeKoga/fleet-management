const _ = require('lodash');
const Database = require('../services/database');

const zonedTime = date => {
    const localeFormat = date.toLocaleString('en-US', {
        timeZone: 'America/Sao_Paulo',
    });
    return new Date(localeFormat);
};

async function getChat(chatId, member) {
    const chat = await Database.chat.getChat(chatId);
    const chatMember = member ? await Database.user.getUser(member) : null;
    const lastMessage = await Database.message.getLastMessage(chatId);
    return {
        ...chat,
        lastMessage,
        user: chatMember,
    };
}

async function getAllChats(username) {
    const userChats = await Database.chat.getUserChats(username);
    const chats = await Promise.all(
        userChats.map(async ({ chatId }) => {
            return Database.chat.getChat(chatId);
        }),
    );

    return Promise.all(
        chats.map(async ({ chatId, isPrivate }) => {
            if (isPrivate) {
                const users = await Database.chat.getChatUsers(chatId);
                const member = users.filter(
                    user => user.username !== username,
                )[0];
                return getChat(chatId, member);
            }

            return getChat(chatId);
        }),
    );
}

async function privateChatAlreadyExists(username, member) {
    const userChats = await Database.chat.getPrivateChats(username);
    const memberChats = await Database.chat.getPrivateChats(member);
    const chatId = _.intersection(
        userChats.map(item => item.chatId),
        memberChats.map(item => item.chatId),
    )[0];

    if (chatId) return getChat(chatId, member);

    return null;
}

async function createChat(username, withUsername) {
    const chatAlreadyExists = await privateChatAlreadyExists(
        username,
        withUsername,
    );
    if (chatAlreadyExists) return chatAlreadyExists;
    const chatId = await Database.chat.createChat({ isPrivate: true });
    await Promise.all([
        Database.chat.addMember(chatId, username),
        Database.chat.addMember(chatId, withUsername),
    ]);
    return getChat(chatId, withUsername);
}

async function createGroup(username, { members, groupName }) {
    const groupId = await Database.chat.createChat({
        groupName,
        admin: username,
        isPrivate: false,
    });

    await Promise.all(
        members.map(async member => {
            Database.chat.addMember(groupId, member);
        }),
        Database.chat.addMember(groupId, username),
    );

    return Database.chat.getChat(groupId);
}

async function addMessage(chatId, username, { message }) {
    const timestamp = +zonedTime(new Date());
    await Database.addMessage(chatId, username, message, timestamp);
    const users = await Database.chat.getChatUsers(chatId);
    const chatUsers = users.filter(u => u !== username);
    await Promise.all(
        chatUsers.map(async user => {
            Database.message.newMessages(chatId, user, timestamp);
        }),
    );
}

async function getMessages(chatId) {
    return Database.message.getMessages(chatId);
}

async function viewedMessages(chatId, username) {
    return Database.message.viewedMessages(chatId, username);
}
module.exports = {
    getAllChats,
    createChat,
    createGroup,
    getMessages,
    addMessage,
    viewedMessages,
};
