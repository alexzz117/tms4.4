<template>
  <div>

    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%">
      <el-table-column label="模块名称" align="left">
        <template slot-scope="scope">
          <a href="javascript:void(0)" @click="showData(scope.row.modelname)">{{scope.row.modelname}}</a>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="待授权信息数" align="left"></el-table-column>
    </el-table>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'

  export default {
    computed: {
      notSelectOne () {
        return this.selectedRows.length !== 1
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.getData()
      })
    },
    data () {
      return {
        tableData: []
      }
    },
    methods: {
      getData () {
        let self = this
        ajax.post({
          url: '/tms/auth/modelList',
          param: {},
          success: function (data) {
            if (data.page) {
              self.tableData = data.page.list
            }
          }
        })
      },
      showData (modelname) {
        this.$router.push({name: 'AuthDataList', query: { modelname: modelname }})
      }
    }
  }
</script>

<style>

</style>
