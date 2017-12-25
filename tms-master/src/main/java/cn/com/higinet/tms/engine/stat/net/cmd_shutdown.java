package cn.com.higinet.tms.engine.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class cmd_shutdown  implements cmd
{

	public int on_cmd(String[] param, BufferedReader in, BufferedWriter out) throws IOException 
	{
		net_main.close_main_sock();
		out.write("+\n");
		in.close();
		return 1;
	}

}
