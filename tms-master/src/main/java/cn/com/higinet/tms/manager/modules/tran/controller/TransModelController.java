package cn.com.higinet.tms.manager.modules.tran.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.CodeDictService;
import cn.com.higinet.tms.manager.modules.tran.service.TransModelService;

/**
 * 交易定义控制器
 * @author yangk
 * @author zhang.lei
 */
@RestController
@RequestMapping(ManagerConstants.URI_PREFIX + "/tranmdl")
public class TransModelController {

	private static Log log = LogFactory.getLog( TransModelController.class );

	@Autowired
	private TransModelService transModelService;
	@Autowired
	private CodeDictService tmsCodeDictService;
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 初始化交易模型
	 */
	@RequestMapping(value = "/query", method = RequestMethod.POST)
	public Model initTranModel( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String txnid = reqs.get( "tab_name" );
		List<Map<String, Object>> tranMdlList = transModelService.getTranModels( txnid );

		List<Map<String, Object>> txn_ref_fds = transModelService.getTranModelsRef( txnid );
		List<Map<String, Object>> txn_ref_tab = transModelService.getTranModelsRefTab( txnid );

		model.set( "txnfds", tranMdlList );
		model.set( "txn_ref_fds", txn_ref_fds );
		model.set( "txn_ref_tab", txn_ref_tab );
		model.set( "allStoreFd", queryAllStoreFd().get( "allStoreFd" ) );

		RequestModel map = new RequestModel();
		map.put( "tab_name", txnid );
		model.set( "enableStoreFd", queryAvailableStoreFd( map ).get( "enableStoreFd" ) );

		return model;
	}

	/**
	 * 初始化交易模型正式库
	 */
	@RequestMapping(value = "/queryOf", method = RequestMethod.POST)
	public Model initTranModeOf( @RequestBody Map<String, String> reqs ) {

		Model model = new Model();
		String txnid = reqs.get( "tab_name" );

		List<Map<String, Object>> refTblFd = transModelService.getCanRefTable();
		List<Map<String, Object>> sourceType = tmsCodeDictService.listCodeCateGorp( null );

		model.set( "sourceType", sourceType );
		model.set( "refTblFd", refTblFd );
		model.set( "canRefTable", getCanRefTable( refTblFd ) );
		model.set( "func", getFunc() );
		model.set( "funcParam", getFuncParam() );

		return model;
	}

	/**
	 * 取ref中可引用字段
	 */
	@RequestMapping(value = "/queryCanRefFd", method = RequestMethod.POST)
	public Model getCanRefFd( @RequestBody String tab_name ) {

		Model model = new Model();
		model.set( "canRefFd", transModelService.getCanRefFd( tab_name ) );
		return model;
	}

	/**
	 * 取ref中可引用表 
	 */
	private List<Map<String, Object>> getCanRefTable( List<Map<String, Object>> refTblFd ) {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for( Map<String, Object> cols : refTblFd ) {

			String tableName = MapUtil.getString( cols, "TAB_NAME" );

			boolean has = false;

			for( Map<String, Object> tabs : list ) {

				String inner_tableName = MapUtil.getString( tabs, "TAB_NAME" );

				if( inner_tableName.equals( tableName ) ) {
					has = true;
					break;
				}
			}

			if( !has ) {
				//cols.remove("FD_NAME");
				//cols.remove("NAME");
				list.add( cols );
			}
		}

		return list;
	}

	/**
	 * 取当前交易下未被占用的存储字段(fd_name)
	 */
	@RequestMapping(value = "/queryAvailableStoreFd", method = RequestMethod.POST)
	public Model queryAvailableStoreFd( @RequestBody RequestModel modelMap ) {
		Model model = new Model();
		model.set( "enableStoreFd", transModelService.getAvailableStoreFd( modelMap.getString( "tab_name" ) ) );
		return model;
	}

	/**
	 * 取所有的存储字段(占用和未占用的)
	 */
	@RequestMapping(value = "/queryAllStoreFd", method = RequestMethod.POST)
	public Model queryAllStoreFd() {
		Model model = new Model();
		model.set( "allStoreFd", transModelService.getAllStoreFd() );
		return model;
	}

	/**
	 * 关联函数和函数参数
	 */
	@RequestMapping(value = "/queryFunWithParam", method = RequestMethod.POST)
	public Model mixFuncParam() {

		Model model = new Model();
		List<Map<String, Object>> func = getFunc();
		List<Map<String, Object>> funParam = getFuncParam();

		List list = new ArrayList();

		for( Map<String, Object> fun : func ) {

			String funcid = MapUtil.getString( fun, "FUNC_ID" );
			List param_list = new ArrayList();

			for( Map<String, Object> param : funParam ) {

				String p_funcid = MapUtil.getString( param, "FUNC_ID" );

				if( funcid.equals( p_funcid ) ) {
					/*String p_orderby = MapUtil.getString(param, "PARAM_ORDERBY");
					if("1".equals(p_orderby)) {
						String p_type = MapUtil.getString(param, "PARAM_TYPE");
						fun.put("FUNC_SRC_TYPE", p_type);//函数源数据类型
					}else{
						param_list.add(param);
					}*/
					param_list.add( param );
				}
			}

			if( param_list.size() > 0 ) {
				fun.put( "params", param_list );
			}

			list.add( fun );
		}
		model.set( "funWithParam", list );

		return model;
	}

	/**
	 * 保存交易模型
	 */
	@RequestMapping(value = "/saveModel", method = RequestMethod.POST)
	public Model saveModel( @RequestBody Map<String, String> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, ?>>> formMap = null;
		try {
			formMap = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e );
			throw new TmsMgrWebException( "保存开关Json数据解析异常" );
		}

		Model model = new Model();
		model.setRow( transModelService.saveModel( formMap ) );

		return model;
	}

	/**
	 * 保存交易模型引用表
	 */
	@RequestMapping(value = "/saveModelRefTab", method = RequestMethod.POST)
	public Model saveModelRefTab( @RequestBody Map<String, String> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, ?>>> formMap = null;
		try {
			formMap = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e );
			throw new TmsMgrWebException( "保存开关Json数据解析异常" );
		}

		Model model = new Model();
		model.setRow( transModelService.saveModelRefTab( formMap ) );

		return model;
	}

	/**
	 * 保存交易模型引用字段
	 */
	@RequestMapping(value = "/saveModelRefFd", method = RequestMethod.POST)
	public Model saveModelRefFd( @RequestBody Map<String, String> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, ?>>> formMap = null;
		try {
			formMap = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e );
			throw new TmsMgrWebException( "保存开关Json数据解析异常" );
		}

		Model model = new Model();
		model.setRow( transModelService.saveModelRefFd( formMap ) );

		return model;
	}

	/**
	 * 取全部函数
	 */
	private List<Map<String, Object>> getFunc() {
		return transModelService.getFunc();
	}

	/**
	 * 取全部函数参数
	 */
	private List<Map<String, Object>> getFuncParam() {
		return transModelService.getFuncParam();
	}

	/********************************shuming*********************************************/
	//交易查询界面，数据模型的查询
	@RequestMapping(value = "/query_trans", method = RequestMethod.GET)
	public String queryTrans() {
		return "tms35/query/query_trans";//htm地址
	}

	@RequestMapping(value = "/get_trans_by_tabname", method = RequestMethod.POST)
	public Model getTransByTabnameList( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();

		try {
			List<Map<String, Object>> dblist = transModelService.getTransByTabnameList( reqs );
			model.setRow( dblist );
		}
		catch( Exception e ) {
			log.error( e );
			model.addError( e.toString() );
		}
		return model;
	}

	@RequestMapping(value = "/get_trandata", method = RequestMethod.POST)
	public Model getTrandataList( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		try {
			List<Map<String, Object>> listdb = transModelService.getTransByTabnameList( reqs );
			boolean isPage = true;
			Page<Map<String, Object>> datePage = transModelService.getTrandataPage( reqs, listdb, isPage );
			model.setPage( datePage );
		}
		catch( Exception e ) {
			log.error( e );
			model.addError( e.toString() );
		}
		return model;
	}

	/********************************wuruiqi*********************************************/
	/**
	 * 导出数据到新版本的Excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/exportXLSX", method = RequestMethod.GET)
	public void exportList2XLSXAction( HttpServletRequest request, HttpServletResponse response ) {
		exportCommonAction( "xlsx", request, response );
	}

	public void exportCommonAction( String expType, HttpServletRequest request, HttpServletResponse response ) {
		transModelService.exportList( expType, request, response );
	}
}
