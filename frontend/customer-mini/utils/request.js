const BASE_URL = 'http://localhost:8080'
const { isE2EMockEnabled, resolveE2EMockRequest } = require('./e2e-mock')

function request(path, options = {}) {
  if (isE2EMockEnabled()) {
    return Promise.resolve(resolveE2EMockRequest(path, options))
  }

  const { method = 'GET', data, headers = {} } = options
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${path}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...headers,
      },
      success: (res) => {
        const payload = res.data || {}
        if (res.statusCode >= 200 && res.statusCode < 300) {
          if (payload.success === false) {
            reject(new Error(payload.message || '请求失败'))
            return
          }
          resolve(payload.data)
          return
        }
        reject(new Error(payload.message || `HTTP ${res.statusCode}`))
      },
      fail: reject,
    })
  })
}

module.exports = {
  request,
}
