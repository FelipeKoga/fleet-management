const S3 = require('aws-sdk/clients/s3');

const s3 = new S3({
    signatureVersion: 'v4',
    region: 'us-east-1',
});

const getObject = key => {
    return s3.getSignedUrl('getObject', {
        Bucket: 'tcc-project-assets',
        Key: key,
        Expires: 604800,
    });
};

module.exports = {
    getObject,
};
