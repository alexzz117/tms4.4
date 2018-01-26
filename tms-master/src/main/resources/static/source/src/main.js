// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'

import Element from 'element-ui'
// import 'element-ui/lib/theme-chalk/index.css'
import './assets/sass/element-variables.scss'

Vue.use(Element, { size: 'small' })
// Vue.use(Element, { size: 'medium' })

import router from './router'
import App from './App'

import axios from 'axios';
import echarts from 'echarts'
import 'babel-polyfill'

Vue.prototype.$axios = axios;
Vue.prototype.$echarts = echarts
Vue.config.productionTip = false

import dictCode from '@/common/dictCode'

let Hub = new Vue(); //创建事件中心
Vue.prototype.Hub = Hub;

new Vue({
  el: '#app',
  router,
  beforeCreate: function () {
    dictCode.getCodeData()
  },
  template: '<App/>',
  components: { App }
})




