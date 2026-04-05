const BASE_URL = 'http://localhost:8080'

function request(path) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${path}`,
      method: 'GET',
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data.data)
          return
        }
        reject(new Error(`HTTP ${res.statusCode}`))
      },
      fail: reject,
    })
  })
}

module.exports = {
  request,
}

