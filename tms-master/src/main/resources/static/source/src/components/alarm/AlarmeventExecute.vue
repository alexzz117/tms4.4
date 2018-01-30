<template>
  <div>
    <transition name="fade">

      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm" :rules="queryRules"
               :inline="true" style="text-align: left" v-show="queryFormShow">

        <el-form-item label="流水号:" prop="txncode">
          <el-input v-model="queryShowForm.txncode" class="alarm-event-query-form-item" auto-complete="off" clearable
                    :maxlength="32"></el-input>
        </el-form-item>

        <el-form-item label="客户号:" prop="userid">
          <el-input v-model="queryShowForm.userid" class="alarm-event-query-form-item" auto-complete="off" clearable
                    :maxlength="32"></el-input>
        </el-form-item>

        <el-form-item label="客户名称:" prop="username">
          <el-input v-model="queryShowForm.username" class="alarm-event-query-form-item" auto-complete="off" clearable
                    :maxlength="32"></el-input>
        </el-form-item>

        <el-form-item label="设备标识:" prop="deviceid">
          <el-input v-model="queryShowForm.deviceid" class="alarm-event-query-form-item" auto-complete="off" clearable
                    :maxlength="32"></el-input>
        </el-form-item>

        <el-form-item label="IP地址:" prop="ipaddr">
          <el-input v-model="queryShowForm.ipaddr" class="alarm-event-query-form-item" auto-complete="off" clearable
                    :maxlength="32"></el-input>
        </el-form-item>

        <el-form-item label="监控交易:" prop="txntypeShow">
          <div @click="openTxnTypedialog">
            <el-input v-model="queryShowForm.txntypeShow" class="alarm-event-query-form-item" auto-complete="off"
                      readonly></el-input>
          </div>
        </el-form-item>

        <el-form-item label="处置方式:" prop="disposal">
          <el-select v-model="queryShowForm.disposal" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in disposalList"
              :key="item.dp_code"
              :label="item.dp_name"
              :value="item.dp_code">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="处置结果:" prop="iscorrect">
          <el-select v-model="queryShowForm.iscorrect" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in iscorrectList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>

        <!--<el-form-item label="操作状态:" prop="txnstatus">-->
          <!--<el-select v-model="queryShowForm.txnstatus" class="alarm-event-query-form-item" placeholder="请选择"-->
                     <!--clearable>-->
            <!--<el-option-->
              <!--v-for="item in txnstatusList"-->
              <!--:key="item.value"-->
              <!--:label="item.label"-->
              <!--:value="item.value">-->
            <!--</el-option>-->

          <!--</el-select>-->
        <!--</el-form-item>-->

        <el-form-item label="处理状态:" prop="psstatus">
          <el-select v-model="queryShowForm.psstatus" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in psstatusList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="时间范围:" prop="queryDate">
          <el-date-picker
            v-model="queryShowForm.queryDate"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期">
          </el-date-picker>
        </el-form-item>

        <!--<el-form-item label="地理位置:" prop="countrycode">-->
          <!--<el-select v-model="queryShowForm.countrycode" class="alarm-event-query-form-item" placeholder="请选择"-->
                     <!--clearable filterable>-->
            <!--<el-option-->
              <!--v-for="item in countrycodeList"-->
              <!--:key="item.countrycode"-->
              <!--:label="item.countryname"-->
              <!--:value="item.countrycode">-->
            <!--</el-option>-->

          <!--</el-select>-->

          <!--<el-select v-model="queryShowForm.regioncode" class="alarm-event-query-form-item" placeholder="请选择"-->
                     <!--clearable filterable>-->
            <!--<el-option-->
              <!--v-for="item in regioncodeList"-->
              <!--:key="item.regioncode"-->
              <!--:label="item.regionname"-->
              <!--:value="item.regioncode">-->
            <!--</el-option>-->

          <!--</el-select>-->

          <!--<el-select v-model="queryShowForm.citycode" class="alarm-event-query-form-item" placeholder="请选择"-->
                     <!--clearable filterable>-->
            <!--<el-option-->
              <!--v-for="item in citycodeList"-->
              <!--:key="item.citycode"-->
              <!--:label="item.cityname"-->
              <!--:value="item.citycode">-->
            <!--</el-option>-->

          <!--</el-select>-->
        <!--</el-form-item>-->
      </el-form>

    </transition>

    <div style="margin-bottom: 10px; text-align: left; height: 30px;">
      <div style="float:right">
        <el-button type="primary" @click="queryFormShow = !queryFormShow">更多</el-button>
        <el-button type="primary" class="el-icon-search" @click="searchData">查询</el-button>
      </div>
      <div style="float:right; padding-right: 15px;">

        <el-date-picker
          v-show="!queryFormShow"
          v-model="queryShowForm.queryDate"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
        </el-date-picker>
      </div>

    </div>

    <section class="table">
      <el-table
        :data="tableData"
        style="width: 100%" tooltip-effect="dark" :cell-style="tableRowClass">
        <el-table-column label="操作" width="65">
          <template slot-scope="scope">
            <el-button type="text" @click="openDialog(scope.row)" title="处理" icon="el-icon-edit-outline"/>
          </template>
        </el-table-column>
        <el-table-column prop="txncode" label="流水号" align="left">
          <template slot-scope="scope">
            <a href="javascript:void(0)" @click="showAlarmEventInfo(scope.row)">{{scope.row.txncode}}</a>
          </template>
        </el-table-column>
        <el-table-column prop="userid" label="客户号" width="120"/>
        <el-table-column prop="username" label="客户名称" width="80"/>
        <el-table-column prop="txntime" label="交易时间" width="135" :formatter="formatter"/>
        <el-table-column prop="txnname" label="监控操作" width="100"/>
        <el-table-column prop="disposal" label="处置方式" width="140" align="left"/>
        <el-table-column prop="iscorrect" label="处置结果" width="140" align="left" :formatter="formatter"/>
        <el-table-column prop="assign_name" label="分派人" width="80"/>
        <el-table-column prop="assigntime" label="分派时间" width="135" :formatter="formatter"/>
        <el-table-column prop="psstatus" label="处理状态" width="80" :formatter="formatter"/>
        <el-table-column prop="oper_name" label="处理人" width="80"/>
      </el-table>

      <el-pagination style="margin-top: 10px; text-align: right;"
                     :current-page="currentPage"
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     :page-sizes="[10, 25, 50, 100]"
                     :page-size="pageSize"
                     layout="total, sizes, prev, pager, next, jumper"
                     :total="total"
      >
      </el-pagination>
    </section>

    <el-dialog title="监控交易" :visible.sync="txntypeDialogVisible" width="400px" :close-on-click-modal="false">
      <el-tree :data="treeData" node-key="id" ref="tree"
               show-checkbox
               :props="defaultTreeProps"
               :highlight-current=true
               :default-expanded-keys="expendKey"
               :expand-on-click-node="false"
               :render-content="renderContent"
               @check-change="handleCheckChange"
               style="overflow-y: auto;">
      </el-tree>

      <div slot="footer" class="dialog-footer">
        <el-button @click="txntypeDialogVisible = false" size="large">取 消</el-button>
      </div>
    </el-dialog>

    <txn-detail ref="txnDetail" :txn="selectedRow"></txn-detail>

    <!-- 报警事件处理弹窗 -->
    <el-dialog title="报警事件处理" :visible.sync="listDialogVisible" width="600px" :close-on-click-modal="false">
      <div>
        <el-form :model="executeForm" ref="executeForm" style="text-align: left" :inline="true" :rules="executeRules">
          <el-form-item label="欺诈类型:" :label-width="formLabelWidth" prop="fraud_type" :style="formItemStyle">

            <el-select v-model="executeForm.fraud_type" class="stat-query-form-item" placeholder="请选择"
                       clearable :style="textareaContentStyle">
              <el-option
                v-for="item in datatypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>

            </el-select>
          </el-form-item>

          <el-form-item label="处理信息:" :label-width="formLabelWidth" prop="fraud_type" :style="textareaFormItemStyle">
            <el-input type="textarea" v-model="executeForm.ps_info" :style="textareaContentStyle" :maxlength=200></el-input>
          </el-form-item>

          <div>
            <el-form-item  label=" " :label-width="formLabelWidth" :style="formItemStyle">
              <el-button type="primary" @click="saveProcess('executeForm')">保 存</el-button>
              <el-button data="cancelBtn" type="primary" @click="listDialogVisible = false">取消</el-button>
            </el-form-item>
          </div>

        </el-form>

        <el-collapse v-model="collapseActiveNames">
          <el-collapse-item title="动作信息" name="1">
            <div style="text-align: left;">
              <div>
                <el-button plain class="el-icon-plus" @click="addPsAct('add')">新建</el-button>
                <el-button plain icon="el-icon-delete" @click="delPsAct" :disabled="!actionSelect">删除</el-button>
              </div>
            </div>

            <!-- GRID -->
            <el-table
              :data="actionData" height="200" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="45"/>
              <el-table-column label="操 作" width="55">
                <template slot-scope="scope">
                  <el-button type="text" @click="editPsAct(scope.$index, scope.row,'edit')" icon="el-icon-edit"/>
                </template>
              </el-table-column>
              <el-table-column prop="ac_name" label="动作名称" align="left"/>
              <el-table-column prop="ac_cond_in" label="条件" align="left" width="150"/>
              <el-table-column prop="ac_expr_in" label="表达式" align="left" width="150"/>

            </el-table>
          </el-collapse-item>
        </el-collapse>

      </div>
    </el-dialog>

    <!-- 添加动作FROM -->
    <el-dialog :title="actionDialogTitle" :visible.sync="actionDialogVisible" width="450px" :modal="false" :close-on-click-modal="false">
      <el-form :model="actionDialogForm" ref="actionDialogForm" style="text-align: left" :rules="actionRules" :inline="true">
        <el-form-item label="动作名称:" :label-width="formLabelWidth" prop="ac_desc" :style="actionFormItemStyle">
          <el-input v-model="actionDialogForm.ac_desc" auto-complete="off" :style="actionFormItemContentStyle" :maxlength="128"></el-input>
        </el-form-item>
        <el-form-item label="动作条件:" :label-width="formLabelWidth" prop="ac_cond_in" :style="actionFormItemStyle">
          <div @dblclick="acCondInPopup">
            <el-input v-show="false" v-model="actionDialogForm.ac_cond" auto-complete="off" :style="actionFormItemContentStyle" readonly></el-input>
            <el-input v-model="actionDialogForm.ac_cond_in" auto-complete="off" :style="actionFormItemContentStyle" readonly></el-input>
          </div>
        </el-form-item>
        <el-form-item class="is-required" label="表达式:" :label-width="formLabelWidth" prop="ac_expr_in" :style="actionFormItemStyle">
          <div @dblclick="acExprInPopup">
            <el-input v-show="false" v-model="actionDialogForm.ac_expr" auto-complete="off" :style="actionFormItemContentStyle" readonly ></el-input>
            <el-input v-model="actionDialogForm.ac_expr_in" auto-complete="off" :style="actionFormItemContentStyle" readonly ></el-input>
          </div>
        </el-form-item>

        <div>
          <el-form-item  label=" " :label-width="formLabelWidth" :style="formItemStyle">
            <el-button type="primary" @click="submitActionForm('actionDialogForm')" :disabled="actionFormSureBtnDisabled" size="large">保 存</el-button>
            <el-button @click="actionDialogVisible = false" size="large">取 消</el-button>
          </el-form-item>
        </div>

      </el-form>
      <StatCondPicker ref="acCondDialog" @valueCallback="acCondInValueCallBack"
                      :statCond="actionDialogForm.ac_cond" :statCondIn="actionDialogForm.ac_cond_in" :txnId="txnIdParent"
                      :hideItems="['stat_fn','rule_func','ac_func']" >

      </StatCondPicker>

      <StatCondPicker ref="acExprDialog" @valueCallback="acExprValueCallBack"
                      :statCond="actionDialogForm.ac_expr" :statCondIn="actionDialogForm.ac_expr_in" :txnId="txnIdParent"
                      :hideItems="['stat_fn','rule_func']" >

      </StatCondPicker>
    </el-dialog>
  </div>
</template>

<script>
  import ajax from "../../common/ajax";
  import util from "../../common/util";
  import dictCode from "../../common/dictCode";
  import check from "../../common/check";

  import TxnDetail from '@/components/synquery/TxnDetail'
  import StatCondPicker from '@/components/common/StatCondPicker'

  let iscorrectList = [{'label': '未认证', 'value': '2'}, {'label': '认证通过', 'value': '1'}, {
    'label': '认证未通过',
    'value': '0'
  }]

  export default {
    name: 'alarmEventQuery',
    computed: {
      actionSelect () {
        return this.selectedRows.length > 0
      }
    },
    data() {
      return {
        collapseActiveNames:[],
        txntypeDialogVisible: false,
        infoDialogVisible: false,
        defaultTreeProps: {
          children: 'children',
          label: 'tab_desc'
        },
        listDialogVisible: false,
        expendKey: ['T'], // 默认展开的功能节点id
        treeData: [],
        queryFormShow: false,
        executeForm: {
          fraud_type: '',
          ps_info: ''
        },
        actionForm:{

        },
        txnIdParent: '',
        actionDialogTitle: '',
        actionDialogVisible: false,
        actionFormSureBtnDisabled: false,
        actionDialogType: '',
        actionDialogForm: {
          ac_desc: '',
          ac_cond: '',
          ac_cond_in: '',
          ac_expr: '',
          ac_expr_in: ''
        },
        actionRules: {
          ac_desc: [
            { required: true, message: '请输入动作名称', trigger: 'blur' },
            { max: 128, message: '长度在128个字符以内', trigger: 'blur' },
            { validator: check.checkFormZhSpecialCharacter, trigger: 'blur' }
          ],
          ac_cond_in: [
            { validator: this.checkAcCond, trigger: 'blur' }
          ],
          ac_expr_in: [
            { validator: this.checkAcRxpr, trigger: 'blur' }
          ],
          ac_enable: [
            { required: true, message: '请输入有效性', trigger: 'change' }
          ]
        },
        executeRules: {
          fraud_type: [
            { required: true, message: '请输入欺诈类型', trigger: 'blur' }
          ],
          ps_info: [
            { required: true, message: '请输入处理信息', trigger: 'blur' },
            { max: 255, message: '长度在255个字符以内', trigger: 'blur' }
          ]
        },
        datatypeOptions: [],
        queryShowForm: {
          txncode: '',
          userid: '',
          username: '',
          deviceid: '',
          ipaddr: '',
          txntype: '',
          txntypeShow: '',
          disposal: '',
          iscorrect: '',
          txnstatus: '',
          psstatus: '',
          countrycode: '',
          regioncode: '',
          citycode: '',
          queryDate: this.initDateRange()
        },
        queryForm: {
          txncode: '',
          userid: '',
          username: '',
          deviceid: '',
          ipaddr: '',
          txntype: '',
          txntypeShow: '',
          disposal: '',
          iscorrect: '',
          txnstatus: '',
          psstatus: '',
          countrycode: '',
          regioncode: '',
          citycode: '',
          queryDate: this.initDateRange()
        },
        queryRules: {
          ipaddr: [
            {validator: check.checkFormIp, trigger: 'blur'}
          ]
        },
        dialogTitle: '',
        dictDialogVisible: false,
        formLabelWidth: '130px',
        formItemStyle: {
          width: '600px'
        },
        actionFormItemStyle:{
          width: '400px'
        },
        actionFormItemContentStyle: {
          width: '250px'
        },
        textareaFormItemStyle: {
          width: '600px'
        },
        formItemContentStyle: {
          width: '400px'
        },
        textareaContentStyle: {
          width: '300px'
        },
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectedRow: {},
        tabActiveName: 'operate',
        tableData: [],
        actionData: [],
        selectedRows:[],
        // 下拉框
        txntypeList: [],
        disposalList: [],
        iscorrectList: iscorrectList,
        txnstatusList: [],
        psstatusList: [],
        countrycodeList: [],
        regioncodeList: [],
        citycodeList: []
      }
    },
    watch: {
      'queryShowForm.countrycode': function (val) {
        let self = this
        ajax.post({
          url: '/monitor/alarm/get_region_by_country',
          param: {country: val},
          success: function (data) {
            self.regioncodeList = data.row
          }
        })
      },
      'queryShowForm.regioncode': function (val) {
        let self = this
        ajax.post({
          url: '/monitor/alarm/get_city_by_region',
          param: {region: val},
          success: function (data) {
            self.citycodeList = data.row
          }
        })
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        let self = this
        this.datatypeOptions = dictCode.getCodeItems('tms.alarm.fraudtype')
        this.initSelectData().then(function () {
          ajax.post({
            url: '/monitor/alarm/get_all_country',
            param: {},
            success: function (data) {
              self.countrycodeList = data.row
            }
          })
        }).then(this.getData())
        // this.getData()
      })
    },
    filters: {
      renderDateTime(value) {
        return util.renderDateTime(value)
      },
      renderTxnstatus(value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.common.txnstatus', value)
      },
      renderIscorrect(value) {
        for (let item of iscorrectList) {
          if (item.value === value) {
            return item.label
          }
        }
        return ''
      },
      renderPsStatus(value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.status', value)
      }
    },
    methods: {
      initDateRange() {
        let start = new Date()
        start.setHours(new Date().getHours() - 1)
        let end = new Date()
        return [start, end]
      },
      tableRowClass(rowInfo) {
        if (rowInfo.columnIndex === 0) {
          return {}
        }
        let row = rowInfo.row
        let currenttime = row.currenttime
        let timeout = row.timeout
        let psstatus = row.psstatus
        let conpareTime = null
        if (psstatus === '02') {
          conpareTime = row.m_assigntime
        } else if (psstatus === '04') {
          conpareTime = row.m_audittime
        }
        if (conpareTime != null && 1 * currenttime - 1 * conpareTime > 1 * timeout) {
          return {'background-color': '#f56c6c'}
          // return 'red-row'
        }
      },
      //GRID数据列表值转换
      formatter(row, column, cellValue) {
        switch (column.property) {
          case 'psstatus':
            return dictCode.rendCode('tms.alarm.process.status', cellValue)
            break;
          case 'txntime':
            return dictCode.rendDatetime(cellValue)
            break;
          case 'assigntime':
            return dictCode.rendDatetime(cellValue)
            break;
          case 'iscorrect': {
            for (let item of iscorrectList) {
              if (item.value === cellValue) {
                return item.label
              }
            }
            return ''
            break
          }
          default:
            return cellValue
            break;
        }
      },
      handleSelectionChange (rows) {
        this.selectedRows = rows
      },
      initSelectData() {
        this.txnstatusList = dictCode.getCodeItems('tms.common.txnstatus')
        this.psstatusList = dictCode.getCodeItems('tms.alarm.process.status')
        let self = this
        this.selTree()
        ajax.post({
          url: '/rule/disposal',
          param: {},
          success: function (data) {
            self.disposalList = data.row
          }
        })
        return new Promise(function (resolve, reject) {
          resolve()
        })
      },
      searchData(formName) {
        this.$refs['queryShowForm'].validate((valid) => {
          if (valid) {
            Object.assign(this.queryForm, this.queryShowForm)
            this.getData()
          }
        })
      },
      getData() {
        let self = this
        let paramsObj = {
          modelType: 'alarmEventExecute',
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        Object.assign(paramsObj, this.queryForm)
        if (this.queryForm.queryDate && this.queryForm.queryDate.length > 1) {
          paramsObj.startDate = this.queryForm.queryDate[0].getTime()
          paramsObj.endDate = this.queryForm.queryDate[1].getTime()
        }

        ajax.post({
          url: '/query/alarmEvent/result',
          model: ajax.model.dualaudit,
          param: paramsObj,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      bindGridData(data) {
        this.tableData = data.page.list
        // 加上这个会出现页码之间来回跳的问题
        // this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      handleSizeChange(val) {
        // console.log(`每页 ${val} 条`)
        this.currentPage = 1
        this.pageSize = val
        this.getData()
      },
      handleCurrentChange(val) {
        this.currentPage = val
        this.getData()
      },
      handleCheckChange(data, checked, indeterminate) {
        // console.log(data, checked, indeterminate)
        let checkedArr = this.$refs.tree.getCheckedNodes()
        let checkedStrArr = []
        let checkedStrKeyArr = []
        for (let item of checkedArr) {
          checkedStrArr.push(item.tab_desc)
          checkedStrKeyArr.push(item.tab_name)
        }
        this.queryShowForm.txntypeShow = checkedStrArr.join(',')
        this.queryShowForm.txntype = checkedStrKeyArr.join(',')
      },
      // 功能树渲染方法
      renderContent(h, {node, data, store}) {
        if (node.data.enable === 0) { // 功能节点状态禁用
          return (<span class="el-tree-node__label disabledFlag">{node.label}</span>)
        } else { // 功能节点状态正常
          return (<span class="el-tree-node__label">{node.label}</span>)
        }
      },
      // 查询树结构
      selTree() {
        var self = this
        var option = {
          url: '/trandef/query',
          success: function (data) {
            if (data.list) {
              self.treeList = (data.list)
              self.treeData = self.formatTreeData(data.list)
            }
          }
        }
        ajax.post(option)
      },
      // 把功能节点列表格式化为树形Json结构
      formatTreeData(list, rootNodes) {
        var tree = []
        // 如果根节点数组不存在，则取fid不存在或为空字符的节点为父节点
        if (rootNodes === undefined || rootNodes.length === 0) {
          rootNodes = []
          for (var i in list) {
            list[i].text = `${list[i].code_value}(${list[i].code_key})`
            if (list[i].fid === undefined || list[i].fid === null || list[i].fid === '') {
              rootNodes.push(list[i])
            }
          }
        }
        // 根节点不存在判断
        if (rootNodes.length === 0) {
          console.error('根节点不存在，请确认树结构是否正确')
          console.info('树结构的根节点是fid不存在（或为空）的节点，否则需手动添加指定得根节点（参数）')
        }
        // 根据根节点遍历组装数据
        for (var r in rootNodes) {
          var node = rootNodes[r]
          node.children = getChildren(list, node.id)
          tree.push(node)
        }

        // 递归查询节点的子节点
        function getChildren(list, id) {
          var childs = []
          for (var i in list) {
            var node = list[i]
            if (node.fid === id) {
              node.children = getChildren(list, node.id)
              // node.icon = 'el-icon-message'
              childs.push(node)
            }
          }
          return childs
        }

        return tree  // 返回树结构Json
      },

      openTxnTypedialog() {
        this.txntypeDialogVisible = true
      },
      showAlarmEventInfo(row) {
        this.selectedRow = row
        this.infoDialogVisible = true
        let self = this
        self.$refs.txnDetail.open()
        // setTimeout(function () {
        //   self.$refs.operateDetail.loadData(row)
        //   self.$refs.strategyDetail.loadData(row)
        //   self.$refs.handleDetail.loadData(row)
        // }, 200)
      },
      handleInfoOpen() {

      },

      setQueryShowForm(queryShowForm) {
        this.queryShowForm = queryShowForm
      },
      //报警事件处理事件
      openDialog(row) {
        let self = this
        this.selectedRow = row
        let TXN_CODE = row.txncode
        this.vTxncode = TXN_CODE
        this.executeForm = {
          fraud_type: '',
          ps_info: ''
        }
        this.getActionData(TXN_CODE)
      },
      getActionData (TXN_CODE) {
        this.actionData = []
        let self = this
        ajax.post({
          url: '/alarmevent/process',
          param: {TXN_CODE: TXN_CODE},
          success: function (data) {
            self.listDialogVisible = true
            if (data.list) {
              self.actionData = data.list
            }
            self.txnIdParent = data.txnmap.txntype
            // if (data.row) {
            //   self.txnMap = data.row
            // Object.assign(this.executeForm)
            // }
          }
        })
      },
      //新增动作信息
      addPsAct(flag){
        var self = this
        this.actionDialogVisible = true
        this.actionDialogType = 'add'
        // this.flag = flag
        this.actionDialogForm= {
          ac_desc: "",
          ac_cond: "",
          ac_cond_in: "",
          ac_expr: "",
          ac_expr_in: ""
        }
      },
      //编辑动作信息
      editPsAct(index, row,flag) {
        console.log(row)
        var self = this
        this.actionDialogType = 'edit'
        this.actionDialogVisible = true
        // this.flag = flag
        this.actionDialogForm = Object.assign({}, row)
        this.actionDialogForm.ac_desc = row.ac_name
      },
      delPsAct(){
        let self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {

          let jsonData = {}
          let acIds = []
          for (let item of this.selectedRows) {
            acIds.push(item.ac_id)
          }
          jsonData.psActs = acIds.join(',')
          ajax.post({
            url: '/alarmevent/delPsAct',
            param: jsonData,
            success: function (data) {
              self.getActionData(self.selectedRow.txncode)
              self.$message.success('删除成功')
            }
          })
        })
      },
      acCondInPopup () {
        this.$refs.acCondDialog.open()
        this.$refs.acCondDialog.setValue({
          stat_cond_value: this.actionDialogForm.ac_cond,
          stat_cond_in: this.actionDialogForm.ac_cond_in
        })
      },
      acExprInPopup () {
        this.$refs.acExprDialog.open()
        this.$refs.acExprDialog.setValue({
          stat_cond_value: this.actionDialogForm.ac_expr,
          stat_cond_in: this.actionDialogForm.ac_expr_in
        })
      },
      acCondInValueCallBack (value) {
        this.actionDialogForm.ac_cond = value.stat_cond_value
        this.actionDialogForm.ac_cond_in = value.stat_cond_in
      },
      acExprValueCallBack (value) {
        this.actionDialogForm.ac_expr = value.stat_cond_value
        this.actionDialogForm.ac_expr_in = value.stat_cond_in
        this.$refs.actionDialogForm.validateField('ac_expr_in', (valid) => {
        })
      },
      submitActionForm (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.actionFormSureBtnDisabled = true
            let self = this
            let submitParam = {}
            Object.assign(submitParam, this.actionDialogForm)
            console.log(this.selectedRow)
            submitParam.TXN_CODE = this.selectedRow.txncode
            submitParam.ac_name = submitParam.ac_desc
            // submitParam = util.toggleObjKey(submitParam, 'upper')
            let message = '提交成功'
            let url = ''
            if (this.actionDialogType === 'add') {
              // jsonData.add = [submitParam]
              url = '/alarmevent/addPsAct'
              message = '新建成功'
            } else {
              // jsonData.mod = [submitParam]
              url = '/alarmevent/updPsAct'
              message = '编辑成功'
            }

            ajax.post({
              url: url,
              param: submitParam,
              success: function (data) {
                self.getActionData(self.selectedRow.txncode)
                self.$message.success(message)
                self.actionDialogVisible = false
                self.actionFormSureBtnDisabled = false
              },
              error: function (data) {
                if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
                  self.$message.error(data.error.join('|'))
                } else {
                  self.$message.error(data.error)
                }
                self.actionFormSureBtnDisabled = false
              },
              fail: function () {
                self.actionFormSureBtnDisabled = false
              }
            })
          }
        })
      },
      saveProcess (formName) {
        var self = this
        this.$refs[formName].validate((valid) => {
          if (valid) {
            console.log(self.selectedRow)
            var param = util.extend({
              TXN_CODE: self.selectedRow.txncode,
              TXNCODE: self.selectedRow.txncode,
              PS_TYPE: '1',
              PSSTATUS: '03',
              PS_RESULT: '1'
            }, this.executeForm)
            param = util.toggleObjKey(param, 'upper')
            ajax.post({
              url: '/alarmevent/saveProcess',
              param: param,
              success: function (data) {
                self.$message({
                  message: '处理成功',
                  type: 'success'
                })
                self.listDialogVisible = false
                self.getData()
              }
            })
          } else {
            return false
          }
        })
      }
    },
    components: {
      'txn-detail': TxnDetail,
      StatCondPicker
    }
  }
</script>

<style scoped>
  .alarm-event-query-form-item {
    width: 200px;
  }
</style>
