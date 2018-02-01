import Vue from 'vue'
import Router from 'vue-router'

// 引入组件
import Login from '@/frame/Login'
import Main from '@/frame/Main'

import User from '@/components/auth/User'
import Role from '@/components/auth/Role'
import Func from '@/components/auth/Func'

import AlarmEventSend from '@/components/alarm/AlarmEventSend'
import AlarmeventExecute from '@/components/alarm/AlarmeventExecute'
import AlarmeventAudit from '@/components/alarm/AlarmeventAudit'

import List from '@/components/run/List'
import NameList from '@/components/run/NameList'
import ShowValueList from '@/components/run/ShowValueList'
import ValueList from '@/components/run/ValueList'
import Txn from '@/components/run/Txn'

import AuthCenter from '@/components/system/AuthCenter'
import AuthCenterShow from '@/components/system/AuthCenterShow'
import AuthDataList from '@/components/system/AuthDataList'
import AuthDataListShow from '@/components/system/AuthDataListShow'
import AuthLogList from '@/components/system/AuthLogList'
import AuthSubDataList from '@/components/system/AuthSubDataList'
import AuthDataCompare from '@/components/system/AuthDataCompare'
import Codedict from '@/components/system/Codedict'
import CodedictInfo from '@/components/system/CodedictInfo'
import Ipaddr from '@/components/system/Ipaddr'
import Log from '@/components/system/Log'
import DeviceFingerPrint from '@/components/system/DeviceFingerPrint'
import Time from '@/components/system/Time'
import ZkAdmin from '@/components/system/ZkAdmin'
import Channel from '@/components/system/Channel'

import TxnEventQuery from '@/components/synquery/TxnEventQuery'
import AlarmEventQuery from '@/components/synquery/AlarmEventQuery'

import TxnListReport from '@/components/report/TxnListReport'
import RuleListReport from '@/components/report/RuleListReport'
import AreaListReport from '@/components/report/AreaListReport'
import DateListReport from '@/components/report/DateListReport'
import FraudListReport from '@/components/report/FraudListReport'
// import Dashboard from '@/components/Dashboard'

import NotFoundComponent from '@/components/NotFoundComponent'
import util from '@/common/util'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: util.getWebRootPath(),
  linkActiveClass: 'active',
  routes: [
    {
      path: '/login_vue',
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
          path: 'user_vue',
          name: 'user',
          component: User
        },
        {
          path: 'role_vue',
          name: 'role',
          component: Role
        },
        {
          path: 'func_vue',
          name: 'func',
          component: Func
        },
        {
          path: 'valuelist_vue',
          name: 'valuelist',
          component: ValueList
        },
        {
          path: 'showValueList_vue',
          name: 'showValueList',
          component: ShowValueList
        },
        {
          path: 'list_vue',
          name: 'list',
          component: List
        },
        {
          path: 'namelist_vue',
          name: 'namelist',
          component: NameList
        },
        {
          path: 'alarmEventSend_vue',
          name: 'alarmEventSend',
          component: AlarmEventSend
        },
        {
          path: 'alarmeventExecute_vue',
          name: 'alarmeventExecute',
          component: AlarmeventExecute
        },
        {
          path: 'alarmeventAudit_vue',
          name: 'alarmeventAudit',
          component: AlarmeventAudit
        },
        {
          path: 'txn_vue',
          name: 'txn',
          component: Txn
        },
        {
          path: 'authCenter_vue',
          name: 'authCenter',
          component: AuthCenter
        },
        {
          path: 'authCenterShow_vue',
          name: 'authCenterShow',
          component: AuthCenterShow
        },
        {
          path: 'authDataCompare_vue',
          name: 'authDataCompare',
          component: AuthDataCompare
        },
        {
          path: 'authDataList_vue',
          name: 'authDataList',
          component: AuthDataList
        },
        {
          path: 'authDataListShow_vue',
          name: 'authDataListShow',
          component: AuthDataListShow
        },
        {
          path: 'authLogList_vue',
          name: 'authLogList',
          component: AuthLogList
        },
        {
          path: 'authSubDataList_vue',
          name: 'authSubDataList',
          component: AuthSubDataList
        },
        {
          path: 'codedict_vue',
          name: 'codedict',
          component: Codedict
        },
        {
          path: 'codeDictInfo_vue',
          name: 'codeDictInfo',
          component: CodedictInfo
        },
        {
          path: 'ipaddr_vue',
          name: 'ipaddr',
          component: Ipaddr
        },
        {
          path: 'log_vue',
          name: 'log',
          component: Log
        },
        {
          path: 'txnEventQuery_vue',
          name: 'txnEventQuery',
          component: TxnEventQuery
        },
        {
          path: 'alarmEventQuery_vue',
          name: 'alarmEventQuery',
          component: AlarmEventQuery
        },
        {
          path: 'txnListReport_vue',
          name: 'txnListReport',
          component: TxnListReport
        },
        {
          path: 'fraudListReport_vue',
          name: 'fraudListReport',
          component: FraudListReport
        },
        {
          path: 'ruleListReport_vue',
          name: 'ruleListReport',
          component: RuleListReport
        },{
          path: 'areaListReport_vue',
          name: 'areaListReport',
          component: AreaListReport
        },{
          path: 'dateListReport_vue',
          name: 'dateListReport',
          component: DateListReport
        },
        {
          path: 'time_vue',
          name: 'time',
          component: Time
        },
        {
          path: 'dfp_vue',
          name: 'dfp',
          component: DeviceFingerPrint
        },
        {
          path: 'zkAdmin_vue',
          name: 'zkAdmin',
          component: ZkAdmin
        },
        {
          path: 'channel_vue',
          name: 'channel',
          component: Channel
        }
        // ,
        // {
        //   path: 'dashboard_vue',
        //   name: 'dashboard',
        //   component: Dashboard
        // }
      ]
    },
    {
      path: '*',
      name: 'notFound',
      component: NotFoundComponent
    }
  ]
})
