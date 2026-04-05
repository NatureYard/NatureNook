const BASE_URL = 'http://localhost:8080'

function request(path) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${path}`,
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200 && res.data && res.data.data !== undefined) {
          resolve(res.data.data)
        } else {
          reject(new Error('Request failed'))
        }
      },
      fail: reject,
    })
  })
}

Page({
  data: {
    pets: [
      { name: '奶球', meta: '柯基 | 8.5kg' },
      { name: '布丁', meta: '英短 | 4.2kg' },
    ],
  },

  onLoad() {
    request('/api/c-app/pets')
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          this.setData({
            pets: data.map((item) => ({
              name: item.name,
              meta: `${item.species}${item.breed ? ' ' + item.breed : ''} | 主人: ${item.ownerName}`,
            })),
          })
        }
      })
      .catch(() => {
        // keep static fallback
      })
  },
})

