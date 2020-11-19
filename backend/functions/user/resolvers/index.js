const { Cognito, Database } = require('../services');

async function get(username) {
    return Database.get(username);
}

async function list(companyId) {
    return Database.list(companyId);
}

async function create(data, companyId) {
    await Cognito.create(data, companyId);
    await Database.create(data, companyId);
    return Database.get(data.email);
}

async function update(data, username) {
    await Database.update(data, username);
    return Database.get(username);
}

async function remove(username) {
    await Cognito.remove(username);
    return Database.remove(username);
}

module.exports = {
    get,
    list,
    create,
    update,
    remove,
};
