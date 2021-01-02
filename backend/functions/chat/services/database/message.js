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

async function getMessage(partitionKey, sortKey) {
    return getByPK(
        {
            ExpressionAttributeValues: {
                ':pk': partitionKey,
                ':sk': sortKey,
            },
        },
        true,
    );
}

async function addMessage({
    chatId,
    username,
    message,
    messageId,
    createdAt,
    status,
}) {
    const payload = {
        partitionKey: `CHAT#${chatId}`,
        sortKey: `MESSAGE#${createdAt}#${messageId}`,
        username,
        message,
        createdAt,
        status,
    };
    await insert(payload);

    const { sortKey, ...newMessage } = await getMessage(
        payload.partitionKey,
        payload.sortKey,
    );

    return {
        ...newMessage,
        messageId: sortKey,
    };
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
