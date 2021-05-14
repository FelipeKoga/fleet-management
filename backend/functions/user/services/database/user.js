const { getByPK, fetchBySK, insert, update, remove } = require('./query');
const { removeConnectionIds } = require('./connection');

const ProjectionExpression =
    'username, #name, email, avatar, avatarKey, avatarExpiration, #status, #role, phone, companyId, customName, locationUpdate, color, locationEnabled, notificationEnabled, pushToTalkEnabled';

async function getUser(username, companyId) {
    return getByPK({
        ExpressionAttributeValues: {
            ':pk': `USER#${username}`,
            ':sk': `CONFIG#${companyId || ''}`,
        },
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
            '#name': 'name',
        },
        ProjectionExpression,
    });
}

async function fetchUsers(companyId) {
    return fetchBySK({
        ExpressionAttributeValues: {
            ':sk': `CONFIG#${companyId}`,
            ':pk': 'USER#',
        },
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
            '#name': 'name',
        },
        ProjectionExpression,
    });
}

async function createUser(data, companyId) {
    const { password, ...user } = data;
    await insert({
        ...user,
        locationUpdate: 30,
        partitionKey: `USER#${data.email}`,
        username: data.email,
        sortKey: `CONFIG#${companyId}`,
        companyId,
    });
}

async function updateUser(
    {
        customName = '',
        name,
        phone,
        email,
        role,
        avatar,
        locationEnabled,
        pushToTalkEnabled,
        notificationEnabled,
    },
    username,
    companyId,
) {
    return update({
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
        UpdateExpression:
            'set customName = :customName, phone = :phone, email = :email, #role = :role, #name = :name, #avatar = :avatar,#locationEnabled = :locationEnabled, #pushToTalkEnabled = :pushToTalkEnabled, #notificationEnabled = :notificationEnabled',
        ExpressionAttributeNames: {
            '#role': 'role',
            '#name': 'name',
            '#avatar': 'avatar',
            '#locationEnabled': 'locationEnabled',
            '#pushToTalkEnabled': 'pushToTalkEnabled',
            '#notificationEnabled': 'notificationEnabled',
        },
        ExpressionAttributeValues: {
            ':customName': customName,
            ':phone': phone,
            ':email': email,
            ':role': role,
            ':name': name,
            ':avatar': avatar,
            ':locationEnabled': locationEnabled,
            ':pushToTalkEnabled': pushToTalkEnabled,
            ':notificationEnabled': notificationEnabled,
        },
    });
}

async function updateUserAvatar(user, avatar, expiration) {
    return update({
        Key: {
            partitionKey: `USER#${user.username}`,
            sortKey: `CONFIG#${user.companyId}`,
        },
        UpdateExpression:
            'set #avatar = :avatar, #avatarExpiration = :avatarExpiration',
        ExpressionAttributeNames: {
            '#avatar': 'avatar',
            '#avatarExpiration': 'avatarExpiration',
        },
        ExpressionAttributeValues: {
            ':avatar': avatar,
            ':avatarExpiration': expiration,
        },
    });
}

async function disableUser(username, companyId) {
    await removeConnectionIds(username);
    return update({
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
        UpdateExpression: 'set #status = :status',
        ExpressionAttributeNames: {
            '#status': 'status',
        },
        ExpressionAttributeValues: {
            ':status': 'DISABLED',
        },
    });
}

async function addNotificationToken(username, token) {
    return insert({
        partitionKey: `USER#${username}`,
        sortKey: `NOTIFICATION#${token}`,
        token,
    });
}

async function removeNotificationToken(username, token) {
    return remove({
        partitionKey: `USER#${username}`,
        sortKey: `NOTIFICATION#${token}`,
    });
}

module.exports = {
    getUser,
    fetchUsers,
    createUser,
    updateUser,
    disableUser,
    updateUserAvatar,
    addNotificationToken,
    removeNotificationToken,
};
