<template>
  <div>
    <transition name="fade">
      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm" :rules="queryRules"
               :inline="true" style="text-align: left" v-show="queryFormShow" >
        <el-form-item label="流水号:" prop="">
          <el-input class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="客户号:" prop="">
          <el-input class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="客户名称:" prop="">
          <el-input class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="IP地址:" prop="">
          <el-input class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="设备信息:" prop="">
          <el-input class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="会话标识:" prop="">
          <el-input class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="监控操作:" prop="">
          <el-input class="alarm-event-query-form-item" auto-complete="off" :maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="处置方式:" prop="">
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
        <el-form-item label="是否评估:" prop="">
          <el-select v-model="queryShowForm.aaa" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in isAssessmentList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>
        <el-form-item label="认证状态:" prop="">
          <el-select v-model="queryShowForm.aaa" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in isCorrectList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="人工确认风险:" prop="">
          <el-select v-model="queryShowForm.aaa" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in confirmRiskList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态:" prop="">
          <el-select v-model="queryShowForm.aaa" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in txnStatusList"
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
    <el-table
      :data="tnxEventTable"
      style="width: 100%">
      <el-table-column prop="" label="流水号" align="left"></el-table-column>
      <el-table-column prop="userid" label="客户号" align="left"></el-table-column>
      <el-table-column prop="" label="会话标识" align="left"></el-table-column>
      <el-table-column prop="txnname" label="监控操作" align="left"></el-table-column>
      <el-table-column prop="txntime" label="操作时间" align="left"></el-table-column>
      <el-table-column prop="" label="地理信息" align="left"></el-table-column>
      <el-table-column prop="" label="设备信息" align="left"></el-table-column>
      <el-table-column prop="" label="IP地址" align="left"></el-table-column>
      <el-table-column prop="" label="是否评估" align="left"></el-table-column>
      <el-table-column prop="" label="风险类型" align="left"></el-table-column>
      <el-table-column prop="" label="规则命中数" align="left"></el-table-column>
      <el-table-column prop="" label="风险分值" align="left"></el-table-column>
      <el-table-column prop="" label="认证状态" align="left"></el-table-column>
      <el-table-column prop="" label="处置方式" align="left"></el-table-column>
      <el-table-column prop="" label="人工确认风险" align="left"></el-table-column>
      <el-table-column prop="" label="操作状态" align="left"></el-table-column>
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
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import dictCode from '@/common/dictCode'
  import check from '@/common/check'
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
      return {
        tnxEventTable: [],    // 操作事件表数据
        queryFormShow: true,  // 查询条件表单显示控制
        queryShowForm: {      // 查询条件表单
          disposal: '',
          countrycode: '',
          regioncode: '',
          citycode: ''
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
        queryRules: {}        // 查询条件表单校验
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        let self = this
        console.log(dictCode.getCodeItems('tms.common.txnstatus'), dictCode.getCodeItems('tms.confirmrisk.status'))
        self.txnStatusList = dictCode.getCodeItems('tms.common.txnstatus')
        self.confirmRiskList = dictCode.getCodeItems('tms.confirmrisk.status')
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
      handleSizeChange () {

      },
      handleCurrentChange () {

      }
    }
  }
</script>

<style>
  .alarm-event-query-form-item{
    width: 200px;
  }
</style>
