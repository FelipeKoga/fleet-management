const { DynamoDB } = require('aws-sdk');

const docClient = new DynamoDB.DocumentClient();

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

module.exports = {
    getUser,
};
