const { request } = require('../../utils/request')

function formatStatus(status) {
  if (status === 'PAID') return '已支付'
  if (status === 'BOOKED') return '已预约'
  if (status === 'CANCELLED') return '已取消'
  return status || '处理中'
}

function formatPassWindow(validFrom, validTo) {
  if (!validFrom && !validTo) return '有效期待确认'
  const fromText = validFrom ? validFrom.replace('T', ' ') : '即刻'
  const toText = validTo ? validTo.replace('T', ' ') : '长期有效'
  return `${fromText} 至 ${toText}`
}

Page({
  data: {
    orders: [],
    passes: [],
    loading: true,
  },

  onShow() {
    this.loadPage()
  },

  loadPage() {
    this.setData({ loading: true })
    Promise.all([request('/api/c-app/orders'), request('/api/c-app/passes')])
      .then(([orders, passes]) => {
        this.setData({
          orders: orders.map((item) => ({
            no: item.orderNo,
            type: item.type,
            status: formatStatus(item.status),
            amount: item.amount,
            statusClass: item.status === 'PAID' ? 'paid' : 'pending',
            storeName: item.storeName || '未分配门店',
            reservationInfo: [item.reservationDate, item.timeSlot].filter(Boolean).join(' | ') || '待安排服务时间',
          })),
          passes: passes.map((item) => ({
            id: item.id,
            name: item.name,
            storeName: item.storeName || '未分配门店',
            status: item.status === 'ACTIVE' ? '可用' : item.status,
            reentryPolicy: item.reentryPolicy === 'SAME_DAY_UNLIMITED' ? '当日可多次出入' : item.reentryPolicy,
            validWindow: formatPassWindow(item.validFrom, item.validTo),
          })),
          loading: false,
        })
      })
      .catch(() => {
        this.setData({
          passes: [
            {
              id: 1,
              name: '单次门票入园资格',
              storeName: '上海萌宠乐园旗舰店',
              status: '可用',
              reentryPolicy: '当日可多次出入',
              validWindow: '2026-04-05 00:00:00 至 2026-04-05 23:59:59',
            },
          ],
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
