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

async function enableE2EMockMode(miniProgram) {
  await miniProgram.evaluate(() => {
    const app = getApp()
    app.globalData = app.globalData || {}
    app.globalData.e2eMockMode = true
    app.globalData.e2eMockState = null
    return true
  })
}

test('customer mini homepage can navigate to tickets page', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await enableE2EMockMode(miniProgram)
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

test('tickets page can switch selected product to boarding', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await enableE2EMockMode(miniProgram)
    const page = await miniProgram.reLaunch('/pages/tickets/index')
    const initialProduct = await waitForElement(page, '#selected-product-name', 8000)
    assert.equal((await initialProduct.text()).trim(), '单次门票')

    const boardingButton = await waitForElement(page, '#select-product-BOARDING_DAY', 5000)
    await boardingButton.tap()
    await page.waitFor(500)

    const selectedProduct = await waitForElement(page, '#selected-product-name', 5000)
    const selectedPrice = await waitForElement(page, '#selected-product-price', 5000)

    assert.equal((await selectedProduct.text()).trim(), '寄养预约')
    assert.equal((await selectedPrice.text()).trim(), '¥188.00')
  } finally {
    await miniProgram.close()
  }
})

test('tickets page can submit reservation in e2e mock mode', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await enableE2EMockMode(miniProgram)
    const page = await miniProgram.reLaunch('/pages/tickets/index')
    const submitButton = await waitForElement(page, '#tickets-submit', 8000)

    await submitButton.tap()
    await page.waitFor(1200)

    const currentPage = await miniProgram.currentPage()
    assert.equal(currentPage.path, 'pages/orders/index')

    const ordersTitle = await waitForElement(currentPage, '#orders-page-title', 5000)
    const ordersCount = await waitForElement(currentPage, '#orders-count-value', 5000)

    assert.equal((await ordersTitle.text()).trim(), '订单与入园凭证')
    assert.equal((await ordersCount.text()).trim(), '1')
  } finally {
    await miniProgram.close()
  }
})

test('project path points at the mini program root', () => {
  assert.equal(path.basename(getProjectPath()), 'customer-mini')
})