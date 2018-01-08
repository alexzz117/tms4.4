package cn.com.higinet.tms.web;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.com.higinet.tms.ElasticsearchServiceImpl;
import cn.com.higinet.tms.Pagination;
import cn.com.higinet.tms.TrafficQueue;
import cn.com.higinet.tms.Trafficdata;
import cn.com.higinet.tms.adapter.ConditionMarkEnum;
import cn.com.higinet.tms.adapter.ElasticsearchAdapter;
import cn.com.higinet.tms.adapter.QueryConditionEntity;
import cn.com.higinet.tms.provider.ElasticsearchConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "测试任务微服务")
@Controller
@RequestMapping(value = "/esOpt")
public class Test {

	@Autowired
	private ElasticsearchServiceImpl elasticsearchServiceImpl;
	
	@Autowired
	private ElasticsearchAdapter elasticsearchAdapter;
	
	@Autowired
	private ElasticsearchConfig elasticsearchConfig;
	
	@Autowired
	private TrafficQueue trafficQueue;
	
	
	@ApiOperation(value = "创建index", notes = "说明:创建index")
    @RequestMapping(value = "createIndex", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createIndex() {
        boolean isOk = true;
        String msg = "成功";
        try {
        	elasticsearchServiceImpl.addIndex("trafficdata", 3,2);
        } catch (Exception e) {
            isOk = false;
            msg = "失败";
        }
        return "aaa";
	}
	
	
	@ApiOperation(value = "创建mapping", notes = "说明:创建mapping")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "优化索引名", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "range", value = "优化时间范围", required = true, paramType = "query", dataType = "String")
    })
    @RequestMapping(value = "createMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createMapping(@RequestParam("name") String name, @RequestParam("range") String range) {
        boolean isOk = true;
        String msg = "成功";
        try {
        	elasticsearchAdapter.createMapping("trafficdata", Trafficdata.class);
        } catch (Exception e) {
            isOk = false;
            msg = "失败";
        }
        return "aaa";
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "根据条件进行查询", notes = "说明:根据条件查询")
    @ApiImplicitParams({
    		@ApiImplicitParam(name = "pageNo", value = "当前页码", required = true, paramType = "query",allowEmptyValue = true, dataType = "Integer"),
    		@ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query",allowEmptyValue = true, dataType = "Integer"),
    		@ApiImplicitParam(name = "txncode", value = "交易流水", required = false, paramType = "query",allowEmptyValue = true, dataType = "String"),
            @ApiImplicitParam(name = "ipAddr", value = "IP地址", required = false, paramType = "query", allowEmptyValue = true,dataType = "String"),
            @ApiImplicitParam(name = "text1", value = "text1模糊匹配", required = false, paramType = "query", allowEmptyValue = true,dataType = "String"),
            @ApiImplicitParam(name = "text2", value = "text2前缀匹配", required = false, paramType = "query", allowEmptyValue = true,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "交易时间起始时间", required = false, paramType = "query", allowEmptyValue = true,dataType = "Long"),
            @ApiImplicitParam(name = "endTime", value = "交易时间结束时间", required = false, paramType = "query", allowEmptyValue = true,dataType = "Long"),
            @ApiImplicitParam(name = "disposal", value = "处置方式", required = false, paramType = "query",allowEmptyValue = true, dataType = "String"),
            @ApiImplicitParam(name = "iseval", value = "是否评估", required = false, paramType = "query",allowEmptyValue = true, dataType = "String")
    })
    @RequestMapping(value = "search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String search(@RequestParam(value = "pageNo", required = false) Integer pageNo,
    					   @RequestParam(value = "pageSize", required = false) Integer pageSize,		
    					   @RequestParam(value = "txncode", required = false) String txncode,
                           @RequestParam(value = "ipAddr", required = false) String ipAddr,
                           @RequestParam(value = "text1", required = false) String text1,
                           @RequestParam(value = "text2", required = false) String text2,
                           @RequestParam(value = "startTime", required = false) String startTime,
                           @RequestParam(value = "endTime", required = false) String endTime,
                           @RequestParam(value = "disposal", required = false) String disposal,
                           @RequestParam(value = "iseval", required = false) String iseval) {
        boolean isOk = true;
        String msg = "查询成功";
        try {
        	List<QueryConditionEntity> conditionList = new ArrayList<QueryConditionEntity>();
        	if(!StringUtils.isEmpty(txncode)){
        		conditionList.add(setValue("txncode", txncode, null, ConditionMarkEnum.EQ
        			, null, ConditionMarkEnum.OTHER));
        	}
        	if(!StringUtils.isEmpty(ipAddr)){
        		conditionList.add(setValue("ipAddr", ipAddr, null, ConditionMarkEnum.EQ
        			, null, ConditionMarkEnum.OTHER));
        	}
        	if(!StringUtils.isEmpty(text1)){
        		conditionList.add(setValue("text1", text1, null, ConditionMarkEnum.LIKE
        			, null, ConditionMarkEnum.OTHER));
        	}
        	if(!StringUtils.isEmpty(text2)){
        		conditionList.add(setValue("text2", text2, null, ConditionMarkEnum.LIKE_
        			, null, ConditionMarkEnum.OTHER));
        	}
        	if(!StringUtils.isEmpty(startTime)&&!StringUtils.isEmpty(endTime)){
        		conditionList.add(setValue("txntime", startTime, endTime, ConditionMarkEnum.GTE
        			, ConditionMarkEnum.LTE, ConditionMarkEnum.BETWEEN));
        	}else if(!StringUtils.isEmpty(startTime)&&StringUtils.isEmpty(endTime)){
        		conditionList.add(setValue("txntime", startTime, null, ConditionMarkEnum.GTE
            			, null, ConditionMarkEnum.OTHER));
        	}else if(StringUtils.isEmpty(startTime)&&!StringUtils.isEmpty(endTime)){
        		conditionList.add(setValue("txntime", endTime, null, ConditionMarkEnum.LTE
            			, null, ConditionMarkEnum.OTHER));
            }
        	if(!StringUtils.isEmpty(disposal)){
        		conditionList.add(setValue("disposal", disposal, null, ConditionMarkEnum.EQ
        			, null, ConditionMarkEnum.OTHER));
        	}
        	if(!StringUtils.isEmpty(iseval)){
        		conditionList.add(setValue("iseval", iseval, null, ConditionMarkEnum.EQ
        			, null, ConditionMarkEnum.OTHER));
        	}
        	conditionList.add(setValue("txntime", "ASC", null, null
        			, null, ConditionMarkEnum.ORDERBY));
        	Pagination<Trafficdata> pagination = (Pagination<Trafficdata>) elasticsearchAdapter.search("trafficdata", pageSize,pageNo,conditionList,Trafficdata.class);
        	System.out.println("dataList的size为:"+pagination.getDataList().size()+"---------");
        } catch (Exception e) {
            e.printStackTrace();
            msg = "查询失败";
        }
        return "aaa";
	}
	
	private QueryConditionEntity setValue(String name,Object startValue,Object endValue
			,ConditionMarkEnum startMark,ConditionMarkEnum endMark,ConditionMarkEnum queryType){
		QueryConditionEntity entity = new QueryConditionEntity();
		entity.setName(name);
		entity.setStartValue(startValue);
		entity.setEndValue(endValue);
		entity.setStartValue(startValue);
		entity.setStartMark(startMark);
		entity.setQueryType(queryType);
		return entity;
	}
	
	@ApiOperation(value = "存在更新不存在插入", notes = "说明:存在更新不存在插入")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "txncode", value = "交易流水", paramType = "query", dataType = "String"),
    	@ApiImplicitParam(name = "txnstatus", value = "交易状态", paramType = "query", dataType = "Integer")
    })
    @RequestMapping(value = "upsert", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String upsert(@RequestParam("txncode") String txncode,@RequestParam("txnstatus") Integer txnstatus) {
        boolean isOk = true;
        String msg = "成功";
        try {
    		Trafficdata data = new Trafficdata();
    		data.setTxncode(txncode);
    		data.setTxnstatus(txnstatus);
    		elasticsearchAdapter.upsertData("trafficdata", data);
        } catch (Exception e) {
            isOk = false;
            msg = "失败";
        }
        return "aaa";
	}
	
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "更新", notes = "说明:存在更新不存在不更新")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "txncode", value = "交易流水", paramType = "query", dataType = "String"),
    	@ApiImplicitParam(name = "txnstatus", value = "交易状态", paramType = "query", dataType = "Integer")
    })
    @RequestMapping(value = "update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String update(@RequestParam("txncode") String txncode,@RequestParam("txnstatus") Integer txnstatus) {
        boolean isOk = true;
        String msg = "成功";
        try {
    		Trafficdata data = new Trafficdata();
    		data.setTxncode(txncode);
    		data.setTxnstatus(txnstatus);
    		elasticsearchAdapter.updateData("trafficdata", data);
        } catch (Exception e) {
            isOk = false;
            msg = "失败";
        }
        return "aaa";
	}
	
	private Trafficdata createData(){
    	Trafficdata data = new Trafficdata();
      	data.setIpaddr("11.11.11.11");
  		data.setTxntime(1513040400000L);
  		data.setDisposal("PS01");
  		data.setCountrycode("5044");
  		data.setRegioncode("5044110000");
  		data.setCitycode("5044110000");
  		data.setIspcode("");
  		data.setCreatetime(1513040400000L);
  		data.setFinishtime(1513040400000L);
  		data.setTxnstatus(1);
  		data.setBatchno("123123");
  		data.setHitrulenum(1);
  		data.setIscorrect("1");
  		data.setConfirmrisk("1");
  		data.setIseval("1");
  		data.setModelid("102");
  		data.setNum1(1.012F);
  		data.setText1("北京三里屯");
  		data.setText2("北京西城区");
  		data.setNum2(11F);
  		data.setText3("啊啊啊啊啊啊啊大大大大大大撒大苏打似的");
  		data.setTxncode(UUID.randomUUID().toString());
  		return data;
    }
	
	
	@ApiOperation(value = "批量新增", notes = "说明:批量新增")
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = "name", value = "优化索引名", paramType = "query", dataType = "String")
//  })
  @RequestMapping(value = "batchUpdate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String batchUpdate() {
      boolean isOk = true;
      String msg = "成功";
      try {
      	List<Trafficdata> list = new ArrayList<Trafficdata>();
      	String txncode = "111111111112-";//"111111111111-";
      	Trafficdata data = null;
      	for(int i=170000;i<180000;i++){
      		data = new Trafficdata();;
      		data.setTxncode(txncode+i);
      		data.setIpaddr("11.11.11.11");
      		data.setTxntime(1513040400000L);
      		data.setDisposal("PS01");
      		data.setCountrycode("5044");
      		data.setRegioncode("5044110000");
      		data.setCitycode("5044110000");
      		data.setIspcode("");
      		data.setCreatetime(1513040400000L);
      		data.setFinishtime(1513040400000L);
      		data.setTxnstatus(1);
      		data.setBatchno("123123");
      		data.setHitrulenum(1);
      		data.setIscorrect("1");
      		data.setConfirmrisk("1");
      		data.setIseval("1");
      		data.setModelid("102");
      		data.setNum1(1.00F);
      		data.setText1("北京长城");
      		data.setText2("北京天安门");
      		if(i%500==0){
      			data.setDisposal("PS02");
      			data.setText1("北京故宫");
      			data.setIpaddr("12.12.11.12");
      			data.setTxntime(1513126800000L);//1512954000000 1513040400000 1513126800000
      		}
      		list.add(data);
      	}
      	elasticsearchAdapter.batchUpdate("trafficdata", list,Trafficdata.class);
      } catch (Exception e) {
    	  e.printStackTrace();
//          isOk = false;
          msg = "失败";
      }
      return "aaa";
	}
	 
	  @ApiOperation(value = "压测批量新增方法", notes = "说明:压测批量新增")
	  @ApiImplicitParams({
	    @ApiImplicitParam(name = "txncode", value = "交易流水", paramType = "query",dataType = "String"),
	    @ApiImplicitParam(name = "startId", value = "起始循环的数字", paramType = "query",  dataType = "Integer"),
	    @ApiImplicitParam(name = "endId", value = "结束循环的数字", paramType = "query",  dataType = "Integer")
	  })	
	  @RequestMapping(value = "testbulkAddTrafficdata", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	  @ResponseBody
	  public String testbulkAddTrafficdata() {
	      boolean isOk = true;
	      String msg = "成功";
	      try {
	    	  trafficQueue.put(createData());
	      } catch (Exception e) {
	    	  e.printStackTrace();
//	          isOk = false;
	          msg = "失败";
	      }
	      return "aaa";
		}
	
  @ApiOperation(value = "根据交易流水删除", notes = "说明:删除")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "txncode", value = "交易流水", paramType = "query",required = true,  dataType = "String")
  })
  @RequestMapping(value = "deleteById", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String deleteById(@RequestParam("txncode") String txncode) {
      boolean isOk = true;
      String msg = "成功";
      try {
    	  elasticsearchAdapter.deleteById("trafficdata", txncode);
      } catch (Exception e) {
          isOk = false;
          msg = "失败";
      }
      return "aaa";
	}
  
  @ApiOperation(value = "根据交易流水查询", notes = "说明:查询")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "txncode", value = "交易流水", paramType = "query",required = true,  dataType = "String")
  })
  @RequestMapping(value = "getById", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String getById(@RequestParam("txncode") String txncode) {
      boolean isOk = true;
      String msg = "成功";
      try {
    	  Trafficdata data = (Trafficdata) elasticsearchAdapter.getById("trafficdata", txncode,Trafficdata.class);
    	  System.out.println(data.getTxncode()+"-----"+data.getTxnstatus()+"------"+data.getTxntime());
      } catch (Exception e) {
          isOk = false;
          msg = "失败";
      }
      return "aaa";
	}
  
  /**
	 * 压测批量更新
	 * @param indexName
	 * @param mappingName
	 * @param dataList
	 */
	private BulkProcessor testbulkProcessorTrafficdata(){
		 BulkProcessor bulkProcessor = BulkProcessor.builder(elasticsearchConfig.getTransportClient(),
             new BulkProcessor.Listener() {
               @Override
                 public void beforeBulk(long executionId,BulkRequest request) {
//             	  System.out.println("executionId:"+executionId+",numberOfActions:"+request.numberOfActions());
                 }
                 @Override
                 public void afterBulk(long executionId, BulkRequest request,  BulkResponse response) {
                 	System.out.println("executionId:"+executionId+",是否有失败:"+response.hasFailures());
                 }
                 //设置ConcurrentRequest 为0，Throwable不抛错
                 @Override
                 public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                     System.out.println("happen fail = " + failure.getMessage() + " cause = " +failure.getCause());
                 }
             })
             .setBulkActions(5000)
             .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
             .setFlushInterval(TimeValue.timeValueSeconds(5))
             .setConcurrentRequests(1)
             .build();
		return bulkProcessor;
	}
	
	
}
