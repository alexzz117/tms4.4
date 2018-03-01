<template>
  <div>
    <section class="table">
    <el-table
      :data="tableData"
      style="width: 100%">
      <el-table-column label="模块名称" align="left">
        <template slot-scope="scope">
          <a href="javascript:void(0)" @click="showData(scope.row.modelname)">{{scope.row.modelname}}</a>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="待授权信息数" align="left"></el-table-column>
    </el-table>
    </section>
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
          url: '/auth/modelList',
          param: {},
          success: function (data) {
            if (data.page) {
              let tableList = data.page.list
              let tableListTemp = []
              for(let item of tableList) {
                if(item.modelname !== '交易策略') {
                  tableListTemp.push(item)
                }
              }

              self.tableData = tableListTemp
            }
          }
        })
      },
      showData (modelname) {
        this.$router.push({name: 'authDataList', query: { modelname: modelname }})
      }
    }
  }
</script>

<style>

</style>
