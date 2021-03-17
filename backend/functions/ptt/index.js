const { sendMessage } = require('./invoke');
const { fetchConnectionIds } = require('./database');

const merge = array => array.reduce((a, b) => a.concat(b), []);

async function getConnectionIds(receivers) {
    const promises = [];
    receivers.forEach(receiver => {
        promises.push(fetchConnectionIds(receiver));
    });

    const connectionIdsArrays = await Promise.all(promises);

    return merge(connectionIdsArrays);
}

exports.handler = async event => {
    const payload = JSON.parse(event.body);

    const {
        type,
        chatId,
        username,
        receiver,
        receivers,
        inputData,
        outputData,
        length,
    } = payload.body;

    console.log(payload.body);

    let connectionIds = [];
    if (receiver) {
        connectionIds = merge(await fetchConnectionIds(receiver));
    } else {
        connectionIds = await getConnectionIds(receivers);
    }

    console.log(connectionIds);
    console.log(type);

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
            outputData,
            length,
        };
    }

    await sendMessage(body, connectionIds, action);

    return {
        statusCode: 200,
    };
};
