package cn.com.higinet.tms.engine.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.dao.dao_ip;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;

/**
 * 功能/模块:省份
 * 
 * @author wujw
 * @version 1.0 Jun 12, 2016 类描述: 修订历史: 日期 作者 参考 描述
 *
 */
public class db_region {

	public static class cache {
		private linear<db_region> list_ = new linear<db_region>();

		public static cache load(data_source ds) {
			cache c = new cache();
			final linear<db_region> list = new linear<db_region>();
			String sql = "SELECT countrycode,regioncode,regionname,regionforshort FROM "
					+ dao_ip.Instance(ds).curr_name("tms_mgr_region");
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try {
				stmt.query(new Object[] {}, new row_fetch() {
					public boolean fetch(ResultSet rs) throws SQLException {
						db_region region = new db_region();
						region.countryCode = rs.getString("countrycode");// 国家编号;
						region.regionCode = rs.getString("regioncode");// 地区编码;
						region.regionName = rs.getString("regionname");// 地区名称;
						region.regionForShort = rs.getString("regionforshort");// 地区名称;
						list.add(region);
						return true;
					}
				});
			} catch (SQLException e) {
				throw new tms_exception("load db_region.cache error.");

			} finally {
				stmt.close();
			}
			c.list_.addAll(list);
			return c;
		}

		public db_region get_map(String regionCode) {
			for (db_region region : list_) {
				if (region.regionCode.equals(regionCode)) {
					return region;
				}
			}
			return null;
		}

	}

	public String countryCode;// 国家编号;
	public String regionCode;// 地区代码;
	public String regionName;// 地区名称;
	public String regionForShort;// 省份缩写;

}