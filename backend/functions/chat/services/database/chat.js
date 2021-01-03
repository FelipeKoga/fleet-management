const { nanoid } = require('nanoid');
const {
    fetchByPK,
    fetchBySK,
    getByPK,
    insert,
    update,
    remove,
} = require('./query');

async function fetchUserChats(username) {
    return fetchBySK({
        ExpressionAttributeValues: {
            ':pk': 'CHAT#',
            ':sk': `MEMBER#${username}`,
        },
    });
}

async function getPrivateChat(username, withUsername) {
    const userChatsMetadata = await fetchUserChats(username);

    if (!userChatsMetadata) return null;

    const promises = [];
    userChatsMetadata.forEach(userChat => {
        promises.push(
            getByPK({
                FilterExpression: 'contains(#private, :user)',
                ExpressionAttributeNames: {
                    '#private': 'private',
                },
                ExpressionAttributeValues: {
                    ':pk': `CHAT#${userChat.id}`,
                    ':sk': `CONFIG`,
                    ':user': withUsername,
                },
            }),
        );
    });

    const response = await Promise.all(promises);

    const chat = response.find(res => res);

    return chat ? chat.id : null;
}

async function getUserChatMetadata(chatId, username) {
    return getByPK({
        ExpressionAttributeValues: {
            ':pk': `CHAT#${chatId}`,
            ':sk': `MEMBER#${username}`,
        },
    });
}

async function addMember(chatId, username) {
    await insert({
        partitionKey: `CHAT#${chatId}`,
        sortKey: `MEMBER#${username}`,
        newMessages: 0,
    });
}

async function removeMember(chatId, username) {
    await remove({
        partitionKey: `CHAT#${chatId}`,
        sortKey: `MEMBER#${username}`,
    });
}

async function createChat(args) {
    const chatId = nanoid();
    await insert({
        partitionKey: `CHAT#${chatId}`,
        sortKey: `CONFIG`,
        ...args,
    });
    return chatId;
}

async function fetchChatUsers(chatId) {
    const chatUsers = await fetchByPK({
        ExpressionAttributeValues: {
            ':pk': `CHAT#${chatId}`,
            ':sk': 'MEMBER#',
        },
    });

    return chatUsers.map(item => {
        return item.sortKey;
    });
}

async function getChat(chatId) {
    const { sortKey, ...chat } = await getByPK({
        ExpressionAttributeValues: {
            ':pk': `CHAT#${chatId}`,
            ':sk': 'CONFIG',
        },
    });

    return chat;
}

async function updateAdmin(chatId, admin) {
    await update({
        Key: {
            partitionKey: `CHAT#${chatId}`,
            sortKey: `CONFIG`,
        },
        UpdateExpression: 'set admin = :admin',
        ExpressionAttributeValues: {
            ':admin': admin,
        },
    });
}

module.exports = {
    fetchChatUsers,
    fetchUserChats,
    getPrivateChat,
    getUserChatMetadata,
    getChat,
    createChat,
    addMember,
    updateAdmin,
    removeMember,
};
