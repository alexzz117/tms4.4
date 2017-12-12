package cn.com.higinet.tms35.manage.query.common;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrWebException;
import cn.com.higinet.tms35.manage.query.common.model.Column;
import cn.com.higinet.tms35.manage.query.common.model.Cond;
import cn.com.higinet.tms35.manage.query.common.model.Custom;
import cn.com.higinet.tms35.manage.query.common.model.Dbdata;
import cn.com.higinet.tms35.manage.query.common.model.DefValue;
import cn.com.higinet.tms35.manage.query.common.model.Export;
import cn.com.higinet.tms35.manage.query.common.model.Field;
import cn.com.higinet.tms35.manage.query.common.model.Group;
import cn.com.higinet.tms35.manage.query.common.model.Handle;
import cn.com.higinet.tms35.manage.query.common.model.Include;
import cn.com.higinet.tms35.manage.query.common.model.Member;
import cn.com.higinet.tms35.manage.query.common.model.Pagination;
import cn.com.higinet.tms35.manage.query.common.model.Result;
import cn.com.higinet.tms35.manage.query.common.model.Script;
import cn.com.higinet.tms35.manage.query.common.model.Stmt;
import cn.com.higinet.tms35.manage.query.common.model.Table;
import cn.com.higinet.tms35.manage.query.common.model.Toolbar;

/**
 * 自定义查询json工具类
 * @author liining
 *
 */
public class QueryJsonUtil {
	private static Log log = LogFactory.getLog(QueryJsonUtil.class);
	public static final String CUSTOM_QUERY_CUSTOM = "custom";
	public static final String CUSTOM_QUERY_CUSTOM_PAGE_TITLE = "pageTitle";
	public static final String CUSTOM_QUERY_CUSTOM_WRAP_TITLE = "wrapTitle";
	public static final String CUSTOM_QUERY_CUSTOM_WIDTH = "width";
	public static final String CUSTOM_QUERY_CUSTOM_DIRECT = "direct";
	public static final String CUSTOM_QUERY_CUSTOM_EFFECT = "effect";
	public static final String CUSTOM_QUERY_CUSTOM_STMT = "stmt";
	public static final String CUSTOM_QUERY_CUSTOM_STMT_MODE = "mode";
	public static final String CUSTOM_QUERY_CUSTOM_STMT_TYPE = "type";
	public static final String CUSTOM_QUERY_CUSTOM_STMT_DSNAME = "dsName";
	public static final String CUSTOM_QUERY_CUSTOM_STMT_CONTENT = "content";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA = "dbData";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLES = "tables";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_NAME = "name";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_ALIAS = "alias";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_LOADFIELD = "loadField";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_DYNAMIC = "dynamic";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_DYNAMIC_SQL = "sql";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_DYNAMIC_METHOD = "method";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_FIELDS = "fields";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELDS = "fields";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELDS_DYNAMIC = "dynamic";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_FD_NAME = "fdName";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_NAME = "name";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_FD_DESC = "fdDesc";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_TYPE = "type";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_DB_TYPE = "dbType";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_DB_LEN = "dbLen";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_CODE = "code";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_ORDERBY = "orderby";
	public static final String CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_LINK = "link";
	public static final String CUSTOM_QUERY_CUSTOM_COND = "cond";
	public static final String CUSTOM_QUERY_CUSTOM_COND_UNFOLD = "unfold";
	public static final String CUSTOM_QUERY_CUSTOM_COND_LAYOUT = "layout";
	public static final String CUSTOM_QUERY_CUSTOM_COND_CALLCHECK = "callcheck";
	public static final String CUSTOM_QUERY_CUSTOM_COND_EXPR = "expr";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUPS = "groups";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_LABEL = "label";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_SEPARATOR = "separator";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_EXPR = "expr";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_RELATION = "relation";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBERS = "members";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_FD_NAME = "fdName";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_CS_NAME = "csName";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_NAME = "name";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_PROMPT = "prompt";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_TYPE = "type";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_ITEM = "item";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_DEFVALUE = "defValue";
	public static final String CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_DEFVALUE_VALUE = "value";
	//conds数据结构未最终确定
	public static final String CUSTOM_QUERY_CUSTOM_TOOLBAR = "toolbar";
	public static final String CUSTOM_QUERY_CUSTOM_TOOLBAR_ID = "id";
	public static final String CUSTOM_QUERY_CUSTOM_TOOLBAR_ICON = "icon";
	public static final String CUSTOM_QUERY_CUSTOM_TOOLBAR_TEXT = "text";
	public static final String CUSTOM_QUERY_CUSTOM_TOOLBAR_ENABLE = "enable";
	public static final String CUSTOM_QUERY_CUSTOM_TOOLBAR_ACTION = "action";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT = "result";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_INITDATA = "initData";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_DISTINCT = "distinct";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_MCENABLE = "mcEnable";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_EXPORT = "export";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_EXPORT_EXPBTN = "expBtn";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_EXPORT_EXPTYPE = "expType";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_EXPORT_EXPNAME = "expName";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS = "columns";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_FD_NAME = "fdName";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_CS_NAME = "csName";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_NAME = "name";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_TITLE = "title";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_WIDTH = "width";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_SHOW = "show";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_OPEN = "open";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_ORDER = "order";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_CALLBACK = "callback";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION = "pagination";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGEABLE = "pageable";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGEBAR = "pagebar";
	public static final String CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGESIZE = "pagesize";
	public static final String CUSTOM_QUERY_CUSTOM_SCRIPT = "script";
	public static final String CUSTOM_QUERY_CUSTOM_SCRIPT_INCLUDES = "includes";
	public static final String CUSTOM_QUERY_CUSTOM_SCRIPT_INCLUDE_TYPE = "type";
	public static final String CUSTOM_QUERY_CUSTOM_SCRIPT_INCLUDE_SOURCE = "source";
	public static final String CUSTOM_QUERY_CUSTOM_SCRIPT_BINDEVENT = "bindEvent";
	public static final String CUSTOM_QUERY_CUSTOM_HANDLE = "handle";
	public static final String CUSTOM_QUERY_CUSTOM_HANDLE_DB = "db";
	public static final String CUSTOM_QUERY_CUSTOM_HANDLE_SCRIPT = "script";
	public static final String CUSTOM_QUERY_CUSTOM_HANDLE_METHOD = "method";
	
	/**
	 * json转实体
	 * @param json
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Custom json2Entity(String json) {
		Custom custom = null;
		try {
			if (StringUtil.isBlank(json)) return null;
			//String tempjson = json.replaceAll("'", "\\\"");
			String tempjson = json.replaceAll("\n", "");
			tempjson = tempjson.replaceAll("\r", "");
			tempjson = tempjson.replaceAll("	", "");
			Map<String, Object> jsonMap = new ObjectMapper().readValue(tempjson, Map.class);
			custom = buildCustom(MapUtil.getObject(jsonMap, CUSTOM_QUERY_CUSTOM));
		}catch(Exception e){
			log.error("CustomQueryJsonUtil json2Entity error. " + e);
			throw new TmsMgrWebException(e.getMessage());
		}
		return custom;
	}
	
	@SuppressWarnings("unchecked")
	private static Custom buildCustom(Object obj) {
		if(obj == null) {
			throw new TmsMgrWebException("自定义查询JSON中[custom]节点的数据不能为空");
		}
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			Custom custom = new Custom();
			custom.setPageTitle(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_PAGE_TITLE));
			custom.setWrapTitle(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_WRAP_TITLE));
			custom.setWidth(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_WIDTH));
			if(!StringUtil.isBlank(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DIRECT))) custom.setDirect(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DIRECT));
			if(!StringUtil.isBlank(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_EFFECT))) custom.setEffect(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_EFFECT));
			custom.setStmt(buildStmt(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_STMT)));
			if(custom.getDirect() == Custom.Direct.QUERY){//普通查询时，页面效果只能是list，其他不生效
				custom.setEffect(Custom.Effect.LIST.getValue());
			}
			if(((custom.getEffect() == Custom.Effect.COMBTTAB || custom.getEffect() == Custom.Effect.COMBTPAGE) 
					&& custom.getStmt().getType() != Stmt.Type.CTM) || (custom.getStmt().getType() == Stmt.Type.CTM 
						&& (custom.getEffect() != Custom.Effect.COMBTTAB && custom.getEffect() != Custom.Effect.COMBTPAGE))){
				throw new TmsMgrWebException("[custom]->[effect]等于combttab或combtpage时，[custom]->[stmt]->[type]必须等于ctm，反之亦然");
			}
			if(custom.getStmt().getType() != Stmt.Type.CTM){
				custom.setDbData(buildDbdata(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_DBDATA)));
				custom.setCond(buildCond(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_COND)));
				custom.setToolbar(buildToolbar(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_TOOLBAR)));
				custom.setResult(buildResult(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_RESULT)));
			}
			custom.setScript(buildScript(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_SCRIPT)));
			return custom;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Stmt buildStmt(Object obj){
		if(obj == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[stmt]节点的数据不能为空");
		}
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			Stmt stmt = new Stmt();
			stmt.setMode(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_STMT_MODE));
			stmt.setType(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_STMT_TYPE));
			stmt.setDsName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_STMT_DSNAME));
			stmt.setContent(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_STMT_CONTENT));
			return stmt;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[stmt]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Dbdata buildDbdata(Object obj){
		if(obj == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbdata]节点的数据不能为空");
		}
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			Dbdata dbdata = new Dbdata();
			dbdata.setTables(buildDbdataForTables(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_DBDATA_TABLES)));
			dbdata.setFields(buildDbdataForField(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELDS)));
			return dbdata;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbdata]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Set<Table> buildDbdataForTables(Object obj){
		Set<Table> tables = new LinkedHashSet<Table>();
		if(obj == null){
			//throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbdata]->[tables]节点的数据不能为空");
			return tables;
		}
		if(obj instanceof List){
			List<Map<String, Object>> list = (List<Map<String, Object>>) obj; 
			for(Map<String, Object> map : list){
				Table table = new Table();
				table.setName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_NAME));
				table.setAlias(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_ALIAS));
				if (!StringUtil.isBlank(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_LOADFIELD)))
					table.setLoadField(MapUtil.getBoolean(map, CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_LOADFIELD));
				table.setDynamic(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_TABLE_DYNAMIC));
				table.setFields(buildDbdataForField(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELDS)));
				tables.add(table);
			}
			return tables;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbdata]->[tables]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Set<Field> buildDbdataForField(Object obj){
		if(obj == null) return null;
		if(obj instanceof List){
			List<Map<String, Object>> list = (List<Map<String, Object>>) obj; 
			Set<Field> fields = new LinkedHashSet<Field>();
			for(Map<String, Object> map : list){
				String fdName = MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_FD_NAME);
				if (fdName.indexOf("{") != -1 && fdName.indexOf("}") != -1){
					String str = fdName.substring(fdName.indexOf("{") + 1, fdName.indexOf("}"));
					String[] strs = str.split("\\-");
					int begin = Integer.parseInt(strs[0]);
					int end = Integer.parseInt(strs[1]);
					if (end >= begin){
						for(int i = begin; i <= end; i++){
							map.put(CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_FD_NAME, fdName.replaceFirst("\\{" + str + "\\}", String.valueOf(i)));
							fields.add(createField(map));
						}
					}
				}else{
					fields.add(createField(map));
				}
			}
			return fields;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbdata]->[fields]节点的数据结构不正确");
		}
	}
	
	public static Field createField(Map<String, Object> map){
		Field field = new Field();
		field.setFdName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_FD_NAME));
		field.setDynamic(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELDS_DYNAMIC));
		field.setName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_NAME));
		field.setFdDesc(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_FD_DESC));
		if(!StringUtil.isBlank(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_TYPE)))
			field.setType(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_TYPE));
		field.setDbType(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_DB_TYPE));
		field.setDbLen(MapUtil.getInteger(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_DB_LEN));
		field.setCode(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_CODE));
		field.setLink(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_LINK));
		field.setOrderby(MapUtil.getInteger(map, CUSTOM_QUERY_CUSTOM_DBDATA_FIELD_ORDERBY));
		return field;
	}
	
	@SuppressWarnings("unchecked")
	private static Cond buildCond(Object obj){
		if(obj == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]节点的数据不能为空");
		}
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			Cond cond = new Cond();
			cond.setUnfold(MapUtil.getBoolean(map, CUSTOM_QUERY_CUSTOM_COND_UNFOLD));
			if(!StringUtil.isBlank(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_LAYOUT))) {
				cond.setLayout(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_LAYOUT));
			}
			cond.setExpr(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_EXPR));
			cond.setGroups(buildGroups(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_COND_GROUPS)));
			if(!StringUtil.isBlank(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_CALLCHECK))) {
				cond.setCallcheck(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_CALLCHECK));
			}
			return cond;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Set<Group> buildGroups(Object obj){
		if(obj == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[group]节点的数据不能为空");
		}
		if(obj instanceof List){
			List<Map<String, Object>> list = (List<Map<String, Object>>) obj; 
			Set<Group> groups = new LinkedHashSet<Group>();
			for(final Map<String, Object> map : list) {
				Group group = new Group();
				group.setType(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_TYPE));
				group.setFdName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_FD_NAME));
				group.setCsName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_CS_NAME));
				group.setName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_NAME));
				group.setPrompt(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_PROMPT));
				group.setType(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_TYPE));
				group.setItem(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_ITEM));
				group.setDefValue(buildDefValue(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_DEFVALUE)));
				group.setHandle(buildHandle(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_HANDLE)));
				
				group.setLabel(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_LABEL));
				group.setSeparator(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_SEPARATOR));
				group.setExpr(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_EXPR));
				group.setRelation(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_RELATION));
				if(group.getType() == Member.Type.CBT || group.getType() == Member.Type.SCP){
					group.setMembers(buildMembers(group, MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBERS)));
				}
				checkGroup(group);
				groups.add(group);
			}
			return groups;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Set<Member> buildMembers(Group group, Object obj){
		if(obj == null) throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]->[type]节点等于" + Member.Type.SCP.getValue() + "、" + Member.Type.CBT.getValue() + "时，其下的[members]节点的数据不能为空");
		if(obj instanceof List){
			List<Map<String, Object>> list = (List<Map<String, Object>>) obj; 
			Set<Member> members = new LinkedHashSet<Member>();
			for(Map<String, Object> map : list) {
				Member member = new Member();
				member.setType(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_TYPE));
				if(member.getType() == Member.Type.CBT || member.getType() == Member.Type.SCP){
					throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]->[members]->[type]节点设置不能等于" + Member.Type.SCP.getValue() + "、" + Member.Type.CBT.getValue());
				}
				member.setFdName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_FD_NAME));
				member.setCsName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_CS_NAME));
				member.setName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_NAME));
				if (StringUtil.isBlank(member.getFdName())) {
					member.setFdName(group.getFdName());
				}
				if (StringUtil.isBlank(member.getCsName())) {
					member.setCsName(group.getCsName());
				}
				if (StringUtil.isBlank(member.getName())) {
					member.setName(group.getName());
				}
				member.setPrompt(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_PROMPT));
				member.setItem(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_ITEM));
				member.setDefValue(buildDefValue(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_DEFVALUE)));
				member.setHandle(buildHandle(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_HANDLE)));
				members.add(member);
			}
			return members;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Set<Toolbar> buildToolbar(Object obj){
		if(obj == null) return null;
		if(obj instanceof List){
			List<Map<String, Object>> list = (List<Map<String, Object>>) obj; 
			Set<Toolbar> toolbars = new LinkedHashSet<Toolbar>();
			for(Map<String, Object> map : list){
				Toolbar toolbar = new Toolbar();
				toolbar.setId(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_TOOLBAR_ID));
				toolbar.setIcon(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_TOOLBAR_ICON));
				toolbar.setText(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_TOOLBAR_TEXT));
				toolbar.setEnable(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_TOOLBAR_ENABLE));
				toolbar.setAction(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_TOOLBAR_ACTION));
				toolbars.add(toolbar);
			}
			return toolbars;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings({ "unchecked", "serial" })
	private static Result buildResult(Object obj){
		if(obj == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]节点的数据不能为空");
		}
		if(obj instanceof Map){
			final Map<String, Object> map = (Map<String, Object>) obj;
			Result result = new Result();
			result.setInitData(MapUtil.getBoolean(map, CUSTOM_QUERY_CUSTOM_RESULT_INITDATA));
			result.setDistinct(MapUtil.getBoolean(map, CUSTOM_QUERY_CUSTOM_RESULT_DISTINCT));
			result.setMcEnable(MapUtil.getBoolean(map, CUSTOM_QUERY_CUSTOM_RESULT_MCENABLE));
			result.setExport(new Export(){{
				Object obj = MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_RESULT_EXPORT);
				if (obj != null && obj instanceof Map) {
					Map<String, Object> expMap = (Map<String, Object>) obj;
					this.setExpBtn(MapUtil.getString(expMap, CUSTOM_QUERY_CUSTOM_RESULT_EXPORT_EXPBTN));
					this.setExpType(MapUtil.getString(expMap, CUSTOM_QUERY_CUSTOM_RESULT_EXPORT_EXPTYPE));
					this.setExpName(MapUtil.getString(expMap, CUSTOM_QUERY_CUSTOM_RESULT_EXPORT_EXPNAME));
				}
			}});
			result.setColumns(new LinkedHashSet<Column>(){{
				Object obj = MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS);
				if(obj == null){
					throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]->[columns]节点的数据不能为空");
				}
				if(obj instanceof List){
					List<Map<String, Object>> list = (List<Map<String, Object>>) obj; 
					for(Map<String, Object> map : list){
						Column column = new Column();
						column.setFdName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_FD_NAME));
						column.setCsName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_CS_NAME));
						column.setName(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_NAME));
						column.setWidth(MapUtil.getInteger(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_WIDTH));
						if(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_SHOW) != null)
							column.setShow(MapUtil.getBoolean(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_SHOW));
						if(!StringUtil.isBlank(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_OPEN))) column.setOpen(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_OPEN));
						column.setOrder(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_RESULT_COLUMNS_ORDER));
						column.setHandle(buildHandle(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_HANDLE)));
						add(column);
					}
				}else{
					throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]->[columns]节点的数据结构不正确");
				}
			}});
			result.setCallback(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_RESULT_CALLBACK));
			result.setPagination(new Pagination(){{
				Object obj = MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION);
				if(obj != null && obj instanceof Map){
					Map<String, Object> pmap = MapUtil.getMap(map, CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION);
					this.setPageable(MapUtil.getObject(pmap, CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGEABLE) == null ? true : MapUtil.getBoolean(pmap, CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGEABLE));
					this.setPagebar(this.getPageable() == true ? true : (MapUtil.getObject(pmap, CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGEBAR) == null ? true : MapUtil.getBoolean(pmap, CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGEBAR)));
					this.setPagesize(MapUtil.getInteger(pmap, CUSTOM_QUERY_CUSTOM_RESULT_PAGINATION_PAGESIZE));
				}
			}});
			return result;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]节点的数据结构不正确");
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static Handle buildHandle(Object obj){
		if(obj == null) return null;
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			Handle handle = new Handle();
			handle.setDb(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_HANDLE_DB));
			handle.setScript(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_HANDLE_SCRIPT));
			handle.setMethod(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_HANDLE_METHOD));
			return handle;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[handle]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static DefValue buildDefValue(Object obj){
		if(obj == null) return null;
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			DefValue defValue = new DefValue();
			defValue.setValue(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_COND_GROUP_MEMBER_DEFVALUE_VALUE));
			defValue.setHandle(buildHandle(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_HANDLE)));
			return defValue;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]->[defValue]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Script buildScript(Object obj){
		if(obj == null) return null;
		if(obj instanceof Map){
			final Map<String, Object> map = (Map<String, Object>) obj;
			Script script = new Script();
			script.setIncludes(buildInclude(MapUtil.getObject(map, CUSTOM_QUERY_CUSTOM_SCRIPT_INCLUDES)));
			script.setBindEvent(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_SCRIPT_BINDEVENT));
			return script;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[script]节点的数据结构不正确");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Set<Include> buildInclude(Object obj){
		if(obj == null) return null;
		if(obj instanceof List){
			List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
			Set<Include> includes = new LinkedHashSet<Include>();
			for(Map<String, Object> map : list){
				Include include = new Include();
				include.setType(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_SCRIPT_INCLUDE_TYPE));
				include.setSource(MapUtil.getString(map, CUSTOM_QUERY_CUSTOM_SCRIPT_INCLUDE_SOURCE));
				includes.add(include);
			}
			return includes;
		}else{
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[script]->[includes]节点的数据结构不正确");
		}
	}
	
	/**
	 * 校验查询组成员数据
	 * @param member
	 */
	public static void checkGroup(Group group){
		if(group.getType() == Member.Type.CBT || group.getType() == Member.Type.SCP){
			Set<Member> members = group.getMembers();
			if(members == null || members.size() == 0){
				throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]->[members]节点的数据不能为空");
			}
			for(Member member : members){
				if(StringUtil.isBlank(group.getFdName()) && StringUtil.isBlank(member.getFdName())){
					throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]或"
							+"[custom]->[cond]->[groups]->[members]节点中的[fdName]数据项，至少设置其中之一");
				}
			}
		}else{
			if(StringUtil.isBlank(group.getFdName())){
				throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]节点中的[fdName]数据项必须设置");
			}
		}
	}
	
	public static void checkMember(Member member){
		if(member instanceof Group){
			System.out.println("Group .... ");
			Group group = (Group) member;
			System.out.println(group.getLabel());
			group.setLabel("I am Group extends Member");
		}else if(member instanceof Member){
			System.out.println("Member .... ");
		}
	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//QueryJsonUtil.checkMember(new Group());
		Group group = new Group();
		group.setLabel("I am Group");
		checkMember(group);
		System.out.println(group.getLabel());
		String json = "{    \"custom\": {        \"pageTitle\": \"\",        \"wrapTitle\": \"\",        \"direct\": \"entity\",        \"effect\": \"list\",        \"stmt\": {            \"mode\": \"auto\",            \"type\": \"ctm\",            \"cont\": \"\"        },        \"dbdata\": {            \"tables\": [                {                    \"name\": \"T\",                    \"alias\": \"t1\",                    \"dynamic\": \"\",                    \"fields\": [                        {                            \"fdName\": \"t1.abc\"                        }                    ]                }            ],            \"fields\": [                {                    \"fdName\": \"t1.cbd\"                }            ]        },        \"cond\": {            \"unfold\": true,            \"expr\": \"\",            \"initdata\": true        },        \"toolbar\": [            {                \"id\": \"custom_query_find\",                \"icon\": \"find\",                \"text\": \"查询\"            }        ],        \"result\": {            \"distinct\": true,            \"columns\": [                {                    \"fdName\": \"t1.sessionid\",                    \"name\": \"SESSIONID\",                   \"show\": false,                    \"open\": \"pop\",                    \"handle\": {                        \"db\": \"\"                    }                }            ],            \"row_callback\": \"\",            \"callback\": \"\",            \"pagination\": {                \"pageable\": true,                \"pagebar\": true,                \"pagesize\": 15            }        },        \"script\": \"\"    }}";
		Custom custom = QueryJsonUtil.json2Entity(json);
		System.out.println(custom.toString());
	}

}
