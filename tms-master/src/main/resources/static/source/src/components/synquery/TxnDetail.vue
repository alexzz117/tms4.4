<template>
  <div>
    <el-dialog title="交易流水详细信息" :visible.sync="infoDialogVisible" width="900px" :close-on-click-modal="false">
      <el-tabs v-model="tabActiveName" @tab-click="handleTabClick">
        <el-tab-pane label="交易信息" name="operate" v-if="getTabItemShow('operate')">
          <txn-info-query ref="operateDetail" :showItem="selectedRow"></txn-info-query>
        </el-tab-pane>
        <el-tab-pane label="规则命中信息" name="strategy" v-if="getTabItemShow('strategy')">
          <alarm-event-query-strategy-detail ref="strategyDetail" :showItem="selectedRow"></alarm-event-query-strategy-detail>
        </el-tab-pane>
        <el-tab-pane label="统计信息" name="count" v-if="getTabItemShow('count')">
          <txn-stat-query ref="txnStatQuery" :showItem="selectedRow"></txn-stat-query>
        </el-tab-pane>
        <el-tab-pane label="交易用户信息" name="user" v-if="getTabItemShow('user')">
          <alarm-event-query-user-detail ref="userDetail" :showItem="selectedRow"></alarm-event-query-user-detail>
        </el-tab-pane>
        <el-tab-pane label="报警处置信息" name="handle" v-if="getTabItemShow('handle')">
          <alarm-event-query-handle-detail ref="handleDetail" :showItem="selectedRow"></alarm-event-query-handle-detail>
        </el-tab-pane>
        <el-tab-pane label="设备信息" name="device" v-if="getTabItemShow('device')">
          <alarm-event-query-device-detail ref="deviceDetail" :showItem="selectedRow"></alarm-event-query-device-detail>
        </el-tab-pane>
        <el-tab-pane label="设备指纹信息" name="deviceFinger" v-if="getTabItemShow('deviceFinger')">
          <alarm-event-query-device-finger-detail ref="deviceFingerDetail" :showItem="selectedRow"></alarm-event-query-device-finger-detail>
        </el-tab-pane>
        <el-tab-pane label="会话信息" name="session" v-if="getTabItemShow('session')">
        </el-tab-pane>
      </el-tabs>

      <div slot="footer" class="dialog-footer">
        <el-button @click="infoDialogVisible = false" size="large">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
  import AlarmEventQueryOperateDetail from '@/components/synquery/AlarmEventQueryOperateDetail'
  import AlarmEventQueryStrategyDetail from '@/components/synquery/AlarmEventQueryStrategyDetail'
  import AlarmEventQueryHandleDetail from '@/components/synquery/AlarmEventQueryHandleDetail'
  import AlarmEventQueryUserDetail from '@/components/synquery/AlarmEventQueryUserDetail'
  import AlarmEventQueryDeviceDetail from '@/components/synquery/AlarmEventQueryDeviceDetail'
  import AlarmEventQueryDeviceFingerDetail from '@/components/synquery/AlarmEventQueryDeviceFingerDetail'
  import TxnStatQuery from '@/components/synquery/TxnStatQuery'
  import TxnInfoQuery from '@/components/synquery/TxnInfoQuery'

  export default {
    name: 'txnDetail',
    computed: {
      selectedRow () {
        return this.txn
      }
    },
    data () {
      return {
        infoDialogVisible: false,
        tabActiveName: 'operate',
        tabShowItem: ['operate', 'strategy', 'count', 'user', 'handle', 'device', 'deviceFinger', 'session'],
        firstActive: {
          operate: true,
          strategy: true,
          count: true,
          user: true,
          handle: true,
          device: true,
          deviceFinger: true
        }
      }
    },
    props: ['txn', 'defaultTab', 'showItem'],
    methods: {
      handleTabClick (tab, event) {
        this.tabClick(tab.name)
      },
      open () {
        let self = this
        this.infoDialogVisible = true
        if (this.showItem) {
          this.tabShowItem = this.showItem
        }
        if (this.defaultTab) {
          this.tabActiveName = this.defaultTab
        } else {
          this.tabActiveName = 'operate'
        }
        for (let key in this.firstActive) {
          this.firstActive[key] = true
        }
        setTimeout(function () {
          if (self.defaultTab) {
            self.tabClick(self.defaultTab)
          } else {
            self.$refs.operateDetail.loadData(self.selectedRow)
          }
        }, 200)
      },
      tabClick (tabName) {
        switch (tabName) {
          case 'operate': {
            if (this.firstActive.operate) {
              let self = this
              setTimeout(function () {
                self.$refs.operateDetail.loadData(self.selectedRow)
                self.firstActive.operate = false
              }, 200)
            }
            break
          }
          case 'strategy': {
            if (this.firstActive.strategy) {
              let self = this
              setTimeout(function () {
                self.$refs.strategyDetail.loadData(self.selectedRow)
                self.firstActive.strategy = false
              }, 200)
            }
            break
          }
          case 'count': {
            if (this.firstActive.count) {
              let self = this
              setTimeout(function () {
                self.firstActive.count = false
              }, 200)
            }
            break
          }
          case 'user': {
            if (this.firstActive.user) {
              let self = this
              setTimeout(function () {
                self.$refs.userDetail.loadData(self.selectedRow)
                self.firstActive.user = false
              }, 200)
            }
            break
          }
          case 'handle': {
            if (this.firstActive.handle) {
              let self = this
              setTimeout(function () {
                self.$refs.handleDetail.loadData(self.selectedRow)
                self.firstActive.handle = false
              }, 200)
            }
            break
          }
          case 'device': {
            if (this.firstActive.device) {
              let self = this
              setTimeout(function () {
                self.$refs.deviceDetail.loadData(self.selectedRow)
                self.firstActive.device = false
              }, 200)
            }
            break
          }
          case 'deviceFinger': {
            if (this.firstActive.deviceFinger) {
              let self = this
              setTimeout(function () {
                self.$refs.deviceFingerDetail.loadData(self.selectedRow)
                self.firstActive.deviceFinger = false
              }, 200)
            }
            break
          }
          default: {
          }
        }
      },
      getTabItemShow (name) {
        let showItems = this.tabShowItem
        if (showItems.indexOf(name) > -1) {
          return true
        } else {
          return false
        }
      }
    },
    components: {
      'alarm-event-query-operate-detail': AlarmEventQueryOperateDetail,
      'alarm-event-query-strategy-detail': AlarmEventQueryStrategyDetail,
      'alarm-event-query-handle-detail': AlarmEventQueryHandleDetail,
      'alarm-event-query-user-detail': AlarmEventQueryUserDetail,
      'alarm-event-query-device-detail': AlarmEventQueryDeviceDetail,
      'alarm-event-query-device-finger-detail': AlarmEventQueryDeviceFingerDetail,
      'txn-stat-query': TxnStatQuery,
      'txn-info-query': TxnInfoQuery
    }
  }
</script>
<style>
</style>
