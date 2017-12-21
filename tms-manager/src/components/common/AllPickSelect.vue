<template>
  <el-select v-model="selectedRows" class="query-form-item" placeholder="请选择"
             multiple collapse-tags :disabled="selectDisabled"
             @change="selectChange">

    <el-option key="&ALLPICK&" label="全选" value="&ALLPICK&">
    </el-option>

    <el-option
      v-for="item in allDataList"
      :key="item.code_key"
      :label="item.code_value"
      :value="item.code_key">
    </el-option>

  </el-select>
</template>

<script>
  export default {
    computed: {
      allPick () {
        let allValuesArr = this.allDataList.map(x => x.code_key)
        let b = new Set(this.selectedRows)
        let differenceABSet = new Set(allValuesArr.filter(x => !b.has(x)))
        return differenceABSet.size === 0
      },
      selectDisabled () {
        return this.disabled
      }
    },
    data () {
      return {
        // selectDisabled: false,
        allDataList: [],
        selectedCached: [],
        selectedRows: []
      }
    },
    props: {
      dataList: {
        type: Array,
        default: []
      },
      selectedList: {
        type: Array,
        default: []
      },
      disabled: {
        type: Boolean,
        default: false
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.allDataList = this.dataList
        // this.selectDisabled = this.disabled
        this.selectedRows = this.selectedList
      })
    },
    watch: {
      selectedRows () {
        this.value = this.selectedRows
        let realSelectRow = this.selectedRows.filter(x => x !== '&ALLPICK&')
        this.$emit('dataChange', realSelectRow)
      },
      // allDataList () {
      //   this.dataList = this.allDataList
      // },
      dataList () {
        this.allDataList = this.dataList
      }
    },
    methods: {
      // 下面这个方法支持了查询中的全选操作
      selectChange (val) {
        let a = new Set(this.selectedCached)
        let b = new Set(this.selectedRows)

        let differenceABSet = new Set(this.selectedCached.filter(x => !b.has(x)))

        if (differenceABSet.size > 0) {
          if (differenceABSet.has('&ALLPICK&')) {
            // 取消了全选
            this.selectedRows = []
          } else {
            // 取消了其他
            this.selectedRows = this.selectedRows.filter(x => x !== '&ALLPICK&')
          }
        }

        differenceABSet = new Set(this.selectedRows.filter(x => !a.has(x)))
        if (differenceABSet.size > 0) {
          if (differenceABSet.has('&ALLPICK&')) {
            // 添加了全选
            this.selectedRows = []
            this.selectedRows = this.allDataList.map((x) => {
              return x.code_key
            })
            this.selectedRows.push('&ALLPICK&')
          } else {
            // 添加了其他
            if (this.allPick) {
              this.selectedRows.push('&ALLPICK&')
            }
          }
        }
        this.selectedCached = []
        let tempSet = new Set(this.selectedRows)
        let finalArr = []
        if (tempSet.has('&ALLPICK&')) {
          finalArr.push('&ALLPICK&')
          tempSet.delete('&ALLPICK&')
          finalArr = finalArr.concat(Array.from(tempSet))
          this.selectedRows = finalArr
        }

        Object.assign(this.selectedCached, this.selectedRows)
      }
    }
  }
</script>

<style>

</style>
