/* eslint-disable eqeqeq */
const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');

const execute = command => {
    console.log(`RUNNING -> ${command}`);

    exec(command, (error, stdout, stderr) => {
        if (error) {
            console.log(error.message);
            console.log(`ERROR -> ${stdout}`);
            return;
        }
        if (stderr) {
            console.log(stderr);
            console.log(`ERROR -> ${stdout}`);
            return;
        }
        console.log(`SUCCESS -> \n${stdout}`);
    });
};

function fileExists(_path) {
    try {
        return fs.statSync(_path).isFile();
    } catch (e) {
        if (e.code == 'ENOENT') {
            console.log(`File ${path.basename(_path)} does not exist.`);
            return false;
        }

        console.log(`Exception fs.statSync (${_path}): ${e}`);
        throw e;
    }
}

module.exports = {
    execute,
    fileExists,
};
