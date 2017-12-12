
var model = window.model ||{};

(function(){
	var grid = null;
	var user_id  ;
	var stat_id  ;
	model.statPattern = {
		init: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			$('#statPatternGridDiv').empty();// 清空列表
			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				width:728,
				table:{
					sortType: 'local',
					columns:[
						{name:"统计函数", width: 40, dataIndex:'STAT_FN'},
						{name:"统计目标", width: 40, dataIndex:'STAT_FD'},
						{name:"统计值", width: 40, dataIndex:'STAT_VALUE'},
						{name:"次数", width: 30, dataIndex:'STAT_COUNT'},
						{name:"百分比", width: 30, dataIndex:'STAT_PERCENT'}
					],
					pagebar: false
				}
			}, $('#statPatternGridDiv'));

			// 加载数据
			this.load(user_id,stat_id);

		},
		load: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			
			var param = "user_id="+user_id+"&stat_id="+stat_id;
			jcl.postJSON('/tms35/userpattern/statPatternList', param, function(data){
				if(data.list != null && data.list.length > 0){
					$.each(data.list,function(i,list){
						list.STAT_FN = grid_s.selectedRows()[0].FUNC_NAME;
						list.STAT_FD = grid_s.selectedRows()[0].STAT_DATAFD;
					});
				}
				grid.renderPage({list: data.list});
			});
		},
		isDirty: function(){
			return false;
		},
		save: function(){
			
			
		}
	};
})();
