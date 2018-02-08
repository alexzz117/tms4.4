<template>
  <div>
    <el-row>
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item title="交易报警信息查询" name="search">
          <el-form :inline="true" ref="searchForm" :model="searchForm" label-width="80px">
            <el-form-item label="交易名称:" prop="txnIds">
              <div @click="openTxnTypedialog" >
                <el-input v-model="searchForm.txnIds" class="alarm-event-query-form-item" auto-complete="off" readonly clearable></el-input>
              </div>
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
            <el-form-item label="地理位置:" prop="countrycode">
              <el-select v-model="searchForm.countrycode" class="alarm-event-query-form-item" placeholder="请选择" clearable filterable>
                <el-option
                  v-for="item in countryCodeList"
                  :key="item.countrycode"
                  :label="item.countryname"
                  :value="item.countrycode">
                </el-option>
              </el-select>
              <el-select v-model="searchForm.regioncode" class="alarm-event-query-form-item" placeholder="请选择" clearable filterable>
                <el-option
                  v-for="item in regionCodeList"
                  :key="item.regioncode"
                  :label="item.regionname"
                  :value="item.regioncode">
                </el-option>
              </el-select>
              <el-select v-model="searchForm.citycode" class="alarm-event-query-form-item" placeholder="请选择" clearable filterable>
                <el-option
                  v-for="item in cityCodeList"
                  :key="item.citycode"
                  :label="item.cityname"
                  :value="item.citycode">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchFunc">搜索</el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>
        <el-collapse-item title="交易报警信息展示图" name="chart">
          <div>
            <el-form :inline="true" ref="filterForm" :model="filterForm" label-width="80px">
              <el-form-item label="处置类型">
                <el-select multiple v-model="filterForm.ps" class="alarm-event-query-form-item" placeholder="请选择">
                  <el-option v-for="item in tableColumns"
                             :key="item.dp_code"
                             :label="item.dp_name"
                             :value="item.dp_code">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="监控指标">
                <el-select multiple v-model="filterForm.target" class="alarm-event-query-form-item" placeholder="请选择" @change="reportTypeChange">
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
              <el-form-item>
                <el-button type="primary" @click="filterFunc">搜索</el-button>
              </el-form-item>
            </el-form>
            <div id="chartdiv" style="width: 100%;height:400px;"></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="交易报警信息列表" name="table">
          <template slot="title">
            <div style="margin:10px;text-align: left ">
              <span style="margin-right: 10px">交易报警信息列表</span>
              <el-button plain class="el-icon-plus" @click.stop="exportFunc">导出</el-button>
              <el-button plain class="el-icon-plus" @click.stop="printFunc">打印</el-button>
            </div>
          </template>
          <div>
            <el-table :data="tableData" style="width: 100%">
              <el-table-column prop="txnname" label="交易名称" width="150"/>
              <el-table-column prop="txnnumber" label="交易总数" width="150"/>
              <el-table-column v-for="column in tableColumns" :label="column.dp_name" :key="column.dp_code" align="center">
                <el-table-column
                  :prop="connectString(column.dp_code, '_rate')"
                  label="总数"
                  width="120">
                </el-table-column>
                <el-table-column
                  :prop="connectString(column.dp_code, '_fraudrate')"
                  label="欺诈数"
                  width="120">
                </el-table-column>
                <el-table-column
                  :prop="connectString(column.dp_code, '_nonfraudrate')"
                  label="非欺诈数"
                  width="120">
                </el-table-column>
              </el-table-column>
            </el-table>
          </div>
          <!--<el-pagination style="margin-top: 10px; text-align: right;"-->
                         <!--:current-page="currentPage"-->
                         <!--@size-change="handleSizeChange"-->
                         <!--@current-change="handleCurrentChange"-->
                         <!--:page-sizes="[10, 25, 50, 100]"-->
                         <!--:page-size="pageSize"-->
                         <!--layout="total, sizes, prev, pager, next, jumper"-->
                         <!--:total="total">-->
          <!--</el-pagination>-->
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
        activeNames: ['chart', 'table'],   // 显示的折叠面板
        tableColumns: [],     // 表格表头
        tableData: [],        // 表格数据
        currentPage: 1,       // 当前页码
        pageSize: 10,         // 分页显示条目
        total: 0,             // 表格记录总条数
        reportTypeList: [{label: '日报', value: 'dayreport'}, {label: '月报', value: 'monthreport'}, {label: '年报', value: 'yearreport'}],
        countryCodeList: [],  // 地理位置信息列表
        regionCodeList: [],   // 地理位置信息列表
        cityCodeList: [],     // 地理位置信息列表
        targetList: [{label: '总数', value: '_NUM'}, {label: '欺诈数', value: '_FRAUDNUMBER'}, {label: '非欺诈数', value: '_NONFRAUDNUMBER'}],   // 监控指标下拉列表
        topList: [{label: '前5名', value: '5'}, {label: '前10名', value: '10'}, {label: '前20名', value: '20'}, {label: '前30名', value: '30'}], // 排名设置下拉列表
        searchForm: {
          txnIds: '',         // 交易名称
          reporttype: 'dayreport', // 报表类型
          duraSeparator: [],  // 日期范围
          yearDate: '',
          monthDate: '',
          startTime: '',      // 开始日期
          endTime: '',        // 结束日期
          countrycode: '',    // 国家
          regioncode: '',     // 省份
          citycode: ''        // 城市
        },
        filterForm: {
          ps: ['PS01'],
          target: ['_NUM'],
          tops: '10'
        }
      }
    },
    created () {
    },
    mounted: function () {
      this.$nextTick(function () {
        let self = this
        ajax.post({
          url: '/report/txn/getCountry',
          param: {},
          success: function (data) {
            self.countryCodeList = data.countrylist
          }
        })
        // this.searchForm.duraSeparator = [new Date(), new Date()]
        myChart = this.$echarts.init(document.getElementById('chartdiv'))
        this.selTree()
        this.selTableColumn()
        this.selTable()
      })
    },
    watch: {
      'searchForm.duraSeparator': function (val) {
        let self = this
        let datePickerType = self.datePickerType
        switch (datePickerType) {
          case 'daterange':
            if (val && val.length === 2) {
              this.searchForm.startTime = util.renderDate(val[0].getTime())
              this.searchForm.endTime = util.renderDate(val[1].getTime())
            }
            break
          case 'month':
            let monthDate = new Date(val)
            let monthStr = monthDate.getMonth() + 1
            if (monthStr < 10) {
              monthStr = '0' + monthStr
            }
            this.searchForm.monthDate = monthDate.getFullYear().toString() + monthStr
            break
          case 'year':
            let yearDate = new Date(val)
            this.searchForm.yearDate = yearDate.getFullYear().toString()
            break
          default:
            break
        }
      },
      'searchForm.countrycode': function (val) {
        let self = this
        self.searchForm.regioncode = ''
        ajax.post({
          url: '/report/txn/getRegion',
          param: {countryCode: val},
          success: function (data) {
            self.regionCodeList = data.reglist
          }
        })
      },
      'searchForm.regioncode': function (val) {
        let self = this
        self.searchForm.citycode = ''
        ajax.post({
          url: '/report/txn/getCity',
          param: {regionCode: val},
          success: function (data) {
            self.cityCodeList = data.citylist
          }
        })
      }
    },
    methods: {
      handleChange (val) {
      },
      bindGridData (data) {
        this.tableData = data.page.list
        // this.currentPage = data.page.index
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
      selTableColumn () {
        let self = this
        ajax.post({
          url: '/report/txn/getPS',
          loading: true,
          params: {},
          success: function (data) {
            self.tableColumns = data.list
          }
        })
      },
      selTable () {
        let self = this
        let params = Object.assign({}, self.searchForm, self.filterForm)
        ajax.post({
          url: '/report/txn/list',
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
      connectString (code1, code2) {
        return (code1 + code2).toLowerCase()
      },
      filterFunc () {
        this.getChart()
      },
      getChart () {
        let self = this
        // 使用刚指定的配置项和数据显示图表。
        let option = self.getOptionV()
        myChart.setOption(option)
      },
      getOptionV () {
        let self = this
        let list = self.tableData
        // var cf_txn = chartForm.getItem('txn').val();
        // var cf_txn_t = chartForm.getItem('txn').getText();
        // 处置
        var cf_ps = self.filterForm.ps
        var cf_ps_t = self.getPsText(cf_ps)
        // 数据类型
        var cf_target = self.filterForm.target
        var cf_target_t = self.getTargetText(cf_target)
        // 排名
        var cf_tops = self.filterForm.tops

        var g_list = list
        var op_legend_data = [] // 显示数据类型
        var op_x_data = []      // xAxis显示列
        var op_series = []      // 数据
        // var cf_ps_t_arr = cf_ps_t.split(',')
        // var cf_ps_arr = cf_ps.split(',')
        // var cf_target_t_arr = cf_target_t.split(',')
        // var cf_target_arr = cf_target.split(',')
        var cf_ps_t_arr = cf_ps_t
        var cf_ps_arr = cf_ps
        var cf_target_t_arr = cf_target_t
        var cf_target_arr = cf_target
        for (var i = 0; i < cf_ps_t_arr.length; i++) {
          for (var j = 0; j < cf_target_t_arr.length; j++) {
            op_legend_data.push(cf_ps_t_arr[i] + '-' + cf_target_t_arr[j])
            op_series.push(
              { name: cf_ps_t_arr[i] + '-' + cf_target_t_arr[j],
                ps: cf_ps_arr[i] + cf_target_arr[j],
                itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                type: 'bar',
                // stack: '处置',
                data: []
                //        ,
                //        markLine : {
                //               data : markLine_data
                // }
              }
            )
          }
        }
        // 排序，从大到小
        g_list.sort(function (a, b) {
          var al = 0
          var bl = 0
          for (var i = 0; i < cf_ps_arr.length; i++) {
            // 如果包含总数，直接利用总是排序
            if (cf_target_arr.indexOf('_NUM') > -1) {
              al = al + parseInt(a[cf_ps_arr[i] + '_NUM'])
              bl = bl + parseInt(b[cf_ps_arr[i] + '_NUM'])
            } else {
              // 不包含总数，利用其他所有数据合排序
              for (var j = 0; j < cf_target_arr.length; j++) {
                al = al + parseInt(a[cf_ps_arr[i] + cf_target_arr[j]])
                bl = bl + parseInt(b[cf_ps_arr[i] + cf_target_arr[j]])
              }
            }
          }
          // alert(bl + '--' + al)
          return bl - al
        })
        var g_index = 1
        for (var i = 0; i < g_list.length; i++) {
          if (op_x_data.length >= cf_tops) {
            break
          } else {
            if (g_list[i]['txnid'] === 'REPORTTXNTOTAL') {
              continue
            }
            if ((g_index) % 2 === 0) {
              op_x_data.push('\n' + g_list[i]['txnname'])
            } else {
              op_x_data.push(g_list[i]['txnname'])
            }
            for (var j = 0; j < op_series.length; j++) {
              var list_v = g_list[i][op_series[j].ps.toLowerCase()]
              if (list_v != null) {
                op_series[j].data.push(list_v)
              }
            }
            g_index++
          }
        }
        var option = {}
        option.op_legend_data = op_legend_data
        option.op_x_data = op_x_data
        option.op_series = op_series
        option.chartID = 'chartdiv'
        return reportEcharts.GenOption(option)
      },
      getPsText () {
        let resultList = []
        let valueList = this.filterForm.ps
        let list = this.tableColumns
        for (let i in valueList) {
          let value = valueList[i]
          for (let j in list) {
            let item = list[j]
            if (value === item.dp_code) {
              resultList.push(item.dp_name)
              break
            }
          }
        }
        return resultList
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
        let params = this.searchForm
        console.info(params)
        let url = '/report/txn/export?' + util.serializeObj(params)
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
          queryTitle: '交易报警信息查询', // 查询标题
          queryForm: [{         // 查询条件
            label: '交易名称',
            value: searchForm.txnIds
          }, {
            label: '报表类型',
            value: this.getReportTypeName(searchForm.reporttype)
          }, {
            label: '日期范围',
            value: this.getQueryDataRange()
          }, {
            label: '地区名称',
            value: this.getLocation()
          }]
        }
        option.chartBox = {
          chartTitle: '交易报警信息展示图', // 图表标题
          chartForm: [{         // 图表筛选条件
            label: '处置类型',
            value: this.getPsText()
          }, {
            label: '监控指标',
            value: this.getTargetText()
          }, {
            label: '排名设置',
            value: filterForm.tops
          }],
          chartUrl: myChart.getDataURL()        // 图表地址
        }
        option.tablebBox = {
          tableTitle: '交易报警信息列表', // 表格标题
          columns: [],
          tableData: []        // 表格数据
        }
        let tableColumns = this.tableColumns
        let columns = [{name: '交易名称', dataIndex: 'txnname'},
          {name: '交易总数', dataIndex: 'txnnumber'}]
        for (let i in tableColumns) {
          let tableColumn = tableColumns[i]
          columns.push({            // 表格表头
            name: tableColumn.dp_name,
            dataIndex: null,
            items: [{name: '总数', dataIndex: this.connectString(tableColumn.dp_code, '_rate')},
              {name: '欺诈数', dataIndex: this.connectString(tableColumn.dp_code, '_fraudrate')},
              {name: '非欺诈数', dataIndex: this.connectString(tableColumn.dp_code, '_nonfraudrate')}]
          })
        }
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
      },
      getLocation () {
        let location = ''
        let searchForm = this.searchForm
        let countryCode = searchForm.countrycode
        if (countryCode) {
          let countryCodeList = this.countryCodeList
          let regionCode = searchForm.regioncode
          let countryName = regionCode
          for (let i in countryCodeList) {
            if (countryCodeList[i].countrycode === countryCode) {
              countryName = countryCodeList[i].countryname
            }
          }
          location += countryName
          if (regionCode) {
            let regionCodeList = this.regionCodeList
            let cityCode = searchForm.citycode
            let regionName = regionCode
            for (let i in regionCodeList) {
              if (regionCodeList[i].regioncode === regionCode) {
                regionName = regionCodeList[i].regionname
              }
            }
            location = location + '-' + regionName
            if (cityCode) {
              let cityCodeList = this.cityCodeList
              let cityName = cityCode
              for (let i in cityCodeList) {
                if (cityCodeList[i].citycode === cityCode) {
                  cityName = cityCodeList[i].cityname
                }
              }
              location = location + '-' + cityName
            }
          }
        }
        return location
      }
    }
  }
</script>
<style scoped>
  .alarm-event-query-form-item{
    width: 200px;
  }
</style>
