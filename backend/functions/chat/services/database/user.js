const { getByPK, fetchByPK } = require('./query');

async function getUser(username) {
    return getByPK(
        {
            ExpressionAttributeValues: {
                ':pk': `USER#${username}`,
                ':sk': 'CONFIG#',
            },
        },
        false,
    );
}

async function fetchConnectionIds(username) {
    const connectionIds = await fetchByPK(
        {
            ExpressionAttributeValues: {
                ':pk': `USER#${username}`,
                ':sk': 'CONNECTION#',
            },
            ProjectionExpression: 'connectionId',
        },
        false,
    );

    return connectionIds.map(res => res.connectionId);
}

module.exports = {
    getUser,
    fetchConnectionIds,
};
