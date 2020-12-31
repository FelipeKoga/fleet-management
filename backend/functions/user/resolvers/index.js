const { Cognito, Database, Lambda } = require('../services');

const generateRandomString = length => {
    const characters =
        'abcdefghijklmnopqrstuvwxyz123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let randomPassword = '';
    for (let i = 0; i <= length; i += 1) {
        randomPassword += characters.charAt(
            Math.floor(Math.random() * characters.length),
        );
    }
    return randomPassword;
};

async function postMessage(user, action) {
    try {
        const connectionIds = await Database.connection.fetchCompanyConnectionIDs(
            user.companyId,
        );

        await Lambda.sendMessage(user, connectionIds, action);
        return true;
    } catch (e) {
        return false;
    }
}

async function get(username, companyId) {
    return Database.user.getUser(username, companyId);
}

async function list(companyId) {
    return Database.user.fetchUsers(companyId);
}

async function create(data, companyId) {
    const password = generateRandomString(8);
    const newUser = { ...data, password };
    await Cognito.create(newUser, companyId);
    await Database.user.createUser(newUser, companyId);
    const user = await Database.user.getUser(newUser.email, companyId);
    await postMessage(user, 'user_created');
    return user;
}

async function update(data, username, companyId) {
    await Database.user.updateUser(data, username, companyId);
    const user = await Database.user.getUser(username, companyId);
    await postMessage(user, 'user_updated');
    return user;
}

async function remove(username, companyId) {
    const user = await Database.user.getUser(username, companyId);
    await Cognito.remove(username);
    await Database.user.removeUser(username, companyId);
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
