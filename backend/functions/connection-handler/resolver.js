const { DynamoDB, ApiGatewayManagementApi } = require('aws-sdk');
const docClient = new DynamoDB.DocumentClient();
const CONNECTION_TABLE = 'connection';
const USER_TABLE = 'user';

const addConnection = async (connectionId, userId) => {
  const response = await getById(userId);
  if (response.Count) {
    const user = response.Items[0];
    const params = {
      TableName: CONNECTION_TABLE,
      Item: {
        id: userId,
        connectionId: connectionId,
        companyId: user.companyId,
      },
    };
    await docClient.put(params).promise();
    return await send(user, 'connected');
  }
};

const deleteConnection = async (connectionId) => {
  const response = await getByConnectionId(connectionId);
  const connectionResponse = response.Items[0];
  const userResponse = await getById(connectionResponse.id);
  const user = userResponse.Items[0];

  const params = {
    TableName: CONNECTION_TABLE,
    Key: {
      connectionId: connectionId,
    },
  };
  await docClient.delete(params).promise();
  return await send(user, 'disconnected');
};

const getByConnectionId = async (connectionId) => {
  const params = {
    TableName: CONNECTION_TABLE,
    KeyConditionExpression: 'connectionId = :cId',
    ExpressionAttributeValues: {
      ':cId': connectionId,
    },
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

const list = async (companyId) => {
  const params = {
    TableName: CONNECTION_TABLE,
    IndexName: 'companyIdIndex',
    KeyConditionExpression: 'companyId = :cId',
    ExpressionAttributeValues: {
      ':cId': companyId,
    },
  };
  return await docClient.query(params).promise();
};

const send = async (user, action) => {
  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: 'https://a2xzjwkbb8.execute-api.us-east-1.amazonaws.com/dev',
  });
  const responseList = await list(user.companyId);
  const requests = [];
  responseList.Items.forEach((item) => {
    const params = {
      ConnectionId: item.connectionId,
      Data: JSON.stringify({
        action,
        data: {
          name: user.name,
          id: user.id,
          companyId: user.companyId,
        },
      }),
    };
    requests.push(
      apigwManagementApi
        .postToConnection(params)
        .promise()
        .catch((e) => console.log(e))
    );
  });

  return Promise.all(requests);
};

module.exports = {
  addConnection,
  deleteConnection,
};
