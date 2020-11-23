const { Cognito, Database, Lambda } = require('../services');

async function postMessage(user, action) {
    try {
        const connectionIds = await Database.connection.getCompanyIDs(
            user.companyId,
        );

        await Lambda.sendMessage(user, connectionIds, action);
        return true;
    } catch {
        return false;
    }
}

async function get(username) {
    return Database.user.get(username);
}

async function list(companyId) {
    return Database.user.userse.list(companyId);
}

async function create(data, companyId) {
    await Cognito.create(data, companyId);
    await Database.user.create(data, companyId);
    const user = await Database.user.get(data.email);
    await postMessage(user, 'user_created');
    return user;
}

async function update(data, username) {
    await Database.user.update(data, username);
    const user = await Database.user.get(username);
    await postMessage(user, 'user_updated');
    return user;
}

async function remove(username) {
    const user = await Database.user.get(username);
    await Cognito.remove(username);
    await Database.user.remove(username);
    await postMessage(user, 'user_removed');
    return true;
}

module.exports = {
    get,
    list,
    create,
    update,
    remove,
};
