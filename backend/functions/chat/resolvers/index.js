const { nanoid } = require('nanoid');
const Database = require('../services/database');
const Lambda = require('../services/lambda');

async function sendWebSocketMessage(username, body, action) {
    const connectionIds = await Database.user.fetchConnectionIds(username);
    await Lambda.sendMessage(body, connectionIds, action);
}

async function getChat(chatId, username, member) {
    const chat = await Database.chat.getChat(chatId);
    const { newMessages } = await Database.chat.getUserChatMetadata(
        chatId,
        username,
    );
    const chatMember = member ? await Database.user.getUser(member) : null;
    const lastMessage = await Database.message.getLastMessage(chatId);

    return {
        ...chat,
        newMessages,
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
                return getChat(chat.id, username, member);
            }
            return getChat(chat.id, username);
        }),
    );

    return response
        .filter(chat => chat.lastMessage || !chat.private)
        .sort((a, b) => {
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
        return getChat(chatAlreadyExists, username, withUsername);
    }

    const chatId = await Database.chat.createChat({
        private: `${username}:${withUsername}`,
    });

    await Promise.all([
        Database.chat.addMember(chatId, username),
        Database.chat.addMember(chatId, withUsername),
    ]);

    const chat = await getChat(chatId, username, withUsername);
    await sendWebSocketMessage(
        withUsername,
        await getChat(chatId, withUsername, username),
        'created_chat',
    );
    return chat;
}

async function createGroup(username, { members, groupName }) {
    const groupId = await Database.chat.createChat({
        groupName,
        admin: username,
        private: '',
        createdAt: Date.now(),
    });

    const chat = await Database.chat.getChat(groupId, username);
    await Promise.all(
        members.map(async member => {
            await Database.chat.addMember(groupId, member);
            await sendWebSocketMessage(member, chat, 'created_chat');
        }),
        await Database.chat.addMember(groupId, username),
        await sendWebSocketMessage(username, chat, 'created_chat'),
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

    const chatConfig = await Database.chat.getChat(chatId);

    const users = await Database.chat.fetchChatUsers(chatId);
    const chatUsers = users.filter(u => u !== username);

    const currentUserChat = chatConfig.private
        ? await getChat(
              chatId,
              username,
              chatConfig.private
                  .split(':')
                  .filter(member => member !== username),
          )
        : await getChat(chatId, username);

    await Promise.all(
        chatUsers.map(async memberUsername => {
            await Database.message.newMessages(chatId, memberUsername);
            const memberChat = chatConfig.private
                ? await getChat(chatId, memberUsername, username)
                : await getChat(chatId, memberUsername);

            await sendWebSocketMessage(
                memberUsername,
                messageResponse,
                'new_message',
            );
            await sendWebSocketMessage(
                memberUsername,
                memberChat,
                'chat_updated',
            );
        }),

        sendWebSocketMessage(username, messageResponse, 'message_sent'),
        sendWebSocketMessage(username, currentUserChat, 'chat_updated'),
    );
}

async function getMessages(chatId) {
    return Database.message.fetchMessages(chatId);
}

async function viewedMessages({ chatId, username }) {
    await Database.message.viewedMessages(chatId, username);
    const chatConfig = await Database.chat.getChat(chatId);
    const chat = chatConfig.private
        ? await getChat(
              chatId,
              username,
              chatConfig.private
                  .split(':')
                  .filter(member => member !== username),
          )
        : await getChat(chatId, username);

    await sendWebSocketMessage(username, chat, 'chat_updated');
}

async function getGroupMembers(chatId) {
    const chatUsers = await Database.chat.fetchChatUsers(chatId);

    const promises = [];

    chatUsers.forEach(username => {
        promises.push(Database.user.getUser(username));
    });

    return Promise.all(promises);
}

async function addMember(chatId, username) {
    const usersChat = await Database.chat.fetchChatUsers(chatId);

    if (usersChat.find(member => member === username)) {
        return true;
    }

    await Database.chat.addMember(chatId, username);
    const chatConfig = await Database.chat.getChat(chatId);
    const chat = chatConfig.private
        ? await getChat(
              chatId,
              username,
              chatConfig.private
                  .split(':')
                  .filter(member => member !== username),
          )
        : await getChat(chatId, username);

    await sendWebSocketMessage(username, chat, 'chat_updated');

    return true;
}

async function removeMember(chatId, username) {
    const chatConfig = await Database.chat.getChat(chatId);
    const user = await Database.user.getUser(username);
    await Database.chat.removeMember(chatId, username);
    await sendWebSocketMessage(username, { chatId }, 'chat_removed');

    const messageResponse = await Database.message.addMessage({
        chatId,
        username: 'SYSTEM',
        message: `${user.name} foi removido do grupo.`,
        messageId: nanoid(),
        createdAt: Date.now(),
        status: 'SENT',
    });

    const users = await Database.chat.fetchChatUsers(chatId);
    if (users.length && chatConfig.admin === username) {
        await Database.chat.updateAdmin(chatId, users[0]);
    }

    await Promise.all(
        users.map(async memberUsername => {
            await Database.message.newMessages(chatId, memberUsername);
            const memberChat = await getChat(chatId, memberUsername);
            await sendWebSocketMessage(
                memberUsername,
                messageResponse,
                'new_message',
            );
            await sendWebSocketMessage(
                memberUsername,
                memberChat,
                'chat_updated',
            );
        }),
    );

    return true;
}

module.exports = {
    getAllChats,
    createChat,
    createGroup,
    getMessages,
    addMessage,
    viewedMessages,
    getGroupMembers,
    addMember,
    removeMember,
};
