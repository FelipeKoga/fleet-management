const nodemailer = require('nodemailer');

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'tcc.project.koga@gmail.com',
        pass: 'kbZyEnd44trYTzEU',
    },
});

const sendEmail = (to, subject, html) => {
    const params = {
        from: 'tcc.project.koga@gmail.com',
        to,
        subject,
        html,
    };

    return transporter.sendMail(params);
};

module.exports = { sendEmail };
