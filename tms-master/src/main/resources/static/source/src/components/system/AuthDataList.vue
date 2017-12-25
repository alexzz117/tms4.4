<template>
  <div>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-back" type="primary" @click="back()">返回</el-button>
      <el-button plain class="el-icon-tickets" @click="auth" :disabled="notSelectOne">授权</el-button>
      <el-button plain class="el-icon-view" @click="showSub" :disabled="notSelectOne">查看子操作</el-button>
      <el-button plain class="el-icon-view" @click="showLog()" :disabled="notSelectOne">查看日志</el-button>
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
          <a v-if="datavalueLink" href="javascript:void(0)" @click="toCompare(scope.row)">{{scope.row.datavalue}}</a>
          <span v-else>{{scope.row.datavalue}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="auth_id" label="授权编号" align="left"></el-table-column>
      <el-table-column v-if="txnnameRowShow" prop="txnname" label="所属交易" align="left"></el-table-column>
      <el-table-column prop="operatename" label="操作名称" align="left"></el-table-column>
      <el-table-column v-if="subOperateNumShow" prop="sub_operate_num" label="子操作个数" align="left"></el-table-column>
      <el-table-column prop="real_name" label="提交授权人" align="left"></el-table-column>
      <el-table-column label="提交授权时间" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.proposer_time | renderDateTime}}</span>
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

    <el-dialog :title="dialogTitle" :visible.sync="dictDialogVisible">
      <el-form :model="dialogForm" :rules="rules" ref="dialogForm" style="text-align: left;">
        <el-form-item label="是否通过授权:" :label-width="formLabelWidth" prop="auth_status">
          <el-radio v-model="dialogForm.auth_status" label="1">是</el-radio>
          <el-radio v-model="dialogForm.auth_status" label="2">否</el-radio>
        </el-form-item>
        <el-form-item label="授权说明:" :label-width="formLabelWidth" prop="auth_msg">
          <el-input type="textarea" v-model="dialogForm.auth_msg" :maxlength="2048"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dictDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('dialogForm')">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'

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
        modelName: '',
        tableData: [],
        dialogTitle: '授权意见',
        dictDialogVisible: false,
        dialogForm: this.initDialogForm(),
        rules: {
          auth_status: [
            { required: true, message: '请选择是否通过授权', trigger: 'blur' }
          ],
          auth_msg: [
            { required: true, message: '请输入授权说明', trigger: 'blur' },
            { max: 2048, message: '长度在2048个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        },
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
        this.modelName = this.$route.query.modelname
        this.getData()
      })
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      }
    },
    methods: {
      initDialogForm () {
        return {
          auth_status: '1',
          auth_msg: ''
        }
      },
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
        this.$router.push({name: 'authDataCompare', query: params})
        console.log(row)
      },
      addData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this

            let authIds
            let funcName
            let operatedataValue
            let txnname = ''
            let operatename = ''

            let selectedRow = this.selectedRows[0]
            authIds = selectedRow.auth_id
            funcName = selectedRow.funcname
            operatedataValue = selectedRow.operatedata_value
            let txnnameTemp = selectedRow.txnname
            if (txnnameTemp !== undefined && txnnameTemp !== '') {
              txnname = txnnameTemp
            }

            var operatenameTemp = selectedRow.operatename
            if (operatenameTemp !== undefined && operatenameTemp !== '') {
              operatename = operatenameTemp
            }

            let paramsObj = {
              AUTH_IDS: authIds,
              FUNCNAME: funcName,
              TXNNAME: txnname,
              OPERATENAME: operatename,
              OPERATEDATA_VALUE: operatedataValue,
              AUTH_STATUS: this.dialogForm.auth_status,
              AUTH_MSG: this.dialogForm.auth_msg
            }

            ajax.post({
              url: '/auth/modAuth',
              param: paramsObj,
              success: function (data) {
                self.getData()
                self.$message('授权成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      getData () {
        let self = this
        let paramsObj = {
          modelName: this.modelName,
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        ajax.post({
          url: '/auth/authList',
          param: paramsObj,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      showSub () {
        let length = this.selectedRows.length
        if (length !== 1) {
          this.$message('请选择一行授权信息。')
          return
        }
        let params = {
          authId: this.selectedRows[0].auth_id,
          modelName: this.modelName
        }
        this.$router.push({name: 'authSubDataList', query: params})
      },
      showLog () {
        let length = this.selectedRows.length
        if (length !== 1) {
          this.$message('请选择一行授权信息。')
          return
        }
        let params = {
          authId: this.selectedRows[0].auth_id,
          modelName: this.modelName
        }
        this.$router.push({name: 'authLogList', query: params})
      },
      auth () {
        this.dialogForm = this.initDialogForm()
        this.dictDialogVisible = true
      },
      submitForm (formName) {
        this.addData(formName)
      },
      back () {
        this.$router.go(-1)
      }
    }
  }
</script>

<style>

</style>
