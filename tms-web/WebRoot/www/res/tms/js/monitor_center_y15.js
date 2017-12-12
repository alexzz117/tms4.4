// ------------------------------------------------------------------------
var map_option_series_iStyle_n = {
                    label:{show:true,
                        textStyle:{
                            color:'#999999'//页面字体颜色
                        }
                    },
                    borderColor:'rgba(100,149,237,1)',
                    // borderColor:'#000000',
                    borderWidth:0.5,
                    areaStyle:{
                        color: '#212122'   //设置地图背景颜色
                    }
                };
var map_backgroundColor = '#252525';
var other_backgroundColor = '#252525';
// 获取处置数据
function getMapDPSeries(name,symbolSize,data,geocoord,mt){
    return {
            name: name,
            type: 'map',
            mapType: mt,
            selectedMode : 'single',
            roam: true,
            itemStyle:{
                normal:map_option_series_iStyle_n,
                emphasis:{label:{show:true}}
            },
            data:[],
            markPoint:{
                symbolSize: symbolSize,       // 标注大小，半宽（半径）参数，当图形为方向或菱形则总宽度为symbolSize * 2
                itemStyle: {
                    normal: {
                        borderColor: '#87cefa',
                        borderWidth: 1,            // 标注边线线宽，单位px，默认为1
                        label: {
                            show: false
                        }
                    },
                    emphasis: {
                        borderColor: '#1e90ff',
                        borderWidth: 5,
                        label: {
                            show: false
                        }
                    }
                },
                data:data
            },
            geoCoord:geocoord
        };
}
// 获取圆形闪动效果
function getMapMPSeries(name,data,mt){
         return  {
            name: name,
            type: 'map',
            mapType: mt,
            data:[],
            markPoint : {
                symbol:'emptyCircle',
                symbolSize : function (v){
                    return 10 + 100/100;
                },
                effect : {
                    show: true,
                    shadowBlur : 0
                },
                itemStyle:{
                    normal:{
                        label:{show:false}
                    }
                },
                data : data
            }
        };
}
// 获取地图的基础数据
function getMapBaseSeries(mt){
    if (!mt) {
        mt = 'china';
    };
    return [
            {
                name: '中国',
                type: 'map',
                mapType: mt,
                selectedMode : 'single',
                roam: true,
                itemStyle:{
                    normal:map_option_series_iStyle_n,
                    emphasis:{label:{show:true}}
                },
                data:[],
                markPoint:{
                    symbolSize: 2,       // 标注大小，半宽（半径）参数，当图形为方向或菱形则总宽度为symbolSize * 2
                    itemStyle: {
                        normal: {
                            borderColor: '#87cefa',
                            borderWidth: 1,            // 标注边线线宽，单位px，默认为1
                            label: {
                                show: false
                            }
                        },
                        emphasis: {
                            borderColor: '#1e90ff',
                            borderWidth: 5,
                            label: {
                                show: false
                            }
                        }
                    },
                    data:[]
                }
            },
            {
                name: '欧洲',
                type: 'map',
                roam:true,
                mapType: 'continent|欧洲', // 自定义扩展图表类型
                mapLocation: {
                    x: '10%',
                    y: 'top',
                    height:'20%',
                    width: '20%'
                },
                itemStyle:{
                    normal:map_option_series_iStyle_n,
                    emphasis:{label:{show:true}}
                },
                data:[],
                // 文本位置修正
                textFixed : {
                    '欧洲' : [200, -10]
                }
            },
            {
                name: '非洲',
                type: 'map',
                roam:true,
                mapType: 'continent|非洲', // 自定义扩展图表类型
                mapLocation: {
                    x: '10%',
                    y: 'center',
                    height:'30%',
                    width: '30%'
                },
                itemStyle:{
                    normal:map_option_series_iStyle_n,
                    emphasis:{label:{show:true}}
                },
                data:[],
                // 文本位置修正
                textFixed : {
                      '非洲' : [10, -10]
                }
            },
            {
                name: '亚洲',
                type: 'map',
                roam:true,
                mapType: 'continent|亚洲', // 自定义扩展图表类型
                mapLocation: {
                    x: '10%',
                    y: 'bottom',
                    height:'30%',
                    width: '30%'
                },
                itemStyle:{
                    normal:map_option_series_iStyle_n,
                    emphasis:{label:{show:true}}
                },
                data:[],
                // 文本位置修正
                textFixed : {
                     '亚洲' : [20, -30]
                }
            },
            {
                name: '北美洲',
                type: 'map',
                roam:true,
                mapType: 'continent|北美洲', // 自定义扩展图表类型
                mapLocation: {
                    x: '80%',
                    y: 'top',
                    height:'30%',
                    width: '30%'
                },
                itemStyle:{
                    normal:map_option_series_iStyle_n,
                    emphasis:{label:{show:true}}
                },
                data:[],
                // 文本位置修正
                textFixed : {
                     '北美洲' : [20, 0]
                },
                markPoint:{
                    symbolSize: 3.5,       // 标注大小，半宽（半径）参数，当图形为方向或菱形则总宽度为symbolSize * 2
                    itemStyle: {
                        normal: {
                            borderColor: '#87cefa',
                            borderWidth: 1,            // 标注边线线宽，单位px，默认为1
                            label: {
                                show: false
                            }
                        },
                        emphasis: {
                            borderColor: '#1e90ff',
                            borderWidth: 5,
                            label: {
                                show: false
                            }
                        }
                    },
                    data:[
                        // {name: "New York", value: 80}
                    ]
                },
                geoCoord: {
                    // 'New York':[-55.401,51.58]
                    // 'Boston':'42.3496000,-71.0746000'
                    // 'New York':[-73.9725000,40.7528000]
                }
            },
            {
                name: '南美洲',
                type: 'map',
                roam:true,
                mapType: 'continent|南美洲', // 自定义扩展图表类型
                mapLocation: {
                    x: '80%',
                    y: '30%',
                    height:'30%',
                    width: '30%'
                },
                itemStyle:{
                    normal:map_option_series_iStyle_n,
                    emphasis:{label:{show:true}}
                },
                data:[],
                // 文本位置修正
                textFixed : {
                    '南美洲' : [0, -10]
                }
            },
            {
                name: '大洋洲',
                type: 'map',
                roam:true,
                mapType: 'continent|大洋洲', // 自定义扩展图表类型
                mapLocation: {
                    x: '50%',
                    y: '80%',
                    height:'30%',
                    width: '30%'
                },
                itemStyle:{
                    normal:map_option_series_iStyle_n,
                    emphasis:{label:{show:true}}
                },
                data:[],
                // 文本位置修正
                textFixed : {
                     '大洋洲' : [170, 0]
                }
            }
        ];
}
// 获取地图主数据
function getMapOption(legend_data,color,series_data,mt){
    return {
        backgroundColor: map_backgroundColor,
        title : {
           text: '当前高风险分布图',
            subtext: '前1000笔高风险交易',
            x:'center',
            textStyle : {
                color: '#fff'
            }
        },
        tooltip : {
            trigger: 'item'
        },
        // color: [
        //     'red',
        //     'orange',
        //     'yellow'
        // ],
        color:color,
        legend: {
            orient: 'vertical',
            x:'left',
            // data:['阻断','身份认证','告警'],
            data:legend_data,
            textStyle : {
                // color: '#fff'
                color:'auto'
            }
        },
        // dataRange: {
        //     show:false,
        //     min: 0,
        //     max: 100,
        //     x: 'left',
        //     y: 'bottom',
        //     text:['高','低'],           // 文本，默认为数值文本
        //     calculable : true,
        //     color: ['red','orange','yellow']
        // },
        toolbox: {
            show: true,
            orient : 'vertical',
            x: 'right',
            y: 'center',
            feature : {
                // mark : {show: true},
//                dataView : {show: true, readOnly: false},
                restore : {show: true},
                saveAsImage : {show: true}
            }
        },
       /* roamController: {
            show: true,
            x: 'right',
            mapTypeControl: {
                'china': true
            }
        },*/
        series:(function(){
            if (!series_data) {
                return getMapBaseSeries(mt);
            };
            var base = getMapBaseSeries(mt);
            for (var i = 0; i < series_data.length; i++) {
                base.push(series_data[i]); 
            };
            return base;
        })()
    };
}
// 交易   交易名称,图表显示数据
function getTXNOption(legend_data,series_data){

//    var idx = 1;
    if (!legend_data || legend_data.length == 0) {
        legend_data.push('交易总数');
        series_data.push(0);
    };
    return {
        // timeline : {
        //     padding:3,
        //     data : [
        //         '2013-01-01', '2013-02-01', '2013-03-01', '2013-04-01', '2013-05-01',
        //         { name:'2013-06-01', symbol:'emptyStar6', symbolSize:8 },
        //         '2013-07-01', '2013-08-01', '2013-09-01', '2013-10-01', '2013-11-01',
        //         { name:'2013-12-01', symbol:'star6', symbolSize:8 }
        //     ],
        //     controlStyle:{
        //         normal : {
        //             color : '#fff'
        //         }
        //     },
        //     label : {
        //         formatter : function(s) {
        //             return s.slice(0, 7);
        //         },
        //         textStyle : {
        //             color: '#fff'
        //         }    
        //     }
        // },
        // options : [
            // {
                backgroundColor: other_backgroundColor,
                title : {
                    text: '交易总数前10',
                    subtext: '',
                    x:'center',
                    textStyle : {
                        color: '#fff'
                    }    
                },
                tooltip : {
                    trigger: 'item',
                    formatter: "{a} <br/>{b} : {c} ({d}%)"
                },
                legend: {
                    show:false,
                    orient : 'vertical',
                    x : 'left',
                    itemGap:2,
                    padding:2,
                    textStyle:{
                        fontSize:12,
                        color: '#fff'
                    },
                    // data:['网银登录','网银转账','手机转账','POS机转账','手机登录'],
                    data:legend_data
                },
                toolbox: {
                    show : true,
                    // orient : 'vertical',
                    // x: 'right',
                    // y: 'center',
                    feature : {
//                        mark : {show: true},
                        dataView : {show: true, readOnly: true},
                        magicType : {
                            show: true, 
                            type: ['pie', 'funnel'],
                            option: {
                                funnel: {
                                    x: '25%',
                                    width: '50%',
                                    funnelAlign: 'left',
                                    max: 1700
                                }
                            }
                        },
                        restore : {show: true},
                        saveAsImage : {show: true}
                    }
                },
                series : [
                    {
                        name:'交易总数',
                        type:'pie',
                        // center: ['50%', '45%'],
                        radius: '50%',
                        data:series_data
                    }
                ]
    };
}


// 欺诈
// 交易   交易名称,图表显示数据
function getFraudOption(legend_data,series_data){

    // 取消判断为了在没有数据的时候暂时页面
    // var markLine_data = [];
    // if (legend_data && legend_data.length > 0) {
    var  markLine_data = [
                        {type : 'average', name : '平均值'},
                        {type : 'max'},
                        {type : 'min'}
                    ];
    // };
    if (!legend_data || legend_data.length == 0) {
        legend_data.push('');
//        series_data.push(0);
    };
    return {
        backgroundColor: other_backgroundColor,
        title : {
            text: '欺诈类型前10',
            subtext: '',
            x:'center',
            textStyle : {
                color: '#fff'
            }    
        },
        tooltip : {
            show: true,
            trigger: 'item'
        },
        legend: {
            orient : 'vertical',
            x : 'left',
            itemGap:2,
            padding:2,
            data:['欺诈类型数据'],
            textStyle : {
                color: '#fff'
            }
        },
        toolbox: {
            show : true,
            // orient : 'vertical',
            // x: 'right',
            // y: 'right',
            feature : {
                // mark : {show: true},
                dataView : {show: true, readOnly: true},
                magicType : {show: true, type: ['line', 'bar']},
                restore : {show: true},
                saveAsImage : {show: true}
            }
        },
        calculable : true,
        xAxis : [
            {
                type : 'category',
                 axisLabel:{
                    interval:0,//全部显示
                    textStyle:{
                        color: '#fff'    
                    }
                },
                // data : [{
                //             value:'欺诈类型1',            //文本内容，如指定间隔名称格式器formatter，则这个值将被作为模板变量值或参数传入
                //             textStyle:{             //详见textStyle
                //                 color : '#fff'
                //             }
                //         },'欺诈类型2','欺诈类型3','欺诈类型4','欺诈类型5','欺诈类型6','欺诈类型7','欺诈类型8','欺诈类型9','欺诈类型10']
                data:legend_data
            }
        ],
        yAxis : [
            {
                type : 'value',
                axisLabel:{
                    textStyle:{
                        color: '#fff'    
                    }
                } 
            }
        ],
        grid: {x:50,y:40,y2:40, x2:50},
        series : [
            {
                name:'欺诈类型数据',
                type:'bar',
                // barGap:'50%',
                barCategoryGap:'50%',
                // barWidth: 40,                   // 系列级个性化，柱形宽度
                itemStyle: {
                    normal: {                   // 系列级个性化，横向渐变填充
                        borderRadius: 5,
                        // color : (function (){
                        //     var zrColor = require('zrender/tool/color');
                        //     return zrColor.getLinearGradient(
                        //         0, 0, 1000, 0,
                        //         [[0, 'rgba(30,144,255,0.8)'],[1, 'rgba(138,43,226,0.8)']]
                        //     )
                        // })(),
                        label : {
                            show : true,
                            textStyle : {
                                fontSize : '10',
                                fontFamily : '微软雅黑',
                                fontWeight : 'bold'
                            }
                        }
                    }
                },
                // data:[
                //     920, 832,732,732,632, 
                //     // {//将值写在柱子中间
                //     //     value: 701,
                //     //     itemStyle : { normal: {label : {position: 'inside'}}}
                //     // },
                //     534, 490, 332,230, 120
                // ],
                data:series_data,
                markLine : {
                    data : markLine_data
                }
            }
        ]
    };
}

// 规则
var mo_rule_option = {
    backgroundColor: other_backgroundColor,
    title : {
        text: '规则执行总数',
        subtext: '',
        textStyle : {
            color: '#fff'
        }
    },
    tooltip : {
        trigger: 'item',
        formatter: "{a} <br/>{b} : {c}%"
    },
    toolbox: {
        show : true,
        feature : {
            // mark : {show: true},
            dataView : {show: true, readOnly: true},
            restore : {show: true},
            saveAsImage : {show: true}
        }
    },
/*    legend: {
        data : ['展现','点击','访问','咨询','订单']
    },*/
    calculable : true,
    series : [
        {
            name:'规则执行总数',
            type:'funnel',
            x: '2%',
            y: 60,
            x2: 10,
            y2: 10,
            // width: '80%',
            // height: {totalHeight} - y - y2,
            min: 0,
            max: 100,
            // minSize: '0%',
            // maxSize: '100%',
            sort : 'descending', // 'ascending', 'descending'
            gap : 10,
            itemStyle: {
                normal: {
                    // color: 各异,
                    borderColor: '#fff',
                    borderWidth: 1,
                    label: {
                        show: true,
                        position: 'inside'
                        // textStyle: null      // 默认使用全局文本样式，详见TEXTSTYLE
                    },
                    labelLine: {
                        show: false,
                        length: 10,
                        lineStyle: {
                            // color: 各异,
                            width: 1,
                            type: 'solid'
                        }
                    }
                },
                emphasis: {
                    // color: 各异,
                    borderColor: 'red',
                    borderWidth: 5,
                    label: {
                        show: true,
                        // formatter: '{b}:{c}',
                        textStyle:{
                            fontSize:20
                        }
                    },
                    labelLine: {
                        show: true
                    }
                }
            },
            data:[
                {value:60, name:'访问'},
                {value:40, name:'咨询'},
                {value:20, name:'订单'},
                {value:80, name:'点击'},
                {value:100, name:'展现'}
            ]
        }
    ]
};