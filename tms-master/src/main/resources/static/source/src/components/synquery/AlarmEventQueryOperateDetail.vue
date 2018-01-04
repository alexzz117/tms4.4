<template>
  <div>
    <el-row>
      <el-col :span="6">是否进行风险评估:</el-col>
      <el-col :span="6">{{detailData.iseval}}</el-col>
      <el-col :span="6">规则触发数:</el-col>
      <el-col :span="6">{{detailData.trigrulenum}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">规则命中数:</el-col>
      <el-col :span="6">{{detailData.hitrulenum}}</el-col>
      <el-col :span="6">处置方式:</el-col>
      <el-col :span="6">{{detailData.disposal}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">报警是否正确:</el-col>
      <el-col :span="6">{{detailData.iscorrect}}</el-col>
      <el-col :span="6">IS_MAIN:</el-col>
      <el-col :span="6">{{detailData.is_main}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">风险人工确认:</el-col>
      <el-col :span="6">{{detailData.confirmrisk}}</el-col>
      <el-col :span="6">所属模型:</el-col>
      <el-col :span="6">{{detailData.modelid}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">是否模型风险:</el-col>
      <el-col :span="6">{{detailData.ismodelrisk}}</el-col>
      <el-col :span="6">是否有风险:</el-col>
      <el-col :span="6">{{detailData.isrisk}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">报警处理状态:</el-col>
      <el-col :span="6">{{detailData.psstatus}}</el-col>
      <el-col :span="6">策略标识:</el-col>
      <el-col :span="6">{{detailData.strategy}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">批次号:</el-col>
      <el-col :span="6">{{detailData.batchno}}</el-col>
      <el-col :span="6">操作流水号:</el-col>
      <el-col :span="6">{{detailData.txncode}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作确认时间:</el-col>
      <el-col :span="6">{{detailData.finishtime}}</el-col>
      <el-col :span="6">操作识别时间:</el-col>
      <el-col :span="6">{{detailData.createtime}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作状态:</el-col>
      <el-col :span="6">{{detailData.txnstatus}}</el-col>
      <el-col :span="6">用户标识:</el-col>
      <el-col :span="6">{{detailData.userid}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作标识:</el-col>
      <el-col :span="6">{{detailData.txnid}}</el-col>
      <el-col :span="6">互联网服务提供商:</el-col>
      <el-col :span="6">{{detailData.ispcode}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">设备相似度:</el-col>
      <el-col :span="6">{{detailData.m_num53}}</el-col>
      <el-col :span="6">设备信息:</el-col>
      <el-col :span="6">{{detailData.deviceid}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">操作时间:</el-col>
      <el-col :span="6">{{detailData.txntime}}</el-col>
      <el-col :span="6">操作类型:</el-col>
      <el-col :span="6">{{detailData.txntype}}</el-col>
    </el-row>
    <el-row>
      <el-col :span="6">IP地址:</el-col>
      <el-col :span="6">{{detailData.ipaddr}}</el-col>
      <el-col :span="6">风险分值:</el-col>
      <el-col :span="6">{{detailData.score}}</el-col>

    </el-row>

    <el-row>
      <el-col :span="6">会话标识:</el-col>
      <el-col :span="6">{{detailData.sessionid}}</el-col>
      <el-col :span="6">渠道代码:</el-col>
      <el-col :span="6">{{detailData.chancode}}</el-col>

    </el-row>

    <el-row>
      <el-col :span="6">设备用户关系:</el-col>
      <el-col :span="6">{{detailData.m_num23}}</el-col>
      <el-col :span="6">支付金额1:</el-col>
      <el-col :span="6">{{detailData.m_text10}}</el-col>

    </el-row>
    <el-row>
      <el-col :span="6">登录手机号码:</el-col>
      <el-col :span="6">{{detailData.m_text1}}</el-col>
      <el-col :span="6">地理位置:</el-col>
      <el-col :span="6">{{detailData.location}}</el-col>
    </el-row>
  </div>
</template>
<script>
  import ajax from "../../common/ajax";

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
    props: ['showItem'],
    mounted: function () {
      this.$nextTick(function () {
        let self = this
        ajax.post({
          url: '/query/alarmEvent/operateDetail',
          param: {txncode: this.showItemParent.txncode},
          success: function (data) {
            self.detailData = data.row
          }
        })
      })
    }
  }
</script>
<style>

</style>
