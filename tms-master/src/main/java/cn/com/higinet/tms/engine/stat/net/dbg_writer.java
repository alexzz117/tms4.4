package cn.com.higinet.tms.engine.stat.net;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class dbg_writer extends java.io.BufferedWriter
{
	static Logger log = LoggerFactory.getLogger(dbg_writer.class);

	public dbg_writer(Writer out)
	{
		super(out);
	}

	@Override
	public void flush() throws IOException
	{
		if (log.isDebugEnabled())
			log.debug("--><flush>\n");
		super.flush();
	}

	@Override
	public void write(int c) throws IOException
	{
		System.out.print((char) c);
		super.write(c);
	}

	@Override
	public void write(String str) throws IOException
	{
		System.out.print("-->" + str);
		super.write(str);
	}

}
