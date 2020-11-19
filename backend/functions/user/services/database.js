const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();

const UserTable = process.env.USER_TABLE;
const ConnectionTable = process.env.CONNECTION_TABLE;

async function get(username) {
    const { Items } = await docClient
        .query({
            TableName: UserTable,
            KeyConditionExpression:
                'username = :u and begins_with(sortKey, :sk)',
            ExpressionAttributeValues: {
                ':u': username,
                ':sk': `METADATA`,
            },
            ProjectionExpression:
                'username, fullName, email, avatar, companyId, settings, phone',
        })
        .promise();

    if (Items.length) {
        return Items[0];
    }
    throw new Error('User not found');
}

async function list(companyId) {
    const { Items } = await docClient
        .query({
            TableName: UserTable,
            IndexName: 'companyIdIndex',
            KeyConditionExpression: 'companyId = :cId',
            ExpressionAttributeValues: {
                ':cId': companyId,
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
        sortKey: `METADATA`,
        ...data,
    };

    await docClient
        .put({
            TableName: UserTable,
            Item: {
                ...user,
                companyId,
            },
        })
        .promise();
}

async function update({ customName, phone, email, settings }, username) {
    const params = {
        TableName: UserTable,
        Key: {
            username,
            sortKey: `METADATA`,
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

async function remove(username) {
    const params = {
        TableName: UserTable,
        Key: {
            username,
        },
    };
    await docClient
        .delete({
            ...params,
            Key: { ...params.Key, sortKey: 'METADATA' },
        })
        .promise();
    await docClient
        .delete({
            ...params,
            Key: { ...params.Key, sortKey: 'CONNECTION' },
        })
        .promise();
    await docClient
        .delete({
            ...params,
            Key: { ...params.Key, sortKey: 'LOCATION' },
        })
        .promise();
}

async function listConnectionIds(companyId) {
    const params = {
        TableName: ConnectionTable,
    };
}

module.exports = {
    get,
    list,
    create,
    update,
    remove,
};
