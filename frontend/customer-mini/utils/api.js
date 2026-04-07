var req = require('./request')
var config = require('./config')
var mock = require('./mock')

function withFallback(loader, fallbackFactory) {
  return loader().catch(function (error) {
    if (config.shouldUseMockFallback()) {
      return fallbackFactory()
    }
    throw error
  })
}

function fetchHome() {
  return withFallback(function () {
    return req.request('/api/c-app/home')
  }, mock.getMockHome)
}

function fetchOrders() {
  return withFallback(function () {
    return req.request('/api/c-app/orders')
  }, mock.getMockOrders)
}

function fetchContext() {
  return withFallback(function () {
    return req.request('/api/c-app/context')
  }, mock.getMockContext)
}

function fetchPets() {
  return withFallback(function () {
    return req.request('/api/c-app/pets')
  }, mock.getMockPets)
}

function fetchCards() {
  return withFallback(function () {
    return req.request('/api/c-app/cards')
  }, mock.getMockCards)
}

function fetchProfile() {
  return withFallback(function () {
    return req.request('/api/c-app/profile')
  }, mock.getMockProfile)
}

function fetchTickets() {
  return withFallback(function () {
    return req.request('/api/c-app/tickets')
  }, mock.getMockTickets)
}

/**
 * 创建预约。返回 {reservationId, orderId, orderNo, status: "PENDING_PAY"}
 */
function createReservation(payload) {
  return withFallback(function () {
    return req.request('/api/c-app/reservations', {
      method: 'POST',
      data: payload,
    })
  }, function () {
    return mock.createMockReservation(payload)
  })
}

/**
 * 获取预支付参数。返回 wx.requestPayment 所需的全部参数。
 */
function prepay(orderNo) {
  return withFallback(function () {
    return req.request('/api/c-app/prepay', {
      method: 'POST',
      data: { orderNo: orderNo },
    })
  }, function () {
    return mock.getMockPrepay(orderNo)
  })
}

/**
 * 确认支付完成（开发模式用，正式环境由微信回调触发）。
 */
function confirmPayment(orderNo) {
  return withFallback(function () {
    return req.request('/api/c-app/payment/confirm', {
      method: 'POST',
      data: { orderNo: orderNo },
    })
  }, function () {
    return mock.confirmMockPayment(orderNo)
  })
}

module.exports = {
  confirmPayment: confirmPayment,
  createReservation: createReservation,
  fetchCards: fetchCards,
  fetchContext: fetchContext,
  fetchHome: fetchHome,
  fetchOrders: fetchOrders,
  fetchPets: fetchPets,
  fetchProfile: fetchProfile,
  fetchTickets: fetchTickets,
  prepay: prepay,
}
