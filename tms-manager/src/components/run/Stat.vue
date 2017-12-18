<template>
  <div>
    <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
             :inline="true" style="text-align: left">
      <el-form-item label="统计名称:" prop="stat_name">
        <el-input v-model="queryShowForm.stat_name" class="query-form-item" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
      <el-form-item label="统计描述:" prop="stat_desc">
        <el-input v-model="queryShowForm.stat_desc" class="query-form-item" auto-complete="off" :maxlength=50></el-input>
      </el-form-item>
      <el-form-item label="统计引用对象:" prop="stat_param">
        <el-select v-model="queryShowForm.stat_param" class="query-form-item" placeholder="请选择"
                   multiple collapse-tags
                   @change="statParamSelectChange">

          <el-option key="&ALLPICK&" label="全选" value="&ALLPICK&">
          </el-option>

          <el-option
            v-for="item in statParamList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>

        </el-select>
      </el-form-item>
      <el-form-item label="统计目标:" prop="stat_datafd">

        <el-select v-model="queryShowForm.stat_datafd" class="query-form-item" placeholder="请选择"
                   clearable>
          <el-option
            v-for="item in statDataFdList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>

        </el-select>

      </el-form-item>
      <el-form-item label="统计函数:" prop="stat_fn">
        <el-select v-model="queryShowForm.stat_fn" class="query-form-item" placeholder="请选择"
                   clearable>
          <el-option
            v-for="item in statFnList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>

        </el-select>
      </el-form-item>
      <el-form-item label="有效性:" prop="stat_valid">
        <el-select v-model="queryShowForm.stat_valid" class="query-form-item" placeholder="请选择"
                   clearable>
          <el-option
            v-for="item in statValidList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>

        </el-select>
      </el-form-item>
    </el-form>

    <div style="margin-bottom: 10px;text-align: left ">
      <!--<el-button class="el-icon-view" type="primary">查询</el-button>-->
      <el-button plain class="el-icon-view" :disabled="notSelectOne">查看</el-button>
      <el-button plain class="el-icon-plus">新建</el-button>
      <el-button plain class="el-icon-edit" :disabled="notSelectOne">编辑</el-button>
      <el-button plain class="el-icon-delete" :disabled="notSelectOne">删除</el-button>
      <el-button plain class="el-icon-circle-check" :disabled="notSelectOne">启用</el-button>
      <el-button plain class="el-icon-remove" :disabled="notSelectOne">停用</el-button>
      <el-button plain class="el-icon-share" :disabled="notSelectOne">引用点</el-button>
    </div>

    <el-table
      :data="tableDataShow"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column prop="stat_name" label="统计名称" align="left" width="80"></el-table-column>
      <el-table-column prop="stat_desc" label="统计描述" align="left" width="250"></el-table-column>
      <el-table-column label="统计引用对象" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.stat_param | statParamFilter}}</span>
        </template>
      </el-table-column>

      <el-table-column prop="stat_datafd" label="统计目标" align="left">
        <template slot-scope="scope">
          <span>{{scope.row.stat_datafd | statDataFdFilter}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stat_fn" label="统计函数" align="left"></el-table-column>
      <el-table-column prop="storecolumn" label="存储字段" align="left"></el-table-column>
      <el-table-column label="有效性" align="left" width="60">
        <template slot-scope="scope">
          <span>{{scope.row.stat_valid | renderStatValidFilter}}</span>
        </template>
      </el-table-column>
    </el-table>

  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'

  let vm = null

  export default {
    computed: {
      notSelectOne () {
        return this.selectedRows.length !== 1
      },
      statParamSelectAllPick () {
        let allValuesArr = this.statParamList.map(x => x.code_key)
        let b = new Set(this.queryShowForm.stat_param)
        let differenceABSet = new Set(allValuesArr.filter(x => !b.has(x)))
        console.log(differenceABSet)
        return differenceABSet.size === 0
        // return this.queryShowForm.stat_param.length > this.statParamList.length
      },
      tableDataShow () {
        console.log('tableDataShow')
        let showData = this.tableData.filter((x) => {
          let statName = this.queryShowForm.stat_name
          let statDesc = this.queryShowForm.stat_desc
          let statParamList = this.queryShowForm.stat_param
          if (statName !== '' && !x.stat_name.includes(statName)) {
            return false
          }
          if (statDesc !== '' && !x.stat_desc.includes(statDesc)) {
            return false
          }

          return true
        })
        return showData
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
        statParamList: [],
        statDataFdList: [],
        statFnList: [],
        statValidList: [],
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
        vm = this
        // this.reloadData()
      })
    },
    watch: {
      txnId: {
        handler: (val, oldVal) => {
          this.txnIdParent = val
          if (this.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      },
      isVisibility: {
        handler: (val, oldVal) => {
          this.isVisibilityParent = val
          if (this.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      }
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      },
      renderStatValidFilter (value) {
        // TODO 远程获取渲染
        if (value === 0) {
          return '停用'
        } else if (value === 1) {
          return '启用'
        } else {
          return ''
        }
      },
      statParamFilter (v) {
        if (v === null || v === '') {
          return ''
        }
        let returnTextArr = []
        let arr = v.split(',')

        for (let value of arr) {
          for (let i = 0; i < vm.statParamList.length; i++) {
            let row = vm.statParamList[i]
            if (value !== '' && value === row.code_key) {
              returnTextArr.push(row.code_value)
              break
            }
          }
        }
        return returnTextArr.join(',')
      },
      statDataFdFilter (v) {
        if (v === null || v === '') {
          return ''
        }
        let returnTextArr = []
        let arr = v.split(',')

        for (let value of arr) {
          for (let i = 0; i < vm.statDataFdList.length; i++) {
            let row = vm.statDataFdList[i]
            if (value !== '' && value === row.code_key) {
              returnTextArr.push(row.code_value)
              break
            }
          }
        }
        return returnTextArr.join(',')
      }
    },
    methods: {
      handleSelectionChange (rows) {
        this.selectedRows = rows
      },
      reloadData () {
        // getData卸载了这一句的回调中，因为需要依赖这个的取值
        this.getStatParamSelectData()
        this.getStatDataFnSelectData()
        this.getStatDataValidSelectData()
        // this.getData()
      },
      getData () {
        let self = this
        let paramsObj = {
          txnId: this.txnIdParent
        }
        ajax.post({
          url: '/manager/stat/list',
          param: paramsObj,
          success: function (data) {
            if (data.row) {
              self.tableData = data.row
            }
          }
        })
      },
      getStatParamSelectData () {
        let self = this
        ajax.post({
          url: '/manager/stat/txnFeature',
          param: {txn_id: this.txnIdParent},
          success: function (data) {
            if (data.row) {
              self.statParamList = []
              self.statDataFdList = []
              for (let value of data.row) {
                self.statParamList.push(value)
                self.statDataFdList.push(value)
              }

              self.getData()
            }
          }
        })
      },
      getStatDataFnSelectData () {
        let self = this
        ajax.post({
          url: '/manager/stat/code',
          param: {
            category_id: 'tms.pub.func',
            args: 2
          },
          success: function (data) {
            if (data.row) {
              self.statFnList = []
              for (let value of data.row) {
                self.statFnList.push(value)
              }
            }
          }
        })
      },
      getStatDataValidSelectData () {
        // TODO select码值获取
        this.statValidList = [{'code_key': '0', 'code_value': '停用'}, {'code_key': '1', 'code_value': '启用'}]
      },
      statParamSelectChange (val) {
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
              return x.code_key
            })
            this.queryShowForm.stat_param.push('&ALLPICK&')
          } else {
            // 添加了其他
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

        Object.assign(this.statParamSelectedCached, this.queryShowForm.stat_param)
      }
    }
  }
</script>

<style>
  .query-form-item{
    width: 200px;
  }
</style>
