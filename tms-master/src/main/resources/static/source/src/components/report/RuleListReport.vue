<template>
  <div>
    <el-row>
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item :title="queryTitle" name="search">
          <el-form :inline="true" ref="searchForm" :model="searchForm" label-width="80px">
            <el-form-item label="交易名称:" prop="txnIds">
              <div @click="openTxnTypedialog" >
                <el-input v-model="searchForm.txnIds" class="alarm-event-query-form-item" auto-complete="off" readonly clearable></el-input>
              </div>
            </el-form-item>
            <el-form-item label="规则名称:" prop="ruleName">
              <el-input v-model="searchForm.ruleName" class="alarm-event-query-form-item" auto-complete="off" clearable></el-input>
            </el-form-item>
            <el-form-item label="报表类型">
              <el-select v-model="searchForm.reporttype" class="alarm-event-query-form-item" placeholder="请选择" @change="reportTypeChange" clearable>
                <el-option v-for="item in reportTypeList"
                           :key="item.value"
                           :label="item.label"
                           :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker v-model="searchForm.duraSeparator" clearable
                              :type="datePickerType"
                              :editable="false"
                              range-separator="至"
                              start-placeholder="开始日期"
                              end-placeholder="结束日期">
              </el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchFunc">搜索</el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>
        <el-collapse-item :title="chartTitle" name="chart">
          <div>
            <el-form :inline="true" ref="filterForm" :model="filterForm" label-width="80px">
              <el-form-item label="监控指标">
                <el-select v-model="filterForm.target" class="alarm-event-query-form-item" placeholder="请选择" @change="reportTypeChange">
                  <el-option v-for="item in targetList"
                             :key="item.value"
                             :label="item.label"
                             :value="item.value">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="排名设置">
                <el-select v-model="filterForm.tops" class="alarm-event-query-form-item" placeholder="请选择" @change="reportTypeChange" clearable>
                  <el-option v-for="item in topList"
                             :key="item.value"
                             :label="item.label"
                             :value="item.value">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="规则名称">
                <el-input></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchFunc">搜索</el-button>
              </el-form-item>
            </el-form>
            <div id="chartdiv" style="width: 100%;height:400px;"></div>
          </div>
        </el-collapse-item>
        <el-collapse-item :title="tableTitle" name="table">
          <template slot="title">
            <div style="margin:10px;text-align: left ">
              <span style="margin-right: 10px">交易报警信息列表</span>
              <el-button plain class="el-icon-plus" @click.stop="exportFunc">导出</el-button>
              <el-button plain class="el-icon-plus" @click.stop="printFunc">打印</el-button>
            </div>
          </template>
          <div>
            <el-table :data="tableData" style="width: 100%">
              <el-table-column prop="rulename" label="规则名称"/>
              <el-table-column prop="txnname" label="监控交易名称"/>
              <el-table-column prop="triggernum" label="交易执行次数"/>
              <el-table-column prop="hitnum" label="规则命中数"/>
              <el-table-column prop="hitrate" label="规则命中率"/>
              <el-table-column prop="triggerrate" label="交易执行占比"/>
              <el-table-column prop="hitnumrate" label="命中占比"/>
            </el-table>
          </div>
          <el-pagination style="margin-top: 10px; text-align: right;"
                         :current-page="currentPage"
                         @size-change="handleSizeChange"
                         @current-change="handleCurrentChange"
                         :page-sizes="[10, 25, 50, 100]"
                         :page-size="pageSize"
                         layout="total, sizes, prev, pager, next, jumper"
                         :total="total">
          </el-pagination>
        </el-collapse-item>
      </el-collapse>
    </el-row>
    <el-dialog title="交易名称" :visible.sync="txntypeDialogVisible" width="400px">
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
  </div>
</template>
<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import reportEcharts from '@/common/reportEcharts'
  import reportPrint from '@/common/reportPrint'
  let myChart // 图标对象

  export default {
    data () {
      return {
        datePickerType: 'daterange',
        txntypeDialogVisible: false,
        defaultTreeProps: {
          children: 'children',
          label: 'tab_desc'
        },
        expendKey: ['T'], // 默认展开的功能节点id
        treeData: [],
        activeNames: ['search', 'chart', 'table'],   // 显示的折叠面板
        queryTitle: '规则报警信息查询',
        chartTitle: '规则报警信息展示图',
        tableTitle: '规则报警信息列表',
        charData: [],
        tableData: [],        // 表格数据
        currentPage: 1,       // 当前页码
        pageSize: 10,         // 分页显示条目
        total: 0,             // 表格记录总条数
        reportTypeList: [{label: '日报', value: 'dayreport'}, {label: '月报', value: 'monthreport'}, {label: '年报', value: 'yearreport'}],
        targetList: [{label: '交易执行次数', value: 'TRIGGERNUM'}, {label: '规则命中数', value: 'HITNUM'}, {label: '规则命中率', value: 'HITRATE'}, {label: '交易执行占比', value: 'TRIGGERRATE'}, {label: '命中占比', value: 'HITNUMRATE'}],   // 监控指标下拉列表
        topList: [{label: '前5名', value: '5'}, {label: '前10名', value: '10'}, {label: '前20名', value: '20'}, {label: '前30名', value: '30'}], // 排名设置下拉列表
        searchForm: {
          txnIds: '',         // 交易名称
          ruleName: '',       // 规则名称
          reporttype: 'dayreport', // 报表类型
          duraSeparator: [],  // 日期范围
          yearDate: '',
          monthDate: '',
          startTime: '',      // 开始日期
          endTime: ''         // 结束日期
        },
        filterForm: {
          ruleNameSelect: [],
          target: 'TRIGGERNUM',
          tops: '10'
        }
      }
    },
    created () {
    },
    mounted: function () {
      this.$nextTick(function () {
        // this.searchForm.duraSeparator = [new Date(), new Date()]
        myChart = this.$echarts.init(document.getElementById('chartdiv'))
        this.selTree()
        this.selTable()
      })
    },
    watch: {},
    methods: {
      handleChange (val) {
      },
      bindGridData (data) {
        this.tableData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      handleSizeChange (val) {
        this.currentPage = 1
        this.pageSize = val
        this.selTable()
      },
      handleCurrentChange (val) {
        this.currentPage = val
        this.selTable()
      },
      selTable () {
        let self = this
        let params = Object.assign({pagesize: self.pageSize, pageindex: self.currentPage}, self.searchForm, self.filterForm)
        ajax.post({
          url: '/report/rule/list',
          loading: true,
          param: params,
          success: function (data) {
            self.bindGridData(data)
            self.getChart()
          }
        })
      },
      searchFunc () {
        this.selTable()
      },
      openTxnTypedialog () {
        this.txntypeDialogVisible = true
      },
      reportTypeChange (val) {
        let self = this
        self.searchForm.startTime = ''
        self.searchForm.endTime = ''
        self.searchForm.monthDate = ''
        self.searchForm.yearDate = ''
        self.searchForm.duraSeparator = null
        switch (val) {
          case 'dayreport' :
            self.datePickerType = 'daterange'
            break
          case 'monthreport' :
            self.datePickerType = 'month'
            break
          case 'yearreport' :
            self.datePickerType = 'year'
            break
          default :
            self.datePickerType = 'daterange'
        }
      },
      handleCheckChange (data, checked, indeterminate) {
        let checkedArr = this.$refs.tree.getCheckedNodes()
        let checkedStrArr = []
        let checkedStrKeyArr = []
        for (let item of checkedArr) {
          checkedStrArr.push(item.tab_desc)
          checkedStrKeyArr.push(item.tab_name)
        }
        this.searchForm.txnIds = checkedStrKeyArr.join(',')
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
      getChart () {
        let self = this
        ajax.post({
          url: '/report/rule/showChart',
          success: function (data) {
            if (data.chartdata) {
              self.charData = data.chartdata
            }
          }
        })
        // 使用刚指定的配置项和数据显示图表。
        let option = self.getOptionV()
        myChart.setOption(option)
      },
      getOptionV () {
        let chartData = this.charData
        let filterForm = this.filterForm
        var cfTarget = filterForm.target
        var cfTargetT = this.getTargetText()
        var gList = chartData
        var opLegendData = [] // 显示数据类型
        opLegendData.push(cfTargetT)
        var opXData = []  // xAxis显示列
        var opSeries = []  // 数据
        var gIndex = 1
        var opSeriesData = []
        for (var i = 0; i < gList.length; i++) {
          var key = gList[i]['rulename'] + '[' + gList[i]['txnname'] + ']'
          if ((gIndex) % 2 === 0) {
            opXData.push('\n' + key)
          } else {
            opXData.push(key)
          }
          opSeriesData.push(gList[i][cfTarget])
          gIndex++
        }
        opSeries.push({
          name: cfTargetT,
          itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
          type: 'bar',
          data: opSeriesData
        })

        var option = {}
        option.op_legend_data = opLegendData
        option.op_x_data = opXData
        option.op_series = opSeries
        option.chartID = 'chartdiv'
        option.magicType = ['line', 'bar']
        option.xAxis = [{
          type: 'category',
          axisLabel: {
            interval: 0, // 全部显示
            // textStyle: {
            //   color: '#fff'
            // },
            formatter: function (params) {
              var res = params
              var index = params.indexOf('[')
              if (index !== -1) {
                res = res.substring(0, index)
              }
              return res
            }
          },
          data: opXData
        }]
        return reportEcharts.GenOption(option)
      },
      getTargetText () {
        let resultList = []
        let valueList = this.filterForm.target
        let list = this.targetList
        for (let i in valueList) {
          let value = valueList[i]
          for (let j in list) {
            let item = list[j]
            if (value === item.value) {
              resultList.push(item.label)
              break
            }
          }
        }
        return resultList
      },
      exportFunc () {
        let params = Object.assign(this.searchForm,this.filterForm)
        console.info(params)
        let url = '/report/rule/export?' + util.serializeObj(params)
        ajax.syncGet({
          url: url
        })
        // window.location.href = url
      },
      printFunc () {
        let searchForm = this.searchForm
        let filterForm = this.filterForm
        let option = {}
        option.queryBox = {
          queryTitle: this.queryTitle, // 查询标题
          queryForm: [{         // 查询条件
            label: '交易名称',
            value: searchForm.txnIds
          },{         // 查询条件
            label: '规则名称',
            value: searchForm.ruleName
          }, {
            label: '报表类型',
            value: this.getReportTypeName(searchForm.reporttype)
          }, {
            label: '日期范围',
            value: this.getQueryDataRange()
          }]
        }
        option.chartBox = {
          chartTitle: this.chartTitle, // 图表标题
          chartForm: [{
            label: '监控指标',
            value: this.getTargetText()
          }, {
            label: '排名设置',
            value: filterForm.tops
          }, {
            label: '规则名称',
            value: filterForm.tops
          }],
          chartUrl: myChart.getDataURL()        // 图表地址
        }
        option.tablebBox = {
          tableTitle: this.tableTitle, // 表格标题
          columns: [],
          tableData: []        // 表格数据
        }
        let columns = [{name: '规则名称', dataIndex: 'rulename'},
          {name: '监控交易名称', dataIndex: 'txnname'},
          {name: '交易执行次数', dataIndex: 'triggernum'},
          {name: '规则命中数', dataIndex: 'hitnum'},
          {name: '规则命中率', dataIndex: 'hitrate'},
          {name: '交易执行占比', dataIndex: 'triggerrate'},
          {name: '命中占比', dataIndex: 'hitnumrate'}]
        option.tablebBox.columns = columns
        option.tablebBox.tableData = this.tableData
        reportPrint.ReportPrint(option)
      },
      getReportTypeName (value) {
        let list = this.reportTypeList
        for (let i in list) {
          let item = list[i]
          if (item.value === value) {
            return item.label
          }
        }
        return value
      },
      getQueryDataRange () {
        let searchForm = this.searchForm
        let duraSeparator = searchForm.duraSeparator
        if (duraSeparator && duraSeparator.length === 2) {
          return searchForm.startTime + '至' + searchForm.endTime
        } else {
          return ''
        }
      }
    }
  }
</script>
<style scoped>
  .alarm-event-query-form-item{
    width: 200px;
  }
</style>
