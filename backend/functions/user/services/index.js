const Cognito = require('./cognito');
const Database = require('./database');
const Lambda = require('./lambda');
const S3 = require('./s3');

module.exports = {
    Cognito,
    Database,
    Lambda,
    S3,
};
