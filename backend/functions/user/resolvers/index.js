const { Cognito, Database, Lambda, S3, Email } = require('../services');
const { getObject } = require('../services/s3');

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
    const user = await Database.user.getUser(username, companyId);
    if (user.avatar) {
        user.avatarUrl = S3.getObject(user.avatar);
    }

    user.location = await Database.location.getLastLocation(username);

    return user;
}

async function list(companyId) {
    const users = await Database.user.fetchUsers(companyId);

    return Promise.all(
        users.map(async user => {
            return {
                ...user,
                avatarUrl: user.avatar ? S3.getObject(user.avatar) : undefined,
                location: await Database.location.getLastLocation(
                    user.username,
                ),
            };
        }),
    );
}

async function create(data, companyId) {
    const password = generateRandomString(8);
    const newUser = { ...data, password };
    await Cognito.create(newUser, companyId);
    await Database.user.createUser(newUser, companyId);
    const user = await get(newUser.email, companyId);
    const message = `Prezado(a) ${user.name}, seu usuário foi criado. Para logar, use as seguintes credenciais: <br><br><b>Usuário:</b> ${user.username}<br><b>Senha:</b> ${password}`;
    await Email.sendEmail(user.email, 'Seu usuário foi criado!', message);
    await postMessage(user, 'USER_CREATED');
    return user;
}

async function update(data, username, companyId) {
    const user = await get(username, companyId);
    if (user.status === 'DISABLED') {
        await Cognito.enable(username);
    }
    const payload = { ...data };
    if (payload.avatar) {
        payload.avatarUrl = getObject(data.avatar);
    }

    await Database.user.updateUser(payload, username, companyId);
    await postMessage(user, 'USER_UPDATED');
    return { ...user, ...payload };
}

async function disable(username, companyId) {
    const user = await get(username, companyId);
    await Cognito.disable(username);
    await Database.user.disableUser(username, companyId);
    await postMessage(user, 'USER_DISABLED');
    const message = `Prezado(a) ${user.name}, seu usuário foi desabilitado por um administrador.`;
    await Email.sendEmail(user.email, 'Seu usuário foi desabilitado.', message);
    return true;
}

async function addLocation(companyId, username, args) {
    await Database.location.addLocation(username, {
        ...args,
        lastUpdate: Date.now(),
    });
    const user = await get(username, companyId);
    await postMessage(user, 'USER_NEW_LOCATION');
    return true;
}

module.exports = {
    get,
    list,
    create,
    update,
    disable,
    addLocation,
};
