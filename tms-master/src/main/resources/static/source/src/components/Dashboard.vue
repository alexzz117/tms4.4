<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6"><div class="grid-content bg-primary">
        <div class="mini-stat clearfix bg-primary">
          <span class="mini-stat-icon"><i class="mdi mdi-cart-outline"></i></span>
          <div class="mini-stat-info text-right text-white">
            <span class="counter">15852</span>交易总数
          </div>
        </div>

        <!--交易总数，欺诈总数、非欺诈数、预警总数-->
      </div></el-col>
      <el-col :span="6"><div class="grid-content bg-primary"></div></el-col>
      <el-col :span="6"><div class="grid-content bg-primary"></div></el-col>
      <el-col :span="6"><div class="grid-content bg-primary"></div></el-col>
    </el-row>
    <div class="content">
      <div id="main"></div>
    </div>
  </div>
</template>
<script>
  // 皮肤引入 import theme-name from theme-folder

  // 以饼图为例
  // 其他种类图表配置项参见百度echarts官网
  export default {
    data() {
      return {
        // 初始化空对象
        chart: null,
        // 初始化图表配置
        opinion: ['高富帅', '矮富帅', '高富挫', '矮富挫', '女生'],
        opinionData: [{
          value: 26,
          name: '高富帅'
        }, {
          value: 31,
          name: '矮富帅'
        }, {
          value: 18,
          name: '高富挫'
        }, {
          value: 28,
          name: '矮富挫'
        }, {
          value: 21,
          name: '女生'
        }]
      }
    },
    methods: {
      // 绘图
      drawGraph(id) {
        // 绘图方法
        this.chart = this.$echarts.init(document.getElementById(id))
        // 皮肤添加同一般使用方式
        this.chart.showLoading()
        // 返回到data中
        var that = this

        // set
        this.chart.setOption({
          title: {
            text: '女生喜欢的男生种类',
            subtext: '纯属扯犊子',
            x: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
          },
          legend: {
            x: 'center',
            y: 'bottom',
            data: this.opinion // 别忘了this
          },
          toolbox: {
            show: true,
            feature: {
              mark: {
                show: true
              },
              dataView: {
                show: true,
                readOnly: false
              },
              magicType: {
                show: true,
                type: ['pie']
              },
              restore: {
                show: true
              },
              saveAsImage: {
                show: true
              }
            }
          },
          calculable: true,
          series: [{
            name: '种类',
            type: 'pie',
            // 内圆半径，外圆半径
            radius: [30, 100],
            // 位置，左右，上下
            center: ['50%', '50%'],
            roseType: 'area',
            data: this.opinionData, // 别忘了this
          }]
        })
        this.chart.hideLoading()
      }
    },
    // keypoint：执行方法
    // “将回调延迟到下次 DOM 更新循环之后执行。在修改数据之后立即使用它，然后等待 DOM 更新。”
    mounted() {
      this.$nextTick(function() {
        this.drawGraph('main')
      })
    }
  }
</script>

<style scoped>
  .el-row {
    margin-bottom: 20px;
  &:last-child {
     margin-bottom: 0;
   }
  }
  .el-col {
    border-radius: 4px;
  }

  .bg-primary {
    background-color: #67a8e4 !important;
  }

  .grid-content {
    /*min-height: 36px;*/
    border: 1px solid rgba(112, 112, 112, 0.12);
    padding: 20px;
    -webkit-border-radius: 3px;
    -moz-border-radius: 3px;
    border-radius: 3px;
    margin-bottom: 20px;
  }

  /* ==============
  Widgets
===================*/
  .widget-chart li {
    width: 31.5%;
    display: inline-block;
    padding: 0;

  i {
    font-size: 22px;
  }
  }

  .mini-stat {
    border: 1px solid rgba($muted,0.12);
    padding: 20px;
    -webkit-border-radius: 3px;
    -moz-border-radius: 3px;
    border-radius: 3px;
    margin-bottom: 20px;
  }

  .mini-stat-icon {
    width: 60px;
    height: 60px;
    display: inline-block;
    line-height: 60px;
    text-align: center;
    font-size: 30px;
    -webkit-border-radius: 100%;
    -moz-border-radius: 100%;
    border-radius: 100%;
    float: left;
    margin-right: 10px;
    color: $white;
    background-color: rgba($white,0.1);
  }

  .mini-stat-info {
    font-size: 14px;
    padding-top: 2px;
  }

  .mini-stat-info span {
    display: block;
    font-size: 24px;
    font-weight: 600;
  }

  .text-white {
    color: #ffffff !important;
  }

  .mdi {
    display: inline-block;
    font: normal normal normal 24px/1 "Material Design Icons";
    font-size: inherit;
    text-rendering: auto;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    transform: translate(0, 0);
  }

  .mdi-cart-outline:before {
    content: "\F111";
  }


  .content {
    /*自行添加样式即可*/
  }


  #main {
    /*需要制定具体高度，以px为单位*/
    height: 400px;
  }
</style>
