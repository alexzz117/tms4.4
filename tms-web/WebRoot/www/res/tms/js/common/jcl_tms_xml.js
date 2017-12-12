/*
 * JavaScript Common Library v1.0
 * 
 * jQuery plugins
 * 
 * Encode: UTF-8
 * Author: yanghui
 *
 */

//namespace: jcl_tms.ui
jcl_tms_xml = window.jcl_tms_xml
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
jcl_tms_xml.ns = jcl_tms_xml.namespace;
jcl_tms_xml.ns("jcl_tms_xml.ui", "jcl_tms_xml.util");

//
(function($) {
	jcl_tms_xml.util.PatternFun = function(options) {
		var defaults = {
			id : ''
		};
		var _index = 1;

		function _init() {

		}

		this.selectRows = function(index) {
			jcl.postJSON('/tms/mgr/fun/getAll', "", function(data) {
				$.each(data.row, function(i, row) {
					$("select[name=func_" + index + "]").append(
							"<option value='" + row['FUNCID'] + "'>"
									+ row['FUNCNAME'] + "</option>");
				});
			});
		}

		this.delLine = function(index) {
			$("#f_" + index).remove();
		}

		var opts = $.extend(defaults, options);

		this.addLine = function(divId) {
			var htmlStr = "<div id=\"f_"
					+ _index
					+ "\" style=\"display: inline\"><BR/>"
					+ "函数：<select id=\"func_"
					+ _index
					+ "\" name=\"func_"
					+ _index
					+ "\" index=\""
					+ _index
					+ "\" >"
					+ "<option value=\"\">--请选择--</option>"
					+ "</select>&nbsp;&nbsp;&nbsp;&nbsp;<div id=\"param_"
					+ _index
					+ "\" style=\"display: inline\"></div>"
					+ "&nbsp;&nbsp;<input type=\"button\" value=\"删除\" class=\"btn\" id=\"funDel_"
					+ _index + "\" /></div>";
			$('#' + divId).append(htmlStr);

			_index++;
			return _index - 1;
		};

		_init();
	};

	$.fn.al = function() {
		//alert("这是什么");
	};
})(jQuery);

(function($) {
	jcl_tms.util.TmsFun = function(options) {
		var $this = this;
		var defaults = {
			id : ''
		};
		var opts = $.extend(defaults, options);

		var _xml = "";
		//		var xml = "<xml>"
		//			+ "<Root>"
		//			+ "<NAME>等于</NAME>" + "<ID>1</ID>"
		//				+ "<GLOBALID>1</GLOBALID>" + "<RISKTYPE></RISKTYPE>"
		//				+ "<DESC></DESC>" + "<FRAGMENT>条件片段</FRAGMENT>"
		//				+ "<CHECKFIELD>校验字段</CHECKFIELD>" + "<ORDERBY>1</ORDERBY>"
		//				+ "<Params>" + " <ParmGroupInfo>" + "  <PGID>1</PGID>"
		//				+ "  <GLOBALID>全局Id</GLOBALID>"
		//				+ "  <GROUPNAME>参数组名称</GROUPNAME>"
		//				+ "  <RWCONTROL>参数访问控制</RWCONTROL>" + "  <ORDERBY>顺序</ORDERBY>"
		//				+ "  <Params>" + "   <Param>" + "    <PID>Id</PID>"
		//				+ "    <GLOBALID>全局Id</GLOBALID>"
		//				+ "    <PARAMNAME>参数名称</PARAMNAME>"
		//				+ "    <PARAMLABEL>显示名称</PARAMLABEL>"
		//				+ "    <DEFAULTVALUE>默认值</DEFAULTVALUE>"
		//				+ "    <SOURCEID>参数来源</SOURCEID>"
		//				+ "    <RWCONTROL>参数访问控制</RWCONTROL>"
		//				+ "    <ORDERBY>顺序</ORDERBY>"
		//				+ "    <SOURCETYPE>参数类型</SOURCETYPE>"
		//				+ "    <PARAMDESC>参数说明</PARAMDESC>" + "   </Param>"
		//				+ "  </Params>" + " </ParmGroupInfo>" + " <Param>"
		//				+ "    <PID>1</PID>" + "    <GLOBALID>1</GLOBALID>"
		//				+ "    <PARAMNAME>value</PARAMNAME>"
		//				+ "    <PARAMLABEL>值</PARAMLABEL>"
		//				+ "    <DEFAULTVALUE></DEFAULTVALUE>"
		//				+ "    <SOURCEID>1</SOURCEID>" + "    <RWCONTROL>2</RWCONTROL>"
		//				+ "    <ORDERBY>1</ORDERBY>" + "    <SOURCETYPE>1</SOURCETYPE>"
		//				+ "    <PARAMDESC>参数说明</PARAMDESC>" + "</Param>" + "</Params>"
		//				+ "</Root></xml>";

		var _index = 1;

		var _groupParams = {};

		var _groupInputs = {};

		function _init() {

		}
		this.aaa = function() {
			return "aaaaa";
		}

		function _getXmlDom(xmlstr) {
			if (xmlstr == '') {
				alert("函数初始化失败！");
				return;
			}
			var xmldom = null
			if (navigator.userAgent.toLowerCase().indexOf("msie") != -1) {
				xmldom = new ActiveXObject("Microsoft.XMLDOM");
				xmldom.loadXML(xmlstr);
			} else
				xmldom = new DOMParser().parseFromString(xmlstr, "text/xml");

			return xmldom;

		}

		function _getDomXml(xmlDom) {
			var xmlstr;
			if ($.browser.msie) {
				xmlstr = xmlDom.xml;
			} else {
				xmlstr = (new XMLSerializer()).serializeToString(xmlDom);
			}
			return xmlstr;
		}

		function _appendList(sourceId) {

		}

		function _appendData(url, params, paramNm, selectObj) {
			jcl.postJSON(url, params, function(data) {
				$.each(data.row, function(i, row) {
					selectObj.append("<option value='" + row['FUNCID'] + "'>"
							+ row['FUNCNAME'] + "</option>");
				});
			});
		}
		function _appendDict(sourceId, paramNm) {
			$("select[name=" + paramNm + "]").each(function() {
				var selectObj = $(this);
				if (selectObj.find("option").length <= 1) {
					_appendData('/tms/mgr/fun/getAll', '', paramNm, selectObj);
				}

			});
		}

		function _appendTxnData(sourceId) {

		}

		function _append(sourceType, sourceId, paramNm) {

			if (sourceType == "LIST") {

			} else if (sourceType == "DICT") {
				_appendDict(sourceId, paramNm);
			} else if (sourceType == "TXNDATA") {

			} else if (sourceType == "TXNSTAT") {

			} else if (sourceType == "TXNBEHA") {

			} else if (sourceType == "USERDATA") {

			} else if (sourceType == "USERSTAT") {

			} else if (sourceType == "USERBEHA") {

			} else {
				return;
			}
		}
		this.setXml = function(xml) {
			_xml = xml;
		}
		this.getXml = function() {
			return _xml;
		}
		this.selectData = function(divId, sourceType, sourceId, obj) {
			var divStr = "<table id='func-box'>"
					+ "<tr>"
					+ "<td valign='top'>"
					+ "<div id='fun-tree-box'>"
					+ "<select id='paramVal' name='paramVal'>"
					+ "<option value=''>--请选择--</option>"
					+ "</select>"
					+ "</div>"
					+ "</td>"
					+ "</tr>"
					+ "<tr>"
					+ "<td colspan='1' align='center'>"
					+ "<input type='button' value='选择' class='btn' id='selectFunBtn'/>"
					+ "<input type='button' value='取消' class='btn' id='cancelFunBtn'/>"
					+ "</td>" + "</tr>" + "</table>";
			$('#' + divId).html(divStr);
			_appendData();
			//$('#paramVal').removeAttr('maskhide');
			//$('#paramVal').removeAttr('style');
			openWin(divId, obj);
		}

		function openWin(openDiv, obj) {
			var funTree = jcl.ui.Dialog( {
				title : '参数值选择',
				width : 450
			});
			funTree.addDom(openDiv);

			funTree.show();
			$('#selectFunBtn').click(function() {
				obj.value = $('#paramVal').val();
				funTree.hide();
			});
			$('#cancelFunBtn').click(function() {
				funTree.hide();
			});
		}

		function _getInput(sourceType, sourceId, rwControl, paramNm, dataType,
				value) {

			var read = "";
			var type = "text";
			if (rwControl == "1") {
				type = "hidden";
			} else if (rwControl == "2") {
				read = "readonly";
			}
			return sourceType == "DIY" ? "<input id=\"" + paramNm
					+ "\" name=\"" + paramNm + "\" dataType=\"" + dataType
					+ "\" type=\"" + type + "\" " + read
					+ " class=\"itext\" value=\"" + value + "\"/>"
					: "<select id=\"" + paramNm + "\" name=\"" + paramNm
							+ "\" dataType=\"" + dataType + "\" " + read
							+ "><option value=\"\">--请选择--</option></select>";
		}

		function _getParamHtml(_this) {
			var label = _this.find("PARAMLABEL").text();
			var paramNm = _this.find("PARAMNAME").text();
			var defVal = _this.find("DEFAULTVALUE").text();
			var dataType = _this.find("DATATYPE").text();
			var sourceId = _this.find("SOURCEID").text();
			var rwControl = _this.find("RWCONTROL").text();
			var sourceType = _this.find("SOURCETYPE").text();
			var value = _this.find("VALUE").text();
			var lab = "";
			if (rwControl != "1") {
				lab = label + "：";
			}
			var html = "<tr><td align=\"right\">" + lab + "" + "</td>";
			return html
					+ "<td>"
					+ _getInput(sourceType, sourceId, rwControl, paramNm,
							dataType, value) + "</td></tr>";
		}

		function _getGroupHtml(gparams) {
			var titleHtml = "<tr id=\"title\">";
			var contentHtml = "<tr>";
			var paramInput = "";
			gparams.children("Param").each(
					function() {
						titleHtml = titleHtml + "<td>"
								+ $(this).find("PARAMLABEL").text() + "</td>";
						var label = $(this).find("PARAMLABEL").text();
						var paramNm = $(this).find("PARAMNAME").text();
						var dataType = $(this).find("DATATYPE").text();
						var defVal = $(this).find("DEFAULTVALUE").text();
						var sourceId = $(this).find("SOURCEID").text();
						var rwControl = $(this).find("RWCONTROL").text();
						var sourceType = $(this).find("SOURCETYPE").text();
						if (paramInput != "")
							paramInput = paramInput + "#";
						paramInput = paramInput + sourceId + "," + sourceType
								+ "," + paramNm;
						contentHtml = contentHtml
								+ "<td>"
								+ _getInput(sourceType, sourceId, rwControl,
										paramNm, dataType, "") + "</td>";
					});
			return titleHtml + "</tr>" + "&&:&&" + contentHtml + "</tr>"
					+ "&&:&&" + paramInput;
		}

		function _getTitle(gparams) {
			var titleHtml = "<tr>";
			gparams.children("Param").each(
					function() {
						titleHtml = titleHtml + "<td>"
								+ $(this).find("PARAMNAME").text() + "</td>";
					});
			return titleHtml + "</tr>";
		}

		this.getGroupParam = function(groupId) {
			return _groupParams[groupId];
		}

		this.addGroupParam = function(groupId) {
			var groupParam = this.getGroupParam(groupId);
			$("#" + groupId).append(groupParam);
			var groupInput = _groupInputs[groupId];
			var paramInput = groupInput.split("#");

			for (i = 0; i < paramInput.length; i++) {
				var input = paramInput[i].split(",");
				var paramNm = input[2];
				var sourceId = input[0];
				var sourceType = input[1];
				_append(sourceType, sourceId, paramNm);
			}
		}
		this.delGroupParam = function(groupId) {
			var rowsnum = $("#" + groupId + " tr").length;
			if (rowsnum <= 3)
				return;
			$("#" + groupId + ">tbody>tr:last").remove();
		}

		this.getParams = function() {
			var xmldom = _getXmlDom(_xml);
			$(xmldom)
					.children()
					.each(function(i) {
						var id = $(this).children("GLOBALID"); //取对象
							var params = $(this).children("Params");

							params.children("Param").each(
									function() {
										var paramNm = $(this).find("PARAMNAME")
												.text();
										$(this).find("VALUE").text(
												$("#" + paramNm).val());
									});

							params
									.children("ParmGroupInfo")
									.each(
											function() {
												var groupId = $(this).find(
														"PGID").text();

												var gparams = $(this).children(
														"Params");
												var groupHtml = _getGroupHtml(
														gparams).split("&&:&&");

												var paramInput = groupHtml[2]
														.split("#");

												var str = "";

												$("#" + groupId + " tr")
														.each(
																function(k) {
																	if (k <= 1)
																		return true;
																	for (i = 0; i < paramInput.length; i++) {
																		var input = paramInput[i]
																				.split(",");
																		var paramNm = input[2];

																		var inval = "";
																		if ($(
																				this)
																				.find(
																						"input[id="
																								+ paramNm
																								+ "]").length = 1) {
																			inval = $(
																					this)
																					.find(
																							"#"
																									+ paramNm
																									+ "")
																					.val();
																		} else if ($(
																				this)
																				.find(
																						"select[id="
																								+ paramNm
																								+ "]").length = 1) {
																			inval = $(
																					this)
																					.find(
																							"select[id="
																									+ paramNm
																									+ "]")
																					.val();
																		}
																		if (inval == "")
																			return false;
																		if (str != ""
																				&& i != 0)
																			str = str
																					+ ",";
																		str = str
																				+ inval;
																	}
																	str = str
																			+ "##";
																});
												$(this).find("VALUE").text(str);

											});
						});

			_xml = _getDomXml(xmldom);
		};

		this.addParams = function(divId) {
			var htmlStr = "";
			$('#' + divId).html(htmlStr);
			//			$(_getXmlDom(_xml)).find("Root").each(function(i) {
			var xmldom = _getXmlDom(_xml);
			$(xmldom)
					.children()
					.each(function(i) {
						var id = $(this).children("GLOBALID"); //取对象

							var params = $(this).children("Params");

							params.children("Param").each(
									function() {
										var paramHtml = _getParamHtml($(this));
										var paramNm = $(this).find("PARAMNAME")
												.text();
										var sourceId = $(this).find("SOURCEID")
												.text();
										var sourceType = $(this).find(
												"SOURCETYPE").text();
										htmlStr = "<table>" + paramHtml
												+ "</table><BR>";
										$('#' + divId).append(htmlStr);
										_append(sourceType, sourceId, paramNm);
									});

							params
									.children("ParmGroupInfo")
									.each(
											function() {
												var groupId = $(this).find(
														"PGID").text();
												var tdSize = $(this).find(
														"Params > Param")
														.size();

												var groupName = $(this).find(
														"GROUPNAME").text();
												var groupTitle = "<table id=\""
														+ groupId
														+ "\"><tr id=\"title\"><td colspan=\""
														+ tdSize
														+ "\" align=\"left\">"
														+ groupName
														+ "&nbsp;<input type=\"button\" value=\"添加一行\" onclick=\"\" class=\"btn\" id=\"addGroupBtn\"/>"
														+ "&nbsp;<input type=\"button\" value=\"删除一行\" onclick=\"\" class=\"btn\" id=\"delGroupBtn\"/>"
														+ "</td></tr>";
												var gparams = $(this).children(
														"Params");
												var groupHtml = _getGroupHtml(
														gparams).split("&&:&&");
												groupTitle = groupTitle
														+ (groupHtml[0] + groupHtml[1])
														+ "</table><BR>";
												htmlStr = groupTitle;
												_groupParams[groupId] = (groupHtml[1]);
												_groupInputs[groupId] = (groupHtml[2]);
												$('#' + divId).append(htmlStr);
												var row = $("#" + groupId
														+ " tr[id=title]");
												row
														.find("#addGroupBtn")
														.click(
																function() {
																	$this
																			.addGroupParam(groupId);
																});

												row
														.find("#delGroupBtn")
														.click(
																function() {
																	$this
																			.delGroupParam(groupId);
																});

												var paramInput = groupHtml[2]
														.split("#");

												for (i = 0; i < paramInput.length; i++) {
													var input = paramInput[i]
															.split(",");
													var paramNm = input[2];
													var sourceId = input[0];
													var sourceType = input[1];
													_append(sourceType,
															sourceId, paramNm);
												}

											});
						});
			//$('#' + divId).html(htmlStr);
		};

		this.editParams = function(divId) {
			var htmlStr = "";
			$('#' + divId).html(htmlStr);
			var xmldom = _getXmlDom(_xml);
			$(xmldom)
					.children()
					.each(function(i) {
						var id = $(this).children("GLOBALID"); //取对象

							var params = $(this).children("Params");

							params.children("Param").each(
									function() {
										var paramHtml = _getParamHtml($(this));
										var paramNm = $(this).find("PARAMNAME")
												.text();
										var sourceId = $(this).find("SOURCEID")
												.text();
										var value = $(this).find("VALUE")
												.text();
										var sourceType = $(this).find(
												"SOURCETYPE").text();
										htmlStr = "<table>" + paramHtml
												+ "</table><BR>";
										$('#' + divId).append(htmlStr);
										_append(sourceType, sourceId, paramNm);

										setTimeout(function() {
											$("#" + paramNm).attr("value",
													value);
										}, 500)

									});

							params
									.children("ParmGroupInfo")
									.each(
											function() {
												var groupId = $(this).find(
														"PGID").text();
												var value = $(this).find(
														"VALUE").text();
												var tdSize = $(this).find(
														"Params > Param")
														.size();

												var groupName = $(this).find(
														"GROUPNAME").text();
												var groupTitle = "<table id=\""
														+ groupId
														+ "\"><tr id=\"title\"><td colspan=\""
														+ tdSize
														+ "\" align=\"left\">"
														+ groupName
														+ "&nbsp;<input type=\"button\" value=\"添加一行\" onclick=\"\" class=\"btn\" id=\"addGroupBtn\"/>"
														+ "&nbsp;<input type=\"button\" value=\"删除一行\" onclick=\"\" class=\"btn\" id=\"delGroupBtn\"/>"
														+ "</td></tr>";
												var gparams = $(this).children(
														"Params");
												var groupHtml = _getGroupHtml(
														gparams).split("&&:&&");
												groupTitle = groupTitle
														+ (groupHtml[0]);
												//+ groupHtml[1])
												//		+ "</table><BR>";
												//
												var valArr = value.split("##");
												var paramInput = groupHtml[2]
														.split("#");
												var fileds = "";
												for (i = 0; i < paramInput.length; i++) {
													var input = paramInput[i]
															.split(",");
													if (fileds != "")
														fileds = fileds + ",";
													fileds = fileds + input[2];
												}
												var jsonDat = "";
												var htmlCon = "";
												for (i = 0; i < valArr.length; i++) {
													var ghtml = groupHtml[1];
													var vals = valArr[i];
													if (vals != "") {
														var key = "value_" + i;
														htmlCon = htmlCon
																+ ghtml
																		.replace(
																				"<tr>",
																				"<tr id=\""
																						+ key
																						+ "\">");
	
														var _val = vals.split(",")
														jsonDat = jsonDat + "{\""
																+ key + "\":";//
														var _fds = fileds
																.split(",")
														var _dat = "{"
														for (j = 0; j < _val.length; j++) {
															if (j != 0)
																_dat = _dat + ",";
															_dat = _dat + "\""
																	+ _fds[j]
																	+ "\":\""
																	+ _val[j]
																	+ "\"";
														}
														jsonDat = jsonDat + _dat
																+ "}}";//
														if (valArr.length - 1 != i)
															jsonDat = jsonDat + ",";
													
													}
												}
												//
												htmlStr = groupTitle + htmlCon
														+ "</table><BR>";

												jsonDat = ("[" + jsonDat + "]");
												_groupParams[groupId] = (groupHtml[1]);
												_groupInputs[groupId] = (groupHtml[2]);
												$('#' + divId).append(htmlStr);

												var row = $("#" + groupId
														+ " tr[id=title]");
												row
														.find("#addGroupBtn")
														.click(
																function() {
																	$this
																			.addGroupParam(groupId);
																});

												row
														.find("#delGroupBtn")
														.click(
																function() {
																	$this
																			.delGroupParam(groupId);
																});

												for (i = 0; i < paramInput.length; i++) {
													var input = paramInput[i]
															.split(",");
													var paramNm = input[2];
													var sourceId = input[0];
													var sourceType = input[1];
													_append(sourceType,
															sourceId, paramNm);
												}

												var dataObj = eval("({root:"
														+ jsonDat + "})");
											setTimeout(function() {
											
												$
														.each(
																dataObj.root,
																function(idx,
																		item) {
																	$
																			.each(
																					item,
																					function(
																							key,
																							map) {
																						var row = $("#"
																								+ groupId
																								+ " tr[id="
																								+ key
																								+ "]");
																						$
																								.each(
																										map,
																										function(
																												k,
																												v) {
																											if (row
																													.find("input[id="
																															+ k
																															+ "]").length = 1) {
																												row
																														.find(
																																"#"
																																		+ k
																																		+ "")
																														.val(
																																v);
																											} else if (row
																													.find("select[id="
																															+ k
																															+ "]").length = 1) {
																												row
																														.find(
																																"select[id="
																																		+ k
																																		+ "]")
																														.attr(
																																"value",
																																v);
																											}
																										});
																					});

																});
												
												}, 500)
												

											});
						});
			//$('#' + divId).html(htmlStr);
		};
		_init();

	};

})(jQuery);
