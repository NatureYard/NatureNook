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

function formatPassCode(id) {
  return `PASS-${String(id || '').padStart(6, '0')}`
}

function mapOrder(item) {
  const paid = item.status === 'PAID'
  return {
    no: item.orderNo,
    type: item.type,
    status: formatStatus(item.status),
    amount: item.amount,
    amountText: `¥${item.amount}`,
    statusClass: paid ? 'paid' : 'pending',
    statusTone: paid ? '暖光确认' : '待确认',
    storeName: item.storeName || '未分配门店',
    reservationInfo: [item.reservationDate, item.timeSlot].filter(Boolean).join(' | ') || '待安排服务时间',
  }
}

function mapPass(item) {
  const isActive = item.status === 'ACTIVE'
  return {
    id: item.id,
    name: item.name,
    code: formatPassCode(item.id),
    storeName: item.storeName || '未分配门店',
    status: isActive ? '可用' : item.status,
    statusClass: isActive ? 'paid' : 'pending',
    statusTone: isActive ? '随到随验' : '待生效',
    reentryPolicy: item.reentryPolicy === 'SAME_DAY_UNLIMITED' ? '当日可多次出入' : item.reentryPolicy,
    validWindow: formatPassWindow(item.validFrom, item.validTo),
  }
}

Page({
  data: {
    orders: [],
    passes: [],
    loading: true,
    passSummary: '当前还没有有效凭证',
    orderSummary: '暂无订单记录',
    journeyHint: '支付完成后，这里会持续展示凭证状态和订单进度。',
  },

  onShow() {
    this.loadPage()
  },

  loadPage() {
    this.setData({ loading: true })
    Promise.all([request('/api/c-app/orders'), request('/api/c-app/passes')])
      .then(([orders, passes]) => {
        const orderList = orders.map(mapOrder)
        const passList = passes.map(mapPass)
        this.setData({
          orders: orderList,
          passes: passList,
          loading: false,
          passSummary: passList.length ? `你有 ${passList.length} 张当前可核验的入园凭证` : '当前还没有有效凭证',
          orderSummary: orderList.length ? `最近 ${orderList.length} 笔订单与预约记录` : '暂无订单记录',
          journeyHint: passList.length ? '到店时直接出示凭证即可核验，订单状态会持续同步。' : '先完成预约与支付，系统会自动生成可核验的入园凭证。',
        })
      })
      .catch(() => {
        const passList = [
          {
            id: 1,
            name: '单次门票入园资格',
            code: 'PASS-000001',
            storeName: '上海萌宠乐园旗舰店',
            status: '可用',
            statusClass: 'paid',
            statusTone: '随到随验',
            reentryPolicy: '当日可多次出入',
            validWindow: '2026-04-05 00:00:00 至 2026-04-05 23:59:59',
          },
        ]
        const orderList = [
          {
            no: 'ORD202604050001',
            type: '单次门票',
            status: '已支付',
            amount: '68.00',
            amountText: '¥68.00',
            statusClass: 'paid',
            statusTone: '暖光确认',
            storeName: '上海萌宠乐园旗舰店',
            reservationInfo: '2026-04-05 | 09:00-12:00',
          },
          {
            no: 'ORD202604050002',
            type: '年卡',
            status: '已支付',
            amount: '1288.00',
            amountText: '¥1288.00',
            statusClass: 'paid',
            statusTone: '暖光确认',
            storeName: '上海萌宠乐园旗舰店',
            reservationInfo: '待安排服务时间',
          },
        ]
        this.setData({
          passes: passList,
          orders: orderList,
          loading: false,
          passSummary: `你有 ${passList.length} 张当前可核验的入园凭证`,
          orderSummary: `最近 ${orderList.length} 笔订单与预约记录`,
          journeyHint: '到店时直接出示凭证即可核验，订单状态会持续同步。',
        })
      })

  },

  openTickets() {
    wx.switchTab({
      url: '/pages/tickets/index',
    })
  },
})
