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
    cards: [
      { name: '月卡', desc: '30 天内多次入园', price: '399' },
      { name: '季卡', desc: '90 天内多次入园', price: '999' },
      { name: '年卡', desc: '365 天内多次入园', price: '1288' },
    ],
  },

  onLoad() {
    request('/api/c-app/cards')
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          this.setData({
            cards: data.map((item) => ({
              name: `${item.memberName} ${item.cardType}`,
              desc: `${item.status} | 有效至 ${item.validTo}`,
              price: '',
            })),
          })
        }
      })
      .catch(() => {
        // keep static fallback
      })
  },
})

