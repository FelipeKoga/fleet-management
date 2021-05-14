const DynamoDB = require('aws-sdk/clients/dynamodb');

const dynamoDB = new DynamoDB.DocumentClient();
const BaseParams = {
    TableName: process.env.TABLE,
    KeyConditionExpression: 'partitionKey = :pk and begins_with(sortKey, :sk)',
};

const getKey = key => {
    return key.split('#').pop();
};
const formatResponse = (items, mapKey = true) => {
    return items.map(item => {
        const { partitionKey, sortKey, ...data } = item;
        return {
            id: mapKey ? getKey(partitionKey) : undefined,
            sortKey: mapKey ? getKey(sortKey) : undefined,
            ...data,
        };
    });
};

const fetchByPK = async (params, mapKey) => {
    const { Items } = await dynamoDB
        .query({
            ...BaseParams,
            ...params,
        })
        .promise();

    return formatResponse(Items, mapKey);
};

async function fetchChatUsers(chatId) {
    const chatUsers = await fetchByPK({
        ExpressionAttributeValues: {
            ':pk': `CHAT#${chatId}`,
            ':sk': 'MEMBER#',
        },
    });

    return chatUsers.map(item => {
        return item.sortKey;
    });
}

async function fetchConnectionIds(username) {
    const { Items } = await dynamoDB
        .query({
            TableName: process.env.TABLE,
            KeyConditionExpression:
                'partitionKey = :pk and begins_with(sortKey, :sk)',
            ExpressionAttributeValues: {
                ':pk': `USER#${username}`,
                ':sk': 'CONNECTION#',
            },
            ProjectionExpression: 'connectionId',
        })
        .promise();

    return Items.map(res => res.connectionId);
}

module.exports = {
    fetchChatUsers,
    fetchConnectionIds,
};
