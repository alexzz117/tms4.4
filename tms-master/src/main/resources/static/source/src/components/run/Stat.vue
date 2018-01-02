<template>
  <div>
    <transition name="fade">

      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
               :inline="true" style="text-align: left" v-show="queryFormShow" >
        <el-form-item label="统计名称:" prop="stat_name">
          <el-input v-model="queryShowForm.stat_name" class="stat-query-form-item" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="统计描述:" prop="stat_desc">
          <el-input v-model="queryShowForm.stat_desc" class="stat-query-form-item" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="统计引用对象:" prop="stat_param">

          <AllPickSelect :dataList="statParamList" @dataChange="statParamDataChange"></AllPickSelect>

        </el-form-item>
        <el-form-item label="统计目标:" prop="stat_datafd">

          <el-select v-model="queryShowForm.stat_datafd" class="stat-query-form-item" placeholder="请选择"
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
          <el-select v-model="queryShowForm.stat_fn" class="stat-query-form-item" placeholder="请选择"
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
          <el-select v-model="queryShowForm.stat_valid" class="stat-query-form-item" placeholder="请选择"
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

      <!--<el-button plain class="el-icon-view" :disabled="notSelectOne" @click="openDialog('view')">查看</el-button>-->
      <el-button plain class="el-icon-plus" :disabled="readonlyParent" @click="openDialog('add')">新建</el-button>
      <!--<el-button plain class="el-icon-edit" :disabled="notSelectOne || isExpand" @click="openDialog('edit')">编辑</el-button>-->
      <!--<el-button plain class="el-icon-delete" :disabled="notSelectOne || isExpand" @click="delData">删除</el-button>-->
      <!--<el-button plain class="el-icon-circle-check" :disabled="notSelectOne || isExpand">启用</el-button>-->
      <!--<el-button plain class="el-icon-remove" :disabled="notSelectOne || isExpand">停用</el-button>-->
      <!--<el-button plain class="el-icon-share" :disabled="notSelectOne || isExpand">引用点</el-button>-->
      <div class="stat-query-box">
        <el-input v-model="queryShowForm.stat_desc" placeholder="统计描述" class="stat-query-form-item" auto-complete="off">
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
        <el-button type="primary" @click="queryFormShow = !queryFormShow">更多</el-button>
      </div>
    </div>

    <div class="stat-expand-table-box">
      <el-table

        ref="dataTable"
        :data="tableDataShow"
        style="width: 100%"
        @selection-change="handleSelectionChange">

        <el-table-column
          label="操作"
          width="100">
          <template slot-scope="scope">

            <el-button v-if="!readonlyParent" type="text" size="small" icon="el-icon-edit" title="编辑" @click="openDialog('edit', scope.row)"></el-button>
            <el-button v-if="readonlyParent" type="text" size="small" icon="el-icon-view" title="查看" @click="openDialog('edit', scope.row)"></el-button>
            <el-button type="text" size="small" icon="el-icon-delete" title="删除" @click="delData(scope.row)" :disabled="readonlyParent"></el-button>
            <el-button type="text" size="small" icon="el-icon-location-outline" title="引用点" @click="openRefsDialog(scope.row)"></el-button>

          </template>
        </el-table-column>

        <!--<el-table-column type="selection" width="55" align="left"></el-table-column>-->
        <el-table-column prop="stat_name" label="统计名称" align="left" width="80"></el-table-column>
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
            <el-switch
              v-model="scope.row.stat_valid"
              :active-value=1
              :inactive-value=0
              :disabled="statValidBtnDisabled || readonlyParent"
              @change="statValidChange(scope.row)">
            </el-switch>
          </template>
        </el-table-column>
      </el-table>

    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px">
      <el-form :model="dialogForm" ref="dialogForm" style="text-align: left" :inline="true" :rules="dialogFormRules">
        <el-form-item label="统计描述:" :label-width="formLabelWidth" prop="stat_desc" :style="formItemStyle" v-show="formStatDescShow">
          <el-input v-model="dialogForm.stat_desc" auto-complete="off" :maxlength=200 :style="formItemContentStyle" :disabled="viewDisabled || readonlyParent"></el-input>
        </el-form-item>

        <el-form-item label="统计引用对象:" class="is-required" :label-width="formLabelWidth" prop="stat_param" :style="formItemStyle" v-show="formStatParamShow">
          <AllPickSelect :collapseTags="allPickCollapse" :selectedList="statParamInitList" :dataList="statParamList" @dataChange="addStatParamDataChange" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled || readonlyParent"></AllPickSelect>
        </el-form-item>

        <el-form-item label="统计函数:" :label-width="formLabelWidth" prop="stat_fn" :style="formItemStyle" v-show="formStatFnShow">
          <el-select v-model="dialogForm.stat_fn" placeholder="请选择" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled || readonlyParent"
                     clearable>
            <el-option
              v-for="item in statFnList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item :label="formStatCondInName" :class="{'is-required':StatCondInRequired}" :label-width="formLabelWidth" prop="stat_cond" :style="formItemStyle" v-show="formStatCondInShow">
          <div @dblclick="statCondInPopup" :disabled="viewDisabled">
            <el-input v-model="dialogForm.stat_cond" auto-complete="off" :style="formItemContentStyle" v-show="false" readonly :disabled="viewDisabled || readonlyParent"></el-input>
            <el-input v-model="dialogForm.stat_cond_in" auto-complete="off" :style="formItemContentStyle" readonly :disabled="viewDisabled || readonlyParent"></el-input>
          </div>
        </el-form-item>

        <el-form-item label="统计目标:" class="is-required" :label-width="formLabelWidth" prop="stat_datafd" :style="formItemStyle" v-show="formStatDatafdShow">
          <el-select v-model="dialogForm.stat_datafd" placeholder="请选择" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled || readonlyParent"
                     clearable>
            <el-option
              v-for="item in statDataFdList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="函数参数:" class="is-required" :label-width="formLabelWidth" prop="fn_param" :style="formItemStyle" v-show="formFnParamShow">
          <div @dblclick="fnParamPopup">
            <el-input v-model="dialogForm.fn_param" auto-complete="off" :style="formItemContentStyle" readonly :disabled="viewDisabled || readonlyParent"></el-input>
          </div>
        </el-form-item>

        <el-form-item label="单位:" class="is-required" :label-width="formLabelWidth" prop="coununit" :style="formItemStyle" v-show="formCoununitShow">
          <el-select v-model="dialogForm.coununit" placeholder="请选择" :style="formItemContentStyle" :disabled="modDisabled || viewDisabled || readonlyParent"
                     clearable>
            <el-option
              v-for="item in coununitList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>

        </el-form-item>

        <el-form-item label="周期:" class="is-required" :label-width="formLabelWidth" prop="countround" :style="formItemStyle" v-show="formCountroundShow">
          <el-input v-model="dialogForm.countround" auto-complete="off" :style="formItemContentStyle" :disabled="viewDisabled || readonlyParent"></el-input>
        </el-form-item>

        <el-form-item label="交易结果:" class="is-required" :label-width="formLabelWidth" prop="result_cond" :style="formItemStyle" v-show="formResultCondShow">
          <el-select v-model="dialogForm.result_cond" placeholder="请选择" :style="formItemContentStyle" :disabled="viewDisabled || readonlyParent"
                     clearable>
            <el-option
              v-for="item in resultCondList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="数据类型:" class="is-required" :label-width="formLabelWidth" prop="datatype" :style="formItemStyle" v-show="formDatatypeShow">
          <el-select v-model="dialogForm.datatype" placeholder="请选择" :style="formItemContentStyle" :disabled="viewDisabled || readonlyParent"
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
          <el-select v-model="dialogForm.storecolumn" placeholder="请选择" :style="formItemContentStyle" :disabled="viewDisabled || readonlyParent"
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
            :disabled="viewDisabled || readonlyParent"
            active-value="1"
            inactive-value="0">
          </el-switch>
        </el-form-item>

        <el-form-item label="事中:" :label-width="formLabelWidth" prop="stat_unresult" :style="formItemStyle" v-show="formStatUnresultShow">
          <el-switch
            v-model="dialogForm.stat_unresult"
            :disabled="viewDisabled || readonlyParent"
            active-value="1"
            inactive-value="0">
          </el-switch>
        </el-form-item>

        <el-form-item label="有效性:" :label-width="formLabelWidth" prop="stat_valid" :style="formItemStyle" v-show="formStatValidShow">

          <el-switch
            v-model="dialogForm.stat_valid"
            :disabled="viewDisabled || readonlyParent"
            active-value="1"
            inactive-value="0"
            >
          </el-switch>

        </el-form-item>
        <div>
          <el-form-item  label=" " :label-width="formLabelWidth" :style="formItemStyle">
            <el-button v-if="!readonlyParent" type="primary" @click="submitForm('dialogForm')" v-show="!viewDisabled" :disabled="dialogFormSureBtnDisabled" size="large">保 存</el-button>
            <el-button @click="dialogVisible = false" v-show="!viewDisabled" size="large">取 消</el-button>
          </el-form-item>
        </div>

      </el-form>
    </el-dialog>

    <StatCondPicker ref="StatCondDialog" @valueCallback="statCondInValueCallBack"
                    :statCond="dialogForm.stat_cond" :statCondIn="dialogForm.stat_cond_in" :txnId="txnId"
                    :hideItems="['rule_func', 'ac_func']" >

    </StatCondPicker>
    <FuncParamPicker ref="FuncParamPickerDialog" @valueCallback="funcParamValueCallBack"></FuncParamPicker>

    <el-dialog :title="refsDialogTitle" :visible.sync="refsDialogVisible" width="500px">

      <el-tree :data="refsTreeData" node-key="id" ref="refsTree"
               :props="defaultRefsTreeProps"
               :highlight-current=true
               :expand-on-click-node="false"
               style="overflow-y: auto;">
      </el-tree>

      <div slot="footer" class="dialog-footer">
      </div>
    </el-dialog>

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
      readonlyParent () {
        return this.readonly
      },
      txnIdParent () {
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
      StatCondInRequired () {
        return this.dialogForm.stat_fn === 'calculat_expressions'
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
            return false
          }
          // 统计描述模糊查询
          if (statDesc !== '' && !x.stat_desc.includes(statDesc)) {
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
        refsDialogTitle: '引用点查看',
        dialogType: '',
        refsDialogVisible: false,
        dialogVisible: false,
        refsTreeData: [],
        defaultRefsTreeProps: {
          children: 'children',
          label: 'text'
        },
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
        statParamInitList: [],
        dialogForm: this.initDialogForm(),
        dialogFormSureBtnDisabled: false,
        statValidBtnDisabled: false,
        allPickCollapse: false, // 控制多选是否隐藏多的被选项
        statCondInDictDialogForm: {},
        // 下面这几条都是下拉框取值用的
        statParamList: [],
        statDataFdList: [],
        statFnList: [],
        coununitList: [],
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
            { required: true, message: '统计描述不能为空', trigger: 'blur' },
            { max: 200, message: '长度在200个字符以内', trigger: 'blur' },
            { validator: check.checkFormZhSpecialCharacter, trigger: 'blur' }
          ],
          stat_cond: [
            { validator: this.checkStatCond, trigger: 'blur' }
          ],
          stat_fn: [
            { required: true, message: '统计函数不能为空', trigger: 'change' }
          ],
          stat_param: [
            // { required: true, message: '统计引用对象不能为空', trigger: 'none' }
            { validator: this.checkStatParam, trigger: 'blur' }
          ],
          stat_datafd: [
            { validator: this.checkStatDataFd, trigger: 'change' }
          ],
          fn_param: [
            { validator: this.checkFnParam, trigger: 'change' }
          ],
          coununit: [
            { validator: this.checkCoununit, trigger: 'change' }
          ],
          countround: [
            { validator: this.checkCountround, trigger: 'blur' }
          ],
          result_cond: [
            { validator: this.checkResultCond, trigger: 'change' }
          ],
          datatype: [
            { validator: this.checkDatatype, trigger: 'change' }
          ],
          stat_valid: [
            { validator: this.checkStatValid, trigger: 'change' }
          ]
        },
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectedRows: []
      }
    },
    props: ['txnId', 'isVisibility', 'readonly'],
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
      // 重建一个新的表单对象
      initDialogForm() {
        return {
          stat_desc: '',
          stat_param: '',
          stat_param_list: '',
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
          continues: 0,
          stat_unresult: 0,
          stat_valid: 0
        }
      },
      // 选中列 现在没用了
      handleSelectionChange(rows) {
        this.selectedRows = rows
      },
      // 重新加载数据
      reloadData() {
        this.getStatParamSelectData()
        this.statValidList = dictCode.getCodeItems('tms.mgr.rulestatus')
        this.resultCondList = dictCode.getCodeItems('tms.stat.txnstatus')
        this.datatypeCodeList = dictCode.getCodeItems('tms.stat.datatype')
        this.coununitList = dictCode.getCodeItems('tms.stat.condunit')
      },
      // 加载列表数据
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
      // 获取统计引用对象，统计目标 统计函数 的下拉值
      getStatParamSelectData() {
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
      // 表单弹窗弹出时的处理
      dialogOpenHandle() {
        this.formDatatypeShow = false

        this.fnChangeEventCommon()

        this.countroundChangeEvent()
      },
      // 删除数据
      delData (row) {
        if (this.readonlyParent) {
          return
        }
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let jsonData = {}
          jsonData.del = [row]
          let finalJsonData = {}
          finalJsonData.postData = jsonData
          finalJsonData.txnId = this.txnIdParent
          let self = this

          ajax.post({
            url: '/stat/save',
            param: finalJsonData,
            success: function (data) {
              self.getData()
              self.$message.success('删除成功')
            }
          })
        })
      },
      // 表格中的可用性按钮点击
      statValidChange (row) {
        if (this.readonlyParent) {
          return
        }
        let jsonData = {}
        this.statValidBtnDisabled = true
        jsonData['valid-y'] = [row]
        let finalJsonData = {}
        finalJsonData.postData = jsonData
        finalJsonData.txnId = this.txnIdParent
        let self = this
        let message = '编辑成功'
        if (row.stat_valid === 1) {
          message = '已启用'
        } else {
          message = '已停用'
        }

        ajax.post({
          url: '/stat/save',
          param: finalJsonData,
          success: function (data) {
            // self.getData()
            self.statValidBtnDisabled = false
            self.$message.success(message)
          },
          error: function (data) {
            if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
              self.$message.error(data.error.join('|'))
            } else {
              self.$message.error(data.error)
            }
            self.statValidBtnDisabled = false
          },
          fail: function () {
            self.statValidBtnDisabled = false
          }
        })
      },
      // 打开表单弹窗 其中view暂时去掉了
      openDialog(dialogType, row) {
        this.dialogType = dialogType
        let self = this
        if (dialogType === 'edit') {
          this.allPickCollapse = true // 编辑时多选为隐藏多的标签
          this.dialogTitle = '编辑交易统计'
          if(this.readonlyParent) {
            this.dialogTitle = '查看交易统计'
          }
          this.modDisabled = true
          this.viewDisabled = false
          // 拷贝而不是赋值
          // Object.assign(this.dialogForm, this.selectRowNum2Str(this.selectedRows[0]))
          Object.assign(this.dialogForm, this.selectRowNum2Str(row))

          setTimeout(function () {
            if (self.dialogForm.stat_param !== '') {
              self.statParamInitList = row.stat_param.split(',')
              // self.statParamInitList = self.selectedRows[0].stat_param.split(',')
            }
            self.dialogForm.fn_param = row.fn_param
            // self.dialogForm.fn_param = self.selectedRows[0].fn_param
          }, 300)
        } else if (dialogType === 'add') {
          this.allPickCollapse = true // 新增时多选为隐藏多的标签
          this.dialogTitle = '新建交易统计'
          this.modDisabled = false
          this.viewDisabled = false
          this.dialogForm = this.initDialogForm()
          this.statParamInitList = []
        } else if (dialogType === 'view') {
          // 这里暂时走不到了
          this.allPickCollapse = false // 查看时多选为显示多的标签
          this.dialogTitle = '查看交易统计'
          let length = this.selectedRows.length
          if (length !== 1) {
            this.$message('请选择一行交易统计信息。')
            return
          }
          this.viewDisabled = true
          this.modDisabled = false
          setTimeout(function () {
            if (self.dialogForm.stat_param !== '') {
              self.statParamInitList = self.selectedRows[0].stat_param.split(',')
            }
            self.dialogForm.fn_param = self.selectedRows[0].fn_param
          }, 300)
          Object.assign(this.dialogForm, this.selectRowNum2Str(this.selectedRows[0]))
        }
        this.dialogVisible = true
        this.dialogOpenHandle()
        setTimeout(function () {
          if (self.$refs['dialogForm']) {
            self.$refs['dialogForm'].clearValidate()
          }
        }, 100)
      },
      openRefsDialog(row) {
        this.selTree(row)
        this.refsDialogVisible = true
      },
      // 下拉获取的码值时字符串，真实数据是数字，要转一下才好用
      selectRowNum2Str(row) {
        let tempObj = {}
        Object.assign(tempObj, row)
        for (let field in tempObj) {
          if (typeof (tempObj[field]) === 'number') {
            tempObj[field] = tempObj[field].toString()
          }
        }
        return tempObj
      },
      // 提交表单，新增，编辑都在这
      submitForm (formName) {
        if (this.readonlyParent) {
          return
        }
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.dialogFormSureBtnDisabled = true
            let self = this
            let submitParam = {}
            Object.assign(submitParam, this.dialogForm)
            submitParam.stat_txn = this.txnIdParent
            submitParam.continues = submitParam.continues == '1' ? '1' : '0'
            submitParam.stat_unresult = submitParam.stat_unresult == '1' ? '1' : '0'
            if (submitParam.stat_param !== null && submitParam.stat_param.length > 0) {
              submitParam.stat_param = submitParam.stat_param.join(',')
            }
            let jsonData = {}
            let message = '提交成功'
            if (this.dialogType === 'add') {
              jsonData.add = [submitParam]
              message = '新建成功'
            } else {
              jsonData.mod = [submitParam]
              message = '编辑成功'
            }
            let finalJsonData = {}
            finalJsonData.postData = jsonData
            finalJsonData.txnId = this.txnIdParent

            ajax.post({
              url: '/stat/save',
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
          }
        })
      },
      // 函数参数弹窗
      fnParamPopup () {
        if (this.readonlyParent) {
          return
        }
        if (!this.viewDisabled) {
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
        }
      },
      // 统计条件弹窗
      statCondInPopup () {
        if (this.readonlyParent) {
          return
        }
        if (!this.viewDisabled) {
          this.$refs.StatCondDialog.open()
          this.$refs.StatCondDialog.setValue({
            stat_cond_value: this.dialogForm.stat_cond,
            stat_cond_in: this.dialogForm.stat_cond_in
          })
        }
      },
      //  下面是子组件的回调
      statParamDataChange (value) {
        this.queryShowForm.stat_param = value
      },
      addStatParamDataChange (value) {
        this.dialogForm.stat_param = value
        this.$refs.dialogForm.validateField('stat_param', (valid) => {
        })
      },
      statCondInValueCallBack (value) {
        this.dialogForm.stat_cond = value.stat_cond_value
        this.dialogForm.stat_cond_in = value.stat_cond_in
      },
      funcParamValueCallBack (value) {
        this.dialogForm.fn_param = value
      },
      // 下面是工具函数（基本控制表单各项的显示隐藏 逻辑按照之前的代码写的）
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
      getEnableStorageFdByDataType(datatype, enableStoreFds) {
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
      changeDialogForm(type) {
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
      fnChangeEventCommon() {
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
      fnChangeEvent() {
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
      countroundChangeEvent() {
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
      datafdChangeEvent() {
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
      checkStatCond(rule, value, callback) {
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
        callback()
      },
      checkStatDataFd(rule, value, callback) {
        let statFn = this.dialogForm.stat_fn
        // let fnParam = this.dialogForm.fn_param
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if ((!isCaExFunc && statFn !== 'count' && statFn !== 'status') && this.dialogForm.stat_datafd === '') {
          return callback(new Error(`统计目标不能为空`))
        }
        callback()
      },
      checkStatParam(rule, value, callback) {
        let statParam = this.dialogForm.stat_param
        let counUnit = this.dialogForm.coununit
        // let fnParam = this.dialogForm.fn_param
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if (!isCaExFunc && (statParam.length === 0 || statParam === '')) {
          return callback(new Error(`统计引用对象不能为空`))
        } else {
          if (statParam.length > 6) {
            return callback(new Error(`统计引用对象最多选择6个`))
          }

          if (counUnit === '9') {
            let isFind = false
            for (let loop of statParam) {
              if (loop.toUpperCase() === 'SESSIONID') {
                isFind = true
                break
              }
            }
            if (!isFind) {
              return callback(new Error(`单位为会话，必须包含"会话标识"`))
            }
          }
        }

        callback()
      },
      checkFnParam(rule, value, callback) {
        let statFn = this.dialogForm.stat_fn
        let fnParam = this.dialogForm.fn_param
        if (statFn === 'rang_bin_dist') {
          if (fnParam === undefined || fnParam === '') {
            return callback(new Error(`函数参数不能为空`))
          } else {
            if (fnParam.length > 512) {
              return callback(new Error(`函数参数不能超过512个字符`))
            }
          }
        }
        callback()
      },
      checkCoununit(rule, value, callback) {
        let coununit = this.dialogForm.coununit
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if (!isCaExFunc) {
          if (coununit === '') {
            return callback(new Error(`单位不能为空`))
          }
        }
        callback()
      },
      checkCountround(rule, value, callback) {
        let coununit = this.dialogForm.coununit
        let countround = this.dialogForm.countround
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if (!isCaExFunc) {
          if (coununit !== '7' || coununit !== '9') { // 永久，会话不需要周期
            if (util.trim(countround) === '') {
              return callback(new Error(`周期不能为空`))
            } else {
              if (!util.isNumber(countround, '+', '0') || parseInt(countround) < 1 || parseInt(countround) > 100) {
                return callback(new Error(`周期只能输入1-100的正整数`))
              }
            }
          }
        }
        callback()
      },
      checkResultCond(rule, value, callback) {
        let resultCond = this.dialogForm.result_cond
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if (!isCaExFunc) {
          if (util.trim(resultCond) === '') {
            return callback(new Error(`交易结果不能为空`))
          }
        }
        callback()
      },
      checkDatatype(rule, value, callback) {
        let datatype = this.dialogForm.datatype
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if (isCaExFunc) {
          if (util.trim(datatype) === '') {
            return callback(new Error(`数据类型不能为空`))
          }
        }
        callback()
      },
      checkStatValid(rule, value, callback) {
        let statValid = this.dialogForm.stat_valid
        let isCaExFunc = this.dialogForm.stat_fn === 'calculat_expressions'
        if (!isCaExFunc) {
          if (util.trim(statValid) === '') {
            return callback(new Error(`有效性不能为空`))
          }
        }
        callback()
      },
      // 下面是引用点的树形方法
      // 查询树结构
      selTree(selectRow) {
        var self = this
        let params = {
          txnId: this.txnIdParent,
          statName: selectRow.stat_name
        }
        var option = {
          url: '/stat/refTree',
          param: params,
          success: function (data) {
            if (data.list && data.list.length > 0) {
              data.row = data.list
              self.treeList = (data.row)
              self.refsTreeData = util.formatTreeData(data.row)
              // self.expendNodesByLevel(1)
            }
          }
        }
        ajax.post(option)
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
  .stat-query-form-item{
    width: 200px;
  }
  .stat-query-box{
    float: right
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }
  .fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
    opacity: 0
  }
</style>
