package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.NameListService;

/**
 * 名单管理控制类，包括名单的管理和名单值的管理
 * @author wangsch
 * @author zhang.lei
 */
@Controller("nameListController")
@RequestMapping("/tms/mgr")
public class NameListController {
	
	@Autowired
	private NameListService nameListService;

	@Autowired
	private ObjectMapper objectMapper;

	private String error = "";

	private String size = "0";

	//private int export_size = 0;

	private String rosterDesc = "";

	/**
	 * 名单列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listNameListView() {
		return "tms/mgr/name_list";
	}

	/**
	 * 名单列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listNameListActoin( @RequestBody Map<String, String> modelMap ) {
		Model model = new Model();
		model.setPage( nameListService.listNameList( modelMap ) );
		return model;
	}

	/**
	 * 名单新增页面
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addNameListView() {
		return "tms/mgr/name_add";
	}

	/**
	 * 新增名单
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addNameListActoin( @RequestBody ModelMap modelMap, HttpServletRequest request ) {
		//检验名称是否已经存在
		Model model = new Model();
		List<Map<String, Object>> nameList = nameListService.getNameListByName( modelMap.get( "rostername" ).toString() );
		if( nameList.size() > 0 ) {
			model.addError( "名单英文名称已经存在" );
			return model;
		}

		String rosterdesc = MapUtil.getString( modelMap, "rosterdesc" );
		List<Map<String, Object>> descList = nameListService.getNameListByDesc( rosterdesc );

		if( descList.size() > 0 ) {
			model.addError( "名单名称已经存在" );
			return model;
		}
		//String rostertype = reqs.get("rostertype");
		String datatype = MapUtil.getString( modelMap, "datatype" );
		List<Map<String, Object>> list = null;
		if( StringUtil.isNotEmpty( datatype ) ) {
			if( !"acc".equals( datatype ) && !"string".equals( datatype ) ) {
				//名单数据类型和名单类型相同的名单不可重复添加
				String rostertype = MapUtil.getString( modelMap, "rostertype" );
				list = nameListService.getRosterListForAdd( (Map) modelMap );
			}
		}
		if( list == null || list.size() == 0 ) {
			if( nameListService.isDuplicateByRosterAndTransAttr( MapUtil.getString( modelMap, "rostername" ) ) ) {
				model.addError( "名单英文名称和交易模型中的属性代码重复" );
			}
			else {
				model.setRow( nameListService.createNameList( (Map) modelMap ) );
			}
		}
		else {
			model.addError( "该类型的名单已经存在" );
		}
		request.setAttribute( "rosterdesc", rosterdesc );
		return model;
	}

	/**
	 * 名单编辑页面
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String editNameListView() {
		return "tms/mgr/name_edit";
	}

	/**
	 * 名单单个查询交易
	 * @param rosterId
	 * @return
	 */
	@RequestMapping(value = "/get")
	public Model getNameListActoin( @RequestBody RequestModel modelMap ) {
		Model model = new Model();
		model.setRow( nameListService.getOneNameList( modelMap.getString( "rosterId" ) ) );
		return model;
	}

	/**
	 * 名单单个修改交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model updateNameListActoin( @RequestBody Map<String, String> modelMap, HttpServletRequest request ) {
		Model model = new Model();
		request.setAttribute( "rosterdesc", modelMap.get( "rosterdesc_span" ) );
		nameListService.updateOneNameList( modelMap );
		return model;
	}

	/**
	 * 名单删除交易，支持批量删除。删除名单表以及关联的名单值表
	 * @param arrs
	 * @return
	 */
	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public Model delNameListActoin( @RequestBody Map<String, List<Map<String, String>>> modelMap, HttpServletRequest request ) {
		Model model = new Model();
		/*	String json = MapUtil.getString( modelMap, "postData" );
			Map<String, List<Map<String, String>>> formList = null;
			try {
				formList = objectMapper.readValue( json, Map.class );
			}
			catch( Exception e ) {
				e.printStackTrace();
				throw new TmsMgrWebException( "删除名单Json数据解析异常" );
			}*/

		nameListService.deleteNameList( modelMap );

		//日志需要
		try {
			request.setAttribute( "postData", objectMapper.writeValueAsString( modelMap ) );
		}
		catch( Exception e ) {
			e.printStackTrace();
		}

		return model;
	}

	/**
	 * 名单值列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/valuelist", method = RequestMethod.GET)
	public String listValueList() {
		return "tms/mgr/value_list";
	}

	/**
	 * 名单值列表查询交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/valuelist", method = RequestMethod.POST)
	public Model listValueListActoin( @RequestBody Map<String, String> reqs ) {
		String rosterId = reqs.get( "rosterId" );

		Model model = new Model();
		// 查询条件
		reqs.put( DBConstant.TMS_MGR_ROSTERVALUE_ROSTERID, rosterId );

		model.setPage( nameListService.listValueListByPage( reqs ) );

		return model;
	}

	/**
	 * 名单值新建页面 
	 * @return
	 */
	@RequestMapping(value = "/valueadd", method = RequestMethod.GET)
	public String addValueListView() {
		return "tms/mgr/value_add";
	}

	/**
	 * 名单值新建交易
	 * @param reqs
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/valueadd", method = RequestMethod.POST)
	public Model addValueListActoin( @RequestBody RequestModel modelMap, HttpServletRequest request ) {
		Model model = new Model();
		try {
			model.setRow( nameListService.createValueList( modelMap ) );
		}
		catch( Exception e ) {
			e.printStackTrace();
			model.addError( "名单值新建失败" );
		}
		request.setAttribute( "rosterdesc", modelMap.get( "rosterdesc" ) );
		request.setAttribute( "rostervalue", modelMap.get( "rostervalue" ) );
		return model;
	}

	/**
	 * 名单值修改页面
	 * @return
	 */
	@RequestMapping(value = "/valueedit", method = RequestMethod.GET)
	public String editValueListView() {
		return "tms/mgr/value_edit";
	}

	/**
	 * 名单值单个查询交易
	 * @param rosterValueId
	 * @return
	 */
	@RequestMapping(value = "/valueget")
	public Model getValueListActoin( @RequestBody Map<String, String> reqs ) {
		String rosterValueId = reqs.get( "rosterValueId" );
		Model model = new Model();
		// 通过名单值表的主键查询名单值信息
		model.setRow( nameListService.getOneValueList( rosterValueId ) );
		return model;
	}

	/**
	 * 名单值修改交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/valuemod", method = RequestMethod.POST)
	public Model updateValueListActoin( @RequestBody Map<String, Object> reqs, HttpServletRequest request ) {
		Model model = new Model();
		try {
			nameListService.updateOneValueList( reqs );
		}
		catch( Exception e ) {
			model.addError( "名单编辑失败" );
		}
		request.setAttribute( "rosterdesc", reqs.get( "rosterdesc" ) );
		request.setAttribute( "rostervalue", reqs.get( "rostervalue" ) );
		return model;
	}

	/**
	 * 名单值删除交易，支持批量删除
	 * @param arrs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/valuedel")
	public Model delValueListActoin( @RequestBody Map<String, Object> reqs, HttpServletRequest request ) {
		Model model = new Model();
		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, String>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new TmsMgrWebException( "删除名单值Json数据解析异常" );
		}
		nameListService.deleteValueList( formList );
		//日志需要
		try {
			request.setAttribute( "postData", objectMapper.writeValueAsString( formList ) );
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return model;
	}

	/**
	 * 名单值转换的查询交易，查询可供转换的名单类型
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/changevalueget", method = RequestMethod.POST)
	public Model getChangeValueList( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( DBConstant.TMS_MGR_ROSTER_ROSTERTYPE, reqs.get( "rostertype" ) );
		conds.put( DBConstant.TMS_MGR_ROSTER_DATATYPE, reqs.get( "datatype" ) );
		conds.put( DBConstant.TMS_MGR_ROSTER_ROSTERID, reqs.get( "rosterId" ) );
		model.setRow( nameListService.getRosterTypeList( conds ) );
		return model;
	}

	/**
	 * 名单值转换交易
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/changevalue", method = RequestMethod.POST)
	public Model updateRosterIdActoin( @RequestBody Map<String, Object> reqs, HttpServletRequest request ) {
		Model model = new Model();
		String str = MapUtil.getString( reqs, "rosterId" );
		String[] strArr = str.split( "\\|\\|\\|\\|" );
		String rosterId = "";
		String rosterdescEnter = "";
		if( 2 == strArr.length ) {
			rosterId = strArr[0];
			rosterdescEnter = strArr[1];
		}
		reqs.put( "rosterId", rosterId );
		nameListService.updateOneValueListForConvert( reqs );
		request.setAttribute( "rosterdescOut", reqs.get( "rosterdescOut" ) );
		request.setAttribute( "rosterdescEnter", rosterdescEnter );
		request.setAttribute( "rostervalue", reqs.get( "rostervalue" ) );
		return model;
	}

	/**
	 * 导入列表数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/import", method = RequestMethod.GET)
	public String importNameListView() {
		return "tms/mgr/name_import";
	}

	/**
	 * 名单导入excel
	 * @return
	 */
	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public String importNameListAction( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Map<String, Object> inputNode = new HashMap<String, Object>();
		MultipartHttpServletRequest multipartrequest = (MultipartHttpServletRequest) request;
		MultipartFile file = multipartrequest.getFile( "importFile" );
		String pathfilePath = this.getClass().getClassLoader().getResource( "/" ).getPath();
		String rosterId = reqs.get( "ROSTERID" );
		String rosterDesc = reqs.get( "ROSTERDESC" );
		String format = reqs.get( "FORMAT" );
		if( ".xls".equals( format ) ) {
			inputNode.put( "TYPE", "excel2k3" );
		}
		else if( ".xlsx".equals( format ) ) {
			inputNode.put( "TYPE", "excel2k7" );
		}
		else if( ".csv".equals( format ) || ".txt".equals( format ) ) {
			inputNode.put( "TYPE", "text" );
		}
		inputNode.put( "importFile", file );
		inputNode.put( "filePath", pathfilePath.replace( "%20", " " ) );
		inputNode.put( "ROSTERID", rosterId );
		inputNode.put( "ROSTERDESC", rosterDesc );

		List<Map<String, ?>> reqsList = new ArrayList();
		Map<String, Object> reqsMap = new HashMap<String, Object>();
		Map<String, List<Map<String, ?>>> batchUpdateMap = new HashMap<String, List<Map<String, ?>>>();
		reqsMap.put( "reqs", inputNode );
		reqsList.add( reqsMap );
		batchUpdateMap.put( "reqsList", reqsList );

		Object importObj = nameListService.importRoster( batchUpdateMap );
		if( importObj instanceof String ) {
			this.error = String.valueOf( importObj );
		}
		else if( importObj instanceof Integer ) {
			this.error = "";
			this.size = String.valueOf( importObj );
			this.rosterDesc = rosterDesc;
		}
		//Map<String,Object> roster = nameListService.selectById(rosterId);
		return "/tms/mgr/name_error";
	}

	/**
	 * 获取名单导入时的信息，记录日志
	 */
	@RequestMapping(value = "/importLog", method = RequestMethod.POST)
	public Model getErrorInfoActoin( HttpServletRequest request ) {
		Model model = new Model();
		if( StringUtil.isEmpty( this.error ) ) {
			model.set( "importInfo", "导入成功！" );
			request.setAttribute( "size", StringUtil.isEmpty( this.size ) ? "0" : this.size ); //导入的数据条数
			request.setAttribute( "rosterdesc", this.rosterDesc ); //名单名称
		}
		else {
			model.addError( this.error );
			model.set( "url", "/tms/mgr/list" );
		}
		return model;
	}

	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public void exportListAction( @RequestBody Map<String, String> reqs, HttpServletResponse response ) {
		String type; //导出类型
		if( StringUtil.isEmpty( reqs.get( "flag" ) ) ) { //默认导出类型为csv
			type = "csv";
		}
		else {
			type = reqs.get( "flag" );
		}

		List<Map<String, Object>> dateList = nameListService.listValueListById( reqs );
		String rosterdesc = MapUtil.getString( dateList.get( 0 ), "rosterdesc" );
		String name = rosterdesc + "-" + CalendarUtil.getCalendarByFormat( CalendarUtil.FORMAT9 );
		response.reset();

		if( "xlsx".equals( type ) || "xls".equals( type ) ) { //导出Excel
			try {
				response.setContentType( "application/msexcel" );
				OutputStream os = response.getOutputStream();

				if( "xlsx".equals( type ) ) {
					XSSFWorkbook workbook = createExcel2k7Workbook( dateList );

					response.setHeader( "Content-disposition", "attachment; filename=" + new String( name.getBytes( "GB2312" ), "iso8859-1" ) + ".xlsx" );
					workbook.write( os );
				}
				else if( "xls".equals( type ) ) {
					HSSFWorkbook workbook = createExcel2k3Workbook( dateList );

					response.setHeader( "Content-disposition", "attachment; filename=" + new String( name.getBytes( "GB2312" ), "iso8859-1" ) + ".xls" );
					workbook.write( os );
				}
			}
			catch( Exception e ) {
				e.printStackTrace();
				throw new TmsMgrWebException( "导出名单失败。" );
			}
		}
		else { //导出csv或txt
			try {
				if( "csv".equals( type ) ) {
					response.setHeader( "Content-disposition", "attachment; filename=" + new String( name.getBytes( "GB2312" ), "iso8859_1" ) + ".csv" );
					response.setContentType( "application/octet-stream" );
				}
				else if( type == "txt" || "txt".equals( type ) ) {
					response.setHeader( "Content-disposition", "attachment; filename=" + new String( name.getBytes( "GB2312" ), "iso8859_1" ) + ".txt" );
					response.setContentType( "application/octet-stream" );
				}

				ServletOutputStream outSTr = response.getOutputStream(); //从response获取输出流
				BufferedOutputStream buff = new BufferedOutputStream( outSTr ); //在ServletOutputStream基础上套一层，构成具有缓冲功能的BufferedOutputStream
				StringBuffer write = new StringBuffer();
				String enter = "\r\n";
				write.append( "名单值,开始时间,结束时间,备注" ).append( enter );//各个列用逗号分隔
				for( int i = 0; i < dateList.size(); i++ ) {
					Map<String, Object> txn = dateList.get( i );
					if( "csv".equals( type ) ) {
						write.append( MapUtil.getString( txn, "ROSTERVALUE" ) + "\t" ).append( "," ).append( MapUtil.getString( txn, "ENABLETIME" ) + "\t" ).append( "," ).append( MapUtil.getString( txn, "DISABLETIME" ) + "\t" ).append( "," ).append( MapUtil.getString( txn, "REMARK" ).trim().length() == 0 ? " " : MapUtil.getString( txn, "REMARK" ) + "\t" ).append( enter );
					}
					else if( "txt".equals( type ) ) {
						write.append( MapUtil.getString( txn, "ROSTERVALUE" ) ).append( "," ).append( MapUtil.getString( txn, "ENABLETIME" ) ).append( "," ).append( MapUtil.getString( txn, "DISABLETIME" ) ).append( "," ).append( MapUtil.getString( txn, "REMARK" ).trim().length() == 0 ? " " : MapUtil.getString( txn, "REMARK" ) ).append( enter );
					}
				}
				buff.write( write.toString().getBytes( "GB2312" ) );
				buff.flush();
				buff.close();
			}
			catch( IOException e ) {
				e.printStackTrace();
				throw new TmsMgrWebException( "导出名单失败！" );
			}

			//export_size = dateList.size();
		}
	}

	/**
	 * @param dateList
	 * @return
	 */
	private XSSFWorkbook createExcel2k7Workbook( List<Map<String, Object>> dateList ) {
		XSSFWorkbook workbook = new XSSFWorkbook(); //创建工作簿
		XSSFSheet sheet = workbook.createSheet( "名单值列表" ); //创建工作表

		//创建字体格式     
		XSSFFont font = workbook.createFont();
		font.setBold( true );
		// 设置样式
		XSSFCellStyle cs = workbook.createCellStyle();
		cs.setFont( font );

		XSSFRow titleRow = sheet.createRow( 0 ); //一行
		String titles[] = {
				"名单值", "开始时间", "结束时间", "备注"
		}; //标题
		for( int i = 0; i < titles.length; i++ ) { //产生标题
			sheet.setColumnWidth( i, 3800 );
			XSSFCell cell = titleRow.createCell( i );
			cell.setCellValue( titles[i] );
			cell.setCellStyle( cs );
		}

		for( int i = 0; i < dateList.size(); i++ ) { //产生数据行
			Map<String, Object> rosterValueMap = dateList.get( i );
			XSSFRow dataRow = sheet.createRow( i + 1 );
			XSSFCell valueCell = dataRow.createCell( 0 );
			XSSFCell enabletimeCell = dataRow.createCell( 1 );
			XSSFCell disabletimeCell = dataRow.createCell( 2 );
			XSSFCell remarkCell = dataRow.createCell( 3 );

			//填充数据
			valueCell.setCellValue( MapUtil.getString( rosterValueMap, "ROSTERVALUE" ) );
			String enabletime = MapUtil.getString( rosterValueMap, "ENABLETIME" );
			String disabletime = MapUtil.getString( rosterValueMap, "DISABLETIME" );
			enabletimeCell.setCellValue( enabletime );
			disabletimeCell.setCellValue( disabletime );

			remarkCell.setCellValue( MapUtil.getString( rosterValueMap, "REMARK" ) );
		}
		return workbook;
	}

	/**
	 * @param dateList
	 * @return
	 */
	private HSSFWorkbook createExcel2k3Workbook( List<Map<String, Object>> dateList ) {
		HSSFWorkbook workbook = new HSSFWorkbook(); //创建工作簿
		HSSFSheet sheet = workbook.createSheet( "名单值列表" ); //创建工作表

		//创建字体格式     
		HSSFFont font = workbook.createFont();
		font.setBoldweight( (short) 2000 );
		// 设置样式
		HSSFCellStyle cs = workbook.createCellStyle();
		cs.setFont( font );

		HSSFRow titleRow = sheet.createRow( 0 ); //一行
		String titles[] = {
				"名单值", "开始时间", "结束时间", "备注"
		}; //标题
		for( int i = 0; i < titles.length; i++ ) { //产生标题
			sheet.setColumnWidth( i, 3800 );
			HSSFCell cell = titleRow.createCell( i );
			cell.setCellValue( titles[i] );
			cell.setCellStyle( cs );
		}

		for( int i = 0; i < dateList.size(); i++ ) { //产生数据行
			Map<String, Object> rosterValueMap = dateList.get( i );
			HSSFRow dataRow = sheet.createRow( i + 1 );
			HSSFCell valueCell = dataRow.createCell( 0 );
			HSSFCell enabletimeCell = dataRow.createCell( 1 );
			HSSFCell disabletimeCell = dataRow.createCell( 2 );
			HSSFCell remarkCell = dataRow.createCell( 3 );

			//填充数据
			valueCell.setCellValue( MapUtil.getString( rosterValueMap, "ROSTERVALUE" ) );
			String enabletime = MapUtil.getString( rosterValueMap, "ENABLETIME" );
			String disabletime = MapUtil.getString( rosterValueMap, "DISABLETIME" );
			enabletimeCell.setCellValue( enabletime );
			disabletimeCell.setCellValue( disabletime );

			remarkCell.setCellValue( MapUtil.getString( rosterValueMap, "REMARK" ) );
		}
		return workbook;
	}

	/**
	 * 导出方法
	 * @param reqs
	 * @param response
	 */
	@RequestMapping(value = "/exportLog", method = RequestMethod.POST)
	public Model exportLog( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		String rosterdesc = reqs.get( "rosterdesc" );
		List<Map<String, Object>> dateList = nameListService.listValueListById( reqs );
		int export_size = dateList.size();
		request.setAttribute( "rosterdesc", rosterdesc );
		request.setAttribute( "size", String.valueOf( export_size ) ); //导出的数据条数
		return model;
	}
}
