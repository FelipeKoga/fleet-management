const { nanoid } = require('nanoid');
const { fetchByPK, getByPK, insert, update } = require('./query');

async function fetchMessages(chatId) {
    const messages = await fetchByPK({
        ExpressionAttributeValues: {
            ':pk': `CHAT#${chatId}`,
            ':sk': 'MESSAGE#',
        },
        ScanIndexForward: true,
    });
    return messages.map(item => {
        const { sortKey, ...message } = item;
        const messageId = sortKey;
        return {
            messageId,
            ...message,
        };
    });
}

async function addMessage(chatId, username, message, createdAt, status) {
    const payload = {
        partitionKey: `CHAT#${chatId}`,
        sortKey: `MESSAGE#${createdAt}#${nanoid(4)}`,
        username,
        message,
        createdAt,
        status,
    };
    await insert(payload);
    return payload;
}

async function viewedMessages(chatId, username) {
    await update({
        Key: {
            partitionKey: `CHAT#${chatId}`,
            sortKey: `MEMBER#${username}`,
        },
        UpdateExpression: 'set newMessages = :nm',
        ExpressionAttributeValues: {
            ':nm': 0,
        },
    });
}

async function newMessages(chatId, username, createdAt) {
    await update({
        Key: {
            partitionKey: `CHAT#${chatId}`,
            sortKey: `MEMBER#${username}`,
        },
        UpdateExpression:
            'set newMessages = newMessages + :nm, createdAt = :ca ',
        ExpressionAttributeValues: {
            ':nm': 1,
            ':ca': createdAt,
        },
    });
}

async function getLastMessage(chatId) {
    return getByPK({
        ScanIndexForward: false,
        ExpressionAttributeValues: {
            ':pk': `CHAT#${chatId}`,
            ':sk': 'MESSAGE#',
        },
        Limit: 1,
    });
}

module.exports = {
    fetchMessages,
    getLastMessage,
    addMessage,
    viewedMessages,
    newMessages,
};
