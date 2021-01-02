const Database = require('../services/database');
const Lambda = require('../services/lambda');

async function sendWebSocketMessage(username, body, action) {
    const connectionIds = await Database.user.fetchConnectionIds(username);
    await Lambda.sendMessage(body, connectionIds, action);
}

async function getChat(chatId, member) {
    const chat = await Database.chat.getChat(chatId);
    const chatMember = member ? await Database.user.getUser(member) : null;
    const lastMessage = await Database.message.getLastMessage(chatId);

    return {
        ...chat,
        lastMessage: lastMessage || null,
        user: chatMember,
    };
}

async function getAllChats(username) {
    const userChatsMetadata = await Database.chat.fetchUserChats(username);

    const chats = await Promise.all(
        userChatsMetadata.map(async ({ id }) => {
            return Database.chat.getChat(id);
        }),
    );

    const response = await Promise.all(
        chats.map(async chat => {
            if (chat.private) {
                const users = await Database.chat.fetchChatUsers(chat.id);
                const member = users.filter(user => user !== username)[0];
                return getChat(chat.id, member);
            }
            return getChat(chat.id);
        }),
    );

    return response.sort((a, b) => {
        if (!a.lastMessage) return 1;

        if (a.lastMessage && !b.lastMessage) return -1;

        return b.lastMessage.createdAt - a.lastMessage.createdAt;
    });
}

async function createChat(username, withUsername) {
    const chatAlreadyExists = await Database.chat.getPrivateChat(
        username,
        withUsername,
    );

    if (chatAlreadyExists) {
        return getChat(chatAlreadyExists, username);
    }

    const chatId = await Database.chat.createChat({
        private: `${username}:${withUsername}`,
    });

    await Promise.all([
        Database.chat.addMember(chatId, username),
        Database.chat.addMember(chatId, withUsername),
    ]);

    const chat = await getChat(chatId, withUsername);
    await sendWebSocketMessage(
        withUsername,
        await getChat(chatId, username),
        'created_chat',
    );
    return chat;
}

async function createGroup(username, { members, groupName }) {
    const groupId = await Database.chat.createChat({
        groupName,
        admin: username,
        private: null,
    });

    const chat = await Database.chat.getChat(groupId);

    await Promise.all(
        members.map(async member => {
            await Database.chat.addMember(groupId, member);
            await sendWebSocketMessage(member, chat, 'created_chat');
        }),
        Database.chat.addMember(groupId, username),
        sendWebSocketMessage(username, chat, 'created_chat'),
    );

    return chat;
}

async function addMessage({ chatId, username, message, createdAt, messageId }) {
    const messageResponse = await Database.message.addMessage({
        chatId,
        username,
        message,
        messageId,
        createdAt,
        status: 'SENT',
    });

    const users = await Database.chat.fetchChatUsers(chatId);
    const chatUsers = users.filter(u => u !== username);
    await Promise.all(
        chatUsers.map(async user => {
            await Database.message.newMessages(chatId, user, createdAt);
            await sendWebSocketMessage(user, messageResponse, 'new_message');
        }),
        await sendWebSocketMessage(username, messageResponse, 'message_sent'),
    );
}

async function getMessages(chatId) {
    return Database.message.fetchMessages(chatId);
}

async function viewedMessages({ chatId, username }) {
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
