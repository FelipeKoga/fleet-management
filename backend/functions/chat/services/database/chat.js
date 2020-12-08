const { DynamoDB } = require('aws-sdk');
const { v4 } = require('uuid');

const docClient = new DynamoDB.DocumentClient();

async function getUserChats(username) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        IndexName: 'chatSortKeyIndex',
        KeyConditionExpression: 'chatSortKey = :csk',
    };
    const { Items } = await docClient
        .query({
            ...params,
            ExpressionAttributeValues: {
                ':csk': `MEMBER#${username}`,
            },
        })
        .promise();

    const chats = Items.sort((a, b) => {
        if (a.messageTimestamp < b.messageTimestamp) return -1;

        if (a.messageTimestamp > b.messageTimestamp) return 1;

        return 0;
    });

    return chats;
}

async function getUserChatMetadata(chatId, username) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        KeyConditionExpression:
            'chatId = :cId and begins_with(chatSortKey, :sk)',
        ExpressionAttributeValues: {
            ':cId': chatId,
            ':sk': `MEMBER#${username}`,
        },
    };
    const { Items } = await docClient.query(params).promise();
    return Items[0] || {};
}

async function addMember(chatId, username) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        Item: {
            chatId,
            chatSortKey: `MEMBER#${username}`,
            newMessages: 0,
        },
    };
    await docClient.put(params).promise();
}

async function createChat(args) {
    const chatId = v4();
    const params = {
        TableName: process.env.CHAT_TABLE,
        Item: {
            chatId,
            chatSortKey: `CONFIG`,
            ...args,
        },
    };

    await docClient.put(params).promise();
    return chatId;
}

async function getChatUsers(chatId) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        KeyConditionExpression:
            'chatId = :cId and begins_with(chatSortKey, :sk)',
        ExpressionAttributeValues: {
            ':cId': chatId,
            ':sk': 'MEMBER#',
        },
    };
    const { Items } = await docClient.query(params).promise();
    return Items.map(item => {
        return item.chatSortKey.split('#').pop();
    });
}

async function getChat(chatId) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        KeyConditionExpression:
            'chatId = :cId AND begins_with(chatSortKey, :sk)',
        ExpressionAttributeValues: {
            ':cId': chatId,
            ':sk': 'CONFIG',
        },
    };
    const { Items } = await docClient.query(params).promise();
    return Items[0];
}

async function getPrivateChats(username) {
    const userChats = await getUserChats(username);
    const chats = [];
    await Promise.all(
        userChats.map(async ({ chatId }) => {
            chats.push(await getChat(chatId));
        }),
    );

    return chats.filter(chat => chat.isPrivate);
}

module.exports = {
    getPrivateChats,
    getChatUsers,
    createChat,
    addMember,
    getUserChatMetadata,
    getUserChats,
    getChat,
};
