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

    if (user.status === 'OFFLINE') {
        await Database.updateStatus(username, user.companyId, 'ONLINE');
        user.status = 'ONLINE';
        await postMessage(user, 'connected');
    }
}

async function deleteConnection(connectionId) {
    const username = await Database.getUsernameByConnectionId(connectionId);
    await Database.deleteConnectionId(username, connectionId);
    const user = await Database.getUser(username);

    if (user.status === 'ONLINE') {
        const hasConnectionActive = await Database.getUserConnectionId(
            username,
        );
        if (!hasConnectionActive.length) {
            user.status = 'OFFLINE';
            await Database.updateStatus(username, user.companyId, 'OFFLINE');

            await postMessage(user, 'disconnected');
        }
    }
}

module.exports = {
    addConnection,
    deleteConnection,
};
