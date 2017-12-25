<template>
  <div>
    <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm" :rules="queryRules"
             :inline="inline" style="text-align: left">
      <el-form-item label="代码类别key:" prop="category_id">
        <el-input v-model="queryShowForm.category_id" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
      <el-form-item label="代码类别value:" prop="category_name">
        <el-input v-model="queryShowForm.category_name" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
    </el-form>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" @click="openDialog('edit')" :disabled="notSelectOne">编辑</el-button>
      <el-button plain class="el-icon-delete" @click="delData()" :disabled="notSelectOne">删除</el-button>
      <el-button plain class="el-icon-view" @click="showData()" :disabled="notSelectOne">查看</el-button>
      <el-button class="el-icon-search" type="primary" @click="searchData('queryShowForm')">查询</el-button>
    </div>

    <el-table
      :data="dictData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column prop="category_id" label="代码类别key" align="left" width="180"></el-table-column>
      <el-table-column prop="category_name" label="代码类别value" align="left" width="180"></el-table-column>
      <el-table-column prop="category_sql" label="代码类别sql" align="left"></el-table-column>
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
      <el-form :model="dictDialogForm" :rules="rules" ref="dictDialogForm">
        <el-form-item label="代码类别key:" :label-width="formLabelWidth" prop="category_id">
          <el-input v-model="dictDialogForm.category_id" auto-complete="off" :disabled="categoryIdReadonly" :maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="代码类别value:" :label-width="formLabelWidth" prop="category_name">
          <el-input v-model="dictDialogForm.category_name" auto-complete="off" :maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="代码类别sql:" :label-width="formLabelWidth" prop="category_sql">
          <el-input v-model="dictDialogForm.category_sql" auto-complete="off" :maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="描述信息:" :label-width="formLabelWidth" prop="info">
          <el-input type="textarea" v-model="dictDialogForm.info" :maxlength="200"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dictDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('dictDialogForm')">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import check from '@/common/check'
  import util from '@//common/util'

  export default {
    computed: {
      categoryIdReadonly () {
        return this.dialogType === 'edit'
      },
      notSelectOne () {
        return this.selectedRows.length !== 1
      }
    },
    data () {
      let categoryIdExist = (rule, value, callback) => {
        if (this.dialogType === 'edit' && this.selectedRows[0].category_id === value) {
          callback()
        } else {
          ajax.post({
            url: '/codedict/check/categoryId',
            param: {categoryId: value},
            success: function (data) {
              if (data.checkresult === false) {
                return callback(new Error('该代码类别key已存在'))
              } else {
                callback()
              }
            }
          })
        }
      }
      return {
        inline: true,
        dialogTitle: '',
        dictDialogVisible: false,
        formLabelWidth: '130px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        queryShowForm: {
          category_id: '',
          category_name: ''
        },
        queryForm: {
          category_id: '',
          category_name: ''
        },
        dictData: [],
        selectedRows: [],
        dictDialogForm: this.initDialogForm(),
        queryRules: {
          category_id: [
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          category_name: [
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        },
        rules: {
          category_id: [
            { required: true, message: '请输入代码类别key', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' },
            { validator: categoryIdExist, trigger: 'blur' }
          ],
          category_name: [
            { required: true, message: '请输入代码类别value', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          category_sql: [
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          info: [
            { max: 200, message: '长度在200个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        }
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.getData()
      })
    },
    methods: {
      initDialogForm () {
        return {
          category_id: '',
          category_name: '',
          category_sql: '',
          info: ''
        }
      },
      searchData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            Object.assign(this.queryForm, this.queryShowForm)
            this.getData()
          }
        })
      },
      getData () {
        let self = this
        let paramsObj = {
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        let upperParams = util.toggleObjKey(this.queryForm, 'upper')
        Object.assign(paramsObj, upperParams)
        ajax.post({
          url: '/codedict/category/list',
          param: paramsObj,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      addData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this
            let paramsObj = this.dictDialogForm
            ajax.post({
              url: '/codedict/category/add',
              param: paramsObj,
              success: function (data) {
                self.getData()
                self.$message('添加成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      delData () {
        let self = this
        let data = this.selectedRows[0]
        let length = this.selectedRows.length
        if (length !== 1) {
          this.$message('请选择一行代码类别信息。')
          return
        }
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/codedict/category/del',
            param: {
              categoryId: data.category_id
            },
            success: function (data) {
              self.$message('删除成功')
              self.getData()
            }
          })
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
      },
      updData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this
            let paramsObj = this.dictDialogForm
            ajax.post({
              url: '/codedict/category/update',
              param: paramsObj,
              success: function (data) {
                self.getData()
                self.$message('编辑成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      showData () {
        let length = this.selectedRows.length
        if (length !== 1) {
          this.$message('请选择一行代码类别信息。')
          return
        }
        this.$router.push({name: 'codeDictInfo', query: { category_id: this.selectedRows[0].category_id }})
      },
      submitForm (formName) {
        if (this.dialogType === 'add') {
          this.addData(formName)
        } else if (this.dialogType === 'edit') {
          this.updData(formName)
        }
      },
      bindGridData (data) {
        this.dictData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
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
      openDialog (dialogType) {
        this.dialogType = dialogType
        if (dialogType === 'edit') {
          this.dialogTitle = '编辑代码类别'
          let length = this.selectedRows.length
          if (length !== 1) {
            this.$message('请选择一行代码类别信息。')
            return
          }
          // 拷贝而不是赋值
          Object.assign(this.dictDialogForm, this.selectedRows[0])
        } else if (dialogType === 'add') {
          this.dialogTitle = '新建代码类别'
          this.dictDialogForm = this.initDialogForm()
        }
        this.dictDialogVisible = true
        if (this.$refs['dictDialogForm']) {
          this.$refs['dictDialogForm'].clearValidate()
        }
      }
    }
  }
</script>

<style>

</style>
