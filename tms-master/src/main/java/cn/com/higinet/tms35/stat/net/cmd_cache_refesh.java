package cn.com.higinet.tms35.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class cmd_cache_refesh  implements cmd
{

	public int on_cmd(String[] param, BufferedReader in, BufferedWriter out) throws IOException 
	{
		
		out.write("+\n");
		in.close();
		return 1;
	}

}
