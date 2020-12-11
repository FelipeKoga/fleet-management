const DynamoDB = require('aws-sdk/clients/dynamodb');
const { removeUserIDs } = require('./connection');

const docClient = new DynamoDB.DocumentClient();

async function get(username) {
    const { Items } = await docClient
        .query({
            TableName: process.env.USER_TABLE,
            KeyConditionExpression:
                'username = :u and begins_with(userSortKey, :sk)',
            ExpressionAttributeValues: {
                ':u': username,
                ':sk': 'config',
            },
            ProjectionExpression:
                'username, fullName, email, avatar, companyId, settings, phone',
        })
        .promise();

    return Items[0];
}

async function list(companyId) {
    const { Items } = await docClient
        .query({
            TableName: process.env.USER_TABLE,
            IndexName: 'userSortKeyIndex',
            KeyConditionExpression: 'userSortKey = :sk',
            ExpressionAttributeValues: {
                ':sk': `config#${companyId}`,
            },
            ProjectionExpression:
                'username, fullName, email, avatar, companyId, settings, phone',
        })
        .promise();
    return Items;
}

async function create(data, companyId) {
    const { password, ...user } = {
        username: data.email,
        ...data,
    };

    await docClient
        .put({
            TableName: process.env.USER_TABLE,
            Item: {
                ...user,
                userSortKey: `config#${companyId}`,
                companyId,
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
        TableName: process.env.USER_TABLE,
        Key: {
            username,
            userSortKey: `config#${companyId}`,
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
        TableName: process.env.USER_TABLE,
        Key: {
            username,
            userSortKey: `config#${companyId}`,
        },
    };
    await docClient.delete(params).promise();
    await removeUserIDs(username);
    return true;
}

module.exports = {
    get,
    list,
    create,
    update,
    remove,
};
