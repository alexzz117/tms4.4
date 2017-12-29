<template>
  <div>

    <el-row style="height: 100%;border: 1px solid #eee">
      <el-col :span="12" style="border-right: 1px solid #eee">
        <div style="height:38px;margin-top: 5px;text-align: left;border-bottom: 1px solid #eee; margin-left: 10px;">
          <el-row>
            <el-col :span="16">
                <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
            </el-col>
            <el-col :span="8">
                <div style="margin-right: 10px">
                  <el-input v-model="queryShowForm.category_id" auto-complete="off" :maxlength=50
                            @keyup.enter.native="searchData('queryShowForm')"   placeholder="请输入搜索内容">
                    <i slot="prefix" class="el-input__icon el-icon-search"></i>
                  </el-input>

                </div>
            </el-col>
          </el-row>
        </div>
        <el-table
        :data="dictData"
        highlight-current-row
        style="width: 100%"
        @current-change="handleSelectionChange">

        <el-table-column
        label="操作"
        width="80">
        <template slot-scope="scope">
        <el-button type="text" size="small" icon="el-icon-edit" title="编辑" @click="openDialog('edit', scope.row)"></el-button>
        <el-button type="text" size="small" icon="el-icon-delete" title="删除" @click="delData(scope.row)"></el-button>
        <!--<el-button type="text" size="small" @click="showData(scope.row)">查看</el-button>-->
        </template>
        </el-table-column>

        <el-table-column prop="category_id" label="代码类别key" align="left"></el-table-column>
        <el-table-column prop="category_name" label="代码类别value" align="left"></el-table-column>
        <!--<el-table-column prop="category_sql" label="代码类别sql" align="left"></el-table-column>-->
        </el-table>

        <el-pagination style="margin-top: 10px; text-align: right;"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-sizes="[10, 25, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, next, jumper"
        :total="total">
        </el-pagination>
      </el-col>

      <el-col :span="12" style="border-right: 1px solid #eee">
        <div style="height:38px;margin-top: 5px;text-align: left;border-bottom: 1px solid #eee; margin-left: 10px;">
          <el-row v-show="dictInfoShow">
            <el-button plain class="el-icon-plus" @click="openInfoDialog('add')">新建</el-button>
          </el-row>
        </div>
        <el-table
          :data="dictInfoData"
          style="width: 100%"
          v-show="dictInfoShow"
          >

          <el-table-column
            label="操作"
            width="80">
            <template slot-scope="scope">
              <el-button type="text" size="small" icon="el-icon-edit" title="编辑" @click="openInfoDialog('edit', scope.row)"></el-button>
              <el-button type="text" size="small" icon="el-icon-delete" title="删除" @click="delInfoData(scope.row)"></el-button>
              <!--<el-button type="text" size="small" @click="showData(scope.row)">查看</el-button>-->
            </template>
          </el-table-column>

          <el-table-column prop="code_key" label="代码key" align="left"></el-table-column>
          <el-table-column prop="code_value" label="代码value" align="left"></el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!--<el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm" :rules="queryRules"-->
             <!--:inline="inline" style="text-align: left">-->
      <!--<el-form-item label="代码类别key:" prop="category_id">-->
        <!--<el-input v-model="queryShowForm.category_id" auto-complete="off" :maxlength=50></el-input>-->
      <!--</el-form-item>-->
      <!--<el-form-item label="代码类别value:" prop="category_name">-->
        <!--<el-input v-model="queryShowForm.category_name" auto-complete="off" :maxlength=50></el-input>-->
      <!--</el-form-item>-->
    <!--</el-form>-->

    <!--<div style="margin-bottom: 10px;text-align: left ">-->
      <!--<el-button class="el-icon-search" type="primary" @click="searchData('queryShowForm')">查询</el-button>-->
      <!--<el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>-->
    <!--</div>-->

    <!--<el-table-->
      <!--:data="dictData"-->
      <!--style="width: 100%"-->
      <!--@selection-change="handleSelectionChange">-->

      <!--<el-table-column-->
        <!--fixed="left"-->
        <!--label="操作"-->
        <!--width="150">-->
        <!--<template slot-scope="scope">-->
          <!--<el-button type="text" size="small" @click="openDialog('edit', scope.row)">编辑</el-button>-->
          <!--<el-button type="text" size="small" @click="delData(scope.row)">删除</el-button>-->
          <!--<el-button type="text" size="small" @click="showData(scope.row)">查看</el-button>-->
        <!--</template>-->
      <!--</el-table-column>-->

      <!--<el-table-column prop="category_id" label="代码类别key" align="left" width="180"></el-table-column>-->
      <!--<el-table-column prop="category_name" label="代码类别value" align="left" width="180"></el-table-column>-->
      <!--<el-table-column prop="category_sql" label="代码类别sql" align="left"></el-table-column>-->
    <!--</el-table>-->
    <!--<el-pagination style="margin-top: 10px; text-align: right;"-->
                   <!--:current-page="currentPage"-->
                   <!--@size-change="handleSizeChange"-->
                   <!--@current-change="handleCurrentChange"-->
                   <!--:page-sizes="[10, 25, 50, 100]"-->
                   <!--:page-size="pageSize"-->
                   <!--layout="total, sizes, prev, pager, next, jumper"-->
                   <!--:total="total">-->
    <!--</el-pagination>-->

    <el-dialog :title="dialogTitle" :visible.sync="dictDialogVisible">
      <el-form :model="dictDialogForm" :rules="rules" ref="dictDialogForm" style="text-align: left">
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
        <div>
          <el-form-item label=" " :label-width="formLabelWidth">
            <el-button type="primary" @click="submitForm('dictDialogForm')" size="large">保 存</el-button>
            <el-button @click="dictDialogVisible = false" size="large">取 消</el-button>
          </el-form-item>
        </div>

      </el-form>
    </el-dialog>

    <el-dialog :title="infoDialogTitle" :visible.sync="infoDialogVisible" style="text-align: left">
      <el-form :model="dictInfoDialogForm" :rules="infoRules" ref="dictInfoDialogForm">
        <el-form-item label="代码类别key:" :label-width="formLabelWidth" prop="category_id">
          <el-input v-model="dictInfoDialogForm.category_id" auto-complete="off" :disabled="true" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="代码key:" :label-width="formLabelWidth" prop="code_key">
          <el-input v-model="dictInfoDialogForm.code_key" auto-complete="off" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="代码value:" :label-width="formLabelWidth" prop="code_value">
          <el-input v-model="dictInfoDialogForm.code_value" auto-complete="off" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="顺序:" :label-width="formLabelWidth" prop="onum">
          <el-input v-model="dictInfoDialogForm.onum" auto-complete="off" :maxlength=3></el-input>
        </el-form-item>
        <el-form-item label="描述信息:" :label-width="formLabelWidth" prop="info">
          <el-input type="textarea" v-model="dictInfoDialogForm.info"  :maxlength=200></el-input>
        </el-form-item>
        <div>
          <el-form-item label=" " :label-width="formLabelWidth">
            <el-button type="primary" @click="infoSubmitForm('dictInfoDialogForm')" size="large">保 存</el-button>
            <el-button @click="infoDialogVisible = false" size="large">取 消</el-button>
          </el-form-item>
        </div>

      </el-form>
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
        if (this.dialogType === 'edit' && this.selectRow.category_id === value) {
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
        infoDialogTitle: '',
        dictDialogVisible: false,
        infoDialogVisible: false,
        formLabelWidth: '130px',
        dialogType: '',
        infoDialogType: '',
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
        dictInfoData: [],
        dictInfoShow: false,
        selectedRows: [],
        selectRow: {},
        showDictInfoRow: {},
        dictDialogForm: this.initDialogForm(),
        dictInfoDialogForm: {
          code_id: '',
          category_id: '',
          code_key: '',
          code_value: '',
          onum: '',
          info: ''
        },
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
        },
        infoRules: {
          category_id: [
            { required: true, message: '请输入代码类别key', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          code_key: [
            { required: true, message: '请输入代码key', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          code_value: [
            { required: true, message: '请输入代码value', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          onum: [
            { pattern: /^[1-9]\d{0,2}$/, message: '顺序最多只能输入3位数字' }
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
      initInfoDialogForm () {
        return {
          category_id: '',
          code_key: '',
          code_value: '',
          onum: '',
          info: ''
        }
      },
      searchData (formName) {
        this.dictInfoShow = false
        // this.$refs[formName].validate((valid) => {
        //   if (valid) {
        Object.assign(this.queryForm, this.queryShowForm)
        this.getData()
        //   }
        // })
      },
      getData () {
        let self = this
        let paramsObj = {
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        this.dictInfoShow = false
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
                self.$message.success('添加成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      addInfoData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this
            let paramsObj = this.dictInfoDialogForm
            ajax.post({
              url: '/codedict/code/add',
              param: paramsObj,
              success: function (data) {
                self.handleSelectionChange(self.showDictInfoRow)
                self.$message.success('添加成功')
                self.infoDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      delData (data) {
        let self = this
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
              self.$message.success('删除成功')
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
      delInfoData (data) {
        let self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/codedict/code/del',
            param: {
              codeId: data.code_id
            },
            success: function (data) {
              self.$message.success('删除成功')
              self.handleSelectionChange(self.showDictInfoRow)
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
                self.$message.success('编辑成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      updInfoData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this
            let paramsObj = this.dictInfoDialogForm
            ajax.post({
              url: '/codedict/code/update',
              param: paramsObj,
              success: function (data) {
                self.handleSelectionChange(self.showDictInfoRow)
                self.$message.success('编辑成功')
                self.infoDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      showData (row) {
        this.$router.push({name: 'codeDictInfo', query: { category_id: row.category_id }})
      },
      submitForm (formName) {
        if (this.dialogType === 'add') {
          this.addData(formName)
        } else if (this.dialogType === 'edit') {
          this.updData(formName)
        }
      },
      infoSubmitForm (formName) {
        if (this.infoDialogType === 'add') {
          this.addInfoData(formName)
        } else if (this.infoDialogType === 'edit') {
          this.updInfoData(formName)
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
      handleSelectionChange (row) {
        if (row == null) {
          this.dictInfoShow = false
          return
        }
        this.dictInfoShow = true
        this.showDictInfoRow = row
        let self = this
        let paramsObj = {
          categoryId: this.showDictInfoRow.category_id,
          pageindex: 1,
          pagesize: 2147483647
        }
        ajax.post({
          url: '/codedict/category/codelist',
          param: paramsObj,
          success: function (data) {
            if (data.page) {
              self.dictInfoData = data.page.list
            }
          }
        })
      },
      openDialog (dialogType, row) {
        this.dialogType = dialogType
        if (dialogType === 'edit') {
          this.selectRow = row
          this.dialogTitle = '编辑代码类别'
          // 拷贝而不是赋值
          Object.assign(this.dictDialogForm, row)
        } else if (dialogType === 'add') {
          this.dialogTitle = '新建代码类别'
          this.dictDialogForm = this.initDialogForm()
        }
        this.dictDialogVisible = true
        if (this.$refs['dictDialogForm']) {
          this.$refs['dictDialogForm'].clearValidate()
        }
      },
      openInfoDialog (dialogType, row) {
        this.infoDialogType = dialogType
        if (dialogType === 'edit') {
          this.infoDialogTitle = '编辑字典信息'
          // 拷贝而不是赋值
          Object.assign(this.dictInfoDialogForm, row)
        } else if (dialogType === 'add') {
          this.infoDialogTitle = '新建字典信息'
          this.dictInfoDialogForm = this.initInfoDialogForm()
          this.dictInfoDialogForm.category_id = this.showDictInfoRow.category_id
        }
        this.infoDialogVisible = true
        if (this.$refs['dictInfoDialogForm']) {
          this.$refs['dictInfoDialogForm'].clearValidate()
        }
      },
    }
  }
</script>

<style>

</style>
