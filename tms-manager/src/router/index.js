import Vue from 'vue'
import Router from 'vue-router'

// 引入组件
import Login from '@/frame/Login'
import Main from '@/frame/Main'

import User from '@/components/auth/User'
import Role from '@/components/auth/Role'
import Func from '@/components/auth/Func'

import List from '@/components/run/List'
import ValueList from '@/components/run/ValueList'
import Txn from '@/components/run/Txn'
import Rule from '@/components/run/Rule'
import Stat from '@/components/run/Stat'
import Trandef from '@/components/run/Trandef'
import Tranmdl from '@/components/run/Tranmdl'


import AuthCenter from '@/components/system/AuthCenter'
import AuthDataList from '@/components/system/AuthDataList'
import AuthLogList from '@/components/system/AuthLogList'
import AuthSubDataList from '@/components/system/AuthSubDataList'
import AuthDataCompare from '@/components/system/AuthDataCompare'
import Codedict from '@/components/system/Codedict'
import CodedictInfo from '@/components/system/CodedictInfo'
import Ipaddr from '@/components/system/Ipaddr'
import Log from '@/components/system/Log'

import TxnEventQuery from '@/components/synquery/TxnEventQuery'
import AlarmEventQuery from '@/components/synquery/AlarmEventQuery'

import NotFoundComponent from '@/components/NotFoundComponent'
import util from '@/common/util'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: util.getWebRootPath(),
  linkActiveClass: 'active',
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/',
      name: 'main',
      component: Main,
      meta: {
        requireAuth: true  // 添加该字段，表示进入这个路由是需要登录的
      },
      children: [
        {
          path: 'user',
          name: 'user',
          component: User
        },
        {
          path: 'role',
          name: 'role',
          component: Role
        },
        {
          path: 'func',
          name: 'func',
          component: Func
        },
        {
          path: 'valuelist/:rosterid/:datatype',
          name: 'valuelist',
          component: ValueList
        },
        {
          path: 'list',
          name: 'list',
          component: List
        },
        {
          path: 'txn',
          name: 'txn',
          component: Txn
        },
        {
          path: 'authCenter.vue',
          name: 'authCenter',
          component: AuthCenter
        },
        {
          path: 'authDataCompare.vue',
          name: 'authDataCompare',
          component: AuthDataCompare
        },
        {
          path: 'authDataList.vue',
          name: 'authDataList',
          component: AuthDataList
        },
        {
          path: 'authLogList.vue',
          name: 'authLogList',
          component: AuthLogList
        },
        {
          path: 'authSubDataList.vue',
          name: 'authSubDataList',
          component: AuthSubDataList
        },
        {
          path: 'codedict.vue',
          name: 'codedict',
          component: Codedict
        },
        {
          path: 'codeDictInfo.vue',
          name: 'codeDictInfo',
          component: CodedictInfo
        },
        {
          path: 'ipaddr.vue',
          name: 'ipaddr',
          component: Ipaddr
        },
        {
          path: 'log',
          name: 'log',
          component: Log
        },
        {
          path: 'txnEventQuery',
          name: 'txnEventQuery',
          component: TxnEventQuery
        },
        {
          path: 'alarmEventQuery',
          name: 'alarmEventQuery',
          component: AlarmEventQuery
        }
      ]
    },
    {
      path: '*',
      name: 'notFound',
      component: NotFoundComponent
    }
  ]
})
