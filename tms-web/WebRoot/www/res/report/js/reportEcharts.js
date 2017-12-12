if(!Array.indexOf){
	Array.prototype.indexOf = function(obj){
		for(var i=0; i<this.length; i++){
			if(this[i]==obj){
				return i;
			}
		}
		return -1;
	};
}
function GenOption(chartOption){
	var op_legend_data = chartOption.op_legend_data;
	var op_x_data = chartOption.op_x_data;
	var op_series = chartOption.op_series;
	var chartID   = chartOption.chartID || 'chartdiv';
	var magicType   = chartOption.magicType || ['line', 'bar', 'stack', 'tiled'];

	var option = {
			 title:chartOption.title||{},
			 tooltip : {
			        trigger: 'axis',
			        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
			            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
			        }
			    },
			    legend: {
			    	x:'left',//图例靠左显示
			        data:op_legend_data
			    },
			    toolbox: {
			        show : true,
			        orient: 'vertical',
			        x: 'right',
			        y: 'center',
			        feature : {
			            // mark : {show: true},
			            dataView : {show: true, readOnly: true},
			            magicType : {show: true, type: magicType},
			            restore : {show: true},
			            saveAsImage : {show: true}
			        }
			    },
			    calculable : true,
			    xAxis : chartOption.xAxis||[
			        {
			            type : 'category',
			            axisLabel:{
	                    	interval:0 //全部显示
	                    	// textStyle:{
	                     //   	 color: '#fff'    
	                    	// }
	                	},
			            data : op_x_data
			        }
			    ],
			    yAxis : [
			        {
			            type : 'value'
			        }
			    ],
			    series : op_series
		 };
		 if (op_x_data.length > 8) {
		 	if (!option['dataZoom']) 
		 		option['dataZoom'] = {};
		 	option.dataZoom['orient'] = 'horizontal';
		 	option.dataZoom['show'] = true;
		 	option.dataZoom['end'] = (8/op_x_data.length)*100;
		 	if (!option['grid']) 
		 		option['grid'] = {};
			option.grid['x'] = 60;
			option.grid['y'] = 30;
			option.grid['y2'] = 70;
		 }
		 // 根据报表上层图例标题数量计算宽度
		 var step = $('#'+chartID).width()/110;
		 var op_legend_data_line = (op_legend_data.length+step)/step;
		 if (!option['grid']) 
		 {
		 	option['grid'] = {};
		 }
		 option.grid['y'] = op_legend_data_line*25;
		 return option;
}