const { ApiGatewayManagementApi } = require('aws-sdk');
const broadcast = require('./broadcast');

exports.handler = async ({ method, data }) => {
  console.log(data);
  const apigwManagementApi = new ApiGatewayManagementApi({
    apiVersion: '2018-11-29',
    endpoint: process.env.WS_URL,
  });

  if (method === 'broadcast') {
    return await broadcast(data, apigwManagementApi);
  }

  throw new Error('Method not allowed');
};
