const {
  getAllChats,
  sendMessage,
  getMessages,
  createChat,
} = require('./resolver');

const response = (statusCode, data) => {
  return {
    statusCode: statusCode,
    body: JSON.stringify(data),
  };
};

exports.handler = async (event) => {
  if (event.httpMethod) {
    const { username, companyId, chatId } = event.pathParameters;
    if (event.httpMethod === 'GET') {
      if (chatId) {
        return response(200, await getMessages(chatId));
      } else {
        return response(200, await getAllChats(username, companyId));
      }
    }

    if (event.httpMethod === 'POST') {
      console.log('CREATE CHAT');
      console.log(event);
      console.log(username);
      return response(201, await createChat(JSON.parse(event.body), username));
    }
  }

  if (event.requestContext.routeKey === 'send-message') {
    console.log(event.body);
    console.log(event.body);
    const body = JSON.parse(event.body);
    return response(200, await sendMessage(JSON.parse(body.data)));
  }
  return response(500, 'Method not allowed');
};
