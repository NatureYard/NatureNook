import path from 'node:path'
import { fileURLToPath } from 'node:url'
import automator from 'miniprogram-automator'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

export function getProjectPath() {
  return process.env.MINIPROGRAM_PROJECT_PATH || path.resolve(__dirname, '../../..')
}

export function getDevtoolsPath() {
  return process.env.WECHAT_DEVTOOLS_PATH || ''
}

export async function launchMiniProgram() {
  return automator.launch({
    projectPath: getProjectPath(),
    cliPath: getDevtoolsPath(),
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