<template>
  <el-container style="height: 100%; border: 1px solid #eee" class="wrapper">
    <el-aside width="200px">
      <el-tree :data="data" :props="defaultProps" @node-click="handleNodeClick"></el-tree>
    </el-aside>
    <el-main>
      <!--&lt;!&ndash;用router-view渲染视图&ndash;&gt;-->
      <!--<router-view/>-->
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="交易定义" name="first"><trandef></trandef></el-tab-pane>
        <el-tab-pane label="交易模型定义" name="second"><tranmdl></tranmdl></el-tab-pane>
        <el-tab-pane label="交易统计" name="third"><stat></stat></el-tab-pane>
        <el-tab-pane label="交易规则" name="fourth"><rule></rule></el-tab-pane>
      </el-tabs>
    </el-main>
  </el-container>
</template>

<script>
  import Trandef from '@/components/run/Trandef'
  import Tranmdl from '@/components/run/Tranmdl'
  import Stat from '@/components/run/Stat'
  import Rule from '@/components/run/Rule'

  export default {
    data() {
      return {
        data: [{
          label: '一级 1',
          children: [{
            label: '二级 1-1',
            children: [{
              label: '三级 1-1-1',
              name: '/txn/rule'
            }]
          }]
        }, {
          label: '一级 2',
          children: [{
            label: '二级 2-1',
            children: [{
              label: '三级 2-1-1',
              name: '/txn/stat'
            }]
          }, {
            label: '二级 2-2',
            children: [{
              label: '三级 2-2-1'
            }]
          }]
        }, {
          label: '一级 3',
          children: [{
            label: '二级 3-1',
            children: [{
              label: '三级 3-1-1'
            }]
          }, {
            label: '二级 3-2',
            children: [{
              label: '三级 3-2-1'
            }]
          }]
        }],
        defaultProps: {
          children: 'children',
          label: 'label'
        },
        activeName: 'second'
      };
    },
    methods: {
      handleNodeClick(data) {
        console.log(data);
//        if (!data.children || data.children.length === 0){
//          this.$router.push(data.name);
//        }
      },
      handleClick(tab, event) {
        console.log(tab, event);
      }
    },
    components: {
      Trandef,
      Tranmdl,
      Stat,
      Rule
    }
  };
</script>

<style>

</style>
