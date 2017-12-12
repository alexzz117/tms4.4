package cn.com.higinet.tms35.manage.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.query.model.Filed;
import cn.com.higinet.tms35.manage.query.model.QueryCond;
import cn.com.higinet.tms35.manage.query.model.QueryCond.Cond;
import cn.com.higinet.tms35.manage.query.model.QueryJsonVO;
import cn.com.higinet.tms35.manage.query.model.QueryResult;
import cn.com.higinet.tms35.manage.query.model.QueryResult.Column;
import cn.com.higinet.tms35.manage.query.model.QueryScript;
import cn.com.higinet.tms35.manage.query.model.Statement;
import cn.com.higinet.tms35.manage.query.model.Table;

/**
 * 自定义查询JSON工具类
 * @author liining
 *
 */
public class QueryJsonUtil {
	private static Log log = LogFactory.getLog(QueryJsonUtil.class);
	public static final String QUERY_CUSTOM = "custom";
	public static final String QUERY_CUSTOM_QUERY_TYPE = "query_type";
	public static final String QUERY_CUSTOM_QUERY_TITLE = "query_title";
	public static final String QUERY_CUSTOM_BOX_TITLE = "box_title";
	public static final String QUERY_CUSTOM_STATEMENT = "statement";
	public static final String QUERY_CUSTOM_STATEMENT_TYPE = "type";
	public static final String QUERY_CUSTOM_STATEMENT_CONTENT = "content";
	public static final String QUERY_CUSTOM_SPELL_TYPE = "spell_type";
	public static final String QUERY_CUSTOM_TABLES = "tables";
	public static final String QUERY_CUSTOM_TABLES_NAME = "name";
	public static final String QUERY_CUSTOM_TABLES_ALIAS = "alias";
	public static final String QUERY_CUSTOM_TABLES_DYNAMIC = "dynamic";
	public static final String QUERY_CUSTOM_FILEDS = "fileds";
	public static final String QUERY_CUSTOM_FILEDS_FD_NAME = "fd_name";
	public static final String QUERY_CUSTOM_FILEDS_TITLE = "title";
	public static final String QUERY_CUSTOM_FILEDS_TMS_TYPE = "tms_type";
	public static final String QUERY_CUSTOM_FILEDS_DB_TYPE = "db_type";
	public static final String QUERY_CUSTOM_FILEDS_DB_LEN = "db_len";
	public static final String QUERY_CUSTOM_FILEDS_CODE = "code";
	public static final String QUERY_CUSTOM_FILEDS_LINK = "link";
	public static final String QUERY_CUSTOM_QUERY_SCRIPT = "query_script";
	public static final String QUERY_CUSTOM_QUERY_SCRIPT_INIT_CALL = "init_call";
	public static final String QUERY_CUSTOM_QUERY_SCRIPT_READY_SCRIPT = "ready_script";
	public static final String QUERY_CUSTOM_QUERY_SCRIPT_CALL_SCRIPT = "call_script";
	public static final String QUERY_CUSTOM_QUERY_COND = "query_cond";
	public static final String QUERY_CUSTOM_QUERY_COND_LINE_CONDS = "line_conds";
	public static final String QUERY_CUSTOM_QUERY_COND_QFROM_DISPLAY = "qfrom_display";
	public static final String QUERY_CUSTOM_QUERY_COND_EXPRESSION = "expression";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS = "conds";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_NAME = "name";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_TITLE = "title";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_JOIN = "join";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_EXPRESSION = "expression";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS = "subconds";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_ID = "id";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_NAME = "name";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_FD_NAME = "fd_name";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_REC_FILED = "rec_filed";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_TITLE = "title";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_OPER = "oper";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_DEFAULT_VALUE = "default_value";
	public static final String QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_SCRIPT_EXCITE = "script_excite";
	public static final String QUERY_CUSTOM_QUERY_RESULT = "query_result";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS = "columns";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_REC_FILED = "rec_filed";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_FD_NAME = "fd_name";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_WIDTH = "width";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HIDE = "hide";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_ORDERBY = "orderby";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_OPEN_LINK = "open_link";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE = "handle";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE_DB = "db";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE_SCRIPT = "script";
	public static final String QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE_METHOD = "method";
	public static final String QUERY_CUSTOM_QUERY_RESULT_DS_PARAMS = "ds_params";
	public static final String QUERY_CUSTOM_QUERY_RESULT_DS_CALLBACK = "ds_callback";
	public static final String QUERY_CUSTOM_QUERY_RESULT_DISTINCT_DISPLAY = "distinct_display";
	public static final String QUERY_CUSTOM_QUERY_RESULT_DISPLAY_TYPE = "display_type";
	public static final String QUERY_CUSTOM_QUERY_RESULT_PAGING= "paging";
	public static final String QUERY_CUSTOM_QUERY_RESULT_PAGE_SIZE = "page_size";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static QueryJsonVO jsonToVo(String json) throws Exception {
		QueryJsonVO jsonVO = new QueryJsonVO();
		try {
			if (StringUtil.isBlank(json)) return null;
			//String tempjson = json.replaceAll("'", "\\\"");
			String tempjson = json.replaceAll("\n", "");
			tempjson = tempjson.replaceAll("\r", "");
			tempjson = tempjson.replaceAll("	", "");
			Map jsonMap = new ObjectMapper().readValue(tempjson, Map.class);
			// 设置函数参数值
			Map map = MapUtil.getMap(jsonMap, QUERY_CUSTOM);
			String queryType = MapUtil.getString(map, QUERY_CUSTOM_QUERY_TYPE);
			if(StringUtil.isBlank(queryType) || (!StringUtil.isBlank(queryType)
					&& Integer.parseInt(queryType) < 1 &&  Integer.parseInt(queryType) > 2)) {
				throw new Exception("[query_type]节点，未定义或定义错误[1为普通查询，2为实体查询]");
			}
			jsonVO.setQuery_type(Integer.parseInt(queryType));
			jsonVO.setQuery_title(MapUtil.getString(map, QUERY_CUSTOM_QUERY_TITLE));
			jsonVO.setBox_title(MapUtil.getString(map, QUERY_CUSTOM_BOX_TITLE));
			if(MapUtil.getObject(map, QUERY_CUSTOM_STATEMENT) instanceof Map) {
				jsonVO.setStatement(makeStatement(map));
			}
			if(jsonVO.getStatement().getType() < 3) {
				String spellType = MapUtil.getString(map, QUERY_CUSTOM_SPELL_TYPE);
				if(StringUtil.isBlank(spellType) || (!StringUtil.isBlank(spellType)
						&& Integer.parseInt(spellType) < 1 && Integer.parseInt(spellType) > 2)) {
					throw new Exception("[spell_type]节点，未定义或定义错误[1为自动组织查询语句，2为手工编写查询语句]");
				}
				jsonVO.setSpell_type(Integer.parseInt(spellType));

				if(MapUtil.getObject(map, QUERY_CUSTOM_TABLES) instanceof List) {
					List<Map<String, Object>> tables = (List<Map<String, Object>>) MapUtil.getObject(map, QUERY_CUSTOM_TABLES);
					jsonVO.setTables(makeTables(tables));
				}

				if(MapUtil.getObject(map, QUERY_CUSTOM_FILEDS) instanceof List) {
					List<Map<String, Object>> fileds = (List<Map<String, Object>>) MapUtil.getObject(map, QUERY_CUSTOM_FILEDS);
					jsonVO.setFileds(makeFileds(fileds));
				}

				if(MapUtil.getObject(map, QUERY_CUSTOM_QUERY_SCRIPT) instanceof Map) {
					Map<String, Object> queryScriptMap = (Map<String, Object>) MapUtil.getObject(map, QUERY_CUSTOM_QUERY_SCRIPT);
					jsonVO.setQuery_script(makeQueryScript(queryScriptMap));
				}

				if(MapUtil.getObject(map, QUERY_CUSTOM_QUERY_COND) instanceof Map) {
					Map<String, Object> queryConds = (Map<String, Object>) MapUtil.getObject(map, QUERY_CUSTOM_QUERY_COND);
					jsonVO.setQuery_cond(makeQueryCond(queryConds));
				}

				if(MapUtil.getObject(map, QUERY_CUSTOM_QUERY_RESULT) instanceof Map) {
					Map<String, Object> queryResult = (Map<String, Object>) MapUtil.getObject(map, QUERY_CUSTOM_QUERY_RESULT);
					jsonVO.setQuery_result(makeQueryResult(queryResult));
				}
			}
		} catch (Exception e) {
			log.error("JsonQueryUtil jsonToVo", e);
			throw new Exception(e);
		}
		return jsonVO;
	}

	private static Statement makeStatement(Map<String, Object> map) throws Exception{
		Map<String, Object> statementMap = MapUtil.getMap(map, QUERY_CUSTOM_STATEMENT);
		String type = MapUtil.getString(statementMap, QUERY_CUSTOM_STATEMENT_TYPE);
		if(StringUtil.isBlank(type) || (!StringUtil.isBlank(type)
				&& Integer.parseInt(type) < 1 && Integer.parseInt(type) > 3)) {
			throw new Exception("[statement]-[type]节点，为定义或定义错误[1为sql语句，2为存储过程，3为多查询组合的标签页实体，4为多组合查询的上下同页结构实体]");
		}
		if(MapUtil.getInteger(map, QUERY_CUSTOM_QUERY_TYPE) == 1 && Integer.parseInt(type) > 2) {
			throw new Exception("[query_type]节点和[statement]-[type]节点配置值要对应");
		}
		String content = MapUtil.getString(statementMap, QUERY_CUSTOM_STATEMENT_CONTENT);
		if(StringUtil.isBlank(content)) {
			throw new Exception("[statement]-[content]节点不能为空");
		}
		Statement statement = new Statement();
		statement.setType(Integer.parseInt(type));
		statement.setContent(content);
		return statement;
	}

	private static List<Table> makeTables(List<Map<String, Object>> tables) throws Exception {
		List<Table> list = new LinkedList<Table>();
		for(Map<String, Object> tmap : tables) {
			String tabName = MapUtil.getString(tmap, QUERY_CUSTOM_TABLES_NAME);
			String tabAlias = MapUtil.getString(tmap, QUERY_CUSTOM_TABLES_ALIAS);
			String tabDynamic = MapUtil.getString(tmap, QUERY_CUSTOM_TABLES_DYNAMIC);
			if(StringUtil.isBlank(tabName) || StringUtil.isBlank(tabAlias)) {
				throw new Exception("[tables]节点下的[name]和[alias]数据项必须全部填写");
			}
			Table table = new Table();
			table.setName(tabName);
			table.setAlias(tabAlias);
			table.setDynamic(tabDynamic);
			list.add(table);
		}
		return list;
	}

	private static List<Filed> makeFileds(List<Map<String, Object>> fileds) throws Exception {
		List<Filed> list = new LinkedList<Filed>();
		for(Map<String, Object> fmap : fileds) {
			String fdName = MapUtil.getString(fmap, QUERY_CUSTOM_FILEDS_FD_NAME);
			String tmsType = MapUtil.getString(fmap, QUERY_CUSTOM_FILEDS_TMS_TYPE);
			String code = MapUtil.getString(fmap, QUERY_CUSTOM_FILEDS_CODE);
			if(StringUtil.isBlank(fdName)) {
				throw new Exception("[fileds]节点下的[fd_name]数据项必须填写");
			}
			if(tmsType.equalsIgnoreCase("code") && StringUtil.isBlank(code)) {
				throw new Exception("当[fileds]节点下[tms_type]为code类型时，[code]数据项必须填写");
			}
			Filed filed = new Filed();
			filed.setFd_name(fdName);
			filed.setTitle(MapUtil.getString(fmap, QUERY_CUSTOM_FILEDS_TITLE));
			filed.setTms_type(tmsType);
			filed.setDb_type(MapUtil.getString(fmap, QUERY_CUSTOM_FILEDS_DB_TYPE));
			String dbLen = MapUtil.getString(fmap, QUERY_CUSTOM_FILEDS_DB_LEN);
			filed.setDb_len(StringUtil.isBlank(dbLen) ? 0 : Integer.parseInt(dbLen));
			filed.setCode(code);
			filed.setLink(MapUtil.getString(fmap, QUERY_CUSTOM_FILEDS_LINK));
			list.add(filed);
		}
		return list;
	}

	private static QueryScript makeQueryScript(Map<String, Object> queryScriptMap) throws Exception {
		QueryScript queryScript = new QueryScript();
		queryScript.setInit_call(MapUtil.getString(queryScriptMap, QUERY_CUSTOM_QUERY_SCRIPT_INIT_CALL));
		queryScript.setReady_script(MapUtil.getString(queryScriptMap, QUERY_CUSTOM_QUERY_SCRIPT_READY_SCRIPT));
		queryScript.setCall_script(MapUtil.getString(queryScriptMap, QUERY_CUSTOM_QUERY_SCRIPT_CALL_SCRIPT));
		return queryScript;
	}

	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	private static QueryCond makeQueryCond(Map<String, Object> queryCondMap) throws Exception {
		final QueryCond queryConds = new QueryCond();
		String lineConds = MapUtil.getString(queryCondMap, QUERY_CUSTOM_QUERY_COND_LINE_CONDS);
		queryConds.setLine_conds(StringUtil.isBlank(lineConds) ? 4 : Integer.parseInt(lineConds));
		String qfromDisplay = MapUtil.getString(queryCondMap, QUERY_CUSTOM_QUERY_COND_QFROM_DISPLAY);
		queryConds.setQfrom_display(StringUtil.isBlank(qfromDisplay) ? false : Boolean.valueOf(qfromDisplay));
		queryConds.setExpression(MapUtil.getString(queryCondMap, QUERY_CUSTOM_QUERY_COND_EXPRESSION));
		if(MapUtil.getObject(queryCondMap, QUERY_CUSTOM_QUERY_COND_CONDS) instanceof List) {
			final List<Map<String, Object>> condsList = (List<Map<String, Object>>) MapUtil.getObject(queryCondMap, QUERY_CUSTOM_QUERY_COND_CONDS);
			queryConds.setConds(new LinkedList<Cond>() {{
				for(Map<String, Object> condMap : condsList) {
					final QueryCond.Cond cond = queryConds.new Cond();
					final String name = MapUtil.getString(condMap, QUERY_CUSTOM_QUERY_COND_CONDS_NAME);
					cond.setName(name);
					String title = MapUtil.getString(condMap, QUERY_CUSTOM_QUERY_COND_CONDS_TITLE);
					if(StringUtil.isBlank(title)) {
						throw new Exception("[query_cond]-[conds]-[title]节点数据不能为空");
					}
					if(MapUtil.getObject(condMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS) instanceof List) {
						final List<Map<String, Object>> subCondsList = (List<Map<String, Object>>) MapUtil.getObject(condMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS);
						if(subCondsList == null || subCondsList.size() < 1) {
							throw new Exception("[query_cond]-[conds]-[subconds]节点数据不能为空");
						}
						cond.setSubconds(new LinkedList() {{
							for(Map<String, Object> subCondMap : subCondsList) {
								String fdName = MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_FD_NAME);
								String recFiled = MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_REC_FILED);
								if(StringUtil.isBlank(recFiled)) {
									throw new Exception("[query_cond]-[conds]-[subconds]节点下[rec_filed]的数据不能都为空");
								}
								String oper = MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_OPER);
								QueryCond.Cond.SubCond subCond = cond.new SubCond();
								subCond.setId(MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_ID));
								subCond.setName(StringUtil.isBlank(name) ? MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_NAME) : name);
								subCond.setRec_filed(recFiled);
								subCond.setFd_name(fdName);
								subCond.setTitle(MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_TITLE));
								subCond.setOper(oper.replaceAll("\\s+", " "));
								subCond.setDefault_value(MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_DEFAULT_VALUE));
								subCond.setScript_excite(MapUtil.getString(subCondMap, QUERY_CUSTOM_QUERY_COND_CONDS_SUBCONDS_SCRIPT_EXCITE));
								add(subCond);
							}
						}});
					} else {
						throw new Exception("[query_cond]-[conds]-[subconds]节点数据格式错误");
					}
					cond.setTitle(title);
					cond.setJoin(MapUtil.getString(condMap, QUERY_CUSTOM_QUERY_COND_CONDS_JOIN));
					cond.setExpression(MapUtil.getString(condMap, QUERY_CUSTOM_QUERY_COND_CONDS_EXPRESSION));
					add(cond);
				}
			}});
		} else {
			throw new Exception("[query_cond]-[conds]节点数据格式错误");
		}
		return queryConds;
	}

	@SuppressWarnings({ "serial", "unchecked" })
	private static QueryResult makeQueryResult(Map<String, Object> queryResultMap) throws Exception {
		final QueryResult queryResult = new QueryResult();
		if(MapUtil.getObject(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS) instanceof List) {
			final List<Map<String, Object>> columns = (List<Map<String, Object>>) MapUtil.getObject(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS);
			queryResult.setColumns(new LinkedList<Column>() {{
				for(Map<String, Object> columnMap : columns) {
					QueryResult.Column column = queryResult.new Column();
					String recFiled = MapUtil.getString(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_REC_FILED);
					String fdName = MapUtil.getString(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_FD_NAME);
					if(StringUtil.isBlank(recFiled)) {
						throw new Exception("[query_result]-[columns]节点下[rec_filed]的数据不能都为空");
					}
					String width = MapUtil.getString(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_WIDTH);
					column.setRec_filed(recFiled);
					column.setFd_name(fdName);
					column.setWidth(StringUtil.isBlank(width) ? 0 : Integer.parseInt(width));
					String hide = MapUtil.getString(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HIDE);
					column.setHide(StringUtil.isBlank(hide) ? false : (Boolean.valueOf(hide)));
					column.setOrderby(MapUtil.getString(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_ORDERBY));
					String openLink = MapUtil.getString(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_OPEN_LINK);
					column.setOpen_link(StringUtil.isBlank(openLink) ? 0 : Integer.parseInt(openLink));
					if(MapUtil.getObject(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE) instanceof Map) {
						Map<String, Object> handleMap = MapUtil.getMap(columnMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE);
						QueryResult.Column.Handle handle = column.new Handle();
						handle.setDb(MapUtil.getString(handleMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE_DB));
						handle.setScript(MapUtil.getString(handleMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE_SCRIPT));
						handle.setMethod(MapUtil.getString(handleMap, QUERY_CUSTOM_QUERY_RESULT_COLUMNS_HANDLE_METHOD));
						column.setHandle(handle);
					}
					add(column);
				}
			}});
		} else {
			throw new Exception("[query_result]-[columns]节点数据格式错误");
		}
		queryResult.setDs_params(MapUtil.getString(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_DS_PARAMS));
		queryResult.setDs_callback(MapUtil.getString(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_DS_CALLBACK));
		String distinctDisplay = MapUtil.getString(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_DISTINCT_DISPLAY);
		String pageing = MapUtil.getString(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_PAGING);
		String pageSize = MapUtil.getString(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_PAGE_SIZE);
		queryResult.setDistinct_display(StringUtil.isBlank(distinctDisplay) ? false : Boolean.valueOf(distinctDisplay));
		queryResult.setDisplay_type(MapUtil.getString(queryResultMap, QUERY_CUSTOM_QUERY_RESULT_DISPLAY_TYPE));
		queryResult.setPaging(StringUtil.isBlank(pageing) ? false : Boolean.valueOf(pageing));
		queryResult.setPage_size(StringUtil.isBlank(pageSize) ? 10 : Integer.parseInt(pageSize));
		return queryResult;
	}


	public static void main(String[] args) {
		String json = "{\"conds\":{\"sql\":\"\",\"type\":\"\",\"where\":\"A&B\",\"factors\":{\"A\":{\"name\":\"TXNTYPE\",\"oper\":\">=\",\"define_value\":\"2\",\"default_value\":\"\"},\"B\":{\"name\":\"tab.TXNSTATUS\",\"oper\":\">=,<=\",\"define_value\":\"\",\"default_value\":\"2,6\"}},\"define_value_display\":\"true\",\"result\":\"TRAFFICID,SESSIONID,TXNID,TXNCODE,TXNTIME,TXNSTATUS,TXNTYPE,DEVICEID,IPADDR,COUNTRYCODE,CITYCODE,ISPCODE\",\"from\":\"T01tab1\",\"tabs\":{\"tab\":\"T01\"},\"fileds\":{\"TRAFFICID\":{\"filed_name\":\"tab.TRAFFICID\"},\"SESSIONID\":{\"filed_name\":\"tab.SESSIONID\"},\"TXNID\":{\"filed_name\":\"tab.TXNID\"},\"TXNCODE\":{\"filed_name\":\"tab.TXNCODE\"},\"TXNTIME\":{\"filed_name\":\"tab.TXNTIME\"},\"TXNSTATUS\":{\"filed_name\":\"tab.TXNSTATUS\",\"title_name\":\"\",\"tms_type\":\"\",\"db_type\":\"\",\"code\":\"\",\"link\":\"\"},\"TXNTYPE\":{\"filed_name\":\"tab.TXNTYPE\"},\"DEVICEID\":{\"filed_name\":\"tab.DEVICEID\"},\"IPADDR\":{\"filed_name\":\"tab.IPADDR\"},\"COUNTRYCODE\":{\"filed_name\":\"tab.COUNTRYCODE\"},\"CITYCODE\":{\"filed_name\":\"tab.CITYCODE\"},\"ISPCODE\":{\"filed_name\":\"tab.ISPCODE\"}},\"distinct_display\":\"true\",\"is_page\":\"true\",\"page_size\":\"10\"}}";//"{'conds':{'sql':'','type':'','where':'A&B','factors':{'A':{'name':'TXNTYPE','oper':'>=','define_value':'2','default_value':''},'B':{'name':'tab.TXNSTATUS','oper':'>=,<=','define_value':'','default_value':'2,6'}},'define_value_display':'true','result':'TRAFFICID,SESSIONID,TXNID,TXNCODE,TXNTIME,TXNSTATUS,TXNTYPE,DEVICEID,IPADDR,COUNTRYCODE,CITYCODE,ISPCODE','from':'T01tab1','tabs':{'tab':'T01'},'fileds':{'TRAFFICID':{'filed_name':'tab.TRAFFICID'},'SESSIONID':{'filed_name':'tab.SESSIONID'},'TXNID':{'filed_name':'tab.TXNID'},'TXNCODE':{'filed_name':'tab.TXNCODE'},'TXNTIME':{'filed_name':'tab.TXNTIME'},'TXNSTATUS':{'filed_name':'tab.TXNSTATUS','title_name':'','tms_type':'','db_type':'','code':'','link':''},'TXNTYPE':{'filed_name':'tab.TXNTYPE'},'DEVICEID':{'filed_name':'tab.DEVICEID'},'IPADDR':{'filed_name':'tab.IPADDR'},'COUNTRYCODE':{'filed_name':'tab.COUNTRYCODE'},'CITYCODE':{'filed_name':'tab.CITYCODE'},'ISPCODE':{'filed_name':'tab.ISPCODE'}},'distinct_display':'true','is_page':'true','page_size':'10'}}";//"{\"conds\":{\"sql\":\"\",\"type\":\"\",\"where\":\"A&B\",\"factors\":{\"A\":{\"name\":\"TXNTYPE\",\"oper\":\">=\",\"define_value\":\"2\",\"default_value\":\"\"},\"B\":{\"name\":\"tab.TXNSTATUS\",\"oper\":\">=,<=\",\"define_value\":\"\",\"default_value\":\"2,6\"}},\"define_value_display\":\"true\",\"result\":\"TRAFFICID,SESSIONID,TXNID,TXNCODE,TXNTIME,TXNSTATUS,TXNTYPE,DEVICEID,IPADDR,COUNTRYCODE,CITYCODE,ISPCODE\",\"from\":\"T01tab1\",\"tabs\":{\"tab\":\"T01\"},\"fileds\":{\"TRAFFICID\":{\"filed_name\":\"tab.TRAFFICID\"},\"SESSIONID\":{\"filed_name\":\"tab.SESSIONID\"},\"TXNID\":{\"filed_name\":\"tab.TXNID\"},\"TXNCODE\":{\"filed_name\":\"tab.TXNCODE\"},\"TXNTIME\":{\"filed_name\":\"tab.TXNTIME\"},\"TXNSTATUS\":{\"filed_name\":\"tab.TXNSTATUS\",\"title_name\":\"\",\"tms_type\":\"\",\"db_type\":\"\",\"db_len\":\"\",\"code\":\"\",\"link\":\"\"},\"TXNTYPE\":{\"filed_name\":\"tab.TXNTYPE\"},\"DEVICEID\":{\"filed_name\":\"tab.DEVICEID\"},\"IPADDR\":{\"filed_name\":\"tab.IPADDR\"},\"COUNTRYCODE\":{\"filed_name\":\"tab.COUNTRYCODE\"},\"CITYCODE\":{\"filed_name\":\"tab.CITYCODE\"},\"ISPCODE\":{\"filed_name\":\"tab.ISPCODE\"},},\"distinct_display\":\"true\",\"is_page\":\"true\",\"page_size\":\"10\"}}";
		json = "{\"custom\":{\"artspell\":{\"type\":\"\",\"statement\":\"\"},\"autocompose\":{\"related\":\"T tab\"},\"common\":{\"tables\":[{\"tab_name\":\"T\",\"alias\":\"tab\"}],\"fileds\":[{\"fd_name\":\"\",\"title\":\"\",\"tms_type\":\"\",\"db_type\":\"\",\"db_len\":\"\",\"code\":\"\",\"link\":\"\"}],\"show_query\":\"true\",\"query\":{\"expression\":\"1*2*3\",\"default_value_display\":\"true\",\"conds\":[{\"title\":\"\",\"join\":\"--\",\"expression\":\"\",\"subconds\":[{\"id\":\"\",\"name\":\"\",\"fd_name\":\"\",\"title\":\"\",\"oper\":\"\",\"default_value\":\"\"},{\"id\":\"\",\"name\":\"\",\"fd_name\":\"\",\"title\":\"\",\"oper\":\"\",\"default_value\":\"\"}]}]},\"result\":{\"columns\":[{\"fd_name\":\"\",\"width\":\"\",\"render\":\"\"}],\"display_type\":\"list\",\"distinct_display\":\"true\",\"paging\":\"true\",\"page_size\":\"10\"}}}}";
		json = "{\"custom\":{\"query_type\":\"1\",\"query_title\":\"\",\"statement\":{\"type\":\"1\",\"content\":\"fromTt\"},\"spell_type\":\"1\",\"tables\":[{\"name\":\"T\",\"alias\":\"t\"}],\"fileds\":[{\"fd_name\":\"Location\",\"title\":\"\u5730\u7406\u4fe1\u606f\",\"tms_type\":\"string\",\"db_type\":\"varchar\",\"db_len\":\"\",\"code\":\"\",\"link\":\"\"}],\"query_script\":{\"ready_script\":\"\",\"call_script\":\"\"},\"query_conds\":{\"td_line\":\"\",\"qfrom_display\":\"true\",\"expression\":\"\",\"conds\":[{\"title\":\"\u4f1a\u8bddID\",\"join\":\"\",\"expression\":\"\",\"subconds\":[{\"id\":\"session_id\",\"name\":\"session_id\",\"fd_name\":\"SESSIONID\",\"title\":\"\",\"oper\":\"=\",\"default_value\":\"\",\"script_excite\":\"\"}]},{\"title\":\"\u5ba2\u6237\u53f7\",\"subconds\":[{\"id\":\"userId\",\"name\":\"userId\",\"fd_name\":\"USERID\",\"oper\":\"=\"}]},{\"title\":\"\u65f6\u95f4\u8303\u56f4\",\"join\":\"--\",\"subconds\":[{\"id\":\"startTime\",\"name\":\"startTime\",\"fd_name\":\"TXNTIME\",\"title\":\"\u5f00\u59cb\u65f6\u95f4\",\"oper\":\">=\"},{\"id\":\"endTime\",\"name\":\"endTime\",\"fd_name\":\"TXNTIME\",\"title\":\"\u7ed3\u675f\u65f6\u95f4\",\"oper\":\"<=\"}]}]},\"query_result\":{\"init_script\":\"\",\"columns\":[{\"fd_name\":\"TRAFFICID\",\"width\":\"50\"},{\"fd_name\":\"SESSIONID\",\"width\":\"50\"},{\"fd_name\":\"TXNID\",\"width\":\"50\"},{\"fd_name\":\"TXNCODE\",\"width\":\"50\"},{\"fd_name\":\"TXNTYPE\",\"width\":\"50\"},{\"fd_name\":\"USERID\",\"width\":\"50\"},{\"fd_name\":\"Location\",\"width\":\"50\",\"handle\":{\"db\":\"COUNTRYCODE||'-'||REGIONCODE||'-'||CITYCODE\",\"script\":\"\",\"method\":\"\"}}],\"ds_params\":\"\",\"ds_callback\":\"\",\"distinct_display\":\"true\",\"paging\":\"true\",\"page_size\":\"10\"}}}";
		//System.out.println(json);
		try {
			QueryJsonVO vo = QueryJsonUtil.jsonToVo(json);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}