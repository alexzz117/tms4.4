<template>
  <div>
    <el-container style="border: 1px solid #eee;">
      <el-header height="30px" class="table-header">
        <el-row>
          <el-col :span="12">
            <span>交易模型</span>
            <el-button type="text" icon="el-icon-plus" @click="tmAddFunc">新建交易模型</el-button>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 0px;">
        <el-table :data="tmTableData" ref="tmTable"
                  :span-method="groupHandle"
                  :row-class-name="groupClassName"
                  @row-dblclick="toggleListHandle"
                  max-height="430"
                  style="width: 100%">
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-edit" title="编辑" @click="tmEditFunc(scope.$index, scope.row)"></el-button>
              <el-button type="text" icon="el-icon-delete" title="删除" @click="tmDelFunc(scope.$index, scope.row)"></el-button>
              <el-button type="text" icon="el-icon-search" title="查看" @click="tmInfoFunc(scope.$index, scope.row)"></el-button>
            </template>
          </el-table-column>
          <el-table-column label="属性名称" prop="name">
            <template slot-scope="scope">
              <i v-if="scope.row.group_type==='group'" :class=groupIcon(scope.row)></i>
              <span style="margin-left: 10px">{{scope.row.name}}</span>
            </template>
          </el-table-column>
          <el-table-column label="属性代码" prop="ref_name"></el-table-column>
          <el-table-column label="数据来源" prop="src_id"></el-table-column>
          <el-table-column label="类型" prop="type"></el-table-column>
          <el-table-column label="存储字段" prop="fd_name"></el-table-column>
          <el-table-column label="关联代码集" prop="code" :formatter="formatterCode"></el-table-column>
          <el-table-column label="默认值" prop="src_default"></el-table-column>
          <el-table-column label="处理函数" prop="genesisrul"></el-table-column>
        </el-table>
      </el-main>
    </el-container>
    <el-dialog :title="tmTitle" :visible.sync="tmDialogVisible" @open="openTmDialog">
      <el-form :model="tmForm"  :label-width="formLabelWidth" :rules="tmRules" style="text-align: left;" >
        <el-form-item label="交易名称" prop="tab_name" class="hidden">
          <el-input v-model="tmForm.tab_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="属性名称" prop="name">
          <el-input v-model="tmForm.name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="属性代码" prop="ref_name">
          <el-input v-model="tmForm.ref_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="数据来源" prop="src_id">
          <el-input v-model="tmForm.src_id" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="tmForm.type" placeholder="请选择" @change="tmTypeChange">
            <el-option
              v-for="item in tmTypeList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="存储字段" prop="fd_name">
          <el-select v-model="tmForm.fd_name" placeholder="请选择">
            <el-option
              v-for="item in tmFdNameList"
              :key="item.fd_name"
              :label="item.fd_name"
              :value="item.fd_name">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联代码集" prop="code" v-bind:class="{hidden:tmCodeVisible}">
          <el-select v-model="tmForm.code" placeholder="请选择">
            <el-option
              v-for="item in tmCodeList"
              :key="item.category_id"
              :label="item.category_name"
              :value="item.category_id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="默认值"  prop="src_default">
          <el-select v-if="tmForm.type.toUpperCase() === 'CODE'" v-model="tmForm.src_default" placeholder="请选择">
            <el-option
              v-for="item in codeDefaultList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <el-input v-else v-model="tmForm.src_default" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="处理函数" prop="genesisrul" v-bind:class="{hidden:genesisrulVisible}">
          <el-select v-model="tmForm.genesisrul" placeholder="请选择">
            <el-option
              v-for="item in tmGenesisRulList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理函数参数1" prop="params1" v-bind:class="{hidden:paramsVisible(1)}">
          <el-input v-model="tmForm.params1" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="处理函数参数2" prop="params2" v-bind:class="{hidden:paramsVisible(2)}">
          <el-input v-model="tmForm.params2" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="tmDialogVisible = false">立即创建</el-button>
          <el-button @click="tmDialogVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
    <el-container style="border: 1px solid #eee;">
      <el-header height="30px" class="table-header">
        <div>交易模型引用</div>
      </el-header>
      <el-main style="padding: 0px;">
        <div style="padding-left: 10px;text-align: left;border-bottom: 1px solid #eee;">
          <el-button type="text" class="el-icon-plus" @click="">新建引用表</el-button>
          <el-button type="text" class="el-icon-edit" @click="">编辑引用表</el-button>
          <el-button type="text" class="el-icon-delete" @click="">删除引用表</el-button>
          <el-button type="text" class="el-icon-plus" @click="">新建行字段</el-button>
          <el-button type="text" class="el-icon-edit" @click="">编辑行字段</el-button>
          <el-button type="text" class="el-icon-delete" @click="">删除行字段</el-button>
          <el-button type="text" class="el-icon-search" @click="">查询引用表</el-button>
        </div>
        <el-table :data="refTableData" ref="refTable"
                  :span-method="groupHandle"
                  :row-class-name="groupClassName"
                  @row-dblclick="toggleListHandle"
                  max-height="430"
                  style="width: 100%">
          <el-table-column type="selection" width="35" align="left"></el-table-column>
          <el-table-column label="属性名称" prop="ref_desc">
            <template slot-scope="scope">
              <i v-if="scope.row.group_type==='group'" :class=groupIcon(scope.row)></i>
              <span style="margin-left: 10px">{{scope.row.ref_desc}}</span>
            </template>
          </el-table-column>
          <el-table-column label="属性代码" prop="ref_name"></el-table-column>
          <el-table-column label="数据来源" prop="ref_fd_name"></el-table-column>
          <el-table-column label="条件" prop="src_cond_in"></el-table-column>
          <el-table-column label="表达式" prop="src_expr_in"></el-table-column>
          <el-table-column label="存储字段" prop="storecolumn"></el-table-column>
          <el-table-column label="所属节点" prop="tab_desc"></el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import check from '@/common/check'
  import util from '@/common/util'
  import dictCode from '@/common/dictCode'

  let vm = null
  export default {
    create () {

    },
    computed: {
      txnIdParent () { return this.txnId }, // 交易定义编号
      isVisibilityParent () { return this.isVisibility }, // 功能tab页是否选中（显示）
      tmTableData () { // 交易模型Table数据；根据展开的节点筛选全部数据
        var expendNodeKey = this.expendNodeKey
        var tranModelList = this.tranModelList
        var tableData = []
        for (var i in tranModelList) {              // 取数据列表
          for (var key in tranModelList[i]) {       // 取对象的Key-Value
            // 数据列表List长度大于0或是tranModelList的最后一项
            if (tranModelList[i][key].length > 0 || Number(i) === tranModelList.length - 1) {
              let keys = key.split('____')          // 按格式获取名称
              if (keys && keys.length === 2) {      // 分类节点名称格式正确
                tableData.push({                    // 添加分类节点数据
                  tab_name: keys[0],
                  name: keys[1],
                  group_type: 'group'               // 把当前的数据类型设置为组[group]
                })
                if (expendNodeKey.indexOf(key) === -1) {  // 节点名称是否在展开节点列表中
                  break                                   // 不在列表中则跳出当前循环
                }
                for (var j in tranModelList[i][key]) {    // 添加分类节点下的全部数据到数据列表
                  var item = tranModelList[i][key][j]
                  item.group_type = 'item'                // 把当前的数据类型设置为数据元[item]
                  tableData.push(item)
                }
              }
            }
          }
        }
        return tableData
      },
      tmFdNameList () { // 计算存储字段下拉列表；根据类型计算
        let self = this
        let dataType = self.tmForm.type
        let enableStoreFd = self.enableStoreFd
        let dataTypeClassify = self.dataTypeClassify
        var sfdItems = []
        if (dataType && enableStoreFd) {
          for (let i in enableStoreFd) {
            let fd = enableStoreFd[i]
            for (let d in dataTypeClassify) {
              let dt = dataTypeClassify[d]
              if (dt.type.indexOf(dataType) !== -1) {
                if (dt.recap === 'long' || dt.recap === 'decimal') {
                  if (dataType !== 'double' && dataType !== 'long') {
                    if (fd['type'] === dataType) {
                      sfdItems.push(fd)
                    }
                  }
                  if (fd['type'] === 'double' || fd['type'] === 'long') {
                    sfdItems.push(fd)
                  }
                } else if (dt.recap === 'string') {
                  if (dataType !== 'string') {
                    if (fd['type'] === dataType) {
                      sfdItems.push(fd)
                    }
                  }
                }
                if (fd['type'] === 'string') {
                  sfdItems.push(fd)
                }
              }
            }
          }
        } else {
          // $.extend(sfdItems, enableStoreFd)
        }
        return sfdItems
      },
      codeDefaultList () {
        let type = this.tmForm.type
        if (type !== 'code') {
          return []
        }
        let code = this.tmForm.code
        this.tmForm.src_default = ''
        return dictCode.getCodeItems(code)
      },
      tmCodeVisible () { // 类型为【代码类型】时，显示关联代码集
        let type = this.tmForm.type
        return !type || type.toUpperCase() !== 'CODE'
      },
      genesisrulVisible () { // 根据韩式下拉列表值得数量判断显隐
        return this.tmGenesisRulList.length === 0
      },
      tmGenesisRulList () { // 处理函数列表，根据类型的值筛选
        let type = this.tmForm.type
        let func = this.func
        let list = []
        for (let i in func) {
          if (func[i].func_type === type) {
            list.push({
              label: func[i].func_name,
              value: func[i].func_code
            })
          }
        }
        this.tmForm.genesisrul = ''
        return list
      }
    },
    props: ['txnId', 'isVisibility'], // 父组件传递的参数【交易定义编号，功能显示标识】
    data () {
      // 关联代码集合法校验
      let codeCheck = (rule, value, callback) => {
        var self = this
        if (value.trim() === '' && self.tmForm.type.toUpperCase() === 'CODE') {
          return callback(new Error('数据类型为代码类型时，关联代码集不能为空'))
        } else {
          callback()
        }
      }
      // 判断存储字段是否合法
      let tmFdNameCheck = (rule, value, callback) => {
        let self = this
        let list = self.tmFdNameList
        for (let i in list) {
          if (list[i].fd_name === value) {
            callback()
          }
        }
        return callback(new Error('存储字段对应类型和数据类型不一致'))
      }
      // 判断函数是否存在
      let genesisrulCheck = (rule, value, callback) => {
        let self = this
        let list = self.tmGenesisRulList
        for (let i in list) {
          if (list[i].fd_name === value) {
            callback()
          }
        }
        return callback(new Error('处理函数不存在'))
      }
      // 判断函数参数1合法
      let paramsCheck1 = (rule, value, callback) => {
        let self = this
        if (self.paramsVisible(1)) { // 隐藏参数
          callback()
        }
        if (util.trim(value) === '') {
          callback(new Error('参数不允许为空'))
        }
        let funcParam = self.funcParam // 所有的参数集合
        let paramsList = [] // 使用的参数列表
        for (let i in funcParam) {
          if (funcParam[i].func_code === self.tmForm.genesisrul) {
            paramsList.push(funcParam[i])
          }
        }
        paramsList = util.orderList(paramsList, 'param_orderby') // 排序
        let dataTypeClassify = self.dataTypeClassify
        let paramType = paramsList[0].param_type
        for (let i in dataTypeClassify) {
          let dt = dataTypeClassify[i]
          if (dt.type.indexOf(paramType) > -1) {
            if (dt.recap === 'long' || dt.recap === 'decimal') {
              if (dt.recap === 'long' && !/^[0-9]*$/.test(value)) {
                callback(new Error('处理函数参数1只能填写非负整数'))
              }
              if (dt.recap === 'decimal' && !/^\d+(\.\d+)?$/.test(value)) {
                callback(new Error('处理函数参数1只能填写非负数'))
              }
              if ((value * 1 + '') !== (value + '')) {
                callback(new Error('处理函数参数1所填数据的格式不正确')) // 说明，数字是已0开头的
              }
              if (self.tmForm.params2 !== '') {
                let params1 = Number(value)
                let params2 = Number(self.tmForm.params2)
                if (params2 < params1) {
                  callback(new Error('处理函数参数1必须小于参数2')) // 后面的值小于等于前面的值
                }
              }
            } else if (dt.recap === 'string') {
              if (!(value.charAt(0) === '"' && value.charAt(value.length - 1) === '"')) {
                callback(new Error('处理函数参数为字符类型，请使用双引号包裹，如：["\' + value + \'"]'))
              }
            }
          }
        }
        callback()
      }
      // 判断函数参数2合法
      let paramsCheck2 = (rule, value, callback) => {
        let self = this
        if (self.paramsVisible(2)) { // 隐藏参数
          callback()
        }
        if (util.trim(value) === '') {
          callback(new Error('参数不允许为空'))
        }
        let funcParam = self.funcParam // 所有的参数集合
        let paramsList = [] // 使用的参数列表
        for (let i in funcParam) {
          if (funcParam[i].func_code === self.tmForm.genesisrul) {
            paramsList.push(funcParam[i])
          }
        }
        paramsList = util.orderList(paramsList, 'param_orderby') // 排序
        let dataTypeClassify = self.dataTypeClassify
        let paramType = paramsList[0].param_type
        for (let i in dataTypeClassify) {
          let dt = dataTypeClassify[i]
          if (dt.type.indexOf(paramType) > -1) {
            if (dt.recap === 'long' || dt.recap === 'decimal') {
              if (dt.recap === 'long' && !/^[0-9]*$/.test(value)) {
                callback(new Error('处理函数参数1只能填写非负整数'))
              }
              if (dt.recap === 'decimal' && !/^\d+(\.\d+)?$/.test(value)) {
                callback(new Error('处理函数参数2只能填写非负数'))
              }
              if ((value * 1 + '') !== (value + '')) {
                callback(new Error('处理函数参数2所填数据的格式不正确')) // 说明，数字是已0开头的
              }
              if (self.tmForm.params1 !== '') {
                let params1 = Number(self.tmForm.params1)
                let params2 = Number(value)
                if (params2 < params1) {
                  callback(new Error('处理函数参数2必须大于参数1')) // 后面的值小于等于前面的值
                }
              }
            } else if (dt.recap === 'string') {
              if (!(value.charAt(0) === '"' && value.charAt(value.length - 1) === '"')) {
                callback(new Error('处理函数参数为字符类型，请使用双引号包裹，如：["\' + value + \'"]'))
              }
            }
          }
        }
        callback()
      }
      return {
        toggleIcon: ['el-icon-arrow-down', 'el-icon-arrow-right'], // 交易模型Table中分类[分组]中：展开与收起的Icon
        tranModelList: [],      // 交易模型列表
        expendNodeKey: [],      // 展开的交易模型分类节点
        tmTitle: '',            // 交易模型弹窗标题
        tmDialogVisible: false, // 交易模型弹窗显隐
        tmForm: {
          params1: '',
          params2: '',
          tab_name: '',
          name: '',
          ref_name: '',
          src_id: '',
          type: '',
          fd_name: '',
          code: '',
          src_default: '',
          genesisrul: ''
        },             // 交易模型表单
        tmTypeList: [],         // 交易模型-类型列表
        allStoreFd: [],         // 全部存储字段
        enableStoreFd: [],      // 可用存储字段
        tmCodeList: [],         // 关联代码集
        refTab: [],             // 引用表
        refFd: [],              // 引用字段
        canRefTab: [],          // 可引用表
        canRefTabFd: [],        // 可引用表中的字段
        func: [],               // 函数
        funcParam: [],          // 函数参数
        refTableData: [],       // 交易模型引用表数据
        dataTypeClassify: [     // 数据类型归类
          {recap: 'long', type: ['long', 'time', 'datetime']},
          {recap: 'decimal', type: ['double', 'money']},
          {recap: 'string', type: ['string', 'devid', 'ip', 'userid', 'acc', 'code']}
        ],
        formLabelWidth: '120px',
        tmRules: {
          name: [
            {required: true, message: '请输入属性名称', trigger: 'blur'},
            {validator: check.checkFormZhSpecialCharacter, trigger: 'blur'}, // 输入格式校验：只能包含汉字,字母,数字和下划线
            {max: 50, message: '属性名称不能超过20个字符', trigger: 'blur'}
          ],
          ref_name: [
            {required: true, message: '请输入属性代码', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {max: 50, message: '属性代码不能超过20个字符', trigger: 'blur'}
          ],
          src_id: [
            {required: true, message: '请输入数据来源', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {max: 20, message: '数据来源不能超过20个字符', trigger: 'blur'}
          ],
          type: [
            {required: true, message: '请选择类型', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {max: 20, message: '类型不能超过20个字符', trigger: 'blur'}
          ],
          fd_name: [
            {required: true, message: '请选择存储字段', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {validator: tmFdNameCheck, trigger: 'blur'}, // 判断存储字段是否合法
            {max: 50, message: '存储字段不能超过20个字符', trigger: 'blur'}
          ],
          code: [
            {required: true, message: '请选择关联代码集', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'}, // 特殊字符校验
            {validator: codeCheck, trigger: 'blur'}, // 关联代码集合法校验
            {max: 50, message: '关联代码集不能超过20个字符', trigger: 'blur'}
          ],
          src_default: [
            {validator: check.checkFormSpecialCode, trigger: 'blur'}, // 特殊字符校验,
            {max: 50, message: '默认值不能超过20个字符', trigger: 'blur'}
          ],
          genesisrul: [
            {validator: genesisrulCheck, trigger: 'blur'} // 判断函数是否存在
          ],
          params1: [
            {max: 20, message: '处理函数参数不能超过20个字符', trigger: 'blur'},
            {validator: paramsCheck1, trigger: 'blur'}
          ],
          params2: [
            {max: 20, message: '处理函数参数不能超过20个字符', trigger: 'blur'},
            {validator: paramsCheck2, trigger: 'blur'}
          ]
        }
      }
    },
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
      initForm () { // 初始化当前功能页面
        var self = this
        ajax.post({
          url: '/tranmdl/query', // 临时库
          param: {
            tab_name: self.txnId
          },
          success: function (data) {
            var tranModelList = data.txnfds
            if (tranModelList) {
              self.tranModelList = tranModelList  // 交易模型列表
              let expendNodeKey = []
              for (let i in tranModelList) {
                for (let key in tranModelList[i]) {
                  expendNodeKey.push(key)         // 添加所有的【交易模型定义分类名称】到【展开列表中】
                }
              }
              self.expendNodeKey = expendNodeKey      // 展开列表中（展开的模型分类节点名称）；默认展开全部
              self.refTab = data.txn_ref_tab          // 引用表
              self.refFd = data.txn_ref_fds           // 引用字段
              self.enableStoreFd = data.enablestorefd // 可用存储字段
              self.allStoreFd = data.allstorefd       // 所有的存储字段
            }
            ajax.post({
              url: '/tranmdl/queryOf', // 正式库
              param: {
                tab_name: self.txnId
              },
              success: function (data) {
                self.tmCodeList = data.sourcetype // 关联代码集
                self.canRefTab = data.canreftable // 可引用表
                self.canRefTabFd = data.reftblfd  // 可引用表中的字段
                self.func = data.func             // 函数
                self.funcParam = data.funcparam   // 函数参数
              }
            })
          }
        })
      },
      groupHandle ({ row, column, rowIndex, columnIndex }) {
        if (row.group_type === 'group') { // 行的属性[group_type]值为组[group]的
          switch (columnIndex) {          // 列索引
            case 1 :        // 当前行合并全部列到第一列
              return [1, 9] // 以当前的单元格位置开始【合并行数，合并的列数】；
            default :       // 其他列
              return [0, 0] // 不显示
          }
        }
      },
      groupClassName ({row, rowIndex}) { // 设置分类[组]的样式
        if (row.group_type === 'group') {
          return 'groupStyle'
        } else {
          return ''
        }
      },
      groupIcon (row) {  // 计算组的Icon
        if (row.group_type !== 'group') { // 不是分组行数据
          return ''                       // 空
        }
        let expendNodeKey = this.expendNodeKey      // 展开的组节点
        let key = row.tab_name + '____' + row.name  // 行名称
        if (expendNodeKey.indexOf(key) > -1) {      // 是展开的节点
          return this.toggleIcon[0]
        } else {                                    // 不是展开的节点
          return this.toggleIcon[1]
        }
      },
      toggleListHandle (row, e) {   // 行展开（双击）事件处理
        let self = this
        if (row.group_type !== 'group') { // 不是分类[组]
          return
        }
        let path = e.path
        let cellItem = null   // 单元格
        for (let i in path) { // 根据路径找到td[cell]
          if (path[i].className === 'cell') {
            cellItem = path[i]
          }
        }
        let key = row.tab_name + '____' + row.name // 分类[组]节点名称
        let expendNodeKey = self.expendNodeKey     // 展开的分类[组]名称
        let toggleIcon = self.toggleIcon     // 展开的分类[组]名称
        let iconItem = cellItem.getElementsByTagName('i') // 获取I标签
        if (iconItem[0].getAttribute('class') === toggleIcon[0]) { // 是展开的分类[组]
          if (expendNodeKey.indexOf(key) > -1) {
            expendNodeKey.splice(expendNodeKey.indexOf(key), 1)
          }
        } else if (iconItem[0].getAttribute('class') === toggleIcon[1]) { // 需要展开的行
          expendNodeKey.push(key)
        }
        self.expendNodeKey = expendNodeKey
      },
      formatterCode (row, column) {  // 格式化【关联代码集】数据（table中数据）
        let self = this
        if (row.code === undefined || row.code === null || row.code === '') {
          return ''
        } else {
          let codeList = self.enableStoreFd
          for (let i in codeList) {
            if (codeList[i].category_id === row.code) {
              return codeList[i].category_name
            }
          }
          return row.code
        }
      },
      paramsVisible (index) {
        let self = this
        let funcList = self.func
        let code = self.tmForm.genesisrul
        for (let i in funcList) {
          if (funcList[i].func_code === code) {
            var params = funcList[i].params
            if (!(params === undefined || params === '')) {
              let visible = params.length < index
              if (visible) {
                self.tmForm['params' + index] = ''
              }
              return visible
            }
          }
        }
        self.tmForm['params' + index] = ''
        return true
      },
      tmTypeChange () {
        this.tmForm.src_default = ''
      },
      openTmDialog () { // 当新建交易模型窗口打开时触发
        this.tmTypeList = dictCode.getCodeItems('tms.model.datatype') // 加载类型下拉列表
        this.getFuncList()
      },
      getTmFdNameList (tabName) { // 查询存储字段列表
        let self = this
        ajax.post({
          url: '/tranmdl/queryAvailableStoreFd',
          param: {
            tab_name: (tabName?tabName:self.txnId)
          },
          success: function (data) {
            self.enableStoreFd = data.enableStoreFd // 存储字段
          }
        })
      },
      getFuncList () {
        let self = this
        ajax.post({
          url: '/tranmdl/queryFunWithParam',
          success: function (data) {
            self.func = data.funwithparam
          }
        })
      },
      tmAddFunc () {  // 添加交易模型定义事件处理
        let self = this
        self.tmTitle = '新建交易模型'
        self.tmDialogVisible = true
        self.tmFormReadOnly = false
      },
      tmEditFunc () { // 编辑交易模型定义事件处理
        let self = this
        self.tmTitle = '编辑交易模型'
        self.tmDialogVisible = true
        self.tmFormReadOnly = false
      },
      tmDelFunc () { // 删除交易模型定义事件处理
        // let self = this
        console.info('删除交易模型')
      },
      tmInfoFunc () { // 查看交易模型定义事件处理
        let self = this
        self.tmTitle = '交易模型信息'
        self.tmDialogVisible = true
        self.tmFormReadOnly = true
      }
    }
  }
</script>

<style>
  .hidden {
    display: none;
  }
  tbody .groupStyle {
    background-color: #f5f7fa !important;
    text-align: left;
  }
  tbody .groupStyle .cell {
    padding-left: 15px;
  }

  .table-header {
    text-align: left;
    line-height: 2em;
    background-color: aquamarine;
  }
</style>
