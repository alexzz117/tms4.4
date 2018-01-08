<template>
  <div>
    <div v-show="!hasUser">
      无信息记录
    </div>
    <div v-show="hasUser">
      <el-row>
        <el-col :span="6">用户ID:</el-col>
        <el-col :span="6">{{detailData.userid}}&nbsp</el-col>
        <el-col :span="6">用户名:</el-col>
        <el-col :span="6">{{detailData.username}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">客户姓名:</el-col>
        <el-col :span="6">{{detailData.cusname}}&nbsp</el-col>
        <el-col :span="6">证件号码:</el-col>
        <el-col :span="6">{{detailData.certno}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">证件类型:</el-col>
        <el-col :span="6">{{detailData.certtype|renderCerttype}}&nbsp</el-col>
        <el-col :span="6">手机号码:</el-col>
        <el-col :span="6">{{detailData.cellphoneno}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">渠道用户开户行:</el-col>
        <el-col :span="6">{{detailData.opennode}}&nbsp</el-col>
        <el-col :span="6">开户时间:</el-col>
        <el-col :span="6">{{detailData.opentime|renderDateTime}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">用户类型:</el-col>
        <el-col :span="6">{{detailData.acctype}}&nbsp</el-col>
        <el-col :span="6">性别:</el-col>
        <el-col :span="6">{{detailData.sex|renderSex}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">出生日期:</el-col>
        <el-col :span="6">{{detailData.birthdate |renderDateTime}}&nbsp</el-col>
        <el-col :span="6">累计分值:</el-col>
        <el-col :span="6">{{detailData.sumscore}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">条件类型:</el-col>
        <el-col :span="6">{{detailData.risktype}}&nbsp</el-col>
        <el-col :span="6">风险分值:</el-col>
        <el-col :span="6">{{detailData.riskscore}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">连续无风险次数:</el-col>
        <el-col :span="6">{{detailData.norisknum}}&nbsp</el-col>
        <el-col :span="6">开户城市:</el-col>
        <el-col :span="6">{{detailData.opennodecity}}&nbsp</el-col>
      </el-row>
    </div>
  </div>
</template>
<script>
  import ajax from "../../common/ajax";
  import util from "../../common/util";
  import dictCode from "../../common/dictCode";
  import AlarmEventQuery from './AlarmEventQuery'

  let alarmEventQueryStrategyDetailVm = null

  export default {
    computed: {
      showItemParent () {
        return this.showItem
      }
    },
    data () {
      return {
        detailData: {},
        hasUser: false
      }
    },
    filters: {
      renderDateTime (value) {
        return util.renderDateTime(value)
      },
      renderCerttype (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.certificate.type', value.toString())
      },
      renderIs (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.is', value)
      },
      renderSex (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.sex', value)
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
        console.log(row)
        self.transHitRuleList = []
        ajax.post({
          url: '/query/alarmEvent/userDetail',
          param: {
            userid: row.userid
          },
          success: function (data) {
            if (data.row) {
              self.hasUser = true
              self.detailData = data.row[0]
            } else {
              self.hasUser = false
            }
          }
        })
      }
    }
  }
</script>
<style>

</style>
