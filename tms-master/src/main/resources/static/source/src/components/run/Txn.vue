<template>
  <el-container style="height: 100%; border: 1px solid #eee">
    <el-header height="38px" style="background-color: white;">
      <el-breadcrumb separator-class="el-icon-arrow-right" style="padding: 13px 0px;">
        <el-breadcrumb-item v-for="item in breadcrumbData" :key = "item.text">{{ item.text }}</el-breadcrumb-item>
      </el-breadcrumb>
    </el-header>
    <el-container style="height: 100%; border-top: 1px solid #eee" class="wrapper">
      <el-aside width="200px" style="min-height: 400px;border:0;border-right: 1px solid #eee">
        <div style="height:38px;margin-top: 5px;text-align: left;border-bottom: 1px solid #eee;">
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
      <el-main style="padding: 5px;">
        <!--&lt;!&ndash;用router-view渲染视图&ndash;&gt;-->
        <!--<router-view/>-->
        <el-tabs v-model="activeName" @tab-click="handleClick" ref="tab">
          <el-tab-pane label="交易定义" name="trandef" style="padding-left: 10px;"><trandef :txnId='txnId' :isVisibility="tabVisibility.trandefVisibility" ref="trandef"></trandef></el-tab-pane>
          <el-tab-pane label="交易模型定义" name="tranmdl"><tranmdl :txnId='txnId' :isVisibility="tabVisibility.tranmdlVisibility"></tranmdl></el-tab-pane>
          <el-tab-pane label="交易统计" name="stat"><stat :txnId='txnId' :isVisibility="tabVisibility.statVisibility"></stat></el-tab-pane>
          <el-tab-pane label="交易规则" name="rule"><rule :txnId='txnId' :isVisibility="tabVisibility.ruleVisibility"></rule></el-tab-pane>
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
        txnId: 'T',
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
        breadcrumbData: [], // 地址路径数组
        activeName: 'trandef'
      }
    },
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
        var option = {
          url: '/trandef/query',
          success: function (data) {
            if (data.list) {
              self.treeList = (data.list)
              self.treeData = self.formatTreeData(data.list)
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
        // console.log(tab, event);
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
