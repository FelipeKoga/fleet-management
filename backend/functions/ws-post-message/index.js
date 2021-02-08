const ApiGatewayManagementApi = require('aws-sdk/clients/apigatewaymanagementapi');

const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: process.env.WS_URL,
});

exports.handler = async ({ connectionIds, data }) => {
    if (!connectionIds.length) return;
    const requests = [];
    connectionIds.forEach(connectionId => {
        requests.push(
            apigwManagementApi
                .postToConnection({
                    ConnectionId: connectionId,
                    Data: JSON.stringify({ ...data }),
                })
                .promise(),
        );
    });

    await Promise.all(requests);
};
