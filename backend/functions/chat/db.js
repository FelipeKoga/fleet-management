const { DynamoDB } = require('aws-sdk');
const docClient = new DynamoDB.DocumentClient();

const put = async (params) => {
  return docClient.put(params).promise();
};

const update = async (params) => {
  return docClient.update(params).promise();
};

const query = async (params) => {
  return docClient.query(params).promise();
};

module.exports = {
  put,
  update,
  query,
};
