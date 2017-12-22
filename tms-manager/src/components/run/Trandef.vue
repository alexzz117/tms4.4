<template>
  <div>
    <trandefForm ref="infoForm"></trandefForm>
    <div>
      <el-button type="primary" @click="submitForm('edit')" :disabled="editVisible">保存</el-button>
    </div>
    <!-- Form -->
    <el-dialog title="新建交易" :visible.sync="addFormVisible" @open="resetForm" @close="resetValidate">
      <trandefForm ref="addForm"></trandefForm>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('add')">确 定</el-button>
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
        formLabelWidth: '120px',
        editVisible: true,
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
        self.$refs['infoForm'].$refs['tranDefForm'].clearValidate()
        if (self.txnId === '') {
          return
        }
        var option = {
          url: '/trandef/edit_prepare',
          param: {
            tab_name: self.txnId
          },
          success: function (data) {
            var info = data.infos
            if (info.tab_name && info.tab_name !== '' && info.tab_name !== 'T') {
              self.editVisible = false
            } else {
              self.editVisible = true
            }
            var txnType = '0'
            if (info.txnid !== '' && info.txnid !== undefined) {
              txnType = '1'// 交易类型：交易
            }
            self.$refs['infoForm'].tranDefForm = {
              parent_tab: info.parent_tab,
              tab_name: info.tab_name,
              modelused_o: info.modelused,
              traindate: info.traindate,
              op: 'mod', // 操作的类型：add：添加；mod：编辑
              tab_desc: info.tab_desc, // 名称
              show_order: info.show_order, // 顺序
              txn_type: txnType, // 交易类型
              txnid: info.txnid, // 交易识别标识
              tab_disposal: info.tab_disposal, // 处置策略
              chann: info.chann, // 渠道名称
              par_chann: info.par_chann,
              txnffname: info.par_tab_desc, // 上级交易
              modelused: info.modelused, // 模型状态
              is_enable: info.is_enable + '' // 有效性
            }
            self.$refs['infoForm'].channelList = data.channs // 渠道列表
            self.getDisposal(data.infos.parent_tab, 'infoForm') // 获取处置策略列表 'infoForm' 编辑表单标识
          }
        }
        ajax.post(option)
      },
      resetValidate () {
        this.$refs['addForm'].$refs['tranDefForm'].clearValidate()
      },
      resetForm () {
        var self = this
        var nodeInfo = self.$refs['infoForm'].tranDefForm
        var timer = window.setInterval(function () { // 定时器，延时取弹窗的表单
          if (self.$refs['addForm']) {
            window.clearInterval(timer) // 关闭定时器
            var parChann = nodeInfo.chann ? nodeInfo.chann : ''
            self.$refs['addForm'].channelList = self.$refs['infoForm'].channelList // 同步渠道名称列表（编辑页面的渠道）
            self.getDisposal(nodeInfo.tab_name, 'addForm') // 获取处置策略列表 'addForm' 新建表单标识
            self.$refs['addForm'].tranDefForm = { // 初始化表单值
              parent_tab: nodeInfo.tab_name,
              tab_name: '',
              modelused_o: '',
              traindate: '',
              op: 'add', // 操作的类型：add：添加；mod：编辑
              tab_desc: '', // 名称
              show_order: '0', // 顺序
              txn_type: '0', // 交易类型
              txnid: '', // 交易识别标识
              tab_disposal: '', // 处置策略
              chann: parChann, // 渠道名称
              par_chann: parChann,
              txnffname: nodeInfo.tab_desc, // 上级交易
              modelused: '0', // 模型状态
              is_enable: '0' // 有效性
            }
          }
        }, 100) // 每隔100秒执行一次
      },
      getDisposal (txnId, formId) {
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
            self.$refs[formId].tabDisposalList = disposalList
          }
        }
        ajax.post(option)
      },
      submitForm (op) { // 提交表单；op：操作方式；
        var self = this
        var params = {}
        if (op === 'add') {
          params = self.$refs['addForm'].tranDefForm
        } else {
          params = self.$refs['infoForm'].tranDefForm
        }
        var option = {
          url: '/trandef/save',
          param: params,
          success: function (data) {
            if (data.success) {
              if (op === 'add') {
                self.$message('添加成功')
                self.addFormVisible = false // 关闭弹窗
              } else {
                self.$message('修改成功')
              }
              self.reloadPage()
            }
          }
        }
        ajax.post(option)
      },
      delInfo (param) {
        var self = this
        this.$confirm('确定要删除交易吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          var option = {
            url: '/trandef/save',
            param: param,
            success: function (data) {
              if (data.success) {
                self.$message('删除交易成功')
                self.reloadPage()
              }
            }
          }
          ajax.post(option)
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
      },
      reloadPage () {
        window.location.reload()
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
