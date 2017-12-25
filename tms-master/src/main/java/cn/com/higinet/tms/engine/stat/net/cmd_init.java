package cn.com.higinet.tms.engine.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.stat.serv.stat_serv;

public class cmd_init implements cmd
{
	boolean init_ok = false;

	public int on_cmd(String[] param, BufferedReader in, BufferedWriter out) throws IOException
	{
		String serv_count, hash_id, db_url, db_drv, tab_name, user_name, pass_word;
		serv_count = in.readLine();
		hash_id = in.readLine();
		db_url = in.readLine();
		db_drv = in.readLine();
		tab_name = in.readLine();
		user_name = in.readLine();
		pass_word = in.readLine();

		synchronized (this)
		{
			if (serv_count.equals(tmsapp.get_config("_tms.stat.serv.count", ""))
					&& hash_id.equals(tmsapp.get_config("_tms.stat.serv.hashid", "")))
			{
				out.write("+1\n");
				return 0;
			}

			init_ok = true;
		}

		tmsapp.set_config("_tms.stat.serv.count", serv_count);
		tmsapp.set_config("_tms.stat.serv.hashid", hash_id);

		stat_serv.reset_cache(tab_name, new data_source(db_drv, db_url, user_name, pass_word));

		stat_serv.eval_inst().setup(new String[]
		{ "reset_db", db_drv, db_url, user_name, pass_word, tab_name });
		stat_serv.query_inst().setup(new String[]
		{ "reset_db", db_drv, db_url, user_name, pass_word, tab_name });
		out.write("+0\n");

		return 0;
	}
}
