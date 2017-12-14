/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  TMSDBInitializer.java   
 * @Package cn.com.higinet.tms35.core.persist   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-25 11:00:29   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.core.persist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.common.repository.Attribute;
import cn.com.higinet.tms.common.repository.DataRepository;
import cn.com.higinet.tms.common.repository.Metadata;
import cn.com.higinet.tms.common.repository.PersistedDataRepository;
import cn.com.higinet.tms.common.repository.RepositoryInitializer;
import cn.com.higinet.tms.common.repository.converters.ConvertValueException;
import cn.com.higinet.tms.common.repository.converters.Converter;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/**
 * TMS内部自定义初始化器，初始化结构数据仓
 *
 * @ClassName:  TMSDBInitializer
 * @author: 王兴
 * @date:   2017-9-25 11:00:29
 * @since:  v4.3
 */
public class TMSDBInitializer implements RepositoryInitializer {
	private static final Logger log = LoggerFactory.getLogger(TMSDBInitializer.class);

	private data_source ds;

	private static final String GET_ALL_AVAILABLE_TAB = "select tab.TAB_NAME,tab.TAB_DESC from tms_com_tab tab where tab.can_ref =1 and exists (select 1 from TMS_COM_FD fd where fd.tab_name=tab.tab_name)";

	private static final String GET_ALL_TAB_FD = "select * from tms_com_fd where tab_name = ?";

	private static final String CONVERTER = "tms_inner_converter";

	private static final String TMS_FD_TYPE = "TYPE";

	private static final String TMS_DB_TYPE = "DB_TYPE";

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lang.Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		log.info("TMSDBInitializer initialized.");
		ds = new data_source((DataSource) bean.get("tmsDataSource"));
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lang.Initializable#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		log.info("TMSDBInitializer destroyed.");
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.RepositoryInitializer#initRepositories()
	 */
	@Override
	public List<DataRepository> initRepositories() throws Exception {
		final List<DataRepository> repositoies = new ArrayList<DataRepository>();
		final List<Metadata> metas = new ArrayList<Metadata>();
		batch_stmt_jdbc queryTables = new batch_stmt_jdbc(ds, GET_ALL_AVAILABLE_TAB, null, 200);
		batch_stmt_jdbc queryTabFds = new batch_stmt_jdbc(ds, GET_ALL_TAB_FD, new int[] { Types.VARCHAR }, 200);
		try {
			queryTables.query(null, new row_fetch() {
				public boolean fetch(ResultSet rs) throws SQLException {
					Metadata meta = new Metadata();
					meta.setStructureName(rs.getString("TAB_NAME"));
					meta.setDescription(rs.getString("TAB_DESC"));
					metas.add(meta);
					return true;
				}
			});
			for (final Metadata meta : metas) {
				queryTabFds.query(new Object[] { meta.getStructureName() }, new row_fetch() {
					public boolean fetch(ResultSet rs) throws SQLException {
						Attribute attr = new Attribute();
						attr.setPrimary(rs.getInt("IS_KEY") == 1);
						attr.setNullable(rs.getInt("IS_NULL") == 1);
						attr.setName(rs.getString("FD_NAME")); //之前代码都是使用的FD_NAME
						attr.setDescription(rs.getString("NAME"));
						attr.setPersistenceName(rs.getString("FD_NAME"));
						attr.setLimitedLength(rs.getInt("DB_LEN"));
						attr.setDefaultValue(rs.getString("SRC_DEFAULT"));
						attr.setSrcName(rs.getString("SRC_ID"));
						attr.setOuterType(Object.class); //使用类型，目前tms内部都是按Object类型使用，具体类型在converter中定
						attr.setOuterConverter(CONVERTER);
						attr.setPersistedType(Types.VARCHAR);//如果要持久化，则需要设置此类型，参考java.sql.Types
						Map<String, String> addons = new HashMap<String, String>();
						addons.put(TMS_FD_TYPE, rs.getString(TMS_FD_TYPE));
						addons.put(TMS_DB_TYPE, rs.getString(TMS_DB_TYPE));
						attr.setAddons(addons);
						meta.addAttributes(attr);
						return true;
					}
				});
				PersistedDataRepository persist = bean.get(PersistedDataRepository.class); //非单例
				persist.setMetadata(meta);
				persist.setRepositoryID(meta.getStructureName());
				persist.registerConverter(CONVERTER, new TMSOuterConverter());
				repositoies.add(persist);
			}
		} finally {
			queryTables.close();
			queryTabFds.close();
		}
		return repositoies;
	}

	
	
	private static class TMSOuterConverter implements Converter<Object> {

		@Override
		public Object convert(String value, Attribute attr) throws ConvertValueException {
			if (value == null)
				return null;
			String type = attr.getAddons().get(TMS_FD_TYPE);
			String dbtype = attr.getAddons().get(TMS_DB_TYPE);
			switch (op.name2type(type.toUpperCase())) {
				case op.long_:
					if (value.length() == 0)
						return null;
					return new Long(value);
				case op.time_:
				case op.datetime_: {
					if (value.length() == 0)
						return null;

					if (dbtype.equals("VARCHAR2"))
						return new Long(date_tool.parse(value).getTime());

					if (dbtype.equals("NUMBER"))
						return new Long(value);

					return new Long(date_tool.parse(value).getTime());
				}
				case op.double_:
					if (value.length() == 0)
						return null;
					return new Double(value);
			}

			return value;
		}

	}
}
