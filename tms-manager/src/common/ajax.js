import axios from 'axios'
import util from '@/common/util'

var config = {
  // prefix: 'tms-web',  // 正式测试环境
  suffix: '?format=json',
  prefix: 'api'  // node.js 测试环境
}

function post(url, param, cb) {
  var sendUrl = config.prefix + url + config.suffix
  axios.post(sendUrl, param)
    .then(function (response) {
      // console.log(response)
      if (response.status === 200) {
        if (response.data && response.data.success === true) {
          var data = util.toggleObjKey(response.data)
          cb(data)
        } else {
          alert(response.data.error)
          // this.$message(response.data.error)
        }
      }
    })
    .catch(function (error) {
      cb(error)
      alert(error)
    })
}

export default {
  post:post
}
