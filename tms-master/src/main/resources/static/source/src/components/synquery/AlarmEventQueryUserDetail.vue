<template>
  <div>
    <div v-show="!hasUser">
      无信息记录
    </div>
    <div v-show="hasUser" class="info-item">
      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            用户ID:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.userid}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            用户名:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.username}}&nbsp
          </el-col>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            客户姓名:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.cusname}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            证件号码:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.certno}}&nbsp
          </el-col>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            证件类型:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.certtype|renderCerttype}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            手机号码:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.cellphoneno}}&nbsp
          </el-col>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            渠道用户开户行:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.opennode}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            开户时间:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.opentime|renderDateTime}}&nbsp
          </el-col>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            用户类型:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.acctype}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            性别:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.sex|renderSex}}&nbsp
          </el-col>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            出生日期:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.birthdate |renderDateTime}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            累计分值:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.sumscore}}&nbsp
          </el-col>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            条件类型:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.risktype}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            风险分值:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.riskscore}}&nbsp
          </el-col>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            连续无风险次数:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.norisknum}}&nbsp
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="9" tag="label" class="info-item-label">
            开户城市:
          </el-col>
          <el-col :span="15" tag="label" class="info-item-value">
            {{detailData.opennodecity}}&nbsp
          </el-col>
        </el-col>
      </el-row>

    </div>
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
            if (data.row && data.row.length > 0) {
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
  .info-item {
    border-left: thin solid;
    border-top: thin solid;
  }

  .info-item-label {
    padding: 0px 8px;
    text-align: left;
    border-right: thin solid;
    border-bottom: thin solid;
  }

  .info-item-value {
    padding: 0px 8px;
    text-align: left;
    border-right: thin solid;
    border-bottom: thin solid;
  }
</style>
