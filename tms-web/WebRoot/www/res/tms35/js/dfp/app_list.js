
var model = window.model ||{};

(function(){
	var grid = null;
	var channleList ;
	var app_isDirty = false;
	model.dfpApp = {
		init: function(){
			$('#appGridDiv').empty();

			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				toolbar:[
					{id:"app-btn-find", icon:"icon-tb-find", text:"查询", action:'toggleQform'},
					{id:"app-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"app-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"app-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"app-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"export-btn-edit", icon:"icon-tb-edit", text:"生成脚本", enable:'oneRowSelected'}
				],
				qform:{display:false,items:[
	              {name:'APP_ID', label:'渠道名称', type:'selector',title:'--请选择--', ds:{type:'remote', url:'/tms35/dfp/appList', parser:{key:'row', text:'APP_NAME', value:'APP_ID'}}}
       			 ], action:'localQuery'},
				table:{
					sortType: 'local',
					columns:[
						{name:"采集方式代码", width: 15, dataIndex:'APP_ID'},
						{name:"采集方式名称", width: 15, dataIndex:'APP_NAME'},
						{name:"匹配阀值", width: 15, dataIndex:'THRESHOLD'},
						{name:"Cookie名称", width: 15, dataIndex:'COOKIENAME'},
						{name:"设备标识生成方式", width: 15, dataIndex:'TOKEN_TYPE',render:'tms.dfp.token_type'}
					],
					rowForm:{
		                items:[
	                       {label:"采集方式代码", name:'APP_ID', type:'text', required:true},
	                       {label:"采集方式名称", name:'APP_NAME', type:'text', required:true},
	                       {label:"最多设备数", name:'MAX_DEVICES', type:'text', required:true,type:'hidden' ,value:'10'},
	                       {label:"匹配阀值", name:'THRESHOLD', type:'text', required:true},
	                       {label:"Cookie名称", name:'COOKIENAME', type:'text', required:true},
	                       {label:"设备标识生成方式", name: 'IS_TOKEN',type:'selector',title:'--请选择--',items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.dfp.token_type'}, required:true}
	                   ],
	                   btns:[
	                       {text:'确定', id:'app-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'app-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#appGridDiv'));

			grid.table.onRowFormShow(function(isAdd){
				if(isAdd == undefined || isAdd){
					grid.table.rowForm.getItem('APP_ID').enable();
//					grid.table.rowForm.getItem('CHANNELNAME').disable();
				}else{
					grid.table.rowForm.getItem('APP_ID').disable();	
//					grid.table.rowForm.getItem('CHANNELNAME').enable();
//					grid.table.rowForm.getItem('CHANNELNAME').viewMode();	
				}	
			});
			
//			grid.table.rowForm.getItem('APP_ID').component.onChange(function(v){
//				
//				var _app_name = v.text;
//				
//				grid.table.rowForm.getItem('CHANNELNAME').val(_app_name);
//			});
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

					jcl.postJSON('/tms35/dfp/appSave', url_para, function(data1){
						grid.table.updateEditingRow(data1.row);
						alert("保存成功");
					});
					return false;
				}else{
					return false;
				}
		    });

			// 查看按钮事件
			grid.toolbar.onClick('#app-btn-view', function(){
				grid.table.rowForm.jqDom.find("#app-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#app-btn-add', function(){
				grid.table.rowForm.jqDom.find("#app-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#app-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#app-row-edit-sure").show();
			});

			// 生成JS按钮事件
			grid.toolbar.onClick('#export-btn-edit', function(){
				var rowData = grid.table.selectedOneRow();
				switch(rowData.APP_ID){
					case "2":
					case "4":
					case "6":
					case "8": window.open(jcl.env.contextPath+"/s/file/dfp_js.zip");break;
					case "1": window.open(jcl.env.contextPath+"/s/file/dfp_android.zip");break;
					case "3": window.open(jcl.env.contextPath+"/s/file/dfp_ios.zip");break;
					case "5": 
					case "7": window.open(jcl.env.contextPath+"/s/file/dfp_pc.zip");break;
				}
//				window.open(jcl.env.contextPath+"/tms35/dfp/exportJs?app_id="+rowData.APP_ID);
				var params = "app_id="+rowData.APP_ID+"&app_name="+encodeURIComponent(rowData.APP_NAME)
				jcl.postJSON('/tms35/dfp/exportLog', params, function(data){
				});
			});
			// 获取设备信息
			grid.toolbar.onClick('#app-btn-get', function(){
				var dfp = getDfpValues('t');
				dfp(); //开始采集
			});

			grid.toolbar.onClick('#app-btn-del', function(){
				if(confirm('确定删除？')){
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode;

					jcl.postJSON('/tms35/dfp/appSave', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			

			// 加载数据
			this.load();

		},
		load: function(){
			
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			// 初始化查询列表
			jcl.postJSON("/tms35/dfp/appList", "", function(data){
				grid.renderPage({list: data.row});
//				channleList = data.channleList;
//				channleList.unshift({CHANNELNAME:'--请选择--',CHANNELID:''});
//				grid.table.rowForm.getItem('APP_ID').component.reload(channleList,{text:'CHANNELNAME',value:'CHANNELID'});
//				grid.qform.getItem('APP_ID').component.reload(channleList,{text:'CHANNELNAME',value:'CHANNELID'});
			});
		},
		isDirty: function(){
			return false;//app_isDirty;
		},
		save: function(){
			//alert(1);
		}
	};

	function check_app_data(data){
		var APP_ID = data.APP_ID;
		var app_name = data.APP_NAME;
		var max_devices = data.MAX_DEVICES;
		var threshold = data.THRESHOLD;
		var cookiename = data.COOKIENAME;
		
		if(APP_ID == undefined || Trim(APP_ID)==""){
			alert("渠道名称不能为空");
			return false;
		}
		
		/*if (max_devices == undefined || Trim(max_devices) == "") {
			alert('最多设备数不能为空');
			return false;
		}
		if (!IsInt(max_devices, "+", 0) || parseInt(max_devices) > 100 || parseInt(max_devices) < 1) {
			alert('最多设备数不是1~100之间的整数');
			return false;
		}*/

		if (threshold == undefined || Trim(threshold) == "") {
			alert("匹配阀值不能为空");
			return false;
		}
		else {
			if (!IsNumber(threshold, '+', '2') || parseFloat(threshold) > 1 || parseFloat(threshold) <= 0) {
				alert('匹配阀值不是0-1之间的两位小数');
				return false;
			}
		}
		
		if(cookiename == undefined || Trim(cookiename)==""){
			alert("Cookie名称不能为空");
			return false;
		}else{
			if(!checkeLength(cookiename,50)){
				alert('Cookie名称不能超过50个字符');
				return false;
			}
			if (!checkSpecialCharacter(cookiename, "1")) {
				alert('Cookie名称只能以字母开头，包含字母、数字和下划线');
				return false;
			}
		}
		
		return true;
	}

})();
