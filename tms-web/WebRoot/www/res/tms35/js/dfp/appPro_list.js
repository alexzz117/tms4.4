
var model = window.model ||{};

(function(){
	var grid = null;
	var appPro_isDirty = false;
	var appList = null;
	var proList;
	var appProList;
	model.dfpAppPro = {
		init: function(){
			$('#appProGridDiv').empty();
			
			var storeColumnList = jcl.code.getCodes('tms.dfp.storeColumn');
			
			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				toolbar:[
					{id:"appPro-btn-find", icon:"icon-tb-find", text:"查询", action:'toggleQform'},
					{id:"appPro-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"appPro-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"appPro-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"appPro-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'}					
				],
				qform:{display:false,items:[
	              {name:'APP_ID', label:'渠道名称', type:'selector',title:'--请选择--'}
       			 ], action:'localQuery'},
				table:{
					sortType: 'local',
					columns:[
						{name:"采集方式", width: 15, dataIndex:'APP_NAME'},
						{name:"属性ID", width: 5, dataIndex:'PROP_ID'},
						{name:"属性名称", width: 15, dataIndex:'PROP_NAME'},
						{name:"权重", width: 15, dataIndex:'WEIGHT'},
						{name:"存储字段", width: 15, dataIndex:'STORECOLUMN',render:'tms.dfp.storeColumn'},
						{name:"参与指纹运算", width: 15, dataIndex:'IS_TOKEN',render:'tms.dfp.is_token'},
						{name:"属性说明", width: 15, dataIndex:'PROP_COMMENT'}
					],
					rowForm:{
		                items:[
	                       {label:"ID", name: 'ID', type:'hidden'},
	                       {label:"PROP_NAME", name: 'PROP_NAME', type:'hidden'},
	                       {label:"采集方式", name: 'APP_ID', type:'selector',title:'--请选择--', required:true},
						   {label:"属性名称", name: 'PROP_ID', type:'selector',title:'--请选择--', required:true},
	                       {label:"权重", name: 'WEIGHT', type:'text', required:true},
	                       {label:"存储字段", name: 'STORECOLUMN',type:'selector',title:'--请选择--', required:true},
	                       {label:"参与指纹运算", name: 'IS_TOKEN',type:'selector',title:'--请选择--',items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.dfp.is_token'}, required:true},
	                       {label:"属性说明", name: 'PROP_COMMENT',type:'span'}
	                   ],
	                   btns:[
	                       {text:'确定', id:'appPro-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'appPro-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#appProGridDiv'));

			grid.table.rowForm.getItem('PROP_ID').component.onChange(function(v){
				grid.table.rowForm.getItem('PROP_NAME').val(v.text);
				
				if(v.value==''){
					grid.table.rowForm.getItem('PROP_COMMENT').val('');
				}else{
					$.each(proList,function(i,r){
						if(r.PROP_ID==v.value){
							grid.table.rowForm.getItem('PROP_COMMENT').val(r.PROP_COMMENT);
							return false;
						}
					});
				}
			});
				
			grid.table.rowForm.getItem('APP_ID').component.onChange(function(v){

				var _app_id = v.value;
				
				var id = grid.table.rowForm.getItem('ID').val();
				var _prop_id = '';
				var _storecolumn = '';
				if(id != ''){// 编辑
					var rowData = grid.table.selectedOneRow();
					_prop_id = rowData.PROP_ID;
					_storecolumn = rowData.STORECOLUMN;
				}

				grid.table.rowForm.getItem('PROP_ID').val('');					
				grid.table.rowForm.getItem('STORECOLUMN').val('');					

				
				// 查找属性名称
				var feature_type = findProList(_app_id,_prop_id);
				// 重新加载属性名称下拉列表
				grid.table.rowForm.getItem('PROP_ID').component.reload(feature_type,{text:'PROP_NAME',value:'PROP_ID'});
				
				// 查找存储字段
				var _scList = findStoreColumnList(_app_id,_storecolumn);
				// 重新加载属性名称下拉列表
				grid.table.rowForm.getItem('STORECOLUMN').component.reload(_scList,{text:'CODE_VALUE',value:'CODE_KEY'});
				
				
				
			});
			
			// 新建/编辑打开表单
			grid.table.onRowFormShow(function(isAdd){
				
				if (isAdd) {
					grid.table.rowForm.getItem('PROP_COMMENT').val('');
					return;
				}
				
				var rowData = grid.table.selectedOneRow();
				var _app_id = rowData.APP_ID;
				var _prop_id = rowData.PROP_ID;
				var _prop_name = rowData.PROP_NAME;
				var _storecolumn = rowData.STORECOLUMN;
				
				// 查找属性名称
				var feature_type = findProList(_app_id,_prop_id);
				
				// 重新加载属性名称下拉列表
				grid.table.rowForm.getItem('PROP_ID').component.reload(feature_type,{text:'PROP_NAME',value:'PROP_ID'});
				
				// 查找存储字段
				var _scList = findStoreColumnList(_app_id,_storecolumn);
				// 重新加载属性名称下拉列表
				grid.table.rowForm.getItem('STORECOLUMN').component.reload(_scList,{text:'CODE_VALUE',value:'CODE_KEY'});
				
				if(_app_id == ''){
					grid.table.rowForm.getItem('PROP_ID').val('');					
					grid.table.rowForm.getItem('STORECOLUMN').val('');					
				}else{
					grid.table.rowForm.getItem('PROP_ID').val(_prop_id);					
					grid.table.rowForm.getItem('STORECOLUMN').val(_storecolumn);		
				}

			});

			// 表单确定
			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				if(check_app_data(data)){
					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}

					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					var json_data_encode = encodeURIComponent(json_data);
				
					var url_para = "postData=" + json_data_encode;

					jcl.postJSON('/tms35/dfp/appProSave', url_para, function(data1){
						grid.table.updateEditingRow(data1.row);
						alert("保存成功");
						model.dfpAppPro.load();
					});
					return false;
				}else{
					return false;
				}
		    });

			// 查看按钮事件
			grid.toolbar.onClick('#appPro-btn-view', function(){
				grid.table.rowForm.jqDom.find("#appPro-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#appPro-btn-add', function(){
				grid.table.rowForm.jqDom.find("#appPro-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#appPro-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#appPro-row-edit-sure").show();
			});

			grid.toolbar.onClick('#appPro-btn-del', function(){
				if(confirm('确定删除？')){
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode;

					jcl.postJSON('/tms35/dfp/appProSave', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
						model.dfpAppPro.load();
					});
				}else{
					return false;
				}
			});
			

			// 加载数据
			this.load();

			function findStoreColumnList(app_id,storecolumn){
				var _proList = new Array();	
				_proList.push({CODE_VALUE:'--请选择--',CODE_KEY:''});
				
				if(app_id == '') return _proList;
				var _existProList = findProByAppid(app_id);
				
				// 查找该交易属性的数据类型
				$.each(storeColumnList,function(i,p){
					
					if(i == storecolumn){
						_proList.push({CODE_VALUE:p,CODE_KEY:i});
						return true;
					}
					
					var isExist = false; 
					$.each(_existProList,function(j,r){
						if(i == r.STORECOLUMN){
							isExist = true;
							return false;
						}
					});
					
					if(!isExist){
						_proList.push({CODE_VALUE:p,CODE_KEY:i});
					}
				});
				
				return _proList;
			}
			
			function findProList(app_id,prop_id){
				var _proList = new Array();	
				_proList.push({PROP_NAME:'--请选择--',PROP_ID:''});
				
				if(app_id == '') return _proList;
				var _existProList = findProByAppid(app_id);
				
				// 查找该交易属性的数据类型
				$.each(proList,function(i,p){
					var _prop_id = p.PROP_ID
					
					if(_prop_id == prop_id){
						_proList.push(p);
						return true;
					}
					
					var isExist = false; 
					$.each(_existProList,function(i,r){
						if(_prop_id == r.PROP_ID){
							isExist = true;
							return false;
						}
					});
					
					if(!isExist){
						_proList.push(p);
					}
				});
				
				return _proList;
			}
			
			function findProByAppid(app_id){
				var _proList = new Array();
				$.each(appProList,function(i,r){
					if(r.APP_ID==app_id){
						_proList.push(r);
					}
				});
				return _proList;
			}
		},
		load: function(){
			
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			// 初始化查询列表
			jcl.postJSON("/tms35/dfp/appProList", "", function(data){
				grid.renderPage({list: data.row});
				
				data.appList.unshift({APP_NAME:'--请选择--',APP_ID:''});
				grid.table.rowForm.getItem('APP_ID').component.reload(data.appList,{text:'APP_NAME',value:'APP_ID'});
				grid.qform.getItem('APP_ID').component.reload(data.appList,{text:'APP_NAME',value:'APP_ID'});
				
				appProList = data.row;
				proList = data.proList;
			});
		},
		isDirty: function(){
			return false;//appPro_isDirty;
		},
		save: function(){
			//alert(1);
		}
	};

	function check_app_data(data){
		var app_id = data.APP_ID;
		var prop_id = data.PROP_ID;
		var weight = data.WEIGHT;
		var storecolumn = data.STORECOLUMN;
		
		if (app_id == undefined || Trim(app_id) == "") {
			alert('渠道名称不能为空');
			return false;
		}
		if (prop_id == undefined || Trim(prop_id) == "") {
			alert('属性名称不能为空');
			return false;
		}
		if (weight == undefined || Trim(weight) == "") {
			alert('权重不能为空');
			return false;
		}else {
			if (!(parseFloat(weight) >= 0)) {
				alert('权重必须为正数');
				return false;
			}
		}
		
//		if (storecolumn == undefined || Trim(storecolumn) == "") {
//			alert('存储字段不能为空');
//			return false;
//		}
		
		return true;
	}

})();
