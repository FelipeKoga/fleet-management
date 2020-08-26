const { CognitoIdentityServiceProvider } = require('aws-sdk');

const service = new CognitoIdentityServiceProvider();
const POOL_ID = 'us-east-1_KVttzPJIM';

const createCognitoUser = async ({ name, email, password }, companyId) => {
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
  await service.adminCreateUser(params).promise();
  const setUserPasswordResponse = await service
    .adminSetUserPassword({
      UserPoolId: POOL_ID,
      Username: email,
      Password: password,
      Permanent: true,
    })
    .promise();
  if (setUserPasswordResponse) return true;
  return false;
};

const deleteCognitoUser = async (email) => {
  return await service
    .adminDeleteUser({
      UserPoolId: POOL_ID,
      Username: email,
    })
    .promise();
};

module.exports = {
  createCognitoUser,
  deleteCognitoUser,
};
