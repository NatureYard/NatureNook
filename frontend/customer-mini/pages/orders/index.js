const { request } = require('../../utils/request')

function formatStatus(status) {
  if (status === 'PAID') return '已支付'
  if (status === 'BOOKED') return '已预约'
  if (status === 'CANCELLED') return '已取消'
  return status || '处理中'
}

Page({
  data: {
    orders: [],
    loading: true,
  },

  onShow() {
    this.loadOrders()
  },

  loadOrders() {
    this.setData({ loading: true })
    request('/api/c-app/orders')
      .then((data) => {
        this.setData({
          orders: data.map((item) => ({
            no: item.orderNo,
            type: item.type,
            status: formatStatus(item.status),
            amount: item.amount,
            statusClass: item.status === 'PAID' ? 'paid' : 'pending',
            storeName: item.storeName || '未分配门店',
            reservationInfo: [item.reservationDate, item.timeSlot].filter(Boolean).join(' | ') || '待安排服务时间',
          })),
          loading: false,
        })
      })
      .catch(() => {
        this.setData({
          orders: [
            {
              no: 'ORD202604050001',
              type: '单次门票',
              status: '已支付',
              amount: '68.00',
              statusClass: 'paid',
              storeName: '上海萌宠乐园旗舰店',
              reservationInfo: '2026-04-05 | 09:00-12:00',
            },
            {
              no: 'ORD202604050002',
              type: '年卡',
              status: '已支付',
              amount: '1288.00',
              statusClass: 'paid',
              storeName: '上海萌宠乐园旗舰店',
              reservationInfo: '待安排服务时间',
            },
          ],
          loading: false,
        })
      })
  },
})
