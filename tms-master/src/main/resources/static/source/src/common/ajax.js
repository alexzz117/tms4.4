import axios from 'axios'
import util from '@/common/util'

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
  error: function (data) { alert(data.error) },
  fail: function (error) { alert(error) }
}

function post (opt) {
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
    })
    .catch(function (error) {
      option.fail(error)
    })
}

function get (opt) {
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
    })
    .catch(function (error) {
      option.fail(error)
    })
}

export default {
  post: post,
  get: get,
  model: modelDef
}
