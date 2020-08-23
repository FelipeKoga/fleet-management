const { DynamoDB } = require("aws-sdk");
const { v4 } = require("uuid");

const docClient = new DynamoDB.DocumentClient();
const TableName = "user";

exports.handler = async ({ field, arguments }) => {
  switch (field) {
    case "getUsers":
      return await getUsers(arguments);
    case "putUser":
      return await putUser(arguments);
  }
};

const getUsers = async ({ companyId }) => {
  const params = {
    TableName,
    IndexName: "companyId-index",
    KeyConditionExpression: "companyId = :cId",
    ExpressionAttributeValues: {
      ":cId": companyId,
    },
  };
  return await docClient.query(params).promise();
};

const putUser = async ({ name, companyId }) => {
  const params = {
    TableName,
    Item: {
      uuid: v4(),
      name,
      companyId,
    },
  };
  return await docClient.put(params).promise();
};

const deleteUser = ({ companyId }) => {};
