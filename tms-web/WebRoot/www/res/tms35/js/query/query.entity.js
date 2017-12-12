/**
 * 组织自定义查询实体的JS
 */
var jcq = typeof(jcq) === 'undefined' ? {} : jcq;
jcq.view = jcq.view||{};

jcq.view.QueryEntity = function(options, container) {
	var $entity = this;
	var _defaults = {
		tag : null,
		boxWidth : null,
		minBoxWidth : 688
	};
	this.opts = $.extend({}, jcq.customquery._defaults, options);
	this.parent = (container || $('body'));

	function init() {
		var opts = $entity.opts;
		var stype = opts.json.statement.type;// 1:sql语句,2:存储过程,3:tag页,4:上下结构页
		var content = opts.json.statement.content;
		var box_title = opts.json.box_title;
		var _parent = $entity.parent;
		if(box_title) {
			 var box = new jcl.ui.Box({
				title: box_title,
				marginTop: 0
			}, _parent);
			_parent = box.jqDom.find('.box-content');
		}
		if (stype == '3') {
			_createTagPage(_parent);
		} else if (stype == '4') {
			_createComposePage(_parent);
		} else {
			_createDetailPage(_parent);
		}
	}

	function _queryIframe(query_id, display) {
		var opts = $entity.opts;
		var queryDiv = query_id + 'Div';
		var url = contextPath + '/tms35/query/show?' + replaceValueForParamsString('queryId', query_id, opts.params);
		var iframeHtml = '<iframe id="' + query_id + 'Iframe" '+ (display === true ? ('src="' + url + '"') : ('url="' + url + '"'));
		iframeHtml += ' frameborder="0" scrolling="no" marginwidth="0" marginheight="0" width="100%"></iframe>';
		return iframeHtml;
	}

	/**
	 * 标签页
	 */
	function _createTagPage(container) {
		var opts = $entity.opts;
		var content = opts.json.statement.content;
		var querys = [];
		var tagIndex = opts.tag ? opts.tag : 1;
		querys = content.split('\;');
		var tabPanel = new jcl.ui.TabPanel({}, container);
		tabPanel.onTabClick(function(index){
			var iframe = $('div .tabp-list').find('.tabp-content:eq('+index+') > iframe');
			if (!IsEmpty(iframe.attr('src'))) {
				var url = iframe.attr("url");
				iframe.attr("src", url);
				iframe.removeAttr("url");
			}
	    	return true;
   	 	});
		for(var i=0;i<querys.length;i++) {
			var queryInfo = querys[i];
			var query_id = queryInfo.substring(0,queryInfo.indexOf('['));
			var query_title = queryInfo.substring(queryInfo.indexOf('[')+1, queryInfo.indexOf(']'));
			var ds = tagIndex == i+1 ? true : false;
			tabPanel.addTab(query_title, _queryIframe(query_id, ds));
		}
		tabPanel.activeTab(tagIndex-1);
	}
	/**
	 * 上下结构页
	 */
	function _createComposePage(container) {
		var opts = $entity.opts;
		var content = opts.json.statement.content;
		var querys = [];
		querys = content.split('\;');
		for(var i=0;i<querys.length;i++) {
			var query_id = querys[i];
			$(_queryIframe(query_id, true)).appendTo(container);
		}
	}
	/**
	 * 实体页面
	 */
	function _createDetailPage(container) {
		var opts = $entity.opts;
		opts.boxId = 'queryTable';
		opts.id = 'queryDetail';
		$.getScript(contextPath+'/s/tms35/js/query/query.detail.js', function(){
			jcq.view.QueryEntityDetail(opts, container);
		});
	}

	init();
};