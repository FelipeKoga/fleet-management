const ApiGatewayManagementApi = require('aws-sdk/clients/apigatewaymanagementapi');

const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: process.env.WS_URL,
});

async function send(connectionIds, action, data) {
    const requests = [];
    const params = {
        Data: JSON.stringify({
            action,
            data,
        }),
    };
    connectionIds.forEach(connectionId => {
        requests.push(
            apigwManagementApi
                .postToConnection({
                    ...params,
                    ConnectionId: connectionId,
                })
                .promise(),
        );
    });
}

module.exports = {
    send,
};
