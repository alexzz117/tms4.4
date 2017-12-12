package cn.com.higinet.tms35.manage.query.service.process.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.common.DBConstant.TMS_COM_FD;
import cn.com.higinet.tms35.manage.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrWebException;
import cn.com.higinet.tms35.manage.query.common.QueryJsonUtil;
import cn.com.higinet.tms35.manage.query.common.model.Column;
import cn.com.higinet.tms35.manage.query.common.model.Custom;
import cn.com.higinet.tms35.manage.query.common.model.Custom.Direct;
import cn.com.higinet.tms35.manage.query.common.model.Custom.Effect;
import cn.com.higinet.tms35.manage.query.common.model.Field;
import cn.com.higinet.tms35.manage.query.common.model.Group;
import cn.com.higinet.tms35.manage.query.common.model.JsonDataProcess;
import cn.com.higinet.tms35.manage.query.common.model.Member;
import cn.com.higinet.tms35.manage.query.common.model.Stmt;
import cn.com.higinet.tms35.manage.query.common.model.Table;
import cn.com.higinet.tms35.manage.query.service.QueryService;


/**
 * json数据处理
 * @author liining
 *
 */
@Service("jsonDataProcess")
public class JsonDataProcessImpl extends QueryDataProcessCommon {
	//private static Log log = LogFactory.getLog(JsonDataProcessImpl.class);

	@Autowired
	private QueryService queryService;
	
	@Autowired
	private SimpleDao tmsSimpleDao;
	
	private static final String FIELD_TAB_ALIAS = "TAB_ALIAS";

	@SuppressWarnings("unchecked")
	public Object dataProcess(Object... args) {
		Map<String, Object> queryMap = (Map<String, Object>) args[0];//TMS_COM_QUERY表中数据集合
		Map<String, String[]> paramterMap = (Map<String, String[]>) args[1];
		Map<String, Object> conds = reBuildRequestParameterMap(paramterMap);//@RequestParam conds
		Custom custom = QueryJsonUtil.json2Entity(MapUtil.getString(queryMap, DBConstant.TMS_COM_QUERY_QUERY_DATA));
		if(custom == null){
			return null;
		}
		JsonDataProcess process = new JsonDataProcess();
		process.setCustom(custom);
		process.setFieldsMap(new LinkedHashMap<String, Object>());
		process.setBeanName(getRunSqlBeanName(custom));
		if(!(custom.getEffect() == Effect.COMBTTAB || custom.getEffect() == Effect.COMBTPAGE)){
			this.dbDataProcess(process, conds);
		}
		this.jsonDataProcess(process, queryMap);
		return process;
	}
	/**
	 * 处理数据库数据信息
	 * @param custom	json解析后的实体
	 * @param conds		request的参数
	 */
	private void dbDataProcess(JsonDataProcess process, Map<String, Object> conds){
		Set<String> pepeatSet = new HashSet<String>();//重复字段存储
		this.getDbDataForFieldsMap(process, conds, pepeatSet);
		this.updateDbDataForfieldsMap(process, conds, pepeatSet);
	}
	/**
	 * 处理json实体数据信息
	 * @param custom	json解析后的实体
	 * @param queryMap	TMS_COM_QUERY表中数据
	 */
	private void jsonDataProcess(JsonDataProcess process, Map<String, Object> queryMap) {
		Custom custom = process.getCustom();
		String title = MapUtil.getString(queryMap, DBConstant.TMS_COM_QUERY_QUERY_DESC);
		if(StringUtil.isBlank(custom.getPageTitle())){
			custom.setPageTitle(title);
		}
		if(custom.getDirect() == Direct.QUERY && 
					StringUtil.isBlank(custom.getWrapTitle())){
			custom.setWrapTitle(custom.getPageTitle());
		}
		if(!(custom.getEffect() == Effect.COMBTTAB || custom.getEffect() == Effect.COMBTPAGE)){
			this.updateJsonDataForCondsField(custom, process.getFieldsMap());
			this.updateJsonDataForResultColumnsField(custom, process.getFieldsMap());
		}
	}
	/**
	 * 获取执行sql的service名称
	 * @param custom 	json解析后的实体
	 * @return
	 */
	private String getRunSqlBeanName(Custom custom){
		String beanName = null;
		if(custom.getStmt().getType() == Stmt.Type.SP){//存储过程
			beanName = "storedProceProcess";
		}else if(custom.getStmt().getType() == Stmt.Type.SQL){//普通sql
			if(custom.getStmt().getMode() == Stmt.Mode.AUTO){//自动
				beanName = "autoSqlProcess";
			}else if(custom.getStmt().getMode() == Stmt.Mode.MANU){//手工
				beanName = "manuSqlProcess";
			}
		}else if(custom.getStmt().getType() == Stmt.Type.ACT){
			beanName = "actionProcess";
		}
		return beanName;
	}
	
	/**
	 * 获取数据表中所有字段信息 ，无重复字段值时key：字段名，存在重复字段名时key：表别名.字段名
	 * @param process	
	 * @param conds		参数集合
	 * @param pepeatSet 重复字段名的集合
	 */
	@SuppressWarnings("unchecked")
	private void getDbDataForFieldsMap(JsonDataProcess process, Map<String, Object> conds, Set<String> pepeatSet) {
		Custom custom = process.getCustom();
		Map<String, Object> fieldsMap = process.getFieldsMap();
		Set<Table> tables = custom.getDbData().getTables();
		String content = custom.getStmt().getContent();
		for(Table table : tables){
			Map<String, Object> tabMap = queryService.getQueryTableByTabName(table.getName());
			if(tabMap == null) {
				if(!StringUtil.isBlank(table.getDynamic())){
					String regex = "\\:\\s*([a-zA-Z][\\w]*)";
					Pattern pattern = Pattern.compile(regex);
					Matcher m = pattern.matcher(table.getDynamic());
					while(m.find()){
						String paramName = m.group(1);
						if(!conds.containsKey(paramName)){
							throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbData]->[tables]节点下[name="+table.getName()+"]的表中[dynamic]节点中定义的参数名[" + paramName + "]未传入！");
						}
					}
					List<Map<String, Object>> tabList = tmsSimpleDao.queryForList(table.getDynamic(), conds);
					if(tabList != null && tabList.size() > 0) {
						tabMap = tabList.get(0);
						String tabName = MapUtil.getString(tabMap, TMS_COM_TAB.TAB_NAME);
						if(StringUtil.isBlank(tabName)) {
							throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbData]->[tables]节点下[name="+table.getName()+"]的表中[dynamic]节点查询内容为空！");
						}
						regex = "\\s+("+table.getName()+")\\s+";
						pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);//不区分大小写
						m = pattern.matcher(content);
						StringBuffer sb = new StringBuffer();
						while(m.find()) {
							m.appendReplacement(sb, " " + tabName + " ");
						}
						m.appendTail(sb);
						content = sb.toString();
						table.setName(tabName);
						tabMap = queryService.getQueryTableByTabName(tabName);
					}
				}
			}
			if (tabMap == null)
				continue;
			if (table.isLoadField()) {
				String tabType = MapUtil.getString(tabMap, TMS_COM_TAB.TAB_TYPE);
				List<Map<String, Object>> tabFieldList = queryService.getFieldsByTabName(table.getName(), tabType);
				for (Map<String, Object> fieldMap : tabFieldList) {
					String fieldName = MapUtil.getString(fieldMap, DBConstant.TMS_COM_FD_FD_NAME).toUpperCase();//字段转大写
					if(pepeatSet.contains(fieldName)) {
						fieldsMap.put(table.getAlias() + "." + fieldName, fieldMap);
					} else if(fieldsMap.containsKey(fieldName)) {
						pepeatSet.add(fieldName);
						Map<String, Object> pfieldMap = (Map<String, Object>) fieldsMap.get(fieldName);
						String tabAlias = MapUtil.getString(pfieldMap, FIELD_TAB_ALIAS);
						fieldsMap.put(tabAlias + "." + fieldName, pfieldMap);
						fieldsMap.put(table.getAlias() + "." + fieldName, fieldMap);
						fieldsMap.remove(fieldName);
					} else {
						fieldMap.put(FIELD_TAB_ALIAS, table.getAlias());//表别名
						fieldsMap.put(fieldName, fieldMap);
					}
				}
			}
		}
		custom.getStmt().setContent(content);
	}

	/**
	 * 根据json中的配置，变更字段数据信息Map
	 * @param custom 	json顶级节点实体
	 * @param pepeatSet 重复字段名的集合
	 */
	private void updateDbDataForfieldsMap(JsonDataProcess process, Map<String, Object> conds, Set<String> pepeatSet) {
		Custom custom = process.getCustom();
		Map<String, Object> fieldsMap = process.getFieldsMap();
		Set<Table> tables = custom.getDbData().getTables();
		for(Table table : tables){
			if (table.isLoadField()) {
				Set<Field> fields = table.getFields();
				if(fields != null && fields.size() > 0){
					for(Field field : fields) {
						if (!StringUtil.isBlank(field.getDynamic())) {
							List<Map<String, Object>> fieldList = tmsSimpleDao.queryForList(field.getDynamic(), conds);
							for (Map<String, Object> fieldMap : fieldList) {
								Field f = QueryJsonUtil.createField(fieldMap);
								createTableField(table, f, fieldsMap);
							}
						} else {
							createTableField(table, field, fieldsMap);
						}
					}
				}
			}
		}
		Set<Field> fields = custom.getDbData().getFields();
		if(fields != null && fields.size() > 0) {
			for(Field field : fields) {
				if (!StringUtil.isBlank(field.getDynamic())) {
					List<Map<String, Object>> fieldList = tmsSimpleDao.queryForList(field.getDynamic(), conds);
					for (Map<String, Object> fieldMap : fieldList) {
						Field f = QueryJsonUtil.createField(fieldMap);
						createExtendField(f, fieldsMap, pepeatSet);
					}
				} else {
					createExtendField(field, fieldsMap, pepeatSet);
				}
			}
		}
	}
	
	private void createTableField(Table table, Field field, Map<String, Object> fieldsMap) {
		String tabAlias = table.getAlias();
		String fdName = field.getFdName().toUpperCase();
		String fdAllName = tabAlias + "." + fdName;
		if(fieldsMap.containsKey(fdName) || fieldsMap.containsKey(fdAllName)){
			Map<String, Object> fieldMap = fieldsMap.containsKey(fdName) ? MapUtil.getMap(fieldsMap, fdName) : MapUtil.getMap(fieldsMap, fdAllName);
			if(fieldMap.containsKey(FIELD_TAB_ALIAS) 
					&& MapUtil.getString(fieldMap, FIELD_TAB_ALIAS).equals(tabAlias)){
				if(!StringUtil.isBlank(field.getName())) fieldMap.put(DBConstant.TMS_COM_FD_NAME, field.getName());
				if(!StringUtil.isBlank(field.getFdDesc())) fieldMap.put(DBConstant.TMS_COM_FD_FD_DESC, field.getFdDesc());
				if(field.getType() != null) fieldMap.put(DBConstant.TMS_COM_FD_TYPE, field.getType().getValue());
				if(!StringUtil.isBlank(field.getDbType())) fieldMap.put(DBConstant.TMS_COM_FD_DB_TYPE, field.getDbType());
				if(field.getDbLen() > 0) fieldMap.put(DBConstant.TMS_COM_FD_DB_LEN, field.getDbLen());
				if(!StringUtil.isBlank(field.getCode())) fieldMap.put(DBConstant.TMS_COM_FD_CODE, field.getCode());
				if(field.getOrderby() > 0) fieldMap.put(DBConstant.TMS_COM_FD_ORDERBY, field.getOrderby());
				if(!StringUtil.isBlank(field.getLink())) fieldMap.put(DBConstant.TMS_COM_FD_LINK, field.getLink());
			}
		}
	}
	
	private void createExtendField(Field field, Map<String, Object> fieldsMap, Set<String> pepeatSet) {
		if(field.getType().getValue().equalsIgnoreCase("code") && StringUtil.isBlank(field.getCode())) {
			throw new TmsMgrWebException("json配置中，[custom]下所有[fields]->[type]为code时，必须配置其code节点数据！");
		}
		String fdAllName = field.getFdName();
		String fdName = "";
		String tabAlias = "";
		Map<String, Object> fieldMap = null;
		boolean isAlias = false;//是否包含表别名
		if(fdAllName.indexOf(".") != -1) {
			isAlias = true;
			int len = fdAllName.indexOf(".");
			tabAlias = fdAllName.substring(0, len);
			fdName = fdAllName.substring(len+1, fdAllName.length()).toUpperCase();
			fdAllName = tabAlias + "." + fdName;
		} else {
			fdAllName = fdAllName.toUpperCase();
			fdName = fdAllName;
		}
		if(fieldsMap.containsKey(fdAllName)) {//存在在字段集合中，覆盖
			fieldMap = MapUtil.getMap(fieldsMap, fdAllName);
			if(!StringUtil.isBlank(field.getName())) fieldMap.put(DBConstant.TMS_COM_FD_NAME, field.getName());
			if(!StringUtil.isBlank(field.getFdDesc())) fieldMap.put(DBConstant.TMS_COM_FD_FD_DESC, field.getFdDesc());
			if(field.getType() != null) fieldMap.put(DBConstant.TMS_COM_FD_TYPE, field.getType().getValue());
			if(!StringUtil.isBlank(field.getDbType())) fieldMap.put(DBConstant.TMS_COM_FD_DB_TYPE, field.getDbType());
			if(field.getDbLen() > 0) fieldMap.put(DBConstant.TMS_COM_FD_DB_LEN, field.getDbLen());
			if(!StringUtil.isBlank(field.getCode())) fieldMap.put(DBConstant.TMS_COM_FD_CODE, field.getCode());
			if(field.getOrderby() > 0) fieldMap.put(DBConstant.TMS_COM_FD_ORDERBY, field.getOrderby());
			if(!StringUtil.isBlank(field.getLink())) fieldMap.put(DBConstant.TMS_COM_FD_LINK, field.getLink());
		} else {//不存在在字段集合中
			if(isAlias) {//包含表别名
				if(fieldsMap.containsKey(fdName)) {//字段名存在
					fieldMap = MapUtil.getMap(fieldsMap, fdName);
					String _tabAlias = MapUtil.getString(fieldMap, FIELD_TAB_ALIAS);
					if(_tabAlias.equals(tabAlias)) {//同一个字段数据，覆盖
						if(!StringUtil.isBlank(field.getName())) fieldMap.put(DBConstant.TMS_COM_FD_NAME, field.getName());
						if(!StringUtil.isBlank(field.getFdDesc())) fieldMap.put(DBConstant.TMS_COM_FD_FD_DESC, field.getFdDesc());
						if(field.getType() != null) fieldMap.put(DBConstant.TMS_COM_FD_TYPE, field.getType().getValue());
						if(!StringUtil.isBlank(field.getDbType())) fieldMap.put(DBConstant.TMS_COM_FD_DB_TYPE, field.getDbType());
						if(field.getDbLen() > 0) fieldMap.put(DBConstant.TMS_COM_FD_DB_LEN, field.getDbLen());
						if(!StringUtil.isBlank(field.getCode())) fieldMap.put(DBConstant.TMS_COM_FD_CODE, field.getCode());
						if(field.getOrderby() > 0) fieldMap.put(DBConstant.TMS_COM_FD_ORDERBY, field.getOrderby());
						if(!StringUtil.isBlank(field.getLink())) fieldMap.put(DBConstant.TMS_COM_FD_LINK, field.getLink());
					} else {//不是同一个表中的字段数据，存在重复字段
						pepeatSet.add(fdName);
						fieldsMap.put(_tabAlias + "." + fdName, fieldMap);
						fieldsMap.remove(fdName);

						fieldMap = new HashMap<String, Object>();
						fieldMap.put(DBConstant.TMS_COM_FD_FD_NAME, fdName);
						fieldMap.put(DBConstant.TMS_COM_FD_NAME, field.getName());
						fieldMap.put(DBConstant.TMS_COM_FD_FD_DESC, field.getFdDesc());
						fieldMap.put(DBConstant.TMS_COM_FD_TYPE, field.getType().getValue());
						fieldMap.put(DBConstant.TMS_COM_FD_DB_TYPE, field.getDbType());
						if(field.getDbLen() > 0) fieldMap.put(DBConstant.TMS_COM_FD_DB_LEN, field.getDbLen());
						fieldMap.put(DBConstant.TMS_COM_FD_CODE, field.getCode());
						if(field.getOrderby() > 0) fieldMap.put(DBConstant.TMS_COM_FD_ORDERBY, field.getOrderby());
						fieldMap.put(DBConstant.TMS_COM_FD_LINK, field.getLink());
						fieldMap.put(FIELD_TAB_ALIAS, tabAlias);
						fieldsMap.put(fdAllName, fieldMap);
					}
				} else {//字段名不存在
					fieldMap = new HashMap<String, Object>();
					fieldMap.put(DBConstant.TMS_COM_FD_FD_NAME, fdName);
					fieldMap.put(DBConstant.TMS_COM_FD_NAME, field.getName());
					fieldMap.put(DBConstant.TMS_COM_FD_FD_DESC, field.getFdDesc());
					fieldMap.put(DBConstant.TMS_COM_FD_TYPE, field.getType().getValue());
					fieldMap.put(DBConstant.TMS_COM_FD_DB_TYPE, field.getDbType());
					if(field.getDbLen() > 0) fieldMap.put(DBConstant.TMS_COM_FD_DB_LEN, field.getDbLen());
					fieldMap.put(DBConstant.TMS_COM_FD_CODE, field.getCode());
					if(field.getOrderby() > 0) fieldMap.put(DBConstant.TMS_COM_FD_ORDERBY, field.getOrderby());
					fieldMap.put(DBConstant.TMS_COM_FD_LINK, field.getLink());
					fieldMap.put(FIELD_TAB_ALIAS, tabAlias);
					fieldsMap.put(fdAllName, fieldMap);
				}
			} else {//不包含表别名
				if(pepeatSet.contains(fdName)) {
					throw new TmsMgrWebException("json配置中，[fields]的fdName节点配置为[" + fdName + "]的数据存在多张表中，请指定表别名或更改名字！");
				} else {
					fieldMap = new HashMap<String, Object>();
					fieldMap.put(DBConstant.TMS_COM_FD_FD_NAME, fdName);
					fieldMap.put(DBConstant.TMS_COM_FD_NAME, field.getName());
					fieldMap.put(DBConstant.TMS_COM_FD_FD_DESC, field.getFdDesc());
					fieldMap.put(DBConstant.TMS_COM_FD_TYPE, field.getType().getValue());
					fieldMap.put(DBConstant.TMS_COM_FD_DB_TYPE, field.getDbType());
					if(field.getDbLen() > 0) fieldMap.put(DBConstant.TMS_COM_FD_DB_LEN, field.getDbLen());
					fieldMap.put(DBConstant.TMS_COM_FD_CODE, field.getCode());
					if(field.getOrderby() > 0) fieldMap.put(DBConstant.TMS_COM_FD_ORDERBY, field.getOrderby());
					fieldMap.put(DBConstant.TMS_COM_FD_LINK, field.getLink());
					fieldsMap.put(fdAllName, fieldMap);
				}
			}
		}
	}

	/**
	 * 更新json实体中查询条件里字段数据
	 * @param custom
	 * @param fieldsMap
	 */
	private void updateJsonDataForCondsField(Custom custom, Map<String, Object> fieldsMap) {
		Set<Group> groups = custom.getCond().getGroups();
		for(Group group : groups) {
			updateExtendsMemberData(group, fieldsMap);
			if(group.getType() == Member.Type.SCP || group.getType() == Member.Type.CBT){
				Set<Member> members = group.getMembers();
				for(Member member : members){
					if(StringUtil.isBlank(member.getFdName())) member.setFdName(group.getFdName());
					if(StringUtil.isBlank(member.getItem())) member.setItem(group.getItem());
					if(member.getDefValue() == null) member.setDefValue(group.getDefValue());
					if(member.getHandle() == null) member.setHandle(group.getHandle());
					if(StringUtil.isBlank(member.getPrompt())) member.setPrompt(group.getPrompt());
					updateExtendsMemberData(member, fieldsMap);
				}
			}
		}
	}

	/**
	 * 更新json实体中查询结果里字段数据
	 * @param custom
	 * @param fieldsMap
	 */
	@SuppressWarnings("unchecked")
	private void updateJsonDataForResultColumnsField(Custom custom, Map<String, Object> fieldsMap) {
		Set<Column> columns = custom.getResult().getColumns();
		if(columns == null || columns.size() == 0){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]->[columns]节点的数据不能为空");
		}else{
			Set<Column> newColumns = new LinkedHashSet<Column>();
			for(Column column : columns){
				String fdName = column.getFdName();
				int xLen = fdName.indexOf("*");
				if(xLen != -1){//*号处理
					int tLen = fdName.indexOf(".");
					if(tLen != -1){//有表别名前缀
						String alias = fdName.substring(0, tLen);
						if(checkTabAlias(custom.getDbData().getTables(), alias)){//表别名校验
							String csName = column.getCsName();
							String before = csName.indexOf(".")!=-1 ? csName.substring(0, csName.indexOf(".")) : "";
							for(Map.Entry<String, Object> fieldMapEntry : fieldsMap.entrySet()){
								String _fdName = fieldMapEntry.getKey();
								Map<String, Object> fieldMap = (Map<String, Object>) fieldMapEntry.getValue();
								String tabAlias = MapUtil.getString(fieldMap, FIELD_TAB_ALIAS);
								if(tabAlias.equals(alias)){
									Column c = new Column();
									c.setFdName(_fdName);
									c.setCsName((StringUtil.isBlank(before) ? "" : before + ".") + MapUtil.getString(fieldMap, DBConstant.TMS_COM_FD_FD_NAME));
									c.setName(MapUtil.getString(fieldMap, DBConstant.TMS_COM_FD_NAME));
									if (column.getOrder() != null) c.setOrder(column.getOrder().getValue());
									c.setOpen(column.getOpen().getValue());
									c.setShow(column.isShow());
									c.setWidth(column.getWidth());
									c.setHandle(column.getHandle());
									for(Column _column : columns){
										if(!_column.equals(column)){//不等于当前的
											updateColumnData(_column, fieldsMap);
											if(c.getFdName().equals(_column.getFdName())){//单独配置的覆盖*中的
												c = _column;
											}
										}
									}
									newColumns.add(c);
								}
							}
							if(newColumns.size() == 0){
								throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]->[columns]节点下[fdName="+fdName+"]的数据字段有误，请确认在[custom]->[dbData]->[tables]节点中是否配置了[alias="+alias+"]的表，并且此表真实存在！");
							}
						}else{
							throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]->[columns]节点下[fdName="+fdName+"]的字段数据有误，表别名在[custom]->[dbData]->[tables]->[alias]中不存在");
						}
					}else{//全部，无表别名
						String csName = column.getCsName();
						String before = csName.indexOf(".")!=-1 ? csName.substring(0, csName.indexOf(".")) : "";
						for(Map.Entry<String, Object> fieldMapEntry : fieldsMap.entrySet()){
							String _fdName = fieldMapEntry.getKey();
							Map<String, Object> fieldMap = (Map<String, Object>) fieldMapEntry.getValue();
							Column c = new Column();
							c.setFdName(_fdName);
							c.setCsName((StringUtil.isBlank(before) ? "" : before + ".") + MapUtil.getString(fieldMap, DBConstant.TMS_COM_FD_FD_NAME));
							c.setName(MapUtil.getString(fieldMap, DBConstant.TMS_COM_FD_NAME));
							if (column.getOrder() != null) c.setOrder(column.getOrder().getValue());
							c.setOpen(column.getOpen().getValue());
							c.setShow(column.isShow());
							c.setWidth(column.getWidth());
							c.setHandle(column.getHandle());
							for(Column _column : columns){
								if(!_column.equals(column)){//不等于当前的
									updateColumnData(_column, fieldsMap);
									if(c.getFdName().equals(_column.getFdName())){//单独配置的覆盖*中的
										c = _column;
									}
								}
							}
							newColumns.add(c);
						}
					}
				}else{
					updateColumnData(column, fieldsMap);
					newColumns.add(column);
				}
			}
			custom.getResult().setColumns(newColumns);
		}
	}
	
	/**
	 * 更新继承自Member的数据项
	 * @param member
	 * @param fieldsMap
	 */
	@SuppressWarnings("unchecked")
	private void updateExtendsMemberData(Member member, Map<String, Object> fieldsMap){
		String fdName = member.getFdName();
		Object[] field = getFieldInfoForFdName(fdName, fieldsMap);
		if(field == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]"+(member instanceof Group ? "":"->[members]")+"节点下[fdName=" + fdName + "]的字段名数据不存在");
		}else{
			String fmtFdName = String.valueOf(field[0]);
			Map<String, Object> fieldMap = (Map<String, Object>) field[1]; 
			member.setFdName(fmtFdName);
			if(StringUtil.isBlank(member.getCsName())){
				member.setCsName(MapUtil.getString(fieldMap, TMS_COM_FD.FD_NAME));
			}
			if(StringUtil.isBlank(member.getName())){
				member.setName(fdName.split("\\.")[fdName.split("\\.").length-1]);
			}
			if(StringUtil.isBlank(member.getPrompt())){
				member.setPrompt(MapUtil.getString(fieldMap, TMS_COM_FD.NAME));
			}
			if(member.getDefValue() != null){
				member.setDefValue(defaultValueProcess(member.getDefValue()));
			}
			if(member instanceof Group){
				Group group = (Group) member;
				if(StringUtil.isBlank(group.getLabel())){
					group.setLabel(MapUtil.getString(fieldMap, TMS_COM_FD.NAME));
				}
			}
		}
	}
	
	/**
	 * 更新Column的数据项
	 * @param column
	 */
	@SuppressWarnings("unchecked")
	private void updateColumnData(Column column, Map<String, Object> fieldsMap){
		String fdName = column.getFdName();
		Object[] field = getFieldInfoForFdName(fdName, fieldsMap);
		if(field == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]->[columns]节点下[fdName=" + fdName + "]的字段名数据不存在");
		}else{
			String fmtFdName = String.valueOf(field[0]);
			Map<String, Object> fieldMap = (Map<String, Object>) field[1]; 
			column.setFdName(fmtFdName);
			if(StringUtil.isBlank(column.getCsName())){
				column.setCsName(fmtFdName);
			}
			if(StringUtil.isBlank(column.getName())){
				column.setName(MapUtil.getString(fieldMap, TMS_COM_FD.NAME));
			}
		}
	}
	
	/**
	 * 通过fdName获取相关数据信息
	 * @param fdName
	 * @param fieldsMap
	 * @return [fdName, fieldMap]
	 */
	private Object[] getFieldInfoForFdName(String fdName, Map<String, Object> fieldsMap){
		Object[] objs = new Object[2];
		Map<String, Object> fieldMap = MapUtil.getMap(fieldsMap, fdName);
		String fmtFdName = null;
		if(MapUtil.isEmpty(fieldMap)){
			int len = fdName.indexOf(".");
			if(len != -1){//含有别名
				String alias = fdName.substring(0, len);
				String name = fdName.substring(len+1, fdName.length()).toUpperCase();
				fmtFdName = alias + "." + name;
				fieldMap = MapUtil.getMap(fieldsMap, fmtFdName);
				if(MapUtil.isEmpty(fieldMap)){
					return getFieldInfoForFdName(name, fieldsMap);
				}
			}else{
				fmtFdName = fdName.toUpperCase();
				fieldMap = MapUtil.getMap(fieldsMap, fmtFdName);
			}
		}else{
			fmtFdName = fdName;
		}
		
		if(MapUtil.isEmpty(fieldMap)){
			return null;
		}else{
			objs[0] = fmtFdName;
			objs[1] = fieldMap;
			return objs;
		}
	}
	
	/**
	 * 校验表别名是否存在
	 * @param tables
	 * @param tabAlias
	 * @return
	 */
	private boolean checkTabAlias(Set<Table> tables, String tabAlias){
		String[] tableAlias = new String[tables.size()];
		int t = 0;
		for(Table table : tables){
			tableAlias[t] = table.getAlias();
			t++;
		}
		if(Arrays.binarySearch(tableAlias, tabAlias) == -1){
			return false;
		}else{
			return true;
		}
	}
	
	private Map<String, Object> reBuildRequestParameterMap(Map<String, String[]> ParameterMap){
		Map<String, Object> conds = new HashMap<String, Object>();
		for(Map.Entry<String, String[]> pMapEntry : ParameterMap.entrySet()){
			String key = pMapEntry.getKey();
			String[] values = pMapEntry.getValue();
			if(values.length == 1){
				conds.put(key, values[0]);
			}else if(values.length > 1){
				for(int i=0;i<values.length;i++){
					conds.put(key+"_"+i, values[i]);
				}
			}
		}
		return conds;
	}
}
