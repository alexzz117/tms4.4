<template>
  <div>
    <transition name="fade">

      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
               :inline="true" style="text-align: left" v-show="queryFormShow" >
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
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>
      </el-form>

    </transition>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-view" type="primary" @click="queryFormShow = !queryFormShow">查询</el-button>
      <el-button plain class="el-icon-view" :disabled="notSelectOne" @click="openDialog('view')">查看</el-button>
      <el-button plain class="el-icon-plus" :disabled="isExpand" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" :disabled="notSelectOne || isExpand" @click="openDialog('edit')">编辑</el-button>
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
      <el-form :model="dialogForm" ref="dialogForm" style="text-align: left" :inline="true" :rules="dialogFormRules">
        <el-form-item label="统计描述:" :label-width="formLabelWidth" prop="stat_desc" :style="formItemStyle" v-show="formStatDescShow">
          <el-input v-model="dialogForm.stat_desc" auto-complete="off" :maxlength=200 :style="formItemContentStyle" :disabled="viewDisabled"></el-input>
        </el-form-item>

        <el-form-item label="统计引用对象:" :label-width="formLabelWidth" prop="stat_param" :style="formItemStyle" v-show="formStatParamShow">
          <AllPickSelect :dataList="statParamList" @dataChange="addStatParamDataChange" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled"></AllPickSelect>
        </el-form-item>

        <el-form-item label="统计函数:" :label-width="formLabelWidth" prop="stat_fn" :style="formItemStyle" v-show="formStatFnShow">
          <el-select v-model="dialogForm.stat_fn" placeholder="请选择" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled"
                     clearable>
            <el-option
              v-for="item in statFnList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item :label="formStatCondInName" :class="{'is-required':dialogForm.stat_fn === 'calculat_expressions'}" :label-width="formLabelWidth" prop="stat_cond" :style="formItemStyle" v-show="formStatCondInShow">
          <div @dblclick="statCondInPopup" :disabled="viewDisabled">
            <el-input v-model="dialogForm.stat_cond" auto-complete="off" :style="formItemContentStyle" v-show="false" readonly :disabled="viewDisabled"></el-input>
            <el-input v-model="dialogForm.stat_cond_in" auto-complete="off" :style="formItemContentStyle" readonly :disabled="viewDisabled"></el-input>
          </div>
        </el-form-item>

        <el-form-item label="统计目标:" :label-width="formLabelWidth" prop="stat_datafd" :style="formItemStyle" v-show="formStatDatafdShow">
          <el-select v-model="dialogForm.stat_datafd" placeholder="请选择" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled"
                     clearable>
            <el-option
              v-for="item in statDataFdList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>
          </el-select>
        </el-form-item>


        <el-form-item label="函数参数:" :label-width="formLabelWidth" prop="fn_param" :style="formItemStyle" v-show="formFnParamShow">
          <div @dblclick="fnParamPopup">
            <el-input v-model="dialogForm.fn_param" auto-complete="off" :style="formItemContentStyle" readonly :disabled="viewDisabled"></el-input>
          </div>
        </el-form-item>

        <el-form-item label="单位:" :label-width="formLabelWidth" prop="coununit" :style="formItemStyle" v-show="formCoununitShow">
          <el-input v-model="dialogForm.coununit" auto-complete="off" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled"></el-input>
        </el-form-item>

        <el-form-item label="周期:" :label-width="formLabelWidth" prop="countround" :style="formItemStyle" v-show="formCountroundShow">
          <el-input v-model="dialogForm.countround" auto-complete="off" :style="formItemContentStyle" :disabled="viewDisabled"></el-input>
        </el-form-item>

        <el-form-item label="交易结果:" :label-width="formLabelWidth" prop="result_cond" :style="formItemStyle" v-show="formResultCondShow">
          <el-select v-model="dialogForm.result_cond" placeholder="请选择" :style="formItemContentStyle" :disabled="viewDisabled"
                     clearable>
            <el-option
              v-for="item in resultCondList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="数据类型:" :label-width="formLabelWidth" prop="datatype" :style="formItemStyle" v-show="formDatatypeShow">
          <el-select v-model="dialogForm.datatype" placeholder="请选择" :style="formItemContentStyle" :disabled="viewDisabled"
                     clearable>
            <el-option
              v-for="item in datatypeCodeList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="存储字段:" :label-width="formLabelWidth" prop="storecolumn" :style="formItemStyle" v-show="formStoreColumnShow">
          <el-select v-model="dialogForm.storecolumn" placeholder="请选择" :style="formItemContentStyle" :disabled="viewDisabled"
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
            :disabled="viewDisabled"
            active-color="#13ce66"
            inactive-color="#ff4949">
          </el-switch>
        </el-form-item>

        <el-form-item label="事中:" :label-width="formLabelWidth" prop="stat_unresult" :style="formItemStyle" v-show="formStatUnresultShow">
          <el-switch
            v-model="dialogForm.stat_unresult"
            :disabled="viewDisabled"
            active-color="#13ce66"
            inactive-color="#ff4949">
          </el-switch>
        </el-form-item>

        <el-form-item label="有效性:" :label-width="formLabelWidth" prop="stat_valid" :style="formItemStyle" v-show="formStatValidShow">
            <el-radio v-model="dialogForm.stat_valid" :label=0 :disabled="viewDisabled">停用</el-radio>
            <el-radio v-model="dialogForm.stat_valid" :label=1 :disabled="viewDisabled">启用</el-radio>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" v-show="!viewDisabled">取 消</el-button>
        <el-button type="primary" @click="submitForm('dialogForm')" v-show="!viewDisabled">保 存</el-button>
      </div>
    </el-dialog>

    <!--<el-dialog ref="StatCondDialog" title="条件" :visible.sync="statCondInDialogVisible">-->
      <StatCondPicker ref="StatCondDialog" @closeDialog="closeStatCondInDialog" @valueCallback="statCondInValueCallBack"
                    :statCond="dialogForm.stat_cond" :statCondIn="dialogForm.stat_cond_in" :txnId="txnId"
                    :hideItems="['rule_func', 'ac_func']" >

      </StatCondPicker>
    <!--</el-dialog>-->
      <FuncParamPicker ref="FuncParamPickerDialog" @valueCallback="funcParamValueCallBack"></FuncParamPicker>


  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'

  import AllPickSelect from '@/components/common/AllPickSelect'
  import StatCondPicker from '@/components/common/StatCondPicker'
  import FuncParamPicker from '@/components/common/FuncParamPicker'
  import dictCode from '@/common/dictCode'

  let vm = null
  let _dataTypeClassify = [
    {recap: 'long', type: ['long', 'time', 'datetime']},
    {recap: 'double', type: ['double', 'money']},
    {recap: 'string', type: ['string', 'devid', 'ip', 'userid', 'acc', 'code', 'object']}
  ]

  export default {
    computed: {
      txnIdParent () {
        console.log('stat txnId change')
        return this.txnId
      },
      isVisibilityParent () { return this.isVisibility },
      notSelectOne () {
        return this.selectedRows.length !== 1
      },
      // // 下面是控制表单项显示，隐藏的
      formStatCondInName () {
        if (this.dialogForm.stat_fn === 'calculat_expressions') {
          // if (this.dialogType === 'view') {
          //   return ''
          // } else {
          //   return '表达式:'
          // }
          return '表达式:'
        } else {
          return '统计条件:'
        }
      },
      // tableDataShow 用于表格数据的前台检索
      tableDataShow () {
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
        modelName: '',
        isExpand: false,
        tableData: [],
        allStoreFd: [],
        enableStoreFd: [],
        dialogTitle: '',
        dialogType: '',
        dialogVisible: false,
        statCondInDialogVisible: false,
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
        statCondInDictDialogForm: {},
        // 下面这几条都是下拉框取值用的
        statParamList: [],
        statDataFdList: [],
        statFnList: [],
        statValidList: [],
        resultCondList: [],
        datatypeCodeList: [],
        storecolumnCodeList: [],
        // 控制表单是否显示
        formStatDescShow: true,
        formStatParamShow: true,
        formStatFnShow: true,
        formStatCondInShow: true,
        formStatDatafdShow: true,
        formFnParamShow: true,
        formCoununitShow: true,
        formCountroundShow: true,
        formResultCondShow: true,
        formDatatypeShow: true,
        formStoreColumnShow: true,
        formContinuesShow: true,
        formStatUnresultShow: true,
        formStatValidShow: true,
        modDisabled: false,
        viewDisabled: false,
        dialogFormRules: {
          stat_desc: [
            { required: true, message: '统计描述不能为空' },
            { max: 200, message: '长度在200个字符以内' },
            { validator: check.checkFormZhSpecialCharacter }
          ],
          stat_cond: [
            { validator: this.checkStatCond }
          ],
          stat_fn: [
            { required: true, message: '统计函数不能为空' },
            { validator: this.checkStatFn }
          ]
        },
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
          if (vm.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      },
      isVisibility: {
        handler: (val, oldVal) => {
          if (vm.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      },
      'dialogForm.stat_fn': {
        handler: (val, oldVal) => {
          vm.fnChangeEvent()
          // 周期修改触发
          vm.countroundChangeEvent()
        }
      },
      'dialogForm.coununit': {
        handler: (val, oldVal) => {
          // 周期修改触发
          vm.countroundChangeEvent()
        }
      },
      'dialogForm.stat_datafd': {
        handler: (val, oldVal) => {
          // 周期修改触发
          vm.datafdChangeEvent()
        }
      }
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      },
      renderStatValidFilter (value) {
        return dictCode.rendCode('tms.mgr.rulestatus', value)
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
          fn_param: '',
          stat_datafd: '',
          coununit: '',
          countround: '',
          result_cond: '',
          datatype: '',
          storecolumn: '',
          continues: '',
          stat_unresult: '',
          stat_valid: 0
        }
      },
      handleSelectionChange (rows) {
        this.selectedRows = rows
      },
      reloadData () {
        // getData getStatDataFnSelectData 写在了这一句的回调中，因为需要依赖这个的取值
        this.getStatParamSelectData()
        this.statValidList = dictCode.getCodeItems('tms.mgr.rulestatus')
        this.resultCondList = dictCode.getCodeItems('tms.stat.txnstatus')
        this.datatypeCodeList = dictCode.getCodeItems('tms.stat.datatype')
        // this.getStatDataFnSelectData()
        // this.getStatDataValidSelectData()
        // this.getData()
      },
      getData () {
        let self = this
        let paramsObj = {
          txnId: this.txnIdParent
        }
        ajax.post({
          url: '/stat/list',
          param: paramsObj,
          success: function (data) {
            if (data.row) {
              self.tableData = data.row
              self.allStoreFd = data.allstorefd
              self.enableStoreFd = data.enablestorefd
            }
          }
        })
      },
      getStatParamSelectData () {
        let self = this
        ajax.post({
          url: '/stat/txnFeature',
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
                url: '/stat/code',
                model: ajax.model.manager,
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
      dialogOpenHandle () {
        this.formDatatypeShow = false

        this.fnChangeEventCommon()

        this.countroundChangeEvent()

        if (this.dialogType !== 'add') {

        }
      },
      openDialog (dialogType) {
        this.dialogType = dialogType
        let self = this
        if (dialogType === 'edit') {
          this.dialogTitle = '编辑交易统计'
          let length = this.selectedRows.length
          if (length !== 1) {
            this.$message('请选择一行交易统计信息。')
            return
          }
          this.modDisabled = true
          this.viewDisabled = false
          // 拷贝而不是赋值
          Object.assign(this.dialogForm, this.selectedRows[0])
          setTimeout(function () {
            self.dialogForm.fn_param = self.selectedRows[0].fn_param
          }, 300)
        } else if (dialogType === 'add') {
          this.dialogTitle = '新建交易统计'
          this.modDisabled = false
          this.viewDisabled = false
          this.dialogForm = this.initDialogForm()
        } else if (dialogType === 'view') {
          this.dialogTitle = '查看交易统计'
          let length = this.selectedRows.length
          if (length !== 1) {
            this.$message('请选择一行交易统计信息。')
            return
          }
          this.viewDisabled = true
          this.modDisabled = false
          Object.assign(this.dialogForm, this.selectedRows[0])
        }
        this.dialogVisible = true
        if (this.$refs['dialogForm']) {
          this.$refs['dialogForm'].clearValidate()
        }
        this.dialogOpenHandle()
      },
      getStatDataFnSelectData () {
        // let self = this
      },
      // 函数参数弹窗
      fnParamPopup () {
        console.log('fnParamPopup')
        var objectValue = this.dialogForm.stat_fn
        var statDatafd = this.dialogForm.stat_datafd
        // 区间统计类函数需要修改区间参数
        if (objectValue === 'rang_bin_dist') {
          var fnParam = this.dialogForm.fn_param
          var paramType = ''
          var oneFeature = this.queryTxnFeature(statDatafd)
          if (oneFeature) {
            paramType = oneFeature.type
          }
          this.$refs.FuncParamPickerDialog.open(fnParam, paramType)
          // params.initParamList(fnParam, paramType)
        }
      },
      statCondInPopup () {
        if (!this.viewDisabled) {
          this.$refs.StatCondDialog.open()
          this.$refs.StatCondDialog.setValue({
            stat_cond_value: this.dialogForm.stat_cond,
            stat_cond_in: this.dialogForm.stat_cond_in
          })
          this.statCondInDialogVisible = true
        }
      },
      closeStatCondInDialog () {
        this.statCondInDialogVisible = false
      },
      // 子组件的回调
      statParamDataChange (value) {
        this.queryShowForm.stat_param = value
      },
      addStatParamDataChange (value) {
        this.dialogForm.stat_param = value
      },
      statCondInValueCallBack (value) {
        this.dialogForm.stat_cond = value.stat_cond_value
        this.dialogForm.stat_cond_in = value.stat_cond_in
      },
      funcParamValueCallBack (value) {
        this.dialogForm.fn_param = value
      },
      // 下面是这一页的工具函数
      queryTxnFeature (fd) {
        for (let loopObj of this.statDataFdList) {
          if (loopObj.code_key === fd) {
            return loopObj
          }
        }
        return null
      },
      changeFeatureTypeToDataType (type) {
        var _type = type
        for (let loop of _dataTypeClassify) {
          if (loop.type.includes(type)) {
            _type = loop.recap
            break
          }
        }
        return _type
      },
      getStorageFdByfdName (storeFds, storeFdName) {
        var _storeFd = null
        if (storeFds && storeFdName) {
          for (let loop of storeFds) {
            if (loop.fd_name === storeFdName) {
              _storeFd = loop
              break
            }
          }
        }
        return _storeFd
      },
      getCanUseStorageFdByDataType (datatype, effect) {
        var sfdItems = []
        var enableStoreFds = this.enableStoreFd
        if (!effect) {
          var srows = this.selectedRows
          if (srows && srows.length > 0) {
            var srow = srows[0];
            if (srow && srow.storecolumn) {
              var allStoreFds = this.allStoreFd
              var storeFd = this.getStorageFdByfdName(allStoreFds, srow.storecolumn)
              if (storeFd) {
                enableStoreFds.splice(0, 0, storeFd)
              }
            }
          }
        }
        sfdItems = sfdItems.concat(this.getEnableStorageFdByDataType(datatype, enableStoreFds))
        return sfdItems
      },
      // 根据数据类型获取可用存储字段
      getEnableStorageFdByDataType (datatype, enableStoreFds) {
        // debugger
        var sfdItems = []
        if (datatype && enableStoreFds) {
          for (let fd of enableStoreFds) {
            for (let dt of _dataTypeClassify) {
              if (dt.type.includes(datatype)) {
                if (dt.recap === 'long' || dt.recap === 'double') {
                  if (datatype !== 'double') {
                    if (fd.type === datatype) {
                      sfdItems.push({code_value: fd.fd_name, code_key: fd.fd_name})
                    }
                  }
                  if (fd.type === 'double') {
                    sfdItems.push({code_value: fd.fd_name, code_key: fd.fd_name})
                  }
                } else if (dt.recap === 'string') {
                  if (datatype !== 'string') {
                    if (fd.type === datatype) {
                      sfdItems.push({code_value: fd.fd_name, code_key: fd.fd_name})
                    }
                  }
                }
                if (fd.type === 'string') {
                  sfdItems.push({code_value: fd.fd_name, code_key: fd.fd_name})
                }
              }
            }
          }
        }
        return sfdItems
      },
      // 统计函数变化触发
      changeDialogForm (type) {
        switch (type) {
          case 'calculat_expressions':// 计算表达式
            this.dialogForm.stat_param = ''
            this.dialogForm.stat_datafd = ''
            this.dialogForm.coununit = ''
            this.dialogForm.countround = ''
            this.dialogForm.fn_param = ''
            this.dialogForm.result_cond = ''
            this.dialogForm.continues = ''
            this.dialogForm.stat_unresult = ''
            this.formStatParamShow = false
            this.formStatDatafdShow = false
            this.formCoununitShow = false
            this.formCountroundShow = false
            this.formFnParamShow = false
            this.formResultCondShow = false
            this.formContinuesShow = false
            this.formStatUnresultShow = false
            break
          default:
            this.formStatParamShow = true
            this.formStatDatafdShow = true
            this.formCoununitShow = true
            this.formCountroundShow = true
            this.formFnParamShow = true
            this.formResultCondShow = true
            this.formContinuesShow = true
            this.formStatUnresultShow = true
            break
        }
      },
      // 统计函数修改与弹出窗口共用的调整表单样式的方法
      fnChangeEventCommon () {
        let val = this.dialogForm
        if (val.stat_fn === 'calculat_expressions') { // 计数表达式函数
          this.changeDialogForm('calculat_expressions')
          this.formDatatypeShow = true
        } else {
          this.changeDialogForm()
          if (val.stat_fn === 'rang_bin_dist') { // 区间函数
            let statDatafd = val.stat_datafd
            let dataType = ''
            if (statDatafd != undefined && statDatafd !== '') {
              let txnFeature = vm.queryTxnFeature(statDatafd)
              if (txnFeature) {
                dataType = txnFeature.type
              }

              if (dataType === 'datetime' || dataType === 'time' || dataType === 'double' || dataType === 'money' || dataType === 'long') {
                this.formFnParamShow = true
              } else {
                this.dialogForm.fn_param = ''
                this.formFnParamShow = false
              }
            } else {
              // 隐藏表单的函数参数
              this.dialogForm.fn_param = ''
              this.formFnParamShow = false
            }
          } else {
            if (val.stat_fn === 'count' || val.stat_fn === 'status') {
              // 隐藏表单的统计目标
              this.dialogForm.stat_datafd = ''
              this.formStatDatafdShow = false
            } else {
              // 显示表单的函数参数
              this.formStatDatafdShow = true
            }
            // 隐藏表单的函数参数
            this.dialogForm.fn_param = ''
            this.formFnParamShow = false
          }
        }
      },
      fnChangeEvent () {
        let val = this.dialogForm
        val.storecolumn = ''// 存储字段清空

        this.fnChangeEventCommon()

        let type = '' // 数据类型
        if (val.stat_fn === 'snapshot') {
          // 快照数据类型取统计字段数据类型
          var fd = val.stat_datafd
          var oneFeature = this.queryTxnFeature(fd)
          if (oneFeature) {
            type = this.changeFeatureTypeToDataType(oneFeature.type)
          }
        } else {
          for (let loop of this.statFnList) {
            if (loop.code_key === val.stat_fn) {
              type = loop.func_type
            }
          }
        }
        if (type === 'object' && val.stat_fn === 'calculat_expressions') {
          // 显示数据类型字段
          val.datatype = ''
          val.storecolumn = ''
          val.datatype = ''
          val.storecolumn = ''
          this.formDatatypeShow = true
          type = ''
        } else {
          val.datatype = type
          this.formDatatypeShow = false
          // 给数据类型字段赋值

          this.storecolumnCodeList = this.getCanUseStorageFdByDataType(type, true)
        }
      },
      // 周期修改触发
      countroundChangeEvent () {
        let val = this.dialogForm
        if (val.coununit === '7' || val.coununit === '9' || val.stat_fn === 'calculat_expressions') { // 会话，永久不需要周期
          // 隐藏表单的函数参数
          this.dialogForm.countround = ''
          this.formCountroundShow = false
        } else {
          // 显示表单的函数参数
          this.formCountroundShow = true
        }
      },
      // 统计目标变化响应
      datafdChangeEvent () {
        let val = this.dialogForm
        val.storecolumn = ''// 存储字段清空

        var fn = val.stat_fn
        var fd = val.stat_datafd
        var dataType = ''
        var oneFeature = this.queryTxnFeature(fd)
        if (oneFeature) {
          dataType = oneFeature.type
        }
        // 快照，通过统计目标给数据类型赋值
        if (fn === 'snapshot') {
          var type = this.changeFeatureTypeToDataType(dataType)
          val.datatype = type
          // 根据函数类型过滤存储字段
          this.storecolumnCodeList = this.getCanUseStorageFdByDataType(type, true)
        }
        if (fn === 'rang_bin_dist') { // 区间函数
          if ((dataType === 'datetime' || dataType === 'time' || dataType === 'double' || dataType === 'money' || dataType === 'long')) {
            // 显示表单的函数参数
            val.fn_param = ''
            this.formFnParamShow = true
          } else {
            if (fd !== '') {
              this.$message('非时间类型、日期时间类型和非数值类型的统计目标，不能使用区间分布函数')
              val.stat_datafd = ''
            }
            val.stat_datafd = ''
            val.fn_param = ''
            this.formFnParamShow = false
          }
        } else {
          // 隐藏表单的函数参数
          val.fn_param = ''
          this.formFnParamShow = false
        }
      },
      // 下面是校验函数
      checkStatCond (rule, value, callback) {
        let statCond = this.dialogForm.stat_cond
        let name = this.formStatCondInName.replace(':', '')
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if (isCaExFunc) {
          if (statCond === '') {
            return callback(new Error(`${name}不能为空`))
          }
        }
        if (statCond !== '') {
          if (statCond.length > 512) {
            return callback(new Error(`${name}不能超过512个字符`))
          }
          // 之前的代码这个校验放在后台了
          // if(!check.checkeCondSpecialCode(statCond)){
          //   alert(cond_caption+'不能包含特殊字符');
          //   return false;
          // }
        }
      },
      checkStatFn (rule, value, callback) {
        let statFn = this.dialogForm.stat_fn
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if ((!isCaExFunc && statFn !== 'count' && statFn !== 'status') && this.dialogForm.stat_datafd === '') {
          return callback(new Error(`统计函数为：非"状态"、非"计数"时，统计目标不能为空`))
        }
      }
    },
    components: {
      AllPickSelect,
      StatCondPicker,
      FuncParamPicker
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
