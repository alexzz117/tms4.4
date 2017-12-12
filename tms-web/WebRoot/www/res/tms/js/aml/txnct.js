var aml = window.aml || {};
(function(){
	var opts = {
		txnId : null,
		page : null                                                                                                                                                                                                                                                                                                                                                                                                                                                       
	};
	var txnct_form = null;
	aml.txnct = {
		init : function(_txnId, _page) {
			opts = $.extend(opts, {txnId:_txnId, page:_page, name:'aml_txnct'});
			txnct_form = new aml.form(opts, $('#formbox-txnct'));
			this.load(_txnId);
		},
		load : function(_txnId) {
			opts = $.extend(opts, {txnId:_txnId});
			jcl.postJSON('/tms/aml/getct', 'TAB_NAME='+_txnId, function(data){
				var fields = txnct_form.form.getItem("CT_FIELD");
				fields.component.reload(data);
				txnct_form.setData(data.row, _txnId);
			},false);
		},
		isDirty : function() {

		},
		save : function() {
			
		}
	};
})();