<template>
  <div>
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="采集方式定义" name="appList">
        <dfp-app-list></dfp-app-list>
      </el-tab-pane>
      <el-tab-pane label="属性定义" name="proList">配置管理</el-tab-pane>
      <el-tab-pane label="采集方式属性定义" name="appProList">角色管理</el-tab-pane>
    </el-tabs>

  </div>
</template>

<script>
  import DfpAppList from '@/components/system/DfpAppList'

  export default {
    computed: {
    },
    data () {
      return {
        activeName: 'appList'
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        // this.getData()
      })
    },
    methods: {
      handleTabClick (tab, event) {

      }
    },
    components: {
      'dfp-app-list': DfpAppList
    }
  }
</script>

<style>

</style>
