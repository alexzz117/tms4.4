<template>
  <div>
    <transition name="fade">
      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm" :rules="queryRules"
               :inline="true" style="text-align: center" v-show="queryFormShow" >
        <el-form-item label="流水号:" prop="txncode">
          <el-input v-model="queryShowForm.txncode" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="客户号:" prop="userid">
          <el-input v-model="queryShowForm.userid" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="客户名称:" prop="username">
          <el-input v-model="queryShowForm.username" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="IP地址:" prop="ipaddr">
          <el-input v-model="queryShowForm.ipaddr" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="设备信息:" prop="deviceid">
          <el-input v-model="queryShowForm.deviceid" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="会话标识:" prop="sessionid">
          <el-input v-model="queryShowForm.sessionid" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="监控操作:" prop="txntype">
          <div @click="openTxnTypedialog" >
            <el-input v-model="queryShowForm.txntype" class="alarm-event-query-form-item" auto-complete="off" readonly ></el-input>
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
        <el-form-item label="是否评估:" prop="iseval">
          <el-select v-model="queryShowForm.iseval" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in isAssessmentList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>
        <el-form-item label="认证状态:" prop="iscorrect">
          <el-select v-model="queryShowForm.iscorrect" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in isCorrectList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="人工确认风险:" prop="confirmrisk">
          <el-select v-model="queryShowForm.confirmrisk" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in confirmRiskList"
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
              v-for="item in txnStatusList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围:" prop="txntime">
          <el-date-picker
            v-model="queryShowForm.txntime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="地理位置:" prop="countrycode">
          <el-select v-model="queryShowForm.countrycode" class="alarm-event-query-form-item" placeholder="请选择" clearable filterable>
            <el-option
              v-for="item in countryCodeList"
              :key="item.countrycode"
              :label="item.countryname"
              :value="item.countrycode">
            </el-option>
          </el-select>
          <el-select v-model="queryShowForm.regioncode" class="alarm-event-query-form-item" placeholder="请选择" clearable filterable>
            <el-option
              v-for="item in regionCodeList"
              :key="item.regioncode"
              :label="item.regionname"
              :value="item.regioncode">
            </el-option>
          </el-select>
          <el-select v-model="queryShowForm.citycode" class="alarm-event-query-form-item" placeholder="请选择" clearable filterable>
            <el-option
              v-for="item in cityCodeList"
              :key="item.citycode"
              :label="item.cityname"
              :value="item.citycode">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </transition>
    <el-row>
      <el-col :span="24">
        <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm" :rules="queryRules"
                 :inline="true" style="text-align: right">
          <el-form-item label="流水号:" prop="txncode" v-show="!queryFormShow">
            <el-input v-model="queryShowForm.txncode" class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
          </el-form-item>
          <el-form-item label="时间范围:" prop="txntime" v-show="!queryFormShow">
            <el-date-picker
              v-model="queryShowForm.txntime"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="queryFormShow = !queryFormShow">更多</el-button>
            <el-button type="primary" @click="searchData">搜索</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table :data="tnxEventTableData">
      <el-table-column prop="txncode" label="流水号" align="left" fixed class-name="link-item" min-width="120px" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <a @click="queryTxnInfo(scope.row, 'operate')">{{ scope.row.txncode }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="userid" label="客户号" align="left" fixed class-name="link-item" min-width="115px" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <a @click="queryTxnInfo(scope.row, 'user')">{{ scope.row.userid }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="sessionid" label="会话标识" align="left" class-name="link-item" min-width="100px" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <a @click="queryTxnInfo(scope.row, 'session')">{{ scope.row.sessionid }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="txnname" label="监控操作" align="left" min-width="100px"></el-table-column>
      <el-table-column prop="txntime" label="操作时间" align="left" :formatter="renderDateTime" min-width="100px" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="location" label="地理信息" align="left"  min-width="100px"></el-table-column>
      <el-table-column prop="deviceid" label="设备信息" align="left" class-name="link-item">
        <template slot-scope="scope">
          <a @click="queryTxnInfo(scope.row, 'device')">{{ scope.row.deviceid }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="ipaddr" label="IP地址" align="left" min-width="112px"></el-table-column>
      <el-table-column prop="iseval" label="是否评估" align="left" :formatter="renderIsEval"></el-table-column>
      <el-table-column prop="ismodelrisk" label="风险类型" align="left" :formatter="renderIsModelRisk"></el-table-column>
      <el-table-column prop="hitrulenum" label="规则命中数" align="left" class-name="link-item">
        <template slot-scope="scope">
          <a @click="queryTxnInfo(scope.row, 'strategy')">{{ scope.row.hitrulenum }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="风险分值" align="left"></el-table-column>
      <el-table-column prop="iscorrect" label="认证状态" align="left" :formatter="renderIsCorrect"></el-table-column>
      <el-table-column prop="disposal" label="处置方式" align="left"></el-table-column>
      <el-table-column prop="confirmrisk" label="人工确认风险" align="left" :formatter="renderConfirmRisk" min-width="105px"></el-table-column>
      <el-table-column prop="txnstatus" label="操作状态" align="left" :formatter="renderTxnStatus"></el-table-column>
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
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import dictCode from '@/common/dictCode'
  import check from '@/common/check'
  import TxnDetail from '@/components/synquery/TxnDetail'
  /**
   * 认证状态下拉列表数据
   */
  let isCorrectList = [{'label': '未认证', 'value': '2'}, {'label': '认证通过', 'value': '1'}, {'label': '认证未通过', 'value': '0'}]
  /**
   * 是否评估下拉列表数据
   * @type {*[]}
   */
  let isAssessmentList = [{label: '否', value: 0}, {label: '是', value: 1}]

  export default {
    data () {
      // 时间范围输入校验
      let txnTimeCheck = (rule, value, callback) => {
        let self = this
        let txncode = self.queryShowForm.txncode
        if (!txncode) {
          if (value && value.length === 2) {
            let startTime = value[0]
            let endTime = value[1]
            let range = (endTime - startTime) / 86400000

            let userid = self.queryShowForm.userid
            let username = self.queryShowForm.username
            let ipaddr = self.queryShowForm.ipaddr
            let deviceid = self.queryShowForm.deviceid
            let sessionid = self.queryShowForm.sessionid

            if (userid || username || ipaddr || deviceid || sessionid) {
              if (range > 30) {
                return callback(new Error('时间范围不能超过1个月(30天)'))
              }
            } else {
              if (range > 1) {
                return callback(new Error('时间范围不能超过1天'))
              }
            }
          } else {
            return callback(new Error('流水号与时间范围不能同时为空'))
          }
        }
        callback()
      }
      return {
        txntypeDialogVisible: false,
        defaultTreeProps: {
          children: 'children',
          label: 'tab_desc'
        },
        expendKey: ['T'], // 默认展开的功能节点id
        treeData: [],
        tnxEventTableData: [],    // 操作事件表数据
        queryFormShow: false,  // 查询条件表单显示控制
        queryShowForm: {      // 查询条件表单
          ruleid: '',
          txncode: '',        // 流水号
          userid: '',         // 客户号
          username: '',       // 客户名称
          ipaddr: '',         // IP地址
          deviceid: '',       // 设备信息
          sessionid: '',      // 会话标识
          txntype: '',        // 监控操作
          disposal: '',       // 处置方式
          iseval: '',         // 是否评估
          iscorrect: '',      // 认证状态
          confirmrisk: '',    // 人工确认风险
          txnstatus: '',      // 操作状态
          txntime: [],        // 时间范围
          operate_time: '',   // 开始时间
          end_time: '',       // 截止时间
          location: '',       // 地理位置
          countrycode: '',    // 国家
          regioncode: '',     // 省份
          citycode: ''        // 城市
        },
        isAssessmentList: isAssessmentList, // 是否评估下拉列表数据
        isCorrectList: isCorrectList, // 认证状态下拉列表数据
        confirmRiskList: [],  // 人工确认风险下拉列表
        countryCodeList: [],  // 地理位置信息列表
        regionCodeList: [],   // 地理位置信息列表
        cityCodeList: [],     // 地理位置信息列表
        txnStatusList: [],    // 操作状态列表
        disposalList: [],     // 处置方式列表
        currentPage: 1,       // 当前页码
        pageSize: 10,         // 分页显示条目
        total: 0,             // 表格记录总条数
        selectedRow: {},      // 表选中的行
        showItem: ['operate', 'strategy', 'count', 'user', 'handle', 'device', 'deviceFinger', 'session'],
        defaultTab: '',
        queryRules: {         // 查询条件表单校验
          txncode: [
            {max: 32, message: '长度在32个字符以内', trigger: 'blur'}
          ],
          userid: [
            {max: 32, message: '长度在32个字符以内', trigger: 'blur'}
          ],
          username: [
            {max: 32, message: '长度在32个字符以内', trigger: 'blur'}
          ],
          ipaddr: [
            {max: 16, message: '长度在16个字符以内', trigger: 'blur'}, // ip格式校验
            {validator: check.checkFormIp, trigger: 'blur'}
          ],
          deviceid: [
            {max: 32, message: '长度在32个字符以内', trigger: 'blur'}
          ],
          sessionid: [
            {max: 64, message: '长度在64个字符以内', trigger: 'blur'}
          ],
          txntime: [
            {validator: txnTimeCheck, trigger: 'blur'} // 时间范围输入校验
          ]
        }
      }
    },
    created () {
      let self = this
      let txntime = self.initDateRange()
      // txntime = [new Date('2018/1/5'), new Date('2018/1/6')]
      self.queryShowForm.txntime = txntime
      self.getTxnEventTableData({
        operate_time: txntime[0].getTime(),
        end_time: txntime[1].getTime(),
        pageindex: 1,
        pagesize: self.pageSize
      })
    },
    mounted: function () {
      this.$nextTick(function () {
        let self = this
        self.txnStatusList = dictCode.getCodeItems('tms.common.txnstatus')
        self.confirmRiskList = dictCode.getCodeItems('tms.confirmrisk.status')
        this.selTree()
        ajax.post({
          url: '/monitor/alarm/get_all_country',
          param: {},
          success: function (data) {
            self.countryCodeList = data.row
          }
        })
        ajax.post({
          url: '/rule/disposal',
          param: {},
          success: function (data) {
            self.disposalList = data.row
          }
        })
      })
    },
    watch: {
      'queryShowForm.txntime': function (val) {
        let startTime = ''
        let endTime = ''
        if (val && val.length === 2) {
          startTime = val[0].getTime()
          endTime = val[1].getTime()
        }
        this.queryShowForm.operate_time = startTime
        this.queryShowForm.end_time = endTime
      },
      'queryShowForm.countrycode': function (val) {
        let self = this
        ajax.post({
          url: '/monitor/alarm/get_region_by_country',
          param: {country: val},
          success: function (data) {
            self.regionCodeList = data.row
          }
        })
      },
      'queryShowForm.regioncode': function (val) {
        let self = this
        ajax.post({
          url: '/monitor/alarm/get_city_by_region',
          param: {region: val},
          success: function (data) {
            self.cityCodeList = data.row
          }
        })
      }
    },
    methods: {
      bindGridData (data) {
        this.tnxEventTableData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      handleSizeChange (val) {
        // console.log(`每页 ${val} 条`)
        this.currentPage = 1
        this.pageSize = val
        this.getTxnEventTableData()
      },
      handleCurrentChange (val) {
        this.currentPage = val
        this.getTxnEventTableData()
      },
      getTxnEventTableData (params) {
        let self = this
        let param = params || Object.assign({
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }, self.queryShowForm)
        ajax.post({
          url: '/query/tnxEvent/result',
          param: param,
          success: function (data) {
            if (data.success === true) {
              self.bindGridData(data)
            }
          },
          fail: function (rep) {
            console.error('请求操作时间列表失败', rep)
          },
          error: function (e) {
            console.error('请求操作时间列表异常', e)
          }
        })
      },
      searchData () {
        this.$refs['queryShowForm'].validate((valid) => {
          if (valid) {
            let params = Object.assign({
              pageindex: 1,
              pagesize: this.pageSize
            }, this.queryShowForm)
            this.getTxnEventTableData(params)
          } else {
            this.$message('查询条件不符合规则，请按提示重新输入')
            return false
          }
        })
      },
      initDateRange () {
        let start = new Date()
        start.setHours(new Date().getHours() - 1)
        let end = new Date()
        return [start, end]
      },
      renderDateTime (row, column, cellValue) {
        return util.renderDateTime(cellValue)
      },
      renderIsEval (row, column, cellValue) {
        switch (cellValue) {
          case '0':
            return '未评估'
          case '1':
            return '已评估'
          default:
            return ''
        }
      },
      renderIsModelRisk (row, column, cellValue) {
        switch (cellValue) {
          case '0':
            return '规则风险'
          case '1':
            return '模型风险'
          default:
            return ''
        }
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
      renderConfirmRisk (row, column, cellValue) {
        return dictCode.rendCode('tms.confirmrisk.status', cellValue)
      },
      renderTxnStatus (row, column, cellValue) {
        return dictCode.rendCode('tms.common.txnstatus', cellValue)
      },
      queryTxnInfo (row, defaultTab) {
        let self = this
        self.selectedRow = row
        self.defaultTab = defaultTab
        let num = 0
        let openTimer = window.setInterval(function () {
          if (defaultTab === self.defaultTab) {
            window.clearInterval(openTimer)
            self.$refs['txnDetail'].open()
          }
          if (num > 10) {
            self.$message('网络异常，请刷新后重试')
          }
          num++
        }, 100)
      },
      openTxnTypedialog () {
        this.txntypeDialogVisible = true
      },
      handleCheckChange (data, checked, indeterminate) {
        let checkedArr = this.$refs.tree.getCheckedNodes()
        let checkedStrArr = []
        let checkedStrKeyArr = []
        for (let item of checkedArr) {
          checkedStrArr.push(item.tab_desc)
          checkedStrKeyArr.push(item.tab_name)
        }
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
