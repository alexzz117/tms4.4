<template>
  <div>
    <el-row>
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item title="交易运行效率查询" name="search">
          <el-form :inline="true" ref="searchForm" :model="searchForm" label-width="80px">
            <el-form-item label="交易名称:" prop="txnIds">
              <div @click="openTxnTypedialog" >
                <el-input v-model="searchForm.txnIds" class="alarm-event-query-form-item" auto-complete="off" readonly clearable></el-input>
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
            <el-form-item>
              <el-button type="primary" @click="searchFunc">查询</el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>
        <el-collapse-item title="交易运行效率展示图" name="chart">
          <div>
            <el-form :inline="true" ref="filterForm" :model="filterForm" label-width="80px">
              <el-form-item label="监控指标">
                <el-select  v-model="filterForm.target" class="alarm-event-query-form-item" placeholder="请选择" @change="reportTypeChange">
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
        <el-collapse-item title="交易运行效率列表" name="table">
          <template slot="title">
            <div style="margin:10px;text-align: left ">
              <span style="margin-right: 10px">交易运行效率列表</span>
              <el-button plain class="el-icon-plus" @click.stop="exportFunc">导出</el-button>
              <el-button plain class="el-icon-plus" @click.stop="printFunc">打印</el-button>
            </div>
          </template>
          <el-table
            :data="tableData"
            style="width: 100%">

            <el-table-column
              prop="txnname"
              label="交易名称"
              width="150"
              align="center">
            </el-table-column>
            <el-table-column label="风险评估" align="center">
              <el-table-column
                prop="risk_cfm_number"
                label="调用次数"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_cfm_runtime_avg"
                label="平均时间"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_cfm_runtime_max"
                label="最大时间"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_cfm_runtime_min"
                label="最小时间"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_cfm_tpm_avg"
                label="平均TPM"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_cfm_tpm_max"
                label="最大TPM"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_cfm_tpm_min"
                label="最小TPM"
                width="120"
                align="center">
              </el-table-column>
            </el-table-column>
            <el-table-column label="交易确认" align="center">
              <el-table-column
                prop="risk_eval_number"
                label="调用次数"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_eval_runtime_avg"
                label="平均时间"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_eval_runtime_max"
                label="最大时间"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_eval_runtime_min"
                label="最小时间"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_eval_tpm_avg"
                label="平均TPM"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_eval_tpm_max"
                label="最大TPM"
                width="120"
                align="center">
              </el-table-column>
              <el-table-column
                prop="risk_eval_tpm_min"
                label="最小TPM"
                width="120"
                align="center">
              </el-table-column>
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
  import dictCode from '@/common/dictCode'
  import check from '@/common/check'
  import reportEcharts from '@/common/reportEcharts'
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
        tableData: [],        // 表格数据
        currentPage: 1,       // 当前页码
        pageSize: 10,         // 分页显示条目
        total: 0,             // 表格记录总条数
        reportTypeList: [{label: '日报', value: 'dayreport'}, {label: '月报', value: 'monthreport'}, {label: '年报', value: 'yearreport'}],
        countryCodeList: [],  // 地理位置信息列表
        regionCodeList: [],   // 地理位置信息列表
        cityCodeList: [],     // 地理位置信息列表
        targetList: [{label: '调用次数', value: '_NUMBER'}, {label: '平均时间', value: '_RUNTIME_AVG'}, {label: '最大时间', value: '_RUNTIME_MAX'}, {label: '最小时间', value: '_RUNTIME_MIN'}
        , {label: '平均TPM', value: '_TPM_AVG'}
        , {label: '最大TPM', value: '_TPM_MAX'}
        , {label: '最小TPM', value: '_TPM_MIN'}],   // 监控指标下拉列表
        topList: [{label: '前5名', value: '5'}, {label: '前10名', value: '10'}, {label: '前20名', value: '20'}, {label: '前30名', value: '30'}], // 排名设置下拉列表
        searchForm: {
          txnIds: '',         // 交易名称
          reporttype: 'dayreport', // 报表类型
          duraSeparator: [new Date(), new Date()],  // 日期范围
          startTime: '',      // 开始日期
          endTime: '',        // 结束日期
        },
        filterForm: {
          target: '_NUMBER',
          tops: 10
        }
      }
    },
    created () {

    },
    mounted: function () {
      myChart = this.$echarts.init(document.getElementById('chartdiv'))
      this.selTree()
      this.selTable()
    },

    watch: {
      'searchForm.duraSeparator': function (val) {
        let self = this
        let datePickerType = self.datePickerType
        let startTime = ''
        let endTime = ''
        if (val && val.length === 2) {
          startTime = val[0].getTime()
          endTime = val[1].getTime()
        }
        this.searchForm.startTime = startTime
        this.searchForm.endTime = endTime
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
      selTable () {
        let self = this
        let params = Object.assign(self.searchForm, self.filterForm)

        console.log(params);


        ajax.post({
          url: '/report/txnTime/list',
          loading: true,
          param: params,
          success: function (data) {

            self.bindGridData(data);
            self.getChart();
          }
        })
      },
      exportFunc () {
        let params = this.searchForm
        console.info(params)
        let url = '/report/txnTime/export?' + util.serializeObj(params)
        ajax.syncGet({
          url: url
        })
        // window.location.href = url
      },
      printFunc () {
        alert('打印')
      },
      searchFunc () {
        let self = this
        let params = Object.assign(self.searchForm, self.filterForm)
        console.info(params)
        self.selTable()
      },
      filterFunc () {
        this.getChart()
      },
      getChart () {
        let self = this
        // 使用刚指定的配置项和数据显示图表。
        let option = self.getOptionV();
        myChart.setOption(option);
      },
      getOptionV () {
        let self = this
        let list = self.tableData
        // var cf_txn = chartForm.getItem('txn').val();
        // var cf_txn_t = chartForm.getItem('txn').getText();
        // 处置

        // 数据类型
        var cf_target = self.filterForm.target
        var cf_target_t = self.getTargetText(cf_target)

        // 排名
        var cf_tops = self.filterForm.tops

        var g_list = list

        console.log('g_list');
        console.log(g_list);


        var op_legend_data = [] // 显示数据类型
        var op_x_data = []      // xAxis显示列
        var op_series = []      // 数据

        var cf_target_t_arr = cf_target_t
        var cf_target_arr = [cf_target]

        for (var i = 0; i < 2; i++) {
          for (var j = 0; j < cf_target_t_arr.length; j++) {
            if (i == 0) {
              op_legend_data.push("风险评估");
            } else {
              op_legend_data.push("交易确认");
            }
            var series
            console.log('cf_target_arr[j]');
            console.log(cf_target_arr[j]);
            if (cf_target_arr[j] == "_NUMBER") {
              console.log('_NUMBER');

              if (i == 0) {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_cfm_number",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              } else {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_eval_number",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              }
            }

            if (cf_target_arr[j] == "_RUNTIME_AVG") {

              if (i == 0) {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_cfm_runtime_avg",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              } else {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_eval_runtime_avg",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              }
            }

            if (cf_target_arr[j] == "_RUNTIME_MAX") {

              if (i == 0) {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_cfm_runtime_max",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              } else {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_eval_runtime_max",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              }
            }

            if (cf_target_arr[j] == "_RUNTIME_MIN") {

              if (i == 0) {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_cfm_runtime_min",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              } else {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_eval_runtime_min",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              }
            }

            if (cf_target_arr[j] == "_TPM_AVG") {

              if (i == 0) {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_cfm_tpm_avg",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              } else {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_eval_tpm_avg",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              }
            }

            if (cf_target_arr[j] == "_TPM_MAX") {

              if (i == 0) {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_cfm_tpm_max",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              } else {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_eval_tpm_max",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              }
            }

            if (cf_target_arr[j] == "_TPM_MIN") {

              if (i == 0) {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_cfm_tpm_min",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              } else {

                series = { name: cf_target_t_arr[j],
                    ps: "risk_eval_tpm_min",
                    itemStyle: {normal: {label: {show: true, position: 'insideRight'}}},
                    type: 'bar',
                    // stack: '处置',
                    data: []
                }
              }
            }
            if (series != undefined) {
              op_series.push(series)
            }

          }
        }

        console.log('op_series');
        console.log(op_series);



        // // 排序，从大到小
        // g_list.sort(function (a, b) {
        //   var al = 0
        //   var bl = 0
        //   // 如果包含总数，直接利用总是排序
        //   if (cf_target_arr.indexOf('_NUMBER') > -1) {
        //     al = al + parseInt(a[cf_ps_arr[i] + '_NUMBER'])
        //     bl = bl + parseInt(b[cf_ps_arr[i] + '_NUMBER'])
        //   } else {
        //     // 不包含总数，利用其他所有数据合排序
        //     for (var j = 0; j < cf_target_arr.length; j++) {
        //       al = al + parseInt(a[cf_ps_arr[i] + cf_target_arr[j]])
        //       bl = bl + parseInt(b[cf_ps_arr[i] + cf_target_arr[j]])
        //     }
        //   }
        //   // alert(bl + '--' + al)
        //   return bl - al
        // })

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

        console.log('op_series')
        console.log(op_series)

        var option = {}
        option.op_legend_data = op_legend_data
        option.op_x_data = op_x_data
        option.op_series = op_series
        option.chartID = 'chartdiv'

        return reportEcharts.GenOption(option)
      },
      getTargetText () {
        let resultList = []
        //let valueList = this.filterForm.target
        resultList.push(this.filterForm.target);


        // let list = this.targetList
        // for (let i in valueList) {



        //   let value = valueList[i]
        //   for (let j in list) {
        //     let item = list[j]
        //     if (value === item.value) {
        //       resultList.push(item.label)
        //       break
        //     }
        //   }
        // }
        return resultList
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
      }
    }
  }
</script>
<style scoped>
  .alarm-event-query-form-item{
    width: 200px;
  }
</style>
