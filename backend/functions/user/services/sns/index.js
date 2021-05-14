const SNS = require('aws-sdk/clients/sns');
const admin = require('firebase-admin');

const serviceAccount = require('./sdk.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
});

class NotificationService {
    constructor() {
        this.sns = new SNS();
    }

    async createEndpoint(token) {
        const response = await this.sns
            .createPlatformEndpoint({
                PlatformApplicationArn:
                    'arn:aws:sns:us-east-1:603366505042:app/GCM/tcc-project',
                Token: token,
            })
            .promise();
        return response.EndpointArn;
    }

    async removeEndpoint(token) {
        this.sns
            .deletePlatformApplication({
                PlatformApplicationArn: token,
            })
            .promise();
    }

    async send(token, data) {
        return this.sns
            .publish({
                Message: JSON.stringify({
                    GCM: JSON.stringify({
                        data,
                    }),
                }),
                MessageStructure: 'json',
                TargetArn: token,
            })
            .promise();
    }
}

module.exports = NotificationService;
