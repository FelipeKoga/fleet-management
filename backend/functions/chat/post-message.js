const { DynamoDB, ApiGatewayManagementApi } = require('aws-sdk');
const docClient = new DynamoDB.DocumentClient();
const CONNECTION_TABLE = 'connection';

const list = async (companyId) => {
  const params = {
    TableName: CONNECTION_TABLE,
    IndexName: 'companyIdIndex',
    KeyConditionExpression: 'companyId = :cId',
    ExpressionAttributeValues: {
      ':cId': companyId,
    },
  };
  return await docClient.query(params).promise();
};

const sendToAllConnected = async (data, action) => {
  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: process.env.WS_URL,
  });
  const responseList = await list(data.companyId);
  const requests = [];
  responseList.Items.forEach((item) => {
    const params = {
      ConnectionId: item.connectionId,
      Data: JSON.stringify({
        action,
        data,
      }),
    };
    requests.push(
      apigwManagementApi
        .postToConnection(params)
        .promise()
        .catch((e) => console.log(e))
    );
  });

  return Promise.all(requests);
};

module.exports = sendToAllConnected;
