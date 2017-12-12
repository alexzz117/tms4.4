
var model = window.model ||{};

(function(){
	var grid = null;
	var user_id  ;// 用户id
	var stat_id  ;// 统计id
	var txnFeature ;// 交易属性
	var data_fd_type;// 统计目标数据类型
	var data_fd_code; // 统计目标数据类型为代码的代码集
	var statInfo ;// 统计信息
	var funcList ;// 统计函数列表
//	var ad_list;// 国家代码/城市代码/地区代码
	model.userPattern = {
		init: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			$('#userPatternGridDiv').empty();// 清空列表
			
			data_fd_type = 'long';// 默认值
			
			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				width:728,
				toolbar:[
					{id:"up-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"up-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"up-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"up-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'}
				],
				table:{
					sortType: 'local',
					columns:[
						{name:"统计函数", width: 40, dataIndex:'STAT_FN',render:function(v){
							var return_text = v;
							$.each(funcList.row, function(i,row){
								if(v != '' && v == row['CODE_KEY'])
								{
									return_text = row['CODE_VALUE'];
									return false;
								}
							});
							return return_text;
						}},
						{name:"统计目标", width: 40, dataIndex:'STAT_DATAFD',render:function(v){
							var return_text = v;
							$.each(txnFeature.row, function(i,row){
								if(v != '' && v == row['CODE_KEY'])
								{
									return_text = row['CODE_VALUE'];
									data_fd_type = row['TYPE'];
									data_fd_code = row['FD_CODE'];
									return false;
								}
							});
							return return_text;
						}},
						{name:"行为习惯值", width: 40, dataIndex:'UP_NAME',render:function(v){
							var return_text = v;
							var stat_fn_param = statInfo.FN_PARAM;
							if(stat_fn_param  != undefined && stat_fn_param != ''){
								var ls = stat_fn_param.split('|');
								$.each(ls,function(i,code){
									if(i == v){
										return_text = ls[i];
										return false;
									}
								});
							}
							
							if(data_fd_code != undefined && data_fd_code != ''){
								var ls = jcl.code.getCodes(data_fd_code);
								$.each(ls,function(i,code){
									if(i == v){
										return_text = code;
										return false;
									}
								});	
							}/*else{
								if(ad_list != undefined && ad_list != null){
									$.each(ad_list,function(i,d){
									if(d['CODE_KEY'] == v){
										return_text = d['CODE_VALUE'];
										return false;
									}
								});	
								}
							}*/
							
							return return_text;
						}},
						{name:"开始日期", width: 30, dataIndex:'START_DATE'},
						{name:"结束日期", width: 30, dataIndex:'END_DATE'}
					],
					rowForm:{
		                items:[
	                       {label:"统计目标", name: 'STAT_DATAFD', type:'selector', items:txnFeature},
	                       {label:"统计函数", name: 'STAT_FN', type:'selector', items:funcList},
	                       {label:"行为习惯值", name: 'UP_NAME1', type:'text', required:true},
						   {label:"行为习惯值", name: 'UP_NAME2', type:'selector', required:true},
	                       {label:"行为习惯值", name: 'UP_NAME3', type:'jcq.ui.formItem.CustomQueryItem', required:true,editSeparator:' ',viewSeparator:'-',
						   items:[{name: 'countrycode', type:'selector', items:[{text:'--请选择--',value:''}], ds:{type:'remote',url:'/tms35/userpattern/getCountry',
						   parser:{text:'CODE_VALUE', value:'CODE_KEY'}}}, 
				       		{name: 'regioncode', type:'selector', items:[{text:'--请选择--',value:''}]}, 
							{name: 'citycode', type:'selector', items:[{text:'--请选择--',value:''}]}]},
	                       {label:"开始日期", name: 'START_DATE', type:'date', required:true},
	                       {label:"结束日期", name: 'END_DATE', type:'date'}
	                   ],
	                   btns:[
	                       {text:'确定', id:'up-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'up-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#userPatternGridDiv'));

			grid.table.onRowFormShow(function(isAdd){
				
				var stat_fn = statInfo.STAT_FN;
				var stat_fn_param = statInfo.FN_PARAM;
				var stat_datafd = statInfo.STAT_DATAFD;
				
				var up_name = '';
				if(!isAdd){
					up_name = grid.table.selectedOneRow().UP_VALUE;	
				}
				
				if (stat_datafd == null || stat_datafd == "") {
					grid.table.rowForm.disable('STAT_DATAFD');// 隐藏统计对象标签
					grid.table.rowForm.getItem('STAT_DATAFD').val('');// 为统计对象赋值	
				}else{
					grid.table.rowForm.enable('STAT_DATAFD');// 显示统计对象标签
					grid.table.rowForm.getItem('STAT_DATAFD').val(stat_datafd);// 为统计对象赋值	
				}
				
				grid.table.rowForm.getItem('STAT_FN').val(stat_fn);// 为统计对象赋值	
				
				grid.table.rowForm.disable('UP_NAME2');// 下拉框隐藏	
				grid.table.rowForm.disable('UP_NAME3');// 下拉框隐藏	
				grid.table.rowForm.enable('UP_NAME1'); // 输入框显示
				
				grid.table.rowForm.getItem('UP_NAME1').val(up_name);// 为自定义行为习惯赋值
				grid.table.rowForm.getItem('UP_NAME2').val('');// 为自定义行为习惯赋值
				
				grid.table.rowForm.getItem('UP_NAME1').component.removeAttr('readonly');
				grid.table.rowForm.getItem('UP_NAME1').component.unbind('focus');
				
				// 获取引用对象对应的数据类型、代码集
				$.each(txnFeature.row, function(i,row){						
					if(stat_datafd != '' && stat_datafd == row['CODE_KEY'])
					{
						data_fd_type = row['TYPE'];
						data_fd_code = row['FD_CODE'];
						return false;
					}
				});
					
				if(stat_fn.toLowerCase () == "snapshot" || stat_fn.toLowerCase () == "bin_dist" || stat_fn.toLowerCase () == "rang_bin_dist"){
					
					
					// 根据不同代码集为下拉框加载数据
					if(data_fd_code != undefined && data_fd_code != ''){
						var ls = jcl.code.getCodes(data_fd_code);
						var status_list = new Array();
						status_list.push({text:'--请选择--',value:'', checked:true});
						$.each(ls,function(i,code){
							status_list.push({text:code, value:i});
						});
						
						grid.table.rowForm.disable('UP_NAME1');// 输入框隐藏	
						grid.table.rowForm.disable('UP_NAME3');// 输入框隐藏	
						grid.table.rowForm.enable('UP_NAME2'); // 下拉框显示
						
						grid.table.rowForm.getItem('UP_NAME2').component.reload(status_list);
						
						grid.table.rowForm.getItem('UP_NAME2').val(up_name);// 为自定义行为习惯赋值
						grid.table.rowForm.getItem('UP_NAME1').val('');// 为自定义行为习惯赋值
						grid.table.rowForm.getItem('UP_NAME3').getComponent('citycode').select(0);// 为自定义行为习惯赋值
					}
					
					// 函数参数的加载下拉数据
					else if(stat_fn_param != null && stat_fn_param != ''){
						var status_list = new Array();
						status_list.push({text:'--请选择--',value:'', checked:true});
						var ls = stat_fn_param.split('|');
						$.each(ls,function(i,code){
							status_list.push({text:ls[i], value:i});
						});
						
						grid.table.rowForm.disable('UP_NAME1');// 输入框隐藏	
						grid.table.rowForm.disable('UP_NAME3');// 输入框隐藏	
						grid.table.rowForm.enable('UP_NAME2'); // 下拉框显示
					
						grid.table.rowForm.getItem('UP_NAME2').component.reload(status_list);
						
						grid.table.rowForm.getItem('UP_NAME2').val(up_name);// 为自定义行为习惯赋值
						grid.table.rowForm.getItem('UP_NAME1').val('');// 为自定义行为习惯赋值
						//grid.table.rowForm.getItem('UP_NAME3').val('');// 为自定义行为习惯赋值
					}
					
					// 城市代码、省份代码、国家代码
					else if(stat_datafd && (stat_datafd.toUpperCase() == 'COUNTRYCODE'||stat_datafd.toUpperCase() == 'CITYCODE'||stat_datafd.toUpperCase() == 'REGIONCODE')){
						grid.table.rowForm.disable('UP_NAME1');// 输入框隐藏	
						grid.table.rowForm.disable('UP_NAME2');// 输入框隐藏	
						grid.table.rowForm.enable('UP_NAME3'); // 下拉框显示
						grid.table.rowForm.getItem('UP_NAME2').val('');// 为自定义行为习惯赋值
						grid.table.rowForm.getItem('UP_NAME1').val('');// 为自定义行为习惯赋值
						
						var up_name_s = up_name.split('|');
						var country_d = up_name_s != null && up_name_s.length > 0 ? up_name_s[0]:"";
						var region_d = up_name_s != null && up_name_s.length > 1 ? up_name_s[1]:"";
						var city_d = up_name_s != null && up_name_s.length > 2 ? up_name_s[2]:"";
						
						up_name = country_d+'|'+region_d+'|'+city_d;
						
						var region_code = grid.table.rowForm.getItem('UP_NAME3').getComponent('regioncode');
						var citycode = grid.table.rowForm.getItem('UP_NAME3').getComponent('citycode');
						
						region_code.opts.ds = null;
						region_code.reload([{text:'---请选择---', value:''}]);
						citycode.opts.ds = null;
						citycode.reload([{text:'---请选择---', value:''}]);
						
						
						grid.table.rowForm.getItem('UP_NAME3').getComponent('regioncode').jqDom.hide();
						grid.table.rowForm.getItem('UP_NAME3').getComponent('citycode').jqDom.hide();
						if(stat_datafd == 'REGIONCODE'||stat_datafd == 'CITYCODE'){
							
							
							if (region_d != '') {
								jcl.postJSON('/tms35/userpattern/getRegion', 'country_code='+country_d, function(data){
									region_code.reload(data.list,{text:'CODE_VALUE',value:'CODE_KEY'});
									grid.table.rowForm.getItem('UP_NAME3').val(up_name);
								});
							}
							
							grid.table.rowForm.getItem('UP_NAME3').getComponent('regioncode').jqDom.show();
							
							if(stat_datafd == 'CITYCODE'){
								if (city_d != '') {
									jcl.postJSON('/tms35/userpattern/getCity', 'region_code='+region_d, function(data){
										citycode.reload(data.list,{text:'CODE_VALUE',value:'CODE_KEY'});
										grid.table.rowForm.getItem('UP_NAME3').val(up_name);
									});
								}
								
								grid.table.rowForm.getItem('UP_NAME3').getComponent('citycode').jqDom.show();
							}
						}
						grid.table.rowForm.getItem('UP_NAME3').val(up_name);
					}
					else if(stat_fn.toLowerCase () == "snapshot" && data_fd_type == 'datetime'){
						// readonly
						grid.table.rowForm.getItem('UP_NAME1').component.attr('readonly',true);
						grid.table.rowForm.getItem('UP_NAME1').component.focus(function(){
							jcl_tms.datepicker(this);
						});
					}
				}
				
				grid.table.rowForm.getItem('STAT_DATAFD').viewMode();// 统计目标不可编辑
				grid.table.rowForm.getItem('STAT_FN').viewMode();  // 统计函数不可编辑
			});
			
			// 删除
			grid.toolbar.onClick('#up-btn-del', function(){
				if(confirm('确定删除？')){
					var rows = grid.selectedRows();
					// 只为记录日志使用
					var stat_datafd = statInfo.STAT_DATAFD;
					var data_fd_text = '';
					// 获取引用对象对应的数据类型、代码集
					$.each(txnFeature.row, function(i,row){
						if(stat_datafd != '' && stat_datafd == row['CODE_KEY'])
						{
							data_fd_text = row['CODE_VALUE'];
							data_fd_type = row['TYPE'];
							data_fd_code = row['FD_CODE'];
							return false;
						}
						
						data_fd_text = stat_datafd;
					});
					
					// 根据不同代码集为下拉框加载数据
					var status_list = new Array();
					if(data_fd_code != undefined && data_fd_code != ''){
						var ls = jcl.code.getCodes(data_fd_code);
						status_list.push({text:'--请选择--',value:'', checked:true});
						$.each(ls,function(i,code){
							status_list.push({text:code, value:i});
						});
					}
						
					$.each(rows,function(i,row){
						// 补充数据，记录日志需要
						row.DATA_FD_TEXT = data_fd_text == null ? "":data_fd_text;
						row.USER_ID = user_id;
						var up_name = row.UP_NAME;
						row.UP_TEXT = up_name;
						row.STAT_DESC = statInfo.STAT_DESC;
						
						$.each(status_list,function(i,d){
							if(up_name == d.value){
								row.UP_TEXT = d.text;
								return false;
							}
						});
					});
					
					
					var json_data = JSON.stringify({'del':rows});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode  + "&userId=" + user_id + "&statId=" + stat_id;

					jcl.postJSON('/tms35/userpattern/save', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			
			//国家改变时
			grid.table.rowForm.getItem('UP_NAME3').getComponent('countrycode').onChange(function(_this){
				var regioncode = grid.table.rowForm.getItem('UP_NAME3').getComponent('regioncode');
				var citycode = grid.table.rowForm.getItem('UP_NAME3').getComponent('citycode');
				if(_this.value){
					regioncode.opts.ds = {type:'remote', url:'/tms35/userpattern/getRegion?country_code='
						+_this.value, parser:{text:'CODE_VALUE',value:'CODE_KEY'}};
					regioncode.reload();
					regioncode.opts.items.unshift({text:'---请选择---', value:''});
				}else{
					regioncode.opts.ds = null;
					regioncode.reload([{text:'---请选择---', value:''}]);
				}
				regioncode.select(0);
				citycode.opts.ds = null;
				citycode.reload([{text:'---请选择---', value:''}]);
				citycode.select(0);
			});
			
			//地区改变时
			grid.table.rowForm.getItem('UP_NAME3').getComponent('regioncode').onChange(function(_this){
				var citycode = grid.table.rowForm.getItem('UP_NAME3').getComponent('citycode');
				if(_this.value){
					citycode.opts.ds = {type:'remote', url:'/tms35/userpattern/getCity?region_code='+_this.value, parser:{text:'CODE_VALUE',value:'CODE_KEY'}};
					citycode.reload();
					citycode.opts.items.unshift({text:'---请选择---', value:''});
				}else{
					citycode.opts.ds = null;
					citycode.reload([{text:'---请选择---', value:''}]);
				}
				citycode.select(0);
			});
	
			// 表单确定
			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				if(check_stat_data(data)){
					
					var name1 = data.UP_NAME1;
					var name2 = data.UP_NAME2;
					var name3 = data.UP_NAME3;
					
					if(name1 != undefined && name1 != ''){
						data.UP_VALUE = name1;
						data.UP_NAME = name1;
						data.UP_TEXT = name1;
					}else if(name2 != undefined && name2 != ''){
						data.UP_VALUE = name2;
						data.UP_NAME = name2;
						data.UP_TEXT = grid.table.rowForm.getItem('UP_NAME2').getText();
					}else{
						var stat_datafd = statInfo.STAT_DATAFD;
						var n = name3.split('|');
						var region_code = grid.table.rowForm.getItem('UP_NAME3').getComponent('regioncode');
						var crountrycode = grid.table.rowForm.getItem('UP_NAME3').getComponent('countrycode');
						var citycode = grid.table.rowForm.getItem('UP_NAME3').getComponent('citycode');
						
						if(stat_datafd && stat_datafd.toUpperCase() == 'COUNTRYCODE'){
							data.UP_VALUE = name3;
							data.UP_NAME = crountrycode.select().text;
							data.UP_TEXT = crountrycode.select().text;
						}
						if(stat_datafd && stat_datafd.toUpperCase() == 'REGIONCODE'){
							data.UP_VALUE = name3;
							data.UP_NAME = crountrycode.select().text+'-'+region_code.select().text;
							data.UP_TEXT = crountrycode.select().text+'-'+region_code.select().text;
						}
						if(stat_datafd && stat_datafd.toUpperCase() == 'CITYCODE'){
							data.UP_VALUE = name3;
							data.UP_NAME = crountrycode.select().text+'-'+region_code.select().text+'-'+citycode.select().text;
							data.UP_TEXT = crountrycode.select().text+'-'+region_code.select().text+'-'+citycode.select().text;
						}
					}
					
					// 补充数据，记录日志需要
					data.DATA_FD_TEXT = grid.table.rowForm.getItem('STAT_DATAFD').getText();
					data.USER_ID = user_id;
					data.STAT_DESC = statInfo.STAT_DESC;
					
					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}
					
					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					var json_data_encode = encodeURIComponent(json_data);
					
					var param = "postData=" + json_data_encode + "&userId=" + user_id + "&statId=" + stat_id;

					jcl.postJSON('/tms35/userpattern/save', param, function(data1){
						grid.table.updateEditingRow(data1.row);
						alert("保存成功");
					});
					return false;
				}else{
					return false;
				}
		    });
			
			grid.table.onRowSelectChange(function(){
				resetEffectivenessBtn();
			});

			grid.table.onModelChange(function(){
				resetEffectivenessBtn();
			});
			
			// 查看按钮事件
			grid.toolbar.onClick('#up-btn-view', function(){
				grid.table.rowForm.jqDom.find("#up-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#up-btn-add', function(){
				grid.table.rowForm.jqDom.find("#up-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#up-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#up-row-edit-sure").show();
			});
			
			function resetEffectivenessBtn(){
				if(dualAudit == 'true'){ // readonly mode
					grid.toolbar.disable($('#up-btn-add'), false);
					grid.toolbar.disable($('#up-btn-edit'), false);
					grid.toolbar.disable($('#up-btn-del'), false);
				}
			}

			function check_stat_data(data){
				var up_name1 = data.UP_NAME1;
				var up_name2 = data.UP_NAME2;
				var up_name3 = data.UP_NAME3;
				var start_date = data.START_DATE;
				var end_date = data.END_DATE;
				var stat_fn = data.STAT_FN;
				
				var up_name = (up_name1 == '' || up_name1 == undefined)? up_name2:up_name1;
				if(up_name == undefined || up_name== ''){
					if(up_name3 != undefined && up_name3 != ''){
						var stat_datafd = statInfo.STAT_DATAFD;
						var n = up_name3.split('|');
						if(stat_datafd && stat_datafd.toUpperCase() == 'COUNTRYCODE'){
							up_name = n[0];
						}
						if(stat_datafd && stat_datafd.toUpperCase() == 'CITYCODE'){
							up_name = n[2];
						}
						if(stat_datafd && stat_datafd.toUpperCase() == 'REGIONCODE'){
							up_name = n[1];
						}
					}
				}
				
				if(up_name == '' || up_name == undefined){
					alert("行为习惯值不能为空");
					return false;
				}
				
				// 输入框校验
				if(up_name1 != '' && up_name1 != undefined){
					if(!checkeSpecialCode(up_name)){
						alert("行为习惯值不能输入特殊字符");
						return false;
					}
					
					if(stat_fn == 'status'){
						if(up_name != '0' && up_name != '1'){
							alert("统计函数是状态，行为习惯值请输入0或1");
							return false;						
						}
					}else if(stat_fn == 'count' || stat_fn == 'count_uniq'){
						if(!IsInt(up_name,'+',0)||!(0<=up_name && up_name<=999999999999)){
							alert("行为习惯值请输入非负整数,范围为0-999999999999");
							return false;
						}
					}else if(data_fd_type == 'long'){
						if(!IsInt(up_name,'+',0)||!(0<=up_name && up_name<=999999999999)){
							alert("行为习惯值请输入非负整数,范围为0-999999999999");
							return false;
						}
					}else if(data_fd_type == 'double'||data_fd_type == 'money'){
						if(!IsNumber(up_name,'+',2)||!(0<=up_name && up_name<=999999999999.99)){
							alert("行为习惯值请输入非负小数（保留两位小数）或非负整数,范围为0-999999999999.99");
							return false;
						}	
					}else if(data_fd_type == 'ip'){
						if(!isIP(up_name)){
							alert("行为习惯值请输入IP");
							return false;							
						}
					}else{
						if(!checkeLength(up_name,100)){
							alert("行为习惯值不能超过100个字符");
							return false;
						}
					}				
				}
						
				if(start_date == ''){
					alert("开始日期不能为空");
					return false;
				}
				
				if(start_date != '' && end_date != '' && (new Date(end_date.replace(/-/g, "/")) < new Date(start_date.replace(/-/g, "/")))){
					alert("结束日期不能小于开始日期")
					return false;
				}
				
				return true;
			}

			// 加载数据
			this.load(user_id,stat_id);

		},
		load: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			if(dualAudit == 'true'){ // readonly mode
				grid.toolbar.disable($('#up-btn-add'), false);
				grid.toolbar.disable($('#up-btn-edit'), false);
				grid.toolbar.disable($('#up-btn-del'), false);
			}
			
				
			// 查询自定义行为习惯列表
			var param = "user_id="+user_id+"&stat_id="+stat_id;
			jcl.postJSON('/tms35/userpattern/userPatternList', param, function(data){
				statInfo = data.statInfo;
				/*ad_list = data.ad_list;
						
				if(ad_list != null){
					ad_list.splice(0,0,{CODE_VALUE:"--请选择--",CODE_KEY:""});
					grid.table.rowForm.getItem('UP_NAME2').component.reload(ad_list,{text:'CODE_VALUE',value:'CODE_KEY'});	
				}
				*/
				// 查询交易属性列表
				jcl.postJSON('/tms/stat/txnFeature', 'txn_id='+statInfo.STAT_TXN, function(txnFeatureData){
					txnFeature = txnFeatureData;
					txnFeature.row.splice(0,0,{CODE_VALUE:"--请选择--",CODE_KEY:""});
					
					if (statInfo.STAT_DATAFD != "") {
						grid.table.rowForm.getItem('STAT_DATAFD').component.reload(txnFeatureData.row, {text: 'CODE_VALUE',value: 'CODE_KEY'});
					}
					
					jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='2'", function(data1){
						funcList = data1;
						
						grid.table.rowForm.getItem('STAT_FN').component.reload(funcList.row,{text:'CODE_VALUE',value:'CODE_KEY'});							
						
						grid.renderPage({list: data.list});
						
						cond_dialog.jqDom.center();// dialog居中
					});						
				});						
			});						
			
		},
		isDirty: function(){
			return false;
		},
		save: function(){
			
			
		}
	};
})();
