package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.IPCache;
import cn.com.higinet.tms.manager.modules.mgr.service.IPProtectService;

/**
 * IP地址导入控制类
 * @author zhang.lei
 */

@RestController("tmsIPProtectController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/ip")
public class IPProtectController {

	private static final Logger logger = LoggerFactory.getLogger( IPProtectController.class );

	@Autowired
	private IPProtectService IPProtectService;

	private String ipCharset = PropertiesUtil.getPropInstance().get( "file.ipcity.charset", "UTF-8" );
	private String cityCharset = PropertiesUtil.getPropInstance().get( "file.city.charset", ipCharset );

	String error = ""; //读取文件错误信息
	long ipFileLen = 1; //ip地址文件大小
	long cityFileLen = 0; //城市代码文件大小
	long cardFileLen = 0; //身份证号段文件大小
	long mobileFileLen = 0;//手机号段文件大小
	private String ipFileName = "";//ip名称
	private String cityFileName = "";//城市名称
	private String cardFileName = "";//身份证号段文件名称
	private String mobileFileName = "";//手机号段文件名称

	@RequestMapping(value = "/protect", method = RequestMethod.GET)
	public String metaObjView() {
		return "tms/mgr/ipprotect_index";
	}

	@RequestMapping(value = "/recordImpInfo", method = RequestMethod.POST)
	public Model recordImpInfo( HttpServletRequest request ) {
		Model model = new Model();
		request.setAttribute( "ipFileName", ipFileName );
		request.setAttribute( "cityFileName", cityFileName );
		request.setAttribute( "cardFileName", cardFileName );
		request.setAttribute( "mobileFileName", mobileFileName );
		return model;
	}

	@SuppressWarnings("finally")
	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public Model importThread( @RequestParam Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		//初始化进度参数
		IPProtectService.initialization();
		IPCache.initialization();
		error = "";
		//获取文件
		MultipartHttpServletRequest multipartrequest = (MultipartHttpServletRequest) request;
		MultipartFile ipFile = multipartrequest.getFile( "importIPFile" );
		MultipartFile cityFile = multipartrequest.getFile( "importCityFile" );
		MultipartFile cardFile = multipartrequest.getFile( "importCardFile" );
		MultipartFile mobileFile = multipartrequest.getFile( "importMobileFile" );
		ipFileName = ipFile.getOriginalFilename();
		cityFileName = cityFile.getOriginalFilename();
		cardFileName = cardFile.getOriginalFilename();
		mobileFileName = mobileFile.getOriginalFilename();
		boolean isLoad = false;
		try {

			logger.info( "-------ipFile:" + ipFile );
			logger.info( "-------cityFile:" + cityFile );
			logger.info( "-------cardFile:" + cardFile );
			logger.info( "-------mobileFile:" + mobileFile );

			if( ipFile == null || (ipFileLen = ipFile.getSize()) == 0 ) {
				throw new Exception( "IP地址文件文件内容不能为空" );
			}
			if( cityFile == null || (cityFileLen = cityFile.getSize()) == 0 ) {
				throw new Exception( "城市代码文件内容不能为空" );
			}
			if( cardFile == null || (cardFileLen = cardFile.getSize()) == 0 ) {
				throw new Exception( "身份证号段文件内容不能为空" );
			}
			if( mobileFile == null || (mobileFileLen = mobileFile.getSize()) == 0 ) {
				throw new Exception( "手机号段文件内容不能为空" );
			}
			InputStreamReader ipIsr, cityIsr, cardIsr, mobileIsr, ipIsrTemp, cityIsrTemp, cardIsrTemp, mobileIsrTemp;
			try {
				ipIsr = new InputStreamReader( ipFile.getInputStream(), ipCharset );
				ipIsrTemp = new InputStreamReader( ipFile.getInputStream(), ipCharset );
			}
			catch( IOException e ) {
				throw new IOException( "获取IP地址文件失败", e );
			}
			try {
				cityIsr = new InputStreamReader( cityFile.getInputStream(), cityCharset );
				cityIsrTemp = new InputStreamReader( cityFile.getInputStream(), cityCharset );
			}
			catch( IOException e ) {
				throw new IOException( "获取城市代码文件失败", e );
			}
			try {
				cardIsr = new InputStreamReader( cardFile.getInputStream(), cityCharset );
				cardIsrTemp = new InputStreamReader( cardFile.getInputStream(), cityCharset );
			}
			catch( IOException e ) {
				throw new IOException( "获取身份证号段文件失败", e );
			}
			try {
				mobileIsr = new InputStreamReader( mobileFile.getInputStream(), cityCharset );
				mobileIsrTemp = new InputStreamReader( mobileFile.getInputStream(), cityCharset );
			}
			catch( IOException e ) {
				throw new IOException( "获取手机号段文件失败", e );
			}
			/* 启动IP地址导入线程 */
			Task tast = new Task( ipIsr, cityIsr, cardIsr, mobileIsr, ipIsrTemp, cityIsrTemp, cardIsrTemp, mobileIsrTemp );
			new Thread( tast ).start();
			isLoad = true;
		}
		catch( Exception e ) {
			error = e.getLocalizedMessage();
			logger.error( error, e );
		}
		finally {
			if( CmcStringUtil.isBlank( error ) && !isLoad ) {
				error = "运行时错误，请查看日志！";
				model.addError( "运行时错误，请查看日志！" );
				return model;
			}
			return model;
			//			return "tms/mgr/ipprotect_getprogress";
		}
	}

	class Task implements Runnable {
		InputStreamReader ipFile;
		InputStreamReader cityFile;
		InputStreamReader cardFile;
		InputStreamReader mobileFile;
		InputStreamReader ipFileTemp;
		InputStreamReader cityFileTemp;
		InputStreamReader cardFileTemp;
		InputStreamReader mobileFileTemp;

		public Task( InputStreamReader ipFile, InputStreamReader cityFile, InputStreamReader cardFile, InputStreamReader mobileFile, InputStreamReader ipFileTemp,
				InputStreamReader cityFileTemp, InputStreamReader cardFileTemp, InputStreamReader mobileFileTemp ) {
			this.ipFile = ipFile;
			this.cityFile = cityFile;
			this.cardFile = cardFile;
			this.mobileFile = mobileFile;
			this.ipFileTemp = ipFileTemp;
			this.cityFileTemp = cityFileTemp;
			this.cardFileTemp = cardFileTemp;
			this.mobileFileTemp = mobileFileTemp;
		}

		public void run() {
			importFiles( ipFile, cityFile, cardFile, mobileFile, ipFileTemp, cityFileTemp, cardFileTemp, mobileFileTemp );
		}
	}

	private void importFiles( InputStreamReader ipFile, InputStreamReader cityFile, InputStreamReader cardFile, InputStreamReader mobileFile, InputStreamReader ipFileTemp,
			InputStreamReader cityFileTemp, InputStreamReader cardFileTemp, InputStreamReader mobileFileTemp ) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> inputNode = new HashMap<String, Object>();
		inputNode.put( "importIPFile", ipFile );
		inputNode.put( "importCityFile", cityFile );
		inputNode.put( "importCardFile", cardFile );
		inputNode.put( "importMobileFile", mobileFile );
		inputNode.put( "importIPFileTemp", ipFileTemp );
		inputNode.put( "importCityFileTemp", cityFileTemp );
		inputNode.put( "importCardFileTemp", cardFileTemp );
		inputNode.put( "importMobileFileTemp", mobileFileTemp );
		list.add( inputNode );
		Map<String, Object> node = new HashMap<String, Object>();
		node.put( "mod", list );
		IPProtectService.importIpLocation( node );
	}

	@RequestMapping(value = "/getProgress", method = RequestMethod.POST)
	public Model getProgress( HttpServletRequest request ) {
		Model model = new Model();
		//获取导入进度参数
		Map<String, Object> map = IPProtectService.getProgress();
		String serviceError = MapUtil.getString( map, "errorInfo" );
		long cityFileProgress = MapUtil.getLong( map, "cityFileProgress" );
		long ipFileProgress = MapUtil.getLong( map, "ipFileProgress" );
		long cardFileProgress = MapUtil.getLong( map, "cardFileProgress" );
		long mobileFileProgress = MapUtil.getLong( map, "mobileFileProgress" );
		boolean ipFileOver = MapUtil.getBoolean( map, "ipFileOver" );
		boolean cityFileOver = MapUtil.getBoolean( map, "cityFileOver" );
		boolean cardFileOver = MapUtil.getBoolean( map, "cardFileOver" );
		boolean mobileFileOver = MapUtil.getBoolean( map, "mobileFileOver" );

		if( CmcStringUtil.isBlank( serviceError ) ) {
			serviceError = error;
		}
		if( !CmcStringUtil.isBlank( serviceError ) ) {
			model.set( "errorInfo", serviceError );
			return model;
		}

		//获取生成索引进度
		map = IPCache.getProgress();
		long indexFileProgress = MapUtil.getLong( map, "indexProgress" );
		boolean cahceOver = MapUtil.getBoolean( map, "cahceOver" );

		if( ipFileOver ) {
			ipFileProgress = ipFileLen;
		}
		if( cityFileOver ) {
			cityFileProgress = cityFileLen;
		}
		if( cardFileOver ) {
			cardFileProgress = cardFileLen;
		}
		if( mobileFileOver ) {
			mobileFileProgress = mobileFileLen;
		}
		if( cahceOver ) {
			indexFileProgress = ipFileLen + cityFileLen + cardFileLen + mobileFileLen;
		}
		//当数据为0时，说明用于获取进度的本次请求与第一次上传文件的请求不是同一个节点，返回changeNode通知前端重新请求
		if( indexFileProgress == 0 ) {
			model.set( "errorInfo", "changeNode" );
			return model;
		}
		//将进度参数转换成百分比进度
		long ipProgress = ipFileProgress * 100 / ipFileLen;
		//该进度为(城市代码文件进度80% + 生成索引进度20%)
		long cityProgress = cityFileProgress * 80 / cityFileLen + indexFileProgress * 20 / (ipFileLen + cityFileLen + cardFileLen + mobileFileLen);
		long cardProgress = cardFileProgress * 100 / cardFileLen;
		long mobileProgress = mobileFileProgress * 100 / mobileFileLen;

		model.set( "IPProgress", ipProgress );
		model.set( "cityProgress", cityProgress );
		model.set( "cardProgress", cardProgress );
		model.set( "mobileProgress", mobileProgress );
		return model;
	}
}