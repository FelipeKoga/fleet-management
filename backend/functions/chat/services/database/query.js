const DynamoDB = require('aws-sdk/clients/dynamodb');

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

const fetchBySK = async (params, mapKey) => {
    const { Items } = await docClient
        .query({
            ...BaseParams,
            IndexName: 'sortKeyIndex',
            ...params,
            KeyConditionExpression:
                'sortKey = :sk and begins_with(partitionKey, :pk)',
        })
        .promise();

    return formatResponse(Items, mapKey);
};

const getByPK = async (params, mapKey) => {
    const response = await fetchByPK({ ...params, Limit: 1 }, mapKey);
    return response[0];
};

const getBySK = async (params, mapKey) => {
    const response = await fetchBySK({ ...params, Limit: 1 }, mapKey);
    return response[0];
};

const insert = async params => {
    return docClient
        .put({
            TableName: BaseParams.TableName,
            Item: params,
        })
        .promise();
};

const update = async params => {
    return docClient
        .update({
            TableName: BaseParams.TableName,
            ...params,
        })
        .promise();
};

const remove = async params => {
    return docClient
        .delete({
            TableName: BaseParams.TableName,
            Key: params,
        })
        .promise();
};

module.exports = {
    fetchByPK,
    fetchBySK,
    getByPK,
    getBySK,
    insert,
    update,
    getKey,
    remove,
};
