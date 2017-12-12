var model = window.model || {};
(function() {
	var tabPanel,tabs ;
	model.ruleMain = {
		init: function(txnId, page) {
			 $('#formbox-rule_main').empty();
			tabs = [
		    	{title:'规则列表', dom:'#formbox-rule_list0', handler: model.rule},
		    	{title:'动作列表', dom:'#acGridDiv', handler: model.ac}
		    ];
			
			 //多页签面板
    		tabPanel = page.tabPanel = new jcl.ui.TabPanel({tabsWrap:true}, $('#formbox-rule_main'));
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
				if(index==1){
					var srows = model.rule.grid().table.selectedRows();
					if(srows.length!=1){
						alert("请选择一个规则");
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