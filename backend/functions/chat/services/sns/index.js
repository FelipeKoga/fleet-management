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

    async send(token, { title, body, chatId }) {
        return this.sns
            .publish({
                Message: JSON.stringify({
                    GCM: JSON.stringify({
                        notification: {
                            title,
                            body,
                        },
                        data: {
                            title,
                            body,
                            chatId,
                        },
                    }),
                }),
                MessageStructure: 'json',
                TargetArn: `${token}`,
            })
            .promise()
            .catch(e => console.log(e));
    }
}

module.exports = NotificationService;
