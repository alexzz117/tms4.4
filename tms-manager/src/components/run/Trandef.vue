<template>
  <div>
    <trandefForm ref="infoForm"></trandefForm>
    <!-- Form -->
    <el-dialog title="新建交易" :visible.sync="addFormVisible">
      <trandefForm ref="addForm"></trandefForm>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="addFormVisible = false">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  // import check from '@/common/check'
  import TrandefForm from '@/components/run/TrandefForm'
  import ajax from '@/common/ajax'

  let vm = null

  export default {
    create () {
    },
    computed: {
      txnIdParent () { return this.txnId },
      isVisibilityParent () { return this.isVisibility }
    },
    data () {
      return {
        tabDisposalList: [],
        tabDisposalSelectedCached: [],
        modelUsedList: [{label: '不使用模型', value: '0'}, {label: '模型学习期', value: '1'}],
        channelList: [{label: '全渠道', value: '0'}],
        formReadonly: true,
        formLabelWidth: '120px',
        addFormVisible: false,
        rules: {
          a: ''
        }
      }
    },
    props: ['txnId', 'isVisibility'],
    mounted: function () {
      this.$nextTick(function () {
        vm = this
      })
    },
    watch: {
      addFormVisible: {
        handler: (val, oldVal) => {
          if (val === true) {
            console.info(vm.$refs.addForm)
          }
        }
      },
      txnId: {
        handler: (val, oldVal) => {
          if (vm.isVisibilityParent === true) {
            vm.initForm()
          }
        }
      },
      isVisibility: {
        handler: (val, oldVal) => {
          if (vm.isVisibilityParent === true) {
            vm.initForm()
          }
        }
      }
    },
    methods: {
      initForm () {
        var self = this
        var option = {
          url: '/trandef/edit_prepare',
          param: {
            tab_name: self.txnId
          },
          success: function (data) {
            console.info(data.infos)
            var info = data.infos
            var txnType = '0'
            if (info.txnid !== '' && info.txnid !== undefined) {
              txnType = '1'// 交易类型：交易
            }
            self.$refs.infoForm.tranDefForm = {
              parent_tab: info.parent_tab,
              tab_name: info.tab_name,
              modelused_o: '',
              traindate: info.traindate,
              op: '', // 操作的类型：add：添加；mod：编辑
              tab_desc: info.tab_desc, // 名称
              show_order: info.show_order, // 顺序
              txn_type: txnType, // 交易类型
              txnid: info.txnid, // 交易识别标识
              tab_disposal: info.tab_disposal, // 处置策略
              chann: info.chann, // 渠道名称
              txnffname: info.par_tab_desc, // 上级交易
              modelused: info.modelused, // 模型状态
              is_enable: info.is_enable + '' // 有效性
            }
            self.$refs.infoForm.channelList = data.channs
            self.getDisposal(data.infos.parent_tab)
            // editTxn(data.infos);·
          }
        }
        ajax.post(option)
      },
      getDisposal (txnId) {
        var self = this
        var option = {
          url: '/rule/disposal',
          param: {
            txn_id: txnId
          },
          success: function (data) {
            var disposalList = []
            var list = data.row
            for (var i in list) {
              var item = {
                code_key: list[i].dp_code,
                code_value: list[i].dp_name
              }
              disposalList.push(item)
            }
            self.$refs.infoForm.tabDisposalList = disposalList
          }
        }
        ajax.post(option)
      }
    },
    components: {
      TrandefForm
    }
  }
</script>

<style>
  .hidden {
    display: none;
  }

  .disabledFlag {
    color: #dcdfe6;
  }
</style>
