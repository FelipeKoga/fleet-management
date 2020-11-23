const CognitoIdentityServiceProvider = require('aws-sdk/clients/cognitoidentityserviceprovider');

const cognitoIdentityServiceProvider = new CognitoIdentityServiceProvider();
const POOL_ID = 'us-east-1_qtIxjoqVD';

async function create({ email, password, name }, companyId) {
    const params = {
        UserPoolId: POOL_ID,
        Username: email,
        MessageAction: 'SUPPRESS',
        TemporaryPassword: password,
        UserAttributes: [
            {
                Name: 'name',
                Value: name,
            },
            {
                Name: 'email',
                Value: email,
            },
            {
                Name: 'custom:companyId',
                Value: companyId,
            },
            {
                Name: 'email_verified',
                Value: 'true',
            },
        ],
    };

    await cognitoIdentityServiceProvider.adminCreateUser(params).promise();

    return cognitoIdentityServiceProvider
        .adminSetUserPassword({
            UserPoolId: POOL_ID,
            Username: email,
            Password: password,
            Permanent: true,
        })
        .promise();
}

async function remove(username) {
    return cognitoIdentityServiceProvider
        .adminDeleteUser({
            UserPoolId: POOL_ID,
            Username: username,
        })
        .promise();
}

module.exports = {
    create,
    remove,
};
