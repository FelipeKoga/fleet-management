const {
    fetchByPK,
    fetchBySK,
    getBySK,
    getByPK,
    insert,
    remove,
    update,
} = require('./query');

async function fetchCompanyConnectionIDs(companyId) {
    const users = await fetchBySK({
        ExpressionAttributeValues: {
            ':sk': `CONFIG#${companyId}`,
            ':pk': 'USER#',
        },
    });

    let allConnectionIds = [];

    const promises = users.map(async user => {
        const connectionIds = await fetchByPK({
            ExpressionAttributeValues: {
                ':sk': `CONNECTION#`,
                ':pk': `USER#${user.username}`,
            },
            ProjectionExpression: 'connectionId',
        });

        allConnectionIds = [
            ...allConnectionIds,
            ...connectionIds.map(res => res.connectionId),
        ];
    });

    await Promise.all(promises);
    return allConnectionIds;
}

async function getUserConnectionId(username) {
    return fetchByPK({
        ExpressionAttributeValues: {
            ':sk': `CONNECTION#`,
            ':pk': `USER#${username}`,
        },
        ProjectionExpression: 'connectionId',
        Limit: 1,
    });
}

async function getUsernameByConnectionId(connectionId) {
    const user = await getBySK({
        ExpressionAttributeValues: {
            ':sk': `CONNECTION#${connectionId}`,
            ':pk': `USER#`,
        },
    });

    if (user) return user.partitionKey.split('#').pop();

    return null;
}

async function getUser(username) {
    return getByPK({
        ExpressionAttributeValues: {
            ':pk': `USER#${username}`,
            ':sk': 'CONFIG#',
        },
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
            '#name': 'name',
        },
        ProjectionExpression:
            'username, #name, email, avatar, #status, #role, phone, companyId',
    });
}

async function updateStatus(username, companyId, status) {
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
            ':status': status,
        },
    });
}

async function insertConnectionId({ username, connectionId }) {
    await insert({
        partitionKey: `USER#${username}`,
        sortKey: `CONNECTION#${connectionId}`,
        connectionId,
    });
    return true;
}

async function deleteConnectionId(username, connectionId) {
    await remove({
        partitionKey: `USER#${username}`,
        sortKey: `CONNECTION#${connectionId}`,
    });
    return true;
}

module.exports = {
    fetchCompanyConnectionIDs,
    getUser,
    insertConnectionId,
    deleteConnectionId,
    getUsernameByConnectionId,
    updateStatus,
    getUserConnectionId,
};
