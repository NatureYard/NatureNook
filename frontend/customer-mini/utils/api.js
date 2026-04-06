const { request } = require('./request')
const { shouldUseMockFallback } = require('./config')
const {
  createMockReservation,
  getMockCards,
  getMockContext,
  getMockHome,
  getMockOrders,
  getMockPets,
  getMockProfile,
  getMockTickets,
} = require('./mock')

function withFallback(loader, fallbackFactory) {
  return loader().catch(function(error) {
    if (shouldUseMockFallback()) {
      return fallbackFactory()
    }
    throw error
  })
}

function fetchHome() {
  return withFallback(function() {
    return request('/api/c-app/home')
  }, getMockHome)
}

function fetchOrders() {
  return withFallback(function() {
    return request('/api/c-app/orders')
  }, getMockOrders)
}

function fetchContext() {
  return withFallback(function() {
    return request('/api/c-app/context')
  }, getMockContext)
}

function fetchPets() {
  return withFallback(function() {
    return request('/api/c-app/pets')
  }, getMockPets)
}

function fetchCards() {
  return withFallback(function() {
    return request('/api/c-app/cards')
  }, getMockCards)
}

function fetchProfile() {
  return withFallback(function() {
    return request('/api/c-app/profile')
  }, getMockProfile)
}

function fetchTickets() {
  return withFallback(function() {
    return request('/api/c-app/tickets')
  }, getMockTickets)
}

function createReservation(payload) {
  return withFallback(function() {
    return request('/api/c-app/reservations', {
      method: 'POST',
      data: payload,
    })
  }, function() {
    return createMockReservation(payload)
  })
}

module.exports = {
  createReservation,
  fetchCards,
  fetchContext,
  fetchHome,
  fetchOrders,
  fetchPets,
  fetchProfile,
  fetchTickets,
}
