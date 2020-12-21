const { Cognito, Database, Lambda } = require('../services');

async function postMessage(user, action) {
    try {
        const connectionIds = await Database.connection.getCompanyIDs(
            user.companyId,
        );

        await Lambda.sendMessage(user, connectionIds, action);
        return true;
    } catch (e) {
        return false;
    }
}

async function get(username, companyId) {
    return Database.user.get(username, companyId);
}

async function list(companyId) {
    return Database.user.list(companyId);
}

async function create(data, companyId) {
    await Cognito.create(data, companyId);
    await Database.user.create(data, companyId);
    const user = await Database.user.get(data.email, companyId);
    await postMessage(user, 'user_created');
    return user;
}

async function update(data, username, companyId) {
    await Database.user.update(data, username, companyId);
    const user = await Database.user.get(username, companyId);
    await postMessage(user, 'user_updated');
    return user;
}

async function remove(username, companyId) {
    const user = await Database.user.get(username, companyId);
    await Cognito.remove(username);
    await Database.user.remove(username, companyId);
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
