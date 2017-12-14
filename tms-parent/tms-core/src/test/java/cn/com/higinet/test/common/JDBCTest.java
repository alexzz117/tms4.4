package cn.com.higinet.test.common;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.Test;

import cn.com.higinet.tms.common.cache.ProviderNotFoundException;
import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;
import cn.com.higinet.tms35.ShowCache;

public class JDBCTest {
	//	@Test
	public void jdbcTest() {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String strUrl = "jdbc:oracle:thin:@ 10.118.239.10:1521:npdb";
		Connection conn = null;
		PreparedStatement cstmt = null;

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(strUrl, "tms", "tms");
			cstmt = conn.prepareStatement("update tms_run_stat set STAT_VALUE=?,STAT_VALUE2=?,STAT_LOB=? where STAT_PARAM=?");
			DataInputStream di = new DataInputStream(new FileInputStream("C:\\Users\\michael\\Desktop\\20184995861891.log"));
			String data = null;
			StringBuilder sb2 = new StringBuilder();
			while ((data = di.readLine()) != null) {
				sb2.append(data).append("\n");
			}
			di.close();
			cstmt.setObject(1, sb2.toString());
			cstmt.setNull(2, 12);
			cstmt.setNull(3, -1);
			cstmt.setObject(4, "20184995861891-");
			cstmt.addBatch();
			cstmt.addBatch();
			cstmt.addBatch();
			int[] res = cstmt.executeBatch();
			StringBuilder sb = new StringBuilder(String.valueOf(res[0]));
			for (int i = 1, len = res.length; i < len; i++) {
				sb.append(",").append(res[i]);
			}
			System.out.println("flush statment的执行结果:" + sb.toString());
		} catch (SQLException ex2) {
			ex2.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		} finally {
			try {
				if (cstmt != null) {
					cstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex1) {
			}
		}

	}

	@Test
	public void jdbcTest2() {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String strUrl = "jdbc:oracle:thin:@127.0.0.1:1521:tmsol";
		Connection conn = null;
		PreparedStatement cstmt = null;

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(strUrl, "tmsol", "123456");
			cstmt = conn.prepareStatement("update tms_run_stat set STAT_VALUE=?,STAT_VALUE2=?,STAT_LOB=? where STAT_PARAM=?");
			DataInputStream di = new DataInputStream(new FileInputStream("C:\\Users\\michael\\Desktop\\20170801.log"));
			String data = null;
			StringBuilder sb2 = new StringBuilder();
			while ((data = di.readLine()) != null) {
				sb2.append(data).append("\n");
			}
			di.close();
			cstmt.setObject(1, sb2.toString());
			cstmt.setNull(2, 12);
			cstmt.setNull(3, -1);
			cstmt.setObject(4, "20170937851944-");
			cstmt.addBatch();
			int[] res = cstmt.executeBatch();
			StringBuilder sb = new StringBuilder(String.valueOf(res[0]));
			for (int i = 1, len = res.length; i < len; i++) {
				sb.append(",").append(res[i]);
			}
			System.out.println("flush statment的执行结果:" + sb.toString());
		} catch (SQLException ex2) {
			ex2.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		} finally {
			try {
				if (cstmt != null) {
					cstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex1) {
			}
		}

	}

	@Test
	public void preparedStatmentTest() {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String strUrl = "jdbc:oracle:thin:@127.0.0.1:1521:tmsol";
		Connection conn = null;
		PreparedStatement cstmt = null;

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(strUrl, "tmsol", "123456");
			conn.setAutoCommit(false);
			StringBuilder sql = new StringBuilder("update tms_run_trafficdata set text1=?");
			for (int i = 2; i <= 30; i++) {
				sql.append(",text").append(i).append("=?");
			}
			sql.append(" where txncode=?");
			Clock c = ClockUtils.createClock();
			cstmt = conn.prepareStatement(sql.toString());
			for (int x = 0; x < 100; x++) {
				for (int y = 0; y < 2000; y++) {
					for (int i = 1; i <= 30; i++) {
						cstmt.setString(i, String.valueOf(i + x + y));
					}
					cstmt.setString(31, UUID.randomUUID().toString());
					cstmt.addBatch();
				}
				cstmt.executeBatch();
				conn.commit();
			}
			System.out.println(c.countMillis());
		} catch (SQLException ex2) {
			ex2.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		} finally {
			try {
				if (cstmt != null) {
					cstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex1) {
			}
		}

	}

	@Test
	public void statmentTest() {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String strUrl = "jdbc:oracle:thin:@127.0.0.1:1521:tmsol";
		Connection conn = null;
		Statement cstmt = null;

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(strUrl, "tmsol", "123456");
			conn.setAutoCommit(false);
			cstmt = conn.createStatement();
			Clock c = ClockUtils.createClock();
			for (int x = 0; x < 100; x++) {
				for (int y = 0; y < 2000; y++) {
					StringBuilder sql = new StringBuilder("update tms_run_trafficdata set text1='1'");
					for (int i = 2; i <= 30; i++) {
						sql.append(",text").append(i).append("=").append("'").append(i + x + y).append("'");
					}
					sql.append(" where txncode=").append("'").append(UUID.randomUUID().toString()).append("'");
					cstmt.addBatch(sql.toString());
				}
				cstmt.executeBatch();
				conn.commit();
			}
			System.out.println(c.countMillis());
		} catch (SQLException ex2) {
			ex2.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		} finally {
			try {
				if (cstmt != null) {
					cstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex1) {
			}
		}

	}

	@Test
	public void mergeIntoTest() {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String strUrl = "jdbc:oracle:thin:@127.0.0.1:1521:tmsol";
		Connection conn = null;
		PreparedStatement cstmt = null;

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(strUrl, "tmsol", "123456");
			cstmt = conn.prepareStatement("update tms_run_stat set STAT_VALUE=?,STAT_VALUE2=?,STAT_LOB=? where STAT_PARAM=?");
			DataInputStream di = new DataInputStream(new FileInputStream("C:\\Users\\michael\\Desktop\\20170801.log"));
			String data = null;
			StringBuilder sb2 = new StringBuilder();
			while ((data = di.readLine()) != null) {
				sb2.append(data).append("\n");
			}
			di.close();
			cstmt.setObject(1, sb2.toString());
			cstmt.setNull(2, 12);
			cstmt.setNull(3, -1);
			cstmt.setObject(4, "20170937851944-");
			cstmt.addBatch();
			int[] res = cstmt.executeBatch();
			StringBuilder sb = new StringBuilder(String.valueOf(res[0]));
			for (int i = 1, len = res.length; i < len; i++) {
				sb.append(",").append(res[i]);
			}
			System.out.println("flush statment的执行结果:" + sb.toString());
		} catch (SQLException ex2) {
			ex2.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		} finally {
			try {
				if (cstmt != null) {
					cstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex1) {
			}
		}

	}
	
	@Test
	public void cache() throws ProviderNotFoundException, Exception {
		ShowCache.main(new String[] { "stat", "20166999911952--12915" });
	}
}
