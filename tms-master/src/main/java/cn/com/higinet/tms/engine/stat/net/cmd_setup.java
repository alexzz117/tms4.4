package cn.com.higinet.tms.engine.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import cn.com.higinet.tms.engine.stat.serv.stat_serv;

public class cmd_setup implements cmd
{

	public int on_cmd(String[] param, BufferedReader in, BufferedWriter out) throws IOException
	{
		if (param == null || param.length == 0)
		{
			out.write("+param?\n");
			return 0;
		}
		stat_serv.eval_inst().setup(param);
		stat_serv.query_inst().setup(param);
		out.write("+\n");
		return 0;
	}

}
