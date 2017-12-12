var aml = window.aml || {};
(function(){
	var opts = {
		txnId : null,
		page : null
	};
	var cfg_form = null;
	aml.config = {
		init : function(_txnId, _page) {
			opts = $.extend(opts, {txnId:_txnId, page:_page, name:'aml_config'});
			cfg_form = new aml.form(opts, $('#formbox-config'));
			this.load(_txnId);
		},
		load : function(_txnId) {
			jcl.postJSON('/tms/aml/getcfg', 'TAB_NAME='+_txnId, function(data){
				cfg_form.setData(data.row, _txnId);
			},false);
		},
		isDirty : function() {

		}
	};
})();