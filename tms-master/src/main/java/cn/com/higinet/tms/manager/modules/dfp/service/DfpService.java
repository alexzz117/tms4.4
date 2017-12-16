/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.dfp.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;

/**
 * 功能/模块:
 * @author zhanglq
 * @version 1.0  Mar 3, 2014
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 * 
 * @author zhang.lei
 *
 */
@Service("dfpService")
public class DfpService {
	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	@Qualifier("tmpSimpleDao")
	private SimpleDao tmpSimpleDao;
	@Autowired
	private SimpleDao officialSimpleDao;
	@Autowired
	private SequenceService sequenceService;

	/**
	* 方法描述:查询应用列表
	* @param reqs
	* @return
	*/
	public List<Map<String, Object>> appList( Map<String, Object> reqs ) {
		String sql = "SELECT A.* FROM TMS_DFP_APPLICATION A  ORDER BY A.APP_ID";
		return tmsSimpleDao.queryForList( sql );
	}

	private Map<String, Object> appInfo( String app_id ) {
		return tmsSimpleDao.retrieve( "TMS_DFP_APPLICATION", MapWrap.map( "APP_ID", app_id ).getMap() );
	}

	/**
	* 方法描述:应用
	* @param formList
	*/
	@Transactional
	public Map<String, Object> saveApp( Map<String, List<Map<String, ?>>> formList ) {

		List<Map<String, Object>> delList = MapUtil.getList( formList, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formList, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formList, "add" );

		Map<String, Object> rmap = new HashMap<String, Object>();

		// 删除
		if( delList != null && delList.size() > 0 ) {
			for( Map<String, Object> map : delList ) {
				// 校验。1，被引用不能删除
				if( checkAppReferenced( MapUtil.getString( map, "APP_ID" ) ) ) {
					throw new TmsMgrServiceException( MapUtil.getString( map, "APP_NAME" ) + "被引用不能删除" );
				}
				// 删除
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put( "APP_ID", MapUtil.getString( map, "APP_ID" ) );
				tmsSimpleDao.delete( "TMS_DFP_APPLICATION", cond );
				rmap = map;
			}
		}

		// 新增
		if( addList != null && addList.size() > 0 ) {
			for( Map<String, Object> map : addList ) {

				// 校验：1，渠道名称不能重复
				if( checkAppDuplicate( map, "add" ) ) {
					throw new TmsMgrServiceException( "渠道名称不能重复" );
				}
				//				// 校验：2，Cookie名称不能重复
				//				if(checkCookieDuplicate(map,"add")) {
				//					throw new TmsMgrServiceException("Cookie名称不能重复");
				//				}

				// 创建
				Map<String, Object> c_m = new HashMap<String, Object>();
				c_m.put( "APP_ID", MapUtil.getString( map, "APP_ID" ) );
				c_m.put( "MAX_DEVICES", MapUtil.getLong( map, "MAX_DEVICES" ) );
				c_m.put( "THRESHOLD", MapUtil.getString( map, "THRESHOLD" ) );
				c_m.put( "COOKIENAME", MapUtil.getString( map, "COOKIENAME" ) );
				c_m.put( "APP_NAME", MapUtil.getString( map, "APP_NAME" ) );
				c_m.put( "TOKEN_TYPE", MapUtil.getString( map, "TOKEN_TYPE" ) );
				tmsSimpleDao.create( "TMS_DFP_APPLICATION", c_m );
				rmap = map;
			}
		}

		// 修改
		if( modList != null && modList.size() > 0 ) {
			for( Map<String, Object> map : modList ) {
				String app_id = MapUtil.getString( map, "APP_ID" );

				// 校验：1，Cookie名称不能重复
				//				if(checkCookieDuplicate(map,"mod")) {
				//					throw new TmsMgrServiceException("Cookie名称不能重复");
				//				}

				// 修改
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put( "APP_ID", app_id );
				Map<String, Object> c_m = new HashMap<String, Object>();
				c_m.put( "APP_ID", MapUtil.getString( map, "APP_ID" ) );
				c_m.put( "MAX_DEVICES", MapUtil.getLong( map, "MAX_DEVICES" ) );
				c_m.put( "THRESHOLD", MapUtil.getString( map, "THRESHOLD" ) );
				c_m.put( "COOKIENAME", MapUtil.getString( map, "COOKIENAME" ) );
				c_m.put( "APP_NAME", MapUtil.getString( map, "APP_NAME" ) );
				c_m.put( "TOKEN_TYPE", MapUtil.getString( map, "TOKEN_TYPE" ) );
				tmsSimpleDao.update( "TMS_DFP_APPLICATION", c_m, cond );
				rmap = map;
			}
		}

		return rmap;
	}

	/**
	* 方法描述:查询属性列表
	* @param reqs
	* @return
	*/
	public List<Map<String, Object>> proList( Map<String, Object> reqs ) {
		return tmsSimpleDao.listAll( "TMS_DFP_PROPERTY", new Order().asc( "PROP_ID" ).asc( "PROP_NAME" ) );
	}

	/**
	* 方法描述:保存属性
	* @param formList
	* @return
	*/
	@Transactional
	public Map<String, Object> savePro( Map<String, List<Map<String, ?>>> formList ) {
		List<Map<String, Object>> delList = MapUtil.getList( formList, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formList, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formList, "add" );

		Map<String, Object> rmap = new HashMap<String, Object>();

		// 删除
		if( delList != null && delList.size() > 0 ) {
			for( Map<String, Object> map : delList ) {
				// 校验。1，被引用不能删除
				if( checkProReferenced( MapUtil.getLong( map, "PROP_ID" ) ) ) {
					throw new TmsMgrServiceException( "属性被引用不能删除" );
				}
				// 删除
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put( "PROP_ID", MapUtil.getLong( map, "PROP_ID" ) );
				tmsSimpleDao.delete( "TMS_DFP_PROPERTY", cond );
				rmap = map;
			}
		}

		// 新增
		if( addList != null && addList.size() > 0 ) {
			for( Map<String, Object> map : addList ) {
				// 校验属性名称重复
				//				if(checkProNameDuplicate(MapUtil.getString(map, "PROP_NAME"),"add")) {
				//					throw new TmsMgrServiceException("属性名称不能重复");
				//				}
				// 增加
				String checkIdSql = "SELECT 1 FROM TMS_DFP_APPLICATION WHERE APP_ID = ?";
				if( !tmsSimpleDao.queryForList( checkIdSql, map.get( "PROP_ID" ) ).isEmpty() ) {
					throw new TmsMgrServiceException( "属性ID不能重复" );
				}
				tmsSimpleDao.create( "TMS_DFP_PROPERTY", map );
				rmap = map;
			}
		}

		// 修改
		if( modList != null && modList.size() > 0 ) {
			for( Map<String, Object> map : modList ) {
				// 校验属性名称重复
				//				if(checkProNameDuplicate(MapUtil.getString(map, "PROP_NAME"),"mod")) {
				//					throw new TmsMgrServiceException("属性名称不能重复");
				//				}
				// 更新
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put( "PROP_ID", MapUtil.getLong( map, "PROP_ID" ) );
				tmsSimpleDao.update( "TMS_DFP_PROPERTY", map, cond );
				rmap = map;
			}
		}

		return rmap;
	}

	/**
	* 方法描述:查询应用属性关联列表
	* @param reqs
	* @return
	*/
	public List<Map<String, Object>> appProList( Map<String, Object> reqs ) {
		String sql = "SELECT AP.*,P.PROP_NAME,P.PROP_TYPE,P.PROP_COMMENT,A.APP_NAME FROM TMS_DFP_APP_PROPERTIES AP,TMS_DFP_PROPERTY P,TMS_DFP_APPLICATION A WHERE AP.PROP_ID=P.PROP_ID AND AP.APP_ID=A.APP_ID ORDER BY AP.APP_ID ASC,P.PROP_ID ASC";
		return tmsSimpleDao.queryForList( sql );
	}

	/**
	* 方法描述:报错应用属性关联
	* @param formList
	* @return
	*/
	@Transactional
	public Map<String, Object> saveAppPro( Map<String, List<Map<String, ?>>> formList ) {
		List<Map<String, Object>> delList = MapUtil.getList( formList, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formList, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formList, "add" );

		Map<String, Object> rmap = new HashMap<String, Object>();

		// 删除
		if( delList != null && delList.size() > 0 ) {
			for( Map<String, Object> map : delList ) {
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put( "ID", MapUtil.getLong( map, "ID" ) );
				tmsSimpleDao.delete( "TMS_DFP_APP_PROPERTIES", cond );
				rmap = map;
			}
		}

		// 新增
		if( addList != null && addList.size() > 0 ) {
			for( Map<String, Object> map : addList ) {
				Long id = Long.valueOf( sequenceService.getSequenceIdToString( "SEQ_TMS_DFP_APP_PROPERTIES" ) );
				map.put( "ID", id );

				Map<String, Object> insertData = new HashMap<String, Object>();
				insertData.put( "ID", id );
				insertData.put( "APP_ID", MapUtil.getString( map, "APP_ID" ) );
				insertData.put( "PROP_ID", MapUtil.getString( map, "PROP_ID" ) );
				insertData.put( "WEIGHT", MapUtil.getDouble( map, "WEIGHT" ) );
				insertData.put( "STORECOLUMN", MapUtil.getString( map, "STORECOLUMN" ) );
				insertData.put( "IS_TOKEN", MapUtil.getString( map, "IS_TOKEN" ) );
				tmsSimpleDao.create( "TMS_DFP_APP_PROPERTIES", insertData );
				rmap = map;
			}
		}

		// 修改
		if( modList != null && modList.size() > 0 ) {
			for( Map<String, Object> map : modList ) {
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put( "ID", MapUtil.getLong( map, "ID" ) );
				Map<String, Object> c_m = new HashMap<String, Object>();
				c_m.put( "ID", MapUtil.getString( map, "ID" ) );
				c_m.put( "APP_ID", MapUtil.getString( map, "APP_ID" ) );
				c_m.put( "PROP_ID", MapUtil.getString( map, "PROP_ID" ) );
				c_m.put( "WEIGHT", MapUtil.getDouble( map, "WEIGHT" ) );
				c_m.put( "STORECOLUMN", MapUtil.getString( map, "STORECOLUMN" ) );
				c_m.put( "IS_TOKEN", MapUtil.getString( map, "IS_TOKEN" ) );
				tmsSimpleDao.update( "TMS_DFP_APP_PROPERTIES", c_m, cond );
				rmap = map;
			}
		}

		return rmap;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.dfp.service.DfpService#channleAllList()
	 */
	//	public List<Map<String, Object>> channleAllList() {
	//		String sql = "SELECT CHANNELID,CHANNELNAME FROM TMS_DP_CHANNEL C  ORDER BY ORDERBY";
	//		List<Map<String,Object>> c_l  = tmsSimpleDao.queryForList(sql);
	//		return c_l;
	//	}

	/**
	* 方法描述:校验应用代码和应用名称重复
	* @param cond 条件
	* @param type add:增加操作 mod：修改操作
	* @return true：重复 false：不重复
	 */
	private boolean checkAppDuplicate( Map<String, Object> cond, String type ) {

		String sql = "SELECT * FROM TMS_DFP_APPLICATION WHERE APP_ID = ?";

		List<Map<String, Object>> appList = tmsSimpleDao.queryForList( sql, MapUtil.getString( cond, "APP_ID" ) );

		if( type.equals( "add" ) && appList.size() > 0 ) {
			return true;
		}
		if( type.equals( "mod" ) && appList.size() > 1 ) {
			return true;
		}

		return false;
	}
	//	/**
	//	 * 方法描述:校验应用代码和应用名称重复
	//	 * @param cond 条件
	//	 * @param type add:增加操作 mod：修改操作
	//	 * @return true：重复 false：不重复
	//	 */
	//	private boolean checkCookieDuplicate(Map<String,Object> cond,String type) {
	//		
	//		String sql = "SELECT * FROM TMS_DFP_APPLICATION WHERE COOKIENAME = ?";
	//		
	//		List<Map<String,Object>> appList = tmsSimpleDao.queryForList(sql, MapUtil.getString(cond, "COOKIENAME"));
	//		
	//		if(type.equals("add") && appList.size()>0) {
	//			return true;
	//		}
	//		if(type.equals("mod") && appList.size()>0 && !MapUtil.getString(cond, "APP_ID").equals(MapUtil.getString(appList.get(0), "APP_ID"))) {
	//			return true;
	//		}
	//		
	//		return false;
	//	}

	/**
	* 方法描述:通过应用代码查询是否被引用
	* @param app_id
	* @return true：被引用 false：没被引用
	 */
	public boolean checkAppReferenced( String app_id ) {
		String sql = "SELECT * FROM TMS_DFP_APP_PROPERTIES WHERE APP_ID = ?";
		List<Map<String, Object>> app_pro_list = tmsSimpleDao.queryForList( sql, app_id );
		if( app_pro_list.size() > 0 ) {
			return true;
		}
		return false;
	}

	/**
	* 方法描述:校验属性名称不能重复
	* @param pro_name 属性名称
	* @param type add:增加操作 mod：修改操作
	* @return true：重复 false：不重复
	*/
	//	private boolean checkProNameDuplicate(String pro_name,String type) {
	//		String sql = "SELECT * FROM TMS_DFP_PROPERTY WHERE PROP_NAME = ?";
	//		List<Map<String,Object>> app_pro_list = tmsSimpleDao.queryForList(sql, pro_name);
	//		if(type.equals("add") && app_pro_list.size()>0) {
	//			return true;
	//		}
	//		if(type.equals("mod") && app_pro_list.size()>1) {
	//			return true;
	//		}
	//		return false;
	//	}

	/**
	* 方法描述:属性是否被引用
	* @param pro_id
	* @return true：被引用 false：没被引用
	 */
	private boolean checkProReferenced( Long pro_id ) {
		String sql = "SELECT * FROM TMS_DFP_APP_PROPERTIES WHERE PROP_ID = ?";
		List<Map<String, Object>> app_pro_list = tmsSimpleDao.queryForList( sql, pro_id );
		if( app_pro_list.size() > 0 ) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.dfp.service.DfpService#createJsFile()
	 */
	public void createJsFile( HttpServletResponse response, String app_id ) throws IOException {
		// 查询设备属性
		String[] propertys = queryDeviceProperty( app_id );
		if( propertys == null ) throw new TmsMgrServiceException( "当前渠道未取到属性信息" );

		// 取应用信息
		Map<String, Object> app = appInfo( app_id );
		if( app == null || app.isEmpty() ) throw new TmsMgrServiceException( "未取到应用信息" );

		String app1_cookie = MapUtil.getString( app, "COOKIENAME" );

		// 组织JS串
		StringBuilder sb = makeJsString( propertys, app1_cookie );

		// 生成JS文件，并把JS文件和flashProperty.swf压缩，生成压缩包
		File zipfile = createZipFile( app_id, sb );

		// 把压缩包流的形式返回页面
		String filename = zipfile.getName();
		InputStream fis = new BufferedInputStream( new FileInputStream( zipfile.getPath() ) );

		byte[] buffer = new byte[fis.available()];
		fis.read( buffer );
		fis.close();

		response.reset();
		response.addHeader( "Content-Disposition", "attachment;filename=" + filename );
		response.addHeader( "Content-Length", "" + zipfile.length() );

		OutputStream to = new BufferedOutputStream( response.getOutputStream() );
		response.setContentType( "application/octet-stream" );
		to.write( buffer );
		to.flush();
		to.close();
	}

	private File createZipFile( String app_id, StringBuilder sb ) throws FileNotFoundException, IOException {
		String rootfilePath = this.getClass().getClassLoader().getResource( "" ).getPath().replace( "%20", " " );

		String flashFile = this.getClass().getClassLoader().getResource( PropertiesUtil.getPropInstance().get( "flashProperty_path" ) ).getPath().replace( "%20", " " );

		String js_path = toFilepath( rootfilePath + PropertiesUtil.getPropInstance().get( "upload_path" ) );

		File f1 = new File( js_path + File.separator + "dfp_" + app_id + ".js" );
		FileOutputStream f_o = new FileOutputStream( f1 );
		f_o.write( sb.toString().getBytes() );
		f_o.close();

		File[] fs = {
				f1, new File( flashFile )
		};

		File zipfile = new File( js_path + File.separator + "dfp_" + app_id + ".zip" );

		// 压缩
		zipFiles( fs, zipfile );
		return zipfile;
	}

	/**
	 * 构建路径
	 * @param MultipartFile
	 * @return
	 */
	private String toFilepath( String conpathfilePath ) {
		String pathfilePath = conpathfilePath + File.separator + "";
		File filePath = new File( pathfilePath );
		if( !filePath.exists() ) {
			if( !filePath.mkdir() ) {
				throw new TmsMgrServiceException( "创建目录错误" );
			}
		}
		return pathfilePath;
	}

	/**
	* 功能:压缩多个文件成一个zip文件
	* @param srcfile：源文件列表
	* @param zipfile：压缩后的文件
	*/
	private void zipFiles( File[] srcfile, File zipfile ) {
		byte[] buf = new byte[1024];
		try {
			//ZipOutputStream类：完成文件或文件夹的压缩
			ZipOutputStream out = new ZipOutputStream( new FileOutputStream( zipfile ) );
			for( int i = 0; i < srcfile.length; i++ ) {
				FileInputStream in = new FileInputStream( srcfile[i] );
				out.putNextEntry( new ZipEntry( "tmsdfp/" + srcfile[i].getName() ) );
				int len;
				while ((len = in.read( buf )) > 0) {
					out.write( buf, 0, len );
				}
				out.closeEntry();
				in.close();
			}
			out.close();
			System.out.println( "压缩完成." );
		}
		catch( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private StringBuilder makeJsString( String[] propertys, String app1_cookie ) {
		/******************************开始组织JS代码*****************************/
		StringBuilder sb = new StringBuilder( "var model = window.model ||{};" ).append( "\n" );
		sb.append( "(function(){" ).append( "\n" );
		sb.append( "var cookie_name = '" ).append( app1_cookie ).append( "';" ).append( "\n" );
		sb.append( "var deviceFinger = '';" ).append( "\n" );
		sb.append( "model.dfp = {" ).append( "\n" );
		sb.append( "/**功能---采集客户端设备的JS属性和flash属性" ).append( "\n" );
		sb.append( "  *在页面加载完后调用，var dfp = model.dfp.getDfpValues(); dfp(); //开始采集" ).append( "\n" );
		sb.append( "  *方法说明：" ).append( "\n" );
		sb.append( "  *getDfpValues() 采集设备指纹" ).append( "\n" );
		sb.append( "  *getDeviceToken() 获取设备标识" ).append( "\n" );
		sb.append( "  *getDeviceFinger（） 获取设备指纹" ).append( "\n" );
		sb.append( "  *setCookie() 把设备标识放入cookie" ).append( "\n" );
		sb.append( "  *getCookie() 获取设备指纹" ).append( "\n" );
		sb.append( "  */" ).append( "\n" );
		sb.append( "	 getDfpValues: function(){" ).append( "\n" );// 获取设备信息的方法

		sb.append( "		var pathName = document.location.pathname;" ).append( "\n" );
		sb.append( "		var index = pathName.substr(1).indexOf(\"/\");" ).append( "\n" );
		sb.append( "		var result = pathName.substr(0,index+1);" ).append( "\n" );
		sb.append( "		var curPath = window.location.href;" ).append( "\n" );
		sb.append( "		var pathname = document.location.pathname;" ).append( "\n" );
		sb.append( "		var flashPath = curPath.substr(0,curPath.indexOf(pathname))+result+'/tmsdfp/flashProperty.swf';" ).append( "\n" );

		sb.append( "		var flashId = \"as\";" ).append( "\n" );// 用于返回flash属性的flash名称
		sb.append( "		var FlashMaxLoad = 10;" ).append( "\n" );// 等待flash的最长加载时间，单位秒；   超过该时间放弃采集flash属性

		sb.append( "		var dfp_values = \"\";" ).append( "\n" );// 采集的“属性code:属性集值对”串，作为返回结果

		sb.append( "		try {" ).append( "\n" );
		sb.append(
				"			document.write(\"<object defer classid='clsid:D27CDB6E-AE6D-11CF-96B8-444553540000' id='as' name='as' codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0'  width='1' height='1'>  <param name='allowScriptAccess' value='sameDomain' />   <param name='movie' value='\"+flashPath+\"'/>	<param name='quality' value='High'/><param name='menu' value='false'/><embed allowScriptAccess='sameDomain' id='as' name='as' src='\"+flashPath+\"' pluginspage='http://www.macromedia.com/go/getflashplayer' type='application/x-shockwave-flash' name='Main' id='Main' width='1' height='1' quality='High' menu='false'></object>\");" )
				.append( "\n" );//引入flash对象

		sb.append( "			var deviceJsProperty = '" ).append( propertys[0] ).append( "';" ).append( "\n" );// TODO JS设备属性，取数据库中数据赋值
		sb.append( "			var devicePluginProperty = '" ).append( propertys[1] ).append( "';" ).append( "\n" );// TODO PLUGIN设备属性，取数据库中数据赋值
		sb.append( "			var deviceFlashProperty = '" ).append( propertys[2] ).append( "';" ).append( "\n" );// TODO FLASH设备属性，取数据库中数据赋值
		sb.append( "			var deviceComProperty = '" ).append( propertys[3] ).append( "';" ).append( "\n" );// TODO COM设备属性，取数据库中数据赋值

		sb.append( "			return function() {" ).append( "\n" );// 闭包函数 		

		/***********************************工具函数rd****开始********************************************************/
		sb.append( "				function rd(x) {" ).append( "\n" );// rd：replaceDelimiter，把分隔符|替换掉A，把分隔符:替换掉B，防止属性值中有分隔符（尤其将来的com组件值）
		sb.append( "					x = x + \"\";" ).append( "\n" );// 转换为字符串
		sb.append( "					x = x.replace(/\\|/g, 'A');" ).append( "\n" );// TODO check
		sb.append( "					x = x.replace(/:/g, \"B\");" ).append( "\n" );
		sb.append( "					return x;" ).append( "\n" );
		sb.append( "				}" ).append( "\n" );
		/***********************************工具函数rd****结束********************************************************/
		/***********************************获取javascript属性集值****开始*********************************************/
		/*---------------------plugin预处理-----开始-----------------------*/
		//IEplugin检测函数
		sb.append( "				var pluginCount = 0;" ).append( "\n" );// TODO 伪长度，Ie中为检测到的个数
		sb.append( "				function isIEPlugin(plugin) {" ).append( "\n" );
		sb.append( "					var IEPlugin = \"Flash:ShockwaveFlash.ShockwaveFlash|Media Player:MediaPlayer.MediaPlayer|QuickTime:QuickTime.QuickTime\";" )
				.append( "\n" );// //可检测IE 插件集合
		sb.append( "					var IEPluginArray = IEPlugin.split(\"|\");" ).append( "\n" );
		sb.append( "					var IEPluginArrayLength = IEPluginArray.length;" ).append( "\n" );
		sb.append( "					for ( var m = 0; m < IEPluginArrayLength; m++) {" ).append( "\n" );
		sb.append( "						try {" ).append( "\n" );
		sb.append( "							if (plugin == IEPluginArray[m].match(/^(.*)(:)(.{1,40})$/)[1]) {" ).append( "\n" );
		sb.append( "								new ActiveXObject(IEPluginArray[m].match(/^(.*)(:)(.{1,40})$/)[3]);" ).append( "\n" );
		sb.append( "								pluginCount++;" ).append( "\n" );
		sb.append( "								return true;" ).append( "\n" );
		sb.append( "							}" ).append( "\n" );
		sb.append( "						} catch (e) {" ).append( "\n" );
		sb.append( "							return false;" ).append( "\n" );
		sb.append( "						}" ).append( "\n" );
		sb.append( "					}" ).append( "\n" );
		sb.append( "					return false;" ).append( "\n" );//检测集合中没有
		sb.append( "				}" ).append( "\n" );
		//非IEplugin检测
		sb.append( "				var pluginNameString = \"\";" ).append( "\n" ); //plugins名字串，非IE
		sb.append( "				if (navigator.appName.indexOf(\"Microsoft\") == -1)" ).append( "\n" );//不是IE浏览器，IE浏览器无法采集plugins
		sb.append( "					for ( var subProp in navigator[\"plugins\"]) {" ).append( "\n" );
		sb.append( "						pluginNameString = pluginNameString + rd(navigator[\"plugins\"][subProp].name)+ \";\";" ).append( "\n" );
		sb.append( "						pluginCount++;}" ).append( "\n" );
		/*---------------------plugin预处理-----结束-----------------------*/

		sb.append( "				var deviceJsPropertyArray = deviceJsProperty.split(\"|\");" ).append( "\n" );
		sb.append( "				var deviceJsPropertyArrayLength = deviceJsPropertyArray.length;" ).append( "\n" );
		sb.append( "				var prop = \"\";" ).append( "\n" );
		sb.append( "				var prop_value = \"\";" ).append( "\n" );
		sb.append( "				for ( var i = 0; i < deviceJsPropertyArrayLength; i++) {" ).append( "\n" );
		sb.append( "					var dpa = deviceJsPropertyArray[i].match(/^(.*)(:)(.{1,8})$/);" ).append( "\n" );
		sb.append( "					if(dpa == null || dpa.length == 0) continue;" ).append( "\n" );
		sb.append( "					prop = dpa[1];" ).append( "\n" );
		sb.append( "					prop_value = dpa[3];" ).append( "\n" );
		sb.append( "					dfp_values = dfp_values + prop_value + \":\";" ).append( "\n" );
		//识别在navigator中存在的属性
		sb.append( "					if (prop in navigator){" ).append( "\n" );
		sb.append( "						if (prop == \"javaEnabled\" || prop == \"taintEnabled\") " ).append( "\n" );//prop为方法，调用该方法返回其值
		sb.append( "							dfp_values = dfp_values + rd(eval(\"navigator.\" + prop+ \"()\")) + \"|\";" ).append( "\n" );
		sb.append( "						else if (prop == \"appVersion\" && navigator.appName.indexOf(\"Microsoft\") != -1)" ).append( "\n" );//prop值为属性
		sb.append( "							dfp_values = dfp_values + rd(navigator[prop].match(/^(.*)(IE )(.*)(; Windows)(.*)$/)[3])+ \"|\";" ).append( "\n" );//appVersion IE只取版本
		sb.append( "						else if (prop == \"userAgent\" && navigator.appName.indexOf(\"Microsoft\") != -1)" ).append( "\n" );//userAgent IE只取OEM版本
		sb.append( "							dfp_values = dfp_values + rd(navigator[prop].match(/^(.*)(; )(.*)(\\))(.*)$/)[3])+ \"|\";" ).append( "\n" );
		sb.append( "						else if (prop == \"plugins\" && navigator.appName.indexOf(\"Microsoft\") != -1) " ).append( "\n" );
		sb.append( "							dfp_values = dfp_values + \"object\" + \"|\";" ).append( "\n" );
		sb.append( "						else dfp_values = dfp_values + rd(navigator[prop]) + \"|\";" ).append( "\n" );
		//识别在screen中存在的属性
		sb.append( "					} else " ).append( "\n" );
		sb.append( "						if (prop in screen){" ).append( "\n" );
		sb.append( "							if (screen[prop].toString().indexOf(\"function\") >= 0)" ).append( "\n" );//prop为方法，调用该方法返回其值
		sb.append( "								dfp_values = dfp_values + rd(eval(\"screen.\" + prop + \"()\")) + \"|\";" ).append( "\n" );
		sb.append( "							else" ).append( "\n" );
		sb.append( "								dfp_values = dfp_values + rd(screen[prop])+ \"|\";" ).append( "\n" );//prop值为属性
		//服务器端提供了浏览器不支持的属性
		sb.append( "						} else dfp_values = dfp_values + \"|\";" ).append( "\n" );
		sb.append( "				}" ).append( "\n" );
		sb.append( "				dfp_values = dfp_values.substr(0, dfp_values.length - 1);" ).append( "\n" );//去掉最后一个| 
		/********************************************获取javascript属性集值****结束*************************************/
		/********************************************获取plugin属性集值****开始*************************************/
		sb.append( "				var devicePluginPropertyArray = devicePluginProperty.split(\"|\");" ).append( "\n" );
		sb.append( "				var devicePluginPropertyArrayLength = devicePluginPropertyArray.length;" ).append( "\n" );
		sb.append( "				var prop_plugin = \"\";" ).append( "\n" );
		sb.append( "				var prop_plugin_value = \"\";" ).append( "\n" );
		sb.append( "				var dfp_plugin_values = \"\";" ).append( "\n" );
		sb.append( "				var isExistPluginCount = false;" ).append( "\n" );
		sb.append( "				var pluginCountId = \"\";" ).append( "\n" );
		sb.append( "				for ( var i = 0; i < devicePluginPropertyArrayLength; i++) {" ).append( "\n" );
		sb.append( "					var dpa = devicePluginPropertyArray[i].match(/^(.*)(:)(.{1,8})$/);" ).append( "\n" );
		sb.append( "					if(dpa == null || dpa.length == 0) continue;" ).append( "\n" );
		sb.append( "					prop_plugin = dpa[1];" ).append( "\n" );
		sb.append( "					prop_plugin_value = dpa[3];" ).append( "\n" );
		sb.append( "					if (prop_plugin == \"pluginCount\"){" ).append( "\n" );
		sb.append( "						isExistPluginCount = true;" ).append( "\n" );
		sb.append( "						pluginCountId = prop_plugin_value + \":\";" ).append( "\n" );
		sb.append( "					}" ).append( "\n" );
		sb.append( "					else {" ).append( "\n" );
		sb.append( "						dfp_plugin_values = dfp_plugin_values + prop_plugin_value + \":\";" ).append( "\n" );
		sb.append( "						if (navigator.appName.indexOf(\"Microsoft\") == -1 && pluginNameString != undefined && pluginNameString != \"\")" ).append( "\n" );//不是IE浏览器
		sb.append( "							dfp_plugin_values = dfp_plugin_values + (pluginNameString.indexOf(prop_plugin) > -1) + \"|\";" ).append( "\n" );
		sb.append( "						else" ).append( "\n" );
		sb.append( "							dfp_plugin_values = dfp_plugin_values + isIEPlugin(prop_plugin) + \"|\";" ).append( "\n" );//IE浏览器获取不到plugins
		sb.append( "				}}" ).append( "\n" );
		sb.append( "				if(isExistPluginCount)" ).append( "\n" );
		sb.append( "					dfp_plugin_values = dfp_plugin_values + pluginCountId + pluginCount + \"|\";" ).append( "\n" );
		sb.append( "				if(dfp_plugin_values.length > 0){dfp_values = dfp_values + \"|\" + dfp_plugin_values.substr(0, dfp_plugin_values.length - 1);}" )
				.append( "\n" );
		sb.append( "				deviceFinger = dfp_values;" ).append( "\n" );
		/********************************************获取plugin属性集值****结束*************************************/
		/******************************************* 获取Flash属性集值*********开始**************************************/
		sb.append( "				var flashObject;" ).append( "\n" );//Flash对象 
		sb.append( "				if (navigator.appName.indexOf(\"Microsoft\") != -1)" ).append( "\n" );
		sb.append( "					flashObject = window[flashId];" ).append( "\n" );
		sb.append( "				else" ).append( "\n" );
		sb.append( "					flashObject = document.embeds[flashId];" ).append( "\n" );
		//避免flash未加载完毕，轮询方式调用flash方法，FlashMaxLoad秒钟未加载放弃获取
		sb.append( "				var pollCount = 0;" ).append( "\n" );
		sb.append( "				dfp_values=\"\";" ).append( "\n" );
		sb.append( "				var interval = setInterval(" ).append( "\n" );
		sb.append( "				function() {" ).append( "\n" );
		sb.append( "					try {" ).append( "\n" );
		sb.append( "						if (pollCount > FlashMaxLoad * 10) " ).append( "\n" );//FlashMaxLoad*10*100毫秒
		sb.append( "							{document.getElementById(flashId).style.display = \"none\";" ).append( "\n" );//隐藏FLash
		sb.append( "							clearInterval(interval);}" ).append( "\n" );
		sb.append( "						pollCount++;" ).append( "\n" );
		sb.append( "						if (navigator.appVersion.indexOf(\"MSIE 10\") != -1)" ).append( "\n" );//IE10
		sb.append( "							dfp_values = flashObject[0].jsGetDeviceFlashProperty(deviceFlashProperty);" ).append( "\n" );
		sb.append( "						else" ).append( "\n" );
		sb.append( "							dfp_values = flashObject.jsGetDeviceFlashProperty(deviceFlashProperty);" ).append( "\n" );
		sb.append( "						if (dfp_values.length > 10) {" ).append( "\n" );
		sb.append( "							dfp_values = XMLEncode(dfp_values);" ).append( "\n" );
		sb.append( "							deviceFinger += \"|\" + dfp_values;" ).append( "\n" );
		sb.append( "							document.getElementById(flashId).style.display = \"none\";" ).append( "\n" );//隐藏FLash
		sb.append( "							clearInterval(interval);}" ).append( "\n" );
		sb.append( "					} catch (ex) {pollCount++;}" ).append( "\n" );
		sb.append( "				}, 100);" ).append( "\n" );
		/****************************获取Flash属性集值*********结束********************************************************/

		/****************************获取com属性集值*********开始********************************************************/
		/*********************com属性集合定义*********************
		由服务器端生成；
		格式为 属性名：code对，以|做分隔符；如com1:501|com2:502;
		最多5个，且名称只能为com1，com2，com3，com4，com5
		var deviceComProperty="com1:501|com2:502|com3:503|com4:504|com5:505";
		************************************************************/
		sb.append( "				function getcom1() {return \"1\";}" ).append( "\n" );
		sb.append( "				function getcom2() {return \"2\";}" ).append( "\n" );
		sb.append( "				function getcom3() {return \"3\";}" ).append( "\n" );
		sb.append( "				function getcom4() {return \"4\";}" ).append( "\n" );
		sb.append( "				function getcom5() {return \"5\";}" ).append( "\n" );
		sb.append( "				var deviceComPropertyArray = deviceComProperty.split(\"|\");" ).append( "\n" );
		sb.append( "				var deviceComPropertyArrayLength = deviceComPropertyArray.length;" ).append( "\n" );
		sb.append( "				var comProp = \"\";" ).append( "\n" );
		sb.append( "				var dfp_com_values = \"\";" ).append( "\n" );
		sb.append( "				dfp_values = \"\";" ).append( "\n" );
		sb.append( "				for ( var i = 0; i < deviceComPropertyArrayLength; i++) {" ).append( "\n" );
		sb.append( "					var dpa = deviceComPropertyArray[i].match(/^(.*)(:)(.{1,8})$/);" ).append( "\n" );
		sb.append( "					if(dpa == null || dpa.length == 0) continue;" );
		sb.append( "					comProp = deviceComPropertyArray[i].match(/^(.*)(:)(.{1,8})$/)[1];" ).append( "\n" );
		sb.append( "					comProp_value = deviceComPropertyArray[i].match(/^(.*)(:)(.{1,8})$/)[3];" ).append( "\n" );
		sb.append( "					dfp_values = dfp_values + comProp_value + \":\"+ rd(eval(\"get\" + comProp + \"()\")) + \"|\";" ).append( "\n" );
		/*******************************获取com属性集值*********结束********************************************************/

		sb.append( "			}" ).append( "\n" );
		sb.append( "			if(dfp_values.length > 0){" ).append( "\n" );
		sb.append( "				deviceFinger += \"|\" +  dfp_values.substr(0, dfp_values.length - 1);" ).append( "\n" );
		sb.append( "			}" ).append( "\n" );

		sb.append( "		}} catch (ex) {}" ).append( "\n" );
		sb.append( "}," ).append( "\n" );//getDfpValues函数结束

		//写cookies
		sb.append( "setCookie:function (value)" ).append( "\n" );
		sb.append( "{" ).append( "\n" );
		sb.append( "		if(value == null || value.length==0) return;" ).append( "\n" );
		sb.append( "		var Days = 365;" ).append( "\n" );
		sb.append( "		var exp = new Date();" ).append( "\n" );
		sb.append( "		exp.setTime(exp.getTime() + Days*24*60*60*1000);" ).append( "\n" );
		sb.append( "		document.cookie = cookie_name + \"=\"+ escape (value) + \";expires=\" + exp.toGMTString();" ).append( "\n" );
		sb.append( "}," ).append( "\n" );

		//读cookies
		sb.append( "getCookie: function()" ).append( "\n" );
		sb.append( "{" ).append( "\n" );
		sb.append( "		var arrStr = document.cookie.split(\"; \");" ).append( "\n" );
		sb.append( "		for (var i = 0; i < arrStr.length; i++) {" ).append( "\n" );
		sb.append( "			var temp = arrStr[i].split(\"=\");" ).append( "\n" );
		sb.append( "			if (temp[0] == cookie_name) " ).append( "\n" );
		sb.append( "				return unescape(temp[1]);" ).append( "\n" );
		sb.append( "		}" ).append( "\n" );
		sb.append( "}," ).append( "\n" );

		// 获取设备标识
		sb.append( "getDeviceToken: function()" ).append( "\n" );
		sb.append( "{" ).append( "\n" );
		sb.append( "		var deviceToken = '';" ).append( "\n" );
		sb.append( "		var d = model.dfp.getCookie();" ).append( "\n" );
		sb.append( "		if(d !=null && d!=undefined && d!=''){" ).append( "\n" );
		sb.append( "			deviceToken=d;" ).append( "\n" );
		sb.append( "		}" ).append( "\n" );
		sb.append( "		return deviceToken;" ).append( "\n" );
		sb.append( "}," ).append( "\n" );

		// 获取设备指纹
		sb.append( "getDeviceFinger: function()" ).append( "\n" );
		sb.append( "{" ).append( "\n" );
		sb.append( "		return deviceFinger;" ).append( "\n" );
		sb.append( "}" ).append( "\n" );

		sb.append( "};" ).append( "\n" );

		sb.append( "function XMLEncode(str)" ).append( "\n" );
		sb.append( "{" ).append( "\n" );
		sb.append( "		if(str == null || str.length == 0) return '';" ).append( "\n" );
		sb.append( "     str=str.replace(new RegExp('&','g'),\"&amp;\");" ).append( "\n" );
		sb.append( "     str=str.replace(new RegExp('<','g'),\"&lt;\");" ).append( "\n" );
		sb.append( "     str=str.replace(new RegExp(\">\",'g'),\"&gt;\");" ).append( "\n" );
		sb.append( "     str=str.replace(new RegExp(\"'\",'g'),\"&apos;\");" ).append( "\n" );
		sb.append( "     str=str.replace(new RegExp(\"\\\"\",'g'),\"&quot;\");" ).append( "\n" );
		sb.append( "     return str;" ).append( "\n" );
		sb.append( "}" ).append( "\n" );
		sb.append( "})();" ).append( "\n" );

		return sb;
	}

	/**
	* 方法描述:
	* @return
	*/

	private String[] queryDeviceProperty( String app_id ) {
		String sql = "SELECT P.* FROM TMS_DFP_APP_PROPERTIES AP,TMS_DFP_PROPERTY P WHERE AP.PROP_ID=P.PROP_ID AND AP.APP_ID=?";
		List<Map<String, Object>> p_l = tmsSimpleDao.queryForList( sql, app_id );

		if( p_l == null || p_l.size() == 0 ) return null;

		String[] r = {
				"", "", "", ""
		};
		for( Map<String, Object> p : p_l ) {
			String prop_type = MapUtil.getString( p, "PROP_TYPE" );
			String prop_id = MapUtil.getString( p, "PROP_ID" );
			String prop_name = MapUtil.getString( p, "PROP_NAME" );
			if( "JS".equalsIgnoreCase( prop_type ) ) {
				r[0] = makeProString( r[0], prop_id, prop_name );
			}
			else if( "PLUGIN".equalsIgnoreCase( prop_type ) ) {
				r[1] = makeProString( r[1], prop_id, prop_name );
			}
			else if( "FLASH".equalsIgnoreCase( prop_type ) ) {
				r[2] = makeProString( r[2], prop_id, prop_name );
			}
			else if( "COM".equalsIgnoreCase( prop_type ) ) {
				r[3] = makeProString( r[3], prop_id, prop_name );
			}
		}
		return r;
	}

	private String makeProString( String r, String prop_id, String prop_name ) {
		if( r == null || r.length() == 0 ) {
			r = prop_name + ":" + prop_id;
		}
		else {
			r += "|" + prop_name + ":" + prop_id;
		}
		return r;
	}

	/**
	 * 解除用户和设备的绑定关系
	 * @param list
	 * @return
	 */
	public void unbindUserDeviceRel( List<Map<String, ?>> list ) {
		String sql = "delete from TMS_DFP_USER_DEVICE where DEVICE_ID = :DEVICE_ID and USER_ID = :USERID";
		tmpSimpleDao.batchUpdate( sql, list );
		officialSimpleDao.batchUpdate( sql, list );
	}
}