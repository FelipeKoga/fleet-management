const Resolver = require('./resolvers');

async function handlePostMethod(resource, body, { username, chatId }) {
    const method = resource.split('/').pop();
    if (method === 'group') return Resolver.createGroup(username, body);

    if (method === 'messages')
        return Resolver.addMessage(chatId, username, body);

    const { withUsername } = body;
    return Resolver.createChat(username, withUsername);
}

async function handleGetMethod(resource, { username, chatId }) {
    const method = resource.split('/').pop();
    if (method === 'chats') return Resolver.getAllChats(username);

    if (method === 'messages') return Resolver.getMessages(chatId);

    throw new Error();
}

async function main({ httpMethod, resource, pathParameters, body }) {
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

    throw new Error('Method not defined');
}

exports.handler = async event => {
    try {
        return {
            statusCode: 200,
            body: await main(event),
        };
    } catch (error) {
        return {
            statusCode: 500,
            body: error.message,
        };
    }
};
