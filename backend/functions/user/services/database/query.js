const DynamoDB = require('aws-sdk/clients/dynamodb');

const docClient = new DynamoDB.DocumentClient();
const BaseParams = {
    TableName: process.env.TABLE,
    KeyConditionExpression: 'partitionKey = :pk and begins_with(sortKey, :sk)',
};

const fetchByPK = async params => {
    const { Items } = await docClient
        .query({
            ...BaseParams,
            ...params,
        })
        .promise();

    return Items;
};

const fetchBySK = async params => {
    const { Items } = await docClient
        .query({
            ...BaseParams,
            ...params,
            IndexName: 'sortKeyIndex',
            KeyConditionExpression:
                'sortKey = :sk and begins_with(partitionKey, :pk)',
        })
        .promise();

    return Items;
};

const getByPK = async params => {
    const response = await fetchByPK(params);
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

module.exports = { fetchByPK, fetchBySK, getByPK, insert, update, remove };
