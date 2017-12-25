<template>
  <div>
    <el-dialog ref="StatCondDialog" title="参数配置" :visible.sync="dialogVisible" width="500px">
      <div v-if="paramsType === 'date'">
        <div class="fn-param-item" v-for="(item, index) in dateDataList">
          <el-time-picker
            is-range
            :default-value="defalutTimeValue"
            v-model="item.dateArray"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            placeholder="选择时间范围">
          </el-time-picker>
          <el-button type="danger" @click="delRow(item)">删 除</el-button>
        </div>
      </div>
      <div v-if="paramsType === 'number'">

        <div class="fn-param-item" v-if="paramsType === 'number'">
          <el-form style="text-align: left" :inline="true">
          <div class="fn-param-item" v-for="(item, index) in numDataList">
            <el-input v-model="item.numberArray[0]" auto-complete="off" :style="formItemContentStyle"></el-input>
            -
            <el-input v-model="item.numberArray[1]" auto-complete="off" :style="formItemContentStyle"></el-input>
            <el-button type="danger" @click="delRow(item)">删 除</el-button>
          </div>
          </el-form>

        </div>
      </div>

      <div class="stat-cond-form-footer">
        <el-button @click="addRow">增 加</el-button>
        <el-button type="primary" @click="callback">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import util from "../../common/util";

  export default {
    computed: {
      defalutTimeValue () {
        let date = new Date()
        date.setHours(0)
        date.setMinutes(0)
        date.setSeconds(0)
        return [date, date]
      }
    },
    data () {
      return {
        formItemContentStyle: {
          width: '180px'
        },
        dialogVisible: false,
        paramsType: '',
        paramTypeReal: '',
        dateDataList: [],
        numDataList: []
      }
    },
    props: {

    },
    mounted: function () {
      this.$nextTick(function () {

      })
    },
    watch: {

    },
    methods: {
      open (fnParams, paramType) {
        this.dialogVisible = true
        this.paramTypeReal = paramType
        if (paramType !== undefined && (paramType === 'datetime' || paramType === 'time')) {
          this.paramsType = 'date'
          this.dateDataList = []
          if (fnParams && fnParams !== '') {
            let paramsArr = fnParams.split('|')
            for (let loop of paramsArr) {
              let startEndArr = loop.split(',')
              let startDate = this.str2Date(startEndArr[0])
              let endDate = this.str2Date(startEndArr[1])
              this.dateDataList.push(this.genDateObj(startDate, endDate))
            }
          } else {
            this.dateDataList.push(this.genDateObj(null, null))
          }
        } else {
          this.paramsType = 'number'
          this.numDataList = []
          if (fnParams && fnParams !== '') {
            let paramsArr = fnParams.split('|')
            for (let loop of paramsArr) {
              let startEndArr = loop.split(',')
              this.numDataList.push(this.genNumberObj(startEndArr[0], startEndArr[1]))
            }
          } else {
            this.numDataList.push(this.genNumberObj(null, null))
          }
        }
      },
      callback () {
        if (this.paramsType === 'date') {
          for (let i = 0; i < this.dateDataList.length; i++) {
            let dateLoop = this.dateDataList[i]
            if (dateLoop.dateArray != null) {
              let startTime = dateLoop.dateArray[0]
              let endTime = dateLoop.dateArray[1]
              if (startTime >= endTime) {
                this.$message(`第${i + 1}行，开始时间必须小于结束时间`)
                return
              }
              // 判断区间是否重复
              for (let j = 0; j < i; j++) {
                let dateLoopTemp = this.dateDataList[j]
                if (dateLoopTemp.dateArray != null) {
                  let startTimeTemp = dateLoopTemp.dateArray[0]
                  let endTimeTemp = dateLoopTemp.dateArray[1]
                  console.log(startTimeTemp.toLocaleString(), endTimeTemp.toLocaleString() ,startTime.toLocaleString(), endTime.toLocaleString())
                  if (!((startTimeTemp <= startTime && endTimeTemp <= startTime) || (startTimeTemp >= endTime && endTimeTemp >= endTime))) {
                    this.$message(`第${i + 1}行，时间区间与第${j + 1}重复`)
                    return
                  }
                }
              }
            }
          }
          let resultArr = []
          for (let i = 0; i < this.dateDataList.length; i++) {
            let dateLoop = this.dateDataList[i]
            if (dateLoop.dateArray != null) {
              let startStr = this.formatDate(dateLoop.dateArray[0])
              let endStr = this.formatDate(dateLoop.dateArray[1])
              resultArr.push(`${startStr},${endStr}`)
            }
          }
          this.$emit('valueCallback', resultArr.join('|'))
          this.dialogVisible = false
        } else {
          // 数值型
          for (let i = 0; i < this.numDataList.length; i++) {
            let numLoop = this.numDataList[i]
            if (numLoop.numberArray != null) {
              let startNum = numLoop.numberArray[0]
              let endNum = numLoop.numberArray[1]
              let checkStartNumRes = this.checkNum(startNum, i, true)
              if (checkStartNumRes !== '') {
                this.$message(checkStartNumRes)
                return
              }
              let checkEndNumRes = this.checkNum(endNum, i, false)
              if (checkEndNumRes !== '') {
                this.$message(checkEndNumRes)
                return
              }
              let startNumReal = startNum === '' ? 0 : parseFloat(startNum)
              let endNumReal = endNum === '' ? 0 : parseFloat(endNum)
              if (startNumReal >= endNumReal) {
                this.$message(`第(${parseInt(i) + 1})行：结束值必须大于开始值`)
                return
              }
              if (i > 0) {
                let preRow = this.numDataList[i - 1]
                if (preRow.numberArray != null) {
                  let preEndNum = preRow.numberArray[1]
                  let preEndNumReal = preEndNum === '' ? 0 : parseFloat(preEndNum)
                  if (startNumReal < preEndNumReal) {
                    this.$message(`第(${parseInt(i) + 1})行：开始值不能小于上一行的结束值`)
                    return
                  }
                }
              }
            }
          }

          let resultArr = []
          for (let i = 0; i < this.numDataList.length; i++) {
            let numLoop = this.numDataList[i]
            if (numLoop.numberArray != null) {
              let startNum = numLoop.numberArray[0]
              let endNum = numLoop.numberArray[1]
              resultArr.push(`${startNum},${endNum}`)
            }
          }
          this.$emit('valueCallback', resultArr.join('|'))
          this.dialogVisible = false
        }
      },
      str2Date (str) { // 将字符串转换为日期
        let DateArr = str.split(':')
        let date = new Date()
        date.setHours(DateArr[0])
        date.setMinutes(DateArr[1])
        date.setSeconds(DateArr[2])
        return date
      },
      genDateObj (startDate, endDate) {
        let id = Math.round(Math.random() * 100000000)
        if (startDate != null && endDate != null) {
          return {dateArray: [startDate, endDate], id: id}
        } else {
          return {dateArray: null, id: id}
        }
      },
      genNumberObj (start, end) {
        let id = Math.round(Math.random() * 100000000)
        if (start != null && end != null) {
          return {numberArray: [start, end], id: id}
        } else {
          return {numberArray: ['', ''], id: id}
        }
      },
      delRow (row) {
        if (this.paramsType === 'date') {
          this.dateDataList.splice(this.dateDataList.findIndex(item => item.id === row.id), 1)
        } else {
          this.numDataList.splice(this.numDataList.findIndex(item => item.id === row.id), 1)
        }
      },
      addRow () {
        if (this.paramsType === 'date') {
          this.dateDataList.push(this.genDateObj(null, null))
        } else {
          this.numDataList.push(this.genNumberObj(null, null))
        }
      },
      formatDate (date) {
        var hour = (date.getHours()).toString()
        var min = (date.getMinutes()).toString()
        var sec = (date.getSeconds()).toString()
        return `${hour}:${min}:${sec}`
      },
      checkNum (value, index, isStart) {
        if (value === '') {
          value = '0'
        }
        if (this.paramTypeReal === 'double' || this.paramTypeReal === 'money') {
          if (value !== '' && !util.isNumber(value, '+', 2)) {
            return `第(${parseInt(index) + 1})行：${isStart ? '开始值' : '结束值'}请输入非负小数（保留两位小数）或非负整数`
          }
          if (value.length > 15) {
            return `第(${parseInt(index) + 1})行：${isStart ? '开始值' : '结束值'}不能超过15个字符`
          }
        } else if (this.paramTypeReal === 'long') {
          if (value !== '' && !util.isNumber(value, '+', '0')) {
            return `第(${parseInt(index) + 1})行：${isStart ? '开始值' : '结束值'}请输入非负整数`
          }
          if (value.length > 15) {
            return `第(${parseInt(index) + 1})行：${isStart ? '开始值' : '结束值'}不能超过15个字符`
          }
        }
        return ''
      }
    }
  }
</script>

<style>
  .fn-param-item{
    margin-top: 10px;
  }
</style>
