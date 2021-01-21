const { sendMessage } = require('./invoke');
const { fetchConnectionIds, fetchChatUsers } = require('./database');

const merge = array => array.reduce((a, b) => a.concat(b), []);
async function getChatUsers(chatId, username) {
    const chatUsers = await fetchChatUsers(chatId);

    const promises = [];
    chatUsers
        .filter(chatUser => chatUser !== username)
        .forEach(chatUser => {
            promises.push(fetchConnectionIds(chatUser));
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
        inputData,
        outputData,
        length,
    } = payload.body;

    const connectionIds = await getChatUsers(chatId, username);

    console.log(connectionIds);
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
