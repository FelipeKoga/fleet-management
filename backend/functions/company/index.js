const { DynamoDB } = require('aws-sdk');
const { v4 } = require('uuid');

const docClient = new DynamoDB.DocumentClient();
const TableName = 'user';

exports.handler = async ({ field, arguments }) => {
  switch (field) {
    case 'getChats':
      return await getChats(arguments);
    case 'putUser':
      return await putUser(arguments);
    case 'putCompany':
      return await putCompany(arguments);
  }
};

const getCompany = async ({ companyId }) => {
  const params = {
    TableName,
    KeyConditionExpression:
      'companyId = :cId and begins_with(sortKey, :sortKeyVal)',
    ExpressionAttributeValues: {
      ':cId': companyId,
      ':sortKeyVal': 'METADATA',
    },
    ProjectionExpression: 'companyName, avatar, companyId',
  };
  return await docClient.query(params).promise();
};

const getUsers = async ({ companyId }) => {
  const params = {
    TableName,
    IndexName: 'companyIdIndex',
    KeyConditionExpression: 'companyId = :cId ',
    ExpressionAttributeValues: {
      ':cId': companyId,
    },
    ProjectionExpression: 'fullName, email, avatar, settings, companyId',
  };
  return await docClient.query(params).promise();
};

const getChats = async ({ username }) => {
  const params = {
    TableName: 'chat',
    IndexName: 'sortKeyIndex',
    KeyConditionExpression: 'sortKey = :sk ',
    ExpressionAttributeValues: {
      ':sk': `MEMBER#${username}`,
    },
    ProjectionExpression: 'chatId, groupName, avatar',
  };

  const response = await docClient.query(params).promise();

  const promises = response.Items.map(
    async ({ chatId, groupName, avatar, private }) => {
      const r = await docClient
        .query({
          TableName: 'chat',
          KeyConditionExpression:
            'chatId = :chatId and begins_with(sortKey, :sortKeyVal)',
          ExpressionAttributeValues: {
            ':sortKeyVal': `MEMBER#`,
            ':chatId': chatId,
          },
        })
        .promise();

      const filteredItems = r.Items.filter((item) => {
        return getUsername(item.sortKey) !== username;
      });
      const promises = filteredItems.map(async (item) => {
        const response = await getUser(getUsername(item.sortKey));
        return response.Items[0];
      });

      const members = await Promise.all(promises);

      const messages = await docClient
        .query({
          TableName: 'chat',
          KeyConditionExpression:
            'chatId = :chatId and begins_with(sortKey, :sortKeyVal)',
          ExpressionAttributeValues: {
            ':sortKeyVal': `MESSAGE#`,
            ':chatId': chatId,
          },
          ScanIndexForward: false,
          Limit: 1,
        })
        .promise();

      return {
        chatId,
        groupName,
        avatar,
        private,
        members,
        messages,
      };
    }
  );

  return await Promise.all(promises);
};

const getUsername = (sortKey) => {
  return sortKey.split('#')[1];
};

const getUser = async (username) => {
  const params = {
    TableName: 'user',
    KeyConditionExpression: 'username = :u and begins_with(sortKey, :sk)',
    ExpressionAttributeValues: {
      ':u': username,
      ':sk': 'METADATA',
    },
    ProjectionExpression: 'username, fullName, avatar',
  };
  return await docClient.query(params).promise();
};

const putCompany = async (data) => {
  const params = {
    TableName,
    Item: {
      companyId: v4(),
      sortKey: 'METADATA',
      ...data,
    },
  };
  return await docClient.put(params).promise();
};

const putUser = async ({ companyId, email, avatar, fullName, settings }) => {
  const params = {
    TableName: 'user',
    Item: {
      companyId,
      username: email,
      sortKey: 'METADATA',
      email,
      avatar,
      fullName,
      settings,
    },
  };
  return await docClient.put(params).promise();
};

const deleteUser = ({ companyId }) => {};
