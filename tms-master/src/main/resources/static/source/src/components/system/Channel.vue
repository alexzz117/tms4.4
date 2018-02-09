<template>
  <div>
    <el-row>
      <el-col :span="24">
        <div class="toolbar">
          <div style="float: left ">
            <el-button plain class="el-icon-plus" @click="addChannel">新建</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
    <section class="table">
      <el-table :data="channelData">
        <el-table-column label="操作" width="80px">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" title="编辑" @click="editChannel(scope.row)"></el-button>
            <el-button type="text" icon="el-icon-delete" title="删除" @click="delChannel(scope.row)"></el-button>
          </template>
        </el-table-column>
        <el-table-column prop="channelid" label="渠道编号" align="left"></el-table-column>
        <el-table-column prop="channelname" label="渠道名称" align="left"></el-table-column>
        <el-table-column prop="orderby" label="顺序" align="left"></el-table-column>
      </el-table>
    </section>
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" :close-on-click-modal="false" width="500px" @close="resetForm">
      <el-form :model="channelForm" :rules="rules" ref="channelForm" label-width="120px">
        <el-form-item label="渠道编号" prop="channelid">
          <el-input v-model="channelForm.channelid" auto-complete="off" :disabled="!addFlag" clearable>
          </el-input>
        </el-form-item>
        <el-form-item label="渠道名称" prop="channelname">
          <el-input v-model="channelForm.channelname" auto-complete="off" clearable></el-input>
        </el-form-item>
        <el-form-item label="顺序" prop="orderby">
          <el-input v-model="channelForm.orderby" auto-complete="off" clearable></el-input>
        </el-form-item>
        <div>
          <el-form-item label="">
            <el-button type="primary" @click="submitFunc" size="large">保 存</el-button>
            <el-button @click="dialogVisible = false" size="large">取 消</el-button>
          </el-form-item>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>
<script>
  import ajax from '@/common/ajax'
  import check from '@/common/check'

  export default {
    data () {
      // 渠道编号重复性校验
      let ChannelIdExist = (rule, value, callback) => {
        let dataList = this.channelData
        let op = this.channelForm.op
        let channelid = value
        if (op === 'add') {
          for (let i in dataList) {
            let item = dataList[i]
            if (item.channelid === channelid) {
              return callback(new Error('渠道编号已被占用'))
            }
          }
        }
        callback()
      }
      // 渠道编号重复性校验
      let ChannelNameExist = (rule, value, callback) => {
        let dataList = this.channelData
        let op = this.channelForm.op
        let channelid = this.channelForm.channelid
        let channelname = value
        if (op === 'add') {
          for (let i in dataList) {
            let item = dataList[i]
            if (item.channelname === channelname) {
              return callback(new Error('渠道名称已被占用'))
            }
          }
        } else {
          for (let i in dataList) {
            let item = dataList[i]
            if (item.channelname === channelname && item.channelid !== channelid) {
              return callback(new Error('渠道名称已被占用'))
            }
          }
        }
        callback()
      }
      // 渠道编号重复性校验
      let OrderExist = (rule, value, callback) => {
        if (!(/^(0|[1-9]\d{0,3})$/.test(value))) {
          return callback(new Error('[顺序]必须为小于10000的正整数'))
        }
        let dataList = this.channelData
        let op = this.channelForm.op
        let channelid = this.channelForm.channelid
        let orderby = Number(value)
        if (op === 'add') {
          for (let i in dataList) {
            let item = dataList[i]
            if (item.orderby === orderby) {
              return callback(new Error('顺序已被占用'))
            }
          }
        } else {
          for (let i in dataList) {
            let item = dataList[i]
            if (item.orderby === orderby && item.channelid !== channelid) {
              return callback(new Error('顺序已被占用'))
            }
          }
        }
        callback()
      }
      return {
        channelData: [],
        dialogVisible: false,
        dialogTitle: '',
        channelForm: {
          op: '',
          channelid: '',
          channelname: '',
          orderby: ''
        },
        rules: {
          channelid: [
            {required: true, message: '请输入渠道编号', trigger: 'blur'},
            {max: 32, message: '长度在32个字符以内', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'},  // 渠道编号格式校验
            {validator: ChannelIdExist, trigger: 'blur'}   // 渠道编号重复校验
          ],
          channelname: [
            {required: true, message: '请输入渠道名称', trigger: 'blur'},
            {max: 32, message: '长度在32个字符以内', trigger: 'blur'},
            {validator: check.checkFormZhSpecialCharacter, trigger: 'blur'},  // 渠道名称格式校验
            {validator: ChannelNameExist, trigger: 'blur'}   // 渠道名称重复校验
          ],
          orderby: [
            {required: true, message: '请输入顺序', trigger: 'blur'},
            {validator: OrderExist, trigger: 'blur'} // 顺序校验
          ]
        }
      }
    },
    computed: {
      addFlag () {
        return this.channelForm.op === 'add'
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.selChannel()
      })
    },
    methods: {
      selChannel () {
        let self = this
        ajax.post({
          url: '/channel/list',
          loading: true,
          // param: {},
          success: function (data) {
            self.channelData = data.list
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      resetForm () {
        this.$refs['channelForm'].resetFields()
      },
      addChannel () {
        this.dialogVisible = true
        this.dialogTitle = '新建渠道'
        let order = this.getChannelOrder()
        let channelid = this.makeChannelId(order)
        this.channelForm = {
          op: 'add',
          channelid: channelid,
          channelname: '',
          orderby: order
        }
        this.rules.channelid[0].required = true
      },
      editChannel (row) {
        let self = this
        self.dialogVisible = true
        self.dialogTitle = '编辑渠道'
        self.channelForm.op = 'mod'
        self.rules.channelid[0].required = false
        ajax.post({
          url: '/channel/getChannel',
          loading: true,
          param: {channelid: row.channelid},
          success: function (data) {
            self.channelForm = data.info
          }
        })
      },
      submitFunc () {
        var self = this
        this.$refs['channelForm'].validate((valid) => {
          if (valid) {
            let url = ''
            let opStr = ''
            if (self.channelForm.op === 'add') {
              url = '/channel/add'
              opStr = '创建'
            } else {
              url = '/channel/mod'
              opStr = '编辑'
            }
            ajax.post({
              url: url,
              loading: true,
              param: self.channelForm,
              success: function (data) {
                switch (data.result) {
                  case 'success':
                    self.$message.success(opStr + '成功')
                    self.dialogVisible = false
                    break
                  case 'failed':
                    self.$message.success(opStr + '失败')
                    self.dialogVisible = false
                    break
                  case 'has':
                    self.$message.success('属性重复')
                    break
                  case 'error':
                    self.$message.success(opStr + '异常')
                    self.dialogVisible = false
                    break
                  default:
                    self.$message.success(opStr + '异常')
                    self.dialogVisible = false
                }
                self.selChannel()
              },
              fail: function (e) {
                if (e.response.data.message) {
                  self.$message.error(e.response.data.message)
                }
              }
            })
          }
        })
      },
      delChannel (row) {
        var self = this
        this.$confirm('确定删除', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/channel/del',
            loading: true,
            param: {channelid: row.channelid},
            success: function (data) {
              switch (data.result) {
                case 'sucess':
                  self.$message.success('删除成功')
                  break
                case 'has':
                  self.$message.info('渠道被交易使用，不允许删除')
                  break
                case 'failed':
                  self.$message.error('删除失败')
                  break
                default :
                  self.$message.error('删除异常')
              }

              self.selChannel()
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }).catch(() => {})
      },
      getChannelInfo (params) {
        let dataList = this.channelData
        let result = []
        for (let i in dataList) {
          let item = dataList[i]
          if (params.op === 'add') {
            if (item.channelid === params.channelid || item.channelname === params.channelname || item.orderby === params.orderby) {
              result.push(item)
            }
          } else if (item.channelid !== params.channelid && (item.channelname === params.channelname || item.orderby === params.orderby)) {
            result.push(item)
            console.info(item)
          }
        }
        console.info(dataList)
        return result
      },
      getChannelOrder () {
        let order = 1
        let list = this.channelData
        for (let i in list) {
          let item = list[i]
          let itemOrder = Number(item.orderby)
          if (itemOrder > order) {
            order = itemOrder
          }
        }
        return order + 1
      },
      makeChannelId (order) {
        let channelId = 'CH'
        if (order < 10) {
          channelId = channelId + '0'
        }
        channelId = channelId + order
        return channelId
      }
    }
  }
</script>
<style>

</style>
