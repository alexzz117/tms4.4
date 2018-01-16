// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

import Element from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
Vue.use(Element, { size: 'small' })
// Vue.use(Element, { size: 'medium' })

import axios from 'axios';
import echarts from 'echarts'
import 'babel-polyfill'

Vue.prototype.$axios=axios;
Vue.prototype.$echarts = echarts

Vue.config.productionTip = false
// import AlarmEventQuery from '@/components/synquery/AlarmEventQuery'
//
// console.log('111111')
// console.log(AlarmEventQuery)
// Vue.component('alarmEventQuery', AlarmEventQuery)
import dictCode from '@/common/dictCode'

let vue = null

vue = /* eslint-disable no-new */
  new Vue({
    el: '#app',
    router,
    beforeCreate: function () {
      dictCode.getCodeData()
    },
    template: '<App/>',
    components: { App }
  })




