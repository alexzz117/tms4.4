import Vue from 'vue'
import Router from 'vue-router'

// 引入组件
import Login from '@/frame/Login'
import Main from '@/frame/Main'

import User from '@/components/auth/User'
import Role from '@/components/auth/Role'
import Function from '@/components/auth/Function'

import List from '@/components/run/List'
import ValueList from '@/components/run/ValueList'
import Txn from '@/components/run/Txn'
import Rule from '@/components/run/Rule'
import Stat from '@/components/run/Stat'
import Trandef from '@/components/run/Trandef'
import Tranmdl from '@/components/run/Tranmdl'


import AuthCenter from '@/components/system/AuthCenter'
import Codedict from '@/components/system/Codedict'
import Ipaddr from '@/components/system/Ipaddr'
import Log from '@/components/system/Log'

import NotFoundComponent from '@/components/NotFoundComponent'

Vue.use(Router)

export default new Router({
  mode: 'history',
  linkActiveClass: 'active',
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      name: 'Main',
      component: Main,
      meta: {
        requireAuth: true,  // 添加该字段，表示进入这个路由是需要登录的
      },
      children: [
        {
          path: 'user',
          name: 'User',
          component: User
        },
        {
          path: 'role',
          name: 'Role',
          component: Role
        },
        {
          path: 'function',
          name: 'Function',
          component: Function
        },
        {
          path: 'valuelist/:rosterid/:datatype',
          name: 'ValueList',
          component: ValueList
        },
        {
          path: 'list',
          name: 'List',
          component: List
        },
        {
          path: 'txn',
          name: 'Txn',
          component: Txn
        },
        {
          path: 'authCenter',
          name: 'AuthCenter',
          component: AuthCenter
        },
        {
          path: 'codedict',
          name: 'Codedict',
          component: Codedict
        },
        {
          path: 'ipaddr',
          name: 'Ipaddr',
          component: Ipaddr
        },
        {
          path: 'log',
          name: 'Log',
          component: Log
        }
      ]
    },
    {
      path: '*',
      name: 'NotFound',
      component: NotFoundComponent
    }
  ]
})
