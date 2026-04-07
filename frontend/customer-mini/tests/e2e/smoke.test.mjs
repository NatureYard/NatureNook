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
    if (!app.globalData.e2eAutoConfirmModal) {
      const originalShowModal = wx.showModal
      wx.showModal = function(options = {}) {
        if (typeof options.success === 'function') {
          options.success({ confirm: true, cancel: false })
        }
        if (typeof options.complete === 'function') {
          options.complete({ confirm: true, cancel: false })
        }
        return typeof originalShowModal === 'function'
          ? Promise.resolve({ errMsg: 'showModal:ok', confirm: true, cancel: false })
          : Promise.resolve({ errMsg: 'showModal:ok', confirm: true, cancel: false })
      }
      app.globalData.e2eAutoConfirmModal = true
    }
    return true
  })
}

async function resetE2EMockState(miniProgram, overrides = {}) {
  await miniProgram.evaluate((stateOverrides) => {
    const app = getApp()
    app.globalData = app.globalData || {}
    app.globalData.e2eMockMode = true
    app.globalData.e2eMockState = {
      nextOrderId: 1,
      nextPassId: 1,
      memberName: '张三',
      memberLevel: 'SILVER',
      storeName: '上海萌宠乐园旗舰店',
      banners: ['周末寄养优惠', '年卡限时折扣', '洗护套餐立减'],
      quickEntries: ['购票预约', '卡种中心', '我的宠物', '入园凭证'],
      activeCardTip: '年卡会员可在有效期内多次入园，现场只需亮出凭证即可快速核验。',
      pets: [
        { id: 1, name: '奶球' },
        { id: 3, name: '可乐' },
      ],
      products: [
        { code: 'DAY_TICKET', name: '单次门票', desc: '支持当日多次出入', price: '68.00', type: 'TICKET' },
        { code: 'GROOMING_PACKAGE', name: '洗护套餐', desc: '含基础洗护与护理', price: '128.00', type: 'GROOMING' },
        { code: 'BOARDING_DAY', name: '寄养预约', desc: '按天预约，可追加喂养服务', price: '188.00', type: 'BOARDING' },
      ],
      orders: [],
      passes: [],
      ...stateOverrides,
    }
    return true
  }, overrides)
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
    assert.equal((await ticketsTitle.text()).trim(), '选择服务')
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
    assert.equal((await initialProduct.text()).trim(), '请选择服务项目')

    const boardingButton = await waitForElement(page, '#select-product-BOARDING_DAY', 5000)
    await boardingButton.tap()
    await page.waitFor(500)

    const selectedProduct = await waitForElement(page, '#selected-product-name', 5000)
    const selectedIndicator = await waitForElement(page, '#product-check-BOARDING_DAY', 5000)

    assert.equal((await selectedProduct.text()).trim(), '已选：寄养预约')
    assert.equal((await selectedIndicator.text()).trim(), '已选')
  } finally {
    await miniProgram.close()
  }
})

test('tickets page can submit reservation in e2e mock mode', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await enableE2EMockMode(miniProgram)
    const page = await miniProgram.reLaunch('/pages/tickets/index')
    const boardingButton = await waitForElement(page, '#select-product-BOARDING_DAY', 8000)
    await boardingButton.tap()

    const stepOneNextButton = await waitForElement(page, '#tickets-next-step-1', 5000)
    await stepOneNextButton.tap()
    await page.waitFor(500)

    const stepTwoTitle = await waitForElement(page, '#tickets-page-title', 5000)
    assert.equal((await stepTwoTitle.text()).trim(), '预约信息')

    const timeSlot = await waitForElement(page, '#ticket-slot-0', 5000)
    await timeSlot.tap()

    const stepTwoNextButton = await waitForElement(page, '#tickets-next-step-2', 5000)
    await stepTwoNextButton.tap()
    await page.waitFor(500)

    const confirmTitle = await waitForElement(page, '#tickets-page-title', 5000)
    const confirmProduct = await waitForElement(page, '#tickets-confirm-product-name', 5000)
    assert.equal((await confirmTitle.text()).trim(), '确认预约')
    assert.equal((await confirmProduct.text()).trim(), '寄养预约')

    const submitButton = await waitForElement(page, '#tickets-submit', 8000)

    await submitButton.tap()
    await page.waitFor(1200)

    const currentPage = await miniProgram.currentPage()
    assert.equal(currentPage.path, 'pages/orders/index')

    const ordersTitle = await waitForElement(currentPage, '#orders-page-title', 5000)
    const orderType = await waitForElement(currentPage, '#order-type-0', 5000)

    assert.equal((await ordersTitle.text()).trim(), '我的订单')
    assert.equal((await orderType.text()).trim(), '寄养预约')
  } finally {
    await miniProgram.close()
  }
})

test('home page can open profile center in e2e mock mode', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await enableE2EMockMode(miniProgram)
    const page = await miniProgram.reLaunch('/pages/index/index')
    const profileButton = await waitForElement(page, '#home-open-profile', 8000)

    await profileButton.tap()
    await page.waitFor(800)

    const currentPage = await miniProgram.currentPage()
    assert.equal(currentPage.path, 'pages/profile/index')

    const memberName = await waitForElement(currentPage, '#profile-member-name', 5000)

    assert.equal((await memberName.text()).trim(), '张三')
  } finally {
    await miniProgram.close()
  }
})

test('home page can open cards center in e2e mock mode', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await enableE2EMockMode(miniProgram)
    const page = await miniProgram.reLaunch('/pages/index/index')
    const cardsEntry = await waitForElement(page, '#home-open-cards', 8000)

    await cardsEntry.tap()
    await page.waitFor(800)

    const currentPage = await miniProgram.currentPage()
    assert.equal(currentPage.path, 'pages/cards/index')

    const cardsTitle = await waitForElement(currentPage, '#cards-page-title', 5000)
    const firstCardName = await waitForElement(currentPage, '#membership-card-name-0', 5000)
    const firstCardPrice = await waitForElement(currentPage, '#membership-card-price-0', 5000)
    const thirdCardName = await waitForElement(currentPage, '#membership-card-name-2', 5000)
    const thirdCardBadge = await waitForElement(currentPage, '#membership-card-badge-2', 5000)

    assert.equal((await cardsTitle.text()).trim(), '卡种中心')
    assert.equal((await firstCardName.text()).trim(), '月卡')
    assert.equal((await firstCardPrice.text()).trim(), '¥399')
    assert.equal((await thirdCardName.text()).trim(), '年卡')
    assert.equal((await thirdCardBadge.text()).trim(), '可购买')
  } finally {
    await miniProgram.close()
  }
})

test('home page can open orders center in e2e mock mode', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await enableE2EMockMode(miniProgram)
    const page = await miniProgram.reLaunch('/pages/index/index')
    const ordersEntry = await waitForElement(page, '#home-open-orders', 8000)

    await ordersEntry.tap()
    await page.waitFor(800)

    const currentPage = await miniProgram.currentPage()
    assert.equal(currentPage.path, 'pages/orders/index')

    const ordersTitle = await waitForElement(currentPage, '#orders-page-title', 5000)
    assert.equal((await ordersTitle.text()).trim(), '我的订单')
  } finally {
    await miniProgram.close()
  }
})

test('orders empty state can navigate back to tickets in e2e mock mode', { skip: !devtoolsPath && 'WECHAT_DEVTOOLS_PATH is not configured' }, async () => {
  const miniProgram = await launchMiniProgram()

  try {
    await resetE2EMockState(miniProgram, { orders: [], passes: [] })
    const page = await miniProgram.reLaunch('/pages/orders/index')
    const openTicketsButton = await waitForElement(page, '#orders-open-tickets', 8000)

    await openTicketsButton.tap()
    await page.waitFor(800)

    const currentPage = await miniProgram.currentPage()
    assert.equal(currentPage.path, 'pages/tickets/index')

    const ticketsTitle = await waitForElement(currentPage, '#tickets-page-title', 5000)
    assert.equal((await ticketsTitle.text()).trim(), '选择服务')
  } finally {
    await miniProgram.close()
  }
})

test('project path points at the mini program root', () => {
  assert.equal(path.basename(getProjectPath()), 'customer-mini')
})