const { DynamoDB } = require('aws-sdk');
const sendToAllConnected = require('./post-message');
const { createCognitoUser, deleteCognitoUser } = require('./cognito');
const docClient = new DynamoDB.DocumentClient();
const USER_TABLE = 'user';

const list = async (companyId) => {
  const params = {
    TableName: process.env.USER_TABLE,
    IndexName: 'companyIdIndex',
    KeyConditionExpression: 'companyId = :cId',
    ExpressionAttributeValues: {
      ':cId': companyId,
    },
    ProjectionExpression:
      'username, fullName, email, avatar, companyId, settings, phone',
  };
  const response = await docClient.query(params).promise();
  return response.Items;
};

const getUser = async (username) => {
  const params = {
    TableName: process.env.USER_TABLE,
    KeyConditionExpression: 'username = :u and begins_with(sortKey, :sk)',
    ExpressionAttributeValues: {
      ':u': username,
      ':sk': `METADATA`,
    },
    ProjectionExpression:
      'username, fullName, email, avatar, companyId, settings, phone',
  };

  const response = await docClient.query(params).promise();
  if (response.Items.length) {
    return response.Items[0];
  }
  throw new Error('User not found');
};

const create = async (data, companyId) => {
  await createCognitoUser(data, companyId);
  const newUser = {
    username: data.email,
    sortKey: `METADATA`,
    ...data,
  };
  delete newUser.password;

  const params = {
    TableName: USER_TABLE,
    Item: newUser,
  };
  await docClient.put(params).promise();
  const user = await getUser(newUser.username, companyId);
  await sendToAllConnected(user, 'post-user');
  return user;
};

const update = async (
  { customName, fullName, email, phone, settings },
  username,
  companyId
) => {
  const params = {
    TableName: process.env.USER_TABLE,
    Key: {
      username,
      sortKey: `METADATA`,
    },
    UpdateExpression:
      'set customName = :cn, fullName = :n, phone = :p, email = :e, settings = :s',
    ExpressionAttributeValues: {
      ':cn': customName,
      ':n': fullName,
      ':p': phone,
      ':e': email,
      ':s': settings,
    },
  };
  await docClient.update(params).promise();
  const user = await getUser(username, companyId);
  await sendToAllConnected(user, 'update-user');
  return user;
};

const remove = async (username, companyId) => {
  try {
    await deleteCognitoUser(username);
    const user = await getUser(username, companyId);
    let params = {
      TableName: process.env.USER_TABLE,
      Key: {
        username,
      },
    };

    await Promise.all([
      docClient
        .delete({ ...params, Key: { ...params.Key, sortKey: 'METADATA' } })
        .promise(),
      docClient
        .delete({ ...params, Key: { ...params.Key, sortKey: 'CONNECTION' } })
        .promise(),
      docClient
        .delete({ ...params, Key: { ...params.Key, sortKey: 'LOCATION' } })
        .promise(),
    ]);

    await sendToAllConnected(user, 'delete-user');
    return user;
  } catch (e) {
    throw e;
  }
};
module.exports = {
  list,
  getUser,
  create,
  update,
  remove,
};
