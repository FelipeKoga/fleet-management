const { getByPK, fetchByPK, update } = require('./query');

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
    updateUserAvatar,
};
