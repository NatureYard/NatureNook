var config = require('./utils/config')
var auth = require('./utils/auth')

App({
  globalData: {
    appName: '萌宠乐园消费者端',
    envVersion: 'develop',
    apiBase: '',
    loginReady: false,
  },

  onLaunch: function () {
    this.globalData.envVersion = config.getEnvVersion()
    this.globalData.apiBase = config.resolveApiBase()

    // 静默登录：启动时自动执行微信登录
    var self = this
    auth.ensureLogin()
      .then(function (member) {
        self.globalData.loginReady = true
        console.log('[app] 登录成功:', member ? member.memberName : 'unknown')
        // 通知等待登录的页面
        if (self._loginReadyCallback) {
          self._loginReadyCallback(member)
        }
      })
      .catch(function (error) {
        self.globalData.loginReady = true
        console.error('[app] 登录失败:', error.message)
        if (self._loginReadyCallback) {
          self._loginReadyCallback(null)
        }
      })
  },

  /**
   * 等待登录完成。页面可在 onLoad 中调用此方法确保登录态就绪后再加载数据。
   * 如果登录已完成则立即回调，否则等待 onLaunch 中的登录流程结束。
   */
  waitForLogin: function (callback) {
    if (this.globalData.loginReady) {
      callback(auth.getMember())
    } else {
      this._loginReadyCallback = callback
    }
  },
})
