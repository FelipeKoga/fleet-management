const { list, getUser, create, update, remove } = require('./resolver');

const responsePayload = (statusCode, data) => {
  return {
    statusCode,
    body: JSON.stringify(data),
  };
};

exports.handler = async (event) => {
  const { body, pathParameters, httpMethod, resource } = event;

  console.log(event);

  if (!pathParameters) {
    throw new Error('Missing path parameters');
  }

  const { companyId, username } = pathParameters;
  if (!companyId && resource !== '/user/{username}')
    throw new Error('Missing company ID');

  switch (httpMethod) {
    case 'GET':
      if (username) {
        return responsePayload(200, await getUser(username));
      }
      return responsePayload(200, await list(companyId));
    case 'POST':
      return responsePayload(200, await create(body, companyId));
    case 'PUT':
      return responsePayload(200, await update(body, username, companyId));
    case 'DELETE':
      return responsePayload(200, await remove(username, companyId));
    default:
      return responsePayload(400, 'Method not allowed');
  }
};
