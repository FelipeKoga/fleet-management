const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();

function mapConnectionResponse(items) {
    return items.map(({ userSortKey }) => userSortKey.split('#').pop());
}

async function getCompanyIDs(companyId) {
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
    return mapConnectionResponse(Items);
}

async function getUserIDs(username) {
    const { Items } = await docClient
        .query({
            TableName: process.env.USER_TABLE,
            KeyConditionExpression:
                'username = :u and begins_with(userSortKey, :sk)',
            ExpressionAttributeValues: {
                ':u': username,
                ':sk': 'connection#',
            },
            ProjectionExpression: 'connectionId',
        })
        .promise();

    return mapConnectionResponse(Items);
}

async function removeUserIDs(username) {
    const connectionIds = await getUserIDs(username);
    const requests = [];
    const params = {
        TableName: process.env.USER_TABLE,
    };
    connectionIds.forEach(connectionId => {
        requests.push(
            docClient
                .delete({
                    ...params,
                    Key: {
                        username,
                        userSortKey: `connection#${connectionId}`,
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
