const { deleteConnection, addConnection } = require('./resolvers');

exports.handler = async ({ requestContext, queryStringParameters }) => {
    const { eventType, connectionId } = requestContext;

    if (eventType === 'CONNECT') {
        await addConnection(connectionId, queryStringParameters.username);
    } else if (eventType === 'DISCONNECT') {
        await deleteConnection(connectionId);
    }

    return {
        statusCode: 200,
        body: JSON.stringify(''),
    };
};
