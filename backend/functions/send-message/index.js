const { DynamoDB, ApiGatewayManagementApi } = require('aws-sdk');

const CONNECTION_TABLE = 'connection';
const docClient = new DynamoDB.DocumentClient();

exports.handler = async (event) => {
  console.log(event);
  return {
    statusCode: 200,
    body: await sendMessage(event),
  };
};

const sendMessage = async (event) => {
  const response = await getConnectionIds();

  console.log(response);
  const requests = [];
  response.Items.forEach((connectionId) => {
    requests.push(send(event, connectionId.connectionId));
  });

  await Promise.all(requests);
  return 'Ok';
};

const send = async (event, connectionId) => {
  const body = JSON.parse(event.body);
  const postData = body.data;

  const endpoint = 'https://ga7fxhoxu4.execute-api.us-east-1.amazonaws.com/dev';

  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: endpoint,
  });

  const params = {
    ConnectionId: connectionId,
    Data: JSON.stringify({
      message: postData,
      companyId: 1,
      recipient: 1,
      sender: 2,
    }),
  };

  return apigwManagementApi
    .postToConnection(params)
    .promise()
    .catch((e) => console.log(e));
};

const getConnectionIds = async () => {
  const params = {
    TableName: CONNECTION_TABLE,
    ProjectionExpression: 'connectionId',
  };

  return await docClient.scan(params).promise();
};
