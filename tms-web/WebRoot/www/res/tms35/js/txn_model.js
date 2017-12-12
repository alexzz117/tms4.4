var model = window.model || {};
(function(){
	var opts = {
		txnId : null,
		page : null,
		datatype : {
			type:['','','','',''],
			length:[]
		}
	};
	var tranModel = null, tranModelRef = null;
	model.tranModel = {
		init : function(_txnId, _page){
			opts = $.extend(opts, {txnId:_txnId, page:_page});
			opts = $.extend(opts, {txnName:this.getTxnName()});
			$('#grid-txn_mdl').empty();
			this.load(_txnId);
		},
		load : function(_txnId){
			opts = $.extend(opts, {txnId:_txnId, txnName:this.getTxnName()});
			jcl.postJSON('/tms35/tranmdl/query', 'tab_name='+_txnId, function(data){//临时库
				var tranModelList = data.txnfds;//交易模型
				var refTab = data.txn_ref_tab;//引用表
				var refFd = data.txn_ref_fds;//引用字段
				var enableStoreFd = data.enableStoreFd;//可用存储字段
				var allStoreFd = data.allStoreFd;//所有的存储字段
				jcl.postJSON('/tms35/tranmdl/queryOf', 'tab_name='+_txnId, function(data){//正式库
					var codeList = data.sourceType;//关联代码集
					var canRefTab = data.canRefTable;//可引用表
					var canRefTabFd = data.refTblFd;//可引用表中的字段
					var func = data.func;//函数
					var funcParam = data.funcParam;//函数参数
					var tranModelOpts = $.extend({}, opts, {modellist:tranModelList, code:codeList, enableStoreFd:enableStoreFd, allStoreFd:allStoreFd, func:func, funcparam:funcParam});
					var tranModelRefOpts = $.extend({}, opts, {reftab:refTab, reffd:refFd, canreftab:canRefTab, canreftabfd:canRefTabFd, enableStoreFd:enableStoreFd, allStoreFd:allStoreFd});
					if(tranModel instanceof model.tranModel.tranModelGrid){
						$.extend(tranModel.opts, tranModelOpts);
						tranModel.reload();
					}else{
						tranModel = new model.tranModel.tranModelGrid(tranModelOpts, $('#grid-txn_mdl'));
					}
					if(tranModelRef instanceof model.tranModel.tranModelRefGrid){
						$.extend(tranModelRef.opts, tranModelRefOpts);
						tranModelRef.reload();
					}else{
						tranModelRef = new model.tranModel.tranModelRefGrid(tranModelRefOpts, $('#grid-txn_mdl'));
					}
				});
			});
		},
		isDirty : function(){

		},
		getTxnName : function(){
			if(opts.page){
				return opts.page.tree.getNode(opts.page.tree.select()[0]).text;
			}else{
				return '';
			}
		}
	};
})();

/**
 * 交易模型
 *
 */
model.tranModel.tranModelGrid = function(options, container){
	var _self = this;
	//数据类型归类
	var _dataTypeClassify = [
		{recap:'long', type:['long', 'time', 'datetime']},
		{recap:'decimal', type:['double', 'money']},
		{recap:'string', type:['string', 'devid', 'ip', 'userid', 'acc', 'code']}
	];

	this.opts = $.extend({
		txnId:null,
		title:null,
		modellist:null,
		code:null,
		enableStoreFd:null,
		allStoreFd:null,
		func:null,
		funcparam:null
	}, options);

	this.jqDom = (function(opts){
		_self.parentContainer = container ? container : $('body');
		var gridData = _formatStuffGridData();
		_self.groups = gridData.group;
		_self.grid = _buildTranModelGrid(gridData.list);
		var jqDom  = _self.grid.jqDom;
		_buildGroupByGrid();
		_bindEvent();
		return jqDom;
	})(this.opts);
	
	this.reload = function(){
		var gridData = _formatStuffGridData();
		_self.groups = gridData.group;
		_self.grid.renderPage({list:gridData.list});
		_buildGroupByGrid();
	};

	/**
	 * 重新构建交易模型Grid
	 */
	function _reBuildTranModelGrid(){
		_self.grid.renderPage(_self.grid.table.page);
		_buildGroupByGrid();
	}

	/**
	 * 组织用于填充Gird的数据
	 */
	function _formatStuffGridData(){
		var opts = _self.opts, _list = opts.modellist,
		gridList = [], groups = [], size = 0;
		if(_list){
			try {
				$.each(_list, function(i, map){
					$.each(map, function(key, valList){
						var keys = key.split('____');
						if(keys && keys.length == 2){
							var tabName = keys[0];
							var groupTitle = keys[1];
							groups.push({tranName: tabName, title:groupTitle, between:[size, size+=valList.length]});
							$.each(valList, function(v, tran){
								gridList.push(tran);
							});
						}else{
							throw '数据格式错误！';
						}
					});
				});
			}catch(e){
				jcl.msg.info(e);
				return false;
			}
		}
		var gridData = {list:gridList, group:groups};
		return gridData;
	};

	/**
	 * 构建交易模型Grid界面
	 * @param pageList  填充数据
	 */
	function _buildTranModelGrid(pageList){
		var opts = _self.opts;
		var code = opts.code;
		var tranModelGrid = new jcl.ui.Grid({
			title: opts.title ? opts.title : '交易模型',
			marginTop: 0,
			toolbar:[
				{id:"btn-tm-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
				{id:"btn-tm-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
				{id:"btn-tm-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
				{id:"btn-tm-view", icon:"icon-tb-find", text:"查看", enable:'oneRowSelected', action:'viewRow'}
			],
			table:{
				columns:[
					{name:'属性名称', width:40, dataIndex:'NAME'},
					{name:'属性代码', width:40, dataIndex:'REF_NAME'},
					{name:'数据来源', width:40, dataIndex:'SRC_ID'},
					{name:'类型', width:40, dataIndex:'TYPE', render:'tms.model.datatype'},
					{name:'存储字段', width:40, dataIndex:'FD_NAME'},
					{name:'关联代码集', width:40, dataIndex:'CODE', render:function(c){
						var cName = c;
						$.each(code, function(i, category){
							if(category['CATEGORY_ID'] == c){
								cName = category['CATEGORY_NAME'];
							}
						});
						return cName;
					}},
					{name:'默认值', width:40, dataIndex:'SRC_DEFAULT'},
					{name:'处理函数', width:40, dataIndex:'GENESISRUL'}
				],
				rowForm:{
					items:[
						{label:'交易名称', name:'TAB_NAME', type:'hidden'},
						{label:'属性名称', name:'NAME', type:'text', required:true},
						{label:'属性代码', name:'REF_NAME', type:'text', required:true},
						{label:'数据来源', name:'SRC_ID', type:'text', required:true},
						{label:'类型', name:'TYPE', type:'selector', title:'--请选择--', ds:{type:'code', category:'tms.model.datatype'}, required:true},
						{label:'存储字段', name:'FD_NAME', type:'selector', required:true, title:'--请选择--'},
						{label:'关联代码集', name:'CODE', type:'selector', required:true, items:_buildSourceTypeSelector()},
						{label:'默认值', name:'SRC_DEFAULT', type:'text'},
						{label:'处理函数', name: 'GENESISRUL', type:'model.ui.formItem.HandlerFunction', title:'---请选择---', dataTypeFilter:true, items:[{text:'---请选择---', value:''}], ds:{type:'remote', url:'/tms35/tranmdl/queryFunWithParam', parser:{
							key:'funWithParam', text:'FUNC_NAME', value:'FUNC_CODE', type:'FUNC_TYPE', desc:'FUNC_DESC', param:'PARAMS', param_value:{name:'PARAM_NAME', type:'PARAM_TYPE', order:'PARAM_ORDERBY', desc:'FUNCPARAMDESC'}
						}}}
					],
					btns:[
	                    {text:'确定', id:'row-edit-sure', action:'submit'},
	                    {text:'取消', id:'row-edit-cancel', action: 'cancel'}
	                ]
				},
            	pagebar:false
			}
		}, _self.parentContainer);
		tranModelGrid.renderPage({list:pageList});
		return tranModelGrid;
	};

	/**
	 * 把Grid构建成分组
	 *
	 */
	function _buildGroupByGrid(){
		var groups = _self.groups;
		var grid = _self.grid;
		grid.table.jqDom.find('div.txn-attr-group').remove();
		if(groups && groups.length > 0){
			$.each(groups, function(i,group){
				var title = group.title,
				between = group.between, row;
				var start = between[0];//开始row
				var end = between[1];//结束row[start, end)
				if((start == end) && (i+1 < groups.length)){//不是当前交易，且交易中没有字段则不显示
					return true;
				}
				var temp = '<div class="txn-attr-group">';
				temp +='<span class="group-ec group-expand"></span>';
				temp +='<span class="group-title">'+title+'</span></div>';
				row = grid.table.jqDom.find('.row:eq('+start+')');
				if(row.length > 0){//有当前行
					row.before(temp);
				}else{//没有的话，插入到.table-rows最后
					$(temp).appendTo(grid.table.jqDom.find('div.table-rows'));
				}
			});
		}
		$(window).triggerHandler('resizes');
		_rowCallBack();
	};

	function _addRowData(rowData){
		var groups = _self.groups;
		var list = _self.grid.table.page.list;
		var lastGroup = groups[groups.length-1];
		list.splice(lastGroup.between[1], 0, rowData);
		lastGroup.between[1]++;
		_reBuildTranModelGrid();
		_self.grid.table.jqDom.find('div.row:eq('+(list.length-1)+')').trigger('click');
	}

	/**
	 * 删除所选行数据
	 */
	function _deleteSelectedRowsData(){
		var rows = _self.grid.selectedRows();
		var groups = _self.groups;
		var lastGroup = groups[groups.length-1];
		lastGroup.between[1]-=rows.length;
		_self.grid.table.deleteSelectedRow();
		_reBuildTranModelGrid();
	}
	
	/**
	 * 根据数据类型过滤当前可用存储字段
	 * @param datatype 数据类型
	 * @param effect   是否为有效存储字段
	 */
	function _getCanUseStorageFdByDataType(datatype, effect){
		var opts = _self.opts;
		var sfdItems = [];
		var enableStoreFds = $.extend([], opts.enableStoreFd ? opts.enableStoreFd : []);
		if(!effect){
			var srows = _self.grid.table.selectedRows();
			if(srows && srows.length > 0){
				var srow = srows[0];
				if(srow && srow['FD_NAME']){
					var allStoreFds = opts.allStoreFd;
					var storeFd = _getStorageFdByfdName(allStoreFds, srow['FD_NAME']);
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
	 * 根据数据类型获取可用存储字段
	 * @param	datatype		数据类型
	 * @param	enableStoreFds	可用存储字段集合
	 */
	function _getEnableStorageFdByDataType(datatype, enableStoreFds){
		var sfdItems = [];
		if(datatype && enableStoreFds){
			$.each(enableStoreFds, function(i, fd){
				$.each(_dataTypeClassify, function(d, dt){
					if($.inArray(datatype, dt.type)!=-1){
						if(dt.recap == 'long' || dt.recap == 'decimal' ){
							if(datatype != 'double'&&datatype != 'long'){
								if(fd['TYPE'] == datatype){
									sfdItems.push(fd);
								}
							}
							if(fd['TYPE'] == 'double'||fd['TYPE'] == 'long'){
								sfdItems.push(fd);
							}
											
						}else if(dt.recap == 'string'){
							if(datatype != 'string'){
								if(fd['TYPE'] == datatype){
									sfdItems.push(fd);
								}
							}
						}
						
						if(fd['TYPE'] == 'string'){
							sfdItems.push(fd);
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
	 * 重新加载可用存储字段
	 * @param ds 加载配置对象
	 */
	function _reloadEnableStorageFd(tabName){
		var opts = _self.opts;
		var tab_name = tabName ? tabName : opts.txnId;
		_self.storage = [];
		jcl.postJSON('/tms35/tranmdl/queryAvailableStoreFd', 'tab_name='+tab_name, function(data){
			if(data && data.enableStoreFd){
				_self.opts.enableStoreFd = data.enableStoreFd;
			}
		});
	};
	
	/**
	 * 构建关联代码集下拉列表
	 */
	function _buildSourceTypeSelector(){
		var code = _self.opts.code;
		var _codeItems = [];
		$.each(code, function(i, category){
			if(i == 0){
				_codeItems.push({text:'---请选择---', value:''});
			}
			_codeItems.push({text:category['CATEGORY_NAME'], value:category['CATEGORY_ID']});
		});
		return _codeItems;
	};
	
	/**
	 * 根据数据类型重新构建存储字段
	 * @param datatype 字段类型
	 * @param effect   是否获取有效值
	 */
	function _reBuildStorageFd(datatype, effect){
		var fdNameItem = _self.grid.table.rowForm.getItem('FD_NAME'),
		fdNameVal = '';
		var srows = _self.grid.table.selectedRows();
		if(srows && srows.length > 0){
			var srow = srows[0];
			fdNameVal = srow['FD_NAME'];
		}
		fdNameItem.component.reload(_getCanUseStorageFdByDataType(datatype, effect), {text:'FD_NAME', value:'FD_NAME'});
		fdNameItem.val(fdNameVal);
	}

	/**
	 * 控制关联代码集数据项
	 * @param datatype 		字段类型
	 * @param category 		关联代码
	 * @param src_default	默认值
	 */
	function _reBuildAssociatedCode(datatype, category, src_default){
		if(datatype && datatype.toUpperCase() == 'CODE'){//代码类型
			_self.grid.table.rowForm.getItem('CODE').enable();
			if(_self.grid.table.rowForm.getItem('SRC_DEFAULT').opts.type != 'selector'){
				_self.grid.table.rowForm.setItem({type:'selector', title:'---请选择---', items:[{text:'---请选择---', value:''}]}, 'SRC_DEFAULT');
			}
			var srcDefault = _self.grid.table.rowForm.getItem('SRC_DEFAULT');
			var items = [{text:'---请选择---', value:''}];
			if(category){
				var codes = jcl.code.getCodes(category);
				if(codes){
					for(var i in codes){
						items.push({text:codes[i], value:i});
					}
				}
			}
			srcDefault.component.reload(items);
			srcDefault.val(src_default);
		}else{
			_self.grid.table.rowForm.getItem('CODE').val('');
			_self.grid.table.rowForm.getItem('CODE').disable();
			if(_self.grid.table.rowForm.getItem('SRC_DEFAULT').opts.type != 'text'){
				_self.grid.table.rowForm.setItem({type:'text'}, 'SRC_DEFAULT');
			}
		}
	}

	/**
	 * 控制处理函数项
	 * @param datatype 字段类型
	 */
	function _reBuildHandler(datatype){
		//$.each(_dataTypeClassify, function(d, dt){
 		//	if($.inArray(datatype, dt.type)!=-1){
 				var handlerFunc = _self.grid.table.rowForm.getItem('GENESISRUL');
				//handlerFunc.reFilter(dt.recap);
				handlerFunc.reFilter(datatype);
				if(handlerFunc.handlerItems.length == 0){
					handlerFunc.disable();
				}else{
					handlerFunc.enable();
				}
 		//	}
 		//});
	}
	
	/**
	 * 恢复存储字段选项
	 * @param datatype 字段类型
	 */
	function _restoreStorageFd(datatype){
		var srows = _self.grid.table.selectedRows();
		if(srows && srows.length > 0){
			var srow = srows[0];
			if(srow['TYPE'] == datatype){
				_self.grid.table.rowForm.getItem('FD_NAME').val(srow['FD_NAME']);
			}
		}
	}
	
	function _loadForRowFormShow(isAdd){
		var fdNameItem = _self.grid.table.rowForm.getItem('FD_NAME');//存储字段
		var fdNameValue = null, datatype = null, category = null, src_default = null, effect = false;
		if(isAdd){
			effect = true;
		}else{
			var srow = _self.grid.table.selectedOneRow();
			fdNameValue = srow['FD_NAME'];
			datatype = srow['TYPE'];
			category = srow['CODE'];
			src_default = srow['SRC_DEFAULT'];
		}
		fdNameItem.component.reload(_getCanUseStorageFdByDataType(datatype, effect), {text:'FD_NAME', value:'FD_NAME'});
		fdNameItem.val(fdNameValue);
		_reBuildAssociatedCode(datatype, category, src_default);
		_reBuildHandler(datatype);
	}
	
	function _rowCallBack(rowIndex, rowDom){
		if(rowIndex != null && rowIndex >= 0){
			var row = $(rowDom ? rowDom : _self.grid.table.jqDom.find('div.row:eq('+rowIndex+')'));
			var rowData = $.extend({}, _self.grid.table.page.list[rowIndex]);
			var type = rowData['TYPE'];
			if(type.toUpperCase() == 'CODE'){
				var src_default = rowData['SRC_DEFAULT'];
				if(src_default){
					var src_default_dom = row.find('td:eq(7)').find('div');
					var src_default_value = jcl.code.get(rowData['CODE'], src_default);
					src_default_dom.attr('title', src_default_value);
					src_default_dom.text(src_default_value);
				}
			}
			var func = rowData['GENESISRUL'];//处理函数
			if(func){
				var func_dom = row.find('td:eq(8)').find('div');
				var func_value = func.replace(/\?,?/, '');
				func_dom.attr('title', func_value);
				func_dom.text(func_value);
			}
		}else{
			var rows = _self.grid.table.jqDom.find('div.row');
			$.each(rows, function(r, rowDom){
				_rowCallBack(r, rowDom);
			});
		}
	}

	function resetEffectivenessBtn() {
		var opts = _self.opts;
		var srows = _self.grid.table.selectedRows();
		if(srows.length == 0){
			_self.grid.toolbar.enable($('#btn-tm-add'));
		}else if(srows.length == 1){
			var srow = srows[0];
			if(srow['TAB_NAME'] == opts.txnId){//当前交易
				if(srow['IS_SYS'] == '1'){//系统字段，不可改动
					_self.grid.toolbar.disable($('#btn-tm-edit'));
					_self.grid.toolbar.disable($('#btn-tm-del'));
					_self.grid.toolbar.enable($('#btn-tm-view'));
				}else{
					_self.grid.toolbar.enable($('#btn-tm-edit'));
					_self.grid.toolbar.enable($('#btn-tm-del'));
					_self.grid.toolbar.enable($('#btn-tm-view'));
				}
			}else{
				_self.grid.toolbar.disable($('#btn-tm-edit'));
				_self.grid.toolbar.disable($('#btn-tm-del'));
				_self.grid.toolbar.enable($('#btn-tm-view'));
			}
			_self.grid.toolbar.enable($('#btn-tm-add'));
		}else if(srows.length > 1){
			var _delete = true;
			$.each(srows, function(i, srow){
				if(srow['TAB_NAME'] != opts.txnId){//不是本层交易，不可删除
					_delete = false;
				}
				if(srow['TAB_NAME'] == opts.txnId && srow['IS_SYS'] == '1'){//本层交易，且为系统字段，不可删除
					_delete = false;
				}
			});
			if(_delete){
				_self.grid.toolbar.enable($('#btn-tm-del'));
			}else{
				_self.grid.toolbar.disable($('#btn-tm-del'));
			}
			_self.grid.toolbar.enable($('#btn-tm-add'));
			_self.grid.toolbar.disable($('#btn-tm-edit'));
			_self.grid.toolbar.disable($('#btn-tm-view'));
		}

		if(dualAudit){ // readonly mode
			_self.grid.toolbar.disable($('#btn-tm-add'), false);
			_self.grid.toolbar.disable($('#btn-tm-edit'), false);
			_self.grid.toolbar.disable($('#btn-tm-del'), false);
		}
	};

	/**
	 * 绑定事件
	 */
	function _bindEvent(){
		var opts = _self.opts;
		_self.grid.table.jqDom.on('click', 'span.group-ec', function(){
	        var ec = $(this);
	        if(ec.hasClass('group-expand')){
	            ec.addClass('group-collapse').removeClass('group-expand');
	            ec.parent().nextUntil('div.txn-attr-group', 'div.row').hide();
	        }else{
	            $(this).addClass('group-expand').removeClass('group-collapse');
	             ec.parent().nextUntil('div.txn-attr-group', 'div.row').show();
	        }
	        return false;
	    });

		resetEffectivenessBtn();

		_self.grid.table.onRowSelectChange(function(){
			resetEffectivenessBtn();
		});

		_self.grid.toolbar.onClick('#btn-tm-add', function(){
			_self.grid.table.rowForm.getItem('TAB_NAME').val(opts.txnId);
			_self.grid.table.rowForm.jqDom.find('#row-edit-sure').show();
		});
		_self.grid.toolbar.onClick('#btn-tm-edit', function(){
			_self.grid.table.rowForm.jqDom.find('#row-edit-sure').show();
		});
		_self.grid.toolbar.onClick('#btn-tm-view', function(){
			_self.grid.table.rowForm.jqDom.find('#row-edit-sure').hide();
		});
		_self.grid.table.onRowFormSubmit(function(rowFormJqDom, rowData, isAdd){
			if(_checkRowFormData(rowData, isAdd)){
				var row = rowData;
				if(!isAdd){
					var oldrow = _self.grid.table.selectedOneRow();
					if(oldrow['TYPE'] != rowData['TYPE'] && (oldrow['LINK'] && oldrow['LINK'].length > 0)){
						if(confirm('该字段已在自定义查询中引用，确认修改？')){
							oldrow['LINK'] = '';
						}else{
							return false;
						}
					}
					row = $.extend({}, oldrow, rowData);
					$.extend(row, {old:oldrow});
				}
				var json_data = isAdd ? JSON.stringify({'add':[row]}):JSON.stringify({'mod':[row]});
				var url_para = "postData=" + encodeURIComponent(json_data);
				jcl.postJSON('/tms35/tranmdl/saveModel', url_para, function(data){
					if(isAdd){
						_addRowData(data.row);
					}else{
						_self.grid.table.updateEditingRow(data.row);
						var rowIndex = _self.grid.table.jqDom.find('div.row').index(_self.grid.table.jqDom.find('div.row-bg-selected'));
						_rowCallBack(rowIndex);
					}
					_reloadEnableStorageFd(opts.txnId);//更新可用存储字段
					jcl.msg.info('保存成功');
				});
			}
			return false;
	    });
	    _self.grid.toolbar.onClick('#btn-tm-del', function(){
	    	if(confirm('确定删除？')){
	    		var rows = _self.grid.selectedRows();
				var json_data = JSON.stringify({'del':rows});
				var url_para = "postData=" + json_data;
				jcl.postJSON('/tms35/tranmdl/saveModel', url_para, function(callback){
					_deleteSelectedRowsData();
					_reloadEnableStorageFd(opts.txnId);//更新可用存储字段
					jcl.msg.info("删除成功");
				});
			}
			return false;
	    });
		_self.grid.table.rowForm.getItem('TYPE').component.onChange(function(item){
			var datatype = item.value;//数据类型
			_reBuildStorageFd(datatype, false);//存储字段
			_reBuildAssociatedCode(datatype, _self.grid.table.rowForm.getItem('CODE').val());//关联代码集
			_restoreStorageFd(datatype);
			_reBuildHandler(datatype);//处理函数
		});
		_self.grid.table.rowForm.getItem('CODE').component.onChange(function(item){
			var category = item.value;//关联代码集
			_reBuildAssociatedCode('code', category);
		});
		_self.grid.table.onRowFormShow(function(isAdd){
			
			_reloadEnableStorageFd(opts.txnId);//更新可用存储字段
			
			_loadForRowFormShow(isAdd);
			if(isAdd){
				_self.grid.table.jqDom.find('div.table-rows').find('div.txn-attr-group:last').after(_self.grid.table.rowFormDiv);
				_self.grid.table.rowSelectedScroll(_self.grid.table.rowFormDiv);
			}
		});
	};

	 /**
	  * 校验form中数据是否合法
	  * @param rowData  form表单数据
	  * @param isAdd    是否新增
	  */
	 function _checkRowFormData(rowData, isAdd){
	 	var dataCheck = [
		 	{label:'属性名称', name:'NAME', required:true, length:50, special:'chinese'},
		 	{label:'属性代码', name:'REF_NAME', required:true, length:50, special:'alphabet'},
		 	{label:'数据来源', name:'SRC_ID', required:true, length:20, special:'alphabet'},
		 	{label:'类型', name:'TYPE', required:true, length:20, special:'alphabet'},
		 	{label:'存储字段', name:'FD_NAME', required:true, length:50, special:'alphabet'},
		 	{label:'关联代码集', name:'CODE', length:50, special:'general'},
		 	{label:'默认值', name:'SRC_DEFAULT', length:50, special:'general'},
		 	{label:'处理函数参数', name:'GENESISRUL', length:20}
		];
	 	var //_name = rowData.NAME,//属性名称
	 	//_ref_name = rowData.REF_NAME,//属性代码
	 	//src_id = rowData.SRC_ID,//数据来源
	 	_type = rowData.TYPE,//数据类型
	 	_fd_name = rowData.FD_NAME,//存储字段
	 	//_code = rowData.CODE,//关联代码集
	 	_src_default = rowData.SRC_DEFAULT,//默认值
	 	_genesisrul = rowData.GENESISRUL;//处理函数
	 	try{
	 		$.each(dataCheck, function(i, dc){
		 		if(dc.required){
		 			if(!IsEmpty(rowData[dc.name]) || !IsEmpty(Trim(rowData[dc.name]))){
		 				throw (dc.label + '不能为空');
		 			}
		 		}
		 		if(rowData[dc.name]){
					
					// 处理函数的参数校验需要特殊处理
					if (dc.name == 'GENESISRUL') {
						var param = _genesisrul.substring(_genesisrul.indexOf('(') + 1, _genesisrul.indexOf(')'));//默认参数是?,截取有效参数值
						var params = param.split(',');
						$.each(params,function(i,p_value){
							if (!checkeLength(p_value, dc.length)) {
								throw (dc.label + i + '不能超过' + dc.length + '个字符');
							}
						});
					}
					else {
						if (!checkeLength(rowData[dc.name], dc.length)) {
							throw (dc.label + '不能超过' + dc.length + '个字符');
						}
					}
					var error = _checkSpecial(dc, rowData[dc.name]);
					if(error){
						throw error;
					}
					if(dc.name == 'TYPE' && rowData[dc.name] == 'code'){//数据类型为代码类型时，关联代码集不能为空
						dataCheck[5]['required'] = true;
					}
		 		}
		 	});
	 		var storageFds = _getCanUseStorageFdByDataType(_type, isAdd);//可用存储字段
	 		var isStorage = false;
	 		$.each(storageFds, function(f, fd){
	 			if(fd['FD_NAME'] == _fd_name){
	 				isStorage = true;
	 				return false;
	 			}
	 		});
	 		if(!isStorage){
	 			throw '存储字段对应类型和数据类型不一致';
	 		}
	 		if(IsEmpty(_src_default)){//不为空,默认值处理
	 			$.each(_dataTypeClassify, function(d, dt){
		 			if($.inArray(_type, dt.type)!=-1){
		 				if((dt.recap == 'long' && !IsInt(_src_default, '+', 0))
		 					|| (dt.recap == 'decimal' && !IsFloat(_src_default, '+', 0))){
		 					throw '默认值类型和数据类型不一致';
		 				}
		 			}
		 		});
	 		}
	 		if(IsEmpty(_genesisrul)){//处理函数校验
	 			var param = _genesisrul.substring(_genesisrul.indexOf('(')+1, _genesisrul.indexOf(')'));//默认参数是?,截取有效参数值
	 			var params = param.split(',');
	 			if(params.length > 1){//有参函数
	 				var isFun = false, funcParams = [];
	 				var func = _genesisrul.substring(0, _genesisrul.indexOf('('));
	 				$.each(_self.opts.func, function(f, fun){
	 					if(fun['FUNC_CODE'] == func){
	 						$.each(_self.opts.funcparam, function(p, pm){
	 							if(fun['FUNC_ID'] == pm['FUNC_ID']){
	 								funcParams.push(pm);
	 							}
	 						});
	 						isFun = true;
	 						return false;
	 					}
	 				});
	 				if(!isFun){
	 					throw '处理函数不存在';
	 				}
	 				var validParams = param.substring(param.indexOf(',')+1, param.length).split(',');//有效参数,去除?的参数集合
	 				if(validParams.length != funcParams.length){
	 					throw '处理函数参数个数不正确！';
	 				}
	 				$.each(validParams, function(i, val){
	 					var pconf = funcParams[i];
	 					$.each(_dataTypeClassify, function(d, dt){
				 			if($.inArray(pconf['PARAM_TYPE'], dt.type)!=-1){
				 				if(IsEmpty(val)){
				 					if(dt.recap == 'long' || dt.recap == 'decimal'){
				 						if(dt.recap == 'long' && !IsInt(val, '+', 0)){
				 							throw '处理函数参数'+(i+1)+'只能填写非负整数';
				 						}
				 						if(dt.recap == 'decimal' && !IsFloat(val, '+', 0)){
				 							throw '处理函数参数'+(i+1)+'只能填写非负数';
				 						}
				 						if((val*1+'') != (val+'')){
				 							throw '处理函数参数'+(i+1)+'所填数据的格式不正确';//说明，数字是已0开头的
				 						}
				 						if(i > 0 && (val*1 <= validParams[i-1]*1)){//后面的值小于等于前面的值
				 							throw '处理函数参数'+(i+1)+'必须大于参数'+i;
				 						}
				 					}else if(dt.recap == 'string'){
					 					if(!checkeSpecialCode(val)){
					 						throw '处理函数参数'+(i+1)+'不能包含特殊字符';
					 					}else if(!(val.charAt(0) == '"' && val.charAt(val.length-1) == '"')){
					 						throw '处理函数参数为字符类型，请使用双引号包裹，如：["' + val + '"]';
					 					}
					 				}
				 				}else{
				 					throw '处理函数参数'+(i+1)+'不能为空';
				 				}
				 			}
				 		});
	 				});
	 			}
	 		}
	 	}catch(e){
	 		jcl.msg.info(e);
	 		return false;
	 	}
	 	return true;
	 }
	 
	 function _checkSpecial(dc, value){
		var error = '';
		if(dc.special){
			if(dc.special == 'alphabet'){//字母校验
				if(!checkSpecialCharacter(value, '1')){
					error = (dc.label + '只能以字母开头，包含字母、数字和下划线');
				}
			}else if(dc.special == 'chinese'){//汉字
				if(!checkSpecialCharacter(value, '2')){
					error = (dc.label + '只能包含汉字,字母,数字和下划线');
				}
			}else if(dc.special == 'expression'){//表达式
				if(!checkeCondSpecialCode(value)){
					error = (dc.label + '不能包含特殊字符');
				}
			}else if(dc.special == 'general'){//普通
				if(!checkeSpecialCode(value)){
					error = (dc.label + '不能包含特殊字符');
				}
			}
		}
		return error;
	}
};

/**
 * 交易模型引用
 *
 */
model.tranModel.tranModelRefGrid = function(options, container){
	//数据类型归类
	var _dataTypeClassify = [
		{recap:'long', type:['long', 'time', 'datetime']},
		{recap:'decimal', type:['double', 'money']},
		{recap:'string', type:['string', 'devid', 'ip', 'userid', 'acc', 'code']}
	];
	var _self = this;
	var _loadedCanRefTab = false;
	var _addRowIndex = -1;
	var _modGroupsIndex = -1;
	var _editingRefTabRowIndex = -1;
	var _isToggleForToolbar = -1;
	this.opts = $.extend({
		txnId:null,
		txnName:null,
		title:null,
		reftab:null,//引用表
		reffd:null,//引用字段
		canreftab:null,//可引用表
		enableStoreFd:null,//可用存储字段
		allStoreFd:null,//所有存储字段
		canreftabfd:null//可引用表中的字段
	}, options);

	this.jqDom = (function(opts){
		_self.parentContainer = container ? container : $('body');
		var gridData = _formatStuffGridData();
		_self.groups = gridData.group;
		_self.grid = _buildTranModelRefGrid(gridData.list);
		var jqDom  = _self.grid.jqDom;
		_buildRefTabRowForm(jqDom);
		_buildRefGroupByGrid();
		_initCanRefTabAndFd();
		_bindEvent();
		return jqDom;
	})(this.opts);
	
	this.reload = function(){
		var gridData = _formatStuffGridData();
		_self.groups = gridData.group;
		_self.grid.renderPage({list:gridData.list});
		_buildRefGroupByGrid();
		_initCanRefTabAndFd();
	};

	/**
	 * 构建交易模型引用Grid界面
	 *
	 */
	function _buildTranModelRefGrid(pageList){
		var opts = _self.opts;
		var tranModelRefGrid = new jcl.ui.Grid({
			title: opts.title ? opts.title : '交易模型引用',
			marginTop: 5,
			toolbar:[
				{id:"btn-reftab-add", icon:"icon-tb-add", text:"新建引用表"},
				{id:"btn-reftab-edit", icon:"icon-tb-edit", text:"编辑引用表"},
				{id:"btn-reftab-del", icon:"icon-tb-del", text:"删除引用表"},
				{id:"btn-reffd-add", icon:"icon-tb-add", text:"新建行字段", action:'addRow'},
				{id:"btn-reffd-edit", icon:"icon-tb-edit", text:"编辑行字段", action:'editRow'},
				{id:"btn-reffd-del", icon:"icon-tb-del", text:"删除行字段"},
				{id:"btn-ref-view", icon:"icon-tb-find", text:"查看"}
			],
			table:{
				columns:[
					{name:'属性名称', width:40, dataIndex:'REF_DESC'},
					{name:'属性代码', width:40, dataIndex:'REF_NAME'},
					{name:'数据来源', width:40, dataIndex:'REF_FD_NAME', render:function(v){
							var return_text = v;
							$.each(opts.canreftabfd, function(i,ref){
								if(ref['FD_NAME'] == v){
									return_text = ref['NAME'];
									return false;
								}
							});
							return return_text;
						}},
					{name:'条件', width:40, dataIndex:'SRC_COND_IN'},
					{name:'表达式', width:40, dataIndex:'SRC_EXPR_IN'},
					{name:'存储字段', width:40, dataIndex:'STORECOLUMN'},
					{name:'所属节点', width:40, dataIndex:'TAB_DESC'}
				],
				rowForm:{
					items:[
						{label:'引用ID', name:'REF_ID', type:'hidden'},
						{label:'所属交易', name:'TAB_NAME', type:'hidden'},
						{label:'引用表描述', name:'REF_TAB_DESC', type:'hidden'},
						{label:'数据来源', name:'REF_FD_NAME', type:'selector', required:true, title:'--请选择--'},//引用字段，引用表中的字段
						{label:'属性名称', name:'REF_DESC', type:'text', required:true},//引用描述
						{label:'属性代码', name:'REF_NAME', type:'text', required:true},//引用属性代码
						{label:'条件', name:'SRC_COND', type:'hidden'},
						{label:'表达式', name:'SRC_EXPR', type:'hidden'},
						{label:'条件', name:'SRC_COND_IN', type:'text'},
						{label:'表达式', name:'SRC_EXPR_IN', type:'text'},
						{label:'存储字段', name:'STORECOLUMN', type:'selector', title:'--请选择--'},
						{label:'所属节点', name:'TAB_DESC', type:'text'}
					],
					btns:[
	                    {text:'确定', id:'row-edit-sure', action:'submit'},
	                    {text:'取消', id:'row-edit-cancel', action: 'cancel'}
	                ]
				},
				ds:{},
            	pagebar:false
			}
		}, _self.parentContainer);
		tranModelRefGrid.renderPage({list:pageList});
		return tranModelRefGrid;
	};

	function _buildRefTabRowForm(jqDom){
		var htmlstr = '<div class="row-form box-b-r box-b-b">';
		htmlstr += '<div class="box-content-wrap-b"><div class="box-content-wrap-t"></div></div>';
		htmlstr += '</div>';
		_self.RefTabRowFormDiv = $(htmlstr).appendTo(jqDom);
		_self.RefTabRowForm = new jcl.ui.Form({
			items:[
				{label:'引用ID', name:'REF_ID', type:'hidden'},
				{label:'所属交易', name:'TAB_NAME', type:'hidden'},
				{label:'排序', name:'TXN_ORDER', type:'hidden'},
				{label:'引用表', name:'REF_TAB_NAME', type:'selector', required:true, title:'--请选择--'},
				{label:'引用描述', name:'REF_DESC', type:'text', required:true},
				{label:'引用字段', name:'SRC_EXPR', type:'selector', required:true, title:'--请选择--'},
				{label:'所属节点', name:'TAB_DESC', type:'text'}
			],
			btns:[
				{text:'确定', id:'ref-tab-edit-sure'},
	            {text:'取消', id:'ref-tab-edit-cancel'}
			]
		}, _self.RefTabRowFormDiv.find('div.box-content-wrap-t'));
		_self.RefTabRowFormDiv.hide();
	};

	/**
	 * 组织用于填充Gird的数据
	 *
	 */
	function _formatStuffGridData(){
		var opts = _self.opts,
		refTabList = opts.reftab,
		refFdList = opts.reffd,
		gridList = [], groups = [], size = 0;
		_self.refTabList = [], canShowTabs = [];
		if(refTabList){
			try{
				$.each(refTabList, function(i, txnTab){
					$.each(txnTab, function(key, refTabs){
						var keys = key.split('____');
						canShowTabs.push(keys[0]);//当前交易可以显示的交易名称
					});
				});
				$.each(refTabList, function(i, txnTab){
					$.each(txnTab, function(key, refTabs){
						var keys = key.split('____');
						$.each(refTabs, function(t, refTab){
							groups.push({tranName: keys[0], title:(keys[1] + ':[' + refTab.REF_DESC + ']'), between:[size]});//between[表位置,字段结束位置]
							$.each(refFdList, function(f, refFd){
								if(refTab.REF_ID == refFd.REF_ID && $.inArray(refFd.TAB_NAME, canShowTabs)!=-1){
									gridList.push(refFd);
									size++;
								}
							});
							_self.refTabList.push(refTab);
							groups[groups.length-1].between.push(size);
						});
					});
				});
			}catch(e){
				jcl.msg.info(e);
				return false;
			}
		}
		var gridData = {list:gridList, group:groups};
		return gridData;
	};

	/**
	 * 把Grid构建成分组
	 *
	 */
	function _buildRefGroupByGrid(){
		var groups = _self.groups;
		var grid = _self.grid;
		grid.table.jqDom.find('div.ref-model').remove();
		$.each(groups, function(i,group){
			var title = group.title,
			between = group.between, row;
			var temp = '<div class="txn-attr-group ref-model">';
				temp += '<span style="float:left;"><input type="checkbox" class="row-cb checkbox-group" /></span>';
				temp += '<span class="group-ec group-expand"></span>';
				temp += '<span class="group-title">'+title+'</span></div>';
			var start = between[0];//开始row
			//var end = between[1];//结束row[start, end)
			row = grid.table.jqDom.find('div.row:eq('+start+')');
			if(row.length > 0){//有当前行
				row.before(temp);
			}else{//没有的话，插入到.table-rows最后
				$(temp).appendTo(grid.table.jqDom.find('div.table-rows'));
			}
		});
		$(window).triggerHandler('resizes');
	};

	/**
	 * 重构Grid
	 */
	function _reBuildTranModelRefGrid(){
		_self.grid.table.renderPage(_self.grid.table.page);
		_buildRefGroupByGrid();
	}

	/**
	 * 插入分组(引用表)数据到最后面
	 * @param groupData  分组(引用表)数据
	 */
	function _addGroupDataForGroupsLast(groupData){
		var opts = _self.opts;
		_self.refTabList.push(groupData);
		var groups = _self.groups;
		var _lastGroup = groups[groups.length > 0 ? groups.length-1 : 0];
		var begin = _lastGroup ? _lastGroup.between[1] : 0;
		var group = {tranName:groupData.TAB_NAME, title:(opts.txnName+':['+groupData.REF_DESC + ']'), between:[begin, begin]};
		//groups.splice(groups.length, 0, group);
		groups.push(group);
		_modGroupsIndex = groups.length-1;
	}

	/**
	 * 修改分组(引用表)数据
	 * @param groupData 修改后的数据
	 */
	function _modGroupDataForRefTabList(groupData){
		var groupsIndex = _getSelectedGroupsIndex();
		if(groupsIndex.length == 1){
			var index = groupsIndex[0];
			_modGroupsIndex = index;
			_self.refTabList.splice(index, 1, groupData);//替换索引位置中的分组信息
			_self.groups[index].title = _self.opts.txnName + ':[' + groupData.REF_DESC + ']';
		}
	}

	/**
	 * 删除选中分组的数据
	 */
	function _deleteSelectedGroupsData(){
		var groups = _self.groups;
		var groupsIndex = _getSelectedGroupsIndex();
		//修改分组数据的范围值，删除分组索引后的所有分组进行范围调整
		$.each(groupsIndex, function(i, index){
			var delGroup = groups[index];
			var delRowsNum = delGroup.between[1]-delGroup.between[0];//分组中的行数据数量
			if(delRowsNum > 0){//分组中包含行数据，其后分组数据整体前移
				for(var g=(index+1);g<groups.length;g++){
					var group = groups[g];
					group.between[0]-=delRowsNum;
					group.between[1]-=delRowsNum;
				}
			}
		});
		//删除分组数据
		$.each(groupsIndex, function(i, index){
			groups.splice(index, 1);
			_self.refTabList.splice(index, 1);
			for(var g=(i+1);g<groupsIndex.length;g++){//调整未删除的分组索引号
				groupsIndex[g]--;
			}
		});
		_reBuildTranModelRefGrid();
	}

	/**
	 * 插入行数据
	 * @param rowData
	 */
	function _addRowData(rowData){
		var groupsIndex = _getSelectedGroupsIndex();
		if(groupsIndex.length == 1){
			_addRowDataForGroupRowLast(groupsIndex[0], rowData);
			_reBuildTranModelRefGrid();
			if(_addRowIndex >= 0){
				_self.grid.table.jqDom.find('div.row:eq('+_addRowIndex+')').trigger('click');
			}
		}
	}

	/**
	 * 插入行数据(引用字段)到指定分组(引用表)数据的最后
	 * @param gIndex   分组索引，从0开始
	 * @param rowData  行数据
	 */
	function _addRowDataForGroupRowLast(gIndex, rowData){
		var groups = _self.groups;
		var list = _self.grid.table.page.list;
		$.each(groups, function(g, group){
			if(g == gIndex){
				_addRowIndex = group.between[1];
				list.splice(group.between[1], 0, rowData);
				group.between[1]++;
			}
			if(g > gIndex){
				group.between[0]++;
				group.between[1]++;
			}
		});
	}

	/**
	 * 删除所选行数据
	 */
	function _deleteSelectedRowsData(){
		var deletedNum = 0;//批量删除时，已删除的数量
		_self.grid.table.jqDom.find('div.row').each(function(s, row){
			if($(row).hasClass('row-bg-selected')){
				_delRowDataForGroups(s - deletedNum);
				deletedNum++;
			}
		});
		_self.grid.table.deleteSelectedRow();
	}

	/**
	 * 在分组中删除行数据
	 * @param rowIndex    数据所在行索引，从0开始
	 */
	function _delRowDataForGroups(rowIndex){
		var groups = _self.groups;
		var gIndex = -1;
		$.each(groups, function(g, group){
			var between = group.between;
			var start = between[0], end = between[1];
			if(rowIndex >= start && rowIndex < end){
				gIndex = g;
				between[1]--;
			}
			if(gIndex > 0 && g > gIndex){//之后分组数据减1
				between[0]--;
				between[1]--;
			}
		});
	}

	//初始化并组织，可引用表和表中字段的存储结构
	function _initCanRefTabAndFd(){
		var opts = _self.opts;
		if(opts.canreftab && opts.canreftabfd){
			$.each(opts.canreftab, function(t, tab){
				if(!tab.FDS){
					tab.FDS = [];
				}
				$.each(opts.canreftabfd, function(f, fd){
					if(fd['FD_NAME']){
						if(tab['TAB_NAME'] == fd['TAB_NAME']){
							tab.FDS.push(fd);
						}
					}
				});
			});
		}
	};

	/**
	 * 通过引用ID(REF_ID)或引用表名(REF_TAB_NAME)获取引用表字段
	 * @param {REF_ID:'', REF_TAB_NAME:''}
	 */
	function _getRefTabFdByRefIdOrRefTab(obj){
		var opts = _self.opts;
		var refTabObj = [], refTabName = null;
		if(obj['REF_TAB_NAME']){
			refTabName = obj['REF_TAB_NAME'];
		}else if(obj['REF_ID']||obj['REF_ID']==0){
			$.each(_self.refTabList, function(i, ref){
				if(ref['REF_ID'] == obj['REF_ID']){
					refTabName = ref['REF_TAB_NAME'];
					return false;
				}
			});
		}
		if(refTabName){
			if(opts.canreftab){
				$.each(opts.canreftab, function(t, tab){
					if(tab['TAB_NAME'] == refTabName){
						if(tab['FDS']){
							refTabObj = tab['FDS'];
						}
						return false;
					}
				});
			}
		}
		return refTabObj;
	}

	/**
	 * 初始设置可引用表
	 * @param isAdd  是否新增
	 * @param fn     执行函数
	 */
	function _setRefTabFromData(isAdd, fn){
		var opts = _self.opts;
		if(!_loadedCanRefTab){
			_self.RefTabRowForm.getItem('REF_TAB_NAME').component.reload((opts.canreftab?opts.canreftab:[]), {text:'TAB_DESC', value:'TAB_NAME'});
		}
		_loadedCanRefTab = true;
		if(isAdd){
			_self.RefTabRowForm.getItem('REF_TAB_NAME').enable();
		}else{
			_self.RefTabRowForm.getItem('REF_TAB_NAME').disable(true);
		}
		jcl.postJSON('/tms35/tranmdl/queryCanRefFd', 'tab_name='+opts.txnId, function(data){
			_self.RefTabRowForm.getItem('SRC_EXPR').component.reload(data, {key:'canRefFd', text:'NAME', value:'REF_NAME'});
			if(fn && (typeof(fn) == 'function')){
				fn();
			}
		}, false);
	}

	/**
	 * 获取已选分组的索引号数组集合
	 */
	function _getSelectedGroupsIndex(){
		var _groupsIndex = [];
		var allGroups = _self.grid.table.jqDom.find('.txn-attr-group');
		var selectedGroups = allGroups.find('[type=checkbox]:checked');
		$.each(selectedGroups, function(){
			var _index = allGroups.find('[type=checkbox]').index(this);
			_groupsIndex.push(_index);
		});
		return _groupsIndex;
	}

	/**
	 * toolbar按钮默认初始状态
	 */
	function _initToolbarStatus(){
		if(dualAudit){ // readonly mode
			_self.grid.toolbar.disable($('#btn-reftab-add'), false);
			_self.grid.toolbar.disable($('#btn-reftab-edit'), false);
			_self.grid.toolbar.disable($('#btn-reftab-del'), false);
			_self.grid.toolbar.disable($('#btn-reffd-add'), false);
			_self.grid.toolbar.disable($('#btn-reffd-edit'), false);
			_self.grid.toolbar.disable($('#btn-reffd-del'), false);
			_self.grid.toolbar.disable($('#btn-ref-view'));
			return;
		}
		_self.grid.toolbar.enable($('#btn-reftab-add'));
		_self.grid.toolbar.disable($('#btn-reftab-edit'));
		_self.grid.toolbar.disable($('#btn-reftab-del'));
		_self.grid.toolbar.disable($('#btn-reffd-add'));
		_self.grid.toolbar.disable($('#btn-reffd-edit'));
		_self.grid.toolbar.disable($('#btn-reffd-del'));
		_self.grid.toolbar.disable($('#btn-ref-view'));
	};
	/**
	 * 根据选中checkbox，重新设置Grid
	 */
	function _reSetGridBySelected(){
		$.each(_self.grid.toolbar.opts.btns, function(i){
			var toggle = _self.grid.toolbar.isToggle(i);
			if(toggle){
				_isToggleForToolbar = i;
			}
		});
		var groupsIndex = _getSelectedGroupsIndex();
		var srows = _self.grid.table.selectedRows();//引用字段
		if(((srows && srows.length > 0) && (groupsIndex && groupsIndex.length > 0))
		 || ((!srows || (srows && srows.length == 0)) && (!groupsIndex || (groupsIndex && groupsIndex.length == 0)))){//全选或全不选
			_initToolbarStatus();
			return;
		}
		var opts = _self.opts;
		var refTabList = _self.refTabList;
		var _toolbar = [true, true, true, true, true, true, true];
		if(srows.length > 0 && (!groupsIndex || (groupsIndex && groupsIndex.length == 0))){//只选了引用字段
			_toolbar[1] = false;
			_toolbar[2] = false;
			_toolbar[3] = false;
			$.each(srows, function(i, srow){
				if(srow['TAB_NAME'] != opts.txnId){//不是当前交易下定义的引用字段
					_toolbar[4] = false;
					_toolbar[5] = false;
				}
				if(i > 0){//多选，不可编辑查看
					_toolbar[4] = false;
					_toolbar[6] = false;
				}
				if(srows.length == 1){//更新引用表字段中的数据来源下拉列表
					_self.grid.table.rowForm.getItem('REF_FD_NAME').component.reload(_getRefTabFdByRefIdOrRefTab(srow), {text:'NAME', value:'FD_NAME'});
				}
			});
		}else if((groupsIndex && groupsIndex.length > 0) && (!srows || (srows && srows.length == 0))){//只选了引用表
			_toolbar[4] = false;
			_toolbar[5] = false;
			$.each(groupsIndex, function(i, index){
				var refTab = (index < refTabList.length ? refTabList[index] : null);
				var group = _self.groups[index];
				if(refTab){
					if(refTab['TAB_NAME'] == opts.txnId){//当前交易下的引用表
						/*if(refTab['REF_FDS']){
							$.each(refTab['REF_FDS'], function(i, refFd){
								if(refFd['TAB_NAME'] != opts.txnId){//当前交易的引用表下，包含非当前交易下引用的字段
									_toolbar[2] = false;
								}
							});
						}*/
						var between = group.between;
						var start = between[0], end = between[1];
						if(start != end){//说明分组中包含字段
							_toolbar[2] = false;
						}
					}else{
						_toolbar[1] = false;
						_toolbar[2] = false;
					}
				}
				if(i > 0){//多选，不可编辑查看引用表，不可添加引用字段
					_toolbar[1] = false;
					_toolbar[3] = false;
					_toolbar[6] = false;
				}
			});
		}
		$.each(_toolbar, function(i, _bar){
			if(dualAudit){// readonly mode
				_self.grid.toolbar.disable(i);
				if((i+1) == _toolbar.length && _bar){
					_self.grid.toolbar.enable(i);
				}
			}else{
				if(_bar){
					_self.grid.toolbar.enable(i);
				}else{
					_self.grid.toolbar.disable(i);
				}
			}
		});
	};

	/**
	 * 绑定事件
	 *
	 */
	function _bindEvent(){
		var opts = _self.opts;
		_self.grid.table.jqDom.on('click', 'span.group-ec', function(){
	        var ec = $(this);
	        if(ec.hasClass('group-expand')){
	            ec.addClass('group-collapse').removeClass('group-expand');
	            ec.parent().nextUntil('div.txn-attr-group', 'div.row').hide();
	        }else{
	            $(this).addClass('group-expand').removeClass('group-collapse');
	             ec.parent().nextUntil('div.txn-attr-group', 'div.row').show();
	        }
	        return false;
	    });

	    /*_self.grid.table.jqDom.on('click', '.txn-attr-group:not(.checkbox-group)', function(){
	    	var index = _self.grid.table.jqDom.find('.txn-attr-group').index(this);
	    	_self.grid.table.jqDom.find('.table-rows').find('.txn-attr-group:eq('+index+')').find('[type=checkbox]').trigger('click');
	    });*/

		_initToolbarStatus();

		_self.grid.table.jqDom.on('click', 'input.checkbox-group', function(){
			_reSetGridBySelected();
		});

		_self.grid.table.onRowSelectChange(function(){
			_reSetGridBySelected();
		});
		/* toolbar引用表的操作按钮Begin */
		_self.grid.toolbar.onClick('#btn-reftab-add', function(){//添加引用表
			var toggle = _self.grid.toolbar.isToggle(this);
			if(toggle){
				_unEditRefTabRow();
				_self.grid.toolbar.toggle(this, false);
			}else{
				_setRefTabFromData(true, function(){
					_addRefTabRow();
					_self.RefTabRowForm.getItem('TAB_NAME').val(opts.txnId);
					_self.RefTabRowForm.getItem('TAB_DESC').val(opts.txnName);
				});
				_self.grid.toolbar.toggle(this, true);
				_disableToolbarStatus(0);
			}
			_self.RefTabRowForm.getItem('TAB_DESC').disable(true);
		});
		_self.grid.toolbar.onClick('#btn-reftab-edit', function(){//编辑引用表
			var toggle = _self.grid.toolbar.isToggle(this);
			if(toggle){
				_unEditRefTabRow();
				_self.grid.toolbar.toggle(this, false);
			}else{
				_setRefTabFromData(false, function(){
					_editRefTabRow();
				});
				_self.grid.toolbar.toggle(this, true);
				_disableToolbarStatus(1);
			}
			_self.RefTabRowForm.getItem('TAB_DESC').disable(true);
		});
		_self.grid.table.onRowFormHide(function(){
			_self.grid.table.rowForm.jqDom.find('#row-edit-sure').show();
		});
		_self.grid.toolbar.onClick('#btn-ref-view', function(){//查看引用表和引用字段
	    	var rows = _self.grid.selectedRows();
	    	var toggle = _self.grid.toolbar.isToggle(this);
	    	if(rows && rows.length == 1){
	    		if(toggle){
	    			_self.grid.table.unEditRow();
	    			_self.grid.toolbar.toggle(this, false);
	    		}else{
	    			_self.grid.table.rowForm.jqDom.find('#row-edit-sure').hide();
	    			_self.grid.table.viewSelectedRow();
	    			_self.grid.toolbar.toggle(this, true);
	    			_self.grid.toolbar.enable(this);
	    			_disableToolbarStatus(6);
	    		}
	    	}else{
				if(toggle){
					_unEditRefTabRow();
					_self.grid.toolbar.toggle(this, false);
				}else{
					_setRefTabFromData(false, function(){
						_viewRefTabRow();
					});
					_self.grid.toolbar.toggle(this, true);
					_disableToolbarStatus(6);
				}
	    	}
			return false;
	    });
		_self.grid.toolbar.onClick('#btn-reftab-del', function(){//删除引用表
			var groupsIndex = _getSelectedGroupsIndex();
			var refTabList = _self.refTabList;
			var delRefTab = [];
			$.each(groupsIndex, function(i, index){
				var refTab = (index < refTabList.length ? refTabList[index] : null);
				delRefTab.push(refTab);
			});
			if(confirm('确定删除？')){
				var json_data = JSON.stringify({'del':delRefTab});
				var url_para = "postData=" + json_data;
				jcl.postJSON('/tms35/tranmdl/saveModelRefTab', url_para, function(callback){
					_deleteSelectedGroupsData();
					jcl.msg.info("删除成功");
				});
			}
			return false;
		});
		/* toolbar引用表的操作按钮End */

		/* toolbar引用字段的操作按钮Begin */
		_self.grid.toolbar.onClick('#btn-reffd-add',function(){
			var groupsIndex = _getSelectedGroupsIndex();
			var refTab = _self.refTabList[groupsIndex[0]];
			_self.grid.table.rowForm.getItem('REF_ID').val(refTab['REF_ID']);
			_self.grid.table.rowForm.getItem('REF_TAB_DESC').val(refTab['REF_DESC']);
			_self.grid.table.rowForm.getItem('REF_FD_NAME').component.reload(_getRefTabFdByRefIdOrRefTab(refTab), {text:'NAME', value:'FD_NAME'});
			/*_self.grid.table.rowForm.getItem('TAB_NAME').val(opts.txnId);
			_self.grid.table.rowForm.getItem('TAB_DESC').val(opts.txnName);
			_self.grid.table.rowForm.getItem('TAB_DESC').disable(true);*/
		});
		_self.grid.toolbar.onClick('#btn-reffd-edit',function(){
			var srow = _self.grid.table.selectedOneRow();
			var fd_ref_id = srow['REF_ID'];
			for(var i=0;i<_self.refTabList.length;i++){
				var refTab = _self.refTabList[i];
				if(refTab['REF_ID'] == fd_ref_id){
					_self.grid.table.rowForm.getItem('REF_TAB_DESC').val(refTab['REF_DESC']);
					break;
				}
			}
			/*_self.grid.table.rowForm.getItem('TAB_NAME').val(opts.txnId);
			_self.grid.table.rowForm.getItem('TAB_DESC').disable(true);*/
		});
		/* toolbar引用字段的操作按钮End */

		_self.grid.table.onRowFormSubmit(function(rowFormJqDom, rowData, isAdd){
			if(_checkRefFdRowFormData(rowData, isAdd)){
				var row = rowData;
				if(!isAdd){
					var oldrow = _self.grid.table.selectedOneRow();
					row = $.extend({}, oldrow, rowData);
					$.extend(row, {old:oldrow});
				}
				var json_data = isAdd ? JSON.stringify({'add':[row]}):JSON.stringify({'mod':[row]});
				var url_para = "postData=" + encodeURIComponent(json_data);
				jcl.postJSON('/tms35/tranmdl/saveModelRefFd', url_para, function(data){
					if(data && data.row){
						$.extend(data.row, {TAB_DESC:opts.txnName});
						if(isAdd){
							_addRowData(data.row);
						}else{
							_self.grid.table.updateEditingRow(data.row);
						}
						_reloadEnableStorageFd(opts.txnId);//更新可用存储字段
						jcl.msg.info('保存成功！');
					}
				});
			}
			return false;
	    });
	    _self.grid.toolbar.onClick('#btn-reffd-del', function(){
	    	if(confirm('确定删除？')){
	    		var rows = _self.grid.selectedRows();
				var json_data = JSON.stringify({'del':rows});
				var url_para = "postData=" + encodeURIComponent(json_data);
				jcl.postJSON('/tms35/tranmdl/saveModelRefFd', url_para, function(callback){
					_deleteSelectedRowsData();
					_reloadEnableStorageFd(opts.txnId);//更新可用存储字段
					jcl.msg.info("删除成功");
				});
			}
			return false;
	    });

		//表单赋值(form.set())后，显示
		_self.grid.table.onRowFormShow(function(isAdd){
			
			_reloadEnableStorageFd(opts.txnId);//更新可用存储字段
			
			var fl = true;
			var storecolumn = '';
			if(!isAdd){
				storecolumn = _self.grid.selectedOneRow().STORECOLUMN;
				if(storecolumn==null) storecolumn = '';
				fl = false;
			}
			var type = '';
			$.each(_self.opts.canreftabfd, function(i,f){
				if(f.FD_NAME == _self.grid.table.rowForm.getItem('REF_FD_NAME').val()){
					type = f.TYPE;
				}	
			});
			_self.grid.table.rowForm.getItem('STORECOLUMN').component.reload(_getCanUseStorageFdByDataType(type, fl), {text:'CODE_VALUE', value:'CODE_KEY'});// 根据引用字段类型过滤存储字段
			_self.grid.table.rowForm.getItem('STORECOLUMN').val(storecolumn);

			_self.grid.table.rowForm.getItem('SRC_COND_IN').component.attr("readonly","readonly");
			_self.grid.table.rowForm.getItem('SRC_EXPR_IN').component.attr("readonly","readonly");
						
			if(isAdd){
				/* 新增时变更rowForm的显位置Begin */
				var groupsIndex = _getSelectedGroupsIndex();
				_self.grid.table.jqDom.find('div.table-rows').find('div.txn-attr-group:eq('+groupsIndex[0]+')').after(_self.grid.table.rowFormDiv);
				_self.grid.table.rowSelectedScroll(_self.grid.table.rowFormDiv);
				/* 新增时变更rowForm的显位置End */
				_self.grid.table.rowForm.getItem('TAB_DESC').val(opts.txnName);
			}
			_self.grid.table.rowForm.getItem('TAB_NAME').val(opts.txnId);
			_self.grid.table.rowForm.getItem('TAB_DESC').disable(true);
		});

		//引用表表单保存
		_self.RefTabRowForm.jqDom.on('click', '#ref-tab-edit-sure', function(){
			var rowData = _self.RefTabRowForm.data();
			var isAdd = false;
			if(!rowData['REF_ID']){
				isAdd = true;
			}
			if(_checkRefTabRowFormData(rowData, isAdd)){
				var row = rowData;
				if(!isAdd){
					var groupsIndex = _getSelectedGroupsIndex();
					row = _self.refTabList[groupsIndex[0]];
					$.extend(row, rowData);
				}
				var json_data = isAdd ? JSON.stringify({'add':[row]}):JSON.stringify({'mod':[row]});
				var url_para = "postData=" + encodeURIComponent(json_data);
				jcl.postJSON('/tms35/tranmdl/saveModelRefTab', url_para, function(data){
					if(data && data.row){
						$.extend(data.row, {TAB_DESC:opts.txnName});
						if(isAdd){
							_addGroupDataForGroupsLast(data.row);
						}else{
							_modGroupDataForRefTabList(data.row);
						}
						_unEditRefTabRow();
						_reBuildTranModelRefGrid();
						_self.grid.table.jqDom.find('div.table-rows').find('div.txn-attr-group:eq('+_modGroupsIndex+')').find('input.checkbox-group').prop('checked', true);
						_reSetGridBySelected();
						jcl.msg.info('保存成功！');
					}
				});
			}
			return false;
		});
		//引用表取消表单
		_self.RefTabRowForm.jqDom.on('click', '#ref-tab-edit-cancel', function(){
			_unEditRefTabRow();
		});

		_self.grid.table.rowForm.jqDom.on('click', '#row-edit-cancel', function(){
			$.each(_self.grid.toolbar.opts.btns, function(i, btn){
				_self.grid.toolbar.toggle(i, false);
			});
		});

		//引用表下拉列表onchange事件
		_self.RefTabRowForm.getItem('REF_TAB_NAME').component.onChange(function(item){
			var text = item.text;
			var ref_desc = _self.RefTabRowForm.getItem('REF_DESC');
			ref_desc.val(text);
		});
		//引用字段表中的数据来源onchange事件
		_self.grid.table.rowForm.getItem('REF_FD_NAME').component.onChange(function(item){
			var ref_desc = _self.grid.table.rowForm.getItem('REF_DESC');
			var ref_name = _self.grid.table.rowForm.getItem('REF_NAME');
			ref_desc.val(item.text);
			ref_name.val(item.value);
			var type = '';
			$.each(_self.opts.canreftabfd, function(i,f){
				if(f.FD_NAME == item.value){
					type = f.TYPE;
				}	
			});
			_self.grid.table.rowForm.getItem('STORECOLUMN').component.reload(_getCanUseStorageFdByDataType(type, true), {text:'CODE_VALUE', value:'CODE_KEY'});// 根据引用字段类型过滤存储字段
			
		});

		//引用表字段，表单条件双击
		var _ce = new condEdit();
		_self.grid.table.rowForm.jqDom.on('dblclick', '[name=SRC_COND_IN]', function(){
			var opts = _self.opts;
			var needHideSelectArr = ['STAT_FN', 'ROSTER_FUNC', 'RULE_FUNC', 'DIY_FUNC','AC_FUNC'];
			_ce.init_cond(_self.grid.table.rowForm.getItem('SRC_COND').component,$(this),opts.txnId, needHideSelectArr,'条件');
		});

		//引用表字段，表单表达式双击
		_self.grid.table.rowForm.jqDom.on('dblclick', '[name=SRC_EXPR_IN]', function(){
			var opts = _self.opts;
			var needHideSelectArr = ['STAT_FN', 'ROSTER_FUNC', 'RULE_FUNC', 'DIY_FUNC','AC_FUNC'];
			_ce.init_cond(_self.grid.table.rowForm.getItem('SRC_EXPR').component,$(this),opts.txnId, needHideSelectArr,'表达式');
		});

		_self.grid.table.jqDom.on('click', 'div.row', function(){
			if(_editingRefTabRowIndex > -1){
				_self.grid.table.jqDom.find('div.row').removeClass('row-bg-selected');
				_self.grid.table.jqDom.find('div.table-columns input.row-cb').prop('checked', false);
				_self.grid.table.jqDom.find('div.table-rows input.row-cb').not('input.checkbox-group').prop('checked', false);
				if(_isToggleForToolbar > -1){
					$.each(_self.grid.toolbar.opts.btns, function(i){
						if(i == _isToggleForToolbar){
							_self.grid.toolbar.enable(i);
						}else {
							_self.grid.toolbar.disable(i);
						}
					});
				}
			}
		});
	};

	function _addRefTabRow(){
		_editingRefTabRowIndex = _self.groups.length;
		_self.grid.table.jqDom.find('div').removeClass('row-bg-selected').find('input.row-cb:checkbox').prop('checked', false);
		_self.RefTabRowForm.reset();
		_self.RefTabRowForm.showEditMode();
		_self.grid.jqDom.find('input.row-cb:checkbox').prop('disabled', true);
		_self.RefTabRowFormDiv.prependTo(_self.grid.table.jqDom.find('div.table-rows')).stop(true, true).slideDown('fast');
	};

	function _editRefTabRow(){
		_self.RefTabRowForm.reset();
		var groupsIndex = _getSelectedGroupsIndex();
		var index = groupsIndex[0];
		_editingRefTabRowIndex = index;
		var refTabList = _self.refTabList;
		var refTabData = (index < refTabList.length ? refTabList[index] : null);
		_self.RefTabRowForm.set(refTabData);
		_self.RefTabRowForm.showEditMode();
		_self.grid.jqDom.find('input.row-cb:checkbox').prop('disabled', true);
		var rowDom = _self.grid.table.jqDom.find('div.table-rows').find('div.txn-attr-group:eq('+index+')');
		rowDom.after(_self.RefTabRowFormDiv);
		_self.RefTabRowFormDiv.show();
		_self.grid.table.rowSelectedScroll(rowDom);
	};

	function _viewRefTabRow(){
		_self.RefTabRowForm.reset();
		var groupsIndex = _getSelectedGroupsIndex();
		var index = groupsIndex[0];
		_editingRefTabRowIndex = index;
		var refTabList = _self.refTabList;
		var refTabData = (index < refTabList.length ? refTabList[index] : null);
		_self.RefTabRowForm.set(refTabData);
		_self.RefTabRowForm.showViewMode();
		_self.RefTabRowForm.jqDom.find('#ref-tab-edit-sure').hide();
		_self.grid.jqDom.find('input.row-cb:checkbox').prop('disabled', true);
		var rowDom = _self.grid.table.jqDom.find('div.table-rows').find('div.txn-attr-group:eq('+index+')');
		rowDom.after(_self.RefTabRowFormDiv);
		_self.RefTabRowFormDiv.show();
		_self.grid.table.rowSelectedScroll(rowDom);
	}

function _getCanUseStorageFdByDataType(datatype, effect){
		var opts = _self.opts;
		var sfdItems = [];
		var enableStoreFds = $.extend([], opts.enableStoreFd ? opts.enableStoreFd : []);
		if(!effect){
			var srows = _self.grid.table.selectedRows();
			if(srows && srows.length > 0){
				var srow = srows[0];
				if(srow && srow['STORECOLUMN']){
					var allStoreFds = opts.allStoreFd;
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
						if(dt.recap == 'long' || dt.recap == 'decimal' ){
							if(datatype != 'double'&&datatype != 'long'){
								if(fd['TYPE'] == datatype){
									sfdItems.push({CODE_VALUE: fd.FD_NAME, CODE_KEY: fd.FD_NAME});
								}
							}
							if(fd['TYPE'] == 'double'||fd['TYPE'] == 'long'){
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
		var opts = _self.opts;
		var tab_name = tabName ? tabName : opts.txnId;
		_self.storage = [];
		jcl.postJSON('/tms35/tranmdl/queryAvailableStoreFd', 'tab_name='+tab_name, function(data){
			if(data && data.enableStoreFd){
				_self.opts.enableStoreFd = data.enableStoreFd;
			}
		});
	};
	function _unEditRefTabRow(){
		_editingRefTabRowIndex = -1;
		_isToggleForToolbar = -1;
		//_self.RefTabRowFormDiv.hide();
		_self.RefTabRowFormDiv.stop(true, true).slideUp('fast');
		//_self.RefTabRowFormDiv.appendTo(_self.grid.jqDom);
		_self.grid.jqDom.find('input.row-cb:checkbox').removeAttr('disabled');
		_self.RefTabRowForm.jqDom.find('#ref-tab-edit-sure').show();
		$.each(_self.grid.toolbar.opts.btns, function(i, btn){
			_self.grid.toolbar.toggle(i, false);
		});
		_reSetGridBySelected();
	};

	/**
	 * disabled toolbar的按钮
	 * @param dom  保持状态不修改
	 */
	function _disableToolbarStatus(site){
		$.each(_self.grid.toolbar.opts.btns, function(i, btn){
			if(site != i){
				_self.grid.toolbar.disable(i);
			}
		});
	}

	/**
	 * 校验引用表form数据合法性
	 * @param rowData  form数据
	 * @param isAdd    是否新增
	 */
	function _checkRefTabRowFormData(rowData, isAdd){
		var dataCheck = [
			{label:'引用ID', name:'REF_ID', required:!isAdd, length:8},
			{label:'引用表', name:'REF_TAB_NAME', required:true, length:25, special:'alphabet'},
		 	{label:'引用描述', name:'REF_DESC', required:true, length:64, special:'chinese'},
		 	{label:'引用字段', name:'SRC_EXPR', required:true, length:250, special:'alphabet'},
		 	{label:'所属节点', name:'TAB_NAME', required:true, length:20}
		];
	 	try{
	 		$.each(dataCheck, function(i, dc){
		 		if(dc.required){
		 			if(!IsEmpty(rowData[dc.name]) || !IsEmpty(Trim(rowData[dc.name]))){
		 				throw (dc.label + '不能为空');
		 			}
		 		}
		 		if(rowData[dc.name]){
		 			if(!checkeLength(rowData[dc.name], dc.length)){;
						throw (dc.label + '不能超过' + dc.length + '个字符');
					}
					var error = _checkSpecial(dc, rowData[dc.name]);
					if(error){
						throw error;
					}
		 		}
		 	});
		 }catch(e){
		 	jcl.msg.info(e);
		 	return false;
		 }
		 return true;
	}

	/**
	 * 校验引用表字段form数据合法性
	 * @param rowData  form数据
	 * @param isAdd    是否新增
	 */
	function _checkRefFdRowFormData(rowData, isAdd){
		var dataCheck = [
			{label:'引用ID', name:'REF_ID', required:true, length:8},
		 	{label:'属性名称', name:'REF_DESC', required:true, length:64, special:'chinese'},
		 	{label:'属性代码', name:'REF_NAME', required:true, length:50, special:'alphabet'},
		 	{label:'数据来源', name:'REF_FD_NAME', required:true, length:50, special:'alphabet'},
		 	{label:'条件', name:'SRC_COND', length:250},
		 	{label:'表达式', name:'SRC_EXPR', length:250},
		 	{label:'所属节点', name:'TAB_NAME', required:true, length:20}
		];
	 	try{
	 		$.each(dataCheck, function(i, dc){
		 		if(dc.required){
		 			if(!IsEmpty(rowData[dc.name]) || !IsEmpty(Trim(rowData[dc.name]))){
		 				throw (dc.label + '不能为空');
		 			}
		 		}
		 		if(rowData[dc.name]){
		 			if(!checkeLength(rowData[dc.name], dc.length)){
						throw (dc.label + '不能超过' + dc.length + '个字符');
					}
					var error = _checkSpecial(dc, rowData[dc.name]);
					if(error){
						throw error;
					}
		 		}
		 		if(dc.name == 'SRC_EXPR'){
		 			if(rowData['SRC_COND']){
		 				if(!IsEmpty(rowData[dc.name]) || !IsEmpty(Trim(rowData[dc.name]))){
		 					throw ('填写条件后，' + dc.label + '不能为空');
		 				}
		 			}
		 		}
		 	});
		 }catch(e){
		 	jcl.msg.info(e);
		 	return false;
		 }
		 return true;
	}
	
	function _checkSpecial(dc, value){
		var error = '';
		if(dc.special){
			if(dc.special == 'alphabet'){//字母校验
				if(!checkSpecialCharacter(value, '1')){
					error = (dc.label + '只能以字母开头，包含字母、数字和下划线');
				}
			}else if(dc.special == 'chinese'){//汉字
				if(!checkSpecialCharacter(value, '2')){
					error = (dc.label + '只能包含汉字,字母,数字和下划线');
				}
			}else if(dc.special == 'expression'){//表达式
				if(!checkeCondSpecialCode(value)){
					error = (dc.label + '不能包含特殊字符');
				}
			}else if(dc.special == 'general'){//普通
				if(!checkeSpecialCode(value)){
					error = (dc.label + '不能包含特殊字符');
				}
			}
		}
		return error;
	}
};