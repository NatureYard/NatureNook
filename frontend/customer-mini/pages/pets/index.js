const { request } = require('../../utils/request')

function speciesLabel(species) {
  if (species === 'DOG') return '狗狗档案'
  if (species === 'CAT') return '猫咪档案'
  return '宠物档案'
}

function speciesTone(species) {
  if (species === 'DOG') return 'peach'
  if (species === 'CAT') return 'mint'
  return 'cream'
}

function mapPet(item) {
  return {
    id: item.id,
    name: item.name,
    avatar: item.name ? item.name.substring(0, 1) : '?',
    meta: [item.species, item.breed, item.weight ? `${item.weight}kg` : ''].filter(Boolean).join(' | '),
    speciesLabel: speciesLabel(item.species),
    tone: speciesTone(item.species),
  }
}

Page({
  data: {
    pets: [],
    loading: true,
    summary: '把每一只毛孩子的基本信息放在同一面档案墙上。',
    archiveHint: '宠物档案会直接复用到预约和到店核验流程里。',
  },

  onLoad() {
    request('/api/c-app/pets')
      .then((data) => {
        const pets = data.map(mapPet)
        this.setData({
          pets,
          loading: false,
          summary: pets.length ? `你已经建立 ${pets.length} 份宠物档案` : '把每一只毛孩子的基本信息放在同一面档案墙上。',
          archiveHint: pets.length ? '已建档的宠物可直接在预约流程中选择，无需重复填写。' : '先补齐宠物档案，预约时就不需要再重复输入。',
        })
      })
      .catch(() => {
        const pets = [
          { id: 1, name: '奶球', species: 'DOG', breed: '柯基', weight: '8.50' },
          { id: 2, name: '布丁', species: 'CAT', breed: '英短', weight: '4.20' },
        ].map(mapPet)
        this.setData({
          pets,
          loading: false,
          summary: `你已经建立 ${pets.length} 份宠物档案`,
          archiveHint: '已建档的宠物可直接在预约流程中选择，无需重复填写。',
        })
      })
  },
})
