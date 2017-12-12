model = window.model || {
	ui:{
		formItem:{
			/**
			 * 处理函数FormItem实现
			 * @param options		配置数据项
			 * @param container     父层元素
			 */
			HandlerFunction:function(options, container){
				var _self = this;
				var _items = [];
				var _loaded = false;
				this.opts = $.extend({
					name: '',
					value: '',
					title: null,
					items: [],
					dataTypeFilter: false,
					selfCharacter: '?'//当前值的代表字符
				}, options);
				this.jqDom = (function(opts){
					_self.parentContainer = container;
					_self.handler = new jcl.ui.formItem.Selector($.extend({}, opts, {ds:null, items:[]}), _self.parentContainer);
					_self.item = _self.handler.item;
					var jqDom = _self.item.jqDom;
					_self.component = _self.handler.component;
					if(opts.ds){
						if(opts.ds.type == 'remote' && !opts.ds.lazyLoad){
							_loadList(jqDom);
						}
					}else{
						_initList(jqDom);
					}
					_bindEvent(jqDom);
					return jqDom;
				})(this.opts);
				this.getText = function(){
					return '';
				};
				this.val = function(value){
					var _value = null, func = null, funcParams = ['?'];
					if(value == undefined){
						func = this.handler.val();
						$.each(_items, function(i, _item){
							funcParams.push(_item.val());
						});
						_value = func ? (func + '(' + funcParams.join(',') + ')') : '';
						return _value;
					}else{
						var _handler = _reBuildHandle(value);
						var func = _handler ? _handler.funcName : '';
						this.handler.val(func);
						if(this.handler.item.view){
							this.handler.item.view.text(this.handler.getText());
						}
						$.each(_items, function(i, _item){
							if(_item.item.view){
								_item.item.view.text(_item.getText());
							}
						});
					}
				};
				this.disable = function(showable){
					this.handler.disable(showable);
					$.each(_items, function(i, _item){
						_item.disable(showable);
					});
				};
				this.enable = function(){
					this.handler.enable();
					$.each(_items, function(i, _item){
						_item.enable();
					});
				};
				this.reset = function(){
					var opts = this.opts;
					this.val(opts.value+'');
				};
				this.viewMode = function(){
					this.handler.viewMode();
					$.each(_items, function(i, _item){
						_item.viewMode();
					});
				};
				this.editMode = function(){
					this.handler.editMode();
					$.each(_items, function(i, _item){
						_item.editMode();
					});
				};
				/**
				 * @param datatype  数据类型
				 * 过滤函数下拉选项
				 */
				this.reFilter = function(datatype){
					var opts = this.opts;
					var _reFilter = false;
					if(opts.datatype){
						if(opts.datatype != datatype){
							_reFilter = true;
							opts.datatype = datatype;
						}
					}else{
						_reFilter = true;
						opts.datatype = datatype;
					}
					if(_reFilter){
						_initList(this.jqDom);
						_self.handler.component.select('');
						_removeParamItems();
					}
				};
				this.reload = function(data, parser){
					var opts = this.opts;
					opts.items = [];
					if(data){
						if(parser || (opts.ds && opts.ds.parser)){
							var items = [];
							var _parser = $.extend({
								key:null, text:'text', value:'value', type:'type', desc:'desc', param:'param',
								param_value:{name:'name', type:'type', order:'order', desc:'desc'}
							}, parser || opts.ds.parser);
							if(_parser.key){
								items = data[_parser.key];
							}else{
								items = data;
							}
							for(var i=0;i<items.length;i++){
								var _paramList = items[i][parser.param];
								var _param = null;
								if(_paramList){
									_param = [];
									for(var p=0;p<_paramList.length;p++){
										_param.push({name:_paramList[p][parser.param_value.name], type:_paramList[p][parser.param_value.type], order:_paramList[p][parser.param_value.order], desc:_paramList[p][parser.param_value.desc]});
									}
								}
								opts.items.push({text:items[i][parser.text], value:items[i][parser.value], type:items[i][parser.type], desc:items[i][parser.desc], param:_param});
							}
						}else{
							opts.items = data;
						}
					}else{
						if(opts.ds){
							_loaded = false;
							if(opts.ds.type == 'remote' && !opts.ds.lazyLoad){
								_loadList(jqDom);
							}
						}
					}
				};

				function _loadList(jqDom){
					if(_loaded){
						return;
					}
					var opts = _self.opts;
					var ds = opts.ds;
					var parser = $.extend({
						key:null, text:'text', value:'value', type:'type', desc:'desc', param:'param',
						param_value:{name:'name', type:'type', order:'order', desc:'desc'}
					}, ds.parser);
					jcl.postJSON(ds.url, ds.param||'', function(data){
						var list = data;
						if(parser.key){
							list = data[parser.key];
						}
						for(var i=0;i<list.length;i++){
							var _paramList = list[i][parser.param];
							var _param = null;
							if(_paramList){
								_param = [];
								for(var p=0;p<_paramList.length;p++){
									_param.push({name:_paramList[p][parser.param_value.name], type:_paramList[p][parser.param_value.type], order:_paramList[p][parser.param_value.order], desc:_paramList[p][parser.param_value.desc]});
								}
							}
							opts.items.push({text:list[i][parser.text], value:list[i][parser.value], type:list[i][parser.type], desc:list[i][parser.desc], param:_param});
						}
						_initList(jqDom);
					}, ds.loading || false);
				};

				function _initList(jqDom){
					var opts = _self.opts;
					_self.handlerItems = [];
					_loaded = true;
					if(opts.dataTypeFilter && opts.datatype){
						$.each(opts.items, function(i, item){
							if(!item.type && !item.value){
								_self.handlerItems.push(item);
							}
							if(item.type == opts.datatype){
								_self.handlerItems.push(item);
							}
						});
						if(_self.handlerItems.length == 1){
							var _hItems = _self.handlerItems[0];
							if(!_hItems.type && !_hItems.value){
								_self.handlerItems = [];
							}
						}
					}else{
						_self.handlerItems = opts.items;
					}
					_self.handler.component.reload(_self.handlerItems);
				};

				function _bindEvent(jqDom){
					var opts = _self.opts;
					if(opts.ds && opts.ds.lazyLoad && opts.ds.type == 'remote'){
						jqDom.on('click.function_load', function(){
							if(_disabled){
								return;
							}
							_loadList(jqDom, true);
							this.off('click.function_load');
						});
					}

					_self.handler.component.onChange(function(item){//处理函数下拉选项
						_reBuildParam(item);
					});
				};

				/**
				 * 构建整个处理函数项
				 * @param value  函数值(包含参数的)
				 * @return {func:funcName, param:[]}
				 */
				function _reBuildHandle(value){
					if(!value){
						_removeParamItems();
						return null;
					}
					var opts = _self.opts, func = null, funcParams = [], inFunc = false;
					if(value.indexOf('(')!=-1 && value.indexOf(')')!=-1){//包含参数
						func = value.substring(0, value.indexOf('('));
						funcParams = value.substring(value.indexOf('(')+1, value.indexOf(')')).split('\,');
						if(funcParams && funcParams.length > 0){
							funcParams.splice(0, 1);
						}
					}else{
						func = value;
					}
					$.each(opts.items, function(i, item){
						if(item.value == func){
							inFunc = true;
							if(opts.dataTypeFilter){
								opts.datatype = item.type;
							}
							_initList(_self.jqDom);
							_reBuildParam(item, funcParams);
							return;
						}
					});
					return (inFunc ? {funcName:func, param:funcParams} : null);
				}

				/**
				 * 重新构建处理函数参数项
				 * @param handler 处理函数
				 * @param params  参数值
				 */
				function _reBuildParam(handler, params){
					var opts = _self.opts;
					_removeParamItems();
					if(handler.param && handler.param.length > 0){
						$.each(handler.param, function(i, _param){
							var pname = _param.name, ptype = _param.type,
							plabel = (opts.label + '参数' + (i+1)), pvalue = (params && params[i]) ? params[i] : '';
							if(ptype == 'string' && pvalue){
								pvalue = pvalue.replace(/\"/g, '&quot;');
							}
							var _opts = {label:plabel, name:pname, value:pvalue, required:true};
							switch(ptype){
								/*case 'date':
								case 'datetime':
									_items.push(new jcl.ui.formItem.DateSelector(_opts, _self.parentContainer));
									break;*/
								default:
									_items.push(new jcl.ui.formItem.Text(_opts, _self.parentContainer));
							}
						});
					}
				};
				/**
				 * 删除参数item项
				 *
				 */
				function _removeParamItems() {
					$.each(_items, function(i, _item){
						_item.jqDom.remove();//删除已有的参数项
					});
					_items = [];
				}
			}
		}
	}
};