<template>
  <div>
    <!-- 处理信息 -->
    <section class="table">
    <el-table
      :data="tableData"
      style="width: 100%">

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

      <el-table-column prop="ps_opername" label="处理人员" align="left">
      </el-table-column>

      <el-table-column label="处理结果" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.ps_result | renderPsResult}}</span>
        </template>
      </el-table-column>

      <el-table-column prop="ps_info" label="处理信息" align="left"></el-table-column>

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
    name: 'alarmEventQueryHandleDetail',
    computed: {
      showItemParent () {
        return this.showItem
      }
    },
    data () {
      return {
        tableData: []
      }
    },
    filters: {
      renderDateTime (value) {
        return util.renderDateTime(value)
      },
      renderPsResult (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.result', value)
      },
      renderIs (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.is', value)
      },
      renderPsType (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.type', value)
      },
      renderDisposal (value) {
        if (value === undefined || value === '') {
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
          url: '/query/alarmEvent/handleDetail',
          param: {
            txncode: row.txncode
          },
          success: function (data) {
            if (data.row) {
              self.tableData = data.row
            }
          }
        })
      }
    }
  }
</script>
<style>

</style>
