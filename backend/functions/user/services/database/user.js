const DynamoDB = require('aws-sdk/clients/dynamodb');
const { removeUserIDs } = require('./connection');

const docClient = new DynamoDB.DocumentClient();

async function get(username, companyId) {
    const { Items } = await docClient
        .query({
            TableName: process.env.TABLE,
            KeyConditionExpression:
                'partitionKey = :pk and begins_with(sortKey, :sk)',
            ExpressionAttributeValues: {
                ':pk': `USER#${username}`,
                ':sk': `CONFIG#${companyId}`,
            },
            ProjectionExpression:
                'username, fullName, email, avatar, settings, phone',
        })
        .promise();

    const user = Items[0];

    if (user) {
        return {
            ...user,
            companyId,
        };
    }

    return {};
}

async function list(companyId) {
    const { Items } = await docClient
        .query({
            TableName: process.env.TABLE,
            IndexName: 'sortKeyIndex',
            KeyConditionExpression: 'sortKey = :sk',
            ExpressionAttributeValues: {
                ':sk': `CONFIG#${companyId}`,
            },
            ProjectionExpression:
                'username, fullName, email, avatar, companyId, settings, phone',
        })
        .promise();
    return Items;
}

async function create(data, companyId) {
    const { password, ...user } = data;

    await docClient
        .put({
            TableName: process.env.TABLE,
            Item: {
                ...user,
                partitionKey: `USER#${data.email}`,
                sortKey: `CONFIG#${companyId}`,
            },
        })
        .promise();
}

async function update(
    { customName, phone, email, settings },
    username,
    companyId,
) {
    const params = {
        TableName: process.env.TABLE,
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
        UpdateExpression:
            'set customName = :cn, phone = :p, email = :e, settings = :s',
        ExpressionAttributeValues: {
            ':cn': customName,
            ':p': phone,
            ':e': email,
            ':s': settings,
        },
    };

    await docClient.update(params).promise();
}

async function remove(username, companyId) {
    const params = {
        TableName: process.env.TABLE,
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
    };
    await removeUserIDs(username);
    await docClient.delete(params).promise();
    return true;
}

module.exports = {
    get,
    list,
    create,
    update,
    remove,
};
