const { fetchByPK, getByPK, insert, update } = require('./query');

async function fetchMessages(chatId) {
    const messages = await fetchByPK(
        {
            ExpressionAttributeValues: {
                ':pk': `CHAT#${chatId}`,
                ':sk': 'MESSAGE#',
            },
            ScanIndexForward: true,
        },
        true,
    );
    return messages.map(item => {
        const { id, sortKey, ...message } = item;
        const messageId = sortKey;
        return {
            messageId,
            chatId: id,
            ...message,
        };
    });
}

async function getMessage(partitionKey, sortKey) {
    const response = await getByPK(
        {
            ExpressionAttributeValues: {
                ':pk': partitionKey,
                ':sk': sortKey,
            },
        },
        true,
    );

    const { id, ...rest } = response;
    return { chatId: id, ...rest };
}

async function addMessage({
    chatId,
    username,
    message,
    messageId,
    createdAt,
    status,
    hasAudio,
}) {
    const payload = {
        partitionKey: `CHAT#${chatId}`,
        sortKey: `MESSAGE#${createdAt}#${messageId}`,
        username,
        message,
        createdAt,
        status,
        hasAudio,
    };
    await insert(payload);

    const { sortKey, id, ...newMessage } = await getMessage(
        payload.partitionKey,
        payload.sortKey,
    );

    return {
        ...newMessage,
        chatId,
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

async function newMessages(chatId, username) {
    await update({
        Key: {
            partitionKey: `CHAT#${chatId}`,
            sortKey: `MEMBER#${username}`,
        },
        UpdateExpression: 'set newMessages = newMessages + :nm',
        ExpressionAttributeValues: {
            ':nm': 1,
        },
    });
}

async function getLastMessage(chatId) {
    const response = await getByPK({
        ScanIndexForward: false,
        ExpressionAttributeValues: {
            ':pk': `CHAT#${chatId}`,
            ':sk': 'MESSAGE#',
        },
        Limit: 1,
    });

    if (!response) return null;

    const { id, ...rest } = response;
    return { chatId: id, ...rest };
}

module.exports = {
    fetchMessages,
    getLastMessage,
    addMessage,
    viewedMessages,
    newMessages,
};
