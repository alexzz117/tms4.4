<template>
  <div>
    <!-- 策略信息 -->
    <section class="table">
    <el-table
      :data="transHitRuleList"
      style="width: 100%">

      <el-table-column prop="rule_shortdesc" label="规则名称" align="left"></el-table-column>
      <el-table-column prop="rule_cond_in" label="规则条件" align="left"></el-table-column>
      <el-table-column label="风险类型" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.eval_type | renderEvalType}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rule_score" label="分值" align="left"></el-table-column>
      <el-table-column label="处置方式" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.disposal | renderDisposal}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="numtimes" label="执行耗时" align="left"></el-table-column>
    </el-table>
    </section>
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
        transHitRuleList: [],
        disposalList: []
      }
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
      renderIs (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.is', value)
      },
      renderEvalType (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.rule.evaltype', value)
      },
      renderDisposal (value) {
        if (value === undefined || value === '') {
          return ''
        }
        for (let item of alarmEventQueryStrategyDetailVm.disposalList) {
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
          url: '/rule/disposal',
          param: {},
          success: function (data) {
            self.disposalList = data.row
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
              }
            })
          }
        })
      }
    }
  }
</script>
<style>

</style>
