<template>
  <el-container style="height: 100%; border: 1px solid #eee">
    <el-header height="38px" style="background-color: white;">
      <el-breadcrumb separator-class="el-icon-arrow-right" style="padding: 13px 0px;">
        <el-breadcrumb-item v-for="item in breadcrumbData" :key = "item.text">{{ item.text }}</el-breadcrumb-item>
      </el-breadcrumb>
    </el-header>
    <el-container style="height: 100%; border-top: 1px solid #eee" class="wrapper">
      <el-aside width="200px" style="min-height: 400px;border:0;border-right: 1px solid #eee;background-color: white">
        <div style="height:38px;margin-top: 5px;text-align: left;border-bottom: 1px solid #eee;" v-show="readonly">
          <el-button plain class="el-icon-plus" @click="addFunc" :disabled="toolBtn.addBtn" style="margin-left: 5px;">新建
          </el-button>
          <el-button plain class="el-icon-delete" @click="delFunc" :disabled="toolBtn.delBtn" style="margin-left: 0px;">删除
          </el-button>
        </div>
        <el-tree :data="treeData" node-key="id" ref="tree"
                 :props="defaultProps"
                 :highlight-current=true
                 :default-expanded-keys="expendKey"
                 :expand-on-click-node="false"
                 @node-click="handleNodeClick"
                 :render-content="renderContent"
                 style="overflow-y: auto;">
        </el-tree>
      </el-aside>
      <el-main style="padding: 5px; background-color: white">
        <!--&lt;!&ndash;用router-view渲染视图&ndash;&gt;-->
        <!--<router-view/>-->
        <el-tabs v-model="activeName" @tab-click="handleClick" ref="tab">
          <el-tab-pane label="交易定义" name="trandef" style="padding-left: 10px;"><trandef :txnId='txnId' :isVisibility="tabVisibility.trandefVisibility" ref="trandef" :readonly="readonly"></trandef></el-tab-pane>
          <el-tab-pane label="交易模型定义" name="tranmdl"><tranmdl :txnId='txnId' :isVisibility="tabVisibility.tranmdlVisibility" :txnName="txnName" :readonly="readonly"></tranmdl></el-tab-pane>
          <el-tab-pane label="交易统计" name="stat"><stat :txnId='txnId' :isVisibility="tabVisibility.statVisibility" :readonly="readonly"></stat></el-tab-pane>
          <el-tab-pane label="交易规则" name="rule"><rule :txnId='txnId' :isVisibility="tabVisibility.ruleVisibility" :readonly="readonly"></rule></el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  import Trandef from '@/components/run/Trandef'
  import Tranmdl from '@/components/run/Tranmdl'
  import Stat from '@/components/run/Stat'
  import Rule from '@/components/run/Rule'
  import ajax from '@/common/ajax'
  import util from '@/common/util'

  export default {
    created () {
      this.selTree()
    },
    data () {
      return {
        toolBtn: { // 功能树操作按钮
          addBtn: true, // 添加按钮
          delBtn: true // 删除按钮
        },
        txnId: '',
        txnName: '',
        tabVisibility: {
          trandefVisibility: true,
          tranmdlVisibility: false,
          statVisibility: false,
          ruleVisibility: false
        },
        treeList: [],
        treeData: [],
        expendKey: ['T'], // 默认展开的功能节点id
        defaultProps: {
          children: 'children',
          label: 'text'
        },
        breadcrumbData: [''], // 地址路径数组
        activeName: 'trandef'
      }
    },
    props: ['readonly'],
    methods: {
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
        ajax.post({
          url: '/trandef/query',
          success: function (data) {
            if (data.list) {
              self.treeList = (data.list)
              let treeJson = util.formatTreeData(data.list)
              self.treeData = treeJson
              // 模拟选中第一个节点（根节点）
              self.txnId = treeJson[0].id
              self.txnName = treeJson[0].text
              self.breadcrumbData = [{text: '交易模型：' + treeJson[0].text}]
              self.toolBtn.addBtn = false
              // self.expendNodesByLevel(1)
            }
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      // 展开前几层功能树
      expendNodesByLevel (deep) {
        var expendNodeIds = []
        var treeNode = this.treeData[0]
        var level = 1
        while (level <= deep) {
          expendNodeIds.push(treeNode.id)
          treeNode = treeNode.children[0]
          level++
        }
        this.expendKey = expendNodeIds
      },
      handleNodeClick (data, node) {
        var self = this
        if (self.txnId === data.id) {
          return
        }
        self.showToolBtn()
        self.txnId = data.id
        self.txnName = data.text
        self.activeName = 'trandef' // tab跳转到首位,交易定义
        self.tabVisibility = { // 同时更新显隐状态
          trandefVisibility: true,
          tranmdlVisibility: false,
          statVisibility: false,
          ruleVisibility: false
        }
//        if (!data.children || data.children.length === 0){
//          this.$router.push(data.name);
//        }
        // 同步面包屑导航地址
        self.syncBreadcrumb(node)
      },
      showToolBtn (tabName) {
        var self = this
        if (!tabName) {
          tabName = self.$refs.tab.value
        }
        if (tabName === 'trandef') {
          var data = self.$refs.tree.getCurrentNode()
          if (data) {
            self.toolBtn.addBtn = !(data.txnid === undefined || data.txnid === null || data.txnid.trim() === '')
            self.toolBtn.delBtn = data.id === 'T'  // 当前交易为root时,不允许删除
          }
        } else {
          self.toolBtn.addBtn = true
          self.toolBtn.delBtn = true
        }
      },
      // 同步地址栏显示
      syncBreadcrumb (node) {
        var self = this
        var nodePath = []
        while (node.parent !== null) {
          nodePath.unshift({text: node.data.text})
          node = node.parent
        }
        nodePath[0].text = '交易模型：' + nodePath[0].text
        self.breadcrumbData = nodePath
      },
      handleClick (tab, event) {
        this.tabVisibility.trandefVisibility = false
        this.tabVisibility.tranmdlVisibility = false
        this.tabVisibility.statVisibility = false
        this.tabVisibility.ruleVisibility = false
        switch (tab.name) {
          case 'trandef': {
            this.tabVisibility.trandefVisibility = true
            break
          }
          case 'tranmdl': {
            this.tabVisibility.tranmdlVisibility = true
            break
          }
          case 'stat': {
            this.tabVisibility.statVisibility = true
            break
          }
          case 'rule': {
            this.tabVisibility.ruleVisibility = true
            break
          }
          default: {
          }
        }
        this.showToolBtn(tab.name)
      },
      addFunc () {
        this.$refs.trandef.addFormVisible = true
      },
      delFunc () {
        let data = this.$refs.tree.getCurrentNode()
        let param = {
          tab_name: data.id,
          op: 'del',
          tab_desc: data.tab_desc
        }
        this.$refs['trandef'].delInfo(param)
      }
    },
    components: {
      Trandef,
      Tranmdl,
      Stat,
      Rule
    }
  }
</script>

<style>
  .disabledFlag {
    color: #dcdfe6;
  }
</style>
