<template>
  <div>
    <section class="table">
      <el-table
        :data="tableData"
        style="width: 100%">

        <el-table-column
          fixed="left"
          label="操作"
          width="150">
          <template slot-scope="scope">
            <el-button type="text" size="small">编辑</el-button>
            <el-button type="text" size="small">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="auth_id" label="采集方式代码" align="left">
        </el-table-column>
        <el-table-column prop="auth_id" label="采集方式名称" align="left"></el-table-column>
        <el-table-column prop="txnname" label="最多设备数" align="left"></el-table-column>
        <el-table-column prop="operatename" label="匹配阀值" align="left"></el-table-column>
        <el-table-column prop="sub_operate_num" label="Cookie名称" align="left"></el-table-column>
        <el-table-column prop="real_name" label="设备标识生成方式" align="left" width="140"></el-table-column>
      </el-table>

    </section>

    <el-dialog :title="dialogTitle" :visible.sync="dictDialogVisible" :close-on-click-modal="false">
      <el-form :model="dialogForm" :rules="rules" ref="dialogForm" style="text-align: left;">
        <el-form-item label="是否通过授权:" :label-width="formLabelWidth" prop="auth_status">
          <!--<el-radio v-model="dialogForm.auth_status" label="1">是</el-radio>-->
          <!--<el-radio v-model="dialogForm.auth_status" label="2">否</el-radio>-->
          <el-switch
            v-model="dialogForm.auth_status"
            active-value="1"
            inactive-value="2"
          >
          </el-switch>
        </el-form-item>
        <el-form-item label="授权说明:" :label-width="formLabelWidth" prop="auth_msg">
          <el-input type="textarea" v-model="dialogForm.auth_msg" :maxlength="2048"></el-input>
        </el-form-item>
        <div>
          <el-form-item label=" " :label-width="formLabelWidth">
            <el-button type="primary" @click="submitForm('dialogForm')" size="large">保 存</el-button>
            <el-button @click="dictDialogVisible = false" size="large">取 消</el-button>
          </el-form-item>
        </div>
      </el-form>
    </el-dialog>

  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'

  export default {
    computed: {},
    data() {
      return {
        activeName: 'appList',
        tableData: [],
        dialogTitle: '授权意见',
        dictDialogVisible: false,
        dialogForm: this.initDialogForm(),
        rules: {
          auth_status: [
            {required: true, message: '请选择是否通过授权', trigger: 'blur'}
          ],
          auth_msg: [
            {required: true, message: '请输入授权说明', trigger: 'blur'},
            {max: 2048, message: '长度在2048个字符以内', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'}
          ]
        },
        formLabelWidth: '130px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectRow: {},
        selectedRows: []
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.getData()
      })
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      }
    },
    methods: {
      handleTabClick(tab, event) {

      },
      initDialogForm() {
        return {
          auth_status: '1',
          auth_msg: ''
        }
      },
      handleSizeChange(val) {
        this.currentPage = 1
        this.pageSize = val
        this.getData()
      },
      handleCurrentChange(val) {
        this.currentPage = val
        this.getData()
      },
      handleSelectionChange(rows) {
        this.selectedRows = rows
      },
      addData(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this

            let authIds = []
            let funcName = []
            let operatedataValue = []
            let txnnameTemp = []
            let txnname = ''
            var operatenameTemp = []
            let operatename = ''

            let selectedRow = this.selectRow
            for (let loop of this.selectedRows) {
              authIds.push(loop.auth_id)
              funcName.push(loop.funcname)
              operatedataValue.push(loop.operatedata_value)
              if (loop.txnname !== undefined && loop.txnname !== '') {
                txnnameTemp.push(loop.txnname)
              }
              if (loop.operatename !== undefined && loop.operatename !== '') {
                operatenameTemp.push(loop.operatename)
              }
            }


            let paramsObj = {
              AUTH_IDS: authIds.join(','),
              FUNCNAME: funcName.join(','),
              TXNNAME: txnnameTemp.join(','),
              OPERATENAME: operatenameTemp.join(','),
              OPERATEDATA_VALUE: operatedataValue.join('~'),
              AUTH_STATUS: this.dialogForm.auth_status,
              AUTH_MSG: this.dialogForm.auth_msg
            }

            ajax.post({
              url: '/auth/modAuth',
              param: paramsObj,
              success: function (data) {
                self.getData()
                self.$message.success('授权成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      getData() {
        let self = this
        let paramsObj = {}
        ajax.post({
          url: '/dfp/appList',
          param: paramsObj,
          success: function (data) {
            if (data.appList && data.appList.length > 0) {
              self.tableData = data.appList
            }
          }
        })
      },
      showSub(row) {
        let params = {
          authId: row.auth_id,
          modelName: this.modelName
        }
        this.$router.push({name: 'authSubDataList', query: params})
      },
      showLog(row) {
        let params = {
          authId: row.auth_id,
          modelName: this.modelName
        }
        this.$router.push({name: 'authLogList', query: params})
      },
      auth(row) {
        this.selectRow = row
        this.dialogForm = this.initDialogForm()
        this.dictDialogVisible = true
      },
      submitForm(formName) {
        this.addData(formName)
      },
      back() {
        this.$router.go(-1)
      }
    }
  }
</script>

<style>

</style>
