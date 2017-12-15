<template>
  <div>
    <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
             :inline="true" style="text-align: left">
      <el-form-item label="统计名称:" prop="stat_name">
        <el-input v-model="queryShowForm.stat_name" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
      <el-form-item label="统计描述:" prop="stat_desc">
        <el-input v-model="queryShowForm.stat_desc" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
      <el-form-item label="统计引用对象:" prop="stat_param">
        <el-select v-model="queryShowForm.stat_param" placeholder="请选择"
                   multiple collapse-tags
                   @change="statParamSelectChange">

          <el-option key="&ALLPICK&" label="全选" value="&ALLPICK&">
          </el-option>

          <el-option
            v-for="item in statParamList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>

        </el-select>
      </el-form-item>
      <el-form-item label="统计目标:" prop="stat_datafd">
        <el-input v-model="queryShowForm.stat_datafd" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
      <el-form-item label="统计函数:" prop="stat_fn">
        <el-input v-model="queryShowForm.stat_fn" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
      <el-form-item label="有效性:" prop="stat_valid">
        <el-input v-model="queryShowForm.stat_valid" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
    </el-form>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-view" type="primary">查询</el-button>
      <el-button plain class="el-icon-view" :disabled="notSelectOne">查看</el-button>
      <el-button plain class="el-icon-plus">新建</el-button>
      <el-button plain class="el-icon-edit" :disabled="notSelectOne">编辑</el-button>
      <el-button plain class="el-icon-delete" :disabled="notSelectOne">删除</el-button>
      <el-button plain class="el-icon-circle-check" :disabled="notSelectOne">启用</el-button>
      <el-button plain class="el-icon-remove" :disabled="notSelectOne">停用</el-button>
      <el-button plain class="el-icon-share" :disabled="notSelectOne">引用点</el-button>
    </div>

  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'

  export default {
    computed: {
      notSelectOne () {
        return this.selectedRows.length !== 1
      },
      statParamSelectAllPick () {
        let allValuesArr = this.statParamList.map(x => x.value)
        let b = new Set(this.queryShowForm.stat_param)
        let differenceABSet = new Set(allValuesArr.filter(x => !b.has(x)))
        console.log(differenceABSet)
        return differenceABSet.size === 0
        // return this.queryShowForm.stat_param.length > this.statParamList.length
      }
    },
    data () {
      return {
        // txnId: '123',
        txnIdParent: this.txnId,
        isVisibilityParent: this.isVisibility,
        modelName: '',
        tableData: [],
        dialogTitle: '授权意见',
        dictDialogVisible: false,
        statParamSelectedCached: [],
        queryShowForm: {
          stat_name: '',
          stat_desc: '',
          stat_param: '',
          stat_datafd: '',
          stat_fn: '',
          stat_valid: ''
        },
        statParamList: [{label: 'aaa', value: 'aaa'},{label: 'bbb', value: 'bbb'},{label: 'ccc', value: 'ccc'}],
        rules: {
          auth_status: [
            { required: true, message: '请选择是否通过授权', trigger: 'blur' }
          ],
          auth_msg: [
            { required: true, message: '请输入授权说明', trigger: 'blur' },
            { max: 2048, message: '长度在2048个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        },
        formLabelWidth: '130px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectedRows: []
      }
    },
    props: ['txnId', 'isVisibility'],
    mounted: function () {
      this.$nextTick(function () {
        this.getData()
      })
    },
    watch: {
      txnId: {
        handler: (val, oldVal) => {
          this.txnIdParent = val
          if (this.isVisibilityParent === true) {
            console.log('reloadData')
          }
        }
      },
      isVisibility: {
        handler: (val, oldVal) => {
          this.isVisibilityParent = val
          if (this.isVisibilityParent === true) {
            console.log('reloadData')
          }
        }
      }
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      }
    },
    methods: {
      getData () {
        // let self = this
        // let paramsObj = {
        //   modelName: this.modelName,
        //   pageindex: this.currentPage,
        //   pagesize: this.pageSize
        // }
        // ajax.post({
        //   url: '/tms/auth/authList',
        //   param: paramsObj,
        //   success: function (data) {
        //     if (data.page) {
        //       self.bindGridData(data)
        //     }
        //   }
        // })
      },
      statParamSelectChange (val) {
        console.log(val)
        console.log(this.statParamSelectAllPick)
        let a = new Set(this.statParamSelectedCached)
        let b = new Set(this.queryShowForm.stat_param)

        let differenceABSet = new Set(this.statParamSelectedCached.filter(x => !b.has(x)))

        if (differenceABSet.size > 0) {
          if (differenceABSet.has('&ALLPICK&')) {
            // 取消了全选
            this.queryShowForm.stat_param = []
          } else {
            // 取消了其他
            this.queryShowForm.stat_param = this.queryShowForm.stat_param.filter(x => x !== '&ALLPICK&')
          }
        }

        differenceABSet = new Set(this.queryShowForm.stat_param.filter(x => !a.has(x)))
        if (differenceABSet.size > 0) {
          if (differenceABSet.has('&ALLPICK&')) {
            // 添加了全选
            this.queryShowForm.stat_param = []
            this.queryShowForm.stat_param = this.statParamList.map((x) => {
              return x.value
            })
            this.queryShowForm.stat_param.push('&ALLPICK&')
          } else {
            // 添加了其他
            console.log(this.statParamSelectAllPick)
            if (this.statParamSelectAllPick) {
              this.queryShowForm.stat_param.push('&ALLPICK&')
            }
          }
        }
        this.statParamSelectedCached = []
        let tempSet = new Set(this.queryShowForm.stat_param)
        let finalArr = []
        if (tempSet.has('&ALLPICK&')) {
          finalArr.push('&ALLPICK&')
          tempSet.delete('&ALLPICK&')
          finalArr = finalArr.concat(Array.from(tempSet))
          this.queryShowForm.stat_param = finalArr
        }
        console.log(123)
        console.log(this.queryShowForm.stat_param)

        Object.assign(this.statParamSelectedCached, this.queryShowForm.stat_param)
      }
    }
  }
</script>

<style>

</style>
