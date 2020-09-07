const { deleteConnection, addConnection } = require('./resolver');

exports.handler = async (event) => {
  if (event.requestContext.eventType === 'CONNECT') {
    await addConnection(
      event.requestContext.connectionId,
      event.queryStringParameters.username
    );
  } else if (event.requestContext.eventType === 'DISCONNECT') {
    await deleteConnection(event.requestContext.connectionId);
  }

  return {
    statusCode: 200,
    body: JSON.stringify('Connection handler'),
  };
};
