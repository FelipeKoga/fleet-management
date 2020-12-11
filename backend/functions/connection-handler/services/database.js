const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();

async function getConnectionIds(companyId) {
    const params = {
        TableName: process.env.USER_TABLE,
        IndexName: 'companyIdIndex',
        KeyConditionExpression: 'companyId = :cId',
        FilterExpression: 'begins_with(userSortKey, :sk)',
        ExpressionAttributeValues: {
            ':cId': companyId,
            ':sk': 'connection',
        },
        ProjectionExpression: 'userSortKey',
    };
    const { Items } = await docClient.query(params).promise();
    return Items.map(({ userSortKey }) => userSortKey.split('_').pop());
}

async function getUsernameByConnectionId(connectionId) {
    const params = {
        TableName: process.env.USER_TABLE,
        IndexName: 'userSortKeyIndex',
        KeyConditionExpression: 'userSortKey = :sk',
        ExpressionAttributeValues: {
            ':sk': `connection#${connectionId}`,
        },
    };
    const { Items } = await docClient.query(params).promise();

    return Items[0];
}

async function getUser(username) {
    const params = {
        TableName: process.env.USER_TABLE,
        KeyConditionExpression:
            'username = :u and begins_with(userSortKey, :sk)',
        ExpressionAttributeValues: {
            ':u': username,
            ':sk': 'config#',
        },
        ProjectionExpression: 'username, customName, email, companyId',
    };
    const { Items } = await docClient.query(params).promise();

    return Items[0];
}

async function insertConnectionId({ username, companyId, connectionId }) {
    const params = {
        TableName: process.env.USER_TABLE,
        Item: {
            username,
            userSortKey: `connection#${connectionId}`,
            companyId,
        },
    };
    await docClient.put(params).promise();
    return true;
}

async function deleteConnectionId(username, connectionId) {
    const params = {
        TableName: process.env.USER_TABLE,
        Key: {
            username,
            userSortKey: `connection#${connectionId}`,
        },
    };

    await docClient.delete(params).promise();
    return true;
}

module.exports = {
    getConnectionIds,
    getUser,
    insertConnectionId,
    deleteConnectionId,
    getUsernameByConnectionId,
};
