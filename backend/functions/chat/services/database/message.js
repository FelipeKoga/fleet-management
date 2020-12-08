const { DynamoDB } = require('aws-sdk');
const { getUserChatMetadata } = require('./chat');

const docClient = new DynamoDB.DocumentClient();

async function getMessages(chatId) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        KeyConditionExpression:
            'chatId = :cId and begins_with(chatSortKey, :sk)',
        ScanIndexForward: true,
        ExpressionAttributeValues: {
            ':cId': chatId,
            ':sk': 'MESSAGE#',
        },
    };
    const { Items } = await docClient.query(params).promise();
    return Items;
}

async function addMessage(chatId, username, message, timestamp) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        Item: {
            chatId,
            chatSortKey: `MESSAGE#${timestamp}`,
            username,
            message,
            timestamp,
        },
    };

    await docClient.put(params).promise();
}

async function viewedMessages(chatId, username) {
    const chat = await getUserChatMetadata(chatId, username);
    const params = {
        TableName: process.env.CHAT_TABLE,
        Key: {
            chatId,
            chatSortKey: chat.chatSortKey,
        },
        UpdateExpression: 'set newMessages = :nm',
        ExpressionAttributeValues: {
            ':nm': 0,
        },
    };
    await docClient.update(params).promise();
}

async function newMessages(chatId, username, timestamp) {
    const chat = await getUserChatMetadata(chatId, username);
    const params = {
        TableName: process.env.CHAT_TABLE,
        Key: {
            chatId,
            chatSortKey: chat.chatSortKey,
        },
        UpdateExpression:
            'set newMessages = newMessages + :nm, newMessageTimestamp = :tp ',
        ExpressionAttributeValues: {
            ':nm': 1,
            ':tp': timestamp,
        },
    };
    await docClient.update(params).promise();
}

async function getLastMessage(chatId) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        KeyConditionExpression:
            'chatId = :cId and begins_with(chatSortKey, :sk)',
        ScanIndexForward: false,
        ExpressionAttributeValues: {
            ':cId': chatId,
            ':sk': 'MESSAGE#',
        },
        Limit: 1,
    };
    const { Items } = await docClient.query(params).promise();
    return Items[0] || {};
}

module.exports = {
    getMessages,
    addMessage,
    viewedMessages,
    newMessages,
    getLastMessage,
};
