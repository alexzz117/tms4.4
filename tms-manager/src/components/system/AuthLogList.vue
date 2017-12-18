<template>
  <div>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-back" type="primary" @click="back()">返回</el-button>
    </div>

    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <!--<el-table-column type="selection" width="55" align="left"></el-table-column>-->
      <el-table-column prop="real_name" label="操作员" align="left"></el-table-column>
      <el-table-column prop="login_name" label="用户名" align="left"></el-table-column>
      <el-table-column label="操作时间" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.operate_time | renderDateTime}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="func_name" label="所属功能" align="left"></el-table-column>
      <el-table-column prop="operate_result" label="操作结果" align="left"></el-table-column>
      <el-table-column prop="operate_data" label="操作数据" align="left"></el-table-column>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;"
                   :current-page="currentPage"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pageSize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>

  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'

  export default {
    computed: {
      notSelectOne () {
        return this.selectedRows.length !== 1
      },
      txnnameRowShow () {
        return this.modelName !== '名单管理'
      },
      subOperateNumShow () {
        return this.modelName === '名单管理' || this.modelName === '交易配置'
      },
      datavalueLink () {
        return !(this.modelName === '名单管理' || this.modelName === '交易配置')
      }
    },
    data () {
      return {
        authId: '',
        modelName: '',
        tableData: [],
        dialogTitle: '',
        dictDialogVisible: false,
        formLabelWidth: '130px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectedRows: []
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.modelName = this.$route.query.modelName
        this.authId = this.$route.query.authId
        this.getData()
      })
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      }
    },
    methods: {
      handleSizeChange (val) {
        // console.log(`每页 ${val} 条`)
        this.currentPage = 1
        this.pageSize = val
        this.getData()
      },
      handleCurrentChange (val) {
        this.currentPage = val
        this.getData()
      },
      handleSelectionChange (rows) {
        this.selectedRows = rows
      },
      bindGridData (data) {
        this.tableData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      getData () {
        let self = this
        let paramsObj = {
          authId: this.authId,
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        ajax.post({
          url: '/manager/auth/toLog',
          param: paramsObj,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      back () {
        this.$router.go(-1)
      }
    }
  }
</script>

<style>

</style>
