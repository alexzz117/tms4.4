<template>
  <div>
    <el-row>
      <el-col :span="6">是否进行风险评估:</el-col>
      <el-col :span="6">{{detailData.iseval|renderIs}}&nbsp</el-col>
      <el-col :span="6">规则触发数:</el-col>
      <el-col :span="6">{{detailData.trigrulenum}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">规则命中数:</el-col>
      <el-col :span="6">{{detailData.hitrulenum}}&nbsp</el-col>
      <el-col :span="6">处置方式:</el-col>
      <el-col :span="6">{{detailData.disposal}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">报警是否正确:</el-col>
      <el-col :span="6">{{detailData.iscorrect|renderIs}}&nbsp</el-col>
      <el-col :span="6">IS_MAIN:</el-col>
      <el-col :span="6">{{detailData.is_main}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">风险人工确认:</el-col>
      <el-col :span="6">{{detailData.confirmrisk | renderConfirmrisk}}&nbsp</el-col>
      <el-col :span="6">所属模型:</el-col>
      <el-col :span="6">{{detailData.modelid}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">是否模型风险:</el-col>
      <el-col :span="6">{{detailData.ismodelrisk|renderIs}}&nbsp</el-col>
      <el-col :span="6">是否有风险:</el-col>
      <el-col :span="6">{{detailData.isrisk|renderIs}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">报警处理状态:</el-col>
      <el-col :span="6">{{detailData.psstatus |renderPsStatus}}&nbsp</el-col>
      <el-col :span="6">策略标识:</el-col>
      <el-col :span="6">{{detailData.strategy}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">批次号:</el-col>
      <el-col :span="6">{{detailData.batchno}}&nbsp</el-col>
      <el-col :span="6">操作流水号:</el-col>
      <el-col :span="6">{{detailData.txncode}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作确认时间:</el-col>
      <el-col :span="6">{{detailData.finishtime|renderDateTime}}&nbsp</el-col>
      <el-col :span="6">操作识别时间:</el-col>
      <el-col :span="6">{{detailData.createtime|renderDateTime}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作状态:</el-col>
      <el-col :span="6">{{detailData.txnstatus|renderTxnstatus}}&nbsp</el-col>
      <el-col :span="6">用户标识:</el-col>
      <el-col :span="6">{{detailData.userid}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作标识:</el-col>
      <el-col :span="6">{{detailData.txnid}}&nbsp</el-col>
      <el-col :span="6">互联网服务提供商:</el-col>
      <el-col :span="6">{{detailData.ispcode}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">设备相似度:</el-col>
      <el-col :span="6">{{detailData.m_num53}}&nbsp</el-col>
      <el-col :span="6">设备信息:</el-col>
      <el-col :span="6">{{detailData.deviceid}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作时间:</el-col>
      <el-col :span="6">{{detailData.txntime | renderDateTime}}&nbsp</el-col>
      <el-col :span="6">操作类型:</el-col>
      <el-col :span="6">{{detailData.txntype}}&nbsp</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">IP地址:</el-col>
      <el-col :span="6">{{detailData.ipaddr}}&nbsp</el-col>
      <el-col :span="6">风险分值:</el-col>
      <el-col :span="6">{{detailData.score}}&nbsp</el-col>

    </el-row>

    <el-row>
      <el-col :span="6">会话标识:</el-col>
      <el-col :span="6">{{detailData.sessionid}}&nbsp</el-col>
      <el-col :span="6">渠道代码:</el-col>
      <el-col :span="6">{{detailData.chancode}}&nbsp</el-col>

    </el-row>

    <el-row>
      <el-col :span="6">设备用户关系:</el-col>
      <el-col :span="6">{{detailData.m_num23}}&nbsp</el-col>
      <el-col :span="6">支付金额1:</el-col>
      <el-col :span="6">{{detailData.m_text10}}&nbsp</el-col>

    </el-row>
    <el-row>
      <el-col :span="6">登录手机号码:</el-col>
      <el-col :span="6">{{detailData.m_text1}}&nbsp</el-col>
      <el-col :span="6">地理位置:</el-col>
      <el-col :span="6">{{detailData.location}}&nbsp</el-col>
    </el-row>
  </div>
</template>
<script>
  import ajax from "../../common/ajax";
  import util from "../../common/util";
  import dictCode from "../../common/dictCode";

  export default {
    computed: {
      showItemParent () {
        return this.showItem
      }
    },
    data () {
      return {
        detailData: {}
      }
    },
    filters: {
      renderDateTime (value) {
        return util.renderDateTime(value)
      },
      renderTxnstatus (value) {
        if (!value || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.common.txnstatus', value)
      },
      renderIs (value) {
        if (!value || value === '') {
          return ''
        }
        return dictCode.rendCode('common.is', value)
      },
      renderConfirmrisk (value) {
        if (!value || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.confirmrisk.status', value)
      },
      renderPsStatus (value) {
        if (!value || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.status', value)
      }
    },
    props: ['showItem'],
    mounted: function () {
      this.$nextTick(function () {
      })
    },
    methods: {
      loadData (row) {
        let self = this
        self.detailData = {}
        ajax.post({
          url: '/query/alarmEvent/operateDetail',
          param: {txncode: row.txncode},
          success: function (data) {
            self.detailData = data.row[0]
          }
        })
      }
    }
  }
</script>
<style>

</style>
