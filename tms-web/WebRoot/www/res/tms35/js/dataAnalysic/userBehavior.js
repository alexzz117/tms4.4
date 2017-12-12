//初始化交易维度
function initTransWeiDu(txnId){
	$('#transProperty').show();
	$('#transStat').show();
	all_dic = new Array();
	// 交易属性
	jcl.postJSON('/tms35/dataanalysic/txnFeature', 'txn_id=' + txnId, function(data){
		txnFeatureData = data;
		$("select[name=STAT_DATAFD]").children().remove();
		$("select[name=STAT_DATAFD]").append("<option value=''> --请选择-- </option>");
		$.each(data.row, function(i,row){
			$("select[name=STAT_DATAFD]").append("<option value='" + row['CODE_KEY'] + "'>" + row['CODE_VALUE'] + "</option>");
			all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
		}); 
	});
	
	// 交易统计 /tms35/dataanalysic
	jcl.postJSON('/tms35/dataanalysic/statfunc', "txnId=" + txnId, function(data){
		statFuncData = data;
		$("select[name=STAT_FN]").children().remove();
		$("select[name=STAT_FN]").append("<option value=''> --请选择-- </option>");
		$.each(data.row, function(i,row){
			$("select[name=STAT_FN]").append("<option value='" + row['CODE_KEY'] + "'>" + row['CODE_VALUE'] + "</option>");
			all_dic.push({CODE_KEY:row['CODE_KEY'],CODE_VALUE:row['CODE_VALUE']});
		}); 
	});

}

//根据交易树选择值获取交易类型　
function getTxnId(_this){
	if(_this.id!=''){
		txnId = _this.id;
	}
	initTransWeiDu(txnId);
}

//下拉框选择事件
function confirmCond(_this){
	var isMore = checkWeiDuCount();
	if(isMore){
		jcl.msg.info("交易维度不能超过8个");
		return;
	}
	var isRepeat = checkRepeatedWeiDu(_this);
	if(isRepeat){
		jcl.msg.info("不能重复选择交易维度");
		return;
	} 
	for (var i = 0; i < _this.length; i++) {
		var stat_cond_value = $(_this).val();
		
		if (_this[i].selected == true && stat_cond_value != '') {
			$('#STAT_COND_VALUE').focus();
			$('#STAT_COND_VALUE').val($('#STAT_COND_VALUE').val() + "" + $(_this[i]).val()+";");
			$('#STAT_COND_NAME').val($('#STAT_COND_NAME').val() + "" + $(_this[i]).text()+";");
		}
	}
}
function searchCondName(condvalue){
	if (condvalue == '') {
		$('#STAT_COND_NAME').attr('value','');
		return;
	}
	var condname = condvalue;
	$.each(txnFeatureData.row, function(i,row){
		condname = condname.replace(new RegExp(row['CODE_KEY'],"g"),row['CODE_VALUE']);
	}); 
	$.each(statFuncData.row, function(i,row){
		condname = condname.replace(new RegExp(row['CODE_KEY'],"g"),row['CODE_VALUE']);
	}); 
	$.each(operFuncData.row, function(i,row){
		condname = condname.replace(new RegExp(row['CODE_KEY'],"g"),row['CODE_VALUE']);
	}); 
	$('#STAT_COND_NAME').attr('value',condname);
}

//重置表单
function resetConds(){
	
	/* $('#userId').attr('value','');
	$('#compareUserId').attr('value',''); */
	$('#STAT_FN').attr('value','');
	$('#DATE_FUNC').attr('value','');
	$('#STR_FUNC').attr('value','');
	$('#OPER_FUNC').attr('value','');
	$('#AC_Func').attr('value','');
	$('#STAT_DATAFD').attr('value','');
	$('#STAT_COND_NAME').attr('value','');
	$('#STAT_COND_VALUE').attr('value','');
}

//查询用户行为习惯生成蛛网分析视图
function queryUserHabit(){
	
	var userId = $('#userId').val();
	var compareUserIds = $('#compareUserId').val();
	if(userId==''){
		jcl.msg.info('客户号不能为空');
		return;
	}
	if(compareUserIds==''){
		jcl.msg.info('对比客户号最少1个');
		return;
	}
	
	var userIdArray = compareUserIds.split(';');
	if(userIdArray.length>10){
		jcl.msg.info('对比客户号最多10个');
		return;
	}
	
	for(var i = 0;i<userIdArray.length;i++){
		if(userIdArray.length>1){
			for(var j = 0;j<userIdArray.length;j++){
				if(j+1==userIdArray.length){
					break;
				}
				if(i==j+1){
					continue;
				}
				if(userIdArray[i]==userIdArray[j+1]){
					jcl.msg.info('对比客户号存在重复');
					return;
				}
			}
		}
		
		if(userId==userIdArray[i]){
			jcl.msg.info('客户号与对比客户号存在重复');
			return;
		}
	}
	userIdArray.push(userId);
	
	var weiDus = $('#STAT_COND_VALUE').val();
	if(weiDus==''){
		jcl.msg.info('维度不能为空');
		return;
	}
	
	var weiDuArray = weiDus.split(";");
	var lastWeiDu = weiDuArray[weiDuArray.length-1];
	if(lastWeiDu!=''&&lastWeiDu!=null){
		if(weiDuArray.length<3){
			jcl.msg.info('维度至少需要3个');
			return;
		}
	}else{
		if(weiDuArray.length-1<3){
			jcl.msg.info('维度至少需要3个');
			return;
		}
	}
	
	
	
	
	//组装ECHARTS报表所需参数
	var userObjArray = new Array();//用户对象数组，以用户对象user组成，对象包含属性name（用户名）、value(维度值)
    var maxWeiDuValArray = new Array();//维度最大值数组，以维度对象weiDuObj组成，对象包含属性text（维度名）、max(维度最大值)
    var userNameArray = new Array();//用户名数组，以用户名组成
    
    //维度区间，暂时写死
    var weiDuSection = new Array();
    weiDuSection.push([-100,0]);
    weiDuSection.push([0,20]);
    weiDuSection.push([20,30]);
    weiDuSection.push([30,100]);
    weiDuSection.push([100,1000000]);
 	// 查询用户行为习惯维度
	jcl.postJSON('/tms35/dataanalysic/queryWeiDus', "userIdArray="+userIdArray+"&weiDus="+weiDuArray+"&txnId="+txnId, function(data){
		$.each(data.row, function(i,row){
			if(row==null||row.length==0){
				jcl.msg.info("用户号："+i+"相关数据为空");
				return true;
			}
			var userWeiDuArray = new Array();
			for(var index in row){
				var weiDuObj = new Object();
				for(var name in row[index]){
					var isContain = false;
					 
					if(name=='desc'){
						//取维度名、维度最大值数组去重
						if(maxWeiDuValArray.length>0){
							for(var j = 0;j<maxWeiDuValArray.length;j++){
								if(maxWeiDuValArray[j]['text']==row[index][name]){
									isContain = true;
									break;
								}
							}
						}
						if(maxWeiDuValArray.length==0||isContain!=true){
							weiDuObj.text = row[index][name];
						}
						
					}
					
					/* indicatorArray.push(weiDuObj); */
					if(name!='desc'){
						//获取维度值
						var weiDuVal = row[index][name];
						if(weiDuVal==null){
							weiDuVal= 0;
						}
						if(weiDuObj.text!=undefined||isContain==false){
							weiDuObj.max = weiDuVal;
							userWeiDuArray.push(weiDuVal);
						}
						
					}
				}
				if(weiDuObj.text!=undefined&&weiDuObj.max!=undefined){
					maxWeiDuValArray.push(weiDuObj);
				}
				
			}
			var user = new Object();
			user.name = i;
			user.value = userWeiDuArray;
			userObjArray.push(user);
			userNameArray.push(i);
		});
		
		//判断维度值在哪个维度区间，取该区间的最大值为该维度的最大值
		for(var i = 0;i<maxWeiDuValArray.length;i++){
			var weiDuObj = maxWeiDuValArray[i];
			for(var j = 0;j<weiDuSection.length;j++){
				if(weiDuObj.max==-100){
					weiDuObj.max = weiDuSection[0][1];
					maxWeiDuValArray[i]['max'] = weiDuObj.max;
					break;
				}
				if(weiDuObj.max==1000000){
					weiDuObj.max = weiDuSection[weiDuSection.length-1][1];
					maxWeiDuValArray[i]['max'] = weiDuObj.max;
					break;
				}
				if(weiDuObj.max>=weiDuSection[j][0]&&weiDuObj.max<weiDuSection[j][1]){
					weiDuObj.max = weiDuSection[j][1];
					maxWeiDuValArray[i]['max'] = weiDuObj.max;
					break;
				}
			}
		}
		var myChart = echarts.init(document.getElementById('main')); 
		var option = {
			    title : {
			        text: '',
			        subtext: ''
			    },
			    tooltip : {
			        trigger: 'axis'
			    },
			    legend: {
			        orient : 'vertical',
			        x : 'right',
			        y : 'bottom',
			        data:userNameArray
			    },
			    toolbox: {
			        show : true,
			        feature : {
			            mark : {show: false},
			            dataView : {show: false, readOnly: false},
			            restore : {show: true},
			            saveAsImage : {show: true}
			        }
			    },
			    polar : [
			       {
			           indicator : maxWeiDuValArray
			        }
			    ],
			    calculable : true,
			    series : [
			        {
			            name: '用户行为特征蛛网分析',
			            type: 'radar',
			            data : userObjArray
			        }
			    ]
			};
		// 为echarts对象加载数据 
	    myChart.setOption(option);
	});
	
	
}

function checkWeiDuCount(){
	var isMore = false;
	var weiDuValue = $('#STAT_COND_VALUE').val();
	var weiDuArray = weiDuValue.split(';');
	if(weiDuArray.length>8){
		isMore = true;
	}
	return isMore;
}

function checkRepeatedWeiDu(_this){
	var isRepeat = false;
	var weiDuValue = $('#STAT_COND_VALUE').val();
	var weiDuArray = weiDuValue.split(';');
	var selectedWeiDu = $(_this).val();
	for(var i = 0; i<weiDuArray.length;i++){
		var weiDu = weiDuArray[i];
		if(weiDu==selectedWeiDu){
			isRepeat = true;
		}
	}
	return isRepeat;
}