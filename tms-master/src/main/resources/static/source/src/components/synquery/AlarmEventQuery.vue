<template>
  <div>
    <transition name="fade">

      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
               :inline="true" style="text-align: left" v-show="queryFormShow" >

        <el-form-item label="流水号:" prop="txncode">
          <el-input v-model="queryShowForm.txncode" class="alarm-event-query-form-item" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="客户号:" prop="userid">
          <el-input v-model="queryShowForm.userid" class="alarm-event-query-form-item" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="客户名称:" prop="username">
          <el-input v-model="queryShowForm.username" class="alarm-event-query-form-item" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="设备标识:" prop="deviceid">
          <el-input v-model="queryShowForm.deviceid" class="alarm-event-query-form-item" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="IP地址:" prop="ipaddr">
          <el-input v-model="queryShowForm.ipaddr" class="alarm-event-query-form-item" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="监控交易:" prop="txntype">

          <el-select v-model="queryShowForm.txntype" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in txntypeList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="处置方式:" prop="disposal">
          <el-select v-model="queryShowForm.disposal" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in disposalList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="处置结果:" prop="iscorrect">
          <el-select v-model="queryShowForm.iscorrect" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in iscorrectList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="操作状态:" prop="txnstatus">
          <el-select v-model="queryShowForm.txnstatus" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in txnstatusList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="处理状态:" prop="psstatus">
          <el-select v-model="queryShowForm.psstatus" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in psstatusList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="处理状态:" prop="queryDate">
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
                     clearable>
            <el-option
              v-for="item in countrycodeList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>

          <el-select v-model="queryShowForm.regioncode" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in regioncodeList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>

          <el-select v-model="queryShowForm.citycode" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in citycodeList"
              :key="item.code_key"
              :label="item.code_value"
              :value="item.code_key">
            </el-option>

          </el-select>
        </el-form-item>
      </el-form>

    </transition>

    <div style="margin-bottom: 10px; text-align: left; height: 30px;">

      <div style="float:right">

        <el-date-picker
          v-model="queryShowForm.queryDate"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
        </el-date-picker>

        <el-button type="primary" @click="queryFormShow = !queryFormShow">更多</el-button>
        <el-button type="primary" @click="searchData">搜索</el-button>
      </div>
    </div>

    <el-table
      :data="tableData"
      style="width: 100%">

      <el-table-column prop="category_id" label="流水号" align="left"></el-table-column>
      <el-table-column prop="category_name" label="客户号" align="left"></el-table-column>
      <el-table-column prop="category_name" label="客户名称" align="left"></el-table-column>
      <el-table-column prop="category_name" label="监控交易" align="left"></el-table-column>
      <el-table-column prop="category_name" label="操作时间" align="left"></el-table-column>
      <el-table-column prop="category_name" label="操作状态" align="left"></el-table-column>
      <el-table-column prop="category_name" label="处置方式" align="left"></el-table-column>
      <el-table-column prop="category_name" label="处置结果" align="left"></el-table-column>
      <el-table-column prop="category_name" label="分派人员" align="left"></el-table-column>
      <el-table-column prop="category_name" label="分派时间" align="left"></el-table-column>
      <el-table-column prop="category_name" label="处理人员" align="left"></el-table-column>
      <el-table-column prop="category_name" label="处理时间" align="left"></el-table-column>
      <el-table-column prop="category_name" label="审核人员" align="left"></el-table-column>
      <el-table-column prop="category_name" label="审核时间" align="left"></el-table-column>
      <el-table-column prop="category_name" label="处理状态" align="left"></el-table-column>
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
  import ajax from "../../common/ajax";
  import util from "../../common/util";

  export default {
    data () {
      return {
        queryFormShow: false,
        queryShowForm: {
          txncode: '',
          userid: '',
          username: '',
          deviceid: '',
          ipaddr: '',
          txntype: '',
          disposal: '',
          iscorrect: '',
          txnstatus: '',
          psstatus: '',
          countrycode: '',
          regioncode: '',
          citycode: '',
          queryDate: [new Date().setHours(new Date().getHours() - 1), new Date()]
        },
        queryForm: {
          txncode: '',
          userid: '',
          username: '',
          deviceid: '',
          ipaddr: '',
          txntype: '',
          disposal: '',
          iscorrect: '',
          txnstatus: '',
          psstatus: '',
          countrycode: '',
          regioncode: '',
          citycode: '',
          queryDate: [new Date().setHours(new Date().getHours() - 1), new Date()]
        },
        dialogTitle: '',
        dictDialogVisible: false,
        formLabelWidth: '130px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectRow: {},
        tableData: [],
        // 下拉框
        txntypeList: [],
        disposalList: [],
        iscorrectList: [],
        txnstatusList: [],
        psstatusList: [],
        countrycodeList: [],
        regioncodeList: [],
        citycodeList: []
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.getData()
      })
    },
    methods: {
      searchData (formName) {
        console.log('searchData')
        Object.assign(this.queryForm, this.queryShowForm)
        this.getData()
      },
      getData () {
        // let self = this
        // let paramsObj = {
        //   pageindex: this.currentPage,
        //   pagesize: this.pageSize
        // }
        // let upperParams = util.toggleObjKey(this.queryForm, 'upper')
        // Object.assign(paramsObj, upperParams)
        // ajax.post({
        //   url: '/codedict/category/list',
        //   param: paramsObj,
        //   success: function (data) {
        //     if (data.page) {
        //       self.bindGridData(data)
        //     }
        //   }
        // })
      },
      bindGridData (data) {
        this.tableData = data.page.list
        this.currentPage = data.page.index
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
      }
    }
  }
</script>

<style>
  .alarm-event-query-form-item{
    width: 200px;
  }
</style>
