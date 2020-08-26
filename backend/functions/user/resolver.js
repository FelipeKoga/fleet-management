const { DynamoDB } = require('aws-sdk');
const { v4 } = require('uuid');
const sendToAllConnected = require('./post-message');
const { createCognitoUser, deleteCognitoUser } = require('./cognito');

const docClient = new DynamoDB.DocumentClient();
const USER_TABLE = 'user';

const list = async (companyId) => {
  const params = {
    TableName: USER_TABLE,
    IndexName: 'companyIdIndex',
    KeyConditionExpression: 'companyId = :cId',
    ExpressionAttributeValues: {
      ':cId': companyId,
    },
  };
  const response = await docClient.query(params).promise();

  return response.Items;
};

const getById = async (companyId, userId) => {
  const params = {
    TableName: USER_TABLE,
    KeyConditionExpression: 'id = :userId',
    ExpressionAttributeValues: {
      ':userId': userId,
    },
  };
  const response = await docClient.query(params).promise();
  if (response.Items[0].companyId === companyId) {
    return response.Items[0];
  }
  throw new Error('User not found');
};

const create = async (data, companyId) => {
  const newUser = {
    id: v4(),
    companyId,
    ...data,
  };
  const params = {
    TableName: USER_TABLE,
    Item: newUser,
  };
  await docClient.put(params).promise();
  const user = await getById(companyId, newUser.id);
  await createCognitoUser(data, companyId);
  await sendToAllConnected(user, 'post-user');
  return user;
};

const update = async (data, userId, companyId) => {
  const params = {
    TableName: USER_TABLE,
    Key: {
      id: userId,
    },
    UpdateExpression: 'set #n = :n, #e = :e',
    ExpressionAttributeNames: {
      '#n': 'name',
      '#e': 'email',
    },
    ExpressionAttributeValues: {
      ':n': data.name,
      ':e': data.email,
    },
  };
  await docClient.update(params).promise();
  const user = await getById(companyId, userId);
  await sendToAllConnected(user, 'update-user');
  return user;
};

const remove = async (userId, companyId) => {
  try {
    const user = await getById(companyId, userId);
    const params = {
      TableName: USER_TABLE,
      Key: {
        id: user.id,
      },
    };
    await docClient.delete(params).promise();
    await deleteCognitoUser(user.email);
    await sendToAllConnected(user, 'delete-user');
    return user;
  } catch (e) {
    throw e;
  }
};
module.exports = {
  list,
  getById,
  create,
  update,
  remove,
};
