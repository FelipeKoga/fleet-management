const { DynamoDB, ApiGatewayManagementApi } = require('aws-sdk');
const docClient = new DynamoDB.DocumentClient();

const getTimestamp = () => {
  const date = new Date();
  const format = date.toLocaleString('en-US', {
    timeZone: 'America/Sao_Paulo',
  });
  return +new Date(format);
};

const addConnection = async (connectionId, username) => {
  const response = await getUser(username);
  if (response.Count) {
    const user = response.Items[0];
    const params = {
      TableName: process.env.USER_TABLE,
      Item: {
        sortKey: `CONNECTION#${connectionId}`,
        username,
        connectionId,
      },
    };
    await docClient.put(params).promise();
    return await send(user, 'connected');
  }

  throw new Error('User does not exists');
};

const deleteConnection = async (connectionId) => {
  const params = {
    TableName: process.env.USER_TABLE,
    IndexName: 'connectionIdIndex',
    KeyConditionExpression: 'connectionId = :cId',
    ExpressionAttributeValues: {
      ':cId': connectionId,
    },
    ProjectionExpression: 'username, sortKey',
  };
  const response = await docClient.query(params).promise();

  if (response.Count) {
    const { username, sortKey } = response.Items[0];
    await deleteConnectionId(username, sortKey);
    return await send(user, 'disconnected');
  }
};

const getUser = async (username) => {
  const params = {
    TableName: process.env.USER_TABLE,
    KeyConditionExpression:
      'username = :username and begins_with(sortKey, :sk)',
    ExpressionAttributeValues: {
      ':username': username,
      ':sk': 'METADATA',
    },
    ProjectionExpression: 'username, fullName, avatar, companyId',
  };
  return await docClient.query(params).promise();
};

const getAllUsernames = async (companyId) => {
  const params = {
    TableName: process.env.USER_TABLE,
    IndexName: 'companyIdIndex',
    KeyConditionExpression: 'companyId = :cId',
    ExpressionAttributeValues: {
      ':cId': companyId,
    },
    ProjectionExpression: 'username',
  };
  return await docClient.query(params).promise();
};

const listConnections = async (companyId) => {
  const response = await getAllUsernames(companyId);

  const items = response.Items;

  if (!items.length) return;

  let connectionIds = [];
  const promises = items.map(async ({ username }) => {
    const response = await docClient
      .query({
        TableName: process.env.USER_TABLE,
        KeyConditionExpression: 'username = :u and begins_with(sortKey, :sk)',
        ExpressionAttributeValues: {
          ':sk': `CONNECTION`,
          ':u': username,
        },
        ProjectionExpression: 'sortKey',
      })
      .promise();

    if (response.Items.length) {
      connectionIds = response.Items;
    }
  });

  await Promise.all(promises);

  return connectionIds;
};

const getSortKeyValue = (value) => {
  return value.split('#')[1];
};

const deleteConnectionId = async (username, sortKey) => {
  const params = {
    TableName: process.env.USER_TABLE,
    Key: {
      username,
      sortKey,
    },
  };

  return docClient.delete(params).promise();
};

const send = async (user, action) => {
  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: process.env.WS_URL,
  });
  const connectionIds = await listConnections(user.companyId);
  const requests = [];
  connectionIds.forEach(({ sortKey }) => {
    const connectionId = getSortKeyValue(sortKey);
    const params = {
      ConnectionId: connectionId,
      Data: JSON.stringify({
        action,
        data: user,
      }),
    };
    requests.push(
      apigwManagementApi
        .postToConnection(params)
        .promise()
        .catch(async (e) => {
          await deleteConnection(user.username, sortKey);
        })
    );
  });

  return Promise.all(requests);
};

module.exports = {
  addConnection,
  deleteConnection,
};
