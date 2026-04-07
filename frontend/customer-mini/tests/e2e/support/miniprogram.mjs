import path from 'node:path'
import { fileURLToPath } from 'node:url'
import automator from 'miniprogram-automator'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const CLI_WRAPPER_PATH = path.resolve(__dirname, 'wechat-cli-wrapper.cjs')

export function getProjectPath() {
  return process.env.MINIPROGRAM_PROJECT_PATH || path.resolve(__dirname, '../../..')
}

export function getDevtoolsPath() {
  return process.env.WECHAT_DEVTOOLS_PATH || ''
}

function getLaunchCliConfig() {
  const cliPath = getDevtoolsPath()

  if (process.platform !== 'win32' && cliPath.toLowerCase().endsWith('.bat')) {
    return {
      cliPath: 'node',
      args: [CLI_WRAPPER_PATH, cliPath],
    }
  }

  return {
    cliPath,
    args: [],
  }
}

export async function launchMiniProgram() {
  const launchCliConfig = getLaunchCliConfig()

  return automator.launch({
    projectPath: getProjectPath(),
    cliPath: launchCliConfig.cliPath,
    args: launchCliConfig.args,
  })
}

export async function waitForElement(page, selector, timeout = 5000) {
  const startTime = Date.now()
  while (Date.now() - startTime < timeout) {
    const element = await page.$(selector)
    if (element) {
      return element
    }
    await page.waitFor(200)
  }

  throw new Error(`Timed out waiting for selector: ${selector}`)
}