<template>
  <div>
    <el-form label-position="right" label-width="80px" :model="logForm"
             :inline="inline" style="text-align: left">
      <el-form-item label="操作员" prop="operator_name">
        <el-input v-model="logForm.operator_name"></el-input>
      </el-form-item>
      <el-form-item label="所属功能" prop="operate_func">
        <el-select v-model="logForm.operate_func" placeholder="所属功能">
          <el-option label="全部" value=""></el-option>
          <el-option
            v-for="item in funcList"
            :key="item.func_id"
            :label="item.func_name"
            :value="item.func_id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间" prop="real_name">
        <el-date-picker
          v-model="value6"
          type="datetimerange"
          range-separator="到"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd HH:mm:ss">
        </el-date-picker>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-search" type="primary" @click="selLog">查询</el-button>
    </div>
    <el-table
      :data="roleData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="35" align="left"></el-table-column>
      <el-table-column prop="real_name" label="操作员" align="left" width="160"></el-table-column>
      <el-table-column prop="login_name" label="用户名" align="left" width="160"></el-table-column>
      <el-table-column prop="operate_time" label="操作时间" align="left" width="100" :formatter="formatterDate"></el-table-column>
      <el-table-column prop="func_name" label="所属功能" align="left" width="120"></el-table-column>
      <el-table-column prop="operate_result" label="操作结果" align="left" width="150"></el-table-column>
      <el-table-column prop="operate_data" label="操作数据" align="left"></el-table-column>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :current-page="currentPage"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pagesize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>
  </div>
</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'

  export default {
    created () {
      this.selLog()
      this.selFunc()
    },
    methods: {
      handleSizeChange (val) {
        console.log(`每页 ${val} 条`)
      },
      handleCurrentChange (val) {
        console.log(`当前页: ${val}`)
      },
      handleSelectionChange (val) {
        this.multipleSelection = val
      },
      bindGridData (data) {
        this.roleData = data.page.list
        this.currentPage = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      selFunc () {
        var self = this
        ajax.post({
          url: '/func/getAll',
          success: function (data) {
            self.funcList = data.row
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      selLog () {
        var self = this
        var operateTime = ''
        var endTime = ''
        if (self.value6 !== null && self.value6.length > 0) {
          operateTime = self.value6[0]
          endTime = self.value6[1]
        }
        ajax.post({
          url: '/log/list',
          param: {
            operator_name: self.logForm.operator_name,
            operate_func: self.logForm.operate_func,
            operate_time: operateTime,
            end_time: endTime,
            pageindex: 1,
            pagesize: 10
          },
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      formatterDate (row, column) {
        return util.renderDateTime(row.operate_time)
      }
    },
    data () {
      return {
        inline: true,
        value6: [],
        funcList: [],
        logForm: {
          operator_name: '',
          operate_func: '',
          operate_time: '',
          end_time: ''
        },
        roleData: [{
          operate_time: 1513130185000,
          func_name: '用户登录',
          operate_data: '用户名称:admin;',
          split_rows_num: 1,
          log_mac: '530BB7696F839F0606CC375A201CBF74',
          operate_result: '1',
          log_id: '77564A9B988A40359DADCBE08D49D40A',
          real_name: '系统管理员',
          login_name: 'admin'
        }],
        currentPage: 4,
        pagesize: 10,
        total: 100,
        formLabelWidth: '100px',
        multipleSelection: [],
        flag: ''
      }
    }
  }
</script>

<style>

</style>
