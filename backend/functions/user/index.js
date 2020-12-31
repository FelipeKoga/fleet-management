const Resolvers = require('./resolvers');

const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Credentials': true,
};

async function main(method, { username, companyId }, body) {
    switch (method) {
        case 'GET':
            if (username) return Resolvers.get(username, companyId);
            return Resolvers.list(companyId);
        case 'POST':
            return Resolvers.create(body, companyId);
        case 'PUT':
            return Resolvers.update(body, username, companyId);
        case 'DELETE':
            return Resolvers.remove(username, companyId);
        default:
            throw new Error('Method not allowed');
    }
}

exports.handler = async event => {
    console.log(event);
    const { body, pathParameters, httpMethod } = event;

    try {
        const response = {
            statusCode: 200,
            body: JSON.stringify(
                await main(
                    httpMethod,
                    pathParameters,
                    typeof body === 'string' ? JSON.parse(body) : body,
                ),
            ),
            headers,
        };

        console.log(response);
        return response;
    } catch (error) {
        console.log(error);
        console.log(error.name);
        return {
            statusCode: 500,
            errorType: error.name,
            errorMessage: error.message,
            headers,
        };
    }
};
