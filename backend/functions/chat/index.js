const Resolver = require('./resolvers');

const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Credentials': true,
};

async function handlePostMethod(resource, body, { username, chatId }) {
    const method = resource.split('/').pop();
    if (method === 'group') return Resolver.createGroup(username, body);

    const { withUsername } = body;
    return Resolver.createChat(username, withUsername);
}

async function handleGetMethod(resource, { username, chatId }) {
    const method = resource.split('/').pop();
    let response = [];
    if (method === 'chats') response = await Resolver.getAllChats(username);

    if (method === 'messages') response = await Resolver.getMessages(chatId);

    return JSON.stringify(response);
}

async function main({
    httpMethod,
    resource,
    requestContext,
    pathParameters,
    body,
}) {
    if (httpMethod) {
        switch (httpMethod) {
            case 'POST':
                return handlePostMethod(resource, body, pathParameters);
            case 'GET':
                return handleGetMethod(resource, pathParameters);
            default:
                throw new Error('Method not allowed');
        }
    }

    if (requestContext && body) {
        const payload = JSON.parse(body);
        const { routeKey } = requestContext;

        const { username, chatId, message } = payload.body;

        if (routeKey === 'open-messages') {
            return Resolver.viewedMessages(chatId, username);
        }

        if (routeKey === 'send-message') {
            return Resolver.addMessage(chatId, username, message);
        }
    }

    throw new Error('Method not defined');
}

exports.handler = async event => {
    try {
        return {
            statusCode: 200,
            body: await main(event),
            headers,
        };
    } catch (error) {
        console.log(error);
        return {
            errorType: 500,
            errorMessage: error.message,
            headers,
        };
    }
};
