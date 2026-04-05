import assert from 'node:assert/strict'
import path from 'node:path'
import test from 'node:test'

import {
  getDevtoolsPath,
  getProjectPath,
  launchMiniProgram,
  waitForElement,
} from './support/miniprogram.mjs'

const devtoolsPath = getDevtoolsPath()

test('customer mini homepage can navigate to tickets page', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    const page = await miniProgram.reLaunch('/pages/index/index')
    const memberName = await waitForElement(page, '#home-member-name', 8000)
    const memberText = await memberName.text()

    assert.notEqual(memberText.trim(), '')

    const ticketsButton = await waitForElement(page, '#home-open-tickets')
    await ticketsButton.tap()
    await page.waitFor(800)

    const currentPage = await miniProgram.currentPage()
    assert.equal(currentPage.path, 'pages/tickets/index')

    const ticketsTitle = await waitForElement(currentPage, '#tickets-page-title', 5000)
    assert.equal((await ticketsTitle.text()).trim(), '选好服务，轻松到店')
  } finally {
    await miniProgram.close()
  }
})

test('project path points at the mini program root', () => {
  assert.equal(path.basename(getProjectPath()), 'customer-mini')
})