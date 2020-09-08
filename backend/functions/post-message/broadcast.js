const { DynamoDB } = require('aws-sdk');
const docClient = new DynamoDB.DocumentClient();

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

const broadcast = async ({ body, action, companyId }, apigwManagementApi) => {
  console.log(body);
  console.log(action);
  console.log(companyId);
  const connectionIds = await listConnections(companyId);
  const requests = [];
  connectionIds.forEach(({ sortKey }) => {
    const connectionId = getSortKeyValue(sortKey);
    const params = {
      ConnectionId: connectionId,
      Data: JSON.stringify({
        action,
        data: body,
      }),
    };
    console.log(params);
    requests.push(apigwManagementApi.postToConnection(params).promise());
  });

  return Promise.all(requests);
};

module.exports = broadcast;
