package cn.com.higinet.tms.engine.core.cache;

import cn.com.higinet.tms.engine.comm.bin_stream;
import cn.com.higinet.tms.engine.core.dao.dao_alert;

public class db_alert 
{
	public db_alert(String txncode,long txn_time)
	{
		this.alertid = dao_alert.next_alert_id();
		this.txncode =  txncode;
		this.createtime = txn_time;
		this.score = 0;
		this.is_main=0;
	}
	
	public db_alert()
	{
	}

	public String txncode;
	public long alertid;
	public long createtime;
	public int trigrulenum;
	public int hitrulenum;
	public int iscorrect;
	public float score;
	public String disposal;
	public int is_main;
	
	public static db_alert load_from(bin_stream bs)
	{
		db_alert d = new db_alert();
		d.txncode = bs.load_string();//
		d.alertid = bs.load_long();
		d.createtime = bs.load_long();
		d.trigrulenum = bs.load_int();
		d.hitrulenum = bs.load_int();// 
		d.iscorrect = bs.load_int();// 
		d.score = bs.load_float();// 
		d.disposal = bs.load_string();//
		d.is_main = bs.load_int();
		
		return d;
	}

	public void save_to(bin_stream bs)
	{
		bs.save(txncode)// 
			.save(alertid)// 
			.save(createtime)// 
			.save(trigrulenum)// 
			.save(hitrulenum)// 
			.save(iscorrect)// 
			.save(score)// 
			.save(disposal)
			.save(is_main);
	}

}
