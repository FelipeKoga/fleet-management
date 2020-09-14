const { DynamoDB, ApiGatewayManagementApi, Lambda } = require('aws-sdk');
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
    await send(user, 'connected');
  } else {
    throw new Error('User does not exists');
  }
};

const deleteConnection = async (connectionId) => {
  const params = {
    TableName: process.env.USER_TABLE,
    IndexName: 'sortKeyIndex',
    KeyConditionExpression: 'sortKey = :sk',
    ExpressionAttributeValues: {
      ':sk': `CONNECTION#${connectionId}`,
    },
    ProjectionExpression: 'username, sortKey',
  };
  const response = await docClient.query(params).promise();

  if (response.Count) {
    const { username, sortKey } = response.Items[0];
    await deleteConnectionId(username, sortKey);
    const r = await getUser(username);
    const user = r.Items[0];
    await send(user, 'disconnected');
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
  const lambda = new Lambda();
  return await lambda
    .invoke({
      FunctionName: `${process.env.APP}-${process.env.STAGE}-post-message`,
      Payload: JSON.stringify({
        method: 'broadcast',
        data: {
          action,
          body: user,
          companyId: user.companyId,
        },
      }),
      InvocationType: 'RequestResponse',
    })
    .promise()
    .catch((e) => console.log(e));
};

module.exports = {
  addConnection,
  deleteConnection,
};
