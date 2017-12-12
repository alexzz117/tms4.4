/**
 * 自定义查询
 */
var jcq = typeof(jcq) === 'undefined' ? {} : jcq;

//创建命名空间
jcl.ns = jcl.namespace;
jcl.ns("jcq.view");

/**
 * 自定义查询页面模型加载
 * @param options		字符串参数
 * @param container		{entity:document|jcl.ui.Dialog, element:''} 容器对象{实体, 实体中的元素}
 */
jcq.loadQuery = function(options, container){
	var _self = this;
	this.opts = $.extend({
		id:null,
		params:null,//对象
		newView:true,//是否新页面
		dualAudit:false//
	}, options);
	
	/**
	 * QUERY:普通查询列表
	 * ENTITY:实体查询
	 */
	var dispatcherQuery = {QUERY:'jcq.view.GeneralQuery', ENTITY:'jcq.view.EntityQuery'};
	
	this.jqDom = (function(opts){
		if(container){
			if(container.entity){
				if(container.entity == (window || document)){
					_self.parentContainer = container.element ? $(document).find(container.element) : $('body');
				}else if(container.entity instanceof jcl.ui.Dialog){
					_self.parentContainer = container.element ? (container.entity).jqDom.find(container.element) : (container.entity).box.container;
				}else{
					_self.parentContainer = container.element ? $(container.entity).find(container.element) : $('body');
				}
			}else{
				_self.parentContainer = container.element ? $(document).find(container.element) : $('body');
			}
		}else{
			_self.parentContainer = $('body');
		}
		
		var jqDom = opts.id ? $('#' + opts.id) : null;
		if(!jqDom || jqDom.length == 0){
			var htmlstr = '<div class="custom_query"></div>';
			jqDom = $(htmlstr).appendTo(_self.parentContainer);
		}else{
			jqDom.addClass('custom_query');
		}
		
		if(opts.params){
			if(typeof(opts.params) == 'string'){
				opts.params = jcq.util.paramToObject(opts.params);
			}
		}
		
		_loadQuery(jqDom);
		return jqDom;
	})(this.opts);
	
	function _loadQuery(jqDom){
		var opts = _self.opts;
		var params = opts.params;
		jcl.postJSON(jcq.common.reSetUrl(opts.dualAudit, '/tms35/query/show'), jcq.util.objToParam(params), function(data){
			if(typeof(JSON) != 'undefined' && JSON.parse){
				_initQuery(data, jqDom);
			}else{
				$.getScript(jcl.env.contextPath+'/s/tms35/js/json2.js', function(){
					_initQuery(data, jqDom);
				});
			}
		});
	}
	
	function _initQuery(data, jqDom){
		var custom = data.custom;
		//document.title = custom.pageTitle;//设置页面titile
		if(container && container.entity){
			if(container.entity == (window || document)){
				document.title = custom.pageTitle;
			}else if(container.entity instanceof jcl.ui.Dialog){
				container.entity.setTitle(custom.pageTitle);
			}
		}else{
			if(_self.opts.newView){
				document.title = custom.pageTitle;
			}
		}
		
		if(_self.opts.newView && custom.direct == 'ENTITY'){
			var hasBackButton = jcq.common.findedObjForParentWindow(window.self, '#customQueryBack:visible');
			if(!hasBackButton){
				$('#customQueryBack').show();
			}
		}

		var options = $.extend({}, _self.opts, data);
		if(custom.script && custom.script.includes) _includeJS(custom.script.includes);//js 外部引用
		
		if((custom.cond && custom.cond.callcheck)
				|| (custom.result && custom.result.callback)
					|| (custom.script && custom.script.bindEvent)){//包含自定义的内部js代码
			var unique = data.unique;//唯一标示符
			$.getScript(jcl.env.contextPath + '/tms35/query/jcqView?' + [paramStr, 'unique='+unique].join('&'), function(){
				var func = jcq.util.string2Function(dispatcherQuery[custom.direct] + unique);
				_self.query = new func(options, jqDom);
			});
		}else{
			var func = jcq.util.string2Function(dispatcherQuery[custom.direct]);
			if(!func){
				$.getScript(jcl.env.contextPath+'/s/tms35/js/query/jcq.fix.view.js', function(){
					func = jcq.util.string2Function(dispatcherQuery[custom.direct]);
					_self.query = new func(options, jqDom);
				});
			}else{
				_self.query = new func(options, jqDom);
			}
		}
		_resizes(jqDom);
	}
	
	/**
	 * 引入外部js或代码
	 */
	function _includeJS(includes){
		if(includes){
			$.each(includes, function(i, include){
				jcq.util.includeJs(include.type, include.source);
			});
		}
	}
	
	function _resizes(jqDom){
		$(window).on('resizes', function(){
			jqDom.triggerHandler('resizes');
		});
	}
};

/**
 * 自定义查询工具集
 */
jcq.util = {
	isEmpty:function(str){
		if(str == null || str == 'null' || str == '' || $.trim(str) == '' || str == undefined || str == 'undefined'){
			return true;
		}else{
			return false;
		}
	},
	/**
	 * 取有效字段名
	 */
	getValidFdName:function(fdname){
		if(fdname.indexOf('.')!=-1){
			return fdname.substring(fdname.indexOf('.')+1, fdname.length);
		}else{
			return fdname;
		}
	},
	/**
	 * 替换字符
	 * @param str 要操作的字符串
	 * @param reallyDo 被搜索的子字符串
	 * @param replaceWith 用于替换的子字符串
	 * @param ignoreCase  是否区分大小写
	 */
	replaceAll:function(str, reallyDo, replaceWith, ignoreCase){
		if (!RegExp.prototype.isPrototypeOf(reallyDo)) {
	        return str.replace(new RegExp(reallyDo, (ignoreCase ? "gi": "g")), replaceWith);
	    } else {
	        return str.replace(reallyDo, replaceWith);
	    }
	},
	/**
	 * 字段串转换成函数
	 * @param string
	 * @returns
	 */
	string2Function:function(string){
		var func = (new Function("return " + string))();
		return func;
	},
	/**
	 * 引入js
	 * @param type		数据源类型，FILE:文件，CODE:代码
	 * @param source	数据源
	 */
	includeJs:function(type, source){
		if(type == "FILE"){//引入文件
			source = (source.toUpperCase().indexOf('HTTP')!=-1) ? source : ([jcl.env.contextPath, source].join(''));
		}
		jcq.util.wirteJsFile(("jcq_" + new Date().getTime()), type, source);
	},
	/**
	 * 写js文件到页面中
	 * @param sId		元素id	
	 * @param type		数据源类型FILE:文件，CODE:代码，默认为CODE
	 * @param source	数据源
	 */
	wirteJsFile:function(sId, type, source){
		if ((source !== null) && (!document.getElementById(sId))) {
	        var oHead = document.getElementsByTagName('HEAD').item(0);
	        var oScript = document.createElement("script");
	        oScript.language = "javascript";
	        oScript.type = "text/javascript";
	        oScript.id = sId;
	        oScript.defer = true;
	        if(type){
	        	if(type == "FILE"){
	        		oScript.src = source;
	        	}else if(type == "CODE"){
	        		oScript.text = source;
	        	}
	        }else{
	        	oScript.text = source;
	        }
	        oHead.appendChild(oScript);
	    }
	},
	/**
	 * 获取页面url参数字符串
	 * @returns
	 */
	getUrlParamsString:function(uri){
		var url = uri ? uri : window.location.href;
		var paramStr = url.substring(url.indexOf("?")+1,url.length);
		return paramStr;
	},
	/**
	 * 序列化的参数转换成对象
	 * @param paramStr
	 */
	paramToObject:function(param){
		if(!param) return null;
		var paramObj = {};
		if(typeof(param) == 'string' || $.isArray(param)){
			var params = (typeof(param) == 'string') ? param.split('\&') : param;
			if(!params || params.length == 0) return null;
			
			for(var i=0;i<params.length;i++){
				var param = params[i];
				var key = (typeof(param) == 'string') ? param.substring(0,param.indexOf("=")) : param.name;
				var value = (typeof(param) == 'string') ? param.substring(param.indexOf("=")+1,param.length) : param.value;
				if(paramObj[key]){
					if($.isArray(paramObj[key])){
						paramObj[key].push(value);
					}else{
						paramObj[key] = [paramObj[key], value];
					}
				}else{
					paramObj[key] = value;
				}
			}
		}else{
			return param;
		}
		return paramObj;
	},
	/**
	 * 对象转成序列化参数字符串
	 * @param obj		参数对象
	 * @param enable 	是否有效
	 */
	objToParam:function(obj, enable){
		if(!obj) return obj;
		if(typeof(obj) != 'object') return obj;
		
		var params = [];
		$.each(obj, function(key, value){
			if (enable && jcq.util.isEmpty(value)) {
				
			} else {
				if($.isArray(value)){
					for(var i=0;i<value.length;i++){
						var val = value[i];
						if (enable && jcq.util.isEmpty(val)) {
							
						} else {
							params.push(key+'='+val);
						}
					}
				}else{
					params.push(key+'='+value);
				}
			}
		});
		return params.join('&');
	},
	/**
	 * 更新参数对象
	 * @param param	参数对象(对象或字符串)
	 * @param name	更新参数名
	 * @param value	更新参数值
	 */
	paramPut:function(param, name, value){
		if(!param) return param;
		var paramObj = null;
		if(typeof(param) == 'string'){
			paramObj = jcq.util.paramToObject(param);
			paramObj[name] = value;
			return jcq.util.objToParam(paramObj);
		}else if(typeof(param) == 'object'){
			paramObj = $.extend({}, param);
			paramObj[name] = value;
		}else{
			paramObj = param;
		}
		return paramObj;
	},
	/**
	 * 删除参数对象
	 * @param name	参数名
	 * @param param	参数对象
	 */
	paramRemove:function(name, param){
		if(!param) return param;
		var paramObj = null;
		if(typeof(param) == 'string'){
			paramObj = jcq.util.paramToObject(param);
			delete paramObj[name];
			return jcq.util.objToParam(paramObj);
		}else if(typeof(param) == 'object'){
			paramObj = $.extend({}, param);
			delete paramObj[name];
		}else{
			paramObj = param;
		}
		return paramObj;
	},
	convert:function(value, script){
		if(typeof(script) == 'function'){
			return jcl.convert(value, script);
		}else{
			return jcl.convert(value, jcq.util.string2Function(script+''));
		}
	},
	/*
	 * 校验特殊字符
	 * type 1 
	 * 代码:字母开头 + 字母,数字,下划线(位置无区别)
	 * type 2
	 * 名称:汉字,字母,数字,下划线,!,-(位置无区别)
	 */
	checkSpecialCharacter:function(src, type) {
		return type == '1' 
			? /^[a-zA-Z][\w]*$/.test(src) 
			: /^[\w\!\-\u4e00-\u9fa5]*$/.test(src);
	},
	checkIpAddr:function(ipAddr) {
		var reSpaceCheck = /^(\d+)\.(\d+)\.(\d+)\.(\d+)$/;
	    if(reSpaceCheck.test(ipAddr)){
	    	ipAddr.match(reSpaceCheck); 
	        if (RegExp.$1<=255&&RegExp.$1>=0
	          &&RegExp.$2<=255&&RegExp.$2>=0
	          &&RegExp.$3<=255&&RegExp.$3>=0
	          &&RegExp.$4<=255&&RegExp.$4>=0)
	        {
	            return true;
	        }else{  
	            return false;  
	        } 
	    }else{
	    	return false;  
	    }
	},
	checkNumber:function(src) {
		var reg=/^[-\+]?\d+(\.\d+)?$/;
	    if(reg.test(src)){
	    	return true;
	    }else{
	    	return false;  
	    }
	},
	checkLong:function(src) {
		var reg=/^(0|[1-9][0-9]*)$/;
	    if(reg.test(src)){
	    	return true;
	    }else{
	    	return false;  
	    }
	}
};

/**
 * jcq中公共方法
 */
jcq.common = {
	/**
	 * 构建grid中的columns数据项
	 * @param columns json中的result.columns节点数据
	 * @param fields  字段集合
	 * @returns {Array}
	 */
	buildGridColumns:function(columns, fields, dualAudit){
		var _columns = [];
		$.each(columns, function(i, column) {
			if(column.show) {
				var fdName = column.fdName;
				var field = fields[fdName];
				var name = column.name, dataIndex = jcq.util.getValidFdName(column.csName);
				var width = (column.width === 0 ? name.length*13 : column.width);
				var _column = {name:name, width:width, dataIndex:dataIndex, render:function(value){
					var tms_type = field['TYPE'], link = field['LINK'];
					value = (column.handle && column.handle.script) ? jcq.util.convert(value, column.handle.script) : ((tms_type && tms_type.toUpperCase() == 'CODE') ? jcl.code.get(field['CODE'], value) : ((tms_type && tms_type.toUpperCase() == 'DATETIME') ? jcl.util.convert['datetime'](value) : value)) + "";
					if(link){
						link = jcq.common.reSetUrl(dualAudit, link);
						value = '<a href="javascript:void(0);" type="'+column.open+'" url="'+link+'">' + value + '</a>';
					}
					return value;
				}};
				_columns.push(_column);
			}
		});
		return _columns;
	},
	/**
	 * 查询数据的回调处理
	 * @param list		查询的数据
	 * @param columns	json中的result.columns节点数据
	 * @param fields	字段集合
	 * @returns
	 */
	resultCallBack:function(list, columns, fields, dualAudit, url_params){
		for(var i = 0; i < list.length; i++) {
			var params = null;
			var row = list[i];
			var oldrow = $.extend({}, row);
			$.each(columns, function(c, column) {
				var filed = fields[column.fdName];
				if (filed != null) {
					var dataIndex = jcq.util.getValidFdName(column.csName);
					var value = oldrow[dataIndex];
					if(value != null) {
						var tms_type = filed['TYPE'];
						value = (column.handle && column.handle.script) ? jcq.util.convert(value, column.handle.script) : ((tms_type && tms_type.toUpperCase() == 'CODE') ? jcl.code.get(filed['CODE'], value) : ((tms_type && tms_type.toUpperCase() == 'DATETIME') ? jcl.util.convert['datetime'](value) : value)) + "";
						if(value.indexOf('<a') != -1 || value.indexOf('<A') != -1) {//超链接
							row[dataIndex] = value;
						} else {
							var link = filed['LINK'];
							if(link) {
								var hasLinkParam = false, hasParams = false;
								if(link.indexOf('{') != -1 && link.indexOf('}') != -1) {//自己配置的数据
									hasLinkParam = true;
								}else{
									hasParams = params ? true : false;
									if(!hasParams) params = [];
								}
								if(hasLinkParam){
									$.each(oldrow, function(key, value){
										link = jcq.util.replaceAll(link, '\\$\\{'+key+'\\}', value, true);
									});
								}else{
									
								}
								$.each(oldrow, function(key, value){
									if(hasLinkParam){
										link = jcq.util.replaceAll(link, '\\$\\{'+key+'\\}', value, true);
									}else{
										if(!hasParams){
											$.each(columns, function(_c, _column){
												var _filed = fields[_column.fdName];
												var is_key = _filed['IS_KEY'];
												if(is_key*1 == 1) {//主键
													var _dataIndex = jcq.util.getValidFdName(_column.csName);
													params.push(_dataIndex.toLocaleLowerCase()+"="+oldrow[_dataIndex]);
												}
											});
										}
									}
								});
								if(hasLinkParam){
									$.each(url_params, function(key, value){
										link = jcq.util.replaceAll(link, '\\$\\{'+key+'\\}', value, true);
									});
								}else{
									link += (params ? "&" + params.join('&') : "");
								}
								link = jcq.common.reSetUrl(dualAudit, link);
								row[dataIndex] = '<a href="javascript:void(0);" type="'+column.open+'" url="'+link+'">' + value + '</a>';
							}else{
								row[dataIndex] = value;
							}
						}
					}
				}
			});
		}
		return list;
	},
	resultLink:function(link, row, columns, fields){
		if(link) {
			var hasLinkParam = false, hasParams = false, params = null;
			if(link.indexOf('{') != -1 && link.indexOf('}') != -1) {//自己配置的数据
				hasLinkParam = true;
			}else{
				hasParams = params ? true : false;
				if(!hasParams) params = [];
			}
			$.each(row, function(key, value){
				if(hasLinkParam){
					link = jcq.util.replaceAll(link, '\\$\\{'+key+'\\}', (jcq.util.isEmpty(value) ? "" : value), true);
				}else if(!hasParams){
					if(columns && fields){
						$.each(columns, function(_c, _column){
							var _filed = fields[_column.fdName];
							var is_key = _filed['IS_KEY'];
							if(is_key*1 == 1) {//主键
								var _dataIndex = jcq.util.getValidFdName(_column.csName);
								var _value = row[_dataIndex];
								params.push(_dataIndex.toLocaleLowerCase()+"="+(jcq.util.isEmpty(_value) ? "" : _value));
							}
						});
					}
				}
			});
			if(!hasLinkParam){
				link += ((params && params.length > 0) ? ((link.indexOf("?") != -1 ? "&" : "?") + params.join('&')) : "");
			}
		}
		return link;
	},
	/**
	 * 绑定链接事件
	 * @param obj
	 * @param jqDom
	 */
	bindLinkEvent:function(obj, jqDom){
		jqDom.on({
			'click':function(){
				var open = $(this).attr('type');
				var url = $(this).attr('url');
				if (jcq.util.isEmpty(open) || jcq.util.isEmpty(url)) {
					return;
				}
				var qform = null;
				if(url.indexOf('${') != -1 && url.indexOf('}') != -1) {
					var row = null, page_row = $(this).closest('div.row');
					if(page_row.length > 0){
						var index = jqDom.find('div.row').index(page_row);
						if(obj.grid){
							row = obj.grid.table.page.list[index];
							qform = obj.grid.qform;
						}else if(obj.page){
							row = obj.page.list[index];
						}
					}else{
						row = obj.page.list[0];
					}
					url = jcq.common.resultLink(url, row, obj.opts.custom.result.columns, obj.opts.fields);
				}
				if (qform) {
					var queryParam = qform.jqDom.serializeArray();
					url = jcq.common.resultLink(url, queryParam, null, null);
				}
				if(open == 'POP_LAYER'){//弹出层
					if(!obj.dialog){
						var zindex = jcq.common.getIndex(jqDom);
						obj.dialog = new jcl.ui.Dialog({zindex:(zindex+1), draggable:true, width:1000});
					}else{
						obj.dialog.box.container.empty();
					}
					new jcq.loadQuery({params:jcq.util.getUrlParamsString(url), newView:false}, {entity:obj.dialog});
					obj.dialog.show();
				}else if(open == 'POP_PAGE'){//弹出页面
					if(navigator.userAgent.indexOf("Chrome") > 0){
						var winOption = 'width=1000,height=600,resizable=yes';
						window.open(jcl.env.contextPath + url, '', winOption);
					}else{
						window.showModalDialog(jcl.env.contextPath + url,'','dialogWidth=1000px;dialogHeight=600px;resizable=yes');
					}
				}else if(open == 'EMBED_LAYER'){//内嵌层
					
				}else if(open == 'EMBED_PAGE'){//内嵌页面
					
				}
			}
		}, 'a');
	},
	getIndex:function(jqDom){
		var zindex = jqDom.css('z-index');
		//需要向上递归，取到非零值
		if(!isNaN(zindex) && zindex>0){
			return zindex;
		}
		//如果到达顶层，就返回0
		if(jqDom.is('body,html')){
			return 0;
		}else{
			return jcq.common.getIndex(jqDom.parent());
		}
	},
	/**
	 * 通过当前页面查找父窗口中的元素
	 * @param self当前窗口的jquery对象window|self
	 * @param 查找元素的jquery表达式
	 * @return {boolean}
	 **/
	findedObjForParentWindow:function(self, findObj) {
		//var _self = $(self);
		var _parent = $(self.parent);//获取父窗口
		if(self.location.href == self.parent.location.href) {
			return false;
		}
		if(_parent.length > 0) {//是否存在父窗口
			var back = $(_parent[0].document).find(findObj);//在父窗口的dom中查找返回按钮
			if(back.length > 0) {
				return true;
			} else {
				return jcq.common.findedObjForParentWindow(_parent[0], findObj);
			}
		} else {
			return false;
		}
	},
	autoSiteIframeHeight:function(newView, paramStr, dualAudit){
		jcq.common.autoSiteIframeHeightForNum(newView, paramStr, dualAudit, 2);
	},
	/**
	 * 自动设置父页面的iframe的高度
	 * @param paramStr	参数字符串
	 */
	autoSiteIframeHeightForNum:function(newView, paramStr, dualAudit, setNum){
		if(newView && setNum > 0){
			var url = jcl.env.contextPath + jcq.common.reSetUrl(dualAudit, '/tms35/query/show?') + paramStr;
			var iframes = $(window.parent.document).find('iframe:visible');
			$.each(iframes, function(i, iframe){
				var _src = $(iframe).attr('src') + '';
				var _prm = _src.substring(_src.indexOf('?') + 1, _src.length);
				var _id = $(iframe).attr('id') + '';
				if((_src && _id) && _prm == paramStr && _id.indexOf('query_iframe') > -1){
					var thisHeight = $('body').outerHeight(true);
					$(iframe).height(thisHeight+50);
				}
				/*if((_src && _id) && _src == url && _id.indexOf('query_iframe') > -1){
					var thisHeight = $('body').outerHeight(true);
					$(iframe).height(thisHeight);
				}*/
			});
			jcq.common.autoSiteIframeHeightForNum(newView, paramStr, dualAudit, setNum - 1);
		}
	},
	reSetUrl:function(dualAudit, url){
		if (dualAudit || dualAudit == 'true') {
			return '/dualaudit' + url;
		}
		return url;
	}
};

jcq.ui = {
	Text : function(options, container){
		var _self = this;
		this.opts = $.extend({
			name: '',
			value: ''
		}, options);
		
		this.jqDom = (function(opts){
			var htmlstr = '<input name="'+opts.name+'" type="text" value="'+opts.value+'" class="itext'+(opts.cls ? ' '+opts.cls : '')+'" />';
			var jqDom = $(htmlstr).appendTo(container);
			return jqDom;
		})(this.opts);
		
		this.disable = function(){
			this.jqDom.children('input').prop('disabled', true);
		};
		
		this.enable = function(){
			this.jqDom.children('input').prop('disabled', false);
		};
	},
	buildItemFunc : function(type){
		var defaultNameSpace = ["jcl.ui", "jcq.ui"];
		var defaultType = {
			'text': jcq.ui.Text,
			'selector': jcl.ui.Selector,
			'date': jcl.ui.DateSelector,
			'listselector': jcl.ui.ListSelector,
			'treeselector': jcl.ui.TreeSelector
		};
		function getCustomType(type) {
			var func = null;
			if(type.indexOf('.') == -1){//无命名空间前缀
				for(var n=0;n<defaultNameSpace.length;n++){
					var fullname = defaultNameSpace[n] + '.' + type;
					func = getTypeFunc(fullname);
					if(func){
						break;
					}
				}
			}else{
				func = getTypeFunc(type);
			}
			return func;
		};
		function getTypeFunc(type){
			var arr = type.split('\.');
			var fn = window;
		    for(var n=0,len=arr.length;n<len;n++){
		        if(fn[arr[n]]){
		            fn = fn[arr[n]];
		        }else{
		            fn = null;
		            break;
		        }
		    }
		    if(fn === window || fn === null){
		        return undefined;
		    }
		    return fn;
		};
		function _init(type){
			if(defaultType[type]){
				return defaultType[type];
			}else if(getCustomType(type)){
				return getCustomType(type);
			}else{
				return null;
			}
		}
		return _init(type);
	},
	formItem:{
		CustomQueryItem : function(options, container){
			var _self = this;
			var _components = [];
			
			this.opts = $.extend({
				name: '',
				separator:'',
				editSeparator: null,
				viewSeparator: null,
				valSeparator: '|',
				items: null
			}, options);
			
			this.jqDom = (function(opts){
				_self.item = new jcl.ui.FormItem(opts, container);
				var jqDom = _self.item.jqDom;
				if(opts.editSeparator == null){
					opts.editSeparator = opts.separator;
				}
				if(opts.viewSeparator == null){
					opts.viewSeparator = opts.separator;
				}
				_initComponents();
				return jqDom;
			})(this.opts);
			
			this.getText = function(){
				var textArray = [];
				$.each(_components, function(i, comp){
					var _component = (comp instanceof jQuery ? comp : comp.jqDom);
					if(comp instanceof jcq.ui.Text){
						textArray.push(_component.find(':text').val());
					}else if(comp instanceof (jcl.ui.Selector || jcl.ui.DateSelector || jcl.ui.ListSelector || jcl.ui.TreeSelector)){
						var _val = _component.find('[name='+comp.opts.name+']').val();
						if(!jcq.util.isEmpty(_val)){
							textArray.push(_component.find(':text[readonly]').val());
						}
					}
				});
				return textArray.join(this.opts.viewSeparator);
			};
			
			this.val = function(value){
				var opts = this.opts;
				var valArray = [];
				if(value == null){
					$.each(_components, function(i, comp){
						var _component = (comp instanceof jQuery ? comp : comp.jqDom);
						var _val = null;
						if(_component.attr('name') == comp.opts.name){
							_val = _component.val();
						} else {
							_val = _component.find('[name='+comp.opts.name+']').val();
						}
						valArray.push(jcq.util.isEmpty(_val) ? '' : _val);
					});
					return valArray.join(opts.valSeparator);
				}else{
					if(value instanceof Array){
						valArray = value;
					}else if(typeof(value) == 'string'){
						valArray = value.split(opts.valSeparator);
					}
					if(valArray.length == _components.length){
						$.each(_components, function(i, comp){
							var _component = (comp instanceof jQuery ? comp : comp.jqDom);
							if(comp instanceof jcq.ui.Text){
								_component.find('[name='+comp.opts.name+']').val(valArray[i]);
							}else if(comp instanceof jcl.ui.DateSelector){
								_component.find('[name='+comp.opts.name+']').val(valArray[i]);
							}else if(comp instanceof jcl.ui.Selector){
								comp.select(valArray[i]);
							}else if(comp instanceof jcl.ui.ListSelector){
								comp.render(valArray[i]);
							}else if(comp instanceof jcl.ui.TreeSelector){
								comp.render(valArray[i]);
							}
						});
					}
					if(this.item.view){
						this.item.view.text(this.getText());
					}
				}
			};
			
			this.disable = function(showable){
				this.item.disable(showable);
				$.each(_components, function(i, comp){
					comp.disable();
				});
			};
			
			this.enable = function(){
				$.each(_components, function(i, comp){
					comp.enable();
				});
				this.item.enable();
			};
			
			this.reset = function(){
				var opts = this.opts;
				if(opts.value){
					this.val(opts.value);
				}
			};
			this.viewMode = function(){
				this.item.viewMode(this.getText());
			};
			this.editMode = function(){
				this.item.editMode();
			};
			
			function _initComponents(){
				var opts = _self.opts;
				if(opts.items){
					var items = opts.items;
					for(var i=0;i<items.length;i++){
						var item = items[i];
						if(!item.name){
							item.name = opts.name;
						}
						var func = jcq.ui.buildItemFunc(item.type);
						_components.push(new func(item, _self.item.content));
						_components[i].jqDom.css('display', 'inline-block');
						if(opts.editSeparator){
							if(i+1 < items.length){
								_self.item.content.append(opts.editSeparator);
							}
						}
					}
				}
			};
			
			function _getComponentIndex(name){
				var items = _self.opts.items;
				for(var i = 0, len = items.length; i < len; i++){
					if(items[i].name == name){
						return i;
					}
				}
				return -1;
			};
			
			this.getComponent = function(name){
				var index = typeof(name) == 'number' ? name : _getComponentIndex(name);
				if(index < 0 || index >= _components.length){
					return null;
				}
				return _components[index];
			};
		}
	}
};