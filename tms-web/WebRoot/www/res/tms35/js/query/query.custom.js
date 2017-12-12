/**
 * 组织自定义查询页面
 **/
var jcq = typeof(jcq) === 'undefined' ? {} : jcq;

(function(){
	jcq.customquery = {
		_defaults : {
			id : null,
			title: null,
			json : null,
			fileds : null,
			params : null
		},
		/**
		 * @param params		访问参数
		 * @param container		上级jquery元素
		 **/
		loadQuery : function(params, container) {
			jcl.postJSON('/tms35/query/show',params, function(data) {
				var title = IsEmpty(data.jsonVO.query_title) ? data.jsonVO.query_title : data.queryDesc;
				document.title = title;//设置页面titile
				var options = requests();
				options['id'] = 'queryMainDiv';
				options['title'] = title;
				options['json'] = data.jsonVO;
				options['fileds'] = data.filedsMap;
				options['params'] = params;
				init(options, container);

				var query_type = data.jsonVO.query_type;
				if(query_type == '2') {
					var hasBackButton = findParentWindowObj(window);
					if(!hasBackButton) {
						$('#queryBackDiv').show();
					}
				}
				var queryScript = data.jsonVO.query_script;
				if(IsEmpty(queryScript) && IsEmpty(queryScript.call_script)) {
					includeJS('_callscript' + new Date().getTime(), queryScript.call_script);
				}
			});
		},
		callBackResults : function(resultsList, resultConfigList, filedsMap, ds_callback) {
			for(var i = 0; i < resultsList.length; i++) {
				var allParams = [];
				var resultDataMap = resultsList[i];
				var resultDataMapBak = clone(resultDataMap);
				$.each(resultConfigList, function(c, map) {
					var rec_filed = map.rec_filed;
					var filed = filedsMap[rec_filed];
					var tmsType = filed['TYPE'];
					var code = filed['CODE'];
					var link = filed['LINK'];
					var dataIndex = map.fd_name;
					var handle = map.handle;
					var value = resultDataMapBak[dataIndex];
					if(IsEmpty(value)) {
						value = (IsEmpty(handle) && IsEmpty(handle.script) ? jcl.convert(value, handle.script) : (tmsType == "code" ? jcl.code.get(code, value) : value)) + "";
						if(value.indexOf('<a') != -1 || value.indexOf('<A') != -1) {
							resultDataMap[dataIndex] = value;
						} else {
							resultDataMap[dataIndex] = value;
							if(IsEmpty(link)) {
								var open_link = map.open_link;
								if(link.indexOf('{') != -1 && link.indexOf('}') != -1) {//自己配置的数据
									while(link.indexOf('{') != -1 && link.indexOf('}') != -1) {
										var paramName = link.substring(link.indexOf('{')+1, link.indexOf('}'));
										link = link.substring(0, link.indexOf('{')) + resultDataMapBak[paramName] + link.substring(link.indexOf('}')+1, link.length);
									}
								} else {//全部
									if(!IsEmpty(allParams)) {
										$.each(resultConfigList, function(_c, _map) {
											var _rec_filed = _map.rec_filed;
											var _filed = filedsMap[_rec_filed];
											var is_key = _filed['IS_KEY'];
											var is_query = _filed['IS_QUERY'];
											if(is_key*1 == 1 || is_query*1 == 1) {//主键或可作为查询字段
												var _dataIndex = _map.fd_name;
												allParams.push(_dataIndex.toLocaleLowerCase()+"="+resultDataMapBak[_dataIndex]);
											}
										});
									}
									link += (IsEmpty(allParams) ? ('&' + allParams.join('&')) : '');
								}
								resultDataMap[dataIndex] = '<a href="javascript:void(0);" onclick="jcq.customquery.openLink(this,\''+open_link+'\',\''+link+'\')">' + value + '</a>';
							}
						}
					}
				});
			}
			if(IsEmpty(ds_callback)) {
				jcl.convert(resultsList, ds_callback);
			}
			return resultsList;
		},
		openLink : function(obj, type, url) {
			url = contextPath + '' + url;
			var params = requestsSplit(url, '&');
			params = annexParams(params, requests());
			url = url.substring(0, url.indexOf('?')+1) + formatParamsObj(params, '&');
			if(type*1 === 0) {
				popOpenQueryDetail(url);
			} else if(type*1 === 1) {
				belowOpenQueryDetail(obj, url);
			}
		}
	};

	/**
	 * 初始化页面数据项
	 * @param options 参数项
	 * @param container   父级jquery对象
	 **/
	function init(options, container) {
		var opts = $.extend({}, jcq.customquery._defaults, options);
		var vid = opts.id ? opts.id : ('query_custom_' + new Date().getTime());
		var jqObj = opts.id ? $('#' + opts.id) : null;
		if(!jqObj || jqObj.length === 0) {
			jqObj = $('<div id="' + vid + '"></div>').appendTo(container || $('body'));
		}
		var query_type = opts.json.query_type;
		var _options = $.extend({}, opts, {id: null});
		if(query_type == '1') {
			$.getScript(contextPath+'/s/tms35/js/query/query.list.js', function(){
				new jcq.view.QueryGrid(_options, $('#' + vid));
			});
		} else if(query_type == '2') {
			$.getScript(contextPath+'/s/tms35/js/query/query.entity.js', function(){
				new jcq.view.QueryEntity(_options, $('#' + vid));
			});
		}
	}

	/**
	 * 对象克隆
	 * @param myObj
	 * @returns
	 */
	function clone(myObj){
		if(typeof(myObj) != 'object') {
			return myObj;
		}
		if(myObj === null) {
			return myObj;
		}
		var myNewObj = new Object();
		for(var i in myObj) {
			myNewObj[i] = clone(myObj[i]);
		}
		return myNewObj;
	}

	/**
	 * 弹出页面
	 * @param url
	 **/
	function popOpenQueryDetail(url){
		window.showModalDialog(url,'','dialogWidth=1000px;dialogHeight=600px;resizable=yes');
	}

	/**
	 * 当前页面下方显示
	 * @param
	 *
	 **/
	function belowOpenQueryDetail(obj, url) {
		var params = requestsSplit(url, '&');
		var query_id = params['queryId'];
		var iframeHtml = "<iframe id='" + query_id + "Iframe' name='_queryBellowIframe' src='" + url + "' frameborder='0' scrolling='no' marginwidth='0' marginheight='0' width='100%'></iframe>";
		var iframe = $(window.document).find('[name="_queryBellowIframe"]');
		if(iframe.length > 0) {
			iframe.attr('id', query_id+'Iframe');
			iframe.attr('src', url);
		} else {
			$(obj).closest('#queryMainDiv').after('<div id="_queryBellowIframe">' + iframeHtml + '</div>');
		}
	}
})();