package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.dao_ip;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

/**
 * 功能/模块:国家
 * 
 * @author wujw
 * @version 1.0 Jun 12, 2016 类描述: 修订历史: 日期 作者 参考 描述
 *
 */
public class db_country {

	public static class cache {
		private linear<db_country> list_ = new linear<db_country>();

		public static cache load(data_source ds) {
			cache c = new cache();
			final linear<db_country> list = new linear<db_country>();
			String sql = "SELECT countrycode,countryname FROM "
					+ dao_ip.Instance(ds).curr_name("tms_mgr_country");
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try {
				stmt.query(new Object[] {}, new row_fetch() {
					public boolean fetch(ResultSet rs) throws SQLException {
						db_country region = new db_country();
						region.countryCode = rs.getString("countrycode");// 国家编号;
						region.countryName = rs.getString("countryname");// 地区名称;
						list.add(region);
						return true;
					}
				});
			} catch (SQLException e) {
				throw new tms_exception("load db_country.cache error.");

			} finally {
				stmt.close();
			}
			c.list_.addAll(list);
			return c;
		}

		public db_country get_map(String countryCode) {
			for (db_country country : list_) {
				if (country.countryCode.equals(countryCode)) {
					return country;
				}
			}
			return null;
		}
	}

	public String countryCode;// 国家编号;
	public String countryName;// 国家名称;

}