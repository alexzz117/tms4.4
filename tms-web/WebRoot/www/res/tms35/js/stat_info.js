
var model = window.model ||{};

(function(){
	var form = null;
	var user_id  ;
	var stat_id  ;
	var txnFeature;
	var statFunc;
	model.statInfo = {
		init: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			
			$('#statInfoDiv').empty();// 清空列表
			
			var status = jcl.code.getCodes('tms.mgr.rulestatus');
			var status_list = new Array();
			$.each(status,function(i,code){
				var o ;
				if(i==0){
					o = {text:code, value:i, checked:true};
				}else{
					o = {text:code, value:i};
				}
				status_list.push(o);
			});
			
			// 初始化表格
			form = new jcl.ui.Form({
				id: null,
				layout: 'auto',
				items:[
				   {name:'STAT_NAME', label:'统计名称'},
				   {label:"统计描述", name: 'STAT_DESC', type:'text', required:true},
	               {label:"统计引用对象", name: 'STAT_PARAM', type:'listselector',items:txnFeature},// 下拉
	               {label:"统计条件", name: 'STAT_COND_IN', type:'text'},
	               {label:"统计函数", name: 'STAT_FN', type:'selector',items:statFunc},
	               {label:"统计目标", name: 'STAT_DATAFD', type:'selector' ,items:txnFeature},
	               {label:"函数参数", name: 'FN_PARAM', type:'text', required:true},
	               {label:"单位", name: 'COUNUNIT', type:'selector', title:'--请选择--', ds:{type:'code', category:'tms.stat.condunit', selectedTopItem:true}, required:true},
	               {label:"周期", name: 'COUNTROUND', type:'text', required:true},
	               {label:"交易结果", name: 'RESULT_COND', type:'selector',title:'--请选择--', ds:{type:'code', category:'tms.stat.txnstatus', selectedTopItem:true}, required:true},
	               {label:"连续", name: 'CONTINUES', type:'checkbox',items:[{text:'', value:'1'}]},
	               {label:"事中", name: 'STAT_UNRESULT', type:'checkbox',items:[{text:'', value:'1'}]},
	               {label:"有效性", name: 'STAT_VALID', type:'radio',value:'0', items:status_list}
				]
    		},$("#statInfoDiv"));


			// 加载数据
			this.load(ths_userid,ths_statid);

		},
		load: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			
			var param = "user_id="+user_id+"&stat_id="+stat_id;
			jcl.postJSON('/tms35/userpattern/statInfo', param, function(data){
				var txnId = data.row.STAT_TXN;
				
				jcl.postJSON('/tms/stat/txnFeature', 'txn_id='+txnId, function(txnFeatureData){
					txnFeature = txnFeatureData;
					
					txnFeature.row.unshift({CODE_VALUE:'--请选择--',CODE_KEY:''});
					
					form.getItem('STAT_PARAM').component.reload(txnFeatureData.row,{text:'CODE_VALUE',value:'CODE_KEY'});
					form.getItem('STAT_DATAFD').component.reload(txnFeatureData.row,{text:'CODE_VALUE',value:'CODE_KEY'});
					
					jcl.postJSON('/tms/stat/code', "category_id=tms.pub.func&args='2'", function(data1){
						statFunc = data1;
						form.getItem('STAT_FN').component.reload(data1.row,{text:'CODE_VALUE',value:'CODE_KEY'})
						form.reset();
						form.set(data.row);
						form.showViewMode();	
					});
										
				});
			});

		},
		isDirty: function(){
			return false;//stat_isDirty;
		},
		save: function(){
			//alert(1);
		}
	};
})();
