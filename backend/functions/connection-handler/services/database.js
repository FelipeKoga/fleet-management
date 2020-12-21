const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();

function mapSortKey(items) {
    return items.map(({ sortKey }) => sortKey.split('#').pop());
}

async function getConnectionIds(companyId) {
    const params = {
        TableName: process.env.TABLE,
    };

    const users = await docClient
        .query({
            ...params,
            IndexName: 'sortKeyIndex',
            KeyConditionExpression:
                'sortKey = :sk and begins_with(partitionKey, :pk)',
            ExpressionAttributeValues: {
                ':sk': `CONFIG#${companyId}`,
                ':pk': 'USER#',
            },
        })
        .promise();

    let connectionIds = [];

    const promises = users.Items.map(async user => {
        const userConnections = await docClient
            .query({
                ...params,
                KeyConditionExpression:
                    'partitionKey = :pk and begins_with(sortKey, :sk)',
                ExpressionAttributeValues: {
                    ':sk': `CONNECTION#`,
                    ':pk': `USER#${user.partitionKey.split('#').pop()}`,
                },
            })
            .promise();

        console.log(userConnections);

        connectionIds = [
            ...connectionIds,
            ...mapSortKey(userConnections.Items),
        ];
    });

    await Promise.all(promises);

    return connectionIds;
}

async function getUsernameByConnectionId(connectionId) {
    const params = {
        TableName: process.env.TABLE,
        IndexName: 'sortKeyIndex',
        KeyConditionExpression:
            'sortKey = :sk and begins_with(partitionKey, :pk)',
        ExpressionAttributeValues: {
            ':sk': `CONNECTION#${connectionId}`,
            ':pk': `USER#`,
        },
    };

    const { Items } = await docClient.query(params).promise();

    const connection = Items[0];

    if (connection) return connection.partitionKey.split('#').pop();

    return {};
}

async function getUser(username) {
    const params = {
        TableName: process.env.TABLE,
        KeyConditionExpression:
            'partitionKey = :pk and begins_with(sortKey, :sk)',
        ExpressionAttributeValues: {
            ':pk': `USER#${username}`,
            ':sk': 'CONFIG#',
        },
    };
    const { Items } = await docClient.query(params).promise();

    const user = Items[0];

    if (user) {
        const { sortKey, ...data } = user;

        return {
            companyId: sortKey.split('#').pop(),
            ...data,
        };
    }
    return {};
}

async function insertConnectionId({ username, connectionId }) {
    const params = {
        TableName: process.env.TABLE,
        Item: {
            partitionKey: `USER#${username}`,
            sortKey: `CONNECTION#${connectionId}`,
        },
    };
    await docClient.put(params).promise();
    return true;
}

async function deleteConnectionId(username, connectionId) {
    const params = {
        TableName: process.env.TABLE,
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONNECTION#${connectionId}`,
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
