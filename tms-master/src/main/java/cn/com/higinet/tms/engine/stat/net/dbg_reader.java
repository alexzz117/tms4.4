package cn.com.higinet.tms.engine.stat.net;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class dbg_reader extends java.io.BufferedReader
{
	static Logger log = LoggerFactory.getLogger(dbg_reader.class);
	public dbg_reader(Reader in)
	{
		super(in);
	}

	@Override
	public String readLine() throws IOException
	{
		String tmp=super.readLine();
		System.out.println("<--"+tmp);
		return tmp;
	}
}
