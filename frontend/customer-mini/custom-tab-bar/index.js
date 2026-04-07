Component({
  data: {
    selected: 0,
    color: '#8b7d6b',
    selectedColor: '#ff6b4a',
    list: [
      {
        pagePath: '/pages/index/index',
        text: '首页',
        caption: '乐园概览',
        icon: 'H',
      },
      {
        pagePath: '/pages/tickets/index',
        text: '预约',
        caption: '购票服务',
        icon: 'R',
      },
      {
        pagePath: '/pages/orders/index',
        text: '订单',
        caption: '凭证进度',
        icon: 'O',
      },
      {
        pagePath: '/pages/profile/index',
        text: '我的',
        caption: '会员档案',
        icon: 'M',
      },
    ],
  },

  methods: {
    switchTab(event) {
      const { path, index } = event.currentTarget.dataset
      if (!path) return

      this.setData({ selected: Number(index) || 0 })
      wx.switchTab({ url: path })
    },

    syncSelected() {
      const pages = getCurrentPages()
      const current = pages[pages.length - 1]
      const route = current ? `/${current.route}` : ''
      const selected = this.data.list.findIndex((item) => item.pagePath === route)
      this.setData({ selected: selected >= 0 ? selected : 0 })
    },
  },

  lifetimes: {
    attached() {
      this.syncSelected()
    },
  },

  pageLifetimes: {
    show() {
      this.syncSelected()
    },
  },
})