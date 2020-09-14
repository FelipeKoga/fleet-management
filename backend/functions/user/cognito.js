const { CognitoIdentityServiceProvider } = require('aws-sdk');

const service = new CognitoIdentityServiceProvider();
const POOL_ID = 'us-east-1_qtIxjoqVD';

const createCognitoUser = async ({ name, email, password }, companyId) => {
  console.log(email);
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

const deleteCognitoUser = async (username) => {
  return await service
    .adminDeleteUser({
      UserPoolId: POOL_ID,
      Username: username,
    })
    .promise();
};

module.exports = {
  createCognitoUser,
  deleteCognitoUser,
};
