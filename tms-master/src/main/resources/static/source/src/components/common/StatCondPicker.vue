<template>
  <!--<div>-->
  <el-dialog ref="StatCondDialog" title="条件" :visible.sync="statCondInDialogVisible" width="770px" :modal="false" :close-on-click-modal="false">
    <el-form :model="statCondInDictDialogForm"  ref="statCondInDictDialogForm" style="text-align: left"  :inline="true">
      <el-form-item label="交易属性:" :label-width="formLabelWidth" prop="stat_datafd" :style="formItemStyle" v-show="itemShow.stat_datafd">
        <el-select v-model="statCondInDictDialogForm.stat_datafd" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChange">
          <el-option
            v-for="item in statDatafdList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="交易统计:" :label-width="formLabelWidth" prop="stat_fn" :style="formItemStyle" v-show="itemShow.stat_fn">
        <div @dblclick="statFnDialogVisible = true">
          <el-input v-model="statCondInDictDialogForm.stat_fn" auto-complete="off" :style="formItemContentStyle" readonly ></el-input>
        </div>
      </el-form-item>

      <el-form-item label="日期函数:" :label-width="formLabelWidth" prop="date_func" :style="formItemStyle" v-show="itemShow.date_func" >
        <el-select v-model="statCondInDictDialogForm.date_func" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChangeAddBracket">
          <el-option
            v-for="item in dateFuncList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="字符函数:" :label-width="formLabelWidth" prop="str_func" :style="formItemStyle" v-show="itemShow.str_func" >
        <el-select v-model="statCondInDictDialogForm.str_func" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChangeAddBracket">
          <el-option
            v-for="item in strFuncList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="关系函数:" :label-width="formLabelWidth" prop="oper_func" :style="formItemStyle"  v-show="itemShow.oper_func" >
        <el-select v-model="statCondInDictDialogForm.oper_func" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChange">
          <el-option
            v-for="item in operFuncList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="名单:" :label-width="formLabelWidth" prop="roster_func" :style="formItemStyle" v-show="itemShow.roster_func">
        <el-select v-model="statCondInDictDialogForm.roster_func" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChange">
          <el-option
            v-for="item in rosterFuncList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="自定义函数:" :label-width="formLabelWidth" prop="diy_func" :style="formItemStyle"  v-show="itemShow.diy_func">
        <el-select v-model="statCondInDictDialogForm.diy_func" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChangeAddBracket">
          <el-option
            v-for="item in diyFuncList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="交易规则:" :label-width="formLabelWidth" prop="rule_func" :style="formItemStyle"  v-show="itemShow.rule_func">
        <el-select v-model="statCondInDictDialogForm.rule_func" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChange">
          <el-option
            v-for="item in ruleFuncList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="动作函数:" :label-width="formLabelWidth" prop="ac_func" :style="formItemStyle" v-show="itemShow.ac_func">
        <el-select v-model="statCondInDictDialogForm.ac_func" placeholder="请选择" :style="formItemContentStyle"
                   clearable @change="selectChangeAddBracket">
          <el-option
            v-for="item in acFuncList"
            :key="item.code_key"
            :label="item.code_value"
            :value="item.code_key">
          </el-option>
        </el-select>
      </el-form-item>

      <div>
        <el-form-item label="条件表达式:" :label-width="formLabelWidth" prop="stat_cond_value">
          <el-input type="textarea" v-model="statCondInDictDialogForm.stat_cond_value" ref="statCondValue" :style="textareaContentStyle" :rows="5"></el-input>
        </el-form-item>
      </div>

      <div>
        <el-form-item label="解释:" :label-width="formLabelWidth" prop="stat_cond_in" >
          <el-input type="textarea" v-model="statCondInDictDialogForm.stat_cond_in" ref="statCondIn" :style="textareaContentStyle" :rows="5"></el-input>
        </el-form-item>
      </div>

      <div>
        <el-form-item  label=" " :label-width="formLabelWidth" >
          <el-button type="primary" @click="callback" size="large">确 定</el-button>
          <el-button @click="reset" size="large">重 置</el-button>
          <el-button @click="closeDialog" size="large">取 消</el-button>
        </el-form-item>
      </div>


    </el-form>

    <el-dialog title="交易统计" :visible.sync="statFnDialogVisible" width="400px" :modal="false" :close-on-click-modal="false">
      <el-tree :data="treeData" node-key="id" ref="tree"
               :props="defaultProps"
               :highlight-current=true
               :default-expanded-keys="expendKey"
               :expand-on-click-node="false"
               @node-click="handleNodeClick"
               :render-content="renderContent"
               style="overflow-y: auto;">
      </el-tree>

      <div slot="footer" class="dialog-footer">
        <el-button @click="statFnDialogVisible = false" size="large">关 闭</el-button>
      </div>
    </el-dialog>
  </el-dialog>
  <!--</div>-->
</template>

<script>
  import ajax from '@/common/ajax'
  import util from "../../common/util";
  // import dictCode from "../../common/dictCode";

  export default {
    computed: {
      txnIdParent () {
        return this.txnId
      }
    },
    data () {
      return {
        firstOpen: true,
        statCondInDialogVisible: false,
        formItemStyle: {
          width: '330px'
        },
        formItemContentStyle: {
          width: '200px'
        },
        textareaContentStyle: {
          width: '540px'
        },
        formLabelWidth: '120px',
        statFnDialogVisible: false,
        selectArea: [0, 0],
        statCondInDictDialogForm: this.initStatCondInDictDialogForm(),
        treeData: [],
        defaultProps: {
          children: 'children',
          label: 'text'
        },
        itemShow: {
          stat_datafd: true,
          stat_fn: true,
          date_func: true,
          str_func: true,
          oper_func: true,
          roster_func: true,
          diy_func: true,
          rule_func: true,
          ac_func: true
        },
        expendKey: ['T'], // 默认展开的功能节点id
        statDatafdList: [],
        dateFuncList: [],
        strFuncList: [],
        operFuncList: [],
        rosterFuncList: [],
        diyFuncList: [],
        ruleFuncList: [],
        acFuncList: [],
        allSelectData: new Set() // 表达式解释翻译用的保存所有key value
      }
    },
    props: {
      statCond: {
        type: String,
        default: ''
      },
      statCondIn: {
        type: String,
        default: ''
      },
      txnId: {
        type: String,
        default: ''
      },
      hideItems: {
        type: Array,
        default () {
          return []
        }
      }
    },
    watch: {
      'statCondInDictDialogForm.stat_cond_value': function (val) {
        this.searchCondName(val, 1)
      },
      // 'statCondInDictDialogForm.stat_cond_in': function (val) {
      //   this.searchCondName(val, 2)
      // }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.statCondInDictDialogForm.stat_cond_value = this.statCond
        this.statCondInDictDialogForm.stat_cond_in = this.statCondIn
        let hideArr = this.hideItems

        for (let hideName of hideArr) {
          this.itemShow[hideName] = false
        }

      })
    },
    methods: {
      getSelectedPostion (e) {
        var selectArea = [0, 0]
        var terget = e
        // 如果是Firefox(1.5)的话，方法很简单
        if (typeof (terget.selectionStart) === 'number') {
          selectArea = [terget.selectionStart, terget.selectionEnd]
        } else if (document.selection) {
          // 下面是IE(6.0)的方法，麻烦得很，还要计算上'\n'
          var start = 0, end = 0,
            range = document.selection.createRange();
          if (range.parentElement() == terget) {
            // create a selection of the whole textarea
            var range_all = document.body.createTextRange();
            range_all.moveToElementText(terget);
            //两个range，一个是已经选择的text(range)，一个是整个textarea(range_all)
            //range_all.compareEndPoints()比较两个端点，如果range_all比range更往左(further to the left)，则                //返回小于0的值，则range_all往右移一点，直到两个range的start相同。
            // calculate selection start point by moving beginning of range_all to beginning of range
            for (start = 0; range_all.compareEndPoints('StartToStart', range) < 0; start++){
              range_all.moveStart('character', 1);
            }
            // get number of line breaks from textarea start to selection start and add them to start
            // 计算一下\n
            for (let i = 0; i <= start; i ++){
              if (terget.value.charAt(i) == '\n'){
                start++
              }
            }
            // create a selection of the whole textarea
            var range_all = document.body.createTextRange();
            range_all.moveToElementText(terget);
            // calculate selection end point by moving beginning of range_all to end of range
            for (end = 0; range_all.compareEndPoints('StartToEnd', range) < 0; end ++){
              range_all.moveStart('character', 1);
            }
            // get number of line breaks from textarea start to selection end and add them to end
            for (let i = 0; i <= end; i++) {
              if (terget.value.charAt(i) == '\n'){
                end++
              }
            }
          }
          selectArea = [start, end]
        }
        this.selectArea = selectArea
        // return selectArea
      },
      selectChange (value) {
        this.selectChangeCommon(value)
      },
      selectChangeAddBracket (value) {
        this.selectChangeCommon(value + '()')
      },
      selectChangeCommon (value) {
        let self = this
        let to = this.statCondInDictDialogForm.stat_cond_value
        let area = this.selectArea
        var pre = to.substr(0, area[0])
        var post = to.substr(area[1])
        var _val = [pre, value, post]

        this.statCondInDictDialogForm = this.initStatCondInDictDialogForm()
        this.statCondInDictDialogForm.stat_cond_value = _val.join('')
        setTimeout(function () {
          let statCondValueTextarea = self.$refs.statCondValue.$refs.textarea
          statCondValueTextarea.focus()
          self.getSelectedPostion(statCondValueTextarea)
        }, 300)
      },
      initStatCondInDictDialogForm () {
        return {
          stat_datafd: '',
          stat_fn: '',
          date_func: '',
          str_func: '',
          oper_func: '',
          roster_func: '',
          diy_func: '',
          rule_func: '',
          ac_func: '',
          stat_cond_value: '',
          stat_cond_in: ''
        }
      },
      initData () {
        // 统计函数
        this.selTree()
        // 规则列表
        this.getStatDataFdSelectData()
        // 统计目标
        this.getRuleFuncList()
        // 关系函数
        this.getCodeData('operFuncList', 'tms.pub.func', '3', true)
        // 自定义函数
        this.getCodeData('diyFuncList', 'tms.pub.func', '1')
        // 动作函数
        this.getCodeData('acFuncList', 'tms.pub.func', '7')
        // 日期函数
        this.getCodeData('dateFuncList', 'tms.pub.func', '4')
        // 字符串函数
        this.getCodeData('strFuncList', 'tms.pub.func', '5')
        // 名单函数
        this.getCodeData('rosterFuncList', 'tms.pub.roster')
        let self = this
        setTimeout(function () {
          self.searchCondName(self.statCondInDictDialogForm.stat_cond_value, 1)
        }, 500)
      },
      reloadData () {
        // 规则列表
        this.getStatDataFdSelectData()
        // 统计目标
        this.getRuleFuncList()
      },
      getStatDataFdSelectData () {
        let self = this
        ajax.post({
          url: '/stat/txnFeature',
          param: {txn_id: this.txnIdParent},
          success: function (data) {
            if (data.row) {
              self.statDatafdList = []
              for (let value of data.row) {
                self.allSelectData.add({code_key: value.code_key, code_value: value.code_value})
                value.code_value = `${value.code_value}(${value.code_key})`
                self.statDatafdList.push(value)
              }

              self.statDatafdList.sort(function compareFunction (param1, param2) {
                return param1.code_value.localeCompare(param2.code_value, 'zh')
              })
            }
          }
        })
      },
      getRuleFuncList () {
        let self = this
        ajax.post({
          url: '/stat/txnrulelist',
          param: {txn_id: this.txnIdParent},
          success: function (data) {
            if (data.row) {
              self.ruleFuncList = []
              for (let value of data.row) {
                self.allSelectData.add({code_key: value.code_key, code_value: value.code_value})
                value.code_value = `${value.code_value}(${value.code_key})`
                self.ruleFuncList.push(value)
              }
            }
          }
        })
      },
      getCodeData (listName, categoryId, arg, ignoreDict) { // 最后一个参数是是否将结果放入翻译用的set中
        let self = this
        let param = {}
        param.category_id = categoryId
        if (arg) {
          param.args = arg
        }
        ajax.post({
          url: '/stat/code',
          param: param,
          success: function (data) {
            if (data.row) {
              self[listName] = []
              for (let value of data.row) {
                if (!ignoreDict) {
                  self.allSelectData.add({code_key: value.code_key, code_value: value.code_value})
                }
                value.code_value = `${value.code_value}(${value.code_key})`
                self[listName].push(value)
              }
            }
          }
        })
      },
      // 功能树渲染方法
      renderContent (h, { node, data, store }) {
        if (node.data.enable === 0) { // 功能节点状态禁用
          return (<span class="el-tree-node__label disabledFlag">{node.label}</span>)
        } else { // 功能节点状态正常
          return (<span class="el-tree-node__label">{node.label}</span>)
        }
      },
      // 查询树结构
      selTree () {
        var self = this
        var option = {
          url: '/stat/statfunc',
          success: function (data) {
            if (data.row) {
              self.treeList = (data.row)
              self.treeData = self.formatTreeData(data.row)
              for (let treeNode of data.row) {
                if (treeNode.ftype == 2) {
                  self.allSelectData.add({code_key: treeNode.code_key, code_value: treeNode.code_value})
                }
              }
              // self.expendNodesByLevel(1)
            }
          }
        }
        ajax.post(option)
      },
      // 把功能节点列表格式化为树形Json结构
      formatTreeData (list, rootNodes) {
        var tree = []
        // 如果根节点数组不存在，则取fid不存在或为空字符的节点为父节点
        if (rootNodes === undefined || rootNodes.length === 0) {
          rootNodes = []
          for (var i in list) {
            list[i].text = `${list[i].code_value}(${list[i].code_key})`
            if (list[i].fid === undefined || list[i].fid === null || list[i].fid === '') {
              rootNodes.push(list[i])
            }
          }
        }
        // 根节点不存在判断
        if (rootNodes.length === 0) {
          console.error('根节点不存在，请确认树结构是否正确')
          console.info('树结构的根节点是fid不存在（或为空）的节点，否则需手动添加指定得根节点（参数）')
        }
        // 根据根节点遍历组装数据
        for (var r in rootNodes) {
          var node = rootNodes[r]
          node.children = getChildren(list, node.id)
          tree.push(node)
        }

        // 递归查询节点的子节点
        function getChildren (list, id) {
          var childs = []
          for (var i in list) {
            var node = list[i]
            if (node.fid === id) {
              node.children = getChildren(list, node.id)
              // node.icon = 'el-icon-message'
              childs.push(node)
            }
          }
          return childs
        }
        return tree  // 返回树结构Json
      },
      // kind 1:英译汉 2:汉译英。默认是英译汉
      searchCondName (condValue, kind) {
        if (condValue === '' || condValue == null) {
          this.statCondInDictDialogForm.stat_cond_value = ''
          this.statCondInDictDialogForm.stat_cond_in = ''
          return
        }
        let key = 'code_key'
        let val = 'code_value'
        if (kind === 2) {
          val = 'code_key'
          key = 'code_value'
        }
        var condname = condValue

        for (let loop of this.allSelectData) {
          if (loop[key]) {
            condname = condname.replace(new RegExp(util.escapeExprSpecialWord(loop[key]), 'g'), loop[val])
          }
        }

        if (kind === 2) {
          this.statCondInDictDialogForm.stat_cond_value = condname
        } else {
          this.statCondInDictDialogForm.stat_cond_in = condname
        }
      },
      handleNodeClick (data, node) {
        if (node.isLeaf) {
          this.selectChangeCommon(data.code_key)
        }
      },
      closeDialog () {
        this.statCondInDialogVisible = false
        // this.$emit('closeDialog')
      },
      callback () {
        let callbackObj = {
          stat_cond_value: this.statCondInDictDialogForm.stat_cond_value,
          stat_cond_in: this.statCondInDictDialogForm.stat_cond_in
        }
        this.$emit('valueCallback', callbackObj)
        this.statCondInDialogVisible = false
      },
      reset () {
        this.statCondInDictDialogForm.stat_cond_value = ''
        this.statCondInDictDialogForm.stat_cond_in = ''
      },
      setValue (valueObj) {
        this.statCondInDictDialogForm.stat_cond_value = valueObj.stat_cond_value
        this.statCondInDictDialogForm.stat_cond_in = valueObj.stat_cond_in
      },
      open () {
        if (this.firstOpen) {
          let self = this
          this.initData()
          setTimeout(function () {
            let statCondValueTextarea = self.$refs.statCondValue.$refs.textarea
            statCondValueTextarea.addEventListener('keydown', function () { self.getSelectedPostion(statCondValueTextarea) })
            statCondValueTextarea.addEventListener('keyup', function () { self.getSelectedPostion(statCondValueTextarea) })
            statCondValueTextarea.addEventListener('mousedown', function () { self.getSelectedPostion(statCondValueTextarea) })
            statCondValueTextarea.addEventListener('mouseup', function () { self.getSelectedPostion(statCondValueTextarea) })
            statCondValueTextarea.addEventListener('focus', function () { self.getSelectedPostion(statCondValueTextarea) })
          }, 300)
        }

        this.firstOpen = false
        this.statCondInDialogVisible = true
      }
    }
  }
</script>

<style>
  .stat-cond-form-footer{
    margin-top: 10px;
    margin-right: 20px;
    text-align: right;
  }
</style>
