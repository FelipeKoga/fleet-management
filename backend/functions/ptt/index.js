const ApiGatewayManagementApi = require('aws-sdk/clients/apigatewaymanagementapi');
const { fetchConnectionIds } = require('./database');

const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: 'https://87davwn2wl.execute-api.us-east-1.amazonaws.com/dev',
});

const merge = array => array.reduce((a, b) => a.concat(b), []);

exports.handler = async event => {
    const payload = JSON.parse(event.body);

    const {
        type,
        chatId,
        username,
        receiver,
        inputData,
        length,
    } = payload.body;

    console.log(payload.body);

    const connectionIds = merge(await fetchConnectionIds(receiver));

    let body;
    let action;
    if (type === 'START_PUSH_TO_TALK') {
        action = 'STARTED_PUSH_TO_TALK';
        body = {
            chatId,
            username,
        };
    } else if (type === 'STOP_PUSH_TO_TALK') {
        action = 'STOPPED_PUSH_TO_TALK';
        body = {
            chatId,
            username,
        };
    } else {
        action = 'RECEIVED_PUSH_TO_TALK';

        body = {
            chatId,
            username,
            inputData,
            length,
        };
    }

    const requests = [];
    connectionIds.forEach(connectionId => {
        requests.push(
            apigwManagementApi
                .postToConnection({
                    ConnectionId: connectionId,
                    Data: JSON.stringify({
                        action,
                        body,
                    }),
                })
                .promise(),
        );
    });

    await Promise.all(requests);

    return {
        statusCode: 200,
    };
};
