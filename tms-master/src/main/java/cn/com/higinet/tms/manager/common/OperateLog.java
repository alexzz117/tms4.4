/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.service.LogService;
import cn.com.higinet.tms.manager.common.util.CmcMacUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;

/**
 * 记录操作日志实现类	
 * @author zhangfg
 * @version 2.0.0, 2011-6-30
 */
public class OperateLog {

	private final Logger logger = LoggerFactory.getLogger( OperateLog.class );
	private LogService logService;

	public OperateLog() {}

	public OperateLog( LogService logService ) {
		this.logService = logService;
	}

	private String logId;
	private String operatorId;
	private Date operateTime;
	private String funcId;
	private String operateResult;
	private String logMac;
	private String operateData;

	/**
	 * 记录操作日志
	 * @param message 操作相关信息
	 * @param success 操作成功或失败
	 */
	public void doLog( String message, boolean success ) {
		if( success ) {
			this.setOperateResult( "1" );
		}
		else {
			this.setOperateResult( "0" );
		}
		if( !message.equals( "" ) ) {
			this.operateData += "\r\n其它: " + message;
			this.operateData = CmcStringUtil.getStrs( this.operateData, 4000, "utf-8" );
			assemblyLogMap( this.operateData, "1" );
		}
		else {
			this.operateData = CmcStringUtil.getStrs( this.operateData, 50000, "utf-8" );
			String[] count = this.operateData.split( " \\|\\|\\|\\| " );
			for( int i = 0; i < count.length; i++ ) {
				String[] val = count[i].split( " \\&\\&\\&\\& " );
				if( val.length == 2 ) {
					assemblyLogMap( val[0], val[1] );
				}
			}
		}

	}

	private void assemblyLogMap( String operatedata, String order ) {

		Map<String, Object> log = new HashMap<String, Object>();
		log.put( DBConstant.CMC_OPERATE_LOG_LOG_ID, logId );
		log.put( DBConstant.CMC_OPERATE_LOG_OPERATOR_ID, this.operatorId );
		log.put( DBConstant.CMC_OPERATE_LOG_FUNC_ID, this.funcId );

		log.put( DBConstant.CMC_OPERATE_LOG_OPERATE_DATA, operatedata );
		log.put( DBConstant.CMC_OPERATE_LOG_OPERATE_TIME, new Timestamp( this.operateTime.getTime() ) );
		log.put( DBConstant.CMC_OPERATE_LOG_OPERATE_RESULT, this.operateResult );
		log.put( DBConstant.CMC_OPERATE_LOG_PRIMARY_KEY_ID, Stringz.randomUUID().toUpperCase() );
		log.put( "ORDER_ID", Integer.parseInt( order ) );
		try {
			this.logMac = CmcMacUtil.md5( this.operatorId + this.funcId + this.operateResult + this.operateTime );
		}
		catch( NoSuchAlgorithmException e ) {
			logger.error( "create logMac error. ", e );
		}
		log.put( DBConstant.CMC_OPERATE_LOG_LOG_MAC, this.logMac );
		logService.createLog( log );
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId( String logId ) {
		this.logId = logId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId( String operatorId ) {
		this.operatorId = operatorId;
	}

	public Date getOperateTime() {
		return operateTime;
	}

	public void setOperateTime( Date operateTime ) {
		this.operateTime = operateTime;
	}

	public String getFuncId() {
		return funcId;
	}

	public void setFuncId( String funcId ) {
		this.funcId = funcId;
	}

	public String getOperateResult() {
		return operateResult;
	}

	public void setOperateResult( String operateResult ) {
		this.operateResult = operateResult;
	}

	public String getLogMac() {
		return logMac;
	}

	public void setLogMac( String logMac ) {
		this.logMac = logMac;
	}

	public String getOperateData() {
		return operateData;
	}

	public void setOperateData( String operateData ) {
		this.operateData = operateData;
	}

}
