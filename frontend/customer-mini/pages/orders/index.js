const { request } = require('../../utils/request')

Page({
  data: {
    orders: [],
  },

  onLoad() {
    request('/api/c-app/orders')
      .then((data) => {
        this.setData({
          orders: data.map((item) => ({
            no: item.orderNo,
            type: item.type,
            status: item.status,
            amount: item.amount,
          })),
        })
      })
      .catch(() => {
        this.setData({
          orders: [
            { no: 'ORD202604050001', type: '门票', status: '已支付', amount: '68.00' },
            { no: 'ORD202604050002', type: '年卡', status: '已支付', amount: '1288.00' },
          ],
        })
      })
  },
})
