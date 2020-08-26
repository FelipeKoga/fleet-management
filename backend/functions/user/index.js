const { list, getById, create, update, remove } = require('./resolver');

exports.handler = async ({ body, pathParameters, httpMethod }) => {
  const companyId = pathParameters.companyId;
  if (!companyId) return responsePayload(400, 'Missing companyId');
  try {
    const userId = pathParameters.userId;
    switch (httpMethod) {
      case 'GET':
        let response;
        if (userId) {
          response = await getById(companyId, userId);
        } else {
          response = await list(companyId);
        }
        if (response) {
          return responsePayload(200, response);
        }
        return responsePayload(400, 'Internal Error.');
      case 'POST':
        return responsePayload(200, await create(JSON.parse(body), companyId));
      case 'PUT':
        return responsePayload(
          200,
          await update(JSON.parse(body), userId, companyId)
        );
      case 'DELETE':
        return responsePayload(200, await remove(userId, companyId));
      default:
        return responsePayload(400, 'Internal Error Default.');
    }
  } catch (e) {
    return responsePayload(500, e.message);
  }
};

const responsePayload = (statusCode, data) => {
  return {
    statusCode,
    body: JSON.stringify(data),
  };
};
