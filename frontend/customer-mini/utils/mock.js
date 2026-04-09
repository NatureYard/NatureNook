function formatToDateStr(date) {
  return date.getFullYear() + '-' +
    String(date.getMonth() + 1).padStart(2, '0') + '-' +
    String(date.getDate()).padStart(2, '0')
}

var MOCK_ORDER_STORAGE_KEY = 'mcly_mock_orders_v_' + formatToDateStr(new Date());
function getMockTickets() {
  return [
    {
      code: 'DAY_TICKET',
      name: '单次门票',
      desc: '支持当日多次出入，含基础互动体验',
      price: '68.00',
      type: 'TICKET',
      tags: ['热门'],
    },
    {
      code: 'GROOMING_PACKAGE',
      name: '洗护套餐',
      desc: '含基础洗护与毛发护理，专业宠物美容师服务',
      price: '128.00',
      type: 'GROOMING',
      tags: [],
    },
    {
      code: 'BOARDING_DAY',
      name: '寄养预约',
      desc: '按天预约，含喂养照看，可追加互动陪玩服务',
      price: '188.00',
      type: 'BOARDING',
      tags: ['需提前预约'],
    },
  ]
}

function getMockPets() {
  return [
    {
      id: 1,
      name: '奶球',
      species: 'DOG',
      breed: '柯基',
      gender: 'FEMALE',
      weight: '8.50',
      birthday: '2024-03-15',
      vaccinated: true,
    },
    {
      id: 3,
      name: '可乐',
      species: 'DOG',
      breed: '比熊',
      gender: 'MALE',
      weight: '6.20',
      birthday: '2023-11-20',
      vaccinated: true,
    },
  ]
}

function getMockCards() {
  return [
    {
      id: 1,
      name: '年卡',
      desc: '365 天内不限次数入园，尊享会员专属福利',
      price: '1288',
      status: 'ACTIVE',
      validFrom: '2026-04-05',
      validTo: '2027-04-05',
      benefits: ['不限次入园', '洗护 9 折', '寄养优先'],
    },
  ]
}



function buildDefaultOrders() {
  var now = new Date()
  var todayStr = formatToDateStr(now)
  var yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  var yesterdayStr = formatToDateStr(yesterday)

  return [
    {
      id: 1,
      orderNo: 'ORD' + todayStr.replace(/-/g, '') + '0001',
      type: '单次门票',
      status: 'PAID',
      amount: '68.00',
      storeName: '上海萌宠乐园旗舰店',
      reservationDate: todayStr,
      timeSlot: '09:00-12:00',
      createdAt: yesterdayStr + ' 14:30',
      passEntitlementId: 'PASS-MOCK-001',
    },
    {
      id: 2,
      orderNo: 'ORD202604050002',
      type: '年卡',
      status: 'PAID',
      amount: '1288.00',
      storeName: '上海萌宠乐园旗舰店',
      reservationDate: '',
      timeSlot: '',
      createdAt: yesterdayStr + ' 10:15',
      passEntitlementId: 'PASS-MOCK-002',
    },
    {
      id: 3,
      orderNo: 'ORD202604030001',
      type: '洗护套餐',
      status: 'USED',
      amount: '128.00',
      storeName: '上海萌宠乐园旗舰店',
      reservationDate: yesterdayStr,
      timeSlot: '13:00-15:00',
      createdAt: yesterdayStr + ' 09:00',
    },
    {
      id: 4,
      orderNo: 'ORD202603280001',
      type: '寄养预约',
      status: 'CANCELLED',
      amount: '188.00',
      storeName: '上海萌宠乐园旗舰店',
      reservationDate: yesterdayStr,
      timeSlot: '09:00-12:00',
      createdAt: yesterdayStr + ' 16:45',
    },
  ]
}

function readMockOrders() {
  try {
    var saved = wx.getStorageSync(MOCK_ORDER_STORAGE_KEY)
    if (saved && saved.length) {
      return saved
    }
  } catch (error) {
    // Ignore storage read issues in demo mode.
  }

  var defaultOrders = buildDefaultOrders()
  writeMockOrders(defaultOrders)
  return defaultOrders
}

function writeMockOrders(orders) {
  try {
    wx.setStorageSync(MOCK_ORDER_STORAGE_KEY, orders)
  } catch (error) {
    // Ignore storage write issues in demo mode.
  }
}

function getMockHome() {
  return {
    banners: [
      { title: '周末寄养优惠', subtitle: '本周六日寄养服务立减 30 元', tag: '限时' },
      { title: '年卡限时折扣', subtitle: '新会员首购年卡享 8 折优惠', tag: '推荐' },
      { title: '春季洗护套餐', subtitle: '换季护理，毛发焕新', tag: '新品' },
    ],
    quickEntries: ['购票预约', '卡种中心', '我的宠物', '入园凭证'],
    activeCardTip: '当前为开发演示数据，可在后端启动后切换到真实接口。',
  }
}

function getMockContext() {
  return {
    memberId: 1,
    memberName: '张三',
    memberLevel: 'SILVER',
    storeId: 1,
    storeName: '上海萌宠乐园旗舰店',
    pets: getMockPets(),
    cards: getMockCards(),
  }
}

function getMockProfile() {
  return {
    memberName: '张三',
    memberLevel: 'SILVER',
    storeName: '上海萌宠乐园旗舰店',
    phone: '138****6789',
    faceStatus: 'REGISTERED',
    joinDate: '2026-01-15',
  }
}

function getMockOrders() {
  return readMockOrders()
}

function createMockReservation(payload) {
  var orders = readMockOrders()
  var tickets = getMockTickets()
  var selectedTicket = null
  var index = 0

  for (; index < tickets.length; index += 1) {
    if (tickets[index].code === payload.ticketCode) {
      selectedTicket = tickets[index]
      break
    }
  }

  if (!selectedTicket) {
    selectedTicket = tickets[0]
  }

  var orderNo = 'ORD' + String(payload.reservationDate || '').replace(/-/g, '') + String(Date.now()).slice(-6)
  var newOrder = {
    id: orders.length + 1,
    orderNo: orderNo,
    type: selectedTicket.name,
    status: 'PENDING_PAY',
    amount: selectedTicket.price,
    storeName: '上海萌宠乐园旗舰店',
    reservationDate: payload.reservationDate,
    timeSlot: payload.timeSlot,
    createdAt: new Date().toLocaleString('zh-CN'),
  }

  writeMockOrders([newOrder].concat(orders))

  return {
    reservationId: Date.now(),
    orderId: Date.now() + 1,
    orderNo: orderNo,
    status: 'PENDING_PAY',
  }
}

/**
 * 模拟预支付参数。
 */
function getMockPrepay(orderNo) {
  return {
    timeStamp: String(Math.floor(Date.now() / 1000)),
    nonceStr: 'mock_nonce_' + Date.now(),
    packageValue: 'prepay_id=wx_mock_' + orderNo,
    signType: 'RSA',
    paySign: 'mock_sign_dev',
    orderNo: orderNo,
    amount: '0.00',
  }
}

/**
 * 模拟确认支付：将 mock 订单状态从 PENDING_PAY 改为 PAID。
 */
function confirmMockPayment(orderNo) {
  var orders = readMockOrders()
  var updated = orders.map(function (o) {
    if (o.orderNo === orderNo && o.status === 'PENDING_PAY') {
      return Object.assign({}, o, { status: 'PAID' })
    }
    return o
  })
  writeMockOrders(updated)
  return null
}

/**
 * 模拟生成入园二维码凭证。
 */
function getMockQrCode(passEntitlementId) {
  var now = new Date()
  var expires = new Date(now.getTime() + 60000)
  function pad(n) { return String(n).padStart(2, '0') }
  var expiresStr = expires.getFullYear() + '-' + pad(expires.getMonth() + 1) + '-' + pad(expires.getDate()) +
    'T' + pad(expires.getHours()) + ':' + pad(expires.getMinutes()) + ':' + pad(expires.getSeconds())

  return {
    qrContent: 'eyJwIjoxLCJ0IjoibW9ja3Rva2VuMTIzNDU2Nzg5MCIsInRzIjoxNzAwMDAwMDAwfQ.mock_signature_for_dev',
    expiresAt: expiresStr,
    passEntitlementId: Number(passEntitlementId),
    passName: '单次门票入园资格',
    storeName: '上海萌宠乐园旗舰店',
  }
}

/**
 * 模拟举报非本人入园操作。
 */
function reportMockUnauthorized(passEntitlementId, reason) {
  return {
    riskEventId: Date.now(),
    message: '已收到举报，工作人员将尽快核实处理。',
  }
}

module.exports = {
  confirmMockPayment: confirmMockPayment,
  createMockReservation: createMockReservation,
  getMockCards: getMockCards,
  getMockContext: getMockContext,
  getMockHome: getMockHome,
  getMockOrders: getMockOrders,
  getMockPets: getMockPets,
  getMockPrepay: getMockPrepay,
  getMockProfile: getMockProfile,
  getMockQrCode: getMockQrCode,
  getMockTickets: getMockTickets,
  reportMockUnauthorized: reportMockUnauthorized,
}
