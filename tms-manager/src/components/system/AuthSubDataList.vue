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
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column label="操作数据" align="left">
        <template slot-scope="scope">
          <a v-if="datavalueLink(scope.row)" href="javascript:void(0)" @click="toCompare(scope.row)">{{scope.row.operatedata_value}}</a>
          <span v-else>{{scope.row.operatedata_value}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="is_main" label="是否为初始操作" align="left"></el-table-column>
      <el-table-column prop="operate_name" label="子操作名称" align="left"></el-table-column>
      <el-table-column prop="real_name" label="操作人" align="left"></el-table-column>
      <el-table-column label="操作时间" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.operate_time | renderDateTime}}</span>
        </template>
      </el-table-column>
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
      }
    },
    data () {
      return {
        modelName: '',
        authId: '',
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
        console.log(data.page.list)
        this.tableData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      datavalueLink (row) {
        return (row.table_name !== 'TMS_COM_MTRAIN')
      },
      // 点击标题的事件
      toCompare (row) {
        let params = {
          operate_name: row.orig_operatename,
          table_name: row.query_table_name,
          table_pk: row.query_table_pk,
          table_pkvalue: row.query_pkvalue,
          auth_id: row.auth_id,
          modelName: this.modelName,
          flag: 1
        }
        this.$router.push({name: 'AuthDataCompare', query: params})
        console.log(row)
      },
      getData () {
        let self = this
        let paramsObj = {
          authId: this.authId,
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        ajax.post({
          url: '/tms/auth/listSubOperate',
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
