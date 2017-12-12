(function($) {
condEdit = function(){
	
	var txnFeatureData;// 交易属性下拉列表数据
	var statFuncData;// 统计函数下拉列表数据
	var operFuncData;// 关系函数下拉列表数据
	var ruleFuncData;// 规则下拉列表数据
	var dateFuncData;// 日期下拉列表数据
	var strFuncData;// 字符串下拉列表数据
	var rosterFuncData;// 名单下拉列表数据
	var diyFuncData;// 自定义函数下拉列表数据
	var acFuncData;// 动作函数下拉列表数据
	var all_dic;
	var $fram = '';
	var this_txnId = '';
	var allConds = [
 	   'STAT_DATAFD',
 	   'STAT_FN',
 	   'DATE_FUNC',
 	   'STR_FUNC',
 	   'OPER_FUNC',
 	   'ROSTER_FUNC',
 	   'DIY_FUNC',
 	   'RULE_FUNC',
 	   'AC_FUNC'
 	];
	
	var cond_form = new jcl.ui.Form({
			id: 'cond_form_id',
			layout: 'auto',
			items:[
			   {name:'STAT_DATAFD', label:'交易属性', type:'selector', items:txnFeatureData},
			   {name:'STAT_FN', label:'交易统计', type:'treeselector',title:'--请选择--', items:statFuncData,sm:{type:"row"},rtNode:false, multiple:false, cascadeCheck:false},
			   {name:'DATE_FUNC', label:'日期函数', type:'selector', items:dateFuncData},
			   {name:'STR_FUNC', label:'字符函数', type:'selector', items:strFuncData},
			   {name:'OPER_FUNC', label:'关系函数', type:'selector', items:operFuncData},
			   {name:'ROSTER_FUNC', label:'名单', type:'selector', items:rosterFuncData},
			   {name:'DIY_FUNC', label:'自定义函数', type:'selector', items:diyFuncData},
			   {name:'RULE_FUNC', label:'交易规则', type:'selector', items:ruleFuncData},
			   {name:'AC_FUNC', label:'动作函数', type:'selector', items:diyFuncData},
			   {name:'STAT_COND_VALUE', label:'条件表达式',type:'textarea',cls:"cond-textarea"},
			   {name:'STAT_COND_IN', label:'解释',type:'textarea',cls:"cond-textarea"}
			],
			btns:[
                 {text:'确定', id:'cond_updateBtn'},
                 {text:'重置', id:'cond_resetBtn'},
                 {text:'取消', id:'cond_cancelBtn'}
               ]
    	});
		
	var	cond_dialog = new jcl.ui.Dialog({
			title: '条件',
			marginTop: 0,
			width:650,
			"zindex" : '701',
			draggable: true
		});
	cond_dialog.addDom(cond_form.jqDom);
	
	// 汉译英
	this.c2e = function(){
		//searchCondName("ddddd",2);
	};
	
	this.cond_form = function(){
		return cond_form;
	};
	
	this.init_cond = function(arg,arg_in,txnId, hideConds,cond_title){
			condFormReset();
			//cond_form.getItem('STAT_COND_IN').jqDom.find('[name=STAT_COND_IN]').attr('readonly',"readonly");
			$fram = arg instanceof jQuery ? arg : $(arg);
			$fram_in = arg_in instanceof jQuery ? arg_in : $(arg_in);
			
			// 设置标题
			if(cond_title == undefined || cond_title == null) cond_title = '条件';
			cond_dialog.setTitle(cond_title);
			
			// 设置默认值
			var defaultValue = $fram.val();
			var defaultValue_in = $fram_in.val();
			cond_form.jqDom.find('[name=STAT_COND_VALUE]').val(defaultValue);
			cond_form.jqDom.find('[name=STAT_COND_IN]').val(defaultValue_in);
			
 			this_txnId = txnId;
 			var hides = hideConds ? hideConds : [];
 			/*$.each(allConds, function(i, id){
 				cond_form.enable(id);
 				if($.inArray(id, hides)!=-1){
					cond_form.disable(id);
 				}
 			});*/
			
			all_dic = new Array();
			// 规则列表
			jcl.postJSON('/tms/stat/txnrulelist', 'txn_id=' + txnId, function(data){
				ruleFuncData = new Array();
				ruleFuncData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					ruleFuncData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
					all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
				});
				cond_form.getItem('RULE_FUNC').component.reload(ruleFuncData,{text:'CODE_VALUE',value:'CODE_KEY'});
			});
		
			// 统计目标
			jcl.postJSON('/tms/stat/txnFeature', 'txn_id=' + txnId, function(data){
				txnFeatureData = new Array();
				txnFeatureData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					txnFeatureData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
					all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
				});
				cond_form.getItem('STAT_DATAFD').component.reload(txnFeatureData,{text:'CODE_VALUE',value:'CODE_KEY'});
			});
		
			// 关系函数
			jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='3'", function(data){
				operFuncData = new Array();
				operFuncData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					operFuncData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
				});
				cond_form.getItem('OPER_FUNC').component.reload(operFuncData,{text:'CODE_VALUE',value:'CODE_KEY'});	
			});
		
			// 自定义函数
			jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='1'", function(data){
				diyFuncData = new Array();
				diyFuncData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					diyFuncData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
					all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
				});
				
				cond_form.getItem('DIY_FUNC').component.reload(diyFuncData,{text:'CODE_VALUE',value:'CODE_KEY'});		
			});
			// 动作函数
			jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='7'", function(data){
				acFuncData = new Array();
				acFuncData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					acFuncData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
					all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
				});
				
				cond_form.getItem('AC_FUNC').component.reload(acFuncData,{text:'CODE_VALUE',value:'CODE_KEY'});		
			});
		
			// 日期函数
			jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='4'", function(data){
				dateFuncData = new Array();
				dateFuncData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					dateFuncData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
					all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
				});
				cond_form.getItem('DATE_FUNC').component.reload(dateFuncData,{text:'CODE_VALUE',value:'CODE_KEY'});
			});
		
			// 字符串函数
			jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='5'", function(data){
				strFuncData = new Array();
				strFuncData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					strFuncData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
					all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
				});
				cond_form.getItem('STR_FUNC').component.reload(strFuncData,{text:'CODE_VALUE',value:'CODE_KEY'});
			});
			// 名单函数
			jcl.postJSON('/tms/stat/code', "category_id=tms.pub.roster", function(data){
				rosterFuncData = new Array();
				rosterFuncData.push({CODE_KEY:'',CODE_VALUE:'--请选择--' });
				$.each(data.row, function(i, row){
					rosterFuncData.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' });
					all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
				});
				cond_form.getItem('ROSTER_FUNC').component.reload(rosterFuncData,{text:'CODE_VALUE',value:'CODE_KEY'});
			});
					
			// 统计函数
			jcl.postJSON('/tms/stat/statfunc', "", function(data){
				statFuncData = new Array();
				$.each(data.row, function(i, row){
					statFuncData.push({id:row['ID'],fid:row['FID'],ftype:row['FTYPE'],text:row['CODE_VALUE'] + '('+row['CODE_KEY']+ ')' ,code_key:row['CODE_KEY']});
					// 统计（叶子节点）
					if(row['FTYPE']==2){
						all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});						
					}
				});
				cond_form.getItem('STAT_FN').component.reload(statFuncData);
			});
			
			setTimeout(function(){
				if($fram.val() && $fram.val().length > 0 && $fram_in.val() && $fram.val().length == 0 ){
					searchCondName($fram.val());
				}
			}, 500);
			
			cond_dialog.show();
			$.each(allConds, function(i, id){
 				if($.inArray(id, hides)!=-1){
					cond_form.disable(id);
 				} else {
 					cond_form.enable(id);
 				}
 			});
		}
		
	//保存
	cond_form.jqDom.find('#cond_updateBtn').click(function(){
		var cond_value = cond_form.jqDom.find('[name=STAT_COND_VALUE]').val();
		var cond_value_in = cond_form.jqDom.find('[name=STAT_COND_IN]').val();
		if(!checkeCondSpecialCode(cond_value)){
			alert('条件不能包含特殊字符');
			return;
		}
		
		if(cond_value == ''){
			$fram.val(cond_value);
			$fram_in.val(cond_value_in);
			cond_dialog.hide();
		}else{
			var cond_value_encode = encodeURIComponent(cond_value);
			var params = "TXNID="+this_txnId+ "&STAT_COND_VALUE="+ cond_value_encode;
			//jcl.postJSON('/tms/stat/checkCond', params, function(data){
				$fram.val(cond_value);
				$fram_in.val(cond_value_in);
				cond_dialog.hide();
			//});			
		}
	});
		
	//重置
	cond_form.jqDom.find('#cond_resetBtn').click(function(){
		condFormReset();
	});
		
	//取消
	cond_form.jqDom.find('#cond_cancelBtn').click(function(){
		cond_dialog.hide();
	});
	
	//条件域输入事件
	cond_form.getItem("STAT_COND_VALUE").component.keyup(function(){
		searchCondName($(this).val());
	});
	//条件域输入事件
	cond_form.getItem("STAT_COND_IN").component.keyup(function(){
		searchCondName($(this).val(),2);
	});
	// 规则选择事件
	cond_form.getItem("RULE_FUNC").component.onChange(function(_this){
		confirmCond(_this.value,'RULE_FUNC');
	});
	// 交易属性选择事件
	cond_form.getItem("STAT_DATAFD").component.onChange(function(_this){
		confirmCond(_this.value,'STAT_DATAFD');
	});
	// 交易统计选择事件
	cond_form.getItem("STAT_FN").component.tree.onSelectChange(function(){
		var t = cond_form.getItem("STAT_FN").component.tree;
		var list = t.select();
		if (list.length > 0) {
			var s_node = t.getNode(list[0]);
			confirmCond(s_node.code_key,'STAT_FN');
		}
	});
	// 日期函数选择事件
	cond_form.getItem("DATE_FUNC").component.onChange(function(_this){
		confirmCond(_this.value != '' ? _this.value+'()':'','DATE_FUNC');
	});
	// 字符串选择事件
	cond_form.getItem("STR_FUNC").component.onChange(function(_this){
		confirmCond(_this.value != '' ? _this.value+'()':'','STR_FUNC');
	});
	// 关系函数选择事件
	cond_form.getItem("OPER_FUNC").component.onChange(function(_this){
		confirmCond(_this.value,'OPER_FUNC');
	});
	// 名单选择事件
	cond_form.getItem("ROSTER_FUNC").component.onChange(function(_this){
		confirmCond(_this.value,'ROSTER_FUNC');
	});
	// 自定义函数选择事件
	cond_form.getItem("DIY_FUNC").component.onChange(function(_this){
		confirmCond(_this.value != '' ? _this.value+'()':'','DIY_FUNC');
	});
	// 动作函数选择事件
	cond_form.getItem("AC_FUNC").component.onChange(function(_this){
		confirmCond(_this.value != '' ? _this.value+'()':'','AC_FUNC');
	});
	
	function condFormReset(){
		cond_form.getItem("STAT_FN").jqDom.find('[type=text]').val("--请选择--");
		cond_form.getItem("STAT_FN").component.tree.jqDom.find('.selected').removeClass('selected');
		cond_form.reset();
	}
	

	var selectArea = [0, 0];
	cond_form.getItem('STAT_COND_VALUE').component.on('keydown keyup mousedown mouseup focus', function(e){
		selectArea = getSelectedPostion(this);
	});

	function confirmCond(selected_val,it){
		$.each(allConds, function(i, id){
			if(it != id){
				cond_form.getItem(id).val("");
				if(id == 'STAT_FN' && it != 'STAT_FN'){
					cond_form.getItem("STAT_FN").jqDom.find('[type=text]').val("--请选择--");
					cond_form.getItem("STAT_FN").component.tree.jqDom.find('.selected').removeClass('selected');
				}
					
			}
 		});
		
		var cond_val_o = cond_form.jqDom.find('[name=STAT_COND_VALUE]');
		
		cond_val_o.val(getResetVal(selected_val,cond_val_o.val(),selectArea));
		
		selectArea[0] = parseInt(selectArea[0])+(("").lengths());
		selectArea[1] = parseInt(selectArea[0])+((selected_val+"").lengths());
		
		// firfox
		if (typeof(cond_val_o[0].selectionStart) == "number") {
			cond_val_o[0].selectionStart = selectArea[1];
			cond_val_o[0].selectionEnd = selectArea[1];
		}else{
			var r = cond_val_o[0].createTextRange(); 
		    r.collapse(true); 
		    r.moveStart('character',selectArea[1]);
		    r.select();			
		}

		searchCondName(cond_form.jqDom.find('[name=STAT_COND_VALUE]').val());
		cond_form.jqDom.find('[name=STAT_COND_VALUE]').focus();
		// 插入值
		//insertAtCursor(cond_val_o,selected_val);
	}

	function insertAtCursor(myField, my_v) {
		var myValue = String(my_v);
	     //IE support
	     if (document.selection) {
			 myField.focus();
	         var sel = document.selection.createRange();
	         sel.text = myValue;
	         sel.select();
	     }
	     //MOZILLA/NETSCAPE support
	     else if (myField.selectionStart || myField.selectionStart == '0') {
	         var startPos = myField.selectionStart;
	         var endPos = myField.selectionEnd;
	         // save scrollTop before insert
	         var restoreTop = myField.scrollTop;
	         myField.value = myField.value.substring(0, startPos) + myValue + myField.value.substring(endPos, myField.value.length);
	         if (restoreTop > 0) {
	         myField.scrollTop = restoreTop;
	         }
	         myField.focus();
	         myField.selectionStart = startPos + myValue.length;
	         myField.selectionEnd = startPos + myValue.length;
	     } else {
	         myField.value += myValue;
	         myField.focus();
	     }
	}
	function moveEnd(obj){
	    obj.focus();
	    var len = obj.value.length;
	    if (document.selection) {
	        var sel = obj.createTextRange();
	        sel.moveStart('character',len);
	        sel.collapse();
	        sel.select();
	    } else if (typeof obj.selectionStart == 'number' && typeof obj.selectionEnd == 'number') {
	        obj.selectionStart = obj.selectionEnd = len;
	    }
	
	}

 	// kind 1:英译汉 2:汉译英。默认是英译汉
	function searchCondName(cond_value,kind){
			if (cond_value == '' || cond_value == null) {
				cond_form.jqDom.find('[name=STAT_COND_IN]').val(cond_value);
				cond_form.jqDom.find('[name=STAT_COND_VALUE]').val(cond_value);
				return;
			}
			
			var key = 'CODE_KEY';
			var val = 'CODE_VALUE';
			if(kind == 2){
				val = 'CODE_KEY';
				key = 'CODE_VALUE';
			}
						
			var condname = cond_value;
			$.each(all_dic, function(i, row){
				if (row['CODE_KEY'] ) {
					condname = condname.replace(new RegExp(row[key].replace('(','\\(').replace(')','\\)'), "g"), row[val]);
				}
			});
			
			if(kind == 2){
				cond_form.jqDom.find('[name=STAT_COND_VALUE]').val(condname);
			}else{
				cond_form.jqDom.find('[name=STAT_COND_IN]').val(condname);	
			}
			
		}	
}


  /**
	 * 获取重置后的值
	 * @param from 来源值
	 * @param to   目的值
	 * @param area 位置坐标
	 * @return 
	 */
	function getResetVal(from, to, area){
        var pre = to.substr(0, area[0]);
        var post = to.substr(area[1]);
		var _val = [pre, from, post];
		return _val.join('');
    }
	
	/**
	 * 获取选中的位置范围
	 * @param e 所监听的元素对象
	 * @return selectArea 选中区域的坐标地址[开始,结束]
	 */
	function getSelectedPostion(e){
		var selectArea = [0, 0];
		var terget = e instanceof jQuery ? e.get(0) : e;
        //如果是Firefox(1.5)的话，方法很简单
        if(typeof(terget.selectionStart) == "number"){
			selectArea = [terget.selectionStart, terget.selectionEnd];
        }
        //下面是IE(6.0)的方法，麻烦得很，还要计算上'\n'
        else if(document.selection){
            var start = 0, end = 0, 
			range = document.selection.createRange();
            //if(range.parentElement().id == textBox.id){
			//console.log(range.parentElement());
			//console.log(terget);
			if(range.parentElement() == terget){
				// create a selection of the whole textarea
                var range_all = document.body.createTextRange();
                range_all.moveToElementText(terget);
                //两个range，一个是已经选择的text(range)，一个是整个textarea(range_all)
                //range_all.compareEndPoints()比较两个端点，如果range_all比range更往左(further to the left)，则                //返回小于0的值，则range_all往右移一点，直到两个range的start相同。
                // calculate selection start point by moving beginning of range_all to beginning of range
                for (start=0; range_all.compareEndPoints("StartToStart", range) < 0; start++){
                    range_all.moveStart('character', 1);
				}
                // get number of line breaks from textarea start to selection start and add them to start
                // 计算一下\n
                for (var i = 0; i <= start; i ++){
                    if (terget.value.charAt(i) == '\n')
                        start++;
                }
                // create a selection of the whole textarea
                 var range_all = document.body.createTextRange();
                 range_all.moveToElementText(terget);
                 // calculate selection end point by moving beginning of range_all to end of range
                 for (end = 0; range_all.compareEndPoints('StartToEnd', range) < 0; end ++){
                     range_all.moveStart('character', 1);
				 }
                     // get number of line breaks from textarea start to selection end and add them to end
				 for (var i = 0; i <= end; i ++){
					 if (terget.value.charAt(i) == '\n')
						 end ++;
				 }
			}
			selectArea = [start, end];
        }
        return selectArea;
    }
	
})(jQuery);
