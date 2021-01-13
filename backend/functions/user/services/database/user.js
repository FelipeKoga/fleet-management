const { getByPK, fetchBySK, insert, update } = require('./query');
const { removeConnectionIds } = require('./connection');

const ProjectionExpression =
    'username, #name, email, avatar, #status, #role, phone, companyId, customName';

async function getUser(username, companyId) {
    return getByPK({
        ExpressionAttributeValues: {
            ':pk': `USER#${username}`,
            ':sk': `CONFIG#${companyId}`,
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
        partitionKey: `USER#${data.email}`,
        username: data.email,
        sortKey: `CONFIG#${companyId}`,
        companyId,
    });
}

async function updateUser(
    { customName = '', name, phone, email, role, status, avatar },
    username,
    companyId,
) {
    return update({
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
        UpdateExpression:
            'set customName = :customName, phone = :phone, email = :email, #role = :role, #status = :status, #name = :name, #avatar = :avatar',
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
            '#name': 'name',
            '#avatar': 'avatar',
        },
        ExpressionAttributeValues: {
            ':customName': customName,
            ':phone': phone,
            ':email': email,
            ':role': role,
            ':status': status,
            ':name': name,
            ':avatar': avatar,
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

module.exports = {
    getUser,
    fetchUsers,
    createUser,
    updateUser,
    disableUser,
};
