const { getEnvVersion, resolveApiBase } = require('./utils/config')

App({
  globalData: {
    appName: '萌宠乐园消费者端',
    envVersion: 'develop',
    apiBase: '',
  },

  onLaunch() {
    this.globalData.envVersion = getEnvVersion()
    this.globalData.apiBase = resolveApiBase()
  },
})
