const randomColor = require('randomcolor');
const { setSeconds } = require('date-fns');
const { Cognito, Database, Lambda, S3 } = require('../services');
const { updateUserAvatar } = require('../services/database/user');

const generateRandomString = length => {
    const characters =
        'abcdefghijklmnopqrstuvwxyz123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789';
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
    if (user.avatarKey) {
        if (Date.now() <= user.avatarExpiration) {
            user.avatar = S3.getObject(user.avatarKey);
            await Database.user.updateUserAvatar(
                user,
                user.avatar,
                +setSeconds(Date.now(), 86400),
            );
        }
    }
    user.location = await Database.location.getLastLocation(username);

    return user;
}

async function list(companyId) {
    const users = await Database.user.fetchUsers(companyId);

    return Promise.all(
        users.map(async user => {
            return get(user.username, companyId);
        }),
    );
}

async function create(data, companyId) {
    const password = generateRandomString(8);
    const color = randomColor({
        luminosity: 'dark',
    });

    const newUser = {
        ...data,
        password,
        color: color.replace('#', ''),
        locationEnabled: true,
        pushToTalkEnabled: true,
        notificationEnabled: true,
    };
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
    const payload = { ...user, ...data };

    if (payload.avatar || payload.avatarKey) {
        payload.avatarKey = !payload.avatarKey
            ? payload.avatar
            : payload.avatarKey;
        payload.avatar = S3.getObject(payload.avatarKey);
        payload.avatarExpiration = +setSeconds(Date.now(), 604800);
        await updateUserAvatar(user, payload.avatar, payload.avatarExpiration);
    }

    console.log(payload);

    await Database.user.updateUser(payload, username, companyId);
    await postMessage(payload, 'USER_UPDATED');
    return { ...user, ...payload };
}

async function disable(username, companyId) {
    const user = await get(username, companyId);
    await Cognito.disable(username);
    await Database.user.disableUser(username, companyId);
    await postMessage(user, 'USER_DISABLED');
    const message = `Prezado(a) ${user.name}, seu usuário foi desabilitado por um administrador.`;
    await Email.sendEmail(user.email, 'Seu usuário foi desabilitado.', message);
    return { ...user, status: 'DISABLED' };
}

async function addLocation({ companyId, username, latitude, longitude }) {
    await Database.location.add(username, {
        latitude,
        longitude,
        lastUpdate: Date.now(),
    });
    const user = await get(username, companyId);
    await postMessage(user, 'USER_NEW_LOCATION');
    return true;
}

async function addNotificationToken({ username, token }) {
    await Database.user.addNotificationToken(username, token);
    return true;
}

async function removeNotificationToken({ username, token }) {
    await Database.user.removeNotificationToken(username, token);
    return true;
}

module.exports = {
    get,
    list,
    create,
    update,
    disable,
    addLocation,
    addNotificationToken,
    removeNotificationToken,
};
