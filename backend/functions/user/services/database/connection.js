const { fetchBySK, fetchByPK, remove } = require('./query');

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

async function getUserConnectionIds(username) {
    return fetchByPK({
        ExpressionAttributeValues: {
            ':pk': `USER#${username}`,
            ':sk': 'CONNECTION#',
        },
        ProjectionExpression: 'connectionId',
    });
}

async function removeConnectionIds(username) {
    const connectionIds = await getUserConnectionIds(username);
    const requests = [];
    connectionIds.forEach(connectionId => {
        requests.push(
            remove({
                partitionKey: `USER#${username}`,
                sortKey: `CONNECTION#${connectionId}`,
            }),
        );
    });
    await Promise.all(requests);
}

module.exports = {
    fetchCompanyConnectionIDs,
    removeConnectionIds,
};
