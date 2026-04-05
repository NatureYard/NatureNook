const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

async function request(path) {
  const response = await fetch(`${API_BASE}${path}`)
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }
  const payload = await response.json()
  return payload.data
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

export async function createMember(payload) {
  const response = await fetch(`${API_BASE}/api/admin/members`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  const result = await response.json()
  if (!response.ok || !result.success) {
    throw new Error(result.message || `HTTP ${response.status}`)
  }
  return result.data
}
