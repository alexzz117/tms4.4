/**
 * 组织自定义查询实体详细的JS
 */
var jcq = typeof(jcq) === 'undefined' ? {} : jcq;
jcq.view = jcq.view||{};

jcq.view.QueryEntityDetail = function(options, container) {
	var $detail = this;
	var _defaults = {
		boxId : null
	};

	this.opts = $.extend({}, jcq.customquery._defaults, _defaults, options);
	this.parent = (container || $('body'));

	/**
	 * 生成jcl构建的列表
	 */
	function _generateList() {
		var opts = $detail.opts;
		/* 组织查询结果列数据Begin */
		var cols = [];
		var columnsList = opts.json.query_result.columns;
		$.each(columnsList, function(i, map) {
			if(!map.hide) {
				var _name = opts.fileds[map.rec_filed]['NAME'];
				var _width = (map.width === 0 ? _name.length*13 : map.width);
				var _dataIndex = map.fd_name;
				cols.push({name:_name, width:_width, dataIndex:_dataIndex});
			}
		});
		var detailGrid = new jcl.ui.Grid({
			title: opts.title,
			marginTop : 0,
			columns: cols,
			ds:{
			},
			checkboxEnable:false,
			pagebar: true,
			qform: {id:'searchForm', display:false}
		}, $detail.parent);
		detailGrid.renderPage(this.resultPage);
		/* 组织查询结果列数据End */
	}


	/**
	 * 生成普通table列表
	 */
	function _generateWireList() {
		var opts = $detail.opts;
		var sHtml = '';
		sHtml = "<table width='100%' id='conftab' border='1' style='border-collapse: collapse;'>";
		sHtml += "<tr style='font-weight: bold;font-size: 15px'>";
		var columnsList = opts.json.query_result.columns;

		$.each(columnsList, function(c, map) {
			if(!map.hide) {
				var _name = opts.fileds[map.rec_filed]['NAME'];
				var _width = map.width;
				sHtml += ("<td width='" + _width + "' valign='top' align='center'>" + _name + "</td>");
			}
		});
		sHtml += "</tr><tbody id='detaillist'>";
		$.each(this.resultPage.list, function(i, entry) {
			sHtml += "<tr>";
			$.each(columnsList, function(c, map) {
				if(!map.hide) {
					var value = entry[map.fd_name];
					if(!IsEmpty(value)) {
						value = '';
					}
					sHtml += "<td valign='top' align='center'>" + value + "</td>";
				}
			});
			sHtml += "</tr>";
		});
		sHtml += "</tbody></table>";
		return sHtml;
	}

	function _generateNoWireList() {
		var opts = $detail.opts;
		var sHtml = '<table cellspacing="0" cellpadding="0" border="0" align="left" style="width:100%" id="treeTable" class="tree_table">';
		sHtml += '<thead id="thead"><tr>';
		var columnsList = opts.json.query_result.columns;
		$.each(columnsList, function(c, map) {
			if(!map.hide) {
				var _name = opts.fileds[map.rec_filed]['NAME'];
				var _width = map.width;
				sHtml += ("<th width='" + _width + "' valign='top' align='center'>" + _name + "</td>");
			}
		});
		sHtml += '</tr></thead><tbody>';
		$.each(this.resultPage.list, function(i, entry) {
			sHtml += "<tr>";
			$.each(columnsList, function(c, map) {
				if(!map.hide) {
					var value = entry[map.fd_name];
					if(!IsEmpty(value)) {
						value = '';
					}
					sHtml += "<td valign='top' align='center'>" + value + "</td>";
				}
			});
			sHtml += "</tr>";
		});
		sHtml += '</tbody></table>';
		return sHtml;
	}

	/**
	 * 生成明细
	 */
	function _generateDetail() {
		var opts = $detail.opts;
		var sHtml = '';
		sHtml += "<ul class='list-box'>";
		var columnsList = opts.json.query_result.columns;
		$.each(this.resultPage.list, function(i, entry) {
			$.each(columnsList, function(key, map) {
				if(!map.hide) {
					var _name = opts.fileds[map.rec_filed]['NAME'];
					var _dataIndex = map.fd_name;
					var value = entry[_dataIndex];
					if(!IsEmpty(value)) {
						value = '';
					}
					sHtml += "<li class='list-box-item'>";
					sHtml += "<label class='list-box-item-label'>" + _name + ":</label>";
					sHtml += "<span class='list-box-item-content'><div id='" + _dataIndex + "' style='font-weight: bold'>" + value + "</div></span>";
					sHtml +="</li>";
				}
			});
		});
		sHtml +="</ul>";
		return sHtml;
	}

	function init() {
		var opts = $detail.opts;
		jcl.postJSON('/tms35/query/result',opts.params, function(data) {
			this.resultPage = data.page;
			var html;
			if(IsEmpty(this.resultPage.list) && this.resultPage.list.length > 0) {
				var ds_callback = opts.json.query_result.ds_callback;
				jcq.customquery.callBackResults(this.resultPage.list, opts.json.query_result.columns, opts.fileds, ds_callback);//查询结果处理
				var display_type = opts.json.query_result.display_type;//显示类型
				if(display_type == 'list') {
					_generateList();
				} else {
					if(display_type == 'wirelist') {
						html = _generateWireList();
					} else if(display_type == 'nowirelist') {
						html = _generateNoWireList();
					} else if(display_type == 'detail') {
						if(this.resultPage.list.length == 1) {
							html = _generateDetail();
						} else {
							_generateList();
						}
					}
				}

				var queryScript = opts.json.query_script;
				if(queryScript && queryScript.ready_script) {
					eval(queryScript.ready_script);//加载执行设置函数
				}
			} else {
				html = '<div align="center"><b>没有信息记录</b></div>';
			}

			if(html) {
				$(html).appendTo($detail.parent);
			}
		});
	}

	init();
};