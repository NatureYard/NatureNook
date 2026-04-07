/**
 * 微信小程序认证模块。
 *
 * 登录流程：
 * 1. wx.login() 获取微信临时 code
 * 2. POST /api/c-app/login 发送 code 到后端
 * 3. 后端用 code 换取 openid，签发 token
 * 4. 前端将 token 存储到本地，后续请求自动附加
 *
 * 开发模式（develop 环境）下如果后端不可用，使用 mock 登录。
 */

var TOKEN_KEY = 'mcly_auth_token'
var MEMBER_KEY = 'mcly_auth_member'

/**
 * 获取已存储的 token。
 */
function getToken() {
  try {
    return wx.getStorageSync(TOKEN_KEY) || ''
  } catch (e) {
    return ''
  }
}

/**
 * 存储 token 和会员信息。
 */
function saveAuth(token, member) {
  try {
    wx.setStorageSync(TOKEN_KEY, token)
    if (member) {
      wx.setStorageSync(MEMBER_KEY, member)
    }
  } catch (e) {
    // ignore
  }
}

/**
 * 获取已存储的会员信息。
 */
function getMember() {
  try {
    return wx.getStorageSync(MEMBER_KEY) || null
  } catch (e) {
    return null
  }
}

/**
 * 清除登录态。
 */
function clearAuth() {
  try {
    wx.removeStorageSync(TOKEN_KEY)
    wx.removeStorageSync(MEMBER_KEY)
  } catch (e) {
    // ignore
  }
}

/**
 * 是否已登录（有 token）。
 */
function isLoggedIn() {
  return !!getToken()
}

/**
 * 执行微信登录流程。
 * 返回 Promise<{token, memberId, memberName, isNewUser}>
 */
function login() {
  return new Promise(function (resolve, reject) {
    wx.login({
      success: function (res) {
        if (!res.code) {
          reject(new Error('wx.login 未返回 code'))
          return
        }
        resolve(res.code)
      },
      fail: function (err) {
        reject(new Error(err.errMsg || 'wx.login 调用失败'))
      },
    })
  }).then(function (code) {
    // 将 code 发送到后端换取 token
    var config = require('./config')
    var apiBase = config.resolveApiBase()

    return new Promise(function (resolve, reject) {
      wx.request({
        url: apiBase + '/api/c-app/login',
        method: 'POST',
        data: { code: code },
        header: { 'Content-Type': 'application/json' },
        timeout: 10000,
        success: function (res) {
          var payload = res.data || {}
          if (res.statusCode >= 200 && res.statusCode < 300 && payload.success !== false) {
            var data = payload.data || {}
            saveAuth(data.token, {
              memberId: data.memberId,
              memberName: data.memberName,
              isNewUser: data.isNewUser,
            })
            resolve(data)
          } else {
            reject(new Error(payload.message || '登录请求失败'))
          }
        },
        fail: function (err) {
          reject(new Error(err.errMsg || '网络异常，登录失败'))
        },
      })
    })
  })
}

/**
 * 开发模式 mock 登录。
 */
function mockLogin() {
  var mockToken = 'mock_token_dev_' + Date.now()
  var mockMember = {
    memberId: 1,
    memberName: '张三',
    isNewUser: false,
  }
  saveAuth(mockToken, mockMember)
  return mockMember
}

/**
 * 确保已登录，未登录则自动执行登录。
 * 开发模式下如果真实登录失败，会降级为 mock 登录。
 */
function ensureLogin() {
  if (isLoggedIn()) {
    return Promise.resolve(getMember())
  }

  var config = require('./config')

  return login().catch(function (error) {
    if (config.shouldUseMockFallback()) {
      console.warn('[auth] 真实登录失败，降级为 mock 登录:', error.message)
      return mockLogin()
    }
    throw error
  })
}

module.exports = {
  clearAuth,
  ensureLogin,
  getMember,
  getToken,
  isLoggedIn,
  login,
  mockLogin,
  saveAuth,
}
