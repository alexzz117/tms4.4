import axios from 'axios'
import util from '@/common/util'

var config = {
  // prefix: util.getWebRootPath() + '/manager',  // 正式测试环境
  suffix: '?format=json',
  prefix: '/context' // 分离测试环境
  // prefix: '/api' // local mock
}

// function post (url, param, cb, errorCb) {
//   var sendUrl = config.prefix + url + config.suffix
//   axios.post(sendUrl, param)
//     .then(function (response) {
//       // console.log(response)
//       if (response.status === 200) {
//         if (response.data && response.data.success === true) {
//           var data = util.toggleObjKey(response.data)
//           cb(data)
//         } else {
//           if (typeof errorCb === 'function') {
//             errorCb(response.data)
//           } else {
//             alert(response.data.error)
//           }
//           // this.$message(response.data.error)
//         }
//       }
//     })
//     .catch(function (error) {
//       alert(error)
//     })
// }

let modelDef = {
  manager: '/manager',
  timer: '/timer',
  plus: '/plus'
}

let defaultOption = {
  url: '',
  param: {},
  model: modelDef.manager,
  success: function (data) {},
  error: function (data) { alert(data.error) },
  fail: function (error) { alert(error) }
}

function post (opt) {
  let option = {}
  Object.assign(option, defaultOption, opt)
  var sendUrl = config.prefix + option.model + option.url + config.suffix
  axios.post(sendUrl, option.param)
    .then(function (response) {
      if (response.status === 200) {
        if (response.data && response.data.success === true) {
          var data = util.toggleObjKey(response.data)
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
  var sendUrl = config.prefix + option.model + option.url + config.suffix

  axios.get(sendUrl)
    .then(function (response) {
      if (response.status === 200) {
        if (response.data && response.data.success === true) {
          var data = util.toggleObjKey(response.data)
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
