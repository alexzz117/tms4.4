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

        <el-form-item label="监控操作:" prop="txntypeShow">
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
        :data="tableData"
        style="width: 100%" tooltip-effect="dark">
        <!--<el-table-column type="selection" width="40" align="left" />-->
        <el-table-column fixed="left" label="操 作" width="50" alert="center">
          <template slot-scope="scope">
            <el-button type="text" @click="openDialog(scope.row)" size="mini" icon="el-icon-sort" title="分派"/>
          </template>
        </el-table-column>
        <el-table-column prop="txncode" label="流水号" align="left">
          <template slot-scope="scope">
            <a href="javascript:void(0)" @click="showAlarmEventInfo(scope.row)">{{scope.row.txncode}}</a>
          </template>
        </el-table-column>
        <el-table-column prop="userid" label="客户号" width="150" align="left"/>
        <el-table-column prop="username" label="客户名称" width="120" align="left"/>
        <el-table-column prop="txntime" label="交易时间" width="150" align="left" :formatter="formatter"/>
        <el-table-column prop="txnname" label="监控操作" width="100" align="left"/>
        <el-table-column prop="disposal" label="处置结果" width="140" align="left"/>
        <el-table-column prop="psstatus" label="处理状态" width="80" :formatter="formatter"/>
        <el-table-column prop="oper_name" label="处理人" width="100"/>
      </el-table>

      <el-pagination style="margin-top: 10px; text-align: right;" background
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     :current-page="currentPage"
                     :page-sizes="[10, 25, 50, 100]"
                     :page-size="pageSize"
                     layout="total, sizes, prev, pager, next, jumper"
                     :total="total">
      </el-pagination>

    </section>

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

    <txn-detail ref="txnDetail" :txn="selectedRow"></txn-detail>

    <!-- 选择人员弹窗 -->
    <el-dialog title="报警事件分派" :visible.sync="listDialogVisible">
      <div>
        <el-table
          :data="userData" height="280">
          <el-table-column prop="login_name" label="登录名" align="left"/>
          <el-table-column prop="assign_number" label="总量" width="100" align="left"/>
          <el-table-column prop="process_number" label="已处理" width="100" align="left"/>
          <el-table-column prop="unprocess_number" label="未处理" width="100" align="left"/>
        </el-table>
        <div class="dialog-footer" align="center" slot="footer">
          <el-button data="cancelBtn" icon="el-icon-success" type="primary" @click="onSaveAssign" :disabled="btnStatus">
            确 定
          </el-button>
          <el-button data="cancelBtn" icon="el-icon-arrow-left" type="primary" @click="listDialogVisible = false">返 回
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import ajax from "../../common/ajax";
  import util from "../../common/util";
  import dictCode from "../../common/dictCode";
  import check from "../../common/check";

  import TxnDetail from '@/components/synquery/TxnDetail'

  let iscorrectList = [{'label': '未认证', 'value': '2'}, {'label': '认证通过', 'value': '1'}, {'label': '认证未通过', 'value': '0'}]

  export default {
    name: 'alarmEventQuery',
    data () {
      return {
        listDialogVisible: false,
        txntypeDialogVisible: false,
        infoDialogVisible: false,
        btnStatus: false,
        defaultTreeProps: {
          children: 'children',
          label: 'tab_desc'
        },
        expendKey: ['T'], // 默认展开的功能节点id
        treeData: [],
        userData: [],
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
        dialogTitle: '',
        dictDialogVisible: false,
        formLabelWidth: '130px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectedRow: {},
        tabActiveName: 'operate',
        tableData: [],
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
      renderDateTime (value) {
        return util.renderDateTime(value)
      },
      renderTxnstatus (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.common.txnstatus', value)
      },
      renderIscorrect (value) {
        for (let item of iscorrectList) {
          if (item.value === value) {
            return item.label
          }
        }
        return ''
      },
      renderPsStatus (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.status', value)
      }
    },
    methods: {
      initDateRange () {
        let start = new Date()
        start.setHours(new Date().getHours() - 1)
        let end = new Date()
        return [start, end]
      },
      formatter(row, column, cellValue) {
        //GRID数据列表值转换
        switch (column.property) {
          case 'psstatus':
            return dictCode.rendCode('tms.alarm.process.status', cellValue)
            break
          case 'txntime':
            return dictCode.rendDatetime(cellValue)
            break
          default:
            return cellValue
            break
        }
      },
      initSelectData () {
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
      searchData (formName) {
        Object.assign(this.queryForm, this.queryShowForm)
        this.getData()
      },
      getData () {
        let self = this
        let paramsObj = {
          modelType: 'alarmEventSend',
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
      bindGridData (data) {
        this.tableData = data.page.list
        // 加上这个会出现页码之间来回跳的问题
        // this.currentPage = data.page.index
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
      showAlarmEventInfo (row) {
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
      handleInfoOpen () {

      },

      setQueryShowForm (queryShowForm) {
        this.queryShowForm = queryShowForm
      },
      //分派按钮弹出报警事件人员窗体
      openDialog(row) {
        var self = this
        this.listDialogVisible = true
        var param = {
          txn_code: row.txncode
        }
        ajax.post({
          url: '/alarmevent/assign',
          param: param,
          success: function (data) {
            if (data.list) {
              // self.userData = data.list
              let txn = data['txnmap']
              let opers = data['list']
              let operId = null
              if (txn) {
                let psStatus = txn['psstatus']
                let timeout = txn['timeout'] * 1 === 1
                operId = txn['operid']
                if (!(psStatus === '00' || psStatus === '02' || psStatus === '04')) {
                  self.btnStatus = true
                  // disableView(assignForm);
                }
              }
              if (opers) {
                let _list_ = []
                let items = []
                for (var i = 0, len = opers.length; i < len; i++) {
                  var row = opers[i]
                  if (row['isenable'] * 1 === 1) {
                    _list_.push(row)
                    items.push({text: row['login_name'], value: row['operator_id']})
                  } else {
                    if (row['operator_id'] === operId) {
                      // assignForm.getItem('OLD_OPERID').val(row['LOGIN_NAME'])
                    }
                  }
                }
                self.userData = _list_
                // operStatGrid.renderPage({list:_list_});
                // if(!IsEmpty(assignForm.getItem('OLD_OPERID').val())){
                //   assignForm.getItem('OLD_OPERID').jqDom.remove();
                // }
                // assignForm.getItem('OPERID').component.reload(items);
              }
            }
          }
        })
      },
      onSaveAssign () {

      }
    },
    components: {
      'txn-detail': TxnDetail
    }
  }
</script>

<style scoped>
  .alarm-event-query-form-item{
    width: 200px;
  }
</style>
