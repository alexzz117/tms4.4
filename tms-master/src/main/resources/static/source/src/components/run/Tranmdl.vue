<template>
  <div style="margin-top: -15px;">
    <el-collapse v-model="activeName" accordion>
      <el-collapse-item name="1">
        <template slot="title">
          <div style="margin:10px;text-align: left ">
            <span style="margin-right: 10px">交易模型</span>
            <el-button plain class="el-icon-plus" @click.stop="tmAddFunc" :disabled="readonly">新建</el-button>
          </div>
        </template>
        <div>
          <el-table :data="tmTableData" ref="tmTable"
                    :span-method="groupHandle"
                    :row-class-name="groupClassName"
                    @row-click="toggleListHandle"
                    max-height="550"
                    class="collapse-table-style">
            <el-table-column align="left" width="100px" label="操作">
              <template slot-scope="scope">
                <el-button type="text" icon="el-icon-edit" :disabled="getToolBtnVisible(scope.$index, scope.row)" title="编辑" @click="tmEditFunc(scope.$index, scope.row)"></el-button>
                <el-button type="text" icon="el-icon-delete" :disabled="getToolBtnVisible(scope.$index, scope.row)" title="删除" @click="tmDelFunc(scope.$index, scope.row)"></el-button>
                <el-button type="text" icon="el-icon-zoom-in" title="查看" @click="tmInfoFunc(scope.$index, scope.row)"></el-button>
              </template>
            </el-table-column>
            <el-table-column align="left"  label="属性名称" prop="name">
              <template slot-scope="scope">
                <i v-if="scope.row.group_type==='group'" :class=groupIcon(scope.row,expendNodeKey)></i>
                <span>{{scope.row.name}}</span>
              </template>
            </el-table-column>
            <el-table-column align="left" label="属性代码" prop="ref_name"></el-table-column>
            <el-table-column align="left" label="数据来源" prop="src_id"></el-table-column>
            <el-table-column align="left" label="类型" prop="type"></el-table-column>
            <el-table-column align="left" label="存储字段" prop="fd_name"></el-table-column>
            <el-table-column align="left" label="关联代码集" prop="code" :formatter="formatterCode"></el-table-column>
            <el-table-column align="left" label="默认值" prop="src_default"></el-table-column>
            <el-table-column align="left" label="处理函数" prop="genesisrul"></el-table-column>
          </el-table>
        </div>
      </el-collapse-item>
      <el-collapse-item name="2">
        <template slot="title">
          <div style="float: left">
            <span style="margin-right: 10px">交易模型引用</span>
            <el-button class="el-icon-plus" @click.stop="tableAddFunc" :disabled="readonly">新建</el-button>
          </div>
        </template>
        <div>
          <el-table :data="tableData" ref="refTable"
                    :span-method="tableGroupHandle"
                    :row-class-name="groupClassName"
                    @row-click="toggleTableHandle"
                    max-height="335"
                    class="collapse-table-style">
            <el-table-column align="left" width="125px" label="操作">
              <template slot-scope="scope">
                <el-button type="text" icon="el-icon-edit" title="编辑" @click="tableEditFunc(scope.row)" :disabled="readonly"></el-button>
                <el-button type="text" icon="el-icon-delete" title="删除" @click="tableDelFunc(scope.row)" :disabled="readonly"></el-button>
                <el-button type="text" icon="el-icon-zoom-in" title="查看" @click="tableInfoFunc(scope.row)"></el-button>
                <el-button v-if="scope.row.group_type==='group'" type="text" icon="el-icon-plus" title="新建行字段" @click="tableSetFunc(scope.row)" :disabled="readonly"></el-button>
              </template>
            </el-table-column>
            <el-table-column align="left" label="属性名称" prop="ref_desc">
              <template slot-scope="scope">
                <i v-if="scope.row.group_type==='group'" :class=groupIcon(scope.row,expendTableKey)></i>
                <span v-if="scope.row.group_type==='group'">{{scope.row.group_name}}</span>
                <span v-else>{{scope.row.ref_desc}}</span>
              </template>
            </el-table-column>
            <el-table-column label="属性代码" prop="ref_name" align="left"></el-table-column>
            <el-table-column label="数据来源" prop="ref_fd_name" align="left"></el-table-column>
            <el-table-column label="条件" prop="src_cond_in" align="left"></el-table-column>
            <el-table-column label="表达式" prop="src_expr_in" align="left"></el-table-column>
            <el-table-column label="存储字段" prop="storecolumn" align="left"></el-table-column>
            <el-table-column label="所属节点" prop="tab_desc" align="left"></el-table-column>
          </el-table>
        </div>
      </el-collapse-item>
    </el-collapse>
    <el-dialog :title="tmTitle" :visible.sync="tmDialogVisible" @open="openTmDialog" :close-on-click-modal="false">
      <el-form :model="tmForm"  :label-width="formLabelWidth" :rules="tmRules" ref="tmForm">
        <el-form-item label="交易名称" prop="tab_name" class="hidden">
          <el-input v-model="tmForm.tab_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="属性名称" prop="name">
              <el-input v-model="tmForm.name" :disabled="tmFormReadOnly" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="属性代码" prop="ref_name">
              <el-input v-model="tmForm.ref_name" :disabled="tmFormReadOnly" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据来源" prop="src_id">
              <el-input v-model="tmForm.src_id" :disabled="tmFormReadOnly" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="tmForm.type" :disabled="tmFormReadOnly" placeholder="请选择" @change="tmTypeChange" clearable>
                <el-option
                  v-for="item in tmTypeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="存储字段" prop="fd_name">
              <el-select v-model="tmForm.fd_name" :disabled="tmFormReadOnly" placeholder="请选择" clearable>
                <el-option
                  v-for="item in tmFdNameList"
                  :key="item.fd_name"
                  :label="item.fd_name"
                  :value="item.fd_name">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联代码集" prop="code" :disabled="tmFormReadOnly" v-bind:class="{hidden:tmCodeVisible}" :required="!tmCodeVisible">
              <el-select v-model="tmForm.code" placeholder="请选择" clearable>
                <el-option
                  v-for="item in tmCodeList"
                  :key="item.category_id"
                  :label="item.category_name"
                  :value="item.category_id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理函数" prop="genesisrul" :disabled="tmFormReadOnly" v-bind:class="{hidden:tmFuncVisible}">
              <el-select v-model="tmForm.genesisrul" placeholder="请选择" @change="genesisRulChange" clearable>
                <el-option
                  v-for="item in tmFuncList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="处理函数参数1" prop="params1" :disabled="tmFormReadOnly" v-bind:class="{hidden:paramsVisible(1)}">
              <el-input v-model="tmForm.params1" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理函数参数2" prop="params2" :disabled="tmFormReadOnly" v-bind:class="{hidden:paramsVisible(2)}">
              <el-input v-model="tmForm.params2" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认值"  prop="src_default" :disabled="tmFormReadOnly">
              <el-select v-if="tmForm.type.toUpperCase() === 'CODE'" v-model="tmForm.src_default" placeholder="请选择" clearable>
                <el-option
                  v-for="item in codeDefaultList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
              <el-input v-else v-model="tmForm.src_default" :disabled="tmFormReadOnly" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-show="!tmFormReadOnly">
          <el-col :span="24">
            <el-form-item>
              <el-button type="primary" @click="submitTmForm" size="large">保存</el-button>
              <el-button @click="tmDialogVisible = false" size="large">取消</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-dialog>
    <el-dialog :title="tableTitle" :visible.sync="tableDialogVisible" @open="tableOpenFunc" :close-on-click-modal="false">
      <el-form :model="tableForm"  :label-width="formLabelWidth" :rules="tableRules" ref="tableForm">
        <el-form-item label="引用ID" prop="ref_id" class="hidden">
          <el-input v-model="tableForm.ref_id" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="所属交易" prop="tab_name" class="hidden">
          <el-input v-model="tableForm.tab_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="txn_order" class="hidden">
          <el-input v-model="tableForm.txn_order" auto-complete="off"></el-input>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="引用表" prop="ref_tab_name">
              <el-select v-model="tableForm.ref_tab_name" :disabled="tableFormReadOnly" @change="refTableNameChange" placeholder="请选择" clearable>
                <el-option
                  v-for="item in canRefTab"
                  :key="item.tab_name"
                  :label="item.tab_desc"
                  :value="item.tab_name">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="引用描述" prop="ref_desc">
              <el-input v-model="tableForm.ref_desc" :readonly="tableFormReadOnly" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="引用字段" prop="src_expr">
              <el-select v-model="tableForm.src_expr" :disabled="tableFormReadOnly" placeholder="请选择" clearable>
                <el-option
                  v-for="item in canRefFd"
                  :key="item.fd_name"
                  :label="item.name"
                  :value="item.fd_name">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属节点" prop="tab_desc">
              <el-input v-model="tableForm.tab_desc" :disabled="true" auto-complete="off"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :class="{hidden:tableFormReadOnly}">
          <el-col :span="24">
            <el-form-item>
              <el-button type="primary" @click="submitTableForm" size="large">保存</el-button>
              <el-button @click="tableDialogVisible = false" size="large">取消</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-dialog>
    <el-dialog :title="tableInfoTitle" :visible.sync="tableInfoDialogVisible" @open="tableOpenFunc" :close-on-click-modal="false">
      <el-form :model="tableInfoForm"  :label-width="formLabelWidth" :rules="tableInfoRules" ref="tableInfoForm">
        <el-form-item label="引用ID" prop="ref_id" class="hidden">
          <el-input v-model="tableInfoForm.ref_id" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="所属交易" prop="tab_name" class="hidden">
          <el-input v-model="tableInfoForm.tab_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="引用表描述" prop="ref_tab_desc" class="hidden">
          <el-input v-model="tableInfoForm.ref_tab_desc" auto-complete="off"></el-input>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据来源" prop="ref_fd_name">
              <el-select v-model="tableInfoForm.ref_fd_name" :disabled="tableInfoFormReadOnly" @change="canRefTabFdChange" placeholder="请选择" clearable>
                <el-option
                  v-for="item in canRefTabFdList"
                  :key="item.fd_name"
                  :label="item.name"
                  :value="item.fd_name">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="属性名称" prop="ref_desc">
              <el-input v-model="tableInfoForm.ref_desc" :readonly="tableInfoFormReadOnly" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="属性代码" prop="ref_name">
              <el-input v-model="tableInfoForm.ref_name" :readonly="tableInfoFormReadOnly" auto-complete="off" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="条件" prop="src_cond_in">
              <div @dblclick="showStatCondDialog">
                <el-input v-model="tableInfoForm.src_cond_in" :readonly="true" auto-complete="off" clearable></el-input>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="表达式" prop="src_expr_in">
              <div @dblclick="showSrcExprDialog">
                <el-input v-model="tableInfoForm.src_expr_in" :readonly="true" auto-complete="off" clearable></el-input>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存储字段" prop="storecolumn">
              <el-select v-model="tableInfoForm.storecolumn" :disabled="tableInfoFormReadOnly" placeholder="请选择" clearable>
                <el-option
                  v-for="item in storeColumnList"
                  :key="item.fd_name"
                  :label="item.fd_name"
                  :value="item.fd_name">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属节点" prop="tab_desc">
              <el-input v-model="tableInfoForm.tab_desc" :disabled="true" auto-complete="off"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :class="{hidden:tableInfoFormReadOnly}">
          <el-col :span="24">
            <el-form-item>
              <el-button type="primary" @click="submitTableInfoForm" size="large">保存</el-button>
              <el-button @click="tableInfoDialogVisible = false" size="large">取消</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-dialog>
    <StatCondPicker ref="StatCondDialog" @valueCallback="statCondInValueCallBack"
                    :statCond="tableInfoForm.src_cond" :statCondIn="tableInfoForm.src_cond_in" :txnId="txnId"
                    :hideItems="['rule_func', 'ac_func', 'stat_fn', 'roster_func' , 'diy_func']" >

    </StatCondPicker>
    <StatCondPicker ref="SrcExprDialog" @valueCallback="SrcExprInValueCallBack"
                    :statCond="tableInfoForm.src_expr" :statCondIn="tableInfoForm.src_expr_in" :txnId="txnId"
                    :hideItems="['rule_func', 'ac_func', 'stat_fn', 'roster_func' , 'diy_func']" >

    </StatCondPicker>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import check from '@/common/check'
  import util from '@/common/util'
  import dictCode from '@/common/dictCode'
  import StatCondPicker from '@/components/common/StatCondPicker'
  import FuncParamPicker from '@/components/common/FuncParamPicker'

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
                  group_id: keys[0],
                  group_type: 'group'               // 把当前的数据类型设置为组[group]
                })
                if (expendNodeKey.indexOf(keys[0]) === -1) {  // 节点名称是否在展开节点列表中
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
        let fdName = self.tmForm.old_fd_name
        return self.getFdNameList(dataType, enableStoreFd, fdName)
      },
      codeDefaultList () { // 默认值
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
      tmFuncVisible () { // 根据函数下拉列表值得数量判断显隐
        return this.tmFuncList.length === 0
      },
      tmFuncList () { // 处理函数列表，根据类型的值筛选
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
        return list
      },
      tableData () { // 交易模型引用表数据
        var expendTableKey = this.expendTableKey
        var list = this.tableList
        var tableData = []
        for (var i in list) {
          var group = list[i]
          tableData.push(group)
          if (expendTableKey.indexOf(group.group_id) > -1 && group.children) {
            tableData = tableData.concat(group.children)
          }
        }
        return tableData
      },
      canRefTabFdList () {
        let tabName = this.tableInfoForm.ref_tab_name
        let canRefTabFd = this.canRefTabFd
        let list = []
        for (let i in canRefTabFd) {
          let item = canRefTabFd[i]
          if (item.tab_name === tabName) {
            list.push({
              fd_name: item.fd_name,
              name: item.name
            })
          }
        }
        return list
      },
      storeColumnList () { // 计算存储字段下拉列表；根据类型计算
        let self = this
        let dataType = ''
        let canRefTabFd = this.canRefTabFd
        let refFdName = self.tableInfoForm.ref_fd_name // 数据来源名称
        for (let i in canRefTabFd) {
          let item = canRefTabFd[i]
          if (item.fd_name === refFdName) {
            dataType = canRefTabFd[i].type
            break
          }
        }
        let enableStoreFd = self.enableStoreFd
        let fdName = self.tableInfoForm.old_storecolumn
        if (dataType !== '' && enableStoreFd.length > 0) {
          return self.getFdNameList(dataType, enableStoreFd, fdName)
        } else {
          return []
        }
      }
    },
    props: ['txnId', 'isVisibility', 'txnName', 'readonly'], // 父组件传递的参数【交易定义编号，功能显示标识,功能名称】
    data () {
      // 关联代码集合法校验
      let codeCheck = (rule, value, callback) => {
        var self = this
        if (value.trim() === '' && self.tmForm.type.toUpperCase() === 'CODE') {
          return callback(new Error('数据类型为代码类型时，关联代码集不能为空'))
        } else {
          return callback()
        }
      }
      // 判断存储字段是否合法
      let tmFdNameCheck = (rule, value, callback) => {
        let self = this
        let list = self.tmFdNameList
        for (let i in list) {
          if (list[i].fd_name === value) {
            return callback()
          }
        }
        return callback(new Error('存储字段对应类型和数据类型不一致'))
      }
      // 判断函数是否存在
      let genesisrulCheck = (rule, value, callback) => {
        if (util.trim(value) === '') {
          return callback()
        }
        let self = this
        let list = self.tmFuncList
        for (let i in list) {
          if (list[i].value === value) {
            return callback()
          }
        }
        return callback(new Error('处理函数不存在'))
      }
      // 判断函数参数1合法
      let paramsCheck1 = (rule, value, callback) => {
        let self = this
        if (self.paramsVisible(1)) { // 隐藏参数
          return callback()
        }
        if (util.trim(value) === '') {
          return callback(new Error('请输入参数'))
        }
        let funcParam = self.funcParam // 所有的参数集合
        let paramsList = [] // 使用的参数列表
        for (let i in funcParam) {
          if (funcParam[i].func_code === self.tmForm.genesisrul) {
            paramsList.push(funcParam[i])
          }
        }
        if (paramsList.length === 0) {
          return callback()
        }
        paramsList = util.orderList(paramsList, 'param_orderby') // 排序
        let dataTypeClassify = self.dataTypeClassify
        let paramType = paramsList[0].param_type
        for (let i in dataTypeClassify) {
          let dt = dataTypeClassify[i]
          if (dt.type.indexOf(paramType) > -1) {
            if (dt.recap === 'long' || dt.recap === 'decimal') {
              if (dt.recap === 'long' && !/^[0-9]*$/.test(value)) {
                return callback(new Error('处理函数参数1只能填写非负整数'))
              }
              if (dt.recap === 'decimal' && !/^\d+(\.\d+)?$/.test(value)) {
                return callback(new Error('处理函数参数1只能填写非负数'))
              }
              if ((value * 1 + '') !== (value + '')) {
                return callback(new Error('处理函数参数1所填数据的格式不正确')) // 说明，数字是已0开头的
              }
              if (self.tmForm.params2 !== '') {
                let params1 = Number(value)
                let params2 = Number(self.tmForm.params2)
                if (params2 < params1) {
                  return callback(new Error('处理函数参数1必须小于参数2')) // 后面的值小于等于前面的值
                }
              }
            } else if (dt.recap === 'string') {
              if (!(value.charAt(0) === '"' && value.charAt(value.length - 1) === '"')) {
                return callback(new Error('处理函数参数为字符类型，请使用双引号包裹，如：["\' + value + \'"]'))
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
          return callback()
        }
        if (util.trim(value) === '') {
          return callback(new Error('请输入参数'))
        }
        let funcParam = self.funcParam // 所有的参数集合
        let paramsList = [] // 使用的参数列表
        for (let i in funcParam) {
          if (funcParam[i].func_code === self.tmForm.genesisrul) {
            paramsList.push(funcParam[i])
          }
        }
        if (paramsList.length === 0) {
          return callback()
        }
        paramsList = util.orderList(paramsList, 'param_orderby') // 排序
        let dataTypeClassify = self.dataTypeClassify
        let paramType = paramsList[0].param_type
        for (let i in dataTypeClassify) {
          let dt = dataTypeClassify[i]
          if (dt.type.indexOf(paramType) > -1) {
            if (dt.recap === 'long' || dt.recap === 'decimal') {
              if (dt.recap === 'long' && !/^[0-9]*$/.test(value)) {
                return callback(new Error('处理函数参数1只能填写非负整数'))
              }
              if (dt.recap === 'decimal' && !/^\d+(\.\d+)?$/.test(value)) {
                return callback(new Error('处理函数参数2只能填写非负数'))
              }
              if ((value * 1 + '') !== (value + '')) {
                return callback(new Error('处理函数参数2所填数据的格式不正确')) // 说明，数字是已0开头的
              }
              if (self.tmForm.params1 !== '') {
                let params1 = Number(self.tmForm.params1)
                let params2 = Number(value)
                if (params2 < params1) {
                  return callback(new Error('处理函数参数2必须大于参数1')) // 后面的值小于等于前面的值
                }
              }
            } else if (dt.recap === 'string') {
              if (!(value.charAt(0) === '"' && value.charAt(value.length - 1) === '"')) {
                return callback(new Error('处理函数参数为字符类型，请使用双引号包裹，如：["\' + value + \'"]'))
              }
            }
          }
        }
        callback()
      }
      // 判断表达式是否合法
      let tableSrcExprCheck = (rule, value, callback) => {
        let info = this.tableInfoForm
        if (info.src_expr === '' && info.src_cond !== '') {
          return callback(new Error('填写条件后，表达式不能为空'))
        }
        return callback()
      }
      return {
        activeName: '1',
        op: '',
        toggleIcon: ['el-icon-caret-bottom', 'el-icon-caret-right'], // 交易模型Table中分类[分组]中：展开与收起的Icon
        tranModelList: [],      // 交易模型列表
        expendNodeKey: [],      // 展开的交易模型分类节点
        tmTitle: '',            // 交易模型弹窗标题
        tmDialogVisible: false, // 交易模型弹窗显隐
        tmFormReadOnly: true,   // 表单只读属性
        selectTmRows: [],
        tmForm: {
          params1: '',
          params2: '',
          tab_name: '',
          name: '',
          ref_name: '',
          src_id: '',
          type: '',
          old_fd_name: '',
          fd_name: '',
          code: '',
          src_default: '',
          genesisrul: ''
        },             // 交易模型表单
        tmTypeList: [],         // 交易模型-类型列表
        allStoreFd: [],         // 全部存储字段
        enableStoreFd: [],      // 可用存储字段
        tmCodeList: [],         // 关联代码集
        func: [],               // 函数
        funcParam: [],          // 函数参数
        dataTypeClassify: [     // 数据类型归类
          {recap: 'long', type: ['long', 'time', 'datetime']},
          {recap: 'decimal', type: ['double', 'money']},
          {recap: 'string', type: ['string', 'devid', 'ip', 'userid', 'acc', 'code']}
        ],
        canRefTab: [],          // 可引用表
        canRefFd: [],           // 可引用表中的字段
        canRefTabFd: [],        // 可引用表中的属性
        tableList: [],          // 交易模型引用数据
        expendTableKey: [],     // 展开的节点
        tableTitle: '',
        tableDialogVisible: false,
        tableFormReadOnly: false,
        tableForm: {
          ref_id: '',
          tab_name: '',
          txn_order: '',
          ref_tab_name: '',
          ref_desc: '',
          src_expr: '',
          tab_desc: ''
        },
        tableInfoDialogVisible: false,
        tableInfoTitle: '',
        tableInfoFormReadOnly: false,
        selectTableInfoRows: [],
        tableInfoForm: {
          op: '',
          ref_id: '',
          tab_name: '',
          ref_tab_name: '',
          ref_tab_desc: '',
          ref_fd_name: '',
          ref_desc: '',
          ref_name: '',
          src_cond: '',
          src_cond_in: '',
          src_expr: '',
          src_expr_in: '',
          old_storecolumn: '',
          storecolumn: '',
          tab_desc: ''
        },
        formLabelWidth: '120px',
        tmRules: {
          name: [
            {required: true, message: '请输入属性名称', trigger: 'blur'},
            {validator: check.checkFormZhSpecialCharacter, trigger: 'blur'}, // 输入格式校验：只能包含汉字,字母,数字和下划线
            {max: 50, message: '长度不能超过20个字符', trigger: 'blur'}
          ],
          ref_name: [
            {required: true, message: '请输入属性代码', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {max: 50, message: '长度不能超过20个字符', trigger: 'blur'}
          ],
          src_id: [
            {required: true, message: '请输入数据来源', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {max: 20, message: '长度不能超过20个字符', trigger: 'blur'}
          ],
          type: [
            {required: true, message: '请选择类型', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {max: 20, message: '长度不能超过20个字符', trigger: 'blur'}
          ],
          fd_name: [
            {required: true, message: '请选择存储字段', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {validator: tmFdNameCheck, trigger: 'blur'}, // 判断存储字段是否合法
            {max: 50, message: '长度不能超过20个字符', trigger: 'blur'}
          ],
          code: [
            {validator: check.checkFormSpecialCode, trigger: 'blur'}, // 特殊字符校验
            {validator: codeCheck, trigger: 'blur'}, // 关联代码集合法校验
            {max: 50, message: '长度不能超过20个字符', trigger: 'blur'}
          ],
          src_default: [
            {validator: check.checkFormSpecialCode, trigger: 'blur'}, // 特殊字符校验,
            {max: 50, message: '长度不能超过20个字符', trigger: 'blur'}
          ],
          genesisrul: [
            {validator: genesisrulCheck, trigger: 'blur'} // 判断函数是否存在
          ],
          params1: [
            {max: 20, message: '长度不能超过20个字符', trigger: 'blur'},
            {validator: paramsCheck1, trigger: 'blur'}
          ],
          params2: [
            {max: 20, message: '长度不能超过20个字符', trigger: 'blur'},
            {validator: paramsCheck2, trigger: 'blur'}
          ]
        },
        tableRules: {
          ref_tab_name: [
            {required: true, message: '请选择引用表', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'} // 输入格式校验：包含字母,数字和下划线
          ],
          ref_desc: [
            {required: true, message: '请输入引用描述', trigger: 'blur'},
            {validator: check.checkFormZhSpecialCharacter, trigger: 'blur'}, // 中文校验
            {max: 64, message: '长度不能超过64个字符', trigger: 'blur'}
          ],
          src_expr: [
            {required: true, message: '请选择引用字段', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'} // 输入格式校验：包含字母,数字和下划线
          ]
        },
        tableInfoRules: {
          ref_fd_name: [
            {required: true, message: '请选择数据来源', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'} // 输入格式校验：包含字母,数字和下划线
          ],
          ref_desc: [
            {required: true, message: '请输入属性名称', trigger: 'blur'},
            {validator: check.checkFormZhSpecialCharacter, trigger: 'blur'}, // 中文校验
            {max: 64, message: '长度不能超过64个字符', trigger: 'blur'}
          ],
          ref_name: [
            {required: true, message: '请输入属性代码', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {max: 50, message: '长度不能超过50个字符', trigger: 'blur'}
          ],
          src_expr_in: [
            {validator: tableSrcExprCheck, trigger: 'blur'} // 空校验
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
        self.activeName = '1'
        ajax.post({
          url: '/tranmdl/query', // 临时库
          loading: true,
          toLowerCase: false,
          param: {
            tab_name: self.txnId
          },
          success: function (result) {
            let data = util.toggleObjKey(result)
            var tranModelList = self.formatterResultList(result.txnfds)
            if (tranModelList) {
              self.tranModelList = tranModelList  // 交易模型列表
              let expendNodeKey = []
              for (let i in tranModelList) {
                for (let key in tranModelList[i]) {
                  expendNodeKey.push(key.split('____')[0])         // 添加所有的【交易模型定义分类名称】到【展开列表中】
                }
              }
              self.expendNodeKey = expendNodeKey      // 展开列表中（展开的模型分类节点名称）；默认展开全部
              var tableObj = self.tableDataFormat(data.txn_ref_tab, data.txn_ref_fds)
              self.tableList = tableObj.tableList
              self.expendTableKey = tableObj.expendKey
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
                self.canRefTabFd = data.reftblfd  // 可引用表中的属性
                self.func = data.func             // 函数
                self.funcParam = data.funcparam   // 函数参数
              },
              fail: function (e) {
                if (e.response.data.message) {
                  self.$message.error(e.response.data.message)
                }
              }
            })
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      initTmForm () { // 初始化交易模型表格
        var self = this
        ajax.post({
          url: '/tranmdl/query', // 临时库
          loading: true,
          toLowerCase: false,
          param: {
            tab_name: self.txnId
          },
          success: function (result) {
            var tranModelList = self.formatterResultList(result.txnfds)
            if (tranModelList) {
              self.tranModelList = tranModelList  // 交易模型列表
            }
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      initTableForm () { // 初始化交易模型引用表格·
        var self = this
        ajax.post({
          url: '/tranmdl/query', // 临时库
          loading: true,
          toLowerCase: false,
          param: {
            tab_name: self.txnId
          },
          success: function (result) {
            let data = util.toggleObjKey(result)
            let txnRefTab = self.formatterResultList(result.txn_ref_tab)
            var tableObj = self.tableDataFormat(txnRefTab, data.txn_ref_fds)
            self.tableList = tableObj.tableList
            self.enableStoreFd = data.enablestorefd // 可用存储字段
            self.allStoreFd = data.allstorefd       // 所有的存储字段
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
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
      groupIcon (row, expendKey) {  // 计算组的Icon
        if (row.group_type !== 'group') { // 不是分组行数据
          return ''                       // 空
        }
        let key = row.group_id // 组ID
        if (expendKey.indexOf(key) > -1) {      // 是展开的节点
          return this.toggleIcon[0]
        } else {                                    // 不是展开的节点
          return this.toggleIcon[1]
        }
      },
      toggleListHandle (row, e) {   // 行展开事件处理
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
        if (cellItem === null) {
          return
        }
        let key = row.group_id// 分类[组]节点名称
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
      getToolBtnVisible (index, row) { // 只读控制方法
        if (this.readonly) {
          return true
        }
        if (row.tab_name === this.txnId && (row.is_sys + '') !== '1') {
          return false
        }
        return true
      },
      tmTypeChange () {
        let self = this
        self.tmForm.src_default = ''
        self.tmForm.fd_name = ''
        self.tmForm.code = ''
        self.tmForm.genesisrul = ''
      },
      genesisRulChange () {
        let fields = this.$refs['tmForm'].fields
        let list = ['params1', 'params2']
        for (let i in fields) {
          if (list.indexOf(fields[i].prop) > -1) {
            fields[i].resetField()
          }
        }
      },
      openTmDialog () { // 当新建交易模型窗口打开时触发
        this.tmTypeList = dictCode.getCodeItems('tms.model.datatype') // 加载类型下拉列表
        this.getFuncList()
        if (this.$refs['tmForm']) { // 如果Form存在，则初始化；第一次打开为undefined
          this.$refs['tmForm'].resetFields()
        }
      },
      getTmFdNameList (tabName) { // 查询存储字段列表
        let self = this
        ajax.post({
          url: '/tranmdl/queryAvailableStoreFd',
          param: {
            tab_name: self.txnId
          },
          success: function (data) {
            self.enableStoreFd = data.enablestorefd // 存储字段
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      getFuncList () {
        let self = this
        ajax.post({
          url: '/tranmdl/queryFunWithParam',
          success: function (data) {
            self.func = data.funwithparam
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      tmAddFunc () {  // 添加交易模型定义事件处理
        let self = this
        self.activeName = '1'
        self.tmTitle = '新建交易模型'
        self.tmDialogVisible = true
        self.tmFormReadOnly = false
        self.op = 'add'
        self.tmForm = {
          params1: '',
          params2: '',
          tab_name: self.txnId,
          name: '',
          ref_name: '',
          src_id: '',
          type: '',
          fd_name: '',
          code: '',
          src_default: '',
          genesisrul: ''
        }
      },
      tmEditFunc (index, row) { // 编辑交易模型定义事件处理
        let self = this
        self.tmTitle = '编辑交易模型'
        self.tmDialogVisible = true
        self.tmFormReadOnly = false
        self.op = 'mod'
        let genesisrul = ''
        let params1 = ''
        let params2 = ''
        let genesisrulStr = row.genesisrul
        if (util.trim(genesisrulStr) !== '') {
          genesisrul = genesisrulStr.substring(0, genesisrulStr.indexOf('('))
          let params = genesisrulStr.substring(genesisrulStr.indexOf('(') + 1, genesisrulStr.indexOf(')')).split(',')
          if (params.length > 1) {
            params1 = params[1]
          }
          if (params.length > 2) {
            params2 = params[2]
          }
        }
        self.tmForm = {
          params1: params1,
          params2: params2,
          tab_name: row.tab_name,
          name: row.name,
          ref_name: row.ref_name,
          src_id: row.src_id,
          type: row.type,
          fd_name: row.fd_name,
          old_fd_name: row.fd_name,
          code: row.code,
          src_default: row.src_default,
          genesisrul: genesisrul
        }
        let timer = window.setInterval(function () {
          if (self.tmForm.params1 === params1 && self.tmForm.params2 === params2) {
            window.clearInterval(timer)
          } else {
            if (self.tmForm.params1 !== params1) {
              self.tmForm.params1 = params1
            } else {
              self.tmForm.params2 = params2
            }
          }
        }, 100)
        self.selectTmRows = [row]
      },
      submitTmForm () {
        let self = this
        this.$refs['tmForm'].validate((valid) => {
          if (valid) {
            let op = self.op
            let rowData = Object.assign({}, self.selectTmRows[0])
            let formData = self.tmForm
            let row = {}
            if (op === 'mod') {
              if (rowData.type !== formData.type && rowData.link.length > 0) {
                this.$confirm('该字段已在自定义查询中引用，确认修改', '提示', {
                  confirmButtonText: '确定',
                  cancelButtonText: '取消',
                  type: 'warning'
                }).then(() => {
                  rowData.link = ''
                }).catch(() => {
                  return false
                })
              }
              row = Object.assign({}, rowData, formData)
              row = Object.assign(row, {old: rowData})
            } else {
              row = Object.assign(row, formData)
            }
            let genesisrul = ''
            if (util.trim(row.genesisrul) !== '') {
              genesisrul = util.trim(row.genesisrul) + '(?'
              if (util.trim(row.params1) !== '') {
                genesisrul = genesisrul + ',' + row.params1
                if (util.trim(row.params2) !== '') {
                  genesisrul = genesisrul + ',' + row.params2
                }
              }
              genesisrul = genesisrul + ')'
            }
            row.genesisrul = genesisrul
            let jsonData = {}
            jsonData[op] = [row]
            ajax.post({
              url: '/tranmdl/saveModel',
              loading: true,
              param: {
                postData: jsonData
              },
              success: function (data) {
                if (op === 'mod') {
                  self.$message.success('编辑成功')
                } else {
                  self.$message.success('创建成功')
                }
                self.initTmForm()
                self.getTmFdNameList()
                self.tmDialogVisible = false
              },
              fail: function (e) {
                if (e.response.data.message) {
                  self.$message.error(e.response.data.message)
                }
              }
            })
          } else {
            return false
          }
        })
      },
      tmDelFunc (index, row) { // 删除交易模型定义事件处理
        let self = this
        this.$confirm('确定删除', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let jsonData = {'del': [row]}
          ajax.post({
            url: '/tranmdl/saveModel',
            loading: true,
            param: {
              postData: jsonData
            },
            success: function (data) {
              self.$message.success('删除成功。')
              self.initTmForm()
              self.getTmFdNameList()
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }).catch(() => {
          return false
        })
      },
      tmInfoFunc (index, row) { // 查看交易模型定义事件处理
        let self = this
        self.tmTitle = '交易模型信息'
        self.tmDialogVisible = true
        self.tmFormReadOnly = true
        self.op = 'info'
        let genesisrul = ''
        let params1 = ''
        let params2 = ''
        let genesisrulStr = row.genesisrul
        if (util.trim(genesisrulStr) !== '') {
          genesisrul = genesisrulStr.substring(0, genesisrulStr.indexOf('('))
          let params = genesisrulStr.substring(genesisrulStr.indexOf('(') + 1, genesisrulStr.indexOf(')'))
          if (params.length > 1) {
            params1 = params[1]
          }
          if (params.length > 2) {
            params2 = params[2]
          }
        }
        self.tmForm = {
          params1: params1,
          params2: params2,
          tab_name: row.tab_name,
          name: row.name,
          ref_name: row.ref_name,
          src_id: row.src_id,
          type: row.type,
          fd_name: row.fd_name,
          code: row.code,
          src_default: row.src_default,
          genesisrul: genesisrul
        }
      },
      /**
       *
       * @param tab 交易模型引用的引用表数据
       * @param data 交易模型引用的引用行数据
       * @returns {{tableList: Array, expendKey: Array}}
       * 交易模型引用Table数据和展开的引用表（全部展开）
       */
      tableDataFormat (tab, data) {
        var tableList = []
        var groupList = []
        for (var i in tab) {
          for (var key in tab[i]) {
            let keys = key.split('____')          // 按格式获取名称
            if (keys && keys.length === 2) {      // 分类节点名称格式正确
              for (var j in tab[i][key]) {
                var tabItem = tab[i][key][j]
                var groupObj = {                  // 添加分类节点数据
                  tab_name: keys[0],
                  tab_desc: keys[1],
                  group_id: tabItem.ref_id,
                  group_name: keys[1] + ':[' + tabItem.ref_desc + ']', // 交易定义（树节点）：【交易模型引用】
                  group_type: 'group',            // 把当前的数据类型设置为组[group]
                  info: tabItem
                }
                var children = []
                for (var k in data) {
                  if (data[k].ref_id === tabItem.ref_id) {
                    children.push(data[k])
                  }
                }
                if (children.length > 0) {
                  groupObj.children = children
                }
                tableList.push(groupObj)
                groupList.push(tabItem.ref_id)
              }
            }
          }
        }
        return {tableList: tableList, expendKey: groupList}
      },
      tableGroupHandle ({ row, column, rowIndex, columnIndex }) {
        if (row.group_type === 'group') { // 行的属性[group_type]值为组[group]的
          switch (columnIndex) {          // 列索引
            case 0 :
              return [1, 1]
            case 1 :        // 当前行合并全部列到第一列
              return [1, 7] // 以当前的单元格位置开始【合并行数，合并的列数】；
            default :       // 其他列
              return [0, 0] // 不显示
          }
        }
      },
      toggleTableHandle (row, e) {
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
        if (cellItem === null) {
          return
        }
        let key = row.group_id // 分类[组]节点名称
        let expendTableKey = self.expendTableKey     // 展开的分类[组]名称
        let toggleIcon = self.toggleIcon     // 展开的分类[组]名称
        let iconItem = cellItem.getElementsByTagName('i') // 获取I标签
        if (iconItem[0].getAttribute('class') === toggleIcon[0]) { // 是展开的分类[组]
          if (expendTableKey.indexOf(key) > -1) {
            expendTableKey.splice(expendTableKey.indexOf(key), 1)
          }
        } else if (iconItem[0].getAttribute('class') === toggleIcon[1]) { // 需要展开的行
          expendTableKey.push(key)
        }
        self.expendTableKey = expendTableKey
      },
      tableOpenFunc () {
        let self = this
        if (self.$refs['tableForm']) { // 如果Form存在，则初始化；第一次打开为undefined
          self.$refs['tableForm'].resetFields()
        }
        self.tableForm.tab_desc = self.txnName
        self.tableForm.tab_name = self.txnId
        ajax.post({
          url: '/tranmdl/queryCanRefFd',
          param: {
            tab_name: this.txnId
          },
          success: function (data) {
            self.canRefFd = data.canreffd
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      refTableNameChange (value) {
        let self = this
        let canRefTab = self.canRefTab
        let refDesc = ''
        for (let i in canRefTab) {
          let item = canRefTab[i]
          if (item.tab_name === value) {
            refDesc = item.tab_desc
            break
          }
        }
        self.tableForm.ref_desc = refDesc
      },
      tableAddFunc () {
        this.activeName = '2'
        this.tableDialogVisible = true
        this.tableFormReadOnly = false
        this.tableTitle = '新建引用表'
        this.tableForm = {
          ref_id: '',
          tab_name: '',
          txn_order: '',
          ref_tab_name: '',
          ref_desc: '',
          src_expr: '',
          tab_desc: ''
        }
      },
      tableEditFunc (row) {
        if (row.group_type && row.group_type === 'group') {
          this.tableDialogVisible = true
          this.tableFormReadOnly = false
          this.tableTitle = '编辑引用表'
          this.tableForm = {
            ref_id: row.info.ref_id,
            tab_name: row.info.tab_name,
            txn_order: row.info.txn_order,
            ref_tab_name: row.info.ref_tab_name,
            ref_desc: row.info.ref_desc,
            src_expr: row.info.src_expr,
            tab_desc: row.info.tab_desc
          }
        } else {
          this.tableInfoTitle = '编辑行字段'
          this.tableInfoDialogVisible = true
          this.tableInfoFormReadOnly = false
          let info = row
          this.tableInfoForm = {
            op: 'mod',
            ref_id: info.ref_id,
            tab_name: info.tab_name,
            ref_tab_name: info.ref_tab_name,
            ref_tab_desc: info.ref_tab_desc,
            ref_fd_name: info.ref_fd_name,
            ref_desc: info.ref_desc,
            ref_name: info.ref_name,
            src_cond: info.src_cond,
            src_cond_in: info.src_cond_in,
            src_expr: info.src_expr,
            src_expr_in: info.src_expr_in,
            storecolumn: info.storecolumn,
            old_storecolumn: info.storecolumn,
            tab_desc: info.tab_desc
          }
          this.selectTableInfoRows = [info]
        }
      },
      tableInfoFunc (row) {
        if (row.group_type && row.group_type === 'group') {
          this.tableDialogVisible = true
          this.tableFormReadOnly = true
          this.tableTitle = '查看引用表'
          this.tableForm = {
            ref_id: row.info.ref_id,
            tab_name: row.info.tab_name,
            txn_order: row.info.txn_order,
            ref_tab_name: row.info.ref_tab_name,
            ref_desc: row.info.ref_desc,
            src_expr: row.info.src_expr,
            tab_desc: row.info.tab_desc
          }
        } else {
          this.tableInfoTitle = '查看行字段'
          this.tableInfoDialogVisible = true
          this.tableInfoFormReadOnly = true
          let info = row
          this.tableInfoForm = {
            ref_id: info.ref_id,
            tab_name: info.tab_name,
            ref_tab_name: info.ref_tab_name,
            ref_tab_desc: info.ref_tab_desc,
            ref_fd_name: info.ref_fd_name,
            ref_desc: info.ref_desc,
            ref_name: info.ref_name,
            src_cond: info.src_cond,
            src_cond_in: info.src_cond_in,
            src_expr: info.src_expr,
            src_expr_in: info.src_expr_in,
            storecolumn: info.storecolumn,
            tab_desc: info.tab_desc
          }
        }
      },
      submitTableForm () {
        let self = this
        this.$refs['tableForm'].validate((valid) => {
          if (valid) {
            let op = ''
            let refId = self.tableForm.ref_id
            if (util.trim(refId) === '') {
              op = 'add'
            } else {
              op = 'mod'
            }
            let jsonData = {}
            let row = self.tableForm
            jsonData[op] = [row]
            ajax.post({
              url: '/tranmdl/saveModelRefTab',
              loading: true,
              param: {
                postData: jsonData
              },
              success: function (data) {
                if (op === 'add') {
                  self.$message.success('创建成功')
                } else {
                  self.$message.success('编辑成功')
                }
                self.tableDialogVisible = false
                self.initTableForm()
                self.getTmFdNameList()
              },
              fail: function (e) {
                if (e.response.data.message) {
                  self.$message.error(e.response.data.message)
                }
              }
            })
          } else {
            return false
          }
        })
      },
      tableDelFunc (row) {
        let self = this
        self.$confirm('确定删除', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let url = ''
          let param = {}
          if (row.group_type && row.group_type === 'group') {
            url = '/tranmdl/saveModelRefTab'
            param = {
              postData: {
                del: [row.info]
              }
            }
          } else {
            url = '/tranmdl/saveModelRefFd'
            param = {
              postData: {
                del: [row]
              }
            }
          }
          ajax.post({
            url: url,
            loading: true,
            param: param,
            success: function (data) {
              self.$message.success('删除成功')
              self.initTableForm()
              self.getTmFdNameList()
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }).catch(() => {
          return false
        })
      },
      canRefTabFdChange (value) {
        this.tableInfoForm.ref_name = value
        let list = this.canRefTabFd
        let refDesc = ''
        for (let i in list) {
          if (list[i].fd_name === value) {
            refDesc = list[i].name
          }
        }
        this.tableInfoForm.ref_desc = refDesc
      },
      statCondInValueCallBack (value) {
        this.tableInfoForm.src_cond = value.stat_cond_value
        this.tableInfoForm.src_cond_in = value.stat_cond_in
      },
      SrcExprInValueCallBack (value) {
        this.tableInfoForm.src_expr = value.stat_cond_value
        this.tableInfoForm.src_expr_in = value.stat_cond_in
      },
      tableSetFunc (row) {
        this.tableInfoTitle = '新建行字段'
        this.tableInfoDialogVisible = true
        this.tableInfoFormReadOnly = false
        let info = row.info
        this.tableInfoForm = {
          op: 'add',
          ref_id: info.ref_id,
          tab_name: info.tab_name,
          ref_tab_name: info.ref_tab_name,
          ref_tab_desc: info.ref_desc,
          ref_fd_name: '',
          ref_desc: '',
          ref_name: '',
          src_cond: '',
          src_expr: '',
          src_cond_in: '',
          src_expr_in: '',
          storecolumn: '',
          tab_desc: info.tab_desc
        }
      },
      submitTableInfoForm () {
        let self = this
        this.$refs['tableInfoForm'].validate((valid) => {
          if (valid) {
            let op = this.tableInfoForm.op
            let jsonData = {}
            let row = self.tableInfoForm
            let rowData = Object.assign({}, self.selectTableInfoRows[0])
            row = Object.assign(row, {old: rowData})
            jsonData[op] = [row]
            ajax.post({
              url: '/tranmdl/saveModelRefFd',
              loading: true,
              param: {
                postData: jsonData
              },
              success: function (data) {
                if (op === 'add') {
                  self.$message.success('创建成功。')
                } else {
                  self.$message.success('编辑成功。')
                }
                self.initTableForm()
                self.getTmFdNameList()
                self.tableInfoDialogVisible = false
              },
              fail: function (e) {
                if (e.response.data.message) {
                  self.$message.error(e.response.data.message)
                }
              }
            })
          } else {
            return false
          }
        })
      },
      getFdNameList (dataType, enableStoreFd, fdName) {
        let self = this
        let dataTypeClassify = self.dataTypeClassify
        let hasFdName = false
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
                      hasFdName = hasFdName || fd.fd_name === fdName
                    }
                  }
                  if (fd['type'] === 'double' || fd['type'] === 'long') {
                    sfdItems.push(fd)
                    hasFdName = hasFdName || fd.fd_name === fdName
                  }
                } else if (dt.recap === 'string') {
                  if (dataType !== 'string') {
                    if (fd['type'] === dataType) {
                      sfdItems.push(fd)
                      hasFdName = hasFdName || fd.fd_name === fdName
                    }
                  }
                }
                if (fd['type'] === 'string') {
                  sfdItems.push(fd)
                  hasFdName = hasFdName || fd.fd_name === fdName
                }
              }
            }
          }
        }
        // 编辑的时候，如果下拉列表中不存在编辑值，那么插入这条记录
        if (!hasFdName && fdName !== '') {
          let allStoreFd = self.allStoreFd
          for (let i in allStoreFd) {
            if (allStoreFd[i].fd_name === fdName) {
              sfdItems.splice(0, 0, allStoreFd[i])
            }
          }
        }
        return sfdItems
      },
      showStatCondDialog () {
        if (this.tableInfoFormReadOnly) {
          return
        }
        this.$refs['StatCondDialog'].open()
        this.$refs['StatCondDialog'].setValue({
          stat_cond_value: this.tableInfoForm.src_cond,
          stat_cond_in: this.tableInfoForm.src_cond_in
        })
      },
      showSrcExprDialog () {
        if (this.tableInfoFormReadOnly) {
          return
        }
        this.$refs['SrcExprDialog'].open()
        this.$refs['SrcExprDialog'].setValue({
          stat_cond_value: this.tableInfoForm.src_expr,
          stat_cond_in: this.tableInfoForm.src_expr_in
        })
      },
      formatterResultList (list) {
        let resultList = []
        for (let i in list) {
          let item = list[i]
          let result = {}
          for (let key in item) {
            result[key] = util.toggleObjKey(item[key])
          }
          resultList.push(result)
        }
        return resultList
      }
    },
    components: {
      StatCondPicker,
      FuncParamPicker
    }
  }
</script>

<style>
  .hidden {
    display: none;
  }
  tbody .groupStyle {
    background-color: #f5f7fa !important;
  }
  .table-header {
    text-align: left;
    line-height: 2em;
    margin-bottom: 0px;
  }
  .collapse-table-style{
    width: 100%;margin-bottom: -35px;
  }
</style>
