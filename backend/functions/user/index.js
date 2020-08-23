const { list, getById, create, update, remove } = require('./resolver');

exports.handler = async (event) => {
  console.log(event);
  let response;
  if (event.httpMethod === 'GET') {
    const pathParameters = event.pathParameters;
    if (pathParameters && pathParameters.userId) {
      response = await getById(pathParameters.userId);
    } else {
      response = await list();
    }
  } else if (event.requestContext.routeKey === 'post-user') {
    console.log('Entrei');
    response = await create({
      name: 'Vinicius',
      email: 'vinicius@email.com',
      companyId: '1',
    });
  } else if (event.requestContext.routeKey === 'put-user') {
    response = await update({
      id: '1',
      name: 'Felipe 2',
      email: 'vinicius2@email.com',
      companyId: '1',
    });
  } else if (event.requestContext.routeKey === 'delete-user') {
    response = await remove('1');
  }

  return {
    statusCode: 200,
    body: JSON.stringify(response),
  };
};

// const putUser = async ({ name, companyId }) => {
//   const params = {
//     TableName: USER_TABLE,
//     Item: {
//       id: v4(),
//       name,
//       companyId,
//     },
//   };
//   return await docClient.put(params).promise();
// };

// const deleteUser = async ({ id }) => {
//   const params = {
//     TableName: USER_TABLE,
//     ConditionExpression: 'id = :id',
//     ExpressionAttributeValues: {
//       ':id': id,
//     },
//   };
//   return await docClient.delete(params).promise();
// };

// const getConnectionIds = async () => {
//   const params = {
//     TableName: CONNECTION_TABLE,
//     ProjectionExpression: 'connectionId',
//   };

//   return await docClient.scan(params).promise();
// };

// const send = async (items, connectionId) => {
//   const endpoint = process.env.API_URL;

//   const apigwManagementApi = new ApiGatewayManagementApi({
//     apiVersion: '2018-11-29',
//     endpoint: endpoint,
//   });

//   const params = {
//     ConnectionId: connectionId,
//     Data: JSON.stringify(items),
//   };

//   console.log(params);
//   return apigwManagementApi
//     .postToConnection(params)
//     .promise()
//     .catch((e) => console.log(e));
// };
