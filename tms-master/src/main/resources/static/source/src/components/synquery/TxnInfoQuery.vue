<template>
  <div>
    <!-- 操作信息 -->
    <el-row class="info-item">
      <el-col v-for="item in showFields" :key="item.fd_name" :span="12">
        <el-col :span="9" tag="label" class="info-item-label">
          {{item.name}}
        </el-col>
        <el-col :span="15" tag="label" class="info-item-value">
          {{item.fd_value}}&nbsp
        </el-col>
      </el-col>
    </el-row>
  </div>
</template>
<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import dictCode from '@/common/dictCode'

  export default {
    computed: {
      showItemParent () {
        return this.showItem
      }
    },
    data () {
      return {
        userDialogVisible: false,
        selectedRow: {},
        info: {},
        showFields: []
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
      }
    },
    props: ['showItem'],
    mounted: function () {
      this.$nextTick(function () {
      })
    },
    methods: {
      loadData (row) {
        this.selectedRow = row
        let self = this
        self.infoList = {}
        ajax.post({
          url: '/query/alarmEvent/operateDetail',
          loading: true,
          param: {
            txncode: row.txncode,
            txnTypeShowFields: row.txntype
          },
          success: function (data) {
            let info = data.row[0]
            let list = []
            let showFields = data.showfields
            let firstFieldNameList = ['TXNCODE', 'USERID', 'CREATETIME', 'FINISHTIME', 'TXNSTATUS', 'TXNID', 'TXNTYPE', 'ISRISK', 'DEVICEID', 'IPADDR',
              'COUNTRYCODE', 'REGIONCODE', 'CITYCODE', 'CHANCODE', 'M_NUM53', 'M_NUM23', 'SESSIONID', 'ISEVAL', 'HITRULENUM', 'TRIGRULENUM', 'DISPOSAL',
              'CONFIRMRISK', 'MODELID', 'PSSTATUS', 'BATCHNO']
            let firstFieldList = []
            for (let j in firstFieldNameList) {
              let fieldName = firstFieldNameList[j]
              for (let k in showFields) {
                let field = showFields[k]
                if (field.fd_name === fieldName) {
                  firstFieldList.push(field)
                  showFields.splice(Number(k), 1)
                  break
                }
              }
            }
            showFields = firstFieldList.concat(showFields)
            for (let i in showFields) {
              let item = showFields[i]
              let fdValue = ''
              switch (item.type) {
                case 'code' :
                  let code = item.code
                  let codeValue = info[item.fd_name.toLowerCase()]
                  if (codeValue) {
                    fdValue = dictCode.rendCode(code, codeValue)
                  }
                  break
                case 'datetime' :
                  fdValue = util.renderDateTime(info[item.fd_name.toLowerCase()])
                  break
                default :
                  fdValue = info[item.fd_name.toLowerCase()]
                  break
              }
              item.fd_value = fdValue
              list.push(item)
            }
            self.showFields = list
          }
        })
      },
      showUserDetail () {
        let self = this
        this.userDialogVisible = true
        setTimeout(function () {
          self.$refs.userDetail.loadData(self.selectedRow)
        }, 200)
      }
    }
  }
</script>
<style scoped>
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
