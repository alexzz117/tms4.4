package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.comm.date_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_env;

public class db_monitor_stat_txn extends db_monitor_stat {
	
	db_monitor_stat_txn() {}
	
	public db_monitor_stat_txn(run_env re) {
		super(re);
		txnid = re.get_txntype();
		channelid = re.get_chan_chancode();
		boolean is_modelrisk = "1".equals(String.valueOf(re.get_fd_value(re.field_cache().INDEX_ISMODELRISK)));
		String disposal = (String) re.get_fd_value(re.field_cache().INDEX_DISPOSAL);
		String txn_status = re.get_txn_status();
		if (re.is_confirm()) {
			txn_number = 0;
			if (StaticParameters.TXN_STATUS_SUCCESS.equals(txn_status)
					|| StaticParameters.TXN_STATUS_FAIL.equals(txn_status)) {
				if (!str_tool.is_empty(disposal) && !"PS01".equals(disposal)) {
					if (is_modelrisk) {
					    //模型有风险
					    if ("1".equals(re.get_auth_status())) {
					        mr_ap_number = 1;//模型有风险认证通过
					    } else if("0".equals(re.get_auth_status())) {
					        mr_apnot_number = 1;//模型有风险认证不通过
					    }
					}
				}
			}
		} else {
			txn_number = 1;
			disposal_num = 1;
			if (str_tool.is_empty(disposal) || "PS01".equals(disposal)) {
			    //无风险
			    if (is_modelrisk) {
			        mrnot_number = 1;//模型无风险数
                }
			} else {
			    //有风险
			    if (is_modelrisk) {
			        mr_number = 1;//模型有风险数
			    }
			}
		}
		txn_runtime_max = (Long) re.get_fd_value(re.field_cache().INDEX_FINISHTIME)
				- (Long) re.get_fd_value(re.field_cache().INDEX_CREATETIME);
		txn_runtime_min = txn_runtime_max;
		txn_runtime_avg = txn_runtime_max;
		db_tab model = re.get_txn_model();
		model_id = model == null ? "-1" : model.tab_name;
		statdate = Integer.parseInt(date_tool.get_date_time("yyyyMMdd", re.get_txn_time()));
		this.disposal = disposal;
	}
	
	static public class cache
	{
		private Map<String, Map<String, db_monitor_stat_txn>> stat_txn_cache = new HashMap<String, Map<String,db_monitor_stat_txn>>();
		
		public static cache load(data_source ds, String threadname)
		{
			cache c = new cache();
			return c;
		}
		
		public Map<String, Map<String, db_monitor_stat_txn>> get_stat_txn_cache()
		{
			return stat_txn_cache;
		}
	}
	
	@Override
    public void assign_stat(ResultSet rs) throws SQLException {
        super.assign_stat(rs);
        this.channelid = rs.getString("CHANNELID");
        this.txnid = rs.getString("TXNID");
        this.txn_number = rs.getLong("TXN_NUMBER");
        this.txn_runtime_max = rs.getLong("TXN_RUNTIME_MAX");
        this.txn_runtime_min = rs.getLong("TXN_RUNTIME_MIN");
        this.txn_runtime_avg = rs.getFloat("TXN_RUNTIME_AVG");
        this.mr_number = rs.getLong("MR_NUMBER");
        this.mrnot_number = rs.getLong("MRNOT_NUMBER");
        this.mr_ap_number = rs.getLong("MR_AP_NUMBER");
        this.mr_apnot_number = rs.getLong("MR_APNOT_NUMBER");
        this.model_id = rs.getString("MODEL_ID");
        this.statdate = rs.getInt("STATDATE");
        this.disposal = rs.getString("DISPOSAL");
        this.disposal_num = rs.getLong("DISPOSAL_NUM");
    }
	
	@Override
	public db_monitor_stat update_stat(db_monitor_stat stat) {
		db_monitor_stat_txn stat_txn = (db_monitor_stat_txn) stat;
		super.update_stat(stat_txn);
		if (txn_runtime_max < stat_txn.txn_runtime_max) {
			txn_runtime_max = stat_txn.txn_runtime_max;
		}
		if (txn_runtime_min > stat_txn.txn_runtime_min) {
			txn_runtime_min = stat_txn.txn_runtime_min;
		}
		if (txn_number + stat_txn.txn_number <= 0) {
			txn_runtime_avg = 0;
		} else {
			txn_runtime_avg = ((txn_runtime_avg * txn_number) + 
					(stat_txn.txn_runtime_avg * stat_txn.txn_number)) / 
						(txn_number += stat_txn.txn_number);
		}
		mr_number += stat_txn.mr_number;
		mrnot_number += stat_txn.mrnot_number;
		mr_ap_number += stat_txn.mr_ap_number;
		mr_apnot_number += stat_txn.mr_apnot_number;
		disposal_num += stat_txn.disposal_num;
		return this;
	}
	
	@Override
	public String name() {
		return super.name() + "_" + txnid + "_" + channelid + "_" + model_id + "_" + disposal;
	}

	public String txnid;//交易编码
	public String channelid;// 渠道代码
	public long txn_number;// 交易数
	public long txn_runtime_max;// 交易最大执行时间
	public long txn_runtime_min;// 交易最小执行时间
	public float txn_runtime_avg;// 交易平均执行时间
	public long mr_number;// 模型评估有风险数
	public long mrnot_number;// 模型评估无风险数
	public long mr_ap_number;// 模型有风险认证通过数
	public long mr_apnot_number;// 模型有风险认证不通过数
	public long mr_ap_cr_number;// 模型有风险认证通过人工确认有风险数
	public long mr_ap_crnot_number;// 模型有风险认证通过人工确认无风险数
	public long mr_apnot_cr_number;// 模型有风险认证不通过人工确认有风险数
	public long mr_apnot_crnot_number;// 模型有风险认证不通过人工确认无风险数
	public long mrnot_cr_number;// 模型评估无风险人工确认有风险数
	public long mrnot_crnot_number;// 模型评估无风险人工确认无风险数
	public String model_id;// 模型编码
	public int statdate;// 统计日期
	public String disposal;// 处置编码
	public long disposal_num;// 处置总数
}