const DynamoDB = require('aws-sdk/clients/dynamodb');
const { sendMessage } = require('./invoke');

const docClient = new DynamoDB.DocumentClient();
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
    const { Items } = await docClient
        .query({
            ...BaseParams,
            ...params,
        })
        .promise();

    return formatResponse(Items, mapKey);
};

async function fetchConnectionIds(username) {
    const connectionIds = await fetchByPK(
        {
            ExpressionAttributeValues: {
                ':pk': `USER#${username}`,
                ':sk': 'CONNECTION#',
            },
            ProjectionExpression: 'connectionId',
        },
        false,
    );

    return connectionIds.map(res => res.connectionId);
}

exports.handler = async event => {
    const payload = JSON.parse(event.body);

    const {
        type,
        chatId,
        username,
        inputData,
        outputData,
        length,
    } = payload.body;
    const connectionids = await fetchConnectionIds(username);

    let body;
    let action;
    if (type === 'START_PUSH_TO_TALK') {
        action = 'STARTED_PUSH_TO_TALK';
        body = {
            chatId,
            username,
        };
    } else if (type === 'STOP_PUSH_TO_TALK') {
        action = 'STOPPED_PUSH_TO_TALK';
        body = {
            chatId,
            username,
        };
    } else {
        action = 'RECEIVED_PUSH_TO_TALK';

        body = {
            chatId,
            username,
            inputData,
            outputData,
            length,
        };
    }

    await sendMessage(body, connectionids, action);

    return {
        statusCode: 200,
    };
};
