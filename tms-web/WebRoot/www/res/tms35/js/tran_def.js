var model = window.model||{};

(function(){
	
	var mdl_def_add = null;
	var dirty = false;
	var channelList = {};
	var txnId = '';
	var _page= {};
	var txndefinfos = {};
	var dis_selector_mod = null, dis_selector_add = null;
	model.txnDef = {
		init: function(_txnId, page){
			txnId = _txnId;
			var tree = page.tree;
			
			mdl_def_add = new jcl.ui.Dialog({"title" : '新建交易', "zindex" : '701', "draggable" : "true"});
			mdl_def_add.addDom('#formbox-txn_def_add');
			
			$('#formbox-txn_def_add').on('click', '#add_txnmodSubmitButton', function(){
				$('#formbox-txn_def_add').find('#op').val('add');
				saveTxnSubmit('#formbox-txn_def_add', 'add');
			});
			
			$('#formbox-txn_def_add').on('click', '#add_txncancelSubmitButton', function(){
				mdl_def_add.hide();
			});
			
			$('#formbox-txn_def_add').on('click', ':radio[name=txn_type]', function(){
				var $txn_li = $('#formbox-txn_def_add').find('#txnid_li');

				if ("0" == $(this).val()) { //交易组
					$txn_li.hide();
					$('#formbox-txn_def_add').find('#txnid').val('');
					$('#formbox-txn_def_add').find("#chann_color").hide();
				} else {
					$txn_li.show();
					$('#formbox-txn_def_add').find("#chann_color").show();
				}
			});
			
			$('#formbox-txn_def').on('click', '#txnmodSubmitButton', function(){
				var node = tree.getNode(tree.select()[0]);
				$('#formbox-txn_def_add').find('#tab_name').val(node.id);
				$('#formbox-txn_def_add').find('#parent_tab').val(node.fid);
				$('#formbox-txn_def_add').find('#op').val('mod');
				
				saveTxnSubmit('#formbox-txn_def', 'mod');
			});
			
			$('#formbox-txn_def').on('click', ':radio[name=txn_type]', function(){
				
				var $txn_li = $('#formbox-txn_def').find('#txnid_li');
				//console.log($(this));
				if ("0" == $(this).val()) { //交易组
					$txn_li.hide();
					$('#formbox-txn_def').find('#txnid').val('');
					$('#formbox-txn_def').find("#chann_color").hide();
				} else {
					$txn_li.show();
					$('#formbox-txn_def').find("#chann_color").show();
				}
			});
			
			page.toolbar.onClick('#tree-newtransation', function(){
				addTxn(tree.getNode(txnId));
			});
			page.toolbar.onClick('#tree-deltransation', function(){
				var tab_desc = $('#formbox-txn_def #tab_desc').val();
				delTxn(txnId, tab_desc);
			});
			
			_page = page;
			
			this.load(txnId);
		},
		load: function(txnIdCallback){
			
			// 模型状态下拉列表
			jcl.code.selector('MODELUSED','tms.model.modelused');
			
			txnId = txnIdCallback;
			
			jcl.postJSON('/tms35/trandef/edit_prepare', 'tab_name='+txnId, function(data){
				txndefinfos = data.infos;
				channelList = data.channs;
				editTxn(data.infos);
				
				if(dualAudit){ // readonly mode
					
					_page.toolbar.disable($('#tree-newtransation'), false);
					_page.toolbar.disable($('#tree-deltransation'), false);
					
					$('#formbox-txn_def').find('label.list-box-item-label > font').hide();
					$('#formbox-txn_def').find('#tab_desc').prop('disabled', true);
					$('#formbox-txn_def').find('#show_order').prop('disabled', true);
					$('#formbox-txn_def').find('[name=txn_type]').prop('disabled', true);
					$('#formbox-txn_def').find('#chann').prop('disabled', true);
					$('#formbox-txn_def').find("[name=is_enable]").prop('disabled', true);
					$('#formbox-txn_def').find("#txnmodSubmitButton").prop('disabled', true);
					$('#formbox-txn_def').find("#txnid").prop('disabled', true);
					$('#formbox-txn_def').find("#MODELUSED").prop('disabled', true);
				}
			});

			
		},
		isDirty: function(){
			return dirty;
		},
		save: function(){
			
		}
			
	};
	
	function addTxn(parentNode){
		
		initTab_disposal("add");
			
		// init data
		$('#formbox-txn_def_add').find('#parent_tab').val(parentNode.id);
		$('#formbox-txn_def_add').find('#txnffname').val(parentNode.text);
		
		$('#formbox-txn_def_add').find('#show_order').val('0');
		$('#formbox-txn_def_add').find('#tab_desc').val('');
		$('#formbox-txn_def_add').find('#txnid').val('');
		
		$("#formbox-txn_def_add :radio[name=is_enable][value='0']").prop('checked', true);
		$("#formbox-txn_def_add :radio[name=txn_type][value='0']").prop('checked', true);
		$('#formbox-txn_def_add').find('#txnid_li').hide();
		
		var $chn      = $('#formbox-txn_def').find('#chann');
		var $chn_span = $('#formbox-txn_def').find('#chann_span');
		
		var is_chn_hid = $chn.is(':hidden');
		var selected = $chn.val();
		
		var $add_chann = $('#formbox-txn_def_add').find('#chann');
		var $add_chann_span = $('#formbox-txn_def_add').find('#chann_span');
		
		var channelHtml = '<option value="">请选择</option>';
		for(var i=0;i<channelList.length;i++){
			channelHtml += '<option value="'+channelList[i]["CHANNELID"]+'">'+channelList[i]["CHANNELNAME"]+'</option>';
		}
		$add_chann.html(channelHtml);

		//console.log(txndefinfos);
		var text = txndefinfos.CHANN == null || txndefinfos.CHANN == "" ? '请选择' : getChann(txndefinfos.CHANN);
		var channval = txndefinfos.CHANN == null ? '' : txndefinfos.CHANN;
		
		if(is_chn_hid){ // 父集是继承来的渠道
			
			$add_chann_span.html($chn_span.html()).show();
			$add_chann.val(channval).hide();
		}else{ // 父集是框
			if(channval != ''){ //框里有值
				$add_chann_span.html(text).show();
				$add_chann.val(channval).hide();
			} else{ //框里是请选择
				$add_chann.show();
				$add_chann_span.hide();
			}
		}
		mdl_def_add.show();
	}
	
	function getChann(chann){
		for(var i=0;i<channelList.length;i++){
			if(channelList[i]["CHANNELID"] == chann){
				return channelList[i]["CHANNELNAME"];
			}
		}
	}
	
	//删除交易
	function delTxn(txnId, tab_desc){
		
		if(confirm(jcl.message.get("ui.tms.pub.del.confirm"))){
			
			jcl.postJSON('/tms35/trandef/save', 'tab_name='+txnId+'&op=del&tab_desc='+tab_desc, function(data){
				var info = '';
				if(data.success){
					info += jcl.message.get("ui.tms.pub.del.success");
				}
				alert(info);
				jcl.go("/dualaudit/tms35/trandef/init");
			});
		}
		return false;
	}
	
	function initTab_disposal(oper_type,tab_dis,fullInfo){
		var node = tree.getNode(tree.select()[0]);
		
		var div_disposal = oper_type=='mod' ? 'formbox-txn_def':'formbox-txn_def_add';
		var txn_id = oper_type=='mod' ? node.fid:node.id;
		var dis_selector = oper_type=='mod' ? dis_selector_mod : dis_selector_add;
		if (dis_selector == null) {
			var $disposal_span = $('#'+div_disposal).find('#disposal_span');
			$disposal_span.empty();
			var dis_o = {
				id: "TAB_DISPOSAL",
				label: "处置策略",
				name: "TAB_DISPOSAL",
				title: "--请选择--"
			};
			dis_selector = new jcl.ui.ListSelector(dis_o, $disposal_span);
		}
		jcl.postJSON('/tms35/rule/disposal', "txn_id="+txn_id, function(data1){
			var dis="";

			$.each(data1.row, function(i, arr) {
				dis += arr.DP_CODE+",";
			});
			if(dis.length>0){
				dis = dis.substring(0,dis.length-1);
			}
			
			if(tab_dis && tab_dis!=null && tab_dis.length>0){
				dis = tab_dis;
			}
			dis_selector.enable();
			dis_selector.reload(data1, {key:'row', text:'DP_NAME', value:'DP_CODE'});
			dis_selector.render(dis);
			if (dualAudit) {
				dis_selector.disable();
			} else {
				if (fullInfo && fullInfo != null) {
					if(fullInfo['PARENT_TAB'] == fullInfo['TAB_NAME']){
						dis_selector.disable();
					}
				}
			}
		});
	}
	
	//编辑交易
	function editTxn(fullInfo){
		
		var par_chann = fullInfo.PAR_CHANN;
		var txnid = fullInfo.TXNID;
		var enable = fullInfo.IS_ENABLE;
		
		var chann = fullInfo.CHANN;
		var disposal = fullInfo.TAB_DISPOSAL;
		
		initTab_disposal("mod",disposal,fullInfo);
		
		var modelused = fullInfo['MODELUSED'];
		if(modelused == null || modelused == 0){
			$('#formbox-txn_def').find("#MODELUSED option[value='2']").remove();
		}
		
		var channelHtml = '';
		if(chann == null || chann == '' || chann == undefined){
			
			channelHtml = '<option value="">请选择</option>';
			for(var i=0;i<channelList.length;i++){
				channelHtml += '<option value="'+channelList[i]["CHANNELID"]+'">'+channelList[i]["CHANNELNAME"]+'</option>';
			}
		} else {
			channelHtml = '<option value="">请选择</option>';
			for(var i=0;i<channelList.length;i++){
				if(chann == channelList[i]["CHANNELID"] ){
					channelHtml += '<option selected="selected" value="'+channelList[i]["CHANNELID"]+'">'+channelList[i]["CHANNELNAME"]+'</option>';
				}else
					channelHtml += '<option value="'+channelList[i]["CHANNELID"]+'">'+channelList[i]["CHANNELNAME"]+'</option>';
			}
		}
		$('#formbox-txn_def').find("#chann").html(channelHtml);		
		
		if(par_chann == null || par_chann == '' || undefined == par_chann){ //父集无渠道
			
			$('#formbox-txn_def').find("#chann").show();
			$('#formbox-txn_def').find("#chann_span").hide();
		} else {
			for(var i=0;i<channelList.length;i++){
				if(par_chann == channelList[i]["CHANNELID"] ){
					$('#formbox-txn_def').find('#chann_span').html(channelList[i]["CHANNELNAME"]).show();
					break;
				}
			}
			
			$('#formbox-txn_def').find("#chann").hide();
		}
		var $txn_li = $('#formbox-txn_def').find('#txnid_li');
		
		if(enable == '1'){
			$("#formbox-txn_def :radio[name=is_enable][value='1']").prop('checked', true);
		} else {
			$("#formbox-txn_def :radio[name=is_enable][value='0']").prop('checked', true);
		}
			
		if(txnid == null || txnid.trim() == '' || undefined == txnid){ // 无标识,为交易组
			$('#formbox-txn_def :radio[name=txn_type][value=0]').prop('checked', true);
			$txn_li.hide();
			$('#formbox-txn_def').find("#chann_color").hide();
		} else {
			$('#formbox-txn_def :radio[name=txn_type][value=1]').prop('checked', true);
			$txn_li.show();
			$('#formbox-txn_def').find("#chann_color").show();
		}
		
		$('#formbox-txn_def').find('#op').val('mod');
		
		$('#formbox-txn_def').find('#parent_tab').val(fullInfo['PARENT_TAB']);
		$('#formbox-txn_def').find('#tab_name').val(fullInfo['TAB_NAME']);
		$('#formbox-txn_def').find('#tab_desc').val(fullInfo['TAB_DESC']);
		$('#formbox-txn_def').find('#txnid').val(fullInfo['TXNID']);
		$('#formbox-txn_def').find('#txnffname').val(fullInfo['PAR_TAB_DESC']);
		$('#formbox-txn_def').find('#show_order').val(fullInfo['SHOW_ORDER']);
		$('#formbox-txn_def').find('#MODELUSED').val(fullInfo['MODELUSED']);
		$('#formbox-txn_def').find('#MODELUSED_O').val(fullInfo['MODELUSED']);
		$('#formbox-txn_def').find('#TRAINDATE').val(fullInfo['TRAINDATE']);
		$('#formbox-txn_def').find('input[name=TAB_DISPOSAL]').val(fullInfo['TAB_DISPOSAL']);
		
		//$('#formbox-txn_def').find(":radio[name=is_enable]").val(fullInfo['IS_ENABLE']);
		
		
		if(fullInfo['PARENT_TAB'] == fullInfo['TAB_NAME']){
			$('#formbox-txn_def').find('#show_order').prop('disabled', true);
			$('#formbox-txn_def').find('#txnid').prop('disabled', true);
			$('#formbox-txn_def').find('#chann').prop('disabled', true);
			$('#formbox-txn_def').find('[name=txn_type]').prop('disabled', true);
			$('#formbox-txn_def').find("[name=is_enable]").prop('disabled', true);
			$('#formbox-txn_def').find('#tab_desc').prop('disabled', true);
			$('#formbox-txn_def').find('#txnmodSubmitButton').prop('disabled', true);
			$('#formbox-txn_def').find('#MODELUSED').prop('disabled', true);
		} else {
			$('#formbox-txn_def').find('#show_order').prop('disabled', false);
			$('#formbox-txn_def').find('#txnid').prop('disabled', false);
			$('#formbox-txn_def').find('#chann').prop('disabled', false);
			$('#formbox-txn_def').find('[name=txn_type]').prop('disabled', false);
			
			$('#formbox-txn_def').find("[name=is_enable]").prop('disabled', false);
			$('#formbox-txn_def').find('#tab_desc').prop('disabled', false);
			$('#formbox-txn_def').find('#txnmodSubmitButton').prop('disabled', false);
			$('#formbox-txn_def').find('#MODELUSED').prop('disabled', false);
		}
	}
	
	//添加交易 
	function saveTxnSubmit(selector_str, op){
		
		var tab_desc = $(selector_str).find('#tab_desc').val();
		var txnid = $(selector_str).find('#txnid').val();
		var ORDERBY = $(selector_str).find('#show_order').val();
		var chann = $(selector_str).find('#chann').val();
		var disposal = $(selector_str).find('input[name=TAB_DISPOSAL]').val();
		// $(selector_str).find('#op').val('mod');

		if($(selector_str + ' :radio[name=txn_type]').filter(':checked').val() == '1'){ // 交易
			
			if(txnid.trim()==''){
				alert(jcl.message.get('当交易类型为交易时,交易识别标识不能为空'));
				return ;
			}else {
				var flag = checkeLength(txnid, 20);
				if(!flag){
					alert('交易识别标识不能超过20个字符');
					return;
				}
				flag = checkSpecialCharacter(txnid, "1");
				if(!flag){
					alert("交易识别标识只能以字母开头.包含字母,数字和下划线");
//					alert('交易识别标识含有特殊字符');
					return;
				}
			}
			
			if(chann.trim()==''){
				alert(jcl.message.get('当交易类型为交易时,渠道不能为空'));
				return ;
			}
		}
		
		if (disposal.trim() == '') {
			alert("处置策略不能为空！");
			return ;
		}
		if(tab_desc.trim()==''){
			alert(jcl.message.get('ui.tms.traffic.empty'));
			return ;
		}else {
			var flag = checkeLength(tab_desc, 32);
			if(!flag){
				alert('名称不能超过32个字符');
				return;
			}
			flag = checkSpecialCharacter(tab_desc, "2");
			if(!flag){
				//alert("名称只能以汉字开头.包含汉字,字母,数字和下划线");
				alert("名称只能包含汉字,字母,数字和下划线");
				//alert('名称含有特殊字符');
				return;
			}
		}
		
		if(ORDERBY!=''){
			var r = /^\d*$/;//非负整数   
			if(!r.test(ORDERBY)){
				alert('顺序请输入非负整数');  
				return;
			}
			var flag = checkeLength(ORDERBY,'4');
			if(!flag){
				alert('顺序不能大于4位数');
				return;
			}
		} else {
			alert('顺序不能为空');
			return;
		}
		
		var params = $(selector_str).find('#data-form').serialize();
		jcl.postJSON('/tms35/trandef/save', params, function(data){
			if(data.success){
				if(op == 'add'){
					alert(jcl.message.get('添加成功'));
				} else
					alert(jcl.message.get('修改成功'));
				mdl_def_add.hide();
				jcl.go("/dualaudit/tms35/trandef/init");
			}
		});
	}
	
})();







