// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

import Element from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
Vue.use(Element, { size: 'small' })

import axios from 'axios';
Vue.prototype.$axios=axios;

Vue.config.productionTip = false
// import AlarmEventQuery from '@/components/synquery/AlarmEventQuery'
//
// console.log('111111')
// console.log(AlarmEventQuery)
// Vue.component('alarmEventQuery', AlarmEventQuery)
import dictCode from '@/common/dictCode'

let vue = null

dictCode.getCodeData(function () {
  vue = /* eslint-disable no-new */
    new Vue({
      el: '#app',
      router,
      template: '<App/>',
      components: { App }
    })

})


