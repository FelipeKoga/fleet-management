const { nanoid } = require('nanoid');
const Database = require('../services/database');
const Lambda = require('../services/lambda');
const S3 = require('../services/s3');

async function getUser(username) {
    const user = await Database.user.getUser(username);
    if (user.avatar) {
        user.avatarUrl = S3.getObject(user.avatar);
    }
    return user;
}

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

    chat.user = member ? await getUser(member) : null;
    const lastMessage = await Database.message.getLastMessage(chatId);

    if (lastMessage && lastMessage.hasAudio) {
        lastMessage.message = S3.getObject(lastMessage.message);
    }

    if (chat.avatar) {
        chat.avatarUrl = S3.getObject(chat.avatar);
    }

    if (!chat.private) {
        const users = await Database.chat.fetchChatUsers(chatId);
        const promises = [];

        users
            .filter(user => user !== username)
            .forEach(user => promises.push(Database.user.getUser(user)));

        chat.members = await Promise.all(promises);
    }

    return {
        ...chat,
        newMessages,
        lastMessage: lastMessage || null,
    };
}

async function addMessageAll(message) {
    const users = await Database.chat.fetchChatUsers(message.chatId);
    await Promise.all(
        users.map(async memberUsername => {
            await Database.message.newMessages(message.chatId, memberUsername);
            const memberChat = await getChat(message.chatId, memberUsername);
            await sendWebSocketMessage(memberUsername, message, 'new_message');
            await sendWebSocketMessage(
                memberUsername,
                memberChat,
                'CHAT_UPDATED',
            );
        }),
    );
}

async function getAllChats(username) {
    const userChatsMetadata = await Database.chat.fetchUserChats(username);

    console.log(userChatsMetadata);
    const chats = await Promise.all(
        userChatsMetadata.map(async ({ id }) => {
            return Database.chat.getChat(id);
        }),
    );
    console.log(chats);

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

            return (
                new Date(b.lastMessage.createdAt) -
                new Date(a.lastMessage.createdAt)
            );
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
        'CREATED_CHAT',
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
            await sendWebSocketMessage(member, chat, 'CREATED_CHAT');
        }),
        await Database.chat.addMember(groupId, username),
        await sendWebSocketMessage(username, chat, 'CREATED_CHAT'),
    );

    const user = await getUser(username);
    const messageResponse = await Database.message.addMessage({
        chatId: chat.id,
        username: 'SYSTEM',
        message: `Grupo criado por ${user.name}`,
        messageId: nanoid(),
        createdAt: Date.now(),
        status: 'SENT',
    });
    await addMessageAll(messageResponse);

    return chat;
}

async function addMessage({
    chatId,
    username,
    message,
    createdAt,
    messageId,
    hasAudio,
    recipient,
}) {
    console.log('=========== SEND MESSAGE');
    console.log(recipient);
    let id = chatId;
    if (!chatId && recipient) {
        const chat = await createChat(username, recipient);
        id = chat.id;
    }

    console.log(id);

    const messageResponse = await Database.message.addMessage({
        chatId: id,
        username,
        message,
        messageId,
        createdAt: +createdAt,
        status: 'SENT',
        hasAudio,
    });

    console.log('====================== ADD MESSAGE');
    console.log(messageResponse);

    if (hasAudio) {
        messageResponse.message = S3.getObject(messageResponse.message);
    }
    const chatConfig = await Database.chat.getChat(id);

    const users = await Database.chat.fetchChatUsers(id);
    const chatUsers = users.filter(u => u !== username);

    const currentUserChat = chatConfig.private
        ? await getChat(
              id,
              username,
              chatConfig.private
                  .split(':')
                  .filter(member => member !== username),
          )
        : await getChat(id, username);

    await Promise.all(
        chatUsers.map(async memberUsername => {
            await Database.message.newMessages(id, memberUsername);
            const memberChat = chatConfig.private
                ? await getChat(id, memberUsername, username)
                : await getChat(id, memberUsername);

            await sendWebSocketMessage(
                memberUsername,
                messageResponse,
                'NEW_MESSAGE',
            );
            await sendWebSocketMessage(
                memberUsername,
                memberChat,
                'CHAT_UPDATED',
            );
        }),

        sendWebSocketMessage(username, messageResponse, 'MESSAGE_SENT'),
        sendWebSocketMessage(username, currentUserChat, 'CHAT_UPDATED'),
    );
}

async function getMessages(chatId) {
    const messages = await Database.message.fetchMessages(chatId);
    return messages.map(message => {
        const item = { ...message };
        if (item.hasAudio) {
            item.message = S3.getObject(message.message);
        }

        return item;
    });
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

    await sendWebSocketMessage(username, chat, 'CHAT_UPDATED');
}

async function getGroupMembers(chatId) {
    const chatUsers = await Database.chat.fetchChatUsers(chatId);

    const promises = [];

    chatUsers.forEach(username => {
        promises.push(getUser(username));
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
    const user = await getUser(username);
    const messageResponse = await Database.message.addMessage({
        chatId,
        username: 'SYSTEM',
        message: `${user.name} foi adicionado do grupo`,
        messageId: nanoid(),
        createdAt: Date.now(),
        status: 'SENT',
    });
    await addMessageAll(messageResponse);

    return true;
}

async function removeMember(chatId, username) {
    const chatConfig = await Database.chat.getChat(chatId);
    const user = await getUser(username);
    await Database.chat.removeMember(chatId, username);
    await sendWebSocketMessage(username, { chatId }, 'CHAT_REMOVED');

    const messageResponse = await Database.message.addMessage({
        chatId,
        username: 'SYSTEM',
        message: `${user.name} foi removido do grupo`,
        messageId: nanoid(),
        createdAt: Date.now(),
        status: 'SENT',
    });

    const users = await Database.chat.fetchChatUsers(chatId);
    if (users.length && chatConfig.admin === username) {
        await Database.chat.updateAdmin(chatId, users[0]);
    }

    await addMessageAll(messageResponse);
    return true;
}

async function updateGroup(chatId, username, { groupName, admin }) {
    await Database.chat.updateGroup(chatId, { groupName, admin });
    const chat = await getChat(chatId, username);

    const users = await Database.chat.fetchChatUsers(chatId);

    const promises = [];
    users.forEach(user => {
        promises.push(sendWebSocketMessage(user, chat, 'CHAT_UPDATED'));
    });

    await Promise.all(promises);
    return chat;
}

async function addGroupAvatar(chatId, username, avatar) {
    await Database.chat.updateGroupAvatar(chatId, avatar);
    const chat = await getChat(chatId, username);
    const users = await Database.chat.fetchChatUsers(chatId);

    const promises = [];
    users.forEach(user => {
        promises.push(sendWebSocketMessage(user, chat, 'CHAT_UPDATED'));
    });

    await Promise.all(promises);
    return chat;
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
    addGroupAvatar,
    updateGroup,
};
