const Resolvers = require('./resolvers');

async function main(method, { username, companyId }, body) {
    switch (method) {
        case 'GET':
            if (username) return Resolvers.get(username);
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
    const { body, pathParameters, httpMethod } = event;

    try {
        return {
            statusCode: 200,
            body: JSON.stringify(
                await main(httpMethod, pathParameters, JSON.parse(body)),
            ),
        };
    } catch (error) {
        return {
            statusCode: 500,
            body: JSON.stringify(error.message),
        };
    }
};
