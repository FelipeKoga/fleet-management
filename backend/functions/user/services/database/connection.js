const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();

async function getCompanyIDs(companyId) {
    const { Items } = await docClient
        .query({
            TableName: process.env.CONNECTION_TABLE,
            IndexName: 'companyIdIndex',
            KeyConditionExpression: 'companyId = :cId',
            ExpressionAttributeValues: {
                ':c': companyId,
            },
            ProjectionExpression: 'connectionId',
        })
        .promise();

    return Items.map(item => item.connectionId);
}

async function getUserIDs(username) {
    const { Items } = await docClient
        .query({
            TableName: process.env.CONNECTION_TABLE,
            IndexName: 'usernameIndex',
            KeyConditionExpression: 'username = :u',
            ExpressionAttributeValues: {
                ':u': username,
            },
            ProjectionExpression: 'connectionId',
        })
        .promise();

    return Items.map(item => item.connectionId);
}

async function removeUserIDs(username) {
    const connectionIds = await getUserIDs(username);
    const requests = [];
    const params = {
        TableName: process.env.CONNECTION_TABLE,
    };
    connectionIds.forEach(connectionId => {
        requests.push(
            docClient
                .delete({
                    ...params,
                    Key: {
                        connectionId,
                    },
                })
                .promise(),
        );
    });
    await Promise.all(requests);
}

module.exports = {
    getCompanyIDs,
    removeUserIDs,
};
