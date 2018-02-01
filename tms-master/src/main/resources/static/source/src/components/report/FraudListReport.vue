<template>
  <div>
    <el-row>
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item title="欺诈报警信息查询" name="search">
          <el-form :inline="true" ref="searchForm" :model="searchForm" label-width="80px">
            <el-form-item label="交易名称:" prop="txnids">
              <div @click="openTxnTypedialog" >
                <el-input v-model="searchForm.txnids" class="alarm-event-query-form-item" auto-complete="off" readonly ></el-input>
              </div>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker v-model="searchForm.duraSeparator"
                :type="datePickerType"
                :editable="false"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期">
              </el-date-picker>
            </el-form-item>
            <el-form-item label="地区名称:" prop="countrycode">
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
        <el-collapse-item title="欺诈报警信息展示图" name="chart">
          <el-form :inline="true" ref="searchForm" :model="searchForm" label-width="80px">
            <el-form-item label="欺诈类型:" prop="fdType">
              <div @click="openTxnTypedialog" >
                <el-input v-model="searchForm.txnids" class="alarm-event-query-form-item" auto-complete="off" readonly ></el-input>
              </div>
            </el-form-item>
            <el-form-item label="处置类型:" prop="ps">
              <div @click="openTxnTypedialog" >
                <el-input v-model="searchForm.txnids" class="alarm-event-query-form-item" auto-complete="off" readonly ></el-input>
              </div>
            </el-form-item>
          </el-form>
          <div id="myChart" :style="{width: '300px', height: '300px'}"></div>
        </el-collapse-item>
        <el-collapse-item title="欺诈报警信息列表" name="table">
          <el-table
            :data="tableData"
            style="width: 100%">
            <el-table-column
              prop="txnname"
              label="欺诈类型"
              width="150">
            </el-table-column>
            <el-table-column
              prop="txnnumber"
              label="放行"
              width="150">
            </el-table-column>
            <el-table-column
              prop="txnnumber"
              label="页面提醒"
              width="150">
            </el-table-column>
            <el-table-column
              prop="txnnumber"
              label="人工认证"
              width="150">
            </el-table-column>
            <el-table-column
              prop="txnnumber"
              label="人工审核"
              width="150">
            </el-table-column>
            <el-table-column
              prop="txnnumber"
              label="阻断"
              width="150">
            </el-table-column>
            <el-table-column
              prop="txnnumber"
              label="挂起"
              width="150">
            </el-table-column>
            <el-table-column
              prop="txnnumber"
              label="短信提醒"
              width="150">
            </el-table-column>
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
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>
<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import dictCode from '@/common/dictCode'
  import check from '@/common/check'

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
        tableColumns: [],     // 表格表头
        tableData: [],        // 表格数据
        currentPage: 1,       // 当前页码
        pageSize: 10,         // 分页显示条目
        total: 0,             // 表格记录总条数
        reportTypeList: [{label: '日报', value: 'dayreport'}, {label: '月报', value: 'monthreport'}, {label: '年报', value: 'yearreport'}],
        countryCodeList: [],  // 地理位置信息列表
        regionCodeList: [],   // 地理位置信息列表
        cityCodeList: [],     // 地理位置信息列表
        searchForm: {
          txnids: '',         // 交易名称
          duraSeparator: [new Date(), new Date()],  // 日期范围
          startTime: '',      // 开始日期
          endTime: '',        // 结束日期
          countrycode: '',    // 国家
          regioncode: '',     // 省份
          citycode: ''        // 城市
        },
        filterForm: {
          ps: 'PS01',
          target: '_NUM',
          tops: 10
        }
      }
    },
    created () {
      this.selTree()
      this.selTableColumn()
      this.selTable()
    },
    mounted: function () {
      this.$nextTick(function () {
        let self = this
        ajax.post({
          url: '/monitor/alarm/get_all_country',
          param: {},
          success: function (data) {
            self.countryCodeList = data.row
          }
        })
      })
    },
    watch: {
      'searchForm.duraSeparator': function (val) {
        let self = this
        let startTime = ''
        let endTime = ''
        if (val && val.length === 2) {
          startTime = val[0].getTime()
          endTime = val[1].getTime()
        }
        this.searchForm.startTime = startTime
        this.searchForm.endTime = endTime
      },
      'searchForm.countrycode': function (val) {
        let self = this
        ajax.post({
          url: '/monitor/alarm/get_region_by_country',
          param: {country: val},
          success: function (data) {
            self.regionCodeList = data.row
          }
        })
      },
      'searchForm.regioncode': function (val) {
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
      handleChange (val) {
        console.log(val)
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
          params: {},
          success: function (data) {
            self.tableColumns = data.list
          }
        })
      },
      selTable () {
        let self = this
        let params = Object.assign(self.searchForm, self.filterForm)
        ajax.post({
          url: '/report/fraud/list',
          params: params,
          success: function (data) {
            self.bindGridData(data)
            self.drawLine(data)
          }
        })
      },
      searchFunc () {
        let self = this
        let params = Object.assign(self.searchForm, self.filterForm)
        console.info(params)
        self.selTable()
      },
      openTxnTypedialog () {
        this.txntypeDialogVisible = true
      },
      reportTypeChange (val) {
        let self = this
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
        console.info(val)
      },
      handleCheckChange (data, checked, indeterminate) {
        let checkedArr = this.$refs.tree.getCheckedNodes()
        let checkedStrArr = []
        let checkedStrKeyArr = []
        for (let item of checkedArr) {
          checkedStrArr.push(item.tab_desc)
          checkedStrKeyArr.push(item.tab_name)
        }
        this.searchForm.txnids = checkedStrKeyArr.join(',')
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
      drawLine (data) {
        // 基于准备好的dom，初始化echarts实例
        let myChart = this.$echarts.init(document.getElementById('myChart'))
        myChart.showLoading({
          text: '正在努力的读取数据中...'
        })
        // 处置
        let cfPs = '1'
        let cfPsT = '1'
        // 数据类型
        // var cf_target = ['_FRAUDNUMBER'];
        // var cf_target_t = ['欺诈数'];
        // 欺诈列表
        let cfFdType = '1'
        let cfFdTypeT = '1'
        // 数据集合
        let gList = []
        if (data) {
          gList = data.slice(0)
        }
        // alert(cf_ps_t+'---'+cf_fdType_t);
        let opLegendData = [] // 显示数据类型
        let opXData = []  // xAxis显示列
        let opSeries = []  // 数据

        let cfPsTArr = cfPsT.split(',')
        let cfPsArr = cfPs.split(',')
        let cfTargetTArr = ['欺诈数']
        let cfTargetArr = ['_FRAUDNUMBER']

        opXData = cfFdTypeT.split(',')
        let cfFdTypeArr = cfFdType.split(',')
        for (let i = 0; i < opXData.length; i++) {
          if ((i + 1) % 2 === 0) {
            opXData[i] = '\n' + opXData[i]
          }
        };
        for (let i = 0; i < cfPsTArr.length; i++) {
          for (let j = 0; j < cfTargetTArr.length; j++) {
            opLegendData.push(cfPsTArr[i])
            opSeries.push(
              {
                // name:cf_ps_t_arr[i]+'-'+cf_target_t_arr[j],
                name: cfPsTArr[i],
                ps: cfPsArr[i] + cfTargetArr[j],
                itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                type: 'bar',
                // stack: '处置',
                data: []
              }
            )
          }
        }
        // 将数据放入chart的数据对象中
        for (let i = 0; i < gList.length; i++) {
          if (cfFdTypeArr.indexOf(gList[i]['FRAUDTYPE']) > -1) {
            for (var j = 0; j < opSeries.length; j++) {
              var listV = gList[i][opSeries[j].ps]
              if (listV !== null) {
                opSeries[j].data.push(listV)
              }
            }
          }
        }
        let option = {}
        option.op_legend_data = opLegendData
        option.op_x_data = opXData
        option.op_series = opSeries
        myChart.hideLoading()
        myChart.clear()
        myChart.setOption(option)
      }
    }
  }
</script>
<style>
  .alarm-event-query-form-item{
    width: 200px;
  }
</style>
