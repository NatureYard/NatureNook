const { resolveApiBase } = require('./config')

function getApiBase() {
  try {
    const app = getApp()
    if (app && app.globalData && app.globalData.apiBase) {
      return app.globalData.apiBase
    }
  } catch (error) {
    // Ignore and fall back to config resolution.
  }

  return resolveApiBase()
}

function normalizeError(error, fallbackMessage) {
  if (!error) {
    return new Error(fallbackMessage)
  }

  if (error instanceof Error) {
    return error
  }

  if (typeof error === 'string') {
    return new Error(error)
  }

  return new Error(error.errMsg || fallbackMessage)
}

function request(path, options = {}) {
  const {
    method = 'GET',
    data,
    headers = {},
    timeout = 10000,
  } = options

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${getApiBase()}${path}`,
      method,
      data,
      timeout,
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
      fail: (error) => {
        reject(normalizeError(error, '网络异常，请稍后重试'))
      },
    })
  })
}

function showRequestError(error, fallbackMessage = '操作失败，请稍后重试') {
  const message = normalizeError(error, fallbackMessage).message || fallbackMessage
  wx.showToast({
    title: message.length > 20 ? fallbackMessage : message,
    icon: 'none',
  })
}

module.exports = {
  request,
  showRequestError,
}
