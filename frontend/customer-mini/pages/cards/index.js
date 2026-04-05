const { request } = require('../../utils/request')

Page({
  data: {
    cards: [],
    loading: true,
  },

  onLoad() {
    request('/api/c-app/cards')
      .then((data) => {
        this.setData({
          cards: data.map((item) => ({
            id: item.id,
            name: item.name,
            desc: item.desc,
            meta: `${item.status} | 到期 ${item.validTo}`,
            price: item.price,
          })),
          loading: false,
        })
      })
      .catch(() => {
        this.setData({
          cards: [
            { id: 1, name: '月卡', desc: '30 天内多次入园', meta: '可购买', price: '399' },
            { id: 2, name: '季卡', desc: '90 天内多次入园', meta: '可购买', price: '999' },
            { id: 3, name: '年卡', desc: '365 天内多次入园', meta: '可购买', price: '1288' },
          ],
          loading: false,
        })
      })
  },
})
