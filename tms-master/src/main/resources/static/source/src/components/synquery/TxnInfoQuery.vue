<template>
  <div>
    <!-- 操作信息 -->
    <el-row>
      <el-col v-for="item in showFields" :span="12">
        <el-col :span="10" tag="label" class="item-label">
          {{item.name}}:
        </el-col>
        <el-col :span="14" tag="label" class="item-value">
          {{item.fd_value}}
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
          param: {
            txncode: row.txncode,
            txnTypeShowFields: row.txntype
          },
          success: function (data) {
            let info = data.row[0]
            let list = []
            let showFields = data.showfields
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
<style>
  .item-label {
    text-align: right;
  }

  .item-value {
    text-align: left;
  }
</style>
