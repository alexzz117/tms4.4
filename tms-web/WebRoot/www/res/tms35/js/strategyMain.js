var model = window.model || {};
(function() {
	var tabPanel,tabs;
	model.strategyMain = {
		init: function(txnId, page) {
			$('#strategy_main').empty();
			
			tabs = [
		    	{title:'策略列表', dom:'#strategy_list', handler: model.strategyInfo},
		    	{title:'规则列表', dom:'#strategy_rule_list', handler: model.strategyRule}
		    ];
			
			 //多页签面板
    		tabPanel = page.tabPanel = new jcl.ui.TabPanel({tabsWrap:true}, $('#strategy_main'));
			 $.each(tabs, function(i, tab){
		    	 tabPanel.addTab(tab.title, $(tab.dom), getAcHandler);
		    });
			
			var acTabIndex = -1;

		    function getAcHandler(index){
				var tab = tabs[index];
		   		var list = tree.select();
		   		var txnId = list[0];
		   		if(tab.handler.isInit){
		   			tab.handler.load(txnId);
		   		}else{
					tab.handler.isInit = true;
		   			tab.handler.init(txnId, page);
		   		}
		   		$(tab.dom).triggerHandler('resizes');
		    }
			
			tabPanel.onTabClick(function(index){
				if (index == 1) {
					var srows = model.strategyInfo.grid().table.selectedRows();
					if (srows.length != 1) {
						alert("请选择一个策略");
						return;
					}
				}
			    acTabIndex = index;
			    return true;
			 });
			 
			 tabPanel.activeTab(0); // 默认选中第一个页签
		},

		load: function(txnId) {
			tabPanel.activeTab(0); // 默认选中第一个页签
		},

		isDirty: function() {
			return false;
		},

		save: function() {

		},
		
		tabPanel:function(){
			return tabPanel;
		}
	};
})();