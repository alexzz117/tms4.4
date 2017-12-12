/*
 * JavaScript Common Library v1.0
 * 
 * jQuery plugins
 * 
 * Encode: UTF-8
 * Author: yanghui
 *
 */

//namespace: jcl_tms_json.ui
jcl_tms = window.jcl_tms
		|| {
			env : {
				contextPath : ''
			},
			namespace : function() {
				var o, d;
				for ( var i = 0; i < arguments.length; i++) {
					d = arguments[i].split(".");
					o = window[d[0]] = window[d[0]] || {};
					for ( var j = 1; j < d.length; j++) {
						o = o[d[j]] = o[d[j]] || {};
					}
				}
				return o;
			},
			message : {
				get : function(key, arr, defvalue) {
					var message = '';
					if (null == key)
						return message;
					if (this[key]) {
						message = (this[key]).toString();
						if (arr && arr instanceof Array) {
							for ( var i = 0; i < arr.length; i++) {
								message = message
										.replace('{' + i + '}', arr[i]);
							}
						}
					} else {
						if (defvalue != undefined) {
							message = defvalue;
						} else if (typeof (arr) == 'string') {
							message = arr;
						} else {
							message = key;
						}
					}

					return message;
				}
			},
			code : {
				get : function(category, code) {
					var codes = this.getCodes(category);
					if (codes.length > 0) {
						if (codes[code]) {
							return codes[code];
						}
					}
					return code;
				},
				getCodes : function(category) {
					if (this[category]) {
						return this[category];
					}
					return [];
				},
				selector : function(id, category, title) {
					var codes = this.getCodes(category);
					var htmlstr = '';
					if (title) {//text,value
					htmlstr += '<option value="' + title['value'] + '">'
							+ title['text'] + '</option>';
				}
				for ( var i in codes) {
					htmlstr += '<option value="' + i + '">' + codes[i]
							+ '</option>';
				}
				$('#' + id).html(htmlstr);
			}
			},
			go : function(uri) {
				var url = [ jcl.env.contextPath, uri ].join('');
				window.location.href = url;
			}
		};
jcl_tms.ns = jcl_tms.namespace;
jcl_tms.ns("jcl_tms.ui", "jcl_tms.util");


(function($) {
	jcl_tms.util.FuncInvoke = function(options) {
		//当前对象
		var _$this = this;
		var defaults = {
			id : '',
			txnid : ''
		};
		this.opts = $.extend(defaults, options);
		
		var _oper = "add";

		var _tables = {
			"div_s":"<div id=\"#id\" style=\"display: inline\">",
			"div_e":"</div>",
			"tab_s":"<table id=\"#id\">",
			"tab_e":"</table>",
			"tr_s":"<tr id=\"#id\">",
			"tr_e":"</tr>",
			"td_s":"<td>",
			"td_e":"</td>"
		};
		
		var _grouptr= {};
		
		var _json = "";
		
		//获取json数据
		this.getJson = function() {
			return _json;
		}
		
		//设置json数据
		this.setJson = function(json) {
			_json = json;
		}
		//将组装的html放到div中
		function _appendHtml(divid, htmlStr) {
			$('#' + divid).html(htmlStr);
		}
		
		//JSON字符串到Object，转换之前先对特殊字符进行处理
		function _json2Obj() {
			if (_json == '') {
				//alert("函数初始化失败！");
				return "";
			}
			return eval("({root:"+_jsonCharFilter(_json)+"})");
		}
		
		function _jsonCharFilter(str) {
			str = str.replace(/\n/ig,"\\n");
			str = str.replace(/\\/ig,"\\\\");
			str = str.replace(/\t/ig,"\\t");
			str = str.replace(/\r/ig,"\\r");
			return str;
		}
		
		function _jsonCharRe(str) {
			str = str.replace(/\\n/ig,"\n");
			str = str.replace(/\\\\/ig,"\\");
			str = str.replace(/\\t/ig,"\t");
			str = str.replace(/\\r/ig,"\r");
			return str;
		}
		//JSON对象转字符串
		function _json2str(o) {
           var arr = [];
           var fmt = function(s) {       
        	   if (typeof s == 'object' && s != null) return _json2str(s);
               return /^(string|number)$/.test(typeof s) ? "'" + s + "'" : s;
          }
           for (var i in o) arr.push("'" + i + "':" + fmt(o[i]));
            return '{' + arr.join(',') + '}';
        }
		//生成input框或select下拉框
		function _getInput(item) {
			var pid = item.PID;					//函数参数主键
			var paramlabel = item.PARAMLABEL;	//函数参数标签
			var paramname = item.PARAMNAME;		//函数参数名称
			var datatype = item.DATATYPE;		//函数参数数据类型
			var globalid = item.GLOBALID;		
			var defvalue = item.DEFAULTVALUE;	//函数参数默认值
			var sourceid = item.SOURCEID;		//函数参数数据来源标识
			var rwcontrol = item.RWCONTROL;		//函数参数访问控制
			var orderby = item.ORDERBY;			//函数参数排序
			var sourcetype = item.SOURCETYPE;	//函数参数来源类型
			var onchange = item.FUNONCHANGE;	//函数参数关联字段
			var paramdesc = item.PARAMDESC;		//函数参数描述信息
			var control = "";
			if (rwcontrol == "1") {				//权限为隐藏
				control = " style=\"visibility: hidden;\" ";
				paramlabel = "";
			} else if (rwcontrol == "2") {		//权限为只读
				control = " disabled=\"disabled\" style=\"color:#999; background:#eee;border:1px solid #ccc;\" ";
			}
			
			var opt = "";
			if (sourcetype != "DIY") {
				opt = _append(item)
			}
			
			var disStr = "";
			if(_oper=="view"){
				disStr = "disabled=\"disabled\"";
			}
			
			var _input = "<input id=\"" + pid
					+ "\" name=\"" + pid + "\"  class=\"tms-itext-s\" dataType=\"" + datatype
					+ "\" type=\"text\" " + control
					+ " " + onchange + "  value=\"\" size=\"5\" "+disStr+" />";

			var _select = "<select id=\"" + pid + "\" name=\"" + pid
							+ "\" class=\"tms-sselect-s\" dataType=\"" + datatype + "\" " + control
							+ " " + onchange + " style=\"width:100px\" "+disStr+" >#opt</select>";
			/*
			var _select = "<select id=\"" + pid + "\" name=\"" + pid
			+ "\" class=\"tms-sselect-s\" dataType=\"" + datatype + "\" " + control
			+ " " + onchange + " ><option value=\"\">--请选择--</option>#opt</select>";
			*/
			return sourcetype == "DIY" ? _input : _select.replace("#opt", opt);
		}
		//生成<option>选项
		function _append(item) {
			var sourcetype = item.SOURCETYPE;
			var pid = item.PID;
			var sourceid = item.SOURCEID;
			var option = "";
			if (sourcetype == "LIST") {				//名单列表
				option = _appendList(sourceid, pid);
			} else if (sourcetype == "DICT") {		//数据字典
				option = _appendDict(sourceid, pid);
			} else if (sourcetype == "TXNMODEL") {	//交易业务模型
				option = _appendMetaobj(sourceid, pid);
			} else if (sourcetype == "TXNSTAT") {	//交易统计模型
				option = _appendMetaobj(sourceid, pid);
			} else if (sourcetype == "TXNBEHA") {	//交易行为模型
				option = _appendMetaobj(sourceid, pid);
			} else if (sourcetype == "USERDATA") {	//用户业务模型
				option = _appendMetaobj(sourceid, pid);
			} else if (sourcetype == "USERSTAT") {	//用户统计模型
				option = _appendMetaobj(sourceid, pid);
			} else if (sourcetype == "USERBEHA") {	//用户行为模型
				option = _appendMetaobj(sourceid, pid);
			} else if (sourcetype == "TXNDATA") {	//交易特征模型
				option = _appendFeature(sourceid, pid);
			}
			return option;
		}
		//查询“交易特征模型”，生成<option>
		function _appendFeature(sourceId, pid) {
			var opt = "";
			var txnId = _$this.opts.txnid;
			jcl_tms.postJSON('/tms/dp/feature/listFeats', 'txnId=' + txnId, function(data) {
				$.each(data.row, function(i, row) {
					if(row['TXNFEATUREID']==null || row['TXNFEATUREID']=="" || row['TXNFEATUREID']=="null" ){//如果是组或渠道，添加为optgroup属性，不能选择               
						opt = opt +("<optgroup label='"+row['PROPNAME']+"'> "+ row['PROPNAME'] + "</optgroup>");
					}else{
						opt = opt + ("<option value='" + row['TXNFEATUREID'] + "'>" + row['PROPNAME'] + "</option>");
					}
					//opt = opt + ("<option value='" + row['TXNFEATUREID'] + "'>" + row['PROPNAME'] + "</option>");
				});
			});
			return opt;
		} 
		//查询“元数据表”，生成<option>
		function _appendMetaobj(sourceId, pid) {
			var opt = "";
			jcl_tms.postJSON('/tms/mgr/meta/listAll', 'METAOBJID=' + sourceId, function(data) {
				$.each(data.row, function(i, row) {
					opt = opt + ("<option value='" + row['PROPCODE'] + "'>"
							+ row['PROPNAME'] + "</option>");
				});
			});
			return opt;
		} 
		//查询“名单列表”，生成<option>
		function _appendList(sourceId, pid) {
			var opt = "";
			jcl_tms.postJSON('/tms/mgr/namelist/listAll', '', function(data) {
				$.each(data.row, function(i, row) {
					opt = opt + ("<option value='" + row['KEY'] + "'>"
							+ row['VALUE'] + "</option>");
				});
			});
			return opt;
		} 
		//查询“数据字典”，生成<option>
		function _appendDict(sourceId, pid) {
			var opt = "";
			jcl_tms.postJSON('/tms/codedict/category/codelist', 'categoryId=' + sourceId, function(data) {
				$.each(data.row, function(i, row) {
					opt = opt + ("<option value='" + row['CODE_KEY'] + "'>"
							+ row['CODE_VALUE'] + "</option>");
				});
			});
//			opt = ("<option value=\"1\">okay</option>");
			return opt;
		} 
		//获得参数组的一行，标题行除外
		this.groupRow = function(groupId) {
			return _grouptr[groupId];
		}
		//添加一行
		this.addGroupRow = function(divid,groupId) {
			var row = this.groupRow(groupId);
			
			$("#tab_g_" + divid + groupId).append(row);
		}
		//删除一行
		this.delGroupRow = function(divid,groupId) {
			var rowsnum = $("#tab_g_" + divid + groupId + " tr").length;
			if (rowsnum <= 3)
				return;
			$("#tab_g_" + divid + groupId + ">tbody>tr:last").remove();
		}
		//初始化    _data:数据		_databtn:按钮
		function _initdata(divid, _data, _databtn) {
			$.each(_databtn, function(idx,group){
				var pgid = group.PGID;
				
				var row = $("#" + idx);
				row.find("#addGroupBtn").click(function() {
					_$this.addGroupRow(divid,pgid);
				});
				row.find("#delGroupBtn").click(function() {
					_$this.delGroupRow(divid,pgid);
				});
			});
			$.each(_data, function(idx,item){
				var pid = item.PID;
				var paramlabel = item.PARAMLABEL;
				var paramname = item.PARAMNAME;
				var datatype = item.DATATYPE;
				var globalid = item.GLOBALID;
				var value = item.VALUE;
				var defvalue = item.DEFAULTVALUE;
				var sourceid = item.SOURCEID;
				var rwcontrol = item.RWCONTROL;
				var orderby = item.ORDERBY;
				var sourcetype = item.SOURCETYPE;
				var paramdesc = item.PARAMDESC;
				var key = idx;
				//判断是不是参数组
				if (idx.indexOf("#_") != -1) {
					key = idx.substr(0, idx.indexOf("#_")) + "]";
				}
				var row = $("#" + key);
				var val = "";
				if (_oper == "add") {
					val = defvalue;
				}else {
					val = value;
				}
				if (row.find("input[id="+ pid + "]").length = 1) {
					row.find("#"+ pid + "").val(val);
				} else if (row.find("select[id="+ pid+ "]").length = 1) {																					
					row.find("select[id="+ pid+ "]").attr("value",val);
				}
				
			});
		}
		
		this.show = function(divid, oper) {
			var jsonObj = _json2Obj();
			if (jsonObj == "") {
				return "";
			}
			_appendHtml(divid, "");
			_oper = oper;
			var _html = "";
			var _data = {}; //"div tab tr:item"
			var _databtn = {};//按钮数组
			//初始化方法信息
			var id = jsonObj.root.ID;					//函数ID
			var name = jsonObj.root.NAME;				//函数名称
			var globalid = jsonObj.root.GLOBALID;		//全局ID
			var risktype = jsonObj.root.RISKTYPE;		//风险类型
			var desc = jsonObj.root.DESC;				//描述
			var fragment = jsonObj.root.FRAGMENT;		//条件代码
			var checkfield = jsonObj.root.CHECKFIELD;	
			var orderby = jsonObj.root.ORDERBY;			//排序
			
			var _divId = "div_" + id; //div的Id是所选函数的Id    Math.floor(Math.random()*100000000)
			//参数
			var param = jsonObj.root.Params.Param;
			
			//参数组
			var groupinfo = jsonObj.root.Params.ParmGroupInfo;
			//处理参数
			$.each(param, function(idx,item){
			    var pid = item.PID;
			    var paramlabel = item.PARAMLABEL;
			    var rwcontrol = item.RWCONTROL;
				
//				var paramunit  = item.PARAMUNIT;	//函数参数单位
				
			    if (rwcontrol == "1") {
			    	paramlabel = "";
			    }else {
			    	if (paramlabel != "")
			    		paramlabel = paramlabel + "：";
			    }
					
				var _tabId = "tab_p_" + pid;//div中table的Id是函数参数的Id
				var _paramhtml = _tables["tab_s"].replace("#id", _tabId);
				var _trId = "tab_p_r_" + pid;
				var _formHtml = _tables["tr_s"].replace("#id", _trId) + _tables["td_s"];

//				_html = _html + _paramhtml +_formHtml + paramlabel + _getInput(item)+paramunit + _tables["td_e"] + _tables["tr_e"] + _tables["tab_e"];
				_html = _html + _paramhtml +_formHtml + paramlabel + _getInput(item) + _tables["td_e"] + _tables["tr_e"] + _tables["tab_e"];
				_data[divid + " div[id=" + _divId + "] table[id=" + _tabId + "] tr[id=" + _trId + "]"] = item;
			}); 
			//处理参数组
			$.each(groupinfo, function(idx,group){
			    var pgid = group.PGID;
			    var groupname = group.GROUPNAME;
			    var rwcontrol = group.RWCONTROL;
			    var value = group.VALUE;
			    var _tabId = "tab_g_" + divid + pgid;
			    var gparam = group.Param;
			    //var tdsize = gparam.length;
				
				var tdsize = 0;
				$.each(gparam, function(id,item){
					tdsize ++;
				});
			   
			    // 处理参数组下的参数都有空的情况
			    var control = "button";
			    if (rwcontrol == "1" || rwcontrol == "2" || _oper=="view") {
			    	control = "hidden";
			    	groupname = "";
			    }
			    var _ghtml = _tables["tab_s"].replace("#id", _tabId) 
			   			    + _tables["tr_s"].replace("#id", "title") 
			   			    + "<td colspan=\""
							+ tdsize
							+ "\" align=\"left\">"
							+ groupname
							+ "&nbsp;&nbsp;<input type=\"" + control + "\" class=\"btn\" value=\"添加\" onclick=\"\" class=\"btn\" id=\"addGroupBtn\"/>"
							+ "&nbsp;&nbsp;<input type=\"" + control + "\" class=\"btn\" value=\"删除\" onclick=\"\" class=\"btn\" id=\"delGroupBtn\"/>"
							+ _tables["td_e"] +
							_tables["tr_e"];
				_databtn[divid + " div[id=" + _divId + "] table[id=" + _tabId + "] tr[id=title" + "]"] = group;
				var _trId = "tab_g_r_" + pgid;
				var _trId_menu = "tab_g_r_menu_" + pgid;
				var _ghtmltitle = "";
				var _ghtmlcontent = "";
				
				var tdItem = {};
			    $.each(gparam, function(id,item){
				    var pid = item.PID;
				    var paramlabel = item.PARAMLABEL;
//					var paramunit  = item.PARAMUNIT;	//函数参数单位
				    // 处理参数组下的参数都有空的情况
				    if (rwcontrol == "1") {
			    		paramlabel = "";
			    	}
				    
				    _ghtmltitle = _ghtmltitle +  _tables["td_s"] + paramlabel +  _tables["td_e"];
//					_ghtmlcontent = _ghtmlcontent +  _tables["td_s"] + _getInput(item) +paramunit+  _tables["td_e"];
					_ghtmlcontent = _ghtmlcontent +  _tables["td_s"] + _getInput(item) +  _tables["td_e"];
					tdItem[id] = item;
					// 组参数默认值
					if (_oper != "add") return true;
					_data[divid + " div[id=" + _divId + "] table[id=" + _tabId + "] tr[id=" + _trId + "#_" + id + "]"] = item;
				}); 
			    _ghtmltitle = _tables["tr_s"].replace("#id", _trId_menu) + _ghtmltitle + _tables["tr_e"];
			    var _ghtmlcontent_k = "";
			    var _ghtmltr = _tables["tr_s"].replace("#id", _trId) + _ghtmlcontent + _tables["tr_e"];
			    if (_oper == "add") {
			    	_ghtmlcontent_k = _ghtmltr;
			    }else {
				    var valArr = value.split("##");
				    for (i = 0; valArr != "" && i < valArr.length; i++) {
				    	if(valArr[i] == "") continue;
				    	var vals = valArr[i].split(",");
				    	var trId_val = _trId + "_" + i;
				    	for (j = 0; vals != "" && j < vals.length; j++) {
				    		var temp = jQuery.extend({}, tdItem[j]); 
				    		temp.VALUE=vals[j];
				    		_data[divid + " div[id=" + _divId + "] table[id=" + _tabId + "] tr[id=" + trId_val + "#_" + j + "]"] = temp;
				    	}
				    	_ghtmlcontent_k = _ghtmlcontent_k + _tables["tr_s"].replace("#id", trId_val) + _ghtmlcontent + _tables["tr_e"];
				    }
				 }
			    //
			    _grouptr[pgid] = (_ghtmltr);
			    _html = _html + _ghtml + _ghtmltitle + _ghtmlcontent_k + _tables["tab_e"];

			}); 
			_html = _tables["div_s"].replace("#id", _divId) + _html + _tables["div_e"];
			_appendHtml(divid, _html);
			_initdata(divid, _data, _databtn);
		}
		
		this.save = function(divid) {
			var jsonObj = _json2Obj();
			if (jsonObj == "") {
				return "";
			}
			
			//初始化方法信息
			var id = jsonObj.root.ID;
			var name = jsonObj.root.NAME;
			var globalid = jsonObj.root.GLOBALID;
			var risktype = jsonObj.root.RISKTYPE;
			var desc = jsonObj.root.DESC;
			var fragment = jsonObj.root.FRAGMENT;
			var checkfield = jsonObj.root.CHECKFIELD;
			var orderby = jsonObj.root.ORDERBY;
			var _divId = "div_" + id;
			//参数
			var param = jsonObj.root.Params.Param;
			
			//参数组
			var groupinfo = jsonObj.root.Params.ParmGroupInfo;
			
			var msg = '';
			
			//处理参数
			$.each(param, function(idx,item){
			    var pid = item.PID;
			    var name = item.PARAMLABEL;
			   	var datatype = item.DATATYPE;
			   	
				var _tabId = "tab_p_" + pid;
				var _trId = "tab_p_r_" + pid;
				
				var key = divid + " div[id=" + _divId + "] table[id=" + _tabId + "] tr[id=" + _trId + "]";
				
				var row = $("#" + key);
				var val = "";
				
				if (row.find("input[id="+ pid + "]").length = 1) {
					val = row.find("#"+ pid + "").val();
				} else if (row.find("select[id="+ pid+ "]").length = 1) {																					
					val = row.find("select[id="+ pid+ "]").val();
				}
				//check
				msg = _$this.checktype(val, '');  //非法字符判断
				if (msg != '') {
					return false;
				}
				msg = _$this.checktype(val, datatype); //数据类型判断
				if (msg != '') {
					return  false;
				}
				//check
				item.VALUE = val;
			}); 
			 
			if(msg != ''){
				return msg;
			}
			//处理参数组
			$.each(groupinfo, function(idx,group){
			    var pgid = group.PGID;
			    var _tabId = "tab_g_" + divid + pgid;
			    var gparam = group.Param;
				var _trId = "tab_g_r_" + pgid;
				var _trId_menu = "tab_g_r_menu_" + pgid;
				
				var tdItem = {};
			    $.each(gparam, function(id,item){
				    var pid = item.PID;
					tdItem[id] = item;
				}); 
			    var key = divid + " div[id=" + _divId + "] table[id=" + _tabId + "]";
			    var value = "";
			    var flag = true;
			    $("#" + key + " tr").each(function(k) {
			    	if (k <= 1) return true;
			    	var row = $(this);
			    	var str = "";
			    	$.each(tdItem, function(idx,item){
			    		 var pid = item.PID;
			    		 var name = item.PARAMLABEL;
						 var datatype = item.DATATYPE;
						 
			    		 var val = "";
						if (row.find("input[id="+ pid + "]").length = 1) {
							val = row.find("#"+ pid + "").val();
						} else if (row.find("select[id="+ pid+ "]").length = 1) {																					
							val = row.find("select[id="+ pid+ "]").val();
						}
						//check
						msg = _$this.checktype(val, '');  //非法字符判断
						
						if (msg != '') {
							msg = "第" + (k - 1) + "行,第" +  (parseInt(idx) + 1) + "列," + msg;
							flag=false;
							return false;
						} 
						msg = _$this.checktype(val, datatype); //数据类型判断
						if (msg != '') {
							msg =  "第" + (k - 1) + "行,第" +  (parseInt(idx) + 1) + "列," + msg;
							flag=false;
							return false;
						}  
						//check
						//if (str != "") str = str + ",";
						str = str + val;
						str = str + ","; //bug94
					});
			    	if(!flag)return false;
			    	value = value + str.substr(0, str.length-1) + "##";
			   	});
			    group.VALUE = value;
			}); 

			var jsonData = _json2str(jsonObj).replace("{'root':", "");
			_json = _jsonCharRe(jsonData.substr(0, jsonData.length-1));
			return msg;
		}
		
		//验证参数值的类型
		this.checktype=function(value,type,strlength){
			var f  = '';
			if(value==null || value==''){
				return f;
			}
			if(type=='NumberType'){
				//var r   =   /^\+?[1-9][0-9]*$/;　　//正整数    
				var r   =    /^\d*$/;　　//非负整数     
				if(!r.test(value)){
					f='参数值必须为数字';
				}
			}else if(type=='DoubleType'){
				if(/^[\+\-]?\d+(\.?\d+)?$/.test(value)==false||isNaN(parseFloat(value))){ 
			        f='参数值必须输入数字 '; 
			     } 
			}else if(type=='DateType'){ 
				
				 //年月日正则表达式
				  var r=value.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/); 
				   if(r==null){
					   f='日期格式输入错误'; 
					   return f;
				  }
			     var d=new Date(r[1],r[3]-1,r[4]);   
			     var num = (d.getFullYear()==r[1]&&(d.getMonth()+1)==r[3]&&d.getDate()==r[4]);
			     if(num==0){
			    	 f='日期格式输入错误'; 
			     }
			}else if(type==''){
				var reg = /^[\u4e00-\u9fa5]+$/				//验证汉字
				var hzcount=0;
				var scount =0;
				for(var j=0;j<value.length;j++){
					if(reg.test(value.charAt(j))){
						hzcount++;
					}else if(escape(value.charAt(j))=='%0A') {//验证回车
						scount=scount+2;
					}else{
						scount++;
					}
				}
				var length = hzcount*3+scount;
				if(strlength==null || strlength==''){
					if(length>32){
						return '参数值超长';
					}
				}else{
					if(length>parseInt(strlength)){
						return '参数值超长';
					}
				}
				//验证特殊字符
				var reg = "~`!@#$%^&*(){}][【】《》><?？;：,\"'；\\";	//特殊字符
				for(var j=0;j<reg.length;j++){
					if(value.indexOf(reg.charAt(j))!=-1){
						return '参数值含有特殊字符';
						break;
					};
				}
			}
			return f;
		}
	
	};
	
	jcl_tms.postJSON = function(uri, params, callback, loading){
		var url = [jcl.env.contextPath, uri].join('');
		url += ((url.indexOf('?') != -1) ? '&' : '?' ) + 'format=json&t=' + new Date().getTime();
		var _loading = true;
		if(loading != undefined){_loading = loading;}
		jcl.ajax({
			loading: _loading,
			url : url,
			data: params,
			dataType: 'json',
			async:false,
			callback: function(data){
				if(typeof(data) == 'object'){
					//if(common_callback(data)){;}
					callback(data);
				}else{
				}
			}
		});
	};
jcl_tms.ui.Datepicker=function(options){
		
		this.opts = $.extend({
			id: null
		}, options);

		var $dp = this;
		
		this.date = new Date();

		function _goDate(ymd){
			$dp.date = ymd;
			var $this = $('#' + $dp.opts.id);
			var list = [];
			var y = ymd.getFullYear();
			var m = ymd.getMonth();
			var fd = new Date(y, m, 1).getDay();
			if(fd > 0){
				//补前面
				var prevMonth = new Date(y, m, 0);
				var day = prevMonth.getDate();
				var y0 = prevMonth.getFullYear();
				var m0 = prevMonth.getMonth();
				var i = 0;
				for(var i = fd; i >= 0; i--){
					list[i] = _ymdformat(y0, m0+1, day-(fd-i));
				}
			}
			var maxday = _getMaxDay(y, m);
			for(var i = 0; i < maxday; i++){
				list[fd+i] = _ymdformat(y, m+1, i+1);
			}
			var nextMonth = new Date(y, m+1, 1);
			var y1 = nextMonth.getFullYear();
			var m1 = nextMonth.getMonth();
			for(var i = fd + maxday; i < 42; i++){
				list[i] = _ymdformat(y1, m1+1, i-fd-maxday+1);
			}
			$this.find('.calendar-ym').text(y+'-'+(m+1));

			$this.find('.calendar-day')
				.removeClass('calendar-othermon')
				.removeClass('calendar-day-picked')
				.each(function(i, cell){
					if(i < fd){
						$(cell).addClass('calendar-othermon');
					}else if(i >= fd + maxday){
						$(cell).addClass('calendar-othermon');
					}

					if(list[i] == $dp.objDateStr){
						$(cell).addClass('calendar-day-picked');
					}
					$(cell).html(parseInt(list[i].split('-')[2], 10)).attr('t', list[i]);
			});

		}

		function _ymdformat(y, m, d){
			return [y, _doublefill(m), _doublefill(d)].join('-');
		}

		function _ymd(d){
			return _ymdformat(d.getFullYear(), d.getMonth()+1, d.getDate());
		}

		function _doublefill(i){
			var str = i+'';
			return  (str.length < 2) ? '0' + str : str;
		}

		function _getMaxDay(year, month){
			return new Date(year, month+1, 0).getDate();
		}

		function _init(){
			
			var weektitles = jcl.message.get("ui.common.datepicker.weektitles", "Sun,Mon,Tue,Wed,Thu,Fri,Sat").split(",");
			var btn_clear = jcl.message.get("ui.common.datepicker.btn.clear", "Clear");
			var btn_today = jcl.message.get("ui.common.datepicker.btn.today", "Today");
			var btn_sure = jcl.message.get("ui.common.datepicker.btn.sure", "OK");

			var now = new Date();
			var htmlstr = '';
			var id = 'datepicker_'+now.getTime();
			$dp.opts.id = id;

			htmlstr += '<div id="'+id+'" class="calendar">';
			htmlstr += '<div class="calendar-ym-box">';
			htmlstr += '<table cellspacing="0" cellpadding="0">';
			htmlstr += '<tr>';
			htmlstr += '<td><a class="calendar-y-prev" href="#">&lt;&lt;</a></td>';
			htmlstr += '<td><a class="calendar-m-prev" href="#">&lt;</a></td>';
			htmlstr += '<td class="calendar-ym"></td>';
			htmlstr += '<td><a class="calendar-m-next" href="#">&gt;</a></td>';
			htmlstr += '<td><a class="calendar-y-next" href="#">&gt;&gt;</a></td>';
			htmlstr += '</tr>';
			htmlstr += '</table></div>';
			
			//add by yangk begin
			var currentDate = $dp.opts.value == "" ?  _ymd(now) : $dp.opts.value;
			var currentHour;
			var currentMin;
			var currentSec;
			
			if(currentDate.length > 12){
				currentHour = currentDate.substring(11,19).split(':')[0];
				currentMin = currentDate.substring(11,19).split(':')[1];
				currentSec = currentDate.substring(11,19).split(':')[2] ;
			} else {
				currentHour = now.getHours() ;
				currentMin = now.getMinutes() ;
				currentSec = now.getSeconds();
			}
			//时
			htmlstr += '<div align="center">';
			htmlstr += '<td>时</td>';
			htmlstr += '<td>';
			htmlstr += '<select id="hour" name="hour">';
			for(var i = 0; i < 24; i++){
				htmlstr = addZeroAndSelected(currentHour, i, htmlstr);
			}
			htmlstr += '</select>';
			htmlstr += '<td>'; 
			//分
			htmlstr += '<td>分</td>';
			htmlstr += '<td>';
			htmlstr += '<select id="minute" name="minute">';
			for(var i = 0; i < 60; i++){
				htmlstr = addZeroAndSelected(currentMin, i, htmlstr);
			}
			htmlstr += '</select>';
			htmlstr += '</td>';
			//秒
			htmlstr += '<td>秒</td>';
			htmlstr += '<td>';
			htmlstr += '<select id="second" name="second">';
			for(var i = 0; i < 60; i++){
				htmlstr = addZeroAndSelected(currentSec, i, htmlstr);
			}
			htmlstr += '</select>';
			htmlstr += '</td>';
			
			htmlstr += '</div>';
			//add by yangk end
			
			htmlstr += '<div class="calendar-week-box">';
			htmlstr += '<table class="calendar-week-table" cellspacing="0" cellpadding="0">';
			htmlstr += '<tr class="calendar-week-title">';
			for(var i = 0; i < 7; i++){
				htmlstr += '<td><div class="calendar-week">'+weektitles[i]+'</div></td>';
			}
			htmlstr += '</tr>';
			for(var i = 0; i < 42; i++){
				if(i%7 == 0){
					htmlstr += '<tr>';
				}
				htmlstr += '<td><div class="calendar-day"></div></td>';
				if(i%7 == 6){
					htmlstr += '</tr>';
				}
			}
			htmlstr += '</table></div>';
			htmlstr += '<div class="calendar-btn-box">';
			htmlstr += '<input type="button" class="tms-calendar-btn calendar-btn-clear" value="'+btn_clear+'"> ';
			htmlstr += '<input type="button" class="tms-calendar-btn calendar-btn-today" value="'+btn_today+'"> ';
			htmlstr += '<input type="button" class="tms-calendar-btn calendar-btn-sure" value="'+btn_sure+'">';
			htmlstr += '</div></div>';

			$('body').prepend(htmlstr);
			$('#' + $dp.opts.id).bgiframe();
		}
		
		function addZeroAndSelected(mark, index, arg) {
			//add by yangk 20110929
			//fix bug, format 'mark' from '09' to '9', in order to getting the right compare. 
			//etc typeof(index) is number
			mark = mark.length > 1 && mark.substring(0,1) == '0' ? mark.substring(1, mark.length) : mark;
			if(index < 10){
				
				if(mark == index){
					return arg + '<option value=0' + index + ' selected>0' + index + '</option>';
				} else {
					return arg + '<option value=0' + index + '>0' + index + '</option>';
				}
			} else {
				
				if(mark == index){
					return arg + '<option value=' + index + ' selected>' + index + '</option>';
				} else {
					return arg + '<option value=' + index + '>' + index + '</option>';
				}
			}
		}
		
		function _bind_evnet(){
			var opts = $dp.opts;
			var $this = $('#' + opts.id);

			//翻页 年 月
			$this.find('.calendar-y-prev').click(function(){
				var d = $dp.date;
				_goDate(new Date(d.getFullYear() -1, d.getMonth()));
				return false;
			});
			$this.find('.calendar-y-next').click(function(){
				var d = $dp.date;
				_goDate(new Date(d.getFullYear() +1, d.getMonth()));
				return false;
			
			});
			$this.find('.calendar-m-prev').click(function(){
				var d = $dp.date;
				_goDate(new Date(d.getFullYear(), d.getMonth()-1));
				return false;
			});
			$this.find('.calendar-m-next').click(function(){
				var d = $dp.date;
				_goDate(new Date(d.getFullYear(), d.getMonth()+1));
				return false;
			});

			//移动光标选择 //选中
			$this.find('.calendar-day').mouseover(function(){
				$(this).addClass('calendar-day-hover');
			}).mouseout(function(){
				$(this).removeClass('calendar-day-hover');
			}).click(function(){
				_setInputDate($(this).attr('t'));
				$this.hide();
			});
			
			//按键
			$this.find('.tms-calendar-btn').click(function(){
				var $btn = $(this);
				if($btn.hasClass('calendar-btn-clear')){
					_setInputDate('');
				}else if($btn.hasClass('calendar-btn-today')){
					_setInputDate(_ymd(new Date()));
				}else if($btn.hasClass('calendar-btn-sure')){
					//当页没有默认选中的日期,set初始时间
					if($this.find('.calendar-day-picked').attr('t') == undefined){
						_setInputDate($dp.objDateStr);
					} else 
						_setInputDate($this.find('.calendar-day-picked').attr('t'));
				}
				$this.hide();
			});


			//在其他区域点击取消
			$(document).bind('click.datepicker', function(event){
				if($this.css('display') == 'none')return true;
				if($dp.obj == event.target) return true;
				var $target = $(event.target);
				if($target.hasClass('calendar') 
					|| $target.parents('.calendar').length > 0 ){
					return true;
				}
				$this.hide();
			});

		}
		function _setInputDate(ymd){
			//modify by yangk begin
			if('' != ymd){
				ymd = ymd + " " + $("#hour").val() + ":" + $("#minute").val() + ":" + $("#second").val();
			}
			//modify by yangk end
			$($dp.obj).val(ymd);
			$dp.objDateStr = ymd;
		}
		function _getInputDate(){
			var v = $($dp.obj).val();
			/**
			if(v && /\d{4}-\d{1,2}-\d{1,2}/.test(v)){
				var temp = v.split('-');
				var d = new Date(parseInt(temp[0], 10), parseInt(temp[1], 10)-1, parseInt(temp[2], 10));
				$dp.objDateStr = _ymd(d);
				return d;
			}
			*/
			
			if(v && /^(\d{4})\-(\d{1,2})\-(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/.test(v)){
				var date = v.substring(0,10).split('-');   
    			var time = v.substring(11,19).split(':'); 
    			var d = new Date();
    			d.setFullYear(date[0]);
			    d.setMonth(date[1]-1);
			    d.setDate(date[2]);
			    d.setHours(time[0]);
			    d.setMinutes(time[1]);
			    d.setSeconds(time[2]);
			    
				//var d = new Date(parseInt(temp[0], 10), parseInt(temp[1], 10)-1, parseInt(temp[2], 10));
				$dp.objDateStr = _ymd(d);
				return d;
			}
			$dp.objDateStr = '';
			return new Date();
		}
		function _bindInput(id){
			//绑定输入框
			if(typeof(id) == 'string'){
				$dp.obj = $('#'+id).get(0);
			}else{
				$dp.obj = id;
			}
		}

		function _hideSome(){
			var ele=document.getElementsByTagName("select");
			if(ele!=null){
				for(var i=0;i<ele.length;i++){
					if(ele[i].style.display!="none" 
						&& ele[i].style.visibility!='hidden' 
							&& ele[i].getAttribute("calendar")!="0"){
						ele[i].style.visibility='hidden';
					}
				}
			}	
		}
		function _showSome(){
			var ele=document.getElementsByTagName("select");
			for(var i=0;i<ele.length;i++){
					ele[i].style.visibility='visible';
			};
		}

		this.obj = null;//dom node
		this.objDateStr = null;
		this.picker=function(id){
			//add by yangk
			if($dp.objDateStr != null){//means not first
				var v = id.value;
				//var date = v.substring(0,10).split('-');   
    			
    			var time;
				if(v == ''){
					var now = new Date();
					var h = now.getHours();
					var m = now.getMinutes();
					var s = now.getSeconds();
					time = [h<10?'0'+h:h,m<10?'0'+m:m,s<10?'0'+s:s] ;
				}else{
					time = v.substring(11,19).split(':'); 
				}
				
    			$("#hour").val(time[0]);
    			$("#minute").val(time[1]);
    			$("#second").val(time[2]);
			}
			
			_bindInput(id);
			var $obj = $($dp.obj);
			var $this = $('#' + $dp.opts.id);
			var offset = $obj.offset();
			var left = offset.left;
			var top = offset.top + $obj.outerHeight();
			$this.css({left: left+'px', top: top+'px' });
			//取当前的年月日
			var ymd = _getInputDate();
			_goDate(ymd);
			$this.show();
		};

		_init();
		_bind_evnet();
	};
	
	jcl_tms.datepicker = function(id){
		if(!jcl_tms.datepicker.prototype.dp){
			jcl_tms.datepicker.prototype.dp = new jcl_tms.ui.Datepicker(id);
		}
		jcl_tms.datepicker.prototype.dp.picker(id);	};
	
	jcl_tms.ui.Datepicker_hms=function(options){
		
		this.opts = $.extend({
			id: null
		}, options);

		var $dp = this;
		
		this.date = new Date();

		function _init(){
			
			var btn_sure = jcl.message.get("ui.common.datepicker.btn.sure", "OK");

			var now = new Date();
			var htmlstr = '';
			var id = 'datepicker_hms_'+now.getTime();
			$dp.opts.id = id;

			htmlstr += '<div id="'+id+'" class="calendar">';
			
			var currentDate = $dp.opts.value == "" ?  "" : $dp.opts.value;
			var currentHour = '00';
			var currentMin = '00';
			var currentSec = '00';
			
			if(currentDate.length > 8){
				currentHour = currentDate.split(':')[0];
				currentMin = currentDate.split(':')[1];
				currentSec = currentDate.split(':')[2] ;
			}
			
			//时
			htmlstr += '<div align="center">';
			htmlstr += '<td>时</td>';
			htmlstr += '<td>';
			htmlstr += '<select id="hour" name="hour">';
			for(var i = 0; i < 24; i++){
				htmlstr = addZeroAndSelected(currentHour, i, htmlstr);
			}
			htmlstr += '</select>';
			htmlstr += '<td>'; 
			//分
			htmlstr += '<td>分</td>';
			htmlstr += '<td>';
			htmlstr += '<select id="minute" name="minute">';
			for(var i = 0; i < 60; i++){
				htmlstr = addZeroAndSelected(currentMin, i, htmlstr);
			}
			htmlstr += '</select>';
			htmlstr += '</td>';
			//秒
			htmlstr += '<td>秒</td>';
			htmlstr += '<td>';
			htmlstr += '<select id="second" name="second">';
			for(var i = 0; i < 60; i++){
				htmlstr = addZeroAndSelected(currentSec, i, htmlstr);
			}
			htmlstr += '</select>';
			htmlstr += '</td>';
			
			htmlstr += '</div>';
			
			htmlstr += '<div class="calendar-btn-box">';
			htmlstr += '<input type="button" class="tms-calendar-btn sure" value="'+btn_sure+'">';
			htmlstr += '</div></div>';

			$('body').prepend(htmlstr);
			$('#' + $dp.opts.id).bgiframe();
		}
		
		function addZeroAndSelected(mark, index, arg) {
			mark = mark.length > 1 && mark.substring(0,1) == '0' ? mark.substring(1, mark.length) : mark;
			if(index < 10){
				
				if(mark == index){
					return arg + '<option value=0' + index + ' selected>0' + index + '</option>';
				} else {
					return arg + '<option value=0' + index + '>0' + index + '</option>';
				}
			} else {
				
				if(mark == index){
					return arg + '<option value=' + index + ' selected>' + index + '</option>';
				} else {
					return arg + '<option value=' + index + '>' + index + '</option>';
				}
			}
		}
		
		function _bind_evnet(){
			var opts = $dp.opts;
			var $this = $('#' + opts.id);

			$this.find('.tms-calendar-btn').click(function(){
				var $btn = $(this);
				if($btn.hasClass('sure')){
					//当页没有默认选中的日期,set初始时间
					_setInputDate();
				}
				$this.hide();
			});


			//在其他区域点击取消
			$(document).bind('click.datepicker', function(event){
				if($this.css('display') == 'none')return true;
				if($dp.obj == event.target) return true;
				var $target = $(event.target);
				if($target.hasClass('calendar') 
					|| $target.parents('.calendar').length > 0 ){
					return true;
				}
				$this.hide();
			});

		}
		
		function _setInputDate(){
			var ymd = $("#hour").val() + ":" + $("#minute").val() + ":" + $("#second").val();
			$($dp.obj).val(ymd);
			$dp.objDateStr = ymd;
		}
		
		function _bindInput(id){
			//绑定输入框
			if(typeof(id) == 'string'){
				$dp.obj = $('#'+id).get(0);
			}else{
				$dp.obj = id;
			}
		}

		this.obj = null;//dom node
		this.objDateStr = null;
		this.picker=function(id){
			if($dp.objDateStr != null){//means not first
				var v = id.value;
    			var time = v.split(':'); 
    			
    			$("#hour").val(time[0]);
    			$("#minute").val(time[1]);
    			$("#second").val(time[2]);
			}
			
			_bindInput(id);
			var $obj = $($dp.obj);
			var $this = $('#' + $dp.opts.id);
			var offset = $obj.offset();
			var left = offset.left;
			var top = offset.top + $obj.outerHeight();
			$this.css({left: left+'px', top: top+'px' });

			$this.show();
		};

		_init();
		_bind_evnet();
	};
	
	jcl_tms.datepicker_hms = function(id){
		new jcl_tms.ui.Datepicker_hms(id).picker(id);
	};
})(jQuery);
