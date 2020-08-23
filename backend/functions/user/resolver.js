const { DynamoDB, ApiGatewayManagementApi } = require('aws-sdk');
const { v4 } = require('uuid');

const docClient = new DynamoDB.DocumentClient();
const USER_TABLE = 'user';

const list = async () => {
  const params = {
    TableName: USER_TABLE,
    IndexName: 'companyIdIndex',
    KeyConditionExpression: 'companyId = :cId',
    ExpressionAttributeValues: {
      ':cId': '1',
    },
    // Limit: 1,
  };
  return await docClient.query(params).promise();
};

const getById = async (userId) => {
  const params = {
    TableName: USER_TABLE,
    KeyConditionExpression: 'id = :userId',
    ExpressionAttributeValues: {
      ':userId': userId,
    },
  };
  return await docClient.query(params).promise();
};

const create = async (user) => {
  const params = {
    TableName: USER_TABLE,
    Item: {
      id: v4(),
      ...user,
    },
  };
  await docClient.put(params).promise();
};

const update = async (user) => {
  const params = {
    TableName: USER_TABLE,
    Key: {
      id: user.id,
    },
    UpdateExpression: 'set #n = :n, #e = :e',
    ExpressionAttributeNames: {
      '#n': 'name',
      '#e': 'email',
    },
    ExpressionAttributeValues: {
      ':n': user.name,
      ':e': user.email,
    },
  };
  return await docClient.update(params).promise();
};

const remove = async (userId) => {
  const params = {
    TableName: USER_TABLE,
    Key: {
      id: userId,
    },
  };
  return await docClient.delete(params).promise();
};

const send = async (items, connectionId) => {
  const endpoint = process.env.API_URL;

  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: endpoint,
  });

  const params = {
    ConnectionId: connectionId,
    Data: JSON.stringify(items),
  };

  return apigwManagementApi
    .postToConnection(params)
    .promise()
    .catch((e) => console.log(e));
};

module.exports = {
  list,
  getById,
  create,
  update,
  remove,
};
