var aml = window.aml || {};
aml.form = function(options, container) {
	var _self = this, _parent_val = null, _txnId = null;
	this.form = null;
	this.opts = $.extend({
		txnId:null,
		name:null,
		upcName:null
	}, options);
	
	this.opts.upcName = [this.opts.name.toUpperCase()];
	
	this.jqDom = (function(opts) {
		var tItems = [];
		if(opts.name == 'aml_txnct'){
			opts.upcName = ['ct_type'.toUpperCase(), 'ct_field'.toUpperCase()];
			tItems = [
				{name:opts.upcName[0], label:jcl.message.get('ui.tms.aml.form.' + opts.name) + '类型', type:'selector',
						ds:{type:'code', category:'tms.aml.txnct'}},
				{name:opts.upcName[1], label:jcl.message.get('ui.tms.aml.form.' + opts.name) + '字段', type:'selector',
						ds:{type:'remote', url:'/tms/aml/modelfds', lazyLoad:true, parser:{key:'list', text:'NAME', value:'REF_NAME'}}}
			];
			
		}else{
			tItems = [
			    {name:opts.upcName, label:jcl.message.get('ui.tms.aml.form.' + opts.name) + '配置', type:'textarea'}
			];
		}
		_self.form = new jcl.ui.Form({
			layout: 'list',
			items: tItems,
			btns: [
			    {text:'保存', id:opts.name + '-sure'}
			]
		}, $(container));
		var jqDom = _self.form.jqDom;
		_bindEvent(jqDom);
		return jqDom;
	})(this.opts);
	
	this.setData = function(data, txnId) {
		var opts = _self.opts;
		if (txnId) opts.txnId = txnId;
		if (data) {
			_txnId = data['TAB_NAME'];
			$.each(opts.upcName, function(i, key) {
				var val = data[key];
				var item = _self.form.getItem(key);
				var textarea = item.component;
				if (opts.txnId == _txnId) {
					if ($(textarea).hasClass('parent')) {
						$(textarea).removeClass('parent');
					}
				} else {
					if (!$(textarea).hasClass('parent')) {
						$(textarea).addClass('parent');
					}
					_parent_val = val;
				}
				item.val(val);
			});
		}
	};
	
	function _bindEvent(jqDom) {
		var opts = _self.opts;
		if(opts.name != 'aml_txnct'){
			var textarea = jqDom.find('textarea');
			textarea.focus(function() {
				if ($(this).hasClass('parent')) {
					_parent_val = $(this).val();
					$(this).removeClass('parent');
					$(this).val('');
				}
			}).blur(function() {
				var val = $(this).val();
				if (!val) {
					if (_parent_val) {
						$(this).addClass('parent');
						$(this).val(_parent_val);
					}
				}
			});
			jqDom.find('#' + opts.name + '-sure').click(function(){
				if ($(textarea).hasClass('parent')) {
					jcl.msg.warn(jcl.message.get('ui.tms.aml.form.' + opts.name) + ', 无可提交的有效数据.');
				} else {
					var tval = textarea.val();
					var params = {tab_name: opts.txnId, column: opts.name, value: tval};
					jcl.postJSON('/tms/aml/savecfg', params, function(data){
						if (data.success) {
							jcl.msg.info(jcl.message.get('ui.tms.aml.form.' + opts.name) + ', 保存成功.');
						}
					});
				}
			});
		}else{
			jqDom.find('#' + opts.name + '-sure').click(function(){
				var cols = ['ct_type', 'ct_field'], vals = [];
				$.each(cols, function(i, key) {
					var k = key.toUpperCase();
					var val = _self.form.getItem(k).val();
					vals.push(val);
				});
				var params = {tab_name: opts.txnId, column: cols.join(','), value: vals.join(',')};
				jcl.postJSON('/tms/aml/savecfg', $.param(params), function(data){
					if (data.success) {
						jcl.msg.info(jcl.message.get('ui.tms.aml.form.' + opts.name) + ', 保存成功.');
					}
				});
			});
		}
	}
};