const Database = require('../services/database');

async function getChatWithUser(chatId, username) {
    const chat = await Database.getChat(chatId);
    const user = await Database.getUser(username);
    const lastMessage = await Database.getLastMessage(chatId);
    return {
        ...chat,
        user,
        lastMessage: lastMessage || {},
    };
}

async function getUsersByChat(chatId) {
    return Database.getChatUsers(chatId);
}

async function getAllChats(username) {
    const chatIds = await Database.getUserChats(username);
    const chats = [];
    const promises = chatIds.map(async chatId => {
        const allUsers = await getUsersByChat(chatId);
        const users = allUsers.map(u => u.username !== username);
        chats.push(await getChatWithUser(chatId, username));
    });

    return Promise.all(promises);
}

async function privateChatAlreadyExists(username, withUsername) {
    const userChatIds = await Database.getUserChats(username);
    const withUserChatIds = await Database.getUserChats(withUsername);
    const chatIds = [];
    userChatIds.forEach(userChatId => {
        if (withUserChatIds.includes(userChatId)) chatIds.push(userChatId);
    });

    if (chatIds.length) {
        const requests = [];
        chatIds.forEach(chatId => {
            requests.push(Database.getChat(chatId));
        });

        const chats = await Promise.all(requests);

        if (!chats.length) return null;

        const findChat = chats.find(chat => chat.isPrivate);

        return findChat ? getChatWithUser(findChat.chatId, withUsername) : null;
    }
    return null;
}

async function createChat(username, withUsername) {
    const chatAlreadyExists = await privateChatAlreadyExists(
        username,
        withUsername,
    );
    if (chatAlreadyExists) return chatAlreadyExists;
    const chatId = await Database.createChat();
    await Promise.all([
        Database.createUserChat(username, chatId),
        Database.createUserChat(withUsername, chatId),
    ]);
    return getChatWithUser(chatId, username);
}

async function createGroup(username, { members, groupName }) {
    const groupId = await Database.createGroup(groupName, username);

    await Promise.all(
        members.map(async member => {
            Database.createUserChat(member, groupId);
        }),
        Database.createUserChat(username, groupId),
    );

    return Database.getChat(groupId);
}

async function addMessage(chatId, username, { message }) {
    return Database.addMessage(chatId, username, message);
}

async function getMessages(chatId) {
    return Database.getMessages(chatId);
}

module.exports = {
    getAllChats,
    createChat,
    createGroup,
    getMessages,
    addMessage,
};
