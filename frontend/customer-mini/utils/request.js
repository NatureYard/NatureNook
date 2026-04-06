var config = require('./config')
var auth = require('./auth')

function getApiBase() {
  try {
    var app = getApp()
    if (app && app.globalData && app.globalData.apiBase) {
      return app.globalData.apiBase
    }
  } catch (error) {
    // Ignore and fall back to config resolution.
  }

  return config.resolveApiBase()
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

function request(path, options) {
  options = options || {}
  var method = options.method || 'GET'
  var data = options.data
  var headers = options.headers || {}
  var timeout = options.timeout || 10000

  // 自动附加 token 到 Authorization header
  var token = auth.getToken()
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }

  return new Promise(function (resolve, reject) {
    wx.request({
      url: getApiBase() + path,
      method: method,
      data: data,
      timeout: timeout,
      header: Object.assign({ 'Content-Type': 'application/json' }, headers),
      success: function (res) {
        var payload = res.data || {}

        // 401 未认证 → 清除本地 token，触发重新登录
        if (res.statusCode === 401) {
          auth.clearAuth()
          reject(new Error('登录已过期，请重新打开小程序'))
          return
        }

        if (res.statusCode >= 200 && res.statusCode < 300) {
          if (payload.success === false) {
            reject(new Error(payload.message || '请求失败'))
            return
          }
          resolve(payload.data)
          return
        }
        reject(new Error(payload.message || 'HTTP ' + res.statusCode))
      },
      fail: function (error) {
        reject(normalizeError(error, '网络异常，请稍后重试'))
      },
    })
  })
}

function showRequestError(error, fallbackMessage) {
  fallbackMessage = fallbackMessage || '操作失败，请稍后重试'
  var message = normalizeError(error, fallbackMessage).message || fallbackMessage
  wx.showToast({
    title: message.length > 20 ? fallbackMessage : message,
    icon: 'none',
  })
}

module.exports = {
  request: request,
  showRequestError: showRequestError,
}
