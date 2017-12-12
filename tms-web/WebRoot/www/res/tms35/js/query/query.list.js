var jcq = typeof(jcq) === 'undefined' ? {} : jcq;
jcq.view = jcq.view||{};
/**
 * 自定义查询
 */
jcq.view.QueryGrid = function(options, container) {
	var $grid = this;
	this.opts = $.extend({}, jcq.customquery._defaults, options);
	var grid = '';

	function init() {
		var opts = $grid.opts;
		var id = new Date().getTime();
		var gid = opts.id ? opts.id : ('query_grid_' + id);
		var jqObj = opts.id ? $('#' + opts.id) : null;
		if(!jqObj || jqObj.length === 0) {
			jqObj = $('<div id="' + gid + '"></div>').appendTo(container || $('body'));
		}
		$this = $('#' + gid);
		$(_searchFrom(id)).appendTo($this);
		_resultData(id, $this);
		var queryScript = opts.json.query_script;
		if(queryScript && queryScript.ready_script) {
			eval(queryScript.ready_script);//加载执行设置函数
		}

		//注册查询按钮事件
		$('#query_search_btn').click(function(){
			if(_dataCheck(opts.json.spell_type, opts.json.query_cond)) {
				var params = "queryId=" + queryId + "&" + $('#searchForm').serialize();
				grid.setDsParams(params);
				grid.goPage();
			}
		});
	}

	function _searchFrom(id) {
		var opts = $grid.opts;
		var htmlstr = '';
		htmlstr += '<form id="searchForm' + id + '" name="searchForm" method="post">';
		htmlstr += '<table id="search' + id + '" name="search">';
		var lineConds = opts.json.query_cond.line_conds;// 每行几个查询条件
		var conds = opts.json.query_cond.conds;
		var i = 0;
		$.each(conds, function(c, map) {
			htmlstr += (i===0 ? '<tr>' : '');
			var name = map.name;
			// var expression = map.expression;
			var subconds = map.subconds;
			var condHtml = '';
			$.each(subconds, function(sc, scmap) {
				if(IsEmpty(name)) {
					scmap.name = name;
				}
				if(sc > 0) {
					condHtml += map.join;
				}
				condHtml += _subcondElement(scmap, opts.fileds[scmap.rec_filed]);
			});
			htmlstr += ('<td align="left">' + map.title + '：</td>');
			htmlstr += ('<td>' + condHtml + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>');
			i++;
			if(i == lineConds) {
				htmlstr += '</tr>';
				i = 0;
			}
		});
		htmlstr += '<tr><td colspan="' + lineConds + '"><input id="query_search_btn" type="button" value="查询" class="btn"/></td></tr>';
		htmlstr += '</table></form>';
		return htmlstr;
	}

	function _subcondElement(subcond, filedObj) {
		var qeHtml = "";
		var id = subcond.id;
		var name = subcond.name;
		var title = subcond.title;
		var defaultvalue = subcond.default_value;
		var script_excite = subcond.script_excite;
		title = IsEmpty(title) ? title : filedObj['NAME'];
		var eleHtml = ((IsEmpty(id) ? " id='"+id+"'" : "") + (IsEmpty(name) ? " name='"+name+"'" : ""));
		eleHtml += " title='" + title + "'";
		eleHtml += (IsEmpty(script_excite) ? (" " + script_excite) : "");
		var tmsType = filedObj['TYPE'];
		if(tmsType == 'date') {
			qeHtml = "<input type='text' onfocus='jcl.datepicker(this)' readonly='readonly' class='tms-itext' size='12' "+eleHtml+" " + (IsEmpty(defaultvalue) ? "value='" + defaultvalue + "' " : "") + "/>";
		}else if(tmsType == 'datetime') {
			qeHtml = "<input type='text' onfocus='jcl_tms.datepicker(this)' readonly='readonly' class='tms-itext' "+eleHtml+" " + (IsEmpty(defaultvalue) ? "value='" + defaultvalue + "' " : "") + "/>";
		} else if(tmsType == 'code') {
			qeHtml = "<select "+ eleHtml +">";
			qeHtml += "<option value=''>---请选择---</option>";
			var codes = jcl.code.getCodes(filedObj['CODE']);
			if(IsEmpty(codes)) {
				for(var i in codes){
					qeHtml += "<option value='"+i+"'"+ (IsEmpty(defaultvalue) ? (defaultvalue == i ? " selected " : "") : "") +">"+codes[i]+"</option>";
				}
			}
			qeHtml += "</select>";
		} else {
			qeHtml = "<input type='text' class='itext' " + eleHtml + " "  + (IsEmpty(defaultvalue) ? "value='" + defaultvalue + "' " : "") +  "/>";
		}
		return qeHtml;
	}

	function _resultData(id, container) {
		var opts = $grid.opts;
		/* 组织查询结果列数据Begin */
		var cols = [];
		var columnsList = opts.json.query_result.columns;
		$.each(columnsList, function(i, map) {
			if(!map.hide) {
				var rec_filed = map.rec_filed;
				var _name = opts.fileds[rec_filed]['NAME'];
				var _width = (map.width === 0 ? _name.length*13 : map.width);
				cols.push({name:_name, width:_width, dataIndex:map.fd_name});
			}
		});
		var ds_callback = opts.json.query_result.ds_callback;// grid回调函数
		var qfrom_display = opts.json.query_cond.qfrom_display;
		var box_title = opts.json.box_title;
		var title = IsEmpty(box_title) ? box_title : opts.title;
		grid = new jcl.ui.Grid({
			title: title,
			marginTop: 0,
			columns: cols,
			ds:{
				url: '/tms35/query/result',
				params: opts.params,
				callback: function(list) {
					jcq.customquery.callBackResults(list, columnsList, opts.fileds, ds_callback);
				}
			},
			toolbar:[
				{id:'btn-find', icon:'icon-tb-find', text:"查询", toggleQform:true}
			],
			checkboxEnable:false,
			pagebar: true,
			qform: {id:'searchForm' + id, display:qfrom_display}
		}, container);
		grid.goPage();
		/* 组织查询结果列数据End */
	}

	/**
	 * 查询条件数据校验 spellType 拼写类型 queryCond 查询条件集合
	 */
	function _dataCheck(spellType, queryCond) {
		var expr = queryCond.expression;
		var codes = _getCondCodes(expr);
		var conds = queryCond.conds;
		var errorMsg = '';
		$.each(conds, function(i, cond) {
			var name = cond.name;
			var title = cond.title;
			var subExpr = cond.expression;
			var subconds = cond.subconds;
			var subcodes = _getCondCodes(subExpr);
			var scLen = subconds.length;
			for(var s=0;s<scLen;s++) {
				var subname = subconds[s].name;
				var subtitle = subconds[s].title;
				var oper = subconds[s].oper;
				subtitle = IsEmpty(subtitle) ? subtitle : (title + (scLen == 1 ? '' : ',第' + (s+1) + '项'));
				var subvalue = IsEmpty(name) ? (document.getElementsByName(name)[s]).value : (document.getElementsByName(subname)[0]).value;
				if(spellType == '2') {// 手工编写
					if(!codes.in_array(i+1)) {// 未在表达式中，必须都填写
						if(!IsEmpty(subvalue)) {
							errorMsg = (subtitle + ',不能为空！');
							return;
						}
					} else {
						if(subcodes!==null && subcodes.length>0) {
							if(!subcodes.in_array(s+1)) {// 未在表达式中
								if(!IsEmpty(subvalue)) {
									errorMsg = (subtitle + ',不能为空！');
									return;
								}
							}
						}
					}
				}
				if(subcodes === null && scLen > 1) {//未配置表达式，多个选项都填或都不填
					if(s>=0 && s+1<scLen) {
						var _subname = subconds[s+1].name;
						var _subtitle = subconds[s+1].title;
						var _oper = subconds[s+1].oper;
						_subtitle = IsEmpty(_subtitle) ? _subtitle : (title + ',第' + (s+2) + '项');
						var _subvalue = IsEmpty(name) ? (document.getElementsByName(name)[s+1]).value : (document.getElementsByName(_subname)[0]).value;
						if(IsEmpty(subvalue) && !IsEmpty(_subvalue)) {
							errorMsg = (_subtitle + ',不能为空！');
							return;
						}
						if(!IsEmpty(subvalue) && IsEmpty(_subvalue)) {
							errorMsg = (subtitle + ',不能为空！');
							return;
						}
						if(IsEmpty(subvalue) && IsEmpty(_subvalue)) {// 都不为空，校验大小
							if(oper.indexOf('>')!=-1 && _oper.indexOf('<') !=-1) {
								if(subvalue >= _subvalue) {
									errorMsg = (subtitle + '不能大于等于' + _subtitle + '！');
									return;
								}
							}
						}
					}
				}
			}
			if(IsEmpty(errorMsg)) {
				return;
			}
		});
		if(IsEmpty(errorMsg)) {
			jcl.msg.error(errorMsg);
			return false;
		}
		return true;
	}

	/*
	 * 获取查询条件表达式中的定义的查询条件序号 expression 条件表达式
	 */
	function _getCondCodes(expression) {
		var codes = [];
		codes = expression.match(/\d/g);
		return codes;
	}

	init();
};