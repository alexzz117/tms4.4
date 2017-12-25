package cn.com.higinet.tms.engine.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import cn.com.higinet.tms.engine.comm.tm_tool;

public class cmd_ping implements cmd
{

	public int on_cmd(String[] param, BufferedReader in, BufferedWriter out) throws IOException
	{
		out.write('+');
		out.write(Long.toString(tm_tool.lctm_ms()));
		out.write('\n');
		return 0;
	}
}
