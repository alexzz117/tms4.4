var model = window.model || {};

(function(){
	var dirty = false;
	model.txnModel = {
		init: function(txnId, _page){
			page = _page;
			this.load(txnId);
		},
		load: function(txnId){
			initTxnMdl(txnId);
		},
		isDirty: function(){
			return dirty;
		},
		save: function(){
			
		}
	};
})();

var page = {};
var txnId = '';
var ref_tb_fd;
var txntabs_idx = 0;
var txn_ref_tab_arry = [];
var funcs = {};
var func_Param = {};
var fd_arr = ['NAME', 'SRC_ID', 'REF_NAME', 'SRC_DEFAULT', 'TYPE', 'GENESISRUL', 'CODE'];
var ref_arr = [ 'REF_FD_NAME', 'REF_NAME', 'REF_DESC', 'SRC_COND', 'FD_SRC_EXPR', 'TAB_NAME'];

var txnNeedTransfer = "TYPE____CODE____GENESISRUL"; //
var txnNeedTransferRef = "REF_FD_NAME_____TAB_NAME"; //

var $data;
var txnfdObj = {};
var code_data ;

function Model(id, datatype, domtype, value, attrs, hassub){
	
	var txn_dom = {
		id : id,
		datatype : datatype,
		domtype : domtype,
		value : value,
		attrs : attrs,
		hassub : hassub
	};
	
	txn_dom.add = function(name, value){
		txn_dom[name] = value;
	};
	
	return txn_dom;
}
function intiTable(rows, cols){
	var table = '<form id="fd_conf_form" name="fd_conf_form" method="post">' +
			'<table id="fd_model_tab" border="1" style="border-collapse: collapse;" />' +
			'<BR/>' +
			'<table id="fd_ref_tab" border="1" style="border-collapse: collapse;" />' +
			'</form>';
	$('#formbox-txn_mdl').html(table);
	
	var $tableObj = $('#fd_model_tab');
	var initdata = '<tr style="font-weight: bold;" bgcolor="#CCCCD4" id="introduce____mdl_tr">';
	initdata += '<td valign="middle" align="left" colspan="7">';
	initdata += '<span style="cursor: pointer;"  id="txn_mdl_spn">';
	initdata += '-交易模型';
	initdata += '</span>';
	initdata += '</td></tr>';
	initdata += '<tr><td valign="middle" align="center">';
	initdata += '属性名称';
	initdata += '</td><td valign="middle" align="center">';
	initdata += '数据来源';
	initdata += '</td><td valign="middle" align="center">';
	initdata += '属性代码';
	initdata += '</td><td valign="middle" align="center">';
	initdata += '默认值';
	initdata += '</td><td valign="middle" align="center">';
	initdata += '数据类型';
	initdata += '</td><td valign="middle" align="center">';
	initdata += '转换函数';
	initdata += '</td><td valign="middle" align="center">';
	initdata += '代码';
	initdata += '</td>';
	initdata += '</tr>';

	$tableObj.html(initdata);
			
	$('#txn_mdl_spn').click(function(){
		
		var $type_tr = $('#introduce____mdl_tr');
		
		if($type_tr.next().is( ":hidden ")){
			$(this).html('-交易模型');
			$type_tr.nextUntil('#introduce____ref_tr').show();

			$.each($type_tr.nextUntil('#introduce____ref_tr').find('span'), function(i, dom_span){
				$(dom_span).text($(dom_span).text().replace('+', '-'));
			});
		} else {
			$type_tr.nextUntil('#introduce____ref_tr').hide();
			$(this).html('+交易模型');
// $type_tr.nextUntil('#ref_tr').find('span').val().replace('-', '=');
		}
		
	});
	
// console.log('there is [' + $tableObj.find('td').length +'] td');
	return $tableObj;
}


function getSource(){
	return code_data;
};

function getSourceSelectHtml(selected_value){
	var sourceIdList = getSource();
	var tabHtml = '';
	for(var k=0;k<sourceIdList.length;k++){
		if(selected_value!=null && sourceIdList[k]['CATEGORY_ID']==selected_value){
			tabHtml+='<option value="'+sourceIdList[k]['CATEGORY_ID']+'" selected="selected">'+sourceIdList[k]['CATEGORY_NAME']+'</option>';
		}else{
			tabHtml+='<option value="'+sourceIdList[k]['CATEGORY_ID']+'">'+sourceIdList[k]['CATEGORY_NAME']+'</option>';
		}
	}
	return tabHtml;
}
function getelem(newfd, refcount, txnid, fdsrc, refid){
	if(newfd){ // REF____NEW____1____T0101____REF_DESC____1111
		return "REF____NEW____"+ refcount + "____" + txnid + "____" + fdsrc + "____" + refid ;
	} else { // REF____REF_NAME____1____1111
//				return "REF____" + fdsrc + "____" + refcount + "____" + refid + "____" + txnid;
		return "REF____" + fdsrc + "____" + refcount + "____" + refid;
	}
}
function insert(fd, type){
	//var cols = 6;
	if(type == 'MODEL' && fd.hassub == false){
		cols = 7;
	} else if(type == 'MODEL' && fd.hassub == true){
		cols = 8;
	} else {
		cols = 6;
	}

	var binddataids;
	
	if (fd.domtype == 'introduce') {
		
		var $tr = $('<tr>', {'id' : fd.id, "bgcolor" : "#EDEDED", "style" : "font-weight: bold;"});
		var $td = $('<td>', {"valign" : "middle", "align" : "left",  "colspan" : cols});
		var $span = $('<span>', {"style" : "cursor: pointer;"});
		
		$span.click(function(){
			
// console.log($(this).parent().parent());
// console.log($tr_id.attr("id"));
			var $tr_id = $(this).parent().parent();
			
			if($tr_id.next().is(":hidden ")){
				$(this).text($(this).text().replace('+', '-'));
				$tr_id.nextUntil('tr[id*=introduce____]').show();
			} else {
				$tr_id.nextUntil('tr[id*=introduce____]').hide();
				$(this).text($(this).text().replace('-', '+'));
			}
			return;
		});
		var refcount = fd.id.split('____')[4];
		if(type == 'MODEL'){
			
			$span.html('-' + fd.value);
		}else {
			$span.html('-' + fd.value );
			
		}
		
		var add_btn_idx = 1;
		
		if(fd.hassub == false){
			$td = $('<td>', {"valign" : "middle", "align" : "left", "colspan" : cols - 1});
			var $td_btn = $('<td>', {"valign" : "middle", "align" : "center", "colspan" : "1"});
			var $add_btn = $('<input>', {"id" : "addRow", "class" : "btn", "type" : "button" , "align":"center", "value":"添加行"});
			var $del_btn = $('<input>', {"id" : "deleteRow", "class" : "btn", "type" : "button" , "align":"center", "value":"删除行"});
			var $delRef = $('<input>', {"id" : "deleteRef", "class" : "btn", "type" : "button" , "align":"center", "value":"删除表引用"});
			
			$td.html($span);
			
			if (type == "REF") {
				
				var $ref_src ;
				var $ref_tab ;
				var $tab_ref_desc ;
				var txnname = fd.txnIdAndName[1];
				var txnid = fd.txnIdAndName[0];
				
				var refid = fd.id.split('____')[6];
				var $ref_src = $('<select id='+ getelem(fd.newfd, refcount, txnid, "SRC_EXPR", refid) +' name='+ getelem(fd.newfd, refcount, txnid, "SRC_EXPR", refid) +' align="center">');
				var $ref_tab = $('<select id='+ getelem(fd.newfd, refcount, txnid, "REF_TAB_NAME", refid) +' name='+ getelem(fd.newfd, refcount, txnid, "REF_TAB_NAME", refid) +' align="center">');
				
				var $tab_ref_desc = $('<input>', {
					"id" : getelem(fd.newfd, refcount, txnid, "TAB_REF_DESC", refid),
					"name" : getelem(fd.newfd, refcount, txnid, "TAB_REF_DESC", refid),
					"type" : "text", "align":"left"
				});
					
				$td.append($ref_src);
				$td.append($ref_tab);
				$td.append($tab_ref_desc);
				var _tabname = fd.tabname;
				var src_expr = fd.src_expr;
				var selectHtml = '';
				$.each(txnfdObj, function(key, value){
				    if (src_expr == key) {
				        selectHtml += '<option selected="selected" value="'+key+'">'+value+'</option>';
				    } else 
						selectHtml += '<option value="'+key+'">'+value+'</option>';
				});
				
				$ref_src.html(selectHtml);
				
				var tab_comp = [];
				var idx = 0;
				selectHtml = '';
				$.each(ref_tb_fd, function(i, tab_fd){
                        
                	var tab_name = tab_fd['TAB_NAME'];
                    
                    if($.inArray(tab_name, tab_comp) < 0){
                    	if (_tabname == tab_name) {
                    		selectHtml += '<option selected="selected" value="'+tab_name+'">'+tab_fd['TAB_DESC']+'</option>';
                    	}else
							selectHtml += '<option value="'+tab_name+'">'+tab_fd['TAB_DESC']+'</option>';
                        tab_comp[idx++] = tab_name;
                    }
                });
                
				$ref_tab.html(selectHtml);
				$tab_ref_desc.val(fd.tab_ref_desc);
				
				if (fd.fdnew == undefined) {
					$tab_ref_desc.val($ref_tab.find(":selected").text());
				}
				
				$ref_tab.change(function (){
					
					var baseVal = $ref_tab.val();
					var baseText = $ref_tab.find(":selected").text();
					$('#fd_ref_tab select[id*=REF_FD_NAME][id$='+fd.id.split('____')[6]+']').each(function (i, $eachSelect){
						
						var selectHtml = '';
						 $.each(ref_tb_fd, function(i, tab_fd){
                        	var tab_name = tab_fd['TAB_NAME'];
                            if(baseVal == tab_name){
								selectHtml += '<option value="'+tab_fd['FD_NAME']+'">'+tab_fd['NAME']+'</option>';
                            }
                        });
						
//								$.each(txnfdObj, function(key, value){
//								    if (baseVal == key) {
//								        selectHtml += '<option selected="selected" value="'+key+'">'+value+'</option>';
//								    } else 
//										selectHtml += '<option value="'+key+'">'+value+'</option>';
//								});
						$($eachSelect).html(selectHtml);
					});
					
					$tab_ref_desc.val(baseText);
				});
			} 
			
			$tr.append($td);
			
			$td_btn.append($add_btn);
			$td_btn.append($del_btn);
			
			if(type == 'REF'){
				$td_btn.append($delRef);
				
				$delRef.click(function (){
//    						introduce____REF____tr____T____1____2____4
//    						var thisval = this.id.split('____')[1];
					
					var $tr_id = $(this).parent().parent();
    				$tr_id.nextUntil('tr[id*=introduce____]').remove();
    				$tr_id.remove();
					
				});
			}
			
			$tr.append($td_btn);
			
			$del_btn.click(function(){
				var str;
				
				if($("input[type=checkbox]:checked").length < 1) {
					alert('请选择一条记录');
				}
				
				$("input[type=checkbox]:checked").each(function(){
			         $(this).parent().parent().remove();
				});
			});
			
			$add_btn.click(function(){
				
				buildwhenclick(fd, type , add_btn_idx, refcount );
				add_btn_idx ++;
			});
		} else {
			$td = $('<td>', {"valign" : "middle", "align" : "left", "colspan" : cols - 1});
			$td.html($span);
			
			$tr.append($td);
			
			if(type == 'REF'){
			    var $btn_td = $('<td>', {"valign" : "middle", "align" : "center"});
				var $add_btn = $('<input>', {"id" : "addRow", "class" : "btn", "type" : "button" , "align":"center", "value":"添加行"});
				var $del_btn = $('<input>', {"id" : "deleteRow", "class" : "btn", "type" : "button" , "align":"center", "value":"删除行"});
				$btn_td.append($add_btn);
				$btn_td.append($del_btn);
				$tr.append($btn_td);
				
				$add_btn.click(function (){
					fd.add("fdnew", true);
					buildwhenclick(fd, 'REF', add_btn_idx, refcount);
					add_btn_idx ++;
				});
				
				$del_btn.click(function (){
					var str;
				
					if($("input[type=checkbox]:checked").length < 1) {
						alert('请选择一条记录');
					}
					
					$("input[type=checkbox]:checked").each(function(){
				         $(this).parent().parent().remove();
					});
				});
			}
			
		}
		
		$data = $tr;
	} else if (fd.domtype == 'tr') {
		var $tr = $('<tr>');
		$data = $tr;
	} else if (fd.domtype == 'td') {
		
		var $td;
		if(fd.hassub == false){ // editable
		
			$td = $('<td>', {"valign" : "middle", "align" : "left"});
			
			if(fd.datatype != ''){ // che
				if(fd.datatype != 'text'){ // complex select need
											// transfer
					if (fd.datatype == 'checkbox____text'){
						var $checkbox = $('<input>', {"type" : "checkbox", "align" : "center", "size" : "10"});
						fd.attrs['type'] = 'text';
						fd.attrs['name'] = fd.attrs.id;
						var $text = $('<input>', fd.attrs);
						$td.append($checkbox);
						
						// "MODEL____NAME____1"
						var fdname = fd.attrs.id.split('____')[0] + '____FD_NAME____' + fd.attrs.id.split('____')[2];
						var $fdname = $('<input>', {type : 'hidden', id : fdname, name : fdname , value : fd.coldata.FD_NAME });
						
						var orderby = fd.attrs.id.split('____')[0] + '____ORDERBY____' + fd.attrs.id.split('____')[2];
						var $orderby = $('<input>', {type : 'hidden', id : orderby, name : orderby , value : fd.coldata.ORDERBY });
						$td.append($text);
						$td.append($fdname);
						$td.append($orderby);
					} 
					else if (fd.datatype == 'txntype'){
						var $select = $('<select id='+ fd.attrs.id +' name='+ fd.attrs.id +' align="center">');
						$select.html(getSelect('tms.model.datatype', fd.value));
						$td.append($select);
						
					} 
					else if (fd.datatype == 'txngenesisrul'){
						
						var $select = $('<select id='+ fd.attrs.id +' name='+ fd.attrs.id +' align="center">');
						var $type = $('#' + fd.attrs.id.split('____')[0] + '____TYPE____' + fd.attrs.id.split('____')[2]);

						$td.append($select);

		    			aa(fd, $type, $select);

						$type.change(function (){
					        aa(null, $(this), $select);
						});
						
						// FIXME
						$select.change(function(){

							var selected_val = $(this).val();
							var $subs = $('<div name="g_arg_div">');
							var hasRow = false;
							$(this).nextAll().remove();
							
							var i = 0;
							$.each(func_Param, function(idx, param_obj){ 
							    if(param_obj.FUNC_CODE == selected_val){ // have args
							        var $html = makeGenesisrulArgs({id:selected_val}, "", fd.attrs.id, idx);
							   		$subs.append($html);
							   		i++;
							   		hasRow = true;
							    }
							});
							
							if(!hasRow){
								$(this).nextAll().hide();
							} else 
								$(this).nextAll().show();

							$(this).after($subs);
						});
					}
					else if (fd.datatype == 'noele'){
						$td.html(fd.txnIdAndName[1]);
					}
					else if (fd.datatype == 'txncode'){
						var $way = $('<select align="center">');
						var $code = $('<select id='+ fd.attrs.id +' name='+ fd.attrs.id +' align="center">');
						if(fd.value != '' && fd.value != null) {
							$code.show();
							$way.html('<option value="HAND">手工输入</option><option value="DICT" selected="selected">数据字典</option>');
							$code.html(getSourceSelectHtml(fd.value));
							
						} else {
							$code.hide();
							$way.html('<option value="HAND">手工输入</option><option value="DICT">数据字典</option>');
							
						}
						
						$td.append($way);
						$td.append($code);
						
						$way.change(function(){
							var $this = $(this);
							var $next = $this.next();
							var netx_value = $this.next().val();
							if($this.val()=='DICT'){
								
								$next.removeAttr("disabled");
								$next.html(getSourceSelectHtml(netx_value));
								$next.show();
							}else{
								$next.html('');
								$next.hide();
							}
						});
					} else if (fd.datatype == 'checkbox____select'){
						var $checkbox = $('<input>', {"type" : "checkbox", "align" : "center", "size" : "10"});
						var selectid = fd.attrs.id;
						var tab_value = fd.coldata.REF_TAB_NAME;
						var $select = $('<select id='+ selectid +' name='+ selectid +' align="center">');
						
						
//			    				var selectHtml ='<select id="'+selectid+'" name="'+selectid+'">';
	    				var selectHtml ='';
	    				
                        $.each(ref_tb_fd, function(i, tab_fd){
                        	
                        	var tab_name = tab_fd['TAB_NAME'];
                            
                            if(tab_value == tab_name){
                            	
                            	if(fd.attrs.value == tab_fd['FD_NAME']){
                            		selectHtml += '<option selected="selected" value="'+tab_fd['FD_NAME']+'">'+tab_fd['NAME']+'</option>';
						   		} else 
									selectHtml += '<option value="'+tab_fd['FD_NAME']+'">'+tab_fd['NAME']+'</option>';

                            }
                        });
                        
//								selectHtml += '</select>';
						$select.html(selectHtml);
						$td.append($checkbox);
						$td.append($select);
						// REF____REF_NAME____1____1111
						$select.change(function (){
					    	
					    	$('#' + getelem(false, selectid.split('____')[2], "", "REF_NAME", selectid.split('____')[3])).val($(this).val());
							$('#' + getelem(false, selectid.split('____')[2], "", "REF_DESC", selectid.split('____')[3])).val($(this).find("option:selected").text());
						});
						
					} else if (fd.datatype == 'ref_tab_name'){
						
						var tab_comp = [];
                        var idx = 0;
                        var selectid = fd.attrs.id;
	    				var $select =$('<select id='+ fd.attrs.id +' name='+ fd.attrs.id +' align="center">');
						var selectHtml ='';
						
                        $.each(ref_tb_fd, function(i, tab_fd){
                            
                        	var tab_name = tab_fd['TAB_NAME'];
                            
                            if($.inArray(tab_name, tab_comp) < 0){
                            	
                            	if(fd.attrs.value == tab_name){
                            		selectHtml += '<option selected="selected" value="'+tab_name+'">'+tab_fd['TAB_DESC']+'</option>';
						   		} else 
									selectHtml += '<option value="'+tab_name+'">'+tab_fd['TAB_DESC']+'</option>';
                            	
                                tab_comp[idx++] = tab_name;
                            }
                            
                        });
                        $select.append(selectHtml);
// selectHtml += '</select>';
						$td.append($select); 
						
						$select.change(function (){
		    				var select_value = $(this).val();
		    				var selectid = $(this).attr('id');
		    				var $ref_fd_name = $('#' + selectid.split('____')[0] + '____REF_FD_NAME____' + selectid.split('____')[2]);
		    				$ref_fd_name.html('');
		    				var html = '';
                            $.each(ref_tb_fd, function(i, tab_fd){
                            	
                            	var tab_name = tab_fd['TAB_NAME'];
                                
                                if(select_value == tab_name){
                                	
									html += '<option value="'+tab_fd['FD_NAME']+'">'+tab_fd['NAME']+'</option>';
                                }
                            });
                            
							$ref_fd_name.append(html);
		    			});
		    			
					} 
				} else { // simple text
				
				    fd.attrs['type'] = 'text';
				    fd.attrs['name'] = fd.attrs.id;
					var $text = $('<input>', fd.attrs);
					$td.append($text);
				}
			} else 
				$td.html(fd.attrs.value);
		} else {
			
			$td = $('<td>', fd.attrs);
			$td.html(fd.value);
		}
		
		$data.append($td);
	}
	if (type == 'MODEL') {
		$('#fd_model_tab').append($data);
	} else {
		$('#fd_ref_tab').append($data);
	}
	
	
}
function aa(fd, $type, $select){
		
	var typevalue = $type.val();
	var selectHtml = '';
	var hasRow = false;
	var g_val = fd ? fd.attrs.value : '';
	var g_val_obj = {};
	
	$.each(funcs, function(idx, func){
		if(idx == 0){
			selectHtml += '<option value="">请选择</option>';
		}
		if(typevalue == func['FUNC_TYPE']){
    		selectHtml += '<option value="'+func['FUNC_CODE']+'">'+func['FUNC_NAME']+'</option>';
    		hasRow = true;
		}
	});

	$select.html(selectHtml);

	if(g_val && g_val.indexOf('?') > -1){
	    
	    //g_val = "abcd(?,how do you do)";
	    //g_val = "33(?,0,2)";
		//g_val = "11(?,xxxx,yyyy)";
	    
	    var fmt_val = '';
		g_val_obj.id = g_val.substring(0, g_val.indexOf('('));
		fmt_val = g_val.substring(g_val.indexOf('(') + 3, g_val.length - 1);
		
		var $subs = $('<div>');

		$.each(fmt_val.split(','), function(idx, str){
			var $html = makeGenesisrulArgs(g_val_obj, str, fd.attrs.id, idx);
		    $subs.append($html);
		});

		$select.after($subs);
	}

	g_val_obj.id ? $select.val(g_val_obj.id) : '';
	
	if(!hasRow){
		$select.hide();
		$select.nextAll().hide();
	} else 
		$select.show();
}
function makeGenesisrulArgs(g_val_obj, value, name, idx){
			   
    var html = '';
    
    $.each(func_Param, function(i, param_obj){ 
	    if(param_obj.FUNC_CODE == g_val_obj.id){ // have args
	    	
	        if(param_obj.TYPE == 'code'){ // arg type
	        	// new select
	        } else {
	        	// text 
	        	name = name.replace("GENESISRUL", "GENESISRUL_ARG" + idx);
	        	html = '<input type="text" style="width:20px" name=' + name + ' value="' + value + '" >';
	        }
	        return false;
	    }
	});

	return $(html);
}

function buildwhenclick(fd, type, add_btn_idx, refcount){
				
	function buildDomid(lastArg, type){
		type = type == undefined ? 'MODEL' : type;
		if(type == 'MODEL'){
			return type + '____NEW____'+ add_btn_idx + '____' + fd.id.split('____')[3] + '____' + lastArg;
		} else {
			return type + '____NEW____'+ add_btn_idx + '____' + fd.id.split('____')[3] + '____' + lastArg + '____' +fd.id.split('____')[6];
		}
	}
	
    var idx = parseInt(fd.id.split('____')[5]);
    var $intro_tr = $('#'+fd.id);
    var $intro_tr_next = $intro_tr.nextAll();
    
    var $next_intro_tr = $intro_tr.nextAll('tr[id*=introduce____REF____tr]').eq(0);
    
    var htmlstr = '';
    if(type == 'MODEL'){
// ['NAME', 'SRC_ID', 'REF_NAME', 'TYPE', 'CODE', 'GENESISRUL'];
    	var type_fd_arr = [ {text : 'NAME'}, {text : 'SRC_ID'}, {text : 'REF_NAME'}, {text : 'SRC_DEFAULT'},
    					{select : 'TYPE'}, {select : 'GENESISRUL'},  {select : 'CODE'} ];
    	
    	$.each(type_fd_arr, function (type_fd_arr_idx, type_fd){
    		$.each(type_fd, function (key, value){
    			htmlstr += '<td valign="middle" align="left">';
    			if(key == 'text' && value == 'NAME'){
    				
    				htmlstr += '<input type="checkbox" align="center" size="10">';
                    htmlstr += '<input id= "' + buildDomid(value) + '" name= "' + buildDomid(value) + '" size="10" valign="middle" align="right" type="text">';
                    
                   // htmlstr += '<input type="hidden" id=
					// "' + buildDomid('FD_NAME') + '" name=
					// "' + buildDomid('FD_NAME') + '"
					// value="'+buildDomid('FD_NAME')+'">';
    			} else if(key == 'text'){
    				
    				htmlstr += '<input id= "' + buildDomid(value) + '" name= "' + buildDomid(value) + '" size="10" valign="middle" align="right" type="text">';
    			} 
    			else if(key == 'select' && value == 'CODE'){
    				
    			    htmlstr += '<select id= "' + buildDomid('WAY') + '" name= "' + buildDomid('WAY') + '" />';
    			    htmlstr += '<select id= "' + buildDomid(value) + '" name= "' + buildDomid(value) + '" />';
    			} 
    			else {
    				
    				htmlstr += '<select id= "' + buildDomid(value) + '" name= "' + buildDomid(value) + '" />';
    			}
    			
    			
    			htmlstr += '</td>';
    		});
    	});
    } 
    else {
        
        var type_fd_arr = [  {select : 'REF_FD_NAME'}, 
    					{text : 'REF_NAME'}, {text : 'REF_DESC'}, {text : 'SRC_COND'}, {text : 'FD_SRC_EXPR'}, {noele : 'TAB_NAME'} ];
        
        $.each(type_fd_arr, function (type_fd_arr_idx, type_fd){
    		$.each(type_fd, function (key, value){
    			htmlstr += '<td valign="middle" align="left">'; //dddd
    			if(key == 'select' && value == 'REF_FD_NAME'){
    				
    				htmlstr += '<input type="checkbox" align="center" size="10">';
                    htmlstr += '<select id= "' + buildDomid(value, 'REF') + '" name= "' + buildDomid(value, 'REF')  + '" />';
    			} else if(key == 'text'){
    				
    				htmlstr += '<input id= "' + buildDomid(value, 'REF')  + '" name= "' + buildDomid(value, 'REF')  + '" size="10" valign="middle" align="right" type="text">';
    			} else if(key == 'noele'){
    				var v = ''; 
	    			$.each(txn_ref_tab_arry, function(i, txndef){
	                   
	                	if(txnId == txndef.TAB_NAME){
	                		v = txndef.TAB_DESC;
	                	}
	                	
	                });
    				htmlstr += v;
    			} else {
    				
    				htmlstr += '<select id= "' + buildDomid(value, 'REF')  + '" name= "' + buildDomid(value, 'REF')  + '" />';
    			}
    			htmlstr += '</td>';
    		});
    	});
    }
    
	var $new_tr = $('<tr>');
	$new_tr.html(htmlstr);
	
	if(type == 'MODEL')
		$('#fd_model_tab').append($new_tr);
	else {
		
		if($next_intro_tr.length == 0){ // 无下级
			$('#fd_ref_tab').append($new_tr);
		} else 
			$next_intro_tr.before($new_tr);
//							if($intro_tr_next.length == 0){
//								$('#fd_ref_tab').append($new_tr);
//							} else 
//								$intro_tr_next.eq(idx + add_btn_idx - 1).after($new_tr);
	}
	
    if(type == 'MODEL'){
    	
    	var $new_src_id = $('#' + buildDomid('SRC_ID'));
    	$new_src_id.blur(function (){
    		var index = $(this).attr('id').split('____');
    		var $new_ref_name = $('#' + type + '____NEW____'+ index[2] + '____' + index[3] + '____REF_NAME');

    		// id + 1 了 所以取同级的
    		// var $new_ref_name = $('#' +
			// buildDomid('REF_NAME'));
    		$new_ref_name.val($new_src_id.val());
    	});
    	
    	var $way = $('#'+ buildDomid('WAY'));
        var $code = $('#'+ buildDomid('CODE'));
		$code.hide();
		$way.html('<option value="HAND" selected="selected">手工输入</option><option value="DICT">数据字典</option>');
		
		$way.change(function(){
			var $this = $(this);
			var $next = $this.next();
			var netx_value = $this.next().val();
			if($this.val()=='DICT'){
				
				$next.removeAttr("disabled");
				$next.html(getSourceSelectHtml());
				$next.show();
			}else{
				$next.html('');
				$next.hide();
			}
		});
		
        $('#' + buildDomid('TYPE')).html(getSelect('tms.model.datatype'));
        
        var $type = $('#'+ buildDomid('TYPE'));
        var typevalue = $('#'+ buildDomid('TYPE')).val();
		var $genesisrul = $('#'+ buildDomid('GENESISRUL'));
		var selectHtml = '';
		var hasRow = false;
		
		aa(fd, $type, $genesisrul);
			
		$type.change(function (){
			
    		 aa(null, $(this), $genesisrul);
		});
		// FIXME 
		$genesisrul.change(function(){
			var selected_val = $(this).val();
			var $subs = $('<div name="g_arg_div">');
			var hasRow = false;
			var name =  $(this).attr('name');
			$(this).nextAll().remove();
			
			var i = 0;
			$.each(func_Param, function(idx, param_obj){ 
			    if(param_obj.FUNC_CODE == selected_val){ // have args
			        var $html = makeGenesisrulArgs({id:selected_val}, "", name, i);
			   		$subs.append($html);
			    	i++;
			   		hasRow = true;
			    }
			});
			
			if(!hasRow){
				$(this).nextAll().hide();
			} else 
				$(this).nextAll().show();

			$(this).after($subs);
		});
    } 
    else {
    	// REF____REF_TAB_NAME____1____1111
		// "REF____NEW____"+ refcount + "____" + txnId + "____" + fdsrc + "____" + refid;
        var $select =  $('#' + getelem(fd.fdnew, refcount, txnId, "REF_TAB_NAME", fd.id.split('____')[6]));
        
		var select_value = $select.val();
		
		if( select_value == undefined || select_value == ''
			|| select_value == null){
			
			select_value = fd.tabname;
		}
		var $ref_fd_name = $('#' + buildDomid('REF_FD_NAME', 'REF'));
		var $ref_desc = $('#' + buildDomid('REF_DESC', 'REF'));
		var $ref_name = $('#' + buildDomid('REF_NAME', 'REF'));
		
		var html = '';
        $.each(ref_tb_fd, function(i, tab_fd){
        	
        	var tab_name = tab_fd['TAB_NAME'];
        	
            if(select_value == tab_name){
            	
				html += '<option value="'+tab_fd['FD_NAME']+'">'+tab_fd['NAME']+'</option>';
            }
        });
        
		$ref_fd_name.html(html);
		$ref_name.val($ref_fd_name.val());
		$ref_desc.val($ref_fd_name.find("option:selected").text());
		
		$select.change(function (){
		    
	    	var select_value = $select.val();
			var htm = '';
		    
		     $.each(ref_tb_fd, function(i, tab_fd){
        	
            	var tab_name = tab_fd['TAB_NAME'];
            	
                if(select_value == tab_name){
                	
					htm += '<option value="'+tab_fd['FD_NAME']+'">'+tab_fd['NAME']+'</option>';
                }
            });
            $ref_fd_name.html(htm );
		});
		
		$ref_fd_name.change(function (){
		    
	    	$ref_name.val($ref_fd_name.val());
			$ref_desc.val($ref_fd_name.find("option:selected").text());
		});
		
    }
    
    //add_btn_idx++;

}

function getSelect(category, value){
	
	var codes = jcl.code.getCodes(category);
	var htmlstr = '';
	for(var i in codes){
		if (value == i) {
			htmlstr += '<option selected="selected" value="'+i+'">'+codes[i]+'</option>';
		} else
			htmlstr += '<option value="'+i+'">'+codes[i]+'</option>';
	}
	return htmlstr;
}
					
function insertSimpleTr(){
	var model = new Model(
		'',
		'',
		'tr',
		'', 
		'',
		''
	);
	insert(model);
}

function transferdata(adf, data, tablaname){
	// SRC_EXPR____REF_TAB_NAME____REF_FD_NAME
	if(adf == 'TYPE'){
		return jcl.code.get('tms.model.datatype', data);
	} else if(adf == 'CODE'){
		
		if (data != '' && data != null) {
			
			var code_category = getSource();
			
			for(var k=0;k<code_category.length;k++){
				
				if(data == code_category[k]['CATEGORY_ID']){
					return code_category[k]['CATEGORY_NAME'];
				}
			}
		} else 
			return '';
	} else if(adf == 'GENESISRUL'){

		if (data != '' && data != null) {
			return 'sublime 2';
        }
	} else if(adf == 'TAB_NAME'){
	     var v = ''; 
		$.each(txn_ref_tab_arry, function(i, txndef){
           
        	if(data == txndef.TAB_NAME){
        		v = txndef.TAB_DESC;
        	}
        	
        });
        return v;
	} else if(adf == 'REF_TAB_NAME'){
		// ref_tb_fd
		transfertype = '3';
	} else if(adf == 'REF_FD_NAME'){
		
		var  sss = '';
		 $.each(ref_tb_fd, function(i, tab_fd){
                        	
        	var _tablaname = tab_fd['TAB_NAME'];
            
            if(tablaname == _tablaname){
            	if(data == tab_fd['FD_NAME'])
            		sss = tab_fd['NAME'];
            }
        });
		return sss;
	}  else if(adf == 'SRC_EXPR'){
		
		var v = '';
		$.each(txnfdObj, function(key, value){
			
		    if (data == key) {
		    	v = value;
		    }
		});
		return v;
	} 
}

function buildTrAuto(colsOfOneTxn, type, cbdata_txnfds, single){

	if(type == 'MODEL'){
	    fd_arrs = fd_arr;
		transfer = txnNeedTransfer;
		$.each(colsOfOneTxn, function(n, attrsOfOneCol){
		    
			txnfdObj[attrsOfOneCol['REF_NAME']] = attrsOfOneCol['NAME'];
			$.each(fd_arrs, function(t, afd){
				
				var data;
				if (transfer.indexOf(afd) > -1) { // need transfer
					data = transferdata(afd, attrsOfOneCol[afd]);
				} else 
					data = attrsOfOneCol[afd];
				
				var model = new Model("", "", 'td', data, {"valign" : "middle", "align" : "center"}, true);
					
				insert(model, type);
			});

			insertSimpleTr();
		});
	} else {
	    fd_arrs = ref_arr;
		transfer = txnNeedTransferRef;
		var not_editable_data_array = new Array();
	    $.each(cbdata_txnfds, function (n, fdAttrsOfOneCol){
	    	
    		var currentTxn = txnId;
			var srcTxn = colsOfOneTxn.TAB_NAME;
			var everyRecordTxn = fdAttrsOfOneCol.TAB_NAME;
    			
	    	if(colsOfOneTxn.REF_ID == fdAttrsOfOneCol.REF_ID){ // refid相同,是一个reftab
	    		
	    		var is_self = txnId == everyRecordTxn ? true : false; // 是否是自己
	    		if(txnId.length > srcTxn.length && (is_self ? true : txnId.length > everyRecordTxn.length)){ // 1.下级交易不显示 2.同级中的只显示是自己的
    		
	    			var editable = currentTxn == everyRecordTxn; // 当前交易==记录中的交易
	    			
	    			if(editable){
	    				
					    var txn = new Array();
					    txn.push(fdAttrsOfOneCol.TAB_NAME);
					    $.each(txn_ref_tab_arry, function (idx, txndef){
					   		if(fdAttrsOfOneCol.TAB_NAME == txndef.TAB_NAME){
					   			txn.push(txndef.TAB_DESC);
					   			return false;
					   		}
					    });
					    buildRow(cbdata_txnfds, fdAttrsOfOneCol.REF_ID, everyRecordTxn, "REF", colsOfOneTxn.REF_TAB_NAME, txn);
					    return false; // 一次build一行
	    			} else {
	    			
			    		$.each(fd_arrs, function (n, field){
							
				    		var data = '';
		    				if (transfer.indexOf(field) > -1) { // need transfer
		    					data = transferdata(field, fdAttrsOfOneCol[field], colsOfOneTxn.REF_TAB_NAME);
		    				} else 
		    					data = fdAttrsOfOneCol[field];
		    				
	    				    var model = new Model("", "", 'td', '', { "value" : data }, editable);
							not_editable_data_array.push(model);
	    				    insert(model, type);
	    				
						});
	    			}
					insertSimpleTr();
	    		}
	    	}
	    });
	}
}

function init_ref(){
	var $tableObj = $('#fd_ref_tab');
	var initdata = '';
	initdata +=	'<tr style="font-weight: bold;" bgcolor="#CCCCD4" id="introduce____ref_tr">';
	initdata += '<td valign="middle" align="left" colspan="5">';
	initdata += '<span style="cursor: pointer;"  id="txn_ref_spn">';
	initdata += '-模型引用';
	initdata += '</span>';
	initdata += '</td>';
	initdata += '<td valign="middle" align="center" >';
	initdata += '<input id="addTab" class="btn" type="button" value="添加表" />'
	initdata += '</td>';
	initdata += '</tr>';
	
	initdata += '<tr>';
	initdata += '<td valign="middle" align="center">引用字段</td>' ;
	initdata += '<td valign="middle" align="center">引用属性代码</td>' ;
	initdata += '<td valign="middle" align="center">引用描述</td>';
	initdata += '<td valign="middle" align="center">条件</td>';
	initdata += '<td valign="middle" align="center">表达式</td>';
	initdata += '<td valign="middle" align="center">引用交易</td>';
	initdata += '</tr>';
    $tableObj.append(initdata);
    $('#fd_conf_form').append($tableObj);
	$('#txn_ref_spn').click(function(){
		
		var $type_tr = $('#introduce____ref_tr');
		var $trs = $type_tr.nextUntil('tbody');
		if($type_tr.next().is( ":hidden ")){
			$(this).html('-模型引用');
			$trs.show();
			
			$.each($trs.find('span'), function(i, dom_span){
				$(dom_span).text($(dom_span).text().replace('+', '-'));
			});
		} else {
			$(this).html('+模型引用');
			$trs.hide();
		}
	});
	
	function getTxnName(){
		return page.tree.getNode(page.tree.select()[0]).text;
	}
	var sss = 1;
	$('#addTab').click(function(){

		var intro_count = $('select[id*=REF_TAB_NAME]').length + 1;
		
		model = new Model(
			'introduce____REF____tr____' + txnId + '____' + intro_count + '____0____new'+sss++,
			'',
			'introduce',
			getTxnName()+'引用 '+intro_count +' ',  
			'',
			false
		);
		var txnIdAndName = new Array();
		txnIdAndName.push(txnId);
		txnIdAndName.push(getTxnName());
		//model.add("fdnew", true);
		model.add("txnIdAndName", txnIdAndName);
		insert(model, 'REF');
//    			model = new Model(
//					modelid,
//					'',
//					'introduce',
//					name + '引用[' + trid+ '] 引用字段['+src_expr_name +'] 引用表['+_name+']'+' 引用描述['+ref_desc+']' ,  
//					'',
//					_hassub
//				);
	});
}


function maketable(cbdata_txnfds, type){
	
	if(type == 'MODEL'){
		fd_arrs = fd_arr;
		transfer = txnNeedTransfer;
	} else {
		fd_arrs = ref_arr;
		transfer = txnNeedTransferRef;
	}
	
	var trid = 1;
	var txncount = cbdata_txnfds.length;
	
	$.each(cbdata_txnfds, function(i, txnfds){
		var _hassub = txncount - i > 1 ? true : false;
		var txntrid = 1;
		
		
		$.each(txnfds, function(txnKey, colsOfOneTxn){
			
			var hasSys = false;
			var is_sys_obj_arr = new Array();
			
			if(type == 'MODEL'){
				var not_sys_obj_arr = new Array();
				// 交易建模时,没有下级的时候是可编辑状态..但is_sys的是不可编辑的
				if(!_hassub){ // 当没有下级时候
					$.each(txnfds, function(txnKey, colsOfOneTxn){ // 扫描有没有IS_SYS的字段
						
						$.each(colsOfOneTxn, function(idx, oneTxn){ // 扫描有没有IS_SYS的字段
						
							if(oneTxn.IS_SYS == '1'){
								
								is_sys_obj_arr.push(oneTxn);
							} else {
								not_sys_obj_arr.push(oneTxn);
							}
						});
					});
					hasSys = true;
					colsOfOneTxn = not_sys_obj_arr;
				}
				
			}
			
			txnKey = txnKey.split("____");
			var id = txnKey[0];
			var name = txnKey[1];
			// id, datatype, domtype, value, attrs, hassub
			
			var modelid = 'introduce____' + type +'____tr____' + id + '____' + trid++ + '____' + (colsOfOneTxn.length + is_sys_obj_arr.length);
			// introduce tr
			var model = new Model(
				modelid,
				'',
				'introduce',
				name, 
				'',
				_hassub
			);
			model.add("coldata", colsOfOneTxn);
			insert(model, type);

			if(hasSys){
				insertSimpleTr();
				buildTrAuto(is_sys_obj_arr, type);
			}
			if(_hassub){
				insertSimpleTr();
				buildTrAuto(colsOfOneTxn, type);
				return true; // as continue
			}
			
			var model = new Model(
				'',
				'',
				'tr',
				'', 
				{'id' : id + '____' + txntrid},
				''
			);
			insert(model, type)
			
			$.each(colsOfOneTxn, function(n, attrsOfOneCol){
				
				$.each(fd_arrs, function(x, colname) {
// console.log(attrsOfOneCol[colname]);
					var arg2;
					if (colname == 'NAME') {
						arg2 = 'checkbox____text';
					} 
					else if(colname == 'TYPE') {
						arg2 = 'txntype';
					} 
// else if(colname == 'SRC_ID') {
// arg2 = 'txnsrc_id';
// }
					else if(colname == 'CODE') {
						arg2 = 'txncode';
					} 
					else if(colname == 'GENESISRUL') {
						arg2 = 'txngenesisrul';
					} 
// else if(colname == 'SRC_EXPR') {
// arg2 = 'checkbox____select';
// }
// else if(colname == 'REF_TAB_NAME') {
// arg2 = 'ref_tab_name';
// }
					else if(colname == 'REF_FD_NAME') {
						arg2 = 'checkbox____select';
					} 
					else {
						arg2 = 'text';
					}
					var _attrs;
				    if(type == 'MODEL'){
					    txnfdObj[attrsOfOneCol['REF_NAME']] = attrsOfOneCol['NAME'];
				    } 
				    _attrs = {"id" : type + '____' + colname+ '____' + txntrid, "size" : "10", "valign" : "middle", "align" : "right",  "value" : attrsOfOneCol[colname] };
					var model = new Model(
						'',
						arg2,
						'td',
						attrsOfOneCol[colname], 
						_attrs,
						false
					);
					model.add("coldata", attrsOfOneCol);
					insert(model, type);
				});
				insertSimpleTr();
				txntrid++
			});
		});
	}); 
	// trid = 1;
}

function makereftable(cbdata_txnfds, cbdata__reftab, type){
	
	fd_arrs = ref_arr;
	transfer = txnNeedTransferRef;
	var trid = 1;
	var txncount = cbdata_txnfds.length;
	var reftabcount = cbdata__reftab.length;
	var selectTxnId = txnId;
		
	$.each(cbdata__reftab, function(txntabs_idx, txntabs){
		var _hassub = reftabcount - txntabs_idx > 1 ? true : false;
		$.each(txntabs, function(txnKey, colsOfOneTxn){
			var txntrid = 1; 
			var txnIdAndName = txnKey.split("____");
			var id = txnIdAndName[0];
			var name = txnIdAndName[1];
//					if(colsOfOneTxn.length == 0 && _hassub){ // 没值的时候默认来个intro
//						var modelid = 'introduce____' + type +'____tr____' + id + '____' + trid + '____' + 0;
//						model = new Model(
//							modelid,
//							'',
//							'introduce',
//							name + '引用 ' + trid+ ' ' , 
//							'',
//							_hassub
//						);
//						insert(model, type);
//					}
			
			$.each(colsOfOneTxn, function(n, attrsOfOneCol){
				var tabname = attrsOfOneCol.REF_TAB_NAME;
				var src_expr = attrsOfOneCol.SRC_EXPR;
				var ref_desc = attrsOfOneCol.REF_DESC;
				var ref_id = attrsOfOneCol.REF_ID;
				
				
				var fdcount = 0;
			
				$.each(cbdata_txnfds, function(fda, fdAttrsOfOneCol){
					if(fdAttrsOfOneCol.REF_ID == attrsOfOneCol.REF_ID){
					   fdcount++;
					}
				});
				
				// add intro begin
				var modelid = 'introduce____' + type +'____tr____' + id + '____' + trid + '____' + fdcount + '____' +  attrsOfOneCol.REF_ID;
				
				var model  ;
				if(_hassub){
					var _name = '';
					$.each(ref_tb_fd, function(i, tab_fd){
						
                        if(tabname == tab_fd['TAB_NAME']){
                        	_name = tab_fd['TAB_DESC'];
                        	return false;
                        }
                    });
                    
                    var src_expr_name = '';
                    $.each(txnfdObj, function(key, value){
					    if (src_expr == key) {
					        src_expr_name = value;
					     	return false;
					    }
					});
					
					model = new Model(
						modelid,
						'',
						'introduce',
						name + '引用[' + trid+ '] 引用字段['+src_expr_name +'] 引用表['+_name+']'+' 引用描述['+ref_desc+']' ,  
						'',
						_hassub
					);
					
				} else {
					
					model = new Model(
						modelid,
						'',
						'introduce',
						name + '引用 ' + trid+ ' ' , 
						'',
						_hassub
					);
				}
				
				model.add("tabname", tabname);
				model.add("src_expr", src_expr);
				model.add("tab_ref_desc", ref_desc);
				model.add("fdnew", false);
				model.add("txnIdAndName", txnIdAndName);
				insert(model, type);
				// add intro end
				
				if(_hassub){
					insertSimpleTr();
					buildTrAuto(attrsOfOneCol, type, cbdata_txnfds);
					trid++;
					return true; // as continue
				}
				
				var model = new Model(
					'',
					'',
					'tr',
					'', 
					{'id' : id + '____' + txntrid},
					''
				);
				insert(model, type)
				buildRow(cbdata_txnfds, attrsOfOneCol.REF_ID, selectTxnId, type, tabname, txnIdAndName);
				trid++
			});
			trid = 1;
			txntrid++
		});
	}); 
}

function buildRow(cbdata_txnfds, ref_id, selectTxnId, type, tabname, txnIdAndName){
	
	var reffdcount = 1;
	$.each(cbdata_txnfds, function(fda, fdAttrsOfOneCol){
		if(fdAttrsOfOneCol.REF_ID == ref_id){
			if (selectTxnId == fdAttrsOfOneCol.TAB_NAME){ // 在当前交易定义的ref
			    $.each(fd_arrs, function(x, colname) {
					var arg2;
                    if(colname == 'TAB_NAME') {
						arg2 = 'noele';
					} 
					else if(colname == 'REF_FD_NAME') {
						arg2 = 'checkbox____select';
					} 
					else {
						arg2 = 'text';
					}
					var _attrs;
//									    _attrs = {"id" : type + '____' + colname+ '____' + reffdcount + '____' + fdAttrsOfOneCol.REF_ID+ '____' + fdAttrsOfOneCol.TAB_NAME, "size" : "10", "valign" : "middle", "align" : "right",  "value" : fdAttrsOfOneCol[colname] };
				    _attrs = {"id" : type + '____' + colname+ '____' + reffdcount + '____' + fdAttrsOfOneCol.REF_ID, "size" : "10", "valign" : "middle", "align" : "right",  "value" : fdAttrsOfOneCol[colname] };
					var model = new Model(
						'',
						arg2,
						'td',
						fdAttrsOfOneCol[colname], 
						_attrs,
						false
					);
					fdAttrsOfOneCol.REF_TAB_NAME = tabname;
					model.add("coldata", fdAttrsOfOneCol);
					model.add("txnIdAndName", txnIdAndName);
					insert(model, type);
				});
				insertSimpleTr();
				reffdcount++;
			}
		}
	});
}

function initTxnMdl(txnIdCallback) {
	txnId = txnIdCallback;
	var table = intiTable(txnId.length / 2 + 1, 7);
	jcl.postJSON('/tms35/tranmdl/query', 'tab_name='+txnId, function(cbdata){
		code_data = cbdata.sourceType;
		funcs = cbdata.func;
		func_Param = cbdata.funcParam;
		ref_tb_fd = cbdata.refTblFd;
		// model
		maketable(cbdata.txnfds, 'MODEL');
//$('#fd_model_tab').hide();
		// ref
		txn_ref_tab_arry = cbdata.all_txn_def;
		init_ref();
		cbdata.txn_ref_tab ? makereftable(cbdata.txn_ref_fds, cbdata.txn_ref_tab, 'REF') : '';
		
		var _savebtn = $('<input>', {id : "fd_conf_savebtn", "class" : "btn", type : "button", value : "保存"})
		var _canclbtn = $('<input>', {id : "fd_conf_canclbtn", "class" : "btn", type : "button", value : "取消"})
		$('#fd_conf_form').append('<BR/>');
		$('#fd_conf_form').append(_savebtn);
		$('#fd_conf_form').append(_canclbtn);
//$('#formbox-txn_mdl input[type=text]');
		_savebtn.click(function(){
			
			var params = $('#fd_conf_form').serialize();
			// 1 refname 不能重复
			$('.error-text').removeClass();
			var ref_name_arr = [];
			var _ref_name_obj = [];
			var same_code_error = false;
			var mdl_notnull_error = false;
			var fd_notnull_error = false;
			
			$('#formbox-txn_mdl input[id*="REF_NAME"]').each(function (idx, ref_name){
				
				var elm = $(ref_name).val();
				
				if(elm == ""){
					mdl_notnull_error = true;
					$(ref_name).focus();
					$(ref_name).addClass('error-text');
					return false;
				}
				
				var same_idx = jQuery.inArray(elm, ref_name_arr);
				
				if(same_idx > 0){
					$(ref_name).focus();
					$(_ref_name_obj[same_idx]).addClass('error-text');
					same_code_error = true;
					return false;
				} else {
					ref_name_arr.push(elm);
					_ref_name_obj.push($(ref_name));
				}
			});

			$('#formbox-txn_mdl input[name*="ARG"]').each(function (idx, arg){
				
				var elm = $(arg).val();
				
				if(elm == ""){
					mdl_notnull_error = true;
					$(arg).focus();
					$(arg).addClass('error-text');
				}
			});
			
			$('#fd_ref_tab input[id*="TAB_REF_DESC"], #fd_ref_tab input[id*="REF_DESC"]').each(function (idx, element){
				
				if($(element).val() == ''){
					fd_notnull_error = true;
					$(element).focus();
					$(element).addClass('error-text');
				} 
			});


			
			if(fd_notnull_error || mdl_notnull_error){
				alert("有必填项为空");	
				return true;
			} else if(same_code_error) {
				alert("有字段重复项"); 
				return true;
			}
			
			jcl.postJSON('/tms35/tranmdl/save', params + '&txnId=' + txnId, function(data){
				initTxnMdl(txnId);
			});
		});
		
		_canclbtn.click(function(){
			initTxnMdl(txnId);
		});
	});
	
	$("#formbox-txn_mdl").show();
	$("#formbox-txn_mdl tr").css('height', '24px');
}

