const {
  getAllChats,
  sendMessage,
  getMessages,
  createChat,
} = require('./resolver');

exports.handler = async (event) => {
  if (event.httpMethod) {
    const { username, companyId, chatId } = event.pathParameters;
    if (event.httpMethod === 'GET') {
      if (chatId) {
        return await getMessages(chatId);
      } else {
        return await getAllChats(username, companyId);
      }
    }

    if (event.httpMethod === 'POST') {
      return await createChat(event.arguments, username);
    }
  }

  if (event.requestContext.routeKey === 'send-message') {
    return await sendMessage(event.body.data);
  }
  return {
    statusCode: 400,
    body: JSON.stringify('Error.'),
  };
};
