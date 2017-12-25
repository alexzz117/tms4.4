package cn.com.higinet.tms.manager.modules.alarm.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.modules.common.SocketClient;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms35.comm.hash;

@Service("sendMessageService")
public class SendMessageService {

	private static final Logger log = LoggerFactory.getLogger( SendMessageService.class );

	public String sendMessage( Map<String, Object> transaction, String actionCode, List<Map<String, Object>> servList ) {
		if( servList == null || servList.isEmpty() ) {
			throw new TmsMgrServiceException( "没有可用的服务!" );
		}
		String body = composeBody( transaction );
		String head = composeHead( actionCode, body.length() );
		if( log.isDebugEnabled() ) {
			log.debug( "发送的报文头+报文体：" + head + body );
		}
		String userId = MapUtil.getString( transaction, "USERID" );
		String resultInfo = sendMessage( userId, servList, head, body );
		if( log.isDebugEnabled() ) {
			log.debug( "接收的报文：" + resultInfo );
		}
		return resultInfo;
	}

	public String sendMessage( String userId, List<Map<String, Object>> servList, String head, String body ) {
		int servId = (hash.clac( userId ) >>> 1) % 53777 % servList.size();
		Map<String, Object> server = servList.get( servId );
		// 发送报文，返回结果
		String resultInfo = "";
		String ip = MapUtil.getString( server, "IPADDR" );
		String port = MapUtil.getString( server, "PORT" );
		try {
			resultInfo = sendMessage( ip, port, head.getBytes(), body.getBytes(), 1 );
		}
		catch( Exception e ) {
			servList.remove( servId );
			if( servList.isEmpty() ) {
				throw new TmsMgrServiceException( "没有可用的服务!" );
			}
			resultInfo = sendMessage( userId, servList, head, body );
		}
		return resultInfo;
	}

	public String sendMessage( String ip, String port, byte[] b, byte[] ws, int count ) {
		String result = "";
		try {
			result = new SocketClient( ip, Integer.parseInt( port ) ).sendMsg( b, ws );
		}
		catch( Exception e ) {
			if( count <= 0 ) {
				throw new TmsMgrServiceException( ip + "端口" + port + "发送数据失败,由于" + e.getMessage() );
			}
			count--;
			result = sendMessage( ip, port, b, ws, count );
		}
		return result;
	}

	private String composeHead( String actionCode, int bodyLength ) {
		StringBuffer sb = new StringBuffer();
		if( actionCode != null && !"".equals( actionCode ) ) {
			String len = String.valueOf( bodyLength );
			len = "00000000".substring( len.length() ) + len;
			sb.append( len );// 报文体长度
			sb.append( "TMS" ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " );// 服务号
			sb.append( actionCode );// 交易号
			sb.append( "XML" ).append( " " );// 报文体类型
			sb.append( " " ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " ).append( " " );// 返回码
		}
		return sb.toString();
	}

	private String composeBody( Map<String, Object> transaction ) {
		StringBuilder sb = new StringBuilder();
		sb.append( getXmlHead() );
		sb.append( getXmlExtMap( transaction ) );
		sb.append( "</Message>" );
		return sb.toString();
	}

	private String getXmlHead() {
		StringBuffer sb = new StringBuffer();
		sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		sb.append( "<Message>" );
		return sb.toString();
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private String getXmlExtMap( Map<String, Object> map ) {
		StringBuffer sb = new StringBuffer( "" );
		if( map != null && !map.isEmpty() ) {
			Set keySet = map.keySet();
			Iterator keyIt = keySet.iterator();
			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				Object value = map.get( key );
				if( value instanceof Map ) {
					Map<String, Object> valueMap = (Map<String, Object>) value;
					if( valueMap != null && !valueMap.isEmpty() ) {
						sb.append( "<" ).append( String.valueOf( key ) ).append( " type='map'>" );
						sb.append( getXmlExtMap( valueMap ) );
						sb.append( "</" ).append( key ).append( ">" );
					}
				}
				else {
					sb.append( appendXmlMessage( String.valueOf( key ), value ) );
				}
			}
		}
		return sb.toString();
	}

	private String appendXmlMessage( String key, Object value ) {
		StringBuffer bf = new StringBuffer( "" );
		if( value != null ) {
			bf.append( "<" ).append( key ).append( ">" ).append( value ).append( "</" ).append( key ).append( ">" );
		}
		return bf.toString();
	}
}
