const { insert, getByPK } = require('./query');

async function add(username, { latitude, longitude, lastUpdate }) {
    await insert({
        partitionKey: `USER#${username}`,
        sortKey: `LOCATION#${lastUpdate}`,
        latitude,
        longitude,
        lastUpdate,
    });
}

async function getLastLocation(username) {
    return getByPK({
        ExpressionAttributeValues: {
            ':pk': `USER#${username}`,
            ':sk': `LOCATION#`,
        },

        ProjectionExpression: 'latitude, longitude, lastUpdate',
        Limit: 1,
        ScanIndexForward: false,
    });
}

module.exports = {
    add,
    getLastLocation,
};
