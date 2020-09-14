const { DynamoDB, ApiGatewayManagementApi } = require('aws-sdk');
const docClient = new DynamoDB.DocumentClient();

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
const sendToAllConnected = async (data, action) => {
  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: process.env.WS_URL,
  });
  const connectionIds = await listConnections(data.companyId);
  const requests = [];
  connectionIds.forEach(({ sortKey }) => {
    const connectionId = getSortKeyValue(sortKey);
    const params = {
      ConnectionId: connectionId,
      Data: JSON.stringify({
        action,
        data,
      }),
    };
    requests.push(
      apigwManagementApi
        .postToConnection(params)
        .promise()
        .catch(async (e) => {
          console.log(e);
          await deleteConnectionId(data.username, sortKey);
        })
    );
  });

  return Promise.all(requests);
};

module.exports = sendToAllConnected;
