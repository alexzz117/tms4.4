package cn.com.higinet.tms.engine.core.dao.stmt;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cn.com.higinet.tms.engine.comm.tms_exception;

public class StatDataSource implements DataSource
{
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException
	{
		DataSource ds=make("oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:@127.0.0.1:21521/orcl","lxl","lxl");
		Connection conn=ds.getConnection();
		
		conn.close();
	}

	public static DataSource make(String driver, String url, String username, String password)
	{
		try
		{
			Class.forName(driver);
			return new StatDataSource(driver, url, username, password);

		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	String driver, url;
	String user, password;
	int login_timeout;

	public StatDataSource(String driver, String url, String username, String password)
	{
		login_timeout=15;
		this.driver = driver;
		this.url = url;
		this.user = username;
		this.password = password;
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(url, user, password);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException
	{
		return DriverManager.getConnection(url, username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException
	{
		if(true) throw new tms_exception("该接口未实现！");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLoginTimeout() throws SQLException
	{
		return this.login_timeout;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{
		if(true) throw new tms_exception("该接口未实现！");
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{
		login_timeout=seconds;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		if(true) throw new tms_exception("该接口未实现！");
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		if(true) throw new tms_exception("该接口未实现！");
		return null;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		if(true) throw new tms_exception("该接口未实现！");
		return null;
	}

}
