const Resolver = require('./resolvers');

const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Credentials': true,
};

async function handlePostMethod(resource, body, { username, chatId }) {
    const method = resource.split('/').pop();
    const payload = JSON.parse(body);
    let response;

    if (method === 'group')
        response = await Resolver.createGroup(username, payload);
    else if (resource.includes('avatar')) {
        const { avatar } = payload;

        response = await Resolver.addGroupAvatar(chatId, username, avatar);
    } else {
        const { withUsername } = payload;
        response = await Resolver.createChat(username, withUsername);
    }

    return JSON.stringify(response);
}

async function handleGetMethod(resource, { username, chatId }) {
    const method = resource.split('/').pop();
    let response = [];
    if (method === 'chats') response = await Resolver.getAllChats(username);

    if (method === 'messages') response = await Resolver.getMessages(chatId);

    if (resource.includes('group/'))
        response = await Resolver.getGroupMembers(chatId);

    return JSON.stringify(response);
}

async function handlePutMethod(resource, body, { chatId }) {
    const payload = JSON.parse(body);

    if (resource.includes('group/')) {
        const { member } = payload;
        if (resource.includes('/remove')) {
            return Resolver.removeMember(chatId, member);
        }
        return Resolver.addMember(chatId, member);
    }

    throw new Error('Method not allowed');
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
            case 'PUT':
                return handlePutMethod(resource, body, pathParameters);
            default:
                throw new Error('Method not allowed');
        }
    }

    if (requestContext && body) {
        const payload = JSON.parse(body);
        const { routeKey } = requestContext;

        if (routeKey === 'open-messages') {
            return Resolver.viewedMessages(payload.body);
        }

        if (routeKey === 'send-message') {
            return Resolver.addMessage(payload.body);
        }
    }

    throw new Error('Method not defined');
}

exports.handler = async event => {
    console.log(event);
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
