package cn.com.higinet.tms35.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public interface cmd
{
	public int on_cmd(String[] param,BufferedReader in, BufferedWriter out) throws IOException;
}
