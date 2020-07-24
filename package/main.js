const { spawn } = require("child_process");

console.log(process.argv);

const args = process.argv.slice(2);
const cmd = spawn("java", ["-jar", "react-native-init-shadow.jar"].concat(args));

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
