
var model = window.model ||{};

(function(){
	var grid = null;
	var pro_isDirty = false;
	model.dfpPro = {
		init: function(){
			$('#proGridDiv').empty();

			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				toolbar:[
					{id:"pro-btn-find", icon:"icon-tb-find", text:"查询", action:'toggleQform'},
					{id:"pro-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"pro-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"pro-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"pro-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'}
				],
				qform:{display:false,items:[
	              {name:'PROP_NAME', label:'属性名称'}
       			 ], action:'localQuery'},
				table:{
					sortType: 'local',
					columns:[
						//{name:"属性代码", width: 15, dataIndex:'PROP_CODE'},
						{name:"属性ID", width: 5, dataIndex:'PROP_ID'},
						{name:"属性名称", width: 15, dataIndex:'PROP_NAME'},
						{name:"属性说明", width: 15, dataIndex:'PROP_COMMENT'}
					],
					rowForm:{
		                items:[
	                       {label:"属性ID", name: 'PROP_ID', type:'text', required:true},
	                       {label:"属性名称", name: 'PROP_NAME', type:'text', required:true},
	                       {label:"属性说明", name: 'PROP_COMMENT', type:'text'}
	                   ],
	                   btns:[
	                       {text:'确定', id:'pro-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'pro-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#proGridDiv'));

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

					jcl.postJSON('/tms35/dfp/proSave', url_para, function(data1){
						grid.table.updateEditingRow(data1.row);
						alert("保存成功");
					});
					return false;
				}else{
					return false;
				}
		    });

			// 查看按钮事件
			grid.toolbar.onClick('#pro-btn-view', function(){
				grid.table.rowForm.jqDom.find("#pro-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#pro-btn-add', function(){
				grid.table.rowForm.jqDom.find("#pro-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#pro-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#pro-row-edit-sure").show();
			});

			grid.toolbar.onClick('#pro-btn-del', function(){
				if(confirm('确定删除？')){
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode;

					jcl.postJSON('/tms35/dfp/proSave', url_para, function(callback){
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
			jcl.postJSON("/tms35/dfp/proList", "", function(data){
				grid.renderPage({list: data.row});
			});
		},
		isDirty: function(){
			return false;//pro_isDirty;
		},
		save: function(){
			//alert(1);
		}
	};

	function check_app_data(data){
		var prop_name = data.PROP_NAME;
		var prop_comment = data.PROP_COMMENT;
		
		if(prop_name == undefined || Trim(prop_name)==""){
			alert("属性名称不能为空");
			return false;
		}else{
			if(!checkeLength(prop_name,60)){
				alert('属性名称不能超过60个字符');
				return false;
			}
			if (!/^[a-zA-Z][\w\s]*$/.test(prop_name)) {
				alert('属性名称只能以字母开头，包含字母、数字、空格和下划线');
				return false;
			}
		}
		
		if (!checkeLength(prop_comment, 500)) {
			alert('属性说明不能超过500个字符');
			return;
		}
		if (!checkeSpecialCode(prop_comment)) {
			alert('属性说明不能输入特殊字符');
			return false;
		}
		return true;
	}

})();
