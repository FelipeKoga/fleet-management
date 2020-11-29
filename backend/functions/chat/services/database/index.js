const { DynamoDB } = require('aws-sdk');
const { v4 } = require('uuid');

const docClient = new DynamoDB.DocumentClient();

async function getUserChats(username) {
    const params = {
        TableName: process.env.USER_CHAT_TABLE,
        IndexName: 'usernameIndex',
        KeyConditionExpression: 'username = :u',
        ExpressionAttributeValues: {
            ':u': username,
        },
        ProjectionExpression: 'chatId',
    };

    const { Items } = await docClient.query(params).promise();

    return Items.map(item => item.chatId);
}

async function getChatUsers(chatId) {
    const params = {
        TableName: process.env.USER_CHAT_TABLE,
        IndexName: 'chatIdIndex',
        KeyConditionExpression: 'chatId = :c',
        ExpressionAttributeValues: {
            ':c': chatId,
        },
        ProjectionExpression: 'username',
    };

    const { Items } = await docClient.query(params).promise();

    return Items.map(item => item.username);
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

async function getUser(username) {
    const params = {
        TableName: process.env.USER_TABLE,
        KeyConditionExpression: 'username = :u',
        ExpressionAttributeValues: {
            ':u': username,
        },
    };
    const { Items } = await docClient.query(params).promise();
    return Items[0];
}

// async function createUserChat(username, chatId) {
//     const params = {
//         TableName: process.env.USER_CHAT_TABLE,
//         Item: {
//             userChatId: v4(),
//             username,
//             chatId,
//         },
//     };

//     await docClient.put(params).promise();
// }


/**
 
    getChats(username) => MEMBER#username GSI => chatIds => 



 */

async function createChat() {
    const chatId = v4();
    const params = {
        TableName: process.env.CHAT_TABLE,
        Item: {
            chatId,
            chatSortKey: 'CONFIG',
            isPrivate: true,
        },
    };

    await docClient.put(params).promise();
    return chatId;
}

async function createGroup(groupName, admin) {
    const groupId = v4();
    const params = {
        TableName: process.env.CHAT_TABLE,
        Item: {
            chatId: groupId,
            chatSortKey: 'CONFIG',
            isPrivate: false,
            admin,
            groupName,
        },
    };

    await docClient.put(params).promise();
    return groupId;
}

async function getMessages(chatId) {
    console.log(chatId);
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
    return Items[0];
}

const zonedTime = date => {
    const localeFormat = date.toLocaleString('en-US', {
        timeZone: 'America/Sao_Paulo',
    });
    return new Date(localeFormat);
};

async function addMessage(chatId, username, message) {
    const params = {
        TableName: process.env.CHAT_TABLE,
        Item: {
            chatId,
            chatSortKey: `MESSAGE#${+zonedTime(new Date())}`,
            username,
            message,
        },
    };

    await docClient.put(params).promise();
}

module.exports = {
    getUser,
    getChat,
    getUserChats,
    getChatUsers,
    createUserChat,
    createChat,
    createGroup,
    getMessages,
    addMessage,
    getLastMessage,
};
