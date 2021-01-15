const { Database, Lambda } = require('../services');
const { getObject } = require('../services/s3');

async function postMessage(user, action) {
    const connectionIds = await Database.fetchCompanyConnectionIDs(
        user.companyId,
    );
    await Lambda.sendMessage(user, connectionIds, action);
}

async function addConnection(connectionId, username) {
    await Database.insertConnectionId({
        username,
        connectionId,
    });
    const user = await Database.getUser(username);
    if (user.status === 'OFFLINE') {
        await Database.updateStatus(username, user.companyId, 'ONLINE');
        user.status = 'ONLINE';
        if (user.avatar) {
            user.avatarUrl = getObject(user.avatar);
        }
        user.location = await Database.getLastLocation(username);
        await postMessage(user, 'user_connected');
    }
}

async function deleteConnection(connectionId) {
    const username = await Database.getUsernameByConnectionId(connectionId);
    await Database.deleteConnectionId(username, connectionId);
    const user = await Database.getUser(username);

    if (user.avatar) {
        user.avatarUrl = getObject(user.avatar);
    }

    if (user.status === 'ONLINE') {
        const hasConnectionActive = await Database.getUserConnectionId(
            username,
        );
        if (!hasConnectionActive.length) {
            user.status = 'OFFLINE';
            await Database.updateStatus(username, user.companyId, 'OFFLINE');

            await postMessage(user, 'user_disconnected');
        }
    }
}

module.exports = {
    addConnection,
    deleteConnection,
};
