const DynamoDB = require('aws-sdk/clients/dynamodb');
const { removeUserIDs } = require('./connection');

const docClient = new DynamoDB.DocumentClient();

async function get(username) {
    const { Items } = await docClient
        .query({
            TableName: process.env.USER_TABLE,
            KeyConditionExpression: 'username = :u',
            ExpressionAttributeValues: {
                ':u': username,
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
            TableName: process.env.USER_TABLE,
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
        ...data,
    };

    await docClient
        .put({
            TableName: process.env.USER_TABLE,
            Item: {
                ...user,
                companyId,
            },
        })
        .promise();
}

async function update({ customName, phone, email, settings }, username) {
    const params = {
        TableName: process.env.USER_TABLE,
        Key: {
            username,
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
        TableName: process.env.USER_TABLE,
        Key: {
            username,
        },
    };
    await docClient.delete(params).promise();
    await removeUserIDs(username);
    return true;
    // await removeUserLocations(username);
}

module.exports = {
    get,
    list,
    create,
    update,
    remove,
};
