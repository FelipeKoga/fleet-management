const ApiGatewayManagementApi = require('aws-sdk/clients/apigatewaymanagementapi');
const { METHODS, RESPONSE } = require('./constants');
const { fetchConnectionIds } = require('./database');

const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: 'https://87davwn2wl.execute-api.us-east-1.amazonaws.com/dev',
});

const merge = array => array.reduce((a, b) => a.concat(b), []);

const getAction = type => {
    if (type === METHODS.START_PUSH_TO_TALK) {
        return RESPONSE.STARTED_PUSH_TO_TALK;
    }

    if (type === METHODS.STOP_PUSH_TO_TALK) {
        return RESPONSE.STOPPED_PUSH_TO_TALK;
    }

    return RESPONSE.RECEIVED_PUSH_TO_TALK;
};

exports.handler = async event => {
    const payload = JSON.parse(event.body);

    const { type, receivers, ...body } = payload.body;

    const connectionIdsPromises = [];
    receivers.forEach(receiver => {
        connectionIdsPromises.push(fetchConnectionIds(receiver));
    });

    const connectionIds = [
        ...new Set(merge(await Promise.all(connectionIdsPromises))),
    ];

    const action = getAction(type);

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
