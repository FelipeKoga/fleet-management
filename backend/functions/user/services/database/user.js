const { getByPK, fetchBySK, insert, update, remove } = require('./query');
const { removeConnectionIds } = require('./connection');

const ProjectionExpression =
    'username, #name, email, avatar, #status, #role, phone, companyId';

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
    { customName, phone, email, role, status },
    username,
    companyId,
) {
    return update({
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
        UpdateExpression:
            'set customName = :customName, phone = :phone, email = :email, #role = :role, #status = :status',
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
        },
        ExpressionAttributeValues: {
            ':customName': customName,
            ':phone': phone,
            ':email': email,
            ':role': role,
            ':status': status,
        },
    });
}

async function removeUser(username, companyId) {
    await removeConnectionIds(username);
    await remove({
        partitionKey: `USER#${username}`,
        sortKey: `CONFIG#${companyId}`,
    });
}

module.exports = {
    getUser,
    fetchUsers,
    createUser,
    updateUser,
    removeUser,
};
