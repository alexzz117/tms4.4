
var model = window.model ||{};

(function(){
	var grid = null;
	var stat_isDirty = false;
	var txnfeature ;
	var func ;
	var txnId;
	var enableStoreFd;
	var allStoreFd;
	//数据类型归类
	var _dataTypeClassify = [
		{recap:'long', type:['long', 'time', 'datetime']},
		{recap:'double', type:['double', 'money']},
		{recap:'string', type:['string', 'devid', 'ip', 'userid', 'acc', 'code','object']}
	];
	model.stat = {
		
		init: function(this_txnId, page){
			txnId = this_txnId;
			$('#statGridDiv').empty();
			var isCode = jcl.code.getCodes('common.is');
			var isCode_list = new Array();
			$.each(isCode,function(i,code){
				var o ;
				if(i==0){
					o = {text:code, value:i, checked:true};
				}else{
					o = {text:code, value:i};
				}
				isCode_list.push(o);
			});
			var status = jcl.code.getCodes('tms.mgr.rulestatus');
			var status_list = new Array();
			$.each(status,function(i,code){
				var o ;
				if(i==0){
					o = {text:code, value:i, checked:true};
				}else{
					o = {text:code, value:i};
				}
				status_list.push(o);
			});

			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				toolbar:[
					{id:"btn-find", icon:"icon-tb-find", text:"查询", action:'toggleQform'},
					{id:"st-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"st-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"st-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"st-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"st-btn-valid-y", icon:"icon-tb-edit", text:"启用"},
					{id:"st-btn-valid-n", icon:"icon-tb-edit", text:"停用"},
					{id:"btn-rf", icon:"icon-tb-edit", text:"引用点", enable:'oneRowSelected'}
				],
				qform:{display:false,items:[
	              {name:'STAT_NAME', label:'统计名称'},
	              {name:'STAT_DESC', label:'统计描述'},
	              {name:'STAT_PARAM', label:'统计引用对象', type:'listselector', title:'--请选择--', ds:{type:'remote', url:'/tms/stat/txnFeature', param:'txn_id='+txnId, parser:{key:'row', text:'CODE_VALUE', value:'CODE_KEY'}, lazyLoad:true}},
	              {name:'STAT_DATAFD', label:'统计目标', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'remote', url:'/tms/stat/txnFeature', param:'txn_id='+txnId, parser:{key:'row', text:'CODE_VALUE', value:'CODE_KEY'}, lazyLoad:true}, selectedTopItem:true},
	              {name:'STAT_FN', label:'统计函数', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'remote', url:'/tms/stat/code', param:'category_id=tms.pub.func&args=2', parser:{key:'row', text:'CODE_VALUE', value:'CODE_KEY'}, lazyLoad:true}, selectedTopItem:true},
	              {name:'STAT_VALID', label:'有效性', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}],ds:{type:'code', category:'tms.mgr.rulestatus'}, selectedTopItem:true}
       			 ], action:'localQuery'},
				table:{
					sortType: 'local',
					columns:[
						{name:"统计名称", width: 10, dataIndex:'STAT_NAME'},
						{name:"统计描述", width: 35, dataIndex:'STAT_DESC'},
						{name:"统计引用对象", width: 20, dataIndex:'STAT_PARAM',render:function(v){
							var return_text = '';
							var a = v.split(',');
							$.each(a,function(j,a){
								var _t = '';
								$.each(txnfeature.row, function(i,row){
									if(a != '' && a == row['CODE_KEY'])
									{
										_t = row['CODE_VALUE'];
										return false;
									}
								});
								if(_t.length == 0) _t = a;
								if(return_text.length > 0) return_text += ",";
								return_text += _t;
							});

							return return_text;
						}},// 下拉
						//{name:"统计条件", width: 15, dataIndex:'STAT_COND'},
						{name:"统计目标", width: 20, dataIndex:'STAT_DATAFD',render:function(v){
							var return_text = v;
							$.each(txnfeature.row, function(i,row){
								if(v != '' && v == row['CODE_KEY'])
								{
									return_text = row['CODE_VALUE'];
									return false;
								}
							});
							return return_text;
						}},
						{name:"统计函数", width: 15, dataIndex:'STAT_FN',render:function(v){
							var return_text = v;
							$.each(func.row, function(i,row){
								if(v != '' && v == row['CODE_KEY'])
								{
									return_text = row['CODE_VALUE'];
									return false;
								}
							});
							return return_text;
						}},// 下拉
						{name:'存储字段', width:10, dataIndex:'STORECOLUMN'},
						{name:"有效性", width: 8, dataIndex:'STAT_VALID', render:'tms.mgr.rulestatus'}
					],
					rowForm:{
		                items:[
	                       {label:"统计描述", name: 'STAT_DESC', type:'text', required:true, cls:"cond-textarea"},
	                       {label:"统计引用对象", name: 'STAT_PARAM', type:'listselector', title:'--请选择--',required:true},// 下拉
	                       {label:"统计函数", name: 'STAT_FN', type:'selector', title:'--请选择--', required:true},
	                       {label:"条件表达式", name: 'STAT_COND', type:'hidden'},
	                       {label:"统计条件", name: 'STAT_COND_IN', type:'text'},
	                       {label:"统计目标", name: 'STAT_DATAFD', type:'selector' ,items:[{text:'--请选择--',value:''}], required:true},
	                       {label:"函数参数", name: 'FN_PARAM', type:'text', required:true},
	                       {label:"单位", name: 'COUNUNIT', type:'selector', title:'--请选择--', ds:{type:'code', category:'tms.stat.condunit', selectedTopItem:true}, required:true},
	                       {label:"周期", name: 'COUNTROUND', type:'text', required:true},
	                       {label:"交易结果", name: 'RESULT_COND', type:'selector',title:'--请选择--', ds:{type:'code', category:'tms.stat.txnstatus', selectedTopItem:true}, required:true},
						   {label:'数据类型', name:'DATATYPE', type:'selector', title:'--请选择--', items:[{text:'--请选择--', value:''}], ds:{type:'code', category:'tms.stat.datatype', selectedTopItem:true}, required:true},
						   {label:'存储字段', name:'STORECOLUMN', type:'selector', title:'--请选择--'},
	                       {label:"连续", name: 'CONTINUES', type:'checkbox',items:[{text:'', value:'1'}]},
	                       {label:"事中", name: 'STAT_UNRESULT', type:'checkbox',items:[{text:'', value:'1'}]},
	                       {label:"有效性", name: 'STAT_VALID', type:'radio',value:'0', items:status_list}
	                   ],
	                   btns:[
	                       {text:'确定', id:'st-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'st-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#statGridDiv'));

			// 表单确定
			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				if(check_stat_data(data)){
					data.STAT_TXN = txnId;
					data.CONTINUES = data.CONTINUES == '1' ? '1' : '0';
					data.STAT_UNRESULT = data.STAT_UNRESULT == '1' ? '1' : '0';

					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}

					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					var json_data_encode = encodeURIComponent(json_data);
				
					var url_para = "postData=" + json_data_encode + "&txnId=" + txnId;

					jcl.postJSON('/tms/stat/save', url_para, function(data1){
						grid.table.updateEditingRow(data1.row);
						_reloadEnableStorageFd(txnId);// 加载存储字段下拉列表数据
						alert("保存成功");
					});
					return false;
				}else{
					return false;
				}
		    });

			grid.table.onRowFormShow(function(isAdd){
				grid.table.rowForm.disable('DATATYPE');// 数据类型隐藏，与函数数据类型一致，若函数数据类型是object，则需要手工指定数据类型
				
				var formType ;
				if(isAdd == undefined){
					formType = 'view';
				}
				itemStatus(grid.table.rowForm, 2, formType);// 显示
				
				_reloadEnableStorageFd(txnId);// 加载存储字段下拉列表数据
				
				var fl = true;
				var storecolumn = '';
				var type = '';
				if(!isAdd){
					storecolumn = grid.selectedOneRow().STORECOLUMN;
					fl = false;
					type = grid.selectedOneRow().DATATYPE;
				}
				
				
				grid.table.rowForm.getItem('STORECOLUMN').component.reload(_getCanUseStorageFdByDataType(type, fl), {text:'CODE_VALUE', value:'CODE_KEY'});// 根据函数类型过滤存储字段
				grid.table.rowForm.getItem('STORECOLUMN').val(storecolumn);
				
				var rowData = grid.table.rowForm.data();
				grid.table.rowForm.getItem('STAT_COND_IN').component.attr("readonly","readonly");
				
				if (rowData.STAT_FN == "calculat_expressions") {// 计数表达式函数
					itemStatus(grid.table.rowForm, 1, formType);// 字段隐藏
					grid.table.rowForm.enable('DATATYPE');// 数据类型显示，与函数数据类型一致，若函数数据类型是object，则需要手工指定数据类型
				}
				else {
					if (rowData.STAT_FN == "rang_bin_dist") {// 区间函数
						var fd = rowData.STAT_DATAFD;
						if (fd != undefined && fd != '') {
							var data_type = "";
							
							// 显示表单的函数参数
							grid.table.rowForm.enable('STAT_DATAFD');
							
							var one_feature = queryTxnFeature(fd);
							if (one_feature) 
								data_type = one_feature.TYPE;
							if (data_type == 'datetime' || data_type == 'time' || data_type == 'double' || data_type == 'money' || data_type == 'long') {
								// 显示表单的函数参数
								grid.table.rowForm.enable('FN_PARAM');
							}
							else {
								//隐藏表单的函数参数
								rowData.FN_PARAM = '';
								grid.table.rowForm.disable('FN_PARAM');
							}
						}
					}
					else {
						if (rowData.STAT_FN == "count" || rowData.STAT_FN == "status") {
							// 隐藏表单的统计目标
							rowData.STAT_DATAFD = '';
							grid.table.rowForm.disable('STAT_DATAFD');
						}
						else {
							// 显示表单的函数参数
							grid.table.rowForm.enable('STAT_DATAFD');
						}
						// 隐藏表单的函数参数
						rowData.FN_PARAM = '';
						grid.table.rowForm.disable('FN_PARAM');
					}
				countround_change_event();
				}
				grid.table.rowForm.set(rowData);

				
				if(!isAdd){// 不能修改
					grid.table.rowForm.getItem('COUNUNIT').viewMode();					
					grid.table.rowForm.getItem('FN_PARAM').viewMode();					
					grid.table.rowForm.getItem('STAT_PARAM').viewMode();					
					grid.table.rowForm.getItem('STAT_FN').viewMode();					
					grid.table.rowForm.getItem('STAT_DATAFD').viewMode();					
				}
			});
			
			// 查看按钮事件
			grid.toolbar.onClick('#st-btn-view', function(){
				grid.table.rowForm.jqDom.find("#st-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#st-btn-add', function(){
				grid.table.rowForm.jqDom.find("#st-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#st-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#st-row-edit-sure").show();
			});

			grid.toolbar.onClick('#st-btn-del', function(){
				if(confirm('确定删除？')){
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txnId;

					jcl.postJSON('/tms/stat/save', url_para, function(callback){
						grid.table.deleteSelectedRow();
						_reloadEnableStorageFd(txnId);// 加载存储字段下拉列表数据
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			
			var _ce = new condEdit();
			// 表单条件双击
			grid.table.rowForm.jqDom.on('dblclick', '[name=STAT_COND_IN]', function(){
				_ce.init_cond(grid.table.rowForm.getItem('STAT_COND').component,$(this),txnId, ['RULE_FUNC','AC_FUNC'],'条件');
			});


			// 表单函数选择事件
			 grid.table.rowForm.getItem('STAT_FN').component.onChange(function(item){
			 	fn_change_event();
				
				countround_change_event();
		    });
			
			 // 表单数据类型选择事件
			 grid.table.rowForm.getItem('DATATYPE').component.onChange(function(item){
			 	var rowData = grid.table.rowForm.data();
			 	grid.table.rowForm.getItem('STORECOLUMN').val('');// 存储字段清空
				rowData.STORECOLUMN='';// 存储字段清空
				
				// 根据函数类型过滤存储字段
				grid.table.rowForm.getItem('STORECOLUMN').component.reload(_getCanUseStorageFdByDataType(item.value, true), {
					text:'CODE_VALUE', value:'CODE_KEY'
				});
			});
			
			function changeFeatureTypeToDataType(type){
				var _type = type;
				$.each(_dataTypeClassify, function(d, dt){
					if($.inArray(type, dt.type)!=-1){
						_type = dt.recap;
						return false
			 		}
				});
				return _type;
			}
			
			function fn_change_event(){
				var rowData = grid.table.rowForm.data();

				grid.table.rowForm.getItem('STORECOLUMN').val('');// 存储字段清空
				rowData.STORECOLUMN='';// 存储字段清空
					
				if (rowData.STAT_FN == "calculat_expressions") {// 计数表达式
					// 隐藏表单，只显示统计描述、把统计条件改为表达式、统计字段、有效性
					itemStatus(grid.table.rowForm,1);
				}else {
					itemStatus(grid.table.rowForm,2);
					
					if (rowData.STAT_FN == "rang_bin_dist") {// 区间函数
						var fd = rowData.STAT_DATAFD;
						if (fd != undefined && fd != '') {
							var data_type = "";
							var one_feature = queryTxnFeature(fd);
							if (one_feature) 
								data_type = one_feature.TYPE;
							if (data_type == 'datetime' || data_type == 'time' || data_type == 'double' || data_type == 'money' || data_type == 'long') {
								// 显示表单的函数参数
								grid.table.rowForm.enable('FN_PARAM');
							}
							else {
								//隐藏表单的函数参数
								rowData.FN_PARAM = '';
								grid.table.rowForm.disable('FN_PARAM');
							}
						}
						else {
							//隐藏表单的函数参数
							rowData.FN_PARAM = '';
							grid.table.rowForm.disable('FN_PARAM');
						}
						rowData.STAT_DATAFD = '';
						grid.table.rowForm.enable('STAT_DATAFD');
					}
					else {
						if (rowData.STAT_FN == "count" || rowData.STAT_FN == "status") {
							// 隐藏表单的统计目标
							rowData.STAT_DATAFD = '';
							grid.table.rowForm.disable('STAT_DATAFD');
						}
						else {
							// 显示表单的函数参数
							grid.table.rowForm.enable('STAT_DATAFD');
						}
						// 隐藏表单的函数参数
						rowData.FN_PARAM = '';
						grid.table.rowForm.disable('FN_PARAM');
					}
				}
				
				var type = '';// 数据类型
				if(rowData.STAT_FN == 'snapshot'){
					// 快照数据类型取统计字段数据类型
					var fd = rowData.STAT_DATAFD;
					var one_feature = queryTxnFeature(fd);
					if (one_feature) {
						type = changeFeatureTypeToDataType(one_feature.TYPE);
					}
				}else{
					// 其他函数取取函数的数据类型
					$.each(func.row, function(i,fc){
						if(fc.CODE_KEY == rowData.STAT_FN){
							type = fc.FUNC_TYPE;
						}	
					});
				}
				
				if (type == 'object' && rowData.STAT_FN == "calculat_expressions") {
					// 显示数据类型字段
					rowData.DATATYPE="";
					rowData.STORECOLUMN="";
					grid.table.rowForm.getItem('DATATYPE').val('');
					grid.table.rowForm.getItem('STORECOLUMN').val('');
					grid.table.rowForm.enable('DATATYPE');
					type='';
				}else{
					grid.table.rowForm.getItem('DATATYPE').val(type);
					rowData.DATATYPE=type;
					// 隐藏数据类型字段
					grid.table.rowForm.disable('DATATYPE');
					// 给数据类型字段赋值
				}
				// 根据函数类型过滤存储字段
				grid.table.rowForm.getItem('STORECOLUMN').component.reload(_getCanUseStorageFdByDataType(type, true), {
					text:'CODE_VALUE', value:'CODE_KEY'
				});
				grid.table.rowForm.set(rowData);
			}

			// 表单统计目标选择事件
			 grid.table.rowForm.getItem('STAT_DATAFD').component.onChange(function(item){
			 	datafd_change_event();
		    });
			
			function itemStatus(rowForm,type, formType){
				var rowData = rowForm.data();
				var _rowForm = rowForm;
				switch(type)
				{
					case 1:// 计算表达式
						rowData.STAT_PARAM = '';
						_rowForm.disable('STAT_PARAM');
						rowData.STAT_DATAFD = '';
						_rowForm.disable('STAT_DATAFD');
						rowData.COUNUNIT = '';
						_rowForm.disable('COUNUNIT');
						rowData.COUNTROUND = '';
						_rowForm.disable('COUNTROUND');
						rowData.FN_PARAM = '';
						_rowForm.disable('FN_PARAM');
						rowData.RESULT_COND = '';
						_rowForm.disable('RESULT_COND');
						rowData.CONTINUES = '';
						_rowForm.disable('CONTINUES');
						rowData.STAT_UNRESULT = '';
						_rowForm.disable('STAT_UNRESULT');
						
						_rowForm.getItem('STAT_COND_IN').jqDom.find('.form-item-label').text('');
						// 查看不显示星号
						_rowForm.getItem('STAT_COND_IN').jqDom.find('.form-item-label').prepend((formType=='view'?'':'<font color="red">*</font>')+'表达式:');
						break;
					case 2:
						_rowForm.enable('STAT_PARAM');
						_rowForm.enable('STAT_DATAFD');
						_rowForm.enable('COUNUNIT');
						_rowForm.enable('COUNTROUND');
						_rowForm.enable('FN_PARAM');
						_rowForm.enable('RESULT_COND');
						_rowForm.enable('CONTINUES');
						_rowForm.enable('STAT_UNRESULT');
						
						_rowForm.getItem('STAT_COND_IN').jqDom.find('.form-item-label').text('统计条件:');
						break;
					default:
						break;
				}
			}
			function datafd_change_event(){
				var rowData = grid.table.rowForm.data();
				
				grid.table.rowForm.getItem('STORECOLUMN').val('');// 存储字段清空
				rowData.STORECOLUMN='';// 存储字段清空
				
			 	var fn = rowData.STAT_FN;
			 	var fd = rowData.STAT_DATAFD;
				var data_type="";
				var one_feature = queryTxnFeature(fd);
				if(one_feature) data_type = one_feature.TYPE;
				
				// 快照，通过统计目标给数据类型赋值
				if(fn == 'snapshot'){
					var type = changeFeatureTypeToDataType(data_type);
					rowData.DATATYPE = type;
					grid.table.rowForm.getItem('DATATYPE').val(type);
					// 根据函数类型过滤存储字段
					grid.table.rowForm.getItem('STORECOLUMN').component.reload(_getCanUseStorageFdByDataType(type, true), {
						text:'CODE_VALUE', value:'CODE_KEY'
					});
				}

				if (fn == "rang_bin_dist") {// 区间函数
					if((data_type == 'datetime'||data_type == 'time'||data_type == 'double'||data_type == 'money'||data_type == 'long')){
						// 显示表单的函数参数
						rowData.FN_PARAM='';
						grid.table.rowForm.enable('FN_PARAM');
					}else{
						if(fd != ""){alert("非时间类型、日期时间类型和非数值类型的统计目标，不能使用区间分布函数");}
						rowData.STAT_DATAFD='';
						rowData.FN_PARAM='';
						grid.table.rowForm.disable('FN_PARAM');
					}

				}else{
					// 隐藏表单的函数参数
					rowData.FN_PARAM='';
					grid.table.rowForm.disable('FN_PARAM');
				}
				grid.table.rowForm.set(rowData);
			}
			
			
			function queryTxnFeature(code_value){
				var result;
				$.each(txnfeature.row,function(i,one_feature){
					if(one_feature.CODE_KEY == code_value){
						result = one_feature;
						return false;
					}
				});
				return result;
			}

			// 表单单位选择事件
			 grid.table.rowForm.getItem('COUNUNIT').component.onChange(function(item){
			 	countround_change_event();
		    });

			function countround_change_event(){
				var rowData = grid.table.rowForm.data();
				if (rowData.COUNUNIT == "7" || rowData.COUNUNIT == "9" || rowData.STAT_FN == "calculat_expressions") {// 会话，永久不需要周期
					// 隐藏表单的函数参数
					rowData.COUNTROUND = '';
					grid.table.rowForm.disable('COUNTROUND');

				}else{
					// 显示表单的函数参数
					grid.table.rowForm.enable('COUNTROUND');
				}
				grid.table.rowForm.set(rowData);
			}

			// 启用按钮事件
			grid.toolbar.onClick('#st-btn-valid-y', function(){
				if(confirm('确定启用？')){

					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {STAT_VALID: '1'})
						);
					});

					var json_data = JSON.stringify({'valid-y':list});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txnId;

					jcl.postJSON('/tms/stat/save', url_para, function(callback){
						grid.table.updateSelectedRow({STAT_VALID: 1});
						alert("启用成功");
					});

					return;
				}else{
					return;
				}
			});

			// 停用按钮事件
			grid.toolbar.onClick('#st-btn-valid-n', function(){
				if(confirm('确定停用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {STAT_VALID: '0'})
						);
					});

					var json_data = JSON.stringify({'valid-n':list});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txnId;

					jcl.postJSON('/tms/stat/save', url_para, function(callback){
						grid.table.updateSelectedRow({STAT_VALID: 0});
						alert("停用成功");
					});

					return;
				}else{
					return;
				}
			});

			function resetEffectivenessBtn(){

				if(dualAudit){ // readonly mode
					grid.toolbar.disable($('#st-btn-add'), false);
					grid.toolbar.disable($('#st-btn-edit'), false);
					grid.toolbar.disable($('#st-btn-del'), false);
					grid.toolbar.disable($('#st-btn-valid-y'), false);
					grid.toolbar.disable($('#st-btn-valid-n'), false);
					//grid.toolbar.disable($('#btn-rf'), false);
					grid.table.rowForm.jqDom.find("#st-row-edit-sure").hide();
					return;
				}

				grid.toolbar.enable('#st-btn-valid-y');
				grid.toolbar.enable('#st-btn-valid-n');

				
				if (grid.selectedRows().length == 0) {
					grid.toolbar.disable('#st-btn-valid-y'); // 启用按钮 disable
					grid.toolbar.disable('#st-btn-valid-n'); 
				} else {
					$.each(grid.selectedRows(), function(idx, row){
						if(row.STAT_VALID == '1'){ // 有启用状态的记录
							grid.toolbar.disable('#st-btn-valid-y'); // 启用按钮 disable
						} else if(row.STAT_VALID == '0'){
							grid.toolbar.disable('#st-btn-valid-n');
						}
					});
				}
			}

			grid.table.onRowSelectChange(function(){
				resetEffectivenessBtn();
			});

			grid.table.onModelChange(function(){
				resetEffectivenessBtn();
			});

			var params = new funcParams();

			// 表单函数参数双击
			grid.table.rowForm.jqDom.on('dblclick', '[name=FN_PARAM]', function(){
				var object_value = grid.table.rowForm.jqDom.find('[name=STAT_FN]').val();
				var stat_datafd = grid.table.rowForm.jqDom.find('[name=STAT_DATAFD]').val();
				// 区间统计类函数需要修改区间参数
				if(object_value == "rang_bin_dist"){
					var fn_param = $(this).val();
					//jcl.postJSON('/tms/stat/txnFeatureData','txnId='+txnId+"&stat_datafd="+stat_datafd,function(data){
						var param_type ="";
						var one_feature = queryTxnFeature(stat_datafd);
						if(one_feature) param_type = one_feature.TYPE;
						params.initParamList(fn_param,param_type,grid.table.rowForm);
					//});
				}
			});

			// 加载数据
			this.load(txnId);

			// 列表引用点
			//var ref = new ref_tree();
			 grid.toolbar.onClick('#btn-rf', function(){
			 	var statName = grid.selectedOneRow().STAT_NAME;
			 	ref.initTree(txnId,statName);
		    });
			// 列表保存
			 grid.toolbar.onClick('#btn-save', function(){
				var json_data = JSON.stringify(grid.table.change());
				var url_para = "postData=" + json_data + "&txnId=" + txnId;
				jcl.postJSON('/tms/stat/save', url_para, function(callback){
					stat_isDirty = false;
					alert("保存成功");
				});
		    });

		},
		load: function(this_txnId){
			txnId = this_txnId;
			
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			if(dualAudit){ // readonly mode
				grid.toolbar.disable($('#st-btn-add'), false);
				grid.toolbar.disable($('#st-btn-edit'), false);
				grid.toolbar.disable($('#st-btn-del'), false);
				//grid.toolbar.disable($('#btn-rf'), false);
				grid.toolbar.disable($('#st-btn-valid-y'), false);
				grid.toolbar.disable($('#st-btn-valid-n'), false);
			}
			grid.table.rowForm.jqDom.find('[name=FN_PARAM]').prop("readonly", true);

			jcl.postJSON('/tms/stat/txnFeature', 'txn_id='+txnId, function(data){
					txnfeature = data;

					grid.table.rowForm.getItem('STAT_PARAM').component.reload(data.row,{text:'CODE_VALUE',value:'CODE_KEY'});
					grid.qform.getItem('STAT_PARAM').component.reload(data.row,{text:'CODE_VALUE',value:'CODE_KEY'});

					data.row.unshift({CODE_VALUE:'--请选择--',CODE_KEY:''});
					grid.table.rowForm.getItem('STAT_DATAFD').component.reload(data.row,{text:'CODE_VALUE',value:'CODE_KEY'});
					grid.qform.getItem('STAT_DATAFD').component.reload(data.row,{text:'CODE_VALUE',value:'CODE_KEY'});

					jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='2'", function(data1){
							func = data1;

							grid.table.rowForm.getItem('STAT_FN').component.reload(data1.row,{text:'CODE_VALUE',value:'CODE_KEY'})

							data1.row.unshift({CODE_VALUE:'--请选择--',CODE_KEY:''});
							grid.qform.getItem('STAT_FN').component.reload(data1.row,{text:'CODE_VALUE',value:'CODE_KEY'});

							var param = "txnId="+txnId;
							// 初始化查询列表
							jcl.postJSON("/tms/stat/list", "txnId="+txnId, function(data){
								grid.renderPage({list: data.row});
								allStoreFd = data.allStoreFd;
								enableStoreFd = data.enableStoreFd;
							});
					});

			});

		},
		isDirty: function(){
			return false;//stat_isDirty;
		},
		save: function(){
			//alert(1);
		}
	};

	function check_stat_data(data){
		var statDesc=data.STAT_DESC;
		var statCond=data.STAT_COND;
		var counUnit=data.COUNUNIT;
		var statParam=data.STAT_PARAM;
		var statFn=data.STAT_FN;
		var countRound=data.COUNTROUND;
		var statValid=data.STAT_VALID;
		var result_cond=data.RESULT_COND;
		var continues=data.CONTINUES;
		var fun_param=data.FN_PARAM;
		var stat_datafd=data.STAT_DATAFD;
		var data_type=data.DATATYPE;

		var isCaExFunc = (statFn == 'calculat_expressions' ? true:false);// 计数表达式函数

		if(statDesc == undefined || Trim(statDesc)==""){
			alert("统计描述不能为空");
			return false;
		}else{

			if(!checkeLength(statDesc,200)){
				alert('统计描述不能超过200个字符');
				return false;
			}
			if (!checkSpecialCharacter(statDesc, "2")) {
				alert('统计描述只能包含汉字,字母,数字和下划线');
				return false;
			}

		}

		var cond_caption = "统计条件";
		if(isCaExFunc){
			cond_caption = "表达式";

			if(statCond.length == 0){
				alert(cond_caption+'不能为空');
				return false;
			}
		}


		if(statCond != undefined && statCond != ""){
			if(!checkeLength(statCond,512)){
				alert(cond_caption+'不能超过512个字符');
				return false;
			}else{
				if(!checkeCondSpecialCode(statCond)){
					alert(cond_caption+'不能包含特殊字符');
					return false;
				}
			}
		}
		
		if(statFn == undefined || statFn==""){
			alert("统计函数不能为空");
			return false;
		}else{
			
			if((!isCaExFunc && statFn != 'count' && statFn != 'status') && stat_datafd == ''){
				alert('统计函数为：非"状态"、非"计数"时，统计目标不能为空');
				return false;
			}

			if(!isCaExFunc && statFn == 'rang_bin_dist'){
				if(fun_param == undefined || fun_param == ''){
					alert("函数参数不能为空");
					return false;
				}else{

					if(!checkeLength(fun_param,512)){
						alert('函数参数不能超过512个字符');
						return false;
					}
				}
			}
		}

		if (!isCaExFunc) {
			if (statParam == undefined || statParam == "") {
				alert("统计引用对象不能为空");
				return false;
			}
			else {
				if (statParam.split(',').length > 6) {
					alert('统计引用对象最多选择6个');
					return false;
				}
				
				if (counUnit == '9') {
					var isFind = false;
					$.each(statParam.split(','), function(i, val){
						if (val.toUpperCase() == 'SESSIONID') {
							isFind = true;
							return false;
						}
					});
					if (!isFind) {
						alert('单位为会话，"统计引用对象"中必须包含"会话标识"');
						return false;
					}
				}
			}
			
			if (counUnit == undefined || Trim(counUnit) == "") {
				alert("单位不能为空");
				return false;
			}
			else {
				if (counUnit != '7' && counUnit != '9') {// 永久，会话不需要周期
					if (countRound == undefined || Trim(countRound) == "") {
						alert("周期不能为空");
						return false;
					}
					else {
					
						if (!IsNumber(countRound, '+', '0') || parseInt(countRound) < 1 || parseInt(countRound) > 100) {
							alert('周期只能输入1-100的正整数');
							return false;
						}
					}
				}
			}
			if(result_cond == undefined || result_cond == ""){
				alert("交易结果不能为空");
				return false;
			}
		}
		if(statValid == undefined || statValid==""){
			alert("有效性不能为空");
			return false;
		}

		if (isCaExFunc && (data_type == undefined || data_type=="")) {
			alert("数据类型不能为空");
			return false;
		}

		return true;
	}

function _getCanUseStorageFdByDataType(datatype, effect){
		var sfdItems = [];
		var enableStoreFds = enableStoreFd;
		if(!effect){
			var srows = grid.table.selectedRows();
			if(srows && srows.length > 0){
				var srow = srows[0];
				if(srow && srow['STORECOLUMN']){
					var allStoreFds = allStoreFd;
					var storeFd = _getStorageFdByfdName(allStoreFds, srow['STORECOLUMN']);
					if(storeFd){
						enableStoreFds.splice(0, 0, storeFd);
					}
				}
			}
		}
		sfdItems = sfdItems.concat(_getEnableStorageFdByDataType(datatype, enableStoreFds));
		return sfdItems;
	}
	
		/**
	 * @param storeFds		存储字段集合
	 * @param storeFdName	存储字段名
	 */
	function _getStorageFdByfdName(storeFds, storeFdName){
		var _storeFd = null;
		if(storeFds && storeFdName){
			$.each(storeFds, function(i, storeFd){
				if(storeFd['FD_NAME'] == storeFdName){
					_storeFd = storeFd;
					return false;
				}
			});
		}
		return _storeFd;
	}
	/**
	 * 根据数据类型获取可用存储字段
	 * @param	datatype		数据类型
	 * @param	enableStoreFds	可用存储字段集合
	 */
	function _getEnableStorageFdByDataType(datatype, enableStoreFds){
		var sfdItems = [{
			CODE_VALUE: '--请选择--',
			CODE_KEY: ''
		}];
		if(datatype && enableStoreFds){
			$.each(enableStoreFds, function(i, fd){
				$.each(_dataTypeClassify, function(d, dt){
					if($.inArray(datatype, dt.type)!=-1){
						if(dt.recap == 'long' || dt.recap == 'double' ){
							if(datatype != 'double'){
								if(fd['TYPE'] == datatype){
									sfdItems.push({CODE_VALUE: fd.FD_NAME, CODE_KEY: fd.FD_NAME});
								}
							}
							if(fd['TYPE'] == 'double'){
								sfdItems.push({CODE_VALUE: fd.FD_NAME, CODE_KEY: fd.FD_NAME});
							}
						}else if(dt.recap == 'string'){
							if(datatype != 'string'){
								if(fd['TYPE'] == datatype){
									sfdItems.push({CODE_VALUE: fd.FD_NAME, CODE_KEY: fd.FD_NAME});
								}
							}
						}
						
						if(fd['TYPE'] == 'string'){
							sfdItems.push({CODE_VALUE: fd.FD_NAME, CODE_KEY: fd.FD_NAME});
						}
		 			}
				});
			});
		}else{
			//$.extend(sfdItems, enableStoreFds);
		}
		return sfdItems;
	}
	/**
	 * 重新加载可用存储字段
	 * @param ds 加载配置对象
	 */
	function _reloadEnableStorageFd(tabName){
		jcl.postJSON('/tms35/tranmdl/queryAvailableStoreFd', 'tab_name='+tabName, function(data){
			if(data && data.enableStoreFd){
				enableStoreFd = data.enableStoreFd;
			}
		});
	};
})();