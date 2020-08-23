const { deleteConnection, addConnection } = require('./resolver');

exports.handler = async (event) => {
  console.log(event);
  console.log(process.env.API_TCC);
  if (event.requestContext.eventType === 'CONNECT') {
    await addConnection(
      event.requestContext.connectionId,
      event.queryStringParameters.userId
    );
  } else if (event.requestContext.eventType === 'DISCONNECT') {
    await deleteConnection(event.requestContext.connectionId);
  }

  return {
    statusCode: 200,
    body: JSON.stringify(event.requestContext.eventType),
  };
};
