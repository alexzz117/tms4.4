package cn.com.higinet.partition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PartitionSession {

	public static void main(String[] args) throws ParseException {
		// System.out.println("------:" + System.currentTimeMillis());
		int year = 2019;

		for (int i = 1; i < 13; i++) {
			getSql("TMSDEV.TMS_RUN_SESSION", year, i);
		}
	}

	private static String getSql(String table, int year, int month) {
		String partiDate = year + "" + month + "01";
		String yearMonth = year + "" + (month - 1) + "";
		if (month <=10) {
			yearMonth = year + "0" + (month-1) + "";
		}

		if (month <10) {
			partiDate = year + "0" + month + "01";
		}
		
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");

		System.out.println("/* " + partiDate + "*/");

		try {
			Date d = f.parse(partiDate);
			long milliseconds = d.getTime();
			String sql = "ALTER TABLE " + table + " add partition TMS" + yearMonth + " values less than ('" + milliseconds + "') tablespace TMS_SESSION pctfree 10 initrans 1 maxtrans 255 storage (initial 64K  next 8K  minextents 1 maxextents unlimited);";
			System.out.println(sql);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return partiDate;
	}

}
