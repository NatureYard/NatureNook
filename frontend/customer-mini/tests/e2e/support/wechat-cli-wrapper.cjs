const { spawn } = require('node:child_process')

function toWindowsPath(filePath) {
  if (filePath.startsWith('/mnt/') && filePath.length > 6) {
    const driveLetter = filePath[5].toUpperCase()
    const relativePath = filePath.slice(7).replace(/\//g, '\\')
    return `${driveLetter}:\\${relativePath}`
  }

  return filePath.replace(/\//g, '\\')
}

const cliPath = process.argv[2]
const cliArgs = process.argv.slice(3)

if (!cliPath) {
  console.error('Missing WeChat DevTools CLI path')
  process.exit(1)
}

const child = spawn('cmd.exe', ['/c', toWindowsPath(cliPath), ...cliArgs], {
  stdio: 'ignore',
})

child.on('error', (error) => {
  console.error(error)
  process.exit(1)
})

child.on('exit', (code) => {
  process.exit(code ?? 0)
})