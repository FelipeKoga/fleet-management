const { DynamoDB, ApiGatewayManagementApi } = require('aws-sdk');
const docClient = new DynamoDB.DocumentClient();
const USER_TABLE = 'user';

const addConnection = async (connectionId, userId) => {
  const response = await getById(userId);
  if (response.Count) {
    const user = response.Items[0];

    if (user.connectionIds) {
      user.connectionIds.push(connectionId);
    } else {
      user.connectionIds = [connectionId];
    }

    const params = {
      TableName: USER_TABLE,
      Key: {
        id: userId,
      },
      UpdateExpression: 'set connectionIds = :cIds',
      ExpressionAttributeValues: {
        ':cIds': user.connectionIds,
      },
    };
    await docClient.update(params).promise();

    return await send(user, userId);
  }
};

const deleteConnection = async (user, connectionId) => {
  const index = user.connectionIds.indexOf(connectionId);
  if (index !== -1) {
    const slicedUsers = user.connectionIds.filter((cId) => {
      return cId !== connectionId;
    });
    const params = {
      TableName: USER_TABLE,
      Key: {
        id: user.id,
      },
      UpdateExpression: 'set connectionIds = :cIds',
      ExpressionAttributeValues: {
        ':cIds': slicedUsers,
      },
    };
    return await docClient.update(params).promise();
  }
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

const send = async (user) => {
  console.log(user);
  const endpoint =
    'https://ga7fxhoxu4.execute-api.us-east-1.amazonaws.com/dev/';
  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: endpoint,
  });
  const responseList = await list();
  const requests = [];
  responseList.Items.forEach((item) => {
    if (item.connectionIds) {
      item.connectionIds.forEach((cId) => {
        const params = {
          ConnectionId: cId,
          Data: JSON.stringify({
            name: user.name,
            id: user.id,
            companyId: user.companyId,
          }),
        };
        requests.push(
          apigwManagementApi
            .postToConnection(params)
            .promise()
            .catch((e) => {})
        );
      });
    }
  });

  return Promise.all(requests);
};

module.exports = {
  addConnection,
  deleteConnection,
};
