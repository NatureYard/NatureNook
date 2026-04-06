var MOCK_ORDER_STORAGE_KEY = 'mcly_mock_orders'

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
  return [
    {
      id: 1,
      orderNo: 'ORD202604050001',
      type: '单次门票',
      status: 'PAID',
      amount: '68.00',
      storeName: '上海萌宠乐园旗舰店',
      reservationDate: '2026-04-06',
      timeSlot: '09:00-12:00',
      createdAt: '2026-04-05 14:30',
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
      createdAt: '2026-04-05 10:15',
    },
    {
      id: 3,
      orderNo: 'ORD202604030001',
      type: '洗护套餐',
      status: 'USED',
      amount: '128.00',
      storeName: '上海萌宠乐园旗舰店',
      reservationDate: '2026-04-03',
      timeSlot: '13:00-15:00',
      createdAt: '2026-04-02 09:00',
    },
    {
      id: 4,
      orderNo: 'ORD202603280001',
      type: '寄养预约',
      status: 'CANCELLED',
      amount: '188.00',
      storeName: '上海萌宠乐园旗舰店',
      reservationDate: '2026-03-30',
      timeSlot: '09:00-12:00',
      createdAt: '2026-03-28 16:45',
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

  return buildDefaultOrders()
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
    status: 'PAID',
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
    status: 'PAID',
  }
}

module.exports = {
  createMockReservation,
  getMockCards,
  getMockContext,
  getMockHome,
  getMockOrders,
  getMockPets,
  getMockProfile,
  getMockTickets,
}
