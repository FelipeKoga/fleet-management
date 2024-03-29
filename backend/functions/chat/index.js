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

async function handlePutMethod(resource, body, { chatId, username }) {
    const payload = JSON.parse(body);
    let response;

    if (resource.includes('group/')) {
        const { member } = payload;
        if (resource.includes('/remove')) {
            response = await Resolver.removeMember(chatId, member);
        } else if (resource.includes('/add')) {
            response = await Resolver.addMember(chatId, member);
        } else {
            response = await Resolver.updateGroup(chatId, username, payload);
        }
    }

    if (!response) {
        throw new Error('Method not allowed');
    }

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
            case 'PUT':
                return handlePutMethod(resource, body, pathParameters);
            default:
                throw new Error('Method not allowed');
        }
    }

    if (requestContext && body) {
        const payload = JSON.parse(body);
        const { routeKey } = requestContext;

        const parsedBody =
            typeof payload.body === 'string'
                ? JSON.parse(payload.body)
                : payload.body;

        if (routeKey === 'OPEN_MESSAGES') {
            return Resolver.viewedMessages(parsedBody);
        }

        if (routeKey === 'SEND_MESSAGE') {
            return Resolver.addMessage(parsedBody);
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
