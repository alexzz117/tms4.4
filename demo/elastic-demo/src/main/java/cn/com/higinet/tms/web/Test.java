package cn.com.higinet.tms.web;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.com.higinet.tms.ElasticsearchConfig;
import cn.com.higinet.tms.ElasticsearchServiceImpl;
import cn.com.higinet.tms.Trafficdata;
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
	private ElasticsearchConfig elasticsearchConfig;
	
	@ApiOperation(value = "创建index", notes = "说明:创建index")
    @RequestMapping(value = "createIndex", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createIndex() {
        boolean isOk = true;
        String msg = "成功";
        try {
        	elasticsearchServiceImpl.addIndex("tms", 1,1);
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
//        	elasticsearchServiceImpl.addIndex("tms",3,2);//创建索引
        	elasticsearchServiceImpl.createMapping("tms", "trafficdata");
        } catch (Exception e) {
            isOk = false;
            msg = "失败";
        }
        return "aaa";
	}
	
	@ApiOperation(value = "根据条件进行查询", notes = "说明:根据条件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "txncode", value = "交易流水", required = false, paramType = "query",allowEmptyValue = true, dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "交易起始时间", required = false, paramType = "query",allowEmptyValue = true, dataType = "Long"),
            @ApiImplicitParam(name = "endTime", value = "交易结束时间", required = false, paramType = "query",allowEmptyValue = true, dataType = "Long"),
            @ApiImplicitParam(name = "ipAddr", value = "IP地址", required = false, paramType = "query", allowEmptyValue = true,dataType = "String"),
            @ApiImplicitParam(name = "disposal", value = "处置方式", required = false, paramType = "query",allowEmptyValue = true, dataType = "String"),
            @ApiImplicitParam(name = "iseval", value = "是否评估", required = false, paramType = "query",allowEmptyValue = true, dataType = "String")
    })
    @RequestMapping(value = "search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String search(@RequestParam(value = "txncode", required = false) String txncode,
                           @RequestParam(value = "startTime", required = false) Long startTime,
                           @RequestParam(value = "endTime", required = false) Long endTime,
                           @RequestParam(value = "ipAddr", required = false) String ipAddr,
                           @RequestParam(value = "disposal", required = false) String disposal,
                           @RequestParam(value = "iseval", required = false) String iseval) {
        boolean isOk = true;
        String msg = "查询成功";
        try {
        	Trafficdata data = new Trafficdata();
        	data.setTxncode(txncode);
        	data.setIpaddr(ipAddr);
        	data.setDisposal(disposal);
        	data.setIseval(iseval);
        	elasticsearchServiceImpl.searchTrafficdata("tms", "trafficdata", data, startTime, endTime);
        } catch (Exception e) {
            e.printStackTrace();
            msg = "查询失败";
        }
        return "aaa";
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
        	List<Trafficdata> list = new ArrayList<Trafficdata>();
    		Trafficdata data = new Trafficdata();
    		data.setTxncode(txncode);
    		data.setTxnstatus(txnstatus);
    		list.add(data);
        	elasticsearchServiceImpl.upsertTrafficdata("tms", "trafficdata", list);
        } catch (Exception e) {
            isOk = false;
            msg = "失败";
        }
        return "aaa";
	}
	
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
        	elasticsearchServiceImpl.updateTrafficdata("tms", "trafficdata", data);
        } catch (Exception e) {
            isOk = false;
            msg = "失败";
        }
        return "aaa";
	}
	
	@ApiOperation(value = "批量新增", notes = "说明:批量新增")
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = "name", value = "优化索引名", paramType = "query", dataType = "String")
//  })
  @RequestMapping(value = "bulkAdd", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String bulkAdd() {
      boolean isOk = true;
      String msg = "成功";
      try {
      	List<Trafficdata> list = new ArrayList<Trafficdata>();
//      	String txncode = "874f53a3d10b42f4aa34eaa56398bff3";
//      	String txncode = "974f53a3d10b42f4aa34ebb";
      	
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
      	elasticsearchServiceImpl.bulkProcessorTrafficdata("tms", "trafficdata", list);
      } catch (Exception e) {
    	  e.printStackTrace();
//          isOk = false;
          msg = "失败";
      }
      return "aaa";
	}
	 
	  @ApiOperation(value = "压测批量新增方法", notes = "说明:压测批量新增")
	  @ApiImplicitParams({
	    @ApiImplicitParam(name = "txncode", value = "交易流水", paramType = "query",required = true,  dataType = "String"),
	    @ApiImplicitParam(name = "startId", value = "起始循环的数字", paramType = "query",required = true,  dataType = "Integer"),
	    @ApiImplicitParam(name = "endId", value = "结束循环的数字", paramType = "query",required = true,  dataType = "Integer")
	  })	
	  @RequestMapping(value = "testbulkAddTrafficdata", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	  @ResponseBody
	  public String testbulkAddTrafficdata(@RequestParam("txncode") String txncode
			  ,@RequestParam("startId") Integer startId,@RequestParam("endId") Integer endId) {
	      boolean isOk = true;
	      String msg = "成功";
	      try {
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
      		BulkProcessor bulkProcessor = testbulkProcessorTrafficdata();
	      	for(int i=startId;i<endId;i++){
	      		data.setTxncode(txncode+i);
 				JSONObject jsonObject = (JSONObject) JSONObject.toJSON(data);
 				bulkProcessor.add(new IndexRequest("tms", "trafficdata", jsonObject.getString("txncode")).source(jsonObject));
	      	}
	      	bulkProcessor.close();
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
  @RequestMapping(value = "delete by txncode", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String deleteById(@RequestParam("txncode") String txncode) {
      boolean isOk = true;
      String msg = "成功";
      try {
    	  elasticsearchServiceImpl.deleteById("tms", "trafficdata", txncode);
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
  @RequestMapping(value = "query by txncode", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String getById(@RequestParam("txncode") String txncode) {
      boolean isOk = true;
      String msg = "成功";
      try {
    	  elasticsearchServiceImpl.getById("tms", "trafficdata", txncode);
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
             	  System.out.println("executionId:"+executionId+",numberOfActions:"+request.numberOfActions());
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
