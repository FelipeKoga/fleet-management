/* eslint-disable no-unused-expressions */
const { execute, fileExists } = require('./utils');

const functionName = process.argv[2];
const fileName = process.argv[3];
const enviromentVariables = process.argv[4];

if (!functionName || !fileName || !enviromentVariables) {
    process.exit(0);
}

const path = `functions/${functionName}/tests/${fileName}.json`;
fileExists(path) &&
    execute(
        `dotenv-load serverless invoke local --function ${functionName} --path ${path} ${enviromentVariables}`,
    );
