
var model = window.model ||{};

(function(){
	var grid = null;
	var user_id  ;
	var stat_id  ;
	model.userInfo = {
		init: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			/*$('#userInfoDiv').empty();// 清空列表
			
			// 初始化表格
			var form = new jcl.ui.Form({
				id: null,
				layout: 'list',
				items:[
				   {name:'USERID', label:'客户号'},
				   {name:'USERNAME', label:'客户昵称'},
				   {name:'CUSNAME', label:'客户姓名'},
				   {name:'CERTNO', label:'证件号'},
				   {name:'CERTTYPE', label:'证件类型', type:'selector', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'common.is'}},
				   {name:'CELLPHONENO', label:'手机号码'},
				   {name:'OPENTIME', label:'开户时间'},
				   {name:'SEX', label:'性别'},
				   {name:'BIRTHDATE', label:'出生日期'}
				]
    		});

*/
			// 加载数据
			this.load(ths_userid,ths_statid);

		},
		load: function(ths_userid,ths_statid){
			user_id = ths_userid;
			stat_id = ths_statid;
			$('#userInfoDiv').empty();
			jcq.loadQuery({params:{queryId:209, userid:user_id}}, {element:'#userInfoDiv'});
			//document.getElementById("iframId").src=jcl.env.contextPath+"/tms35/query/show?queryId=209&userid="+user_id;
/*
			var param = "txnId="+txnId;
			// 初始化查询列表
			jcl.postJSON("/tms/stat/list", "txnId="+txnId, function(data){
				grid.renderPage({list: data.row});
			});
*/ 
		},
		isDirty: function(){
			return false;//stat_isDirty;
		},
		save: function(){
			//alert(1);
		}
	};
})();
