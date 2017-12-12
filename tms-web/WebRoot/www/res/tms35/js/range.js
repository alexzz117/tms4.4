(function($){
	/**
	 * @class Range
	 */
	jcl.ui.Range = function(options, container){

		var _self = this;
		var codes = {};

		this.opts = $.extend({
			id : null,
			category : 'tms.rule.disposal',
			value : '',
			ds : {type : 'remote', url : '', params : '',  parser : {row : 'list', key : 'start', value : 'end'}}
		}, options);

		/*
		 *  default parser 
		 *  parse 0,PS01|30,PS02|50,PS04|80,PS05 only
		 */
		this.parser = function(value){

			var ps_value_arr = [];
			var score_arr = [];
			var ps_code_id_arr = [];
			var val_arr = [];

			if (value.indexOf('|') > -1) {
				val_arr = value.split('|');
			} else{
				return null;
			};

			$.each(val_arr, function(idx, arr){
				var firstScore = arr.split(',')[0];
				var ps_code_id = arr.split(',')[1];
				score_arr[idx * 2] = firstScore;
				score_arr[idx * 2 + 1] = firstScore;
				ps_code_id_arr[idx] = ps_code_id;
			});
			score_arr.splice(0, 1);
			score_arr[score_arr.length] = '100';
			
			$.each(ps_code_id_arr, function(idx, ps_code_id){

				var ps_value_obj = {};
				ps_value_obj['ps_value_code'] = ps_code_id;
				ps_value_obj['start_score'] = score_arr[idx * 2];
				ps_value_obj['end_score'] = score_arr[idx * 2 + 1];
				
				ps_value_arr.push(ps_value_obj);
			});
			
			return ps_value_arr;
		};

		this.jqDom = (function(opts){
			
			var jqDom = opts.id ? $('#'+opts.id) : null;
			
			if(!jqDom || jqDom.length == 0){
				jqDom = $('<div>').appendTo(container ? container : 'body');
			}
			return jqDom;

		})(this.opts);

		this.draw = function(value){

			this.opts.value = value;
			_self.jqDom.find('table').remove();
			var ps_value_arr = _self.parser(_self.opts.value);
			var htmlstr = '<table>';

			function makeRowHtml(key, value, start_score, end_score){
				var _default = '';
				_default += '<tr>';
				_default += '<td class="form-item-label">' + value + ':</td>';
				_default += '<td><input name="'+key+'_start_score" type="text" value="' + start_score + '" class="itext" style="width: 40px;"></td>';
				_default += '<td>-</td>';
				_default += '<td><input name="'+key+'_end_score" type="text" value="' + end_score + '" class="itext" style="width: 40px;"></td>';
				_default += '</tr>';
				return _default;
			}
			
			$.each(_getCodes(), function(key, value){

				if (!ps_value_arr || ps_value_arr.length == 0) {
					htmlstr += makeRowHtml(key, value, '0', '0');
				} else{
					var hasCode = false;
					$.each(ps_value_arr, function(idx, obj){
						if (key == obj.ps_value_code) {
							htmlstr += makeRowHtml(key, value, obj.start_score, obj.end_score);
							hasCode = true;
							return false;
						} 
					});
					if (!hasCode) {
						htmlstr += makeRowHtml(key, value, '', '');
					};
				};
			
			});
			htmlstr += '</table>';
			
			_self.jqDom.append(htmlstr);
		};

		function _getCodes(){
			codes = codes ? jcl.code.getCodes(_self.opts.category) : codes;
			return codes;
		}


		this.render = function(list){
			var opts = _self.opts;
			_list = list || [];
			_map = {};

			if(list.length == 0){
				this.jqDom.empty();
				return;
			}

			var top = [];
			
			if(opts.order){
				list.sort(function(x, y){
					x.onum = x.onum || 0;
					y.onum = y.onum || 0;
					return x.onum - y.onum;
				});
			}
			for(var i = 0, l = list.length; i < l; i++){
				_map[list[i].id] = list[i];
			}
			for(var i = 0; i < list.length; i++){
				var node = list[i];
				//node.children = node.children||[];
				var fid = node.fid;
				if(_map[fid]){
					var fnode = _map[fid];
					fnode.children = fnode.children||[];
					fnode.children.push(node.id);
				}else{
					node.top = true;
					top.push(node.id);
				}
			}
			
			this.jqDom.html('<ul class="top">' + _printNodeList(top) + '</ul>');
		};

	};

})(jQuery);