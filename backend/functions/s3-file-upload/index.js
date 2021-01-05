const S3 = require('aws-sdk/clients/s3');

const s3 = new S3({
    signatureVersion: 'v4',
    region: 'us-east-1',
});

exports.handler = async ({ body }) => {
    const { bucket, key } = JSON.parse(body);
    const putURL = s3.getSignedUrl('putObject', {
        Bucket: bucket,
        Key: key,
        Expires: 3600,
    });
    const getURL = s3.getSignedUrl('getObject', {
        Bucket: bucket,
        Key: key,
        Expires: 3600,
    });

    return {
        statusCode: 200,
        body: JSON.stringify({ getURL, putURL }),
        headers: {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Credentials': true,
        },
    };
};
