#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { spawn } = require("child_process");
const jarPath = path.resolve(__dirname, "react-native-init-shadow.jar");
const args = process.argv.slice(2);
const cmd = spawn("java", ["-jar", jarPath].concat(args));

cmd.stdout.on("data", data => {
    console.log(`${data}`.trim());
});

cmd.stderr.on("data", data => {
    console.error(`${data}`.trim());
});

cmd.on('error', (error) => {
    console.error(`error: ${error.message}`.trim());
});

cmd.on("close", code => {
    if (code != 0) {
        console.log(`child process exited with code ${code}`.trim());
    }
});
