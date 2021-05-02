const { setSeconds } = require('date-fns');
const jose = require('node-jose');
const fetch = require('node-fetch');
const { Database, Lambda } = require('../services');
const { getObject } = require('../services/s3');

async function postMessage(user, action) {
    const connectionIds = await Database.fetchCompanyConnectionIDs(
        user.companyId,
    );
    await Lambda.sendMessage(user, connectionIds, action);
}

async function authenticate(token) {
    if (!token) throw new Error('Unathorized');
    const sections = token.split('.');
    let header = jose.util.base64url.decode(sections[0]);
    header = JSON.parse(header);
    const { kid } = header;

    const rawRes = await fetch(
        'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_qtIxjoqVD/.well-known/jwks.json',
    );
    const response = await rawRes.json();

    if (rawRes.status !== 200) throw new Error('Internal server error');

    const { keys } = response;
    const foundKey = keys.find(key => key.kid === kid);

    if (!foundKey) throw new Error('Unathorized');
    const result = await jose.JWK.asKey(foundKey);
    const keyVerify = jose.JWS.createVerify(result);
    const verificationResult = await keyVerify.verify(token);

    const claims = JSON.parse(verificationResult.payload);

    if (claims.aud !== '4ssv3lk591ehbtcspheg30n6ot')
        throw new Error('Wrong app client id.');

    return claims['cognito:username'];
}

async function addConnection(connectionId, token) {
    const username = await authenticate(token);
    const user = await Database.getUser(username);

    if (!user) throw new Error('Unauthorized.');
    await Database.insertConnectionId({
        username,
        connectionId,
    });
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
