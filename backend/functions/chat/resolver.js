const { v4 } = require('uuid');
const { ApiGatewayManagementApi } = require('aws-sdk');
const moment = require('moment-timezone');

const { query, put } = require('./db');

const apigwManagementApi = new ApiGatewayManagementApi({
  apiVersion: '2018-11-29',
  endpoint: process.env.WS_URL,
});

const checkIfExists = async (member_username, username, isPrivate) => {
  if (!isPrivate) return false;

  const chatsMemberA = await getUserChats(member_username);
  const chatsMemberB = await getUserChats(username);

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
  { members, member_username, avatar, groupName, admin, isPrivate },
  username
) => {
  console.log(member_username);
  const existsChatId = await checkIfExists(
    member_username,
    username,
    isPrivate
  );
  console.log(existsChatId);
  if (existsChatId)
    return {
      ...(await getChat(existsChatId, username)),
      ...(await getMessages(existsChatId, 1)),
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

  await put({
    TableName: process.env.CHAT_TABLE,
    Item: {
      chatId,
      sortKey: `MEMBER#${username}`,
      isPrivate,
    },
  });

  if (member_username) {
    await put({
      TableName: process.env.CHAT_TABLE,
      Item: {
        chatId,
        sortKey: `MEMBER#${member_username}`,
        isPrivate,
      },
    });
  } else if (members.length) {
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
  }

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
    const chat = await getChat(chatId, username);

    chats.push(chat);
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

  if (chat.isPrivate) {
    return {
      chatId: chat.chatId,
      isPrivate: chat.isPrivate,
      user: members[0],
      members: [],
    };
  }

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

const getMessages = async (chatId) => {
  const response = await query({
    TableName: process.env.CHAT_TABLE,
    KeyConditionExpression: 'chatId = :chatId and begins_with(sortKey, :sk)',
    ExpressionAttributeValues: {
      ':chatId': chatId,
      ':sk': 'MESSAGE#',
    },
    ProjectionExpression:
      'chatId, body, sender, createdAt, sortKey, messageId, sent',
    ScanForwardIndex: false,
  });

  return response.Items;
};

const sendMessage = async ({ messageId, chatId, sender, body, createdAt }) => {
  const msg = {
    chatId,
    sender,
    body,
    createdAt,
    messageId,
    sortKey: `MESSAGE#${createdAt}`,
    sent: true,
  };

  console.log(msg);

  await put({
    TableName: process.env.CHAT_TABLE,
    Item: msg,
  }).catch((e) => console.log(e));

  const chatMembers = await getChatMembers(chatId);

  const promises = chatMembers.map(async (member) => {
    const username = getUsername(member.sortKey);
    const response = await query({
      TableName: process.env.USER_TABLE,
      KeyConditionExpression: 'username = :u and begins_with(sortKey, :sk)',
      ExpressionAttributeValues: {
        ':u': username,
        ':sk': 'CONNECTION#',
      },
    });

    const promises = response.Items.map(async (item) => {
      if (item.username === sender) {
        await send({
          ConnectionId: item.connectionId,
          Data: JSON.stringify({
            action: 'message_sent',
            data: msg,
          }),
        });
      } else {
        await send({
          ConnectionId: item.connectionId,
          Data: JSON.stringify({
            action: 'message_receive',
            data: msg,
          }),
        });
      }
    });
    await Promise.all(promises);
  });

  await Promise.all(promises);
};

const send = async (params) => {
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
