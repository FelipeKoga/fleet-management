const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();

async function getConnectionIDs(username, companyId) {
    const params = {
        TableName: process.env.CONNECTION_TABLE,
        IndexName: 'companyIdIndex',
        KeyConditionExpression: 'companyId = :cId',
        ExpressionAttributeValues: {
            ':cId': companyId,
        },
        ProjectionExpression: 'connectionId',
    };
    const { Items } = await docClient.query(params).promise();

    const connectionIds = [];
    Items.forEach(item => {
        if (item.username !== username) connectionIds.push(item.connectionId);
    });
    return connectionIds;
}

async function getUsernameByConnectionID(connectionId) {
    const params = {
        TableName: process.env.CONNECTION_TABLE,
        KeyConditionExpression: 'connectionId = :cId',
        ExpressionAttributeValues: {
            ':cId': connectionId,
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

    if (Items[0]) {
        return Items[0];
    }

    throw new Error('User not found');
}

async function insertConnectionId({ username, companyId, connectionId }) {
    const params = {
        TableName: process.env.CONNECTION_TABLE,
        Item: {
            username,
            connectionId,
            companyId,
        },
    };
    await docClient.put(params).promise();
    return true;
}

async function deleteConnectionId(connectionId) {
    const params = {
        TableName: process.env.CONNECTION_TABLE,
        Key: {
            connectionId,
        },
    };

    await docClient.delete(params).promise();
    return true;
}

module.exports = {
    getConnectionIDs,
    getUser,
    insertConnectionId,
    deleteConnectionId,
    getUsernameByConnectionID,
};
