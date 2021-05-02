const { deleteConnection, addConnection } = require('./resolvers');

exports.handler = async ({ requestContext, queryStringParameters }) => {
    const { eventType, connectionId } = requestContext;

    try {
        if (eventType === 'CONNECT') {
            await addConnection(connectionId, queryStringParameters.token);
        } else if (eventType === 'DISCONNECT') {
            await deleteConnection(connectionId);
        }
        return {
            statusCode: 200,
            body: true,
        };
    } catch (e) {
        console.log(e);
        return {
            statusCode: 500,
            body: false,
        };
    }
};
