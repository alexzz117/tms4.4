<template>
  <div>
    <el-form :model="tranDefForm"
             :rules="rules"
             ref="tranDefForm"
             label-suffix="："
             :label-width="formLabelWidth"
             style="text-align: left;padding-right: 5em;overflow-y: auto;height: 430px;">
      <el-form-item label="" prop="parent_tab" v-bind:class="{hidden:tranDefFormVisible.parent_tab}">
        <el-input v-model="tranDefForm.parent_tab" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="tab_name" v-bind:class="{hidden:tranDefFormVisible.tab_name}">
        <el-input v-model="tranDefForm.tab_name" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="modelused_o" v-bind:class="{hidden:tranDefFormVisible.modelused_o}">
        <el-input v-model="tranDefForm.modelused_o" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="traindate" v-bind:class="{hidden:tranDefFormVisible.traindate}">
        <el-input v-model="tranDefForm.traindate" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="op" v-bind:class="{hidden:tranDefFormVisible.op}">
        <el-input v-model="tranDefForm.op" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="名称" prop="tab_desc" v-bind:class="{hidden:tranDefFormVisible.tab_desc}">
        <el-input v-model="tranDefForm.tab_desc" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="顺序" prop="show_order" v-bind:class="{hidden:tranDefFormVisible.show_order}">
        <el-input v-model="tranDefForm.show_order" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="交易类型">
        <el-radio v-model="tranDefForm.flag" label="0">交易组</el-radio>
        <el-radio v-model="tranDefForm.flag" label="1">交易</el-radio>
      </el-form-item>
      <el-form-item label="交易识别标识" prop="tab_desc" v-bind:class="{hidden:tranDefFormVisible.txnid}">
        <el-input v-model="tranDefForm.txnid" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="处置策略:" prop="stat_param">
        <el-select v-model="tranDefForm.tab_disposal" placeholder="请选择"
                   multiple collapse-tags
                   @change="statParamSelectChange">
          <el-option key="&ALLPICK&" label="全选" value="&ALLPICK&">
          </el-option>

          <el-option
            v-for="item in tabDisposalList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>

        </el-select>
      </el-form-item>
      <el-form-item label="渠道名称" prop="chann">
        <el-select v-model="tranDefForm.chann" placeholder="请选择">
          <el-option
            v-for="item in chanNameList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="上级交易" prop="txnffname" v-bind:class="{hidden:tranDefFormVisible.txnffname}">
        <el-input v-model="tranDefForm.txnffname" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="模型状态" prop="modelused">
        <el-select v-model="tranDefForm.modelused" placeholder="请选择">
          <el-option
            v-for="item in modelUsedList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="有效性">
        <el-radio v-model="tranDefForm.is_enable" label="1">启用</el-radio>
        <el-radio v-model="tranDefForm.is_enable" label="0">停用</el-radio>
      </el-form-item>
      <el-form-item style="text-align: center">
        <el-button type="primary" @click="">保 存</el-button>
        <el-button @click="">取 消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
  // import check from '@/common/check'
  // import ajax from '@/common/ajax'
  export default {
    create () {
    },
    computed: {
      statParamSelectAllPick () {
        let allValuesArr = this.tabDisposalList.map(x => x.value)
        let b = new Set(this.tranDefForm.tab_disposal)
        let differenceABSet = new Set(allValuesArr.filter(x => !b.has(x)))
        console.log(differenceABSet)
        return differenceABSet.size === 0
        // return this.queryShowForm.stat_param.length > this.tabDisposalList.length
      }
    },
    data () {
      return {
        tabDisposalList: [{label: 'aaa', value: 'aaa'}, {label: 'bbb', value: 'bbb'}, {label: 'ccc', value: 'ccc'}],
        tabDisposalSelectedCached: [],
        modelUsedList: [{label: '不使用模型', value: '0'}, {label: '模型学习期', value: '1'}],
        chanNameList: [{label: '全渠道', value: '0'}],
        tranDefForm: {
          parent_tab: '',
          tab_name: '',
          modelused_o: '',
          traindate: '',
          op: '',
          tab_desc: '', // 名称
          show_order: '', // 顺序
          txn_type: '', // 交易类型
          txnid: '', // 交易识别标识
          tab_disposal: '', // 处置策略
          chann: '', // 渠道名称
          txnffname: '', // 上级交易
          modelused: '', // 模型状态
          is_enable: '' // 有效性
        },
        tranDefFormVisible: {
          parent_tab: true,
          tab_name: true,
          modelused_o: true,
          traindate: true,
          op: true,
          tab_desc: false, // 名称
          show_order: false, // 顺序
          txn_type: false, // 交易类型
          txnid: false, // 交易识别标识
          tab_disposal: false, // 处置策略
          chann: false, // 渠道名称
          txnffname: false, // 上级交易
          modelused: false, // 模型状态
          is_enable: false // 有效性
        },
        formLabelWidth: '120px',
        rules: {
          a: ''
        }
      }
    },
    methods: {
      statParamSelectChange (val) {
        console.log(val)
        console.log(this.statParamSelectAllPick)
        let a = new Set(this.tabDisposalSelectedCached)
        let b = new Set(this.tranDefForm.tab_disposal)

        let differenceABSet = new Set(this.tabDisposalSelectedCached.filter(x => !b.has(x)))

        if (differenceABSet.size > 0) {
          if (differenceABSet.has('&ALLPICK&')) {
            // 取消了全选
            this.tranDefForm.tab_disposal = []
          } else {
            // 取消了其他
            this.tranDefForm.tab_disposal = this.tranDefForm.tab_disposal.filter(x => x !== '&ALLPICK&')
          }
        }

        differenceABSet = new Set(this.tranDefForm.tab_disposal.filter(x => !a.has(x)))
        if (differenceABSet.size > 0) {
          if (differenceABSet.has('&ALLPICK&')) {
            // 添加了全选
            this.tranDefForm.tab_disposal = []
            this.tranDefForm.tab_disposal = this.tabDisposalList.map((x) => {
              return x.value
            })
            this.tranDefForm.tab_disposal.push('&ALLPICK&')
          } else {
            // 添加了其他
            console.log(this.statParamSelectAllPick)
            if (this.statParamSelectAllPick) {
              this.tranDefForm.tab_disposal.push('&ALLPICK&')
            }
          }
        }
        this.tabDisposalSelectedCached = []
        let tempSet = new Set(this.tranDefForm.tab_disposal)
        let finalArr = []
        if (tempSet.has('&ALLPICK&')) {
          finalArr.push('&ALLPICK&')
          tempSet.delete('&ALLPICK&')
          finalArr = finalArr.concat(Array.from(tempSet))
          this.tranDefForm.tab_disposal = finalArr
        }
        console.log(123)
        console.log(this.tranDefForm.tab_disposal)

        Object.assign(this.tabDisposalSelectedCached, this.tranDefForm.tab_disposal)
      }
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
