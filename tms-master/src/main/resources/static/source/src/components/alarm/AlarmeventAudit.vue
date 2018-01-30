<template>
  <div>
    <transition name="fade">
      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm" :rules="queryRules"
               :inline="true" style="text-align: left" v-show="queryFormShow" >

        <el-form-item label="流水号:" prop="txncode">
          <el-input v-model="queryShowForm.txncode" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="客户号:" prop="userid">
          <el-input v-model="queryShowForm.userid" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="客户名称:" prop="username">
          <el-input v-model="queryShowForm.username" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="设备标识:" prop="deviceid">
          <el-input v-model="queryShowForm.deviceid" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="IP地址:" prop="ipaddr">
          <el-input v-model="queryShowForm.ipaddr" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="监控交易:" prop="txntypeShow">
          <div @click="openTxnTypedialog" >
            <el-input v-model="queryShowForm.txntypeShow" class="alarm-event-query-form-item" auto-complete="off" readonly ></el-input>
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
        <el-form-item label="操作状态:" prop="txnstatus">
          <el-select v-model="queryShowForm.txnstatus" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in txnstatusList"
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
        <el-form-item label="地理位置:" prop="countrycode">
          <el-select v-model="queryShowForm.countrycode" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable filterable>
            <el-option
              v-for="item in countrycodeList"
              :key="item.countrycode"
              :label="item.countryname"
              :value="item.countrycode">
            </el-option>
          </el-select>
          <el-select v-model="queryShowForm.regioncode" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable filterable>
            <el-option
              v-for="item in regioncodeList"
              :key="item.regioncode"
              :label="item.regionname"
              :value="item.regioncode">
            </el-option>
          </el-select>
          <el-select v-model="queryShowForm.citycode" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable filterable>
            <el-option
              v-for="item in citycodeList"
              :key="item.citycode"
              :label="item.cityname"
              :value="item.citycode">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </transition>
    <div style="margin-bottom: 10px; text-align: left; height: 30px;">
      <div style="float:right">
        <el-button type="primary" @click="queryFormShow = !queryFormShow">更多</el-button>
        <el-button type="primary" @click="searchData">搜索</el-button>
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
      <!--   数据列表  -->
      <el-table
        :data="gridData"
        style="width: 100%" tooltip-effect="dark" @selection-change="handleSelectionChange">
        <el-table-column fixed="left" label="操作" width="65" alert="center" >
          <template slot-scope="scope"  >
            <el-button type="text"  @click="openDialog(scope.$index, scope.row)" icon="el-icon-edit-outline" />
          </template>
        </el-table-column>
        <el-table-column  prop="txncode" label="流水号" width="235" align="left" class-name="link-item">
          <template slot-scope="scope">
            <a @click="queryTxnInfo(scope.row)">{{ scope.row.txncode }}</a>
          </template>
        </el-table-column>
        <el-table-column  prop="userid" label="客户号" width="120" align="left"/>
        <el-table-column  prop="username" label="客户名称" min-width="100" align="left"/>
        <el-table-column  prop="txntime" label="交易时间" min-width="135" align="left" :formatter="formatter"/>
        <el-table-column  prop="txnname" label="监控操作" min-width="90" align="left"/>
        <el-table-column  prop="disposal" label="处置方式" min-width="80" align="left"/>
        <el-table-column  prop="iscorrect" label="处置结果" min-width="80" align="left" :formatter="renderIsCorrect"/>
        <el-table-column  prop="psstatus" label="处理状态" min-width="80" :formatter="formatter"/>
        <el-table-column  prop="assign_name" label="分派人" min-width="80" align="left"/>
        <el-table-column  prop="oper_name" label="处理人" min-width="80" align="left"/>
        <el-table-column  prop="opertime" label="处理时间" min-width="135" :formatter="formatter"/>
      </el-table>
      <el-pagination style="margin-top: 10px; text-align: right;" background
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     :current-page="pageindex"
                     :page-sizes="[10, 25, 50, 100]"
                     :page-size="pagesize"
                     layout="total, sizes, prev, pager, next, jumper"
                     :total="total">
      </el-pagination>
    </section>
    <!--报警事件审核ExecuteData -->
    <el-dialog title="报警事件审核" :visible.sync="listDialogVisible" append-to-body :close-on-click-modal="false">
      <div>
        <el-form label-position="right" :model="actionForm" :inline="inline" >
          <el-row>
            <el-col :span="3"><div ><el-form-item label="欺诈类型"/></div></el-col>
            <el-col :span="9"><div ><el-form-item prop="fraud_type" clearable>
              <el-select v-model="actionForm.fraud_type" placeholder="请选择" @focus="selectFocus('fraud_type')" disabled>
                <el-option
                  v-for="item in datatypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            </div>
            </el-col>
          </el-row>
        </el-form>

        <el-collapse v-model="activeNames" @change="handleChange">
          <el-collapse-item title="处理动作信息" name="1">
            <el-table :data="actionData" highlight-current-row max-height="120"
              tooltip-effect="dark" @selection-change="handleCurrentRow">
              <el-table-column  prop="ac_name" label="动作名称"/>
              <el-table-column  prop="ac_cond_in" label="条件"/>
              <el-table-column  prop="ac_expr_in" label="表达式"/>
            </el-table>
          </el-collapse-item>
          <el-collapse-item title="报警处理信息" name="2">
            <el-table
              :data="executeData" highlight-current-row max-height="120"
              tooltip-effect="dark" @selection-change="handleCurrentRow">
              <el-table-column  prop="ps_time" label="处理时间" width="140"  />
              <el-table-column  prop="ps_type" label="处理类型" width="90"   :formatter="formatter"/>
              <el-table-column  prop="login_name" label="处理人员" width="90"  />
              <el-table-column  prop="ps_result" label="处理结果" width="90"   :formatter="formatter"/>
              <el-table-column  prop="ps_info" label="处理信息"/>
            </el-table>
          </el-collapse-item>
        </el-collapse>

        <!--审核信息 -->
        <el-form  label-position="right" :model="auditForm"  ref="auditForm" :rules="rules"    >
          <el-row>
            <el-col :span="3"><div ><el-form-item label="审核结果" prop="PS_RESULT"/></div></el-col>
            <el-col :span="9"><div >
              <el-form-item  >
                <el-radio v-model="auditForm.PS_RESULT" label="1">通过</el-radio>
               <el-radio v-model="auditForm.PS_RESULT" label="0">不通过</el-radio>
              </el-form-item>
            </div>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="3" :rows="3"><div><el-form-item label="处理信息"  prop="PS_INFO"  /></div></el-col>
            <el-col :span="20"><div><el-input
              type="textarea"resize="none" v-model="auditForm.PS_INFO"
              :rows="3"  auto-complete="off"
              placeholder="请输入内容">
            </el-input></div></el-col>
          </el-row>
        </el-form>
      </div>
      <div class="dialog-footer" align="left" slot="footer">
        <el-button data="cancelBtn" icon="el-icon-success" type="primary"  @click="onSaveAudit('auditForm')" >提 交</el-button>
        <el-button data="cancelBtn" icon="el-icon-arrow-left" type="primary" @click="listDialogVisible = false" >返 回</el-button>
      </div>
    </el-dialog>
    <el-dialog title="监控交易" :visible.sync="txntypeDialogVisible" width="400px">
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
    <txn-detail ref="txnDetail" :txn="selectedRow" :defaultTab="defaultTab" :showItem=showItem></txn-detail>
  </div>
</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'
  import dictCode from '@/common/dictCode'
  import check from "../../common/check";
  import TxnDetail from '@/components/synquery/TxnDetail'

  let iscorrectList = [{'label': '未认证', 'value': '2'}, {'label': '认证通过', 'value': '1'}, {'label': '认证未通过', 'value': '0'}]

  export default {
    created () {
      this.datatypeOptions = dictCode.getCodeItems('tms.alarm.fraudtype')
    },
    mounted: function () {
      this.$nextTick(function () {
        let self = this
        this.initSelectData().then(function () {
          ajax.post({
            url: '/monitor/alarm/get_all_country',
            param: {},
            success: function (data) {
              self.countrycodeList = data.row
            }
          })
        }).then(this.sel())
        // this.getData()
      })
    },
    methods: {
      handleSizeChange(val) {
        this.sel({
          pageindex: this.pageindex,
          pagesize: val
        })
      },
      handleCurrentChange(val) {
        this.sel({
          pageindex: val,
          pagesize: this.pagesize
        })
      },
      handleCurrentRow(val){
        this.multipleSelection = val
        if (val.length === 1){
          this.btnStatus = false
        }
        else {
          this.btnStatus = true
        }
      },
      handleSelectionChange(val) {
        this.multipleSelection = val
        if (val.length === 1){
          this.sendBtnStatus = false
        } else if (val.length > 1){
          this.sendBtnStatus = false
        } else {
          this.sendBtnStatus = true
        }
      },
      selectFocus (name) {
        //查询条件下拉值转换
        if (name === 'fraud_type' && this.datatypeOptions.length === 0) {
          this.datatypeOptions = dictCode.getCodeItems('tms.alarm.fraudtype')
        }
      },
      formatter(row, column, cellValue) {
        //GRID数据列表值转换
        switch(column.property)
        {
          case 'psstatus':
            return dictCode.rendCode('tms.alarm.process.status', cellValue)
            break;
          case 'ps_result':
            return dictCode.rendCode('tms.alarm.process.result', cellValue)
            break;
          case 'ps_type':
            return dictCode.rendCode('tms.alarm.process.type', cellValue)
            break;
          case 'txntime':
            return dictCode.rendDatetime(cellValue)
            break;
          case 'opertime':
            return dictCode.rendDatetime(cellValue)
            break;
          default:
            return cellValue
            break;
        }
      },
      searchData (formName) {
        this.$refs['queryShowForm'].validate((valid) => {
          if (valid) {
            Object.assign(this.queryForm, this.queryShowForm)
            this.sel()
          }
        })
      },
      sel(pageinfo) {
        //界面初始化和查询按钮事件
        var self = this
        var param
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend({
            pageindex: this.pageindex,
            pagesize: this.pagesize
          }, this.queryShowForm, pageinfo)
        } else {
          param = util.extend({
            pageindex: this.pageindex,
            pagesize: this.pagesize
          }, this.queryShowForm)
        }
        if (this.queryForm.queryDate && this.queryForm.queryDate.length > 1) {
          param.operate_time = this.queryForm.queryDate[0].getTime()
          param.end_time = this.queryForm.queryDate[1].getTime()
        }
        ajax.post({
          url: '/alarmevent/auditList',
          param: param,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      bindGridData(data) {
        this.gridData = data.page.list
        this.pageindex = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      //报警事件审核弹窗
      openDialog(index, row){
        var self = this
        this.listDialogVisible = true
        var TXN_CODE = row.txncode
        this.vTxncode = TXN_CODE
        ajax.post({
          url: '/alarmevent/audit',
          param: {TXN_CODE: TXN_CODE},
          success: function (data) {
            console.log(data)
            var aa = data['txnmap']
            var psStatus = aa['fraud_type']
            var execueResult = aa['execue_result']
            var execueInfo = aa['execue_info']

            if (data.actlist) {
              self.actionData = data.actlist
            }
            if (data.pslist) {
              self.executeData = data.pslist
            }
            if (util.trim(psStatus)) {            // 欺诈类型
              self.actionForm.fraud_type = psStatus
            } else {
              self.actionForm.fraud_type = ''
            }
            self.auditForm.PS_RESULT = execueResult || '1'  // 审核结果
            self.auditForm.PS_INFO = execueInfo || ''       // 处理信息
            console.info(self.auditForm.PS_RESULT, self.auditForm.PS_INFO)
            // if (data.txnmap) {
            //   self.actionForm =  Object.assign({}, txnmap.fraud_type)
            // }
          }
        })
        self.activeNames = ['1', '2']
      },
      //报警事件审核提交
      onSaveAudit(formName){
        var self = this
        this.$refs[formName].validate((valid) => {
          if (valid) {
            var param = util.extend({
              TXN_CODE: this.vTxncode,
              PS_TYPE: '2'
            }, this.auditForm)
            ajax.post({
              url: '/alarmevent/saveAudit',
              param: param,
              success: function (data) {
                self.$message({
                  message: '审核成功。',
                  type: 'success'
                })
                self.listDialogVisible = false
                self.sel()
              }
            })
          } else {
            return false
          }
        })
      },
      queryTxnInfo (row) {
        let self = this
        self.selectedRow = row
        self.$refs['txnDetail'].open()
      },
      initDateRange () {
        let start = new Date()
        start.setHours(new Date().getHours() - 1)
        let end = new Date()
        return [start, end]
      },
      initSelectData () {
        this.txnstatusList = dictCode.getCodeItems('tms.common.txnstatus')
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
      handleCheckChange (data, checked, indeterminate) {
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
      renderContent (h, { node, data, store }) {
        if (node.data.enable === 0) { // 功能节点状态禁用
          return (<span class="el-tree-node__label disabledFlag">{node.label}</span>)
        } else { // 功能节点状态正常
          return (<span class="el-tree-node__label">{node.label}</span>)
        }
      },
      // 查询树结构
      selTree () {
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
      formatTreeData (list, rootNodes) {
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
        function getChildren (list, id) {
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
      openTxnTypedialog () {
        this.txntypeDialogVisible = true
      },
      renderIsCorrect (row, column, cellValue) {
        switch (cellValue) {
          case '0':
            return '认证未通过'
          case '1':
            return '认证通过'
          case '2':
            return '未认证'
          default:
            return ''
        }
      },
      handleChange () {

      }
    },
    data () {
      return {
        inline: true,
        activeNames: ['1', '2'], // 折叠面板展开配置
        clearable: true,
        listDialogVisible: false,
        actionForm: {
          fraud_type: ''
        },
        auditForm: {
          PS_RESULT: '',
          PS_INFO: ''
        },
        rules: {
          PS_RESULT: [{ required: true, message: '审核结果为空', trigger: 'blur' }],
          PS_INFO: [{ required: true, message: '处理信息为空', trigger: 'blur' },
            { min: 3, max: 255, message: '长度在3到255个字符', trigger: 'blur' }]
        },
        gridData: [],
        actionData: [],
        executeData: [],
        vTxncode: '',
        pageindex: 1,
        pagesize: 10,
        total: 100,
        datatypeOptions: [],
        selectedRow: {},      // 表选中的行
        showItem: ['operate', 'strategy', 'count', 'user', 'handle', 'device', 'deviceFinger'],
        defaultTab: '',
        txntypeDialogVisible: false,
        infoDialogVisible: false,
        defaultTreeProps: {
          children: 'children',
          label: 'tab_desc'
        },
        expendKey: ['T'], // 默认展开的功能节点id
        treeData: [],
        queryFormShow: false,
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
            { validator: check.checkFormIp, trigger: 'blur' }
          ]
        },
        // 下拉框
        disposalList: [],
        iscorrectList: iscorrectList,
        txnstatusList: [],
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
    components: {
      'txn-detail': TxnDetail
    }
  }
</script>
<style>
  .alarm-event-query-form-item{
    width: 200px;
  }
  tbody .link-item:hover {
    color: #FFA000;
    cursor: pointer;
  }
</style>


