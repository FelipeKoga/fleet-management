const { setSeconds } = require('date-fns');
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
            if (Date.now() >= user.avatarExpiration) {
                user.avatar = getObject(user.avatarKey);
                await Database.updateUserAvatar(
                    user,
                    user.avatar,
                    +setSeconds(Date.now(), 86400),
                );
            }
        }
        user.location = await Database.getLastLocation(username);
        await postMessage(user, 'USER_CONNECTED');
    }
}

async function deleteConnection(connectionId) {
    const username = await Database.getUsernameByConnectionId(connectionId);
    await Database.deleteConnectionId(username, connectionId);
    const user = await Database.getUser(username);
    if (user.avatar) {
        if (Date.now() >= user.avatarExpiration) {
            user.avatar = getObject(user.avatarKey);
            await Database.updateUserAvatar(
                user,
                user.avatar,
                +setSeconds(Date.now(), 86400),
            );
        }
    }

    if (user.status === 'ONLINE') {
        const hasConnectionActive = await Database.getUserConnectionId(
            username,
        );
        if (!hasConnectionActive.length) {
            user.status = 'OFFLINE';
            await Database.updateStatus(username, user.companyId, 'OFFLINE');
            user.location = await Database.getLastLocation(username);
            await postMessage(user, 'USER_DISCONNECTED');
        }
    }
}

module.exports = {
    addConnection,
    deleteConnection,
};
