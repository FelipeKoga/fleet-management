const { Database, Lambda } = require('../services');

async function postMessage(user, action) {
    const connectionIds = await Database.fetchCompanyConnectionIDs(
        user.companyId,
    );
    await Lambda.sendMessage(user, connectionIds, action);
}

async function addConnection(connectionId, username) {
    const user = await Database.getUser(username);
    await Database.insertConnectionId({
        username,
        connectionId,
    });

    await postMessage(user, 'connected');
}

async function deleteConnection(connectionId) {
    const username = await Database.getUsernameByConnectionId(connectionId);
    await Database.deleteConnectionId(username, connectionId);
    const user = await Database.getUser(username);
    await postMessage(user, 'disconnected');
}

module.exports = {
    addConnection,
    deleteConnection,
};
