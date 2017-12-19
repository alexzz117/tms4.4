<template>
  <div>
    <transition name="fade">

      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
               :inline="true" style="text-align: left" v-show="queryFormShow">
        <el-form-item label="统计名称:" prop="stat_name">
          <el-input v-model="queryShowForm.stat_name" class="query-form-item" auto-complete="off" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="统计描述:" prop="stat_desc">
          <el-input v-model="queryShowForm.stat_desc" class="query-form-item" auto-complete="off" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="统计引用对象:" prop="stat_param">

          <AllPickSelect :dataList="statParamList" @dataChange="statParamDataChange"></AllPickSelect>

        </el-form-item>
        <el-form-item label="统计目标:" prop="stat_datafd">

          <el-select v-model="queryShowForm.stat_datafd" class="query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in statDataFdList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>

        </el-form-item>
        <el-form-item label="统计函数:" prop="stat_fn">
          <el-select v-model="queryShowForm.stat_fn" class="query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in statFnList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>
        <el-form-item label="有效性:" prop="stat_valid">
          <el-select v-model="queryShowForm.stat_valid" class="query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in statValidList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>
      </el-form>

    </transition>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-view" type="primary" @click="queryFormShow = !queryFormShow">查询</el-button>
      <el-button plain class="el-icon-view" :disabled="notSelectOne" @click="showData">查看</el-button>
      <el-button plain class="el-icon-plus" :disabled="isExpand" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" :disabled="notSelectOne || isExpand">编辑</el-button>
      <el-button plain class="el-icon-delete" :disabled="notSelectOne || isExpand">删除</el-button>
      <el-button plain class="el-icon-circle-check" :disabled="notSelectOne || isExpand">启用</el-button>
      <el-button plain class="el-icon-remove" :disabled="notSelectOne || isExpand">停用</el-button>
      <el-button plain class="el-icon-share" :disabled="notSelectOne || isExpand">引用点</el-button>
    </div>

    <div class="stat-expand-table-box">
      <el-table

        ref="dataTable"
        :data="tableDataShow"
        stripe
        border
        style="width: 100%"
        @expand-change="handleExpandChange"
        @selection-change="handleSelectionChange">

        <el-table-column type="selection" width="55" align="left" :selectable="rowSelectable"></el-table-column>
        <el-table-column prop="stat_name" label="统计名称" align="left" width="80"></el-table-column>
        <el-table-column prop="stat_desc" label="统计描述" align="left" width="250"></el-table-column>
        <el-table-column label="统计引用对象" align="left">
          <template slot-scope="scope">
            <span>{{scope.row.stat_param | statParamFilter}}</span>
          </template>
        </el-table-column>

        <el-table-column prop="stat_datafd" label="统计目标" align="left">
          <template slot-scope="scope">
            <span>{{scope.row.stat_datafd | statDataFdFilter}}</span>
          </template>
        </el-table-column>
        <el-table-column label="统计函数" align="left">
          <template slot-scope="scope">
            <span>{{scope.row.stat_fn | statFnFilter}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="storecolumn" label="存储字段" align="left"></el-table-column>
        <el-table-column label="有效性" align="left" width="60">
          <template slot-scope="scope">
            <span>{{scope.row.stat_valid | renderStatValidFilter}}</span>
          </template>
        </el-table-column>
      </el-table>

    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px">
      <el-form :model="dialogForm" ref="dialogForm" style="text-align: left" :inline="true">
        <el-form-item label="统计描述:" :label-width="formLabelWidth" prop="stat_desc" :style="formItemStyle" v-show="formStatDescShow">
          <el-input v-model="dialogForm.stat_desc" auto-complete="off" :maxlength=50 :style="formItemContentStyle"></el-input>
        </el-form-item>

        <el-form-item label="统计引用对象:" :label-width="formLabelWidth" prop="stat_param" :style="formItemStyle" v-show="formStatParamShow">
          <AllPickSelect :dataList="statParamList" @dataChange="addStatParamDataChange" :style="formItemContentStyle"></AllPickSelect>
        </el-form-item>

        <el-form-item label="统计函数:" :label-width="formLabelWidth" prop="stat_fn" :style="formItemStyle" v-show="formStatFnShow">
          <el-select v-model="dialogForm.stat_fn" placeholder="请选择" :style="formItemContentStyle"
                     clearable>
            <el-option
              v-for="item in statFnList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <!--惊了 这个东西-->
        <el-form-item label="统计条件:" :label-width="formLabelWidth" prop="stat_desc" :style="formItemStyle" v-show="formStatCondInShow">
          <el-input v-model="dialogForm.stat_cond" auto-complete="off" :style="formItemContentStyle" v-show="false" readonly></el-input>
          <el-input v-model="dialogForm.stat_cond_in" auto-complete="off" :style="formItemContentStyle" readonly></el-input>
        </el-form-item>

        <el-form-item label="统计目标:" :label-width="formLabelWidth" prop="stat_datafd" :style="formItemStyle" v-show="formStatDatafdShow">
          <el-select v-model="dialogForm.stat_datafd" placeholder="请选择" :style="formItemContentStyle"
                     clearable>
            <el-option
              v-for="item in statDataFdList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="单位:" :label-width="formLabelWidth" prop="coununit" :style="formItemStyle" v-show="formCoununitShow">
          <el-input v-model="dialogForm.coununit" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>

        <el-form-item label="周期:" :label-width="formLabelWidth" prop="countround" :style="formItemStyle" v-show="formCountroundShow">
          <el-input v-model="dialogForm.countround" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>

        <el-form-item label="交易结果:" :label-width="formLabelWidth" prop="result_cond" :style="formItemStyle" v-show="formResultCondShow">
          <el-select v-model="dialogForm.result_cond" placeholder="请选择" :style="formItemContentStyle"
                     clearable>
            <el-option
              v-for="item in resultCondList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="数据类型:" :label-width="formLabelWidth" prop="datatype" :style="formItemStyle" v-show="formDatatypeShow">
          <el-select v-model="dialogForm.datatype" placeholder="请选择" :style="formItemContentStyle"
                     clearable>
            <el-option
              v-for="item in datatypeCodeList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="存储字段:" :label-width="formLabelWidth" prop="storecolumn" :style="formItemStyle" v-show="formStoreColumnShow">
          <el-select v-model="dialogForm.storecolumn" placeholder="请选择" :style="formItemContentStyle"
                     clearable>
            <el-option
              v-for="item in storecolumnCodeList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="连续:" :label-width="formLabelWidth" prop="continues" :style="formItemStyle" v-show="formContinuesShow">
          <el-switch
            v-model="dialogForm.continues"
            active-color="#13ce66"
            inactive-color="#ff4949">
          </el-switch>
        </el-form-item>

        <el-form-item label="事中:" :label-width="formLabelWidth" prop="stat_unresult" :style="formItemStyle" v-show="formStatUnresultShow">
          <el-switch
            v-model="dialogForm.stat_unresult"
            active-color="#13ce66"
            inactive-color="#ff4949">
          </el-switch>
        </el-form-item>

        <el-form-item label="有效性:" :label-width="formLabelWidth" prop="stat_valid" :style="formItemStyle" v-show="formStatValidShow">
            <el-radio v-model="dialogForm.stat_valid" label="0">停用</el-radio>
            <el-radio v-model="dialogForm.stat_valid" label="1">启用</el-radio>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('dialogForm')">保 存</el-button>
      </div>
    </el-dialog>


  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'

  import AllPickSelect from '@/components/common/AllPickSelect'

  let vm = null

  export default {
    computed: {
      notSelectOne () {
        return this.selectedRows.length !== 1
      },
      // 下面是控制表单项显示，隐藏的
      formStatDescShow () {
        return true
      },
      formStatParamShow () {
        return true
      },
      formStatFnShow () {
        return true
      },
      formStatCondInShow () {
        return true
      },
      formStatDatafdShow () {
        return true
      },
      formCoununitShow () {
        return true
      },
      formCountroundShow () {
        return true
      },
      formResultCondShow () {
        return true
      },
      formDatatypeShow () {
        return this.dialogForm.stat_fn === 'calculat_expressions'
      },
      formStoreColumnShow () {
        return true
      },
      formContinuesShow () {
        return true
      },
      formStatUnresultShow () {
        return true
      },
      formStatValidShow () {
        return true
      },
      // tableDataShow 用于表格数据的前台检索
      tableDataShow () {
        console.log('tableDataShow')
        let statName = this.queryShowForm.stat_name
        let statDesc = this.queryShowForm.stat_desc
        let statParamList = this.queryShowForm.stat_param
        let statFd = this.queryShowForm.stat_datafd
        let statFn = this.queryShowForm.stat_fn
        let statValid = this.queryShowForm.stat_valid

        let showData = this.tableData.filter((x) => {
          // 统计名称模糊查询
          if (statName !== '' && !x.stat_name.includes(statName)) {
            console.log('stat_name')
            return false
          }
          // 统计描述模糊查询
          if (statDesc !== '' && !x.stat_desc.includes(statDesc)) {
            console.log('stat_desc')
            return false
          }
          // 统计引用对象
          if (statParamList != null && statParamList.length > 0) {
            let containStatParam = false
            for (let i = 0; i < statParamList.length; i++) {
              if (x.stat_param === statParamList[i]) {
                containStatParam = true
                break
              }
            }
            if (!containStatParam) {
              console.log('containStatParam')
              return false
            }
          }
          // 统计目标
          if (statFd != null && statFd !== '' && x.stat_datafd !== statFd) {
            return false
          }
          // 统计目标
          if (statFn != null && statFn !== '' && x.stat_fn !== statFn) {
            return false
          }
          // 有效性  下面这个!= 不能改成!==
          if (statValid != null && statValid !== '' && x.stat_valid != statValid) {
            return false
          }

          return true
        })
        return showData
      }
    },
    data () {
      return {
        // txnId: '123',
        txnIdParent: this.txnId,
        isVisibilityParent: this.isVisibility,
        modelName: '',
        isExpand: false,
        tableData: [],
        allStoreFd: [],
        enableStoreFd: [],
        dialogTitle: '',
        dialogVisible: false,
        formLabelWidth: '120px',
        formItemStyle: {
          width: '400px'
        },
        formItemContentStyle: {
          width: '250px'
        },
        queryFormShow: false,
        queryShowForm: {
          stat_name: '',
          stat_desc: '',
          stat_param: '',
          stat_datafd: '',
          stat_fn: '',
          stat_valid: ''
        },
        dialogForm: this.initDialogForm(),
        // 下面这几条都是下拉框取值用的
        statParamList: [],
        statDataFdList: [],
        statFnList: [],
        statValidList: [],
        resultCondList: [],
        datatypeCodeList: [],
        storecolumnCodeList: [],
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
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectedRows: []
      }
    },
    props: ['txnId', 'isVisibility'],
    mounted: function () {
      this.$nextTick(function () {
        vm = this
        // this.reloadData()
      })
    },
    watch: {
      txnId: {
        handler: (val, oldVal) => {
          this.txnIdParent = val
          if (this.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      },
      isVisibility: {
        handler: (val, oldVal) => {
          this.isVisibilityParent = val
          if (this.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      }
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      },
      renderStatValidFilter (value) {
        // TODO 远程获取渲染
        if (value === 0) {
          return '停用'
        } else if (value === 1) {
          return '启用'
        } else {
          return ''
        }
      },
      statParamFilter (v) {
        if (v === null || v === '') {
          return ''
        }
        let returnTextArr = []
        let arr = v.split(',')

        for (let value of arr) {
          for (let i = 0; i < vm.statParamList.length; i++) {
            let row = vm.statParamList[i]
            if (value !== '' && value === row.code_key) {
              returnTextArr.push(row.code_value)
              break
            }
          }
        }
        return returnTextArr.join(',')
      },
      statDataFdFilter (v) {
        if (v === null || v === '') {
          return ''
        }
        let returnTextArr = []
        let arr = v.split(',')

        for (let value of arr) {
          for (let i = 0; i < vm.statDataFdList.length; i++) {
            let row = vm.statDataFdList[i]
            if (value !== '' && value === row.code_key) {
              returnTextArr.push(row.code_value)
              break
            }
          }
        }
        return returnTextArr.join(',')
      },
      statFnFilter (v) {
        if (v === null || v === '') {
          return ''
        }
        let returnTextArr = []
        let arr = v.split(',')

        for (let value of arr) {
          for (let i = 0; i < vm.statFnList.length; i++) {
            let row = vm.statFnList[i]
            if (value !== '' && value === row.code_key) {
              returnTextArr.push(row.code_value)
              break
            }
          }
        }
        return returnTextArr.join(',')
      }
    },
    methods: {
      initDialogForm () {
        return {
          stat_desc: '',
          stat_param: '',
          stat_fn: '',
          stat_cond: '',
          stat_cond_in: '',
          stat_datafd: '',
          coununit: '',
          countround: '',
          result_cond: '',
          datatype: '',
          storecolumn: '',
          continues: '',
          stat_unresult: '',
          stat_valid: '0'
        }
      },
      handleSelectionChange (rows) {
        this.selectedRows = rows
      },
      handleExpandChange (row, expandRows) {
        console.log(row)
        console.log(expandRows)
      },
      reloadData () {
        // getData getStatDataFnSelectData 写在了这一句的回调中，因为需要依赖这个的取值
        this.getStatParamSelectData()
        // this.getStatDataFnSelectData()
        this.getStatDataValidSelectData()
        // this.getData()
      },
      getData () {
        let self = this
        let paramsObj = {
          txnId: this.txnIdParent
        }
        ajax.post({
          url: '/manager/stat/list',
          param: paramsObj,
          success: function (data) {
            if (data.row) {
              self.tableData = data.row
              self.allStoreFd = data.allStoreFd
              self.enableStoreFd = data.enableStoreFd
            }
          }
        })
      },
      getStatParamSelectData () {
        let self = this
        ajax.post({
          url: '/manager/stat/txnFeature',
          param: {txn_id: this.txnIdParent},
          success: function (data) {
            if (data.row) {
              self.statParamList = []
              self.statDataFdList = []
              for (let value of data.row) {
                self.statParamList.push(value)
                self.statDataFdList.push(value)
              }
              ajax.post({
                url: '/manager/stat/code',
                param: {
                  category_id: 'tms.pub.func',
                  args: 2
                },
                success: function (data) {
                  if (data.row) {
                    self.statFnList = []
                    for (let value of data.row) {
                      self.statFnList.push(value)
                    }
                    self.getData()
                  }
                }
              })
            }
          }
        })
      },
      rowSelectable () {
        return !this.isExpand
      },
      showData () {
        let row = this.selectedRows[0]
        let length = this.selectedRows.length
        if (length !== 1) {
          this.$message('请选择一行交易统计信息。')
          return
        }
        this.$refs.dataTable.toggleRowExpansion(row)
        this.isExpand = !this.isExpand
      },
      openDialog (dialogType) {
        this.dialogType = dialogType
        if (dialogType === 'edit') {
          this.dialogTitle = '编辑交易统计'
          let length = this.selectedRows.length
          if (length !== 1) {
            this.$message('请选择一行交易统计信息。')
            return
          }
          // 拷贝而不是赋值
          Object.assign(this.dialogForm, this.selectedRows[0])
        } else if (dialogType === 'add') {
          this.dialogTitle = '新建交易统计'
          this.dialogForm = this.initDialogForm()
        }
        this.dialogVisible = true
        if (this.$refs['initDialogForm']) {
          this.$refs['dialogForm'].clearValidate()
        }
      },
      getStatDataFnSelectData () {
        // let self = this
      },
      statParamDataChange (value) {
        this.queryShowForm.stat_param = value
      },
      addStatParamDataChange (value) {
        this.dialogForm.stat_param = value
      }
    },
    components: {
      AllPickSelect
    }
  }
</script>

<style>
  .query-form-item{
    width: 200px;
  }
  .stat-expand-table-box .el-table__expand-column{
    /*visibility: hidden;*/
    width: 0px;
    border-right: none !important;
  }
  .stat-expand-table-box .el-table__expand-icon{
    visibility: hidden;
  }
  .stat-expand-table-box .el-table__body-wrapper{
    overflow: hidden;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }
  .fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
    opacity: 0
  }
</style>
