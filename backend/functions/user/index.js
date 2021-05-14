const Resolvers = require('./resolvers');

const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Credentials': true,
};

async function main({
    httpMethod,
    resource,
    requestContext,
    pathParameters,
    body,
}) {
    const parsedBody = typeof body === 'string' ? JSON.parse(body) : body;

    if (httpMethod) {
        const { username, companyId } = pathParameters;
        switch (httpMethod) {
            case 'GET':
                if (username) return Resolvers.get(username, companyId);
                return Resolvers.list(companyId);
            case 'POST':
                if (resource.includes('/location')) {
                    return Resolvers.addLocation(
                        companyId,
                        username,
                        parsedBody,
                    );
                }

                if (resource.includes('/notification')) {
                    return Resolvers.addNotificationToken({
                        username,
                        token: parsedBody.token,
                    });
                }
                return Resolvers.create(parsedBody, companyId);
            case 'PUT':
                if (resource.includes('/disable')) {
                    return Resolvers.disable(username, companyId);
                }

                return Resolvers.update(parsedBody, username, companyId);

            case 'DELETE':
                if (resource.includes('notification')) {
                    const { token } = pathParameters;
                    return Resolvers.removeNotificationToken({
                        username,
                        token,
                    });
                }
                break;
            default:
                throw new Error('Method not allowed');
        }
    }

    if (requestContext && body) {
        const { routeKey } = requestContext;
        const webSocketBody =
            typeof parsedBody.body === 'string'
                ? JSON.parse(parsedBody.body)
                : parsedBody.body;
        if (routeKey === 'SEND_LOCATION') {
            return Resolvers.addLocation(webSocketBody);
        }
    }

    throw new Error('Method not defined');
}

exports.handler = async event => {
    console.log(event);
    try {
        const response = {
            statusCode: 200,
            body: JSON.stringify(await main(event)),
            headers,
        };

        console.log(response);
        return response;
    } catch (error) {
        console.log(error);
        return {
            statusCode: 500,
            errorType: error.name,
            errorMessage: error.message,
            headers,
        };
    }
};
