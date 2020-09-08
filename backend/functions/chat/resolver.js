const { v4 } = require('uuid');
const { ApiGatewayManagementApi, Lambda } = require('aws-sdk');

const { query, put } = require('./db');

const apigwManagementApi = new ApiGatewayManagementApi({
  apiVersion: '2018-11-29',
  endpoint: process.env.WS_URL,
});

const checkIfExists = async (members, isPrivate) => {
  if (!isPrivate) return false;

  if (members.length !== 2) throw new Error('Error.');

  const chatsMemberA = await getUserChats(members[0].username);
  const chatsMemberB = await getUserChats(members[1].username);

  let chatId = null;

  chatsMemberA.forEach((memberA) => {
    if (memberA.isPrivate === true) {
      if (
        chatsMemberB.some(
          (memberB) => memberB.chatId === memberA.chatId && memberB.isPrivate
        )
      ) {
        chatId = memberA.chatId;
      }
    }
  });

  return chatId;
};

const createChat = async (
  { members, avatar, groupName, admin, isPrivate },
  username
) => {
  const existsChatId = await checkIfExists(members, isPrivate);
  if (existsChatId)
    return {
      chat: {
        ...(await getChat(existsChatId, username)),
        ...(await getMessages(existsChatId, 1)),
      },
    };

  const chatId = v4();
  await put({
    TableName: process.env.CHAT_TABLE,
    Item: {
      chatId,
      sortKey: 'CONFIG',
      avatar,
      admin,
      groupName,
      isPrivate,
    },
  });

  const promises = members.map(async (member) => {
    await put({
      TableName: process.env.CHAT_TABLE,
      Item: {
        chatId,
        sortKey: `MEMBER#${member.username}`,
        isPrivate,
      },
    });
  });

  await Promise.all(promises);

  return await getChat(chatId, username);
};

const getUser = async (username) => {
  const params = {
    TableName: process.env.USER_TABLE,
    KeyConditionExpression: 'username = :u and begins_with(sortKey, :sk)',
    ExpressionAttributeValues: {
      ':u': username,
      ':sk': 'METADATA',
    },
    ProjectionExpression: 'username, fullName, avatar',
  };
  const response = await query(params);
  if (response.Count) {
    return response.Items[0];
  }
};

const getUsername = (sortKey) => {
  return sortKey.split('#')[1];
};

const getAllChats = async (username) => {
  const userChats = await getUserChats(username);
  const chats = [];
  const promises = userChats.map(async ({ chatId }) => {
    const lastMessage = await getMessages(chatId, 1);
    if (!lastMessage) return;
    const chat = await getChat(chatId, username);

    chats.push({ ...chat, lastMessage });
  });

  await Promise.all(promises);
  return chats.filter((chat) => !!chat);
};

const getChat = async (chatId, username) => {
  const chatResponse = await query({
    TableName: process.env.CHAT_TABLE,
    KeyConditionExpression:
      'chatId = :chatId and begins_with(sortKey, :sortKeyVal)',
    ExpressionAttributeValues: {
      ':sortKeyVal': `CONFIG`,
      ':chatId': chatId,
    },
    ProjectionExpression: 'groupName, chatId, avatar, isPrivate, admin',
  });

  const chat = chatResponse.Items[0];

  const chatMembers = await getChatMembers(chatId);

  const filteredItems = chatMembers.filter((item) => {
    return getUsername(item.sortKey) !== username;
  });

  const promises = filteredItems.map(async (item) => {
    return await getUser(getUsername(item.sortKey));
  });

  const members = await Promise.all(promises);
  return {
    ...chat,
    members: members.filter((member) => !!member),
  };
};

const getUserChats = async (username) => {
  const response = await query({
    TableName: process.env.CHAT_TABLE,
    IndexName: 'sortKeyIndex',
    KeyConditionExpression: 'sortKey = :sk',
    ExpressionAttributeValues: {
      ':sk': `MEMBER#${username}`,
    },
    ProjectionExpression: 'chatId, isPrivate',
  });

  return response.Items;
};

const getChatMembers = async (chatId) => {
  const response = await query({
    TableName: process.env.CHAT_TABLE,
    KeyConditionExpression:
      'chatId = :chatId and begins_with(sortKey, :sortKeyVal)',
    ExpressionAttributeValues: {
      ':sortKeyVal': `MEMBER#`,
      ':chatId': chatId,
    },
  });

  return response.Items;
};

const getMessages = async (chatId, limit) => {
  const response = await query({
    TableName: process.env.CHAT_TABLE,
    KeyConditionExpression: 'chatId = :chatId and begins_with(sortKey, :sk)',
    ScanForwardIndex: true,
    ExpressionAttributeValues: {
      ':chatId': chatId,
      ':sk': 'MESSAGE#',
    },
    ProjectionExpression: 'chatId, body, username, createdAt',
    ScanForwardIndex: false,
    Limit: limit ? limit : null,
  });

  if (limit) return response.Items[0];

  return response.Items;
};

const invokePostMessage = async (action, data) => {
  const lambda = new Lambda();

  console.log('INVOKE');
  await lambda
    .invoke({
      FunctionName: `${process.env.APP}-${process.env.STAGE}-post-message`,
      Payload: JSON.stringify({
        method: 'user',
        data: {
          action,
          data,
        },
      }),
      InvocationType: 'RequestResponse',
    })
    .promise()
    .catch((e) => console.log(e));
};

const sendMessage = async ({ chatId, username, body, createdAt }) => {
  const msg = {
    chatId,
    username,
    body,
    createdAt,
    sortKey: `MESSAGE#${createdAt}`,
  };

  await put({
    TableName: process.env.CHAT_TABLE,
    Item: msg,
  }).catch((e) => console.log(e));

  const chatMembers = await getChatMembers(chatId);

  console.log(chatMembers);
  const promises = chatMembers.map(async (member) => {
    console.log(member);
    const username = getUsername(member.sortKey);
    const response = await query({
      TableName: process.env.USER_TABLE,
      KeyConditionExpression: 'username = :u and begins_with(sortKey, :sk)',
      ExpressionAttributeValues: {
        ':u': username,
        ':sk': 'CONNECTION#',
      },
    });

    console.log(response);
    const promises = response.Items.map(async (item) => {
      if (item.username === username) {
        await send({
          ConnectionId: item.connectionId,
          Data: JSON.stringify({
            action: 'message-sent',
            body: msg,
          }),
        });
      } else {
        await send({
          ConnectionId: item.connectionId,
          Data: JSON.stringify({
            action: 'message-receive',
            body: msg,
          }),
        });
      }
    });
    await Promise.all(promises);
  });

  await Promise.all(promises);
};

const send = async (params) => {
  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: process.env.WS_URL,
  });
  await apigwManagementApi
    .postToConnection(params)
    .promise()
    .catch((e) => console.log(e));
};

module.exports = {
  sendMessage,
  getAllChats,
  getMessages,
  createChat,
};
