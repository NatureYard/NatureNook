const { request } = require('../../utils/request')

Page({
  data: {
    pets: [],
    loading: true,
  },

  onLoad() {
    request('/api/c-app/pets')
      .then((data) => {
        this.setData({
          pets: data.map((item) => ({
            id: item.id,
            name: item.name,
            avatar: item.name ? item.name.substring(0, 1) : '?',
            meta: [item.species, item.breed, item.weight ? `${item.weight}kg` : ''].filter(Boolean).join(' | '),
          })),
          loading: false,
        })
      })
      .catch(() => {
        this.setData({
          pets: [
            { id: 1, name: '奶球', avatar: '奶', meta: 'DOG | 柯基 | 8.50kg' },
            { id: 2, name: '布丁', avatar: '布', meta: 'CAT | 英短 | 4.20kg' },
          ],
          loading: false,
        })
      })
  },
})
