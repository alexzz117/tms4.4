import axios from 'axios'
import util from '@/common/util'

import Vue from 'vue'
import { Loading,Message } from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.prototype.$ELEMENT = { size: 'small' }
Vue.use(Message)
Vue.use(Loading)
let vue = new Vue({})

var config = {
  // prefix: util.getWebRootPath()  // 正式测试环境
  prefix: '/context' // 分离测试环境
}

let modelDef = {
  manager: '/manager',
  dualaudit: '/dualaudit/manager',
  timer: '',
  plus: '/plus'
}

let defaultOption = {
  url: '',
  param: {},
  model: modelDef.manager,
  toLowerCase: true,
  success: function (data) {},
  error: function (data) {
    vue.$message({
      showClose: true,
      message: data.error,
      type: 'error'
    });
  },
  fail: function (error) {
    let response = error.response
    let message
    if (response && response.data && response.data.message) {
      message = response.data.message
    } else {
      message = error.message
    }
    vue.$message({
      showClose: true,
      message: message,
      type: 'error'
    });
  }
}

function post (opt) {
  let loading = vue.$loading({
    lock: true,
    text: 'Loading',
    spinner: 'el-icon-loading',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  let option = {}
  Object.assign(option, defaultOption, opt)
  var sendUrl = config.prefix + option.model + option.url
  axios.post(sendUrl, option.param)
    .then(function (response) {
      if (response.status === 200) {
        if (response.data && response.data.success === true) {
          var data
          if (option.toLowerCase) {
            data = util.toggleObjKey(response.data)
          } else {
            data = response.data
          }
          option.success(data)
        } else {
          option.error(response.data)
        }
      }
      loading.close()
    })
    .catch(function (error) {
      option.fail(error)
      loading.close()
    })
}

function get (opt) {
  let loading = vue.$loading({
    lock: true,
    text: 'Loading',
    spinner: 'el-icon-loading',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  let option = {}
  Object.assign(option, defaultOption, opt)
  var sendUrl = config.prefix + option.model + option.url

  axios.get(sendUrl)
    .then(function (response) {
      if (response.status === 200) {
        if (response.data && response.data.success === true) {
          var data
          if (option.toLowerCase) {
            data = util.toggleObjKey(response.data)
          } else {
            data = response.data
          }
          option.success(data)
        } else {
          option.error(response.data)
        }
      }
      loading.close()
    })
    .catch(function (error) {
      option.fail(error)
      loading.close()
    })
}

export default {
  post: post,
  get: get,
  model: modelDef
}
