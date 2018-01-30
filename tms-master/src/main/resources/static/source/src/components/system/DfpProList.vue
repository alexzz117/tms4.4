<template>
  <div>

    <div style="margin-bottom: 10px; text-align: left; height: 30px;">
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>

      <div style="float:right; padding-right: 15px;">
        <el-input v-model="queryShowForm.prop_name" placeholder="属性名称" class="dfp-app-list-form-item" auto-complete="off" clearable>
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>

        <!--<el-button type="primary" @click="searchData">搜索</el-button>-->
      </div>

    </div>

    <div>
      <section class="table">
        <el-table
          :data="tableDataShow"
          style="width: 100%">

          <el-table-column
            fixed="left"
            label="操作"
            width="100">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="openDialog('edit', scope.row)">编辑</el-button>
              <el-button type="text" size="small" @click="delData(scope.row)">删除</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="prop_id" label="属性ID" align="left">
          </el-table-column>
          <el-table-column prop="prop_name" label="属性名称" align="left"></el-table-column>
          <el-table-column prop="prop_comment" label="属性说明" align="left"></el-table-column>
        </el-table>

      </section>

      <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" :close-on-click-modal="false" width="600px">
        <el-form :model="dialogForm" :rules="rules" ref="dialogForm" style="text-align: left;">
          <el-form-item label="属性ID:" :label-width="formLabelWidth" prop="prop_id" v-show="dialogType == 'add'">
            <el-input v-model="dialogForm.prop_id" ></el-input>
          </el-form-item>
          <el-form-item label="属性名称:" :label-width="formLabelWidth" prop="prop_name">
            <el-input v-model="dialogForm.prop_name" ></el-input>
          </el-form-item>
          <el-form-item label="属性说明:" :label-width="formLabelWidth" prop="prop_comment">
            <el-input type="textarea" v-model="dialogForm.prop_comment" ></el-input>
          </el-form-item>
          <div>
            <el-form-item label=" " :label-width="formLabelWidth">
              <el-button type="primary" @click="submitForm('dialogForm')" size="large" :disabled="dialogFormSureBtnDisabled">保 存</el-button>
              <el-button @click="dialogVisible = false" size="large">取 消</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>

    </div>
  </div>

</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'
  import dictCode from "../../common/dictCode";

  export default {
    computed: {
      tableDataShow () {
        let appId = this.queryShowForm.prop_name

        let showData = this.tableData.filter((x) => {
          if (appId !== '' && !x.prop_name.includes(appId)) {
            return false
          }

          return true
        })
        return showData
      }
    },
    data() {
      return {
        activeName: 'appList',
        tableData: [],
        dialogTitle: '授权意见',
        dialogVisible: false,
        dialogForm: this.initDialogForm(),
        dialogFormSureBtnDisabled:false,
        rules: {
          prop_id: [
            {required: true, message: '请输入属性ID', trigger: 'blur'},
            { pattern: /^(0|[1-9]\d{0,9})$/, message: '属性ID最多只能输入10位数字' }
          ],
          prop_name: [
            {required: true, message: '请输入属性名称', trigger: 'blur'},
            {max: 60, message: '长度在60个字符以内', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'}
          ],
          prop_comment: [
            // {required: true, message: '请输入属性说明', trigger: 'blur'},
            {max: 500, message: '长度在500个字符以内', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'}
          ]
        },
        queryShowForm: {
          prop_name: ''
        },
        queryForm: {
          prop_name: ''
        },
        formLabelWidth: '150px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectRow: {},
        selectedRows: [],
        tokenList: []
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.tokenList = dictCode.getCodeItems('tms.dfp.token_type')
        this.getData()
      })
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      },
      tokenRender (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.dfp.token_type', value)
      }
    },
    methods: {
      handleTabClick(tab, event) {

      },
      maxDevicesValid (rule, value, callback) {
        if (parseInt(value) > 100 || parseInt(value) < 1) {
          return callback(new Error('最多设备数只能为1~100间的整数'))
        }
        callback()
      },
      thresholdValid (rule, value, callback) {
        if (!util.isNumber(value, '+', '2') || parseFloat(value) > 1 || parseFloat(value) <= 0) {
          return callback(new Error('匹配阀值需是0-1之间的两位小数'))
        }
        callback()
      },
      initDialogForm() {
        return {
          prop_id: '',
          prop_name: '',
          prop_comment: ''
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
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.dialogFormSureBtnDisabled = true
            let self = this
            let submitParam = {}
            Object.assign(submitParam, this.dialogForm)
            // submitParam.TOKEN_TYPE = submitParam.is_token
            submitParam = util.toggleObjKey(submitParam, 'upper')

            let jsonData = {}
            let message = '提交成功'
            if (this.dialogType === 'add') {
              jsonData.add = [submitParam]
              message = '创建成功'
            } else {
              jsonData.mod = [submitParam]
              message = '编辑成功'
            }
            let finalJsonData = {}
            finalJsonData.postData = jsonData
            // finalJsonData.txnId = this.txnIdParent

            ajax.post({
              url: '/dfp/proSave',
              param: finalJsonData,
              success: function (data) {
                self.getData()
                self.$message.success(message)
                self.dialogVisible = false
                self.dialogFormSureBtnDisabled = false
              },
              error: function (data) {
                if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
                  self.$message.error(data.error.join('|'))
                } else {
                  self.$message.error(data.error)
                }
                self.dialogFormSureBtnDisabled = false
              },
              fail: function () {
                self.dialogFormSureBtnDisabled = false
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
          url: '/dfp/proList',
          param: paramsObj,
          success: function (data) {
            if (data.row && data.row.length > 0) {
              self.tableData = data.row
            }
          }
        })
      },
      delData (row) {
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let jsonData = {}
          let rowData = util.toggleObjKey(row, 'upper')
          jsonData.del = [rowData]
          let finalJsonData = {}

          finalJsonData.postData = jsonData
          let self = this
          ajax.post({
            url: '/dfp/proSave',
            param: finalJsonData,
            success: function (data) {
              self.getData()
              self.$message.success('删除成功')
            }
          })
        })
      },
      showSub(row) {
        let params = {
          authId: row.auth_id,
          modelName: this.modelName
        }
        this.$router.push({name: 'authSubDataList', query: params})
      },
      openDialog (dialogType, row) {
        this.dialogType = dialogType
        let self = this
        if(dialogType === 'add') {
          this.dialogForm = this.initDialogForm()
        }
        else if (dialogType === 'edit') {
          this.dialogForm = this.initDialogForm()
          Object.assign(this.dialogForm, row)
        }
        this.dialogVisible = true
        setTimeout(function () {
          if (self.$refs['dialogForm']) {
            self.$refs['dialogForm'].clearValidate()
          }
        }, 100)
      },
      searchData () {

      },
      back() {
        this.$router.go(-1)
      }
    }
  }
</script>

<style>
  .dfp-app-list-form-item{
    width: 200px;
  }
</style>
