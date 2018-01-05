<template>
  <div>
    <el-table
      :data="transHitRuleList"
      style="width: 100%">

      <el-table-column prop="ps_id" label="处理主键" align="left"></el-table-column>
      <el-table-column prop="ps_info" label="处理信息" align="left"></el-table-column>
      <el-table-column prop="ps_opername" label="处理人员" align="left">
      </el-table-column>
      <el-table-column label="处理结果" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.ps_result | renderPsResult}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="ps_time" label="处理时间" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.ps_time | renderDateTime}}</span>
        </template>
      </el-table-column>
      <el-table-column label="处理类型" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.ps_type | renderPsType}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="txncode" label="操作流水号" align="left">
      </el-table-column>
    </el-table>

  </div>
</template>
<script>
  import ajax from "../../common/ajax";
  import util from "../../common/util";
  import dictCode from "../../common/dictCode";

  let alarmEventQueryStrategyDetailVm = null

  export default {
    computed: {
      showItemParent () {
        return this.showItem
      }
    },
    data () {
      return {
        strategyList: [],
        strategyRuleEvalList: [],
        transHitRuleList: []
      }
    },
    filters: {
      renderDateTime (value) {
        return util.renderDateTime(value)
      },
      renderPsResult (value) {
        if (!value || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.result', value)
      },
      renderIs (value) {
        if (!value || value === '') {
          return ''
        }
        return dictCode.rendCode('common.is', value)
      },
      renderPsType (value) {
        if (!value || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.type', value)
      },
      renderDisposal (value) {
        if (!value || value === '') {
          return ''
        }
        for (let item of alarmEventQueryStrategyDetailVm.disposalListParent) {
          if (item.dp_code === value) {
            return item.dp_name
          }
        }
        return ''
      }
    },
    props: ['showItem'],
    mounted: function () {
      this.$nextTick(function () {
        alarmEventQueryStrategyDetailVm = this
      })
    },
    methods: {
      loadData (row) {
        let self = this
        self.transHitRuleList = []
        ajax.post({
          url: '/alarmevent/alarmStrategy',
          param: {
            txncode: row.txncode,
            txntype: row.txntype,
            stid: row.strategy
          },
          success: function (data) {
            if (data.row) {
              self.strategyList = data.row
            }
            if (data.list) {
              self.strategyRuleEvalList = data.list
            }
            if (data.rulelist) {
              self.transHitRuleList = data.rulelist
            }
            console.log(self.transHitRuleList)
          }
        })
      }
    }
  }
</script>
<style>

</style>
