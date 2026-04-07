const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

async function request(path) {
  const response = await fetch(`${API_BASE}${path}`)
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }
  const payload = await response.json()
  return payload.data
}

async function post(path, body) {
  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const result = await response.json()
  if (!response.ok || !result.success) {
    throw new Error(result.message || `HTTP ${response.status}`)
  }
  return result.data
}

export function fetchDashboardSummary() {
  return request('/api/admin/dashboard/summary')
}

export function fetchGateRules() {
  return request('/api/admin/gates/rules')
}

export function fetchGateEvents() {
  return request('/api/admin/gates/events')
}

export function fetchMaterialStocks() {
  return request('/api/admin/materials/stocks')
}

export function fetchMembers() {
  return request('/api/admin/members')
}

export function fetchPets() {
  return request('/api/admin/pets')
}

export function fetchCards() {
  return request('/api/admin/cards')
}

export function fetchBoardingOrders() {
  return request('/api/admin/boarding/orders')
}

export function fetchGroomingOrders() {
  return request('/api/admin/grooming/orders')
}

export function fetchRiskEvents() {
  return request('/api/admin/risks/events')
}

export function fetchReservations() {
  return request('/api/admin/reservations')
}

export async function createMember(payload) {
  return post('/api/admin/members', payload)
}

export async function createBoardingOrder(payload) {
  return post('/api/admin/boarding/orders', payload)
}

export async function createGroomingOrder(payload) {
  return post('/api/admin/grooming/orders', payload)
}

export async function createReservation(payload) {
  return post('/api/admin/reservations', payload)
}

