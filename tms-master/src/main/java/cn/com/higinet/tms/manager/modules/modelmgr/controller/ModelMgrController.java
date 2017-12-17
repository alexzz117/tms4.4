package cn.com.higinet.tms.manager.modules.modelmgr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.SocketClient;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.ServerService;
import cn.com.higinet.tms.manager.modules.modelmgr.service.ModelMgrService;

/**
 * 模型管理控制类
 * @author zlq
 * @author zhang.lei
 */

@RestController("modelMgrController")
@RequestMapping("/tms/modelmgr")
public class ModelMgrController {

	@Autowired
	private ModelMgrService modelMgrService;

	@Autowired
	private ServerService serverService;

	/**
	 * 模型列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String modelMgrListView() {
		return "tms35/modelmgr/model_list";
	}

	/**
	 * 模型列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model modelMgrLisActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( modelMgrService.modelList( reqs ) );
		return model;
	}

	/**
	 * 模型相关度历史列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/mcList", method = RequestMethod.POST)
	public Model mcLisActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		page.setList( modelMgrService.modelCorrelationList( reqs ) );
		model.setPage( page );
		return model;
	}

	/**
	 * 模型历史列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/mhList", method = RequestMethod.GET)
	public String mhListView() {
		return "tms35/modelmgr/modelHis_list";
	}

	/**
	 * 模型历史列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/mhList", method = RequestMethod.POST)
	public Model mhListActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( modelMgrService.modelHisList( reqs ) );
		return model;
	}

	/**
	 * 模型训练交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/mTrain", method = RequestMethod.POST)
	public Model mTrainActoin( @RequestBody String[] txnids ) {
		Model model = new Model();
		String t = "";
		for( int i = 0; i < txnids.length; i++ ) {
			t += txnids[i] + ",";
		}
		t = t.substring( 0, t.length() - 1 );

		List<Map<String, Object>> ipList = getIpList();

		if( ipList == null || ipList.size() == 0 ) {
			throw new TmsMgrWebException( "没有可用的风险模型评估服务" );
		}

		String errorMsg = "";
		for( Map<String, Object> server : ipList ) {
			String ip = MapUtil.getString( server, DBConstant.TMS_RUN_SERVER_IPADDR );
			String port = MapUtil.getString( server, DBConstant.TMS_RUN_SERVER_PORT );
			/*String ip  = "10.8.3.107";
			String port = "8811";*/
			Map<String, byte[]> res = getMessage( t );
			byte[] b = res.get( "b" );
			byte[] ws = res.get( "ws" );
			try {
				System.out.println( "正在向服务器ip：" + ip + ",端口：" + port + " 发送风险模型训练请求....." );
				new SocketClient( ip, Integer.parseInt( port ) ).send( b, ws );
				System.out.println( "发送成功！" );
				errorMsg = "";
				break;// 只向一个风险模型服务器发送请求
			}
			catch( Exception e ) {
				e.printStackTrace();
				String e_message = e.getMessage();
				if( e_message != null && e_message.length() > 0 ) {
					errorMsg += "服务器" + ip + "：" + port + " " + e_message + ",";
				}
			}
		}
		if( errorMsg.length() > 0 ) {
			throw new TmsMgrWebException( "模型训练失败，由于" + (errorMsg.substring( 0, errorMsg.length() - 1 )) );
		}
		return model;
	}

	private List<Map<String, Object>> getIpList() {
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "SERVTYPE", "5" );
		return serverService.listServer( conds );
	}

	/**
	 * 生成缓存同步所需的socket消息
	 * @param arg
	 * @return
	 */
	private Map<String, byte[]> getMessage( String refreshMsg ) {
		// 报文头  同步用
		final byte[] MSG_HEADER = {
				' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', //报文体长度
				'T', 'I', 'M', 'E', 'R', ' ', ' ', ' ', //服务号
				'0', '0', '0', '1', //交易号		
				'X', 'M', 'L', ' ', //报文体类型	
				' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' //	返回码 
		};
		Map<String, byte[]> result = new HashMap<String, byte[]>();
		try {
			StringBuffer body = new StringBuffer();
			body.append( getXmlHead( null ) );
			body.append( CmcStringUtil.appendXmlMessage( "TXNID", refreshMsg ) );
			body.append( CmcStringUtil.appendXmlMessage( "TRAINTYPE", "1" ) );// 0：自动训练，1：手工训练
			body.append( "</Message>" );

			byte[] b = MSG_HEADER;
			byte[] ws = body.toString().getBytes( "UTF-8" );

			String len = String.valueOf( ws.length );
			len = "00000000".substring( len.length() ) + len;
			System.arraycopy( len.getBytes(), 0, b, 0, 8 );
			result.put( "b", b );
			result.put( "ws", ws );
			return result;
		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new RuntimeException( "未能成功生成风险模型训练请求报文" );
		}

	}

	/**
	 * 拼装报文体部分xml的头信息
	 * messageProp：message的属性例如：
	 *  xmlns="http://www.higinet.com.cn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance";
	 *  xsi:schemaLocation="http://www.higinet.com.cn/tms.xsd";
	 * @return
	 */
	private static String getXmlHead( String messageProp ) {
		StringBuffer sb = new StringBuffer();
		sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		sb.append( "<Message" ).append( messageProp == null ? "" : messageProp ).append( ">" );
		return sb.toString();
	}
}