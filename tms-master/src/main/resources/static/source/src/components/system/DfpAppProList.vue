<template>
  <div>

    <div style="margin-bottom: 10px; text-align: left; height: 30px;">
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>

      <div style="float:right; padding-right: 15px;">
        <el-input v-model="queryShowForm.app_id" placeholder="采集方式名称" class="dfp-app-list-form-item" auto-complete="off" clearable>
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
          <el-table-column prop="app_name" label="采集方式" align="left">
          </el-table-column>
          <el-table-column prop="prop_name" label="属性名称" align="left"></el-table-column>
          <el-table-column prop="weight" label="权重" align="left"></el-table-column>
          <el-table-column prop="storecolumn" label="存储字段" align="left" :formatter="formatter"></el-table-column>
          <el-table-column prop="is_token" label="参与指纹运算" align="left" :formatter="formatter"></el-table-column>
          <el-table-column prop="prop_comment" label="属性说明" align="left" ></el-table-column>
        </el-table>

      </section>

      <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" :close-on-click-modal="false" width="600px">
        <el-form :model="dialogForm" :rules="rules" ref="dialogForm" style="text-align: left;">
          <el-form-item label="采集方式:" :label-width="formLabelWidth" prop="app_id">
            <el-select v-model="dialogForm.app_id" placeholder="请选择"
                       clearable>
              <el-option
                v-for="item in appIdList"
                :key="item.app_id"
                :label="item.app_name"
                :value="item.app_id">
              </el-option>
            </el-select>
            <!--<el-input v-model="dialogForm.app_id" ></el-input>-->
          </el-form-item>
          <!--<el-form-item label="属性ID:" :label-width="formLabelWidth" prop="prop_name">-->
            <!--<el-input v-model="dialogForm.prop_name" ></el-input>-->
          <!--</el-form-item>-->
          <el-form-item label="属性名称:" :label-width="formLabelWidth" prop="prop_id">
            <el-select v-model="dialogForm.prop_id" placeholder="请选择"
                       clearable>
              <el-option
                v-for="item in propIdListShow"
                :key="item.prop_id"
                :label="item.prop_name"
                :value="item.prop_id">
              </el-option>
            </el-select>
            <!--<el-input v-model="dialogForm.prop_id" ></el-input>-->
          </el-form-item>
          <el-form-item label="权重:" :label-width="formLabelWidth" prop="weight">
            <el-input v-model="dialogForm.weight" ></el-input>
          </el-form-item>
          <el-form-item label="存储字段:" :label-width="formLabelWidth" prop="storecolumn">
            <el-select v-model="dialogForm.storecolumn" placeholder="请选择"
                       clearable>
              <el-option
                v-for="item in storeColumnListShow"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <!--<el-input v-model="dialogForm.storecolumn" ></el-input>-->
          </el-form-item>
          <el-form-item label="参与指纹运算:" :label-width="formLabelWidth" prop="is_token">

            <el-select v-model="dialogForm.is_token" placeholder="请选择"
                       clearable>
              <el-option
                v-for="item in isTokenList"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <!--<el-input v-model="dialogForm.is_token" ></el-input>-->
          </el-form-item>
          <el-form-item label="属性说明:" :label-width="formLabelWidth" prop="prop_comment">
            <el-input type="textarea" v-model="dialogForm.prop_comment" :readonly="true"></el-input>
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
        let appId = this.queryShowForm.app_id

        let showData = this.tableData.filter((x) => {
          if (appId !== '' && !x.app_id.includes(appId)) {
            return false
          }

          return true
        })
        return showData
      },
      propIdListShow () {
        let _proList = new Set();
        let _existProList = []
        let appId = this.dialogForm.app_id
        let propId = this.dialogForm.prop_id
        if(!appId) {
          return []
        }
        for(let item of this.tableData) {
          if(item.app_id === appId){
            _existProList.push(item);
          }
        }

        for(let item of this.propIdList) {
          var _prop_id = item.prop_id

          if(_prop_id === propId){
            _proList.add(item);
            // break
          }

          var isExist = false;
          for(let proItem of _existProList){
            if(_prop_id === proItem.prop_id){
              isExist = true;
              break
            }
          }

          if(!isExist){
            _proList.add(item);
          }
        }
        return Array.from(_proList)
        // return this.propIdList
      },

      storeColumnListShow () {
        let appId = this.dialogForm.app_id
        if(!appId) {
          return []
        }

        let storecolumn = this.dialogForm.storecolumn
        // let _proList = []
        var _proList = new Set();
        let _existProList = []

        for(let item of this.tableData) {
          if(item.app_id === appId){
            _existProList.push(item);
          }
        }
        for(let item of this.storeColumnList) {
          var _storecolumn = item.value

          if(_storecolumn === storecolumn){
            _proList.add(item);
            // break
          }

          var isExist = false;
          for(let proItem of _existProList){
            if(_storecolumn === proItem.storecolumn){
              isExist = true;
              break
            }
          }

          if(!isExist){
            _proList.add(item);
          }
        }
        return Array.from(_proList)
        // return this.storeColumnList
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
          app_id: [
            {required: true, message: '请输入采集方式', trigger: 'blur'}
          ],
          prop_id: [
            {required: true, message: '请输入属性名称', trigger: 'blur'}
          ],
          weight: [
            {required: true, message: '请输入权重', trigger: 'blur'},
            { validator: this.weightValid, trigger: 'blur'}
          ],
          storecolumn: [
            {required: true, message: '请输入存储字段', trigger: 'blur'}
          ],
          is_token: [
            {required: true, message: '请输入参与指纹运算', trigger: 'blur'}
          ]
        },
        queryShowForm: {
          app_id: ''
        },
        queryForm: {
          app_id: ''
        },
        formLabelWidth: '150px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectRow: {},
        selectedRows: [],
        isTokenList:[],
        storeColumnList:[],
        appIdList: [],
        propIdList:[]
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.storeColumnList = dictCode.getCodeItems('tms.dfp.storecolumn')
        this.isTokenList = dictCode.getCodeItems('tms.dfp.is_token')
        this.getData()
      })
    },
    watch: {
      'dialogForm.prop_id': function (val) {
        if(val === '') {
          this.dialogForm.prop_name = ''
          this.dialogForm.prop_comment = ''
        }
        for(let item of this.propIdList) {
          if(item.prop_id === val) {
            this.dialogForm.prop_name = item.prop_name
            this.dialogForm.prop_comment = item.prop_comment
            break;
          }
        }

      }
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      }
    },
    methods: {
      handleTabClick(tab, event) {

      },
      weightValid (rule, value, callback) {
        if (!(parseFloat(value) >= 0)) {
          return callback(new Error('权重必须为正数'))
        }
        if((String(Math.round(parseFloat(value)))).length > 8){
          return callback(new Error('权重不能超过8位数'))
        }
        callback()
      },
      formatter(row, column, cellValue) {
        //GRID数据列表值转换
        switch(column.property)
        {
          case 'is_token':
            return dictCode.rendCode('tms.dfp.is_token', cellValue)
            break;
          case 'storecolumn':
            return dictCode.rendCode('tms.dfp.storecolumn', cellValue)
            break;
          default:
            return cellValue
            break;
        }
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
          id: '',
          prop_name: '',
          app_id: '',
          app_name: '',
          prop_id: '',
          weight: '',
          storecolumn: '',
          is_token: '',
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
              url: '/dfp/appProSave',
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
          url: '/dfp/appProList',
          param: paramsObj,
          success: function (data) {
            if (data.row && data.row.length > 0) {
              self.tableData = data.row
              self.appIdList = data.applist
              self.propIdList = data.prolist
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
            url: '/dfp/appProSave',
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
          this.selectRow = {}
          this.dialogForm = this.initDialogForm()
        }
        else if (dialogType === 'edit') {
          this.dialogForm = this.initDialogForm()
          Object.assign(this.dialogForm, row)
          this.selectRow = row
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
