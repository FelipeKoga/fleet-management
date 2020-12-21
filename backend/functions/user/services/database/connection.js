const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();

function mapSortKey(items) {
    return items.map(({ sortKey }) => sortKey.split('#').pop());
}

async function getCompanyIDs(companyId) {
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

        connectionIds = [
            ...connectionIds,
            ...mapSortKey(userConnections.Items),
        ];
    });

    await Promise.all(promises);

    return connectionIds;
}

async function getUserIDs(username) {
    const { Items } = await docClient
        .query({
            TableName: process.env.TABLE,
            KeyConditionExpression:
                'partitionKey = :pk and begins_with(sortKey, :sk)',
            ExpressionAttributeValues: {
                ':pk': `USER#${username}`,
                ':sk': 'CONNECTION#',
            },
        })
        .promise();

    return mapSortKey(Items);
}

async function removeUserIDs(username) {
    const connectionIds = await getUserIDs(username);
    const requests = [];
    const params = {
        TableName: process.env.TABLE,
    };
    connectionIds.forEach(connectionId => {
        requests.push(
            docClient
                .delete({
                    ...params,
                    Key: {
                        partitionKey: `USER#${username}`,
                        sortKey: `CONNECTION#${connectionId}`,
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
