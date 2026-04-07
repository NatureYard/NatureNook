var api = require('../../utils/api')
var fmt = require('../../utils/formatters')

Page({
  data: {
    pets: [],
    loading: true,
    error: '',
  },

  onLoad: function () {
    this.loadPets()
  },

  onPullDownRefresh: function () {
    this.loadPets()
  },

  loadPets: function () {
    this.setData({ loading: true, error: '' })
    var self = this

    api.fetchPets()
      .then(function (data) {
        self.setData({
          loading: false,
          pets: (data || []).map(function (item) {
            return {
              id: item.id,
              name: item.name,
              avatar: item.name ? item.name.substring(0, 1) : '?',
              species: fmt.formatPetSpecies(item.species),
              breed: item.breed || '未知品种',
              gender: fmt.formatPetGender(item.gender),
              genderClass: item.gender === 'MALE' ? 'male' : 'female',
              weight: item.weight ? item.weight + 'kg' : '未记录',
              vaccinated: item.vaccinated,
              meta: fmt.formatPetMeta(item),
            }
          }),
        })
      })
      .catch(function (error) {
        self.setData({
          loading: false,
          pets: [],
          error: error.message || '宠物档案加载失败，请稍后重试',
        })
      })
      .then(function () {
        wx.stopPullDownRefresh()
      })
  },

  retry: function () {
    this.loadPets()
  },
})
