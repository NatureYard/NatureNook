function getAppSafe() {
  try {
    return getApp()
  } catch (error) {
    return null
  }
}

function clone(data) {
  return JSON.parse(JSON.stringify(data))
}

function createInitialState() {
  return {
    nextOrderId: 1,
    nextPassId: 1,
    memberName: '张三',
    memberLevel: 'SILVER',
    storeName: '上海萌宠乐园旗舰店',
    banners: ['周末寄养优惠', '年卡限时折扣', '洗护套餐立减'],
    quickEntries: ['购票 / 预约', '卡种中心', '我的宠物', '入园凭证'],
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
  }
}

function ensureState() {
  const app = getAppSafe()
  if (!app) {
    return createInitialState()
  }

  app.globalData = app.globalData || {}
  if (!app.globalData.e2eMockState) {
    app.globalData.e2eMockState = createInitialState()
  }

  return app.globalData.e2eMockState
}

function createReservationArtifacts(state, payload = {}) {
  const product = state.products.find((item) => item.code === payload.ticketCode) || state.products[0]
  const orderId = state.nextOrderId++
  const passId = state.nextPassId++
  const orderNo = `MOCK${String(orderId).padStart(8, '0')}`
  const reservationDate = payload.reservationDate || '2026-04-06'
  const timeSlot = payload.timeSlot || '09:00-12:00'
  const amount = product?.price || '68.00'

  const order = {
    orderNo,
    type: product?.name || '单次门票',
    status: 'PAID',
    amount,
    storeName: state.storeName,
    reservationDate,
    timeSlot,
  }

  const pass = {
    id: passId,
    name: `${product?.name || '单次门票'}入园资格`,
    status: 'ACTIVE',
    storeName: state.storeName,
    reentryPolicy: 'SAME_DAY_UNLIMITED',
    validFrom: `${reservationDate}T00:00:00`,
    validTo: `${reservationDate}T23:59:59`,
  }

  state.orders.unshift(order)
  state.passes.unshift(pass)

  return {
    orderNo,
    status: 'PAID',
  }
}

function buildHomeResponse(state) {
  return {
    banners: clone(state.banners),
    quickEntries: clone(state.quickEntries),
    activeCardTip: state.activeCardTip,
  }
}

function buildProfileResponse(state) {
  return {
    memberName: state.memberName,
    memberLevel: state.memberLevel,
    storeName: state.storeName,
    items: [
      `会员等级：${state.memberLevel}`,
      '活跃卡种：1',
      `累计订单：${state.orders.length}`,
      '人脸录入状态：已录入',
    ],
  }
}

function buildContextResponse(state) {
  return {
    memberName: state.memberName,
    storeName: state.storeName,
    pets: clone(state.pets),
  }
}

function buildCardsResponse() {
  return [
    { id: 1, name: '月卡', desc: '30 天内多次入园', status: '可购买', validTo: '2026-05-31', price: '399' },
    { id: 2, name: '季卡', desc: '90 天内多次入园', status: '可购买', validTo: '2026-08-31', price: '999' },
    { id: 3, name: '年卡', desc: '365 天内多次入园', status: '可购买', validTo: '2027-04-06', price: '1288' },
  ]
}

function isE2EMockEnabled() {
  const app = getAppSafe()
  return Boolean(app?.globalData?.e2eMockMode)
}

function resolveE2EMockRequest(path, options = {}) {
  const state = ensureState()
  const method = (options.method || 'GET').toUpperCase()

  if (path === '/api/c-app/home' && method === 'GET') {
    return buildHomeResponse(state)
  }

  if (path === '/api/c-app/profile' && method === 'GET') {
    return buildProfileResponse(state)
  }

  if (path === '/api/c-app/orders' && method === 'GET') {
    return clone(state.orders)
  }

  if (path === '/api/c-app/passes' && method === 'GET') {
    return clone(state.passes)
  }

  if (path === '/api/c-app/tickets' && method === 'GET') {
    return clone(state.products)
  }

  if (path === '/api/c-app/context' && method === 'GET') {
    return buildContextResponse(state)
  }

  if (path === '/api/c-app/cards' && method === 'GET') {
    return buildCardsResponse()
  }

  if (path === '/api/c-app/reservations' && method === 'POST') {
    return createReservationArtifacts(state, options.data)
  }

  throw new Error(`E2E mock route not implemented: ${method} ${path}`)
}

module.exports = {
  isE2EMockEnabled,
  resolveE2EMockRequest,
}