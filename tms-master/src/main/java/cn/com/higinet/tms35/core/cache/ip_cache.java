package cn.com.higinet.tms35.core.cache;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.bin_stream;
import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.core.dao.dao_ip;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class ip_cache {
	static Logger log = LoggerFactory.getLogger(ip_cache.class);

	public static final int LOCATE_TYPE_IP = 0;// ip地址
	public static final int LOCATE_TYPE_CARD = 1;// 身份证号
	public static final int LOCATE_TYPE_MOBILE = 2;// 手机号

	public static class loc {
		static final int PAGE_SIZE = 512;
		byte[] g_loc = null; // 位置信息

		int cvt(double f) {
			return (int) Math.ceil(f * 1000000);
		}

		double cvt(int i) {
			double f = i;
			return f / 1000000;
		}

		long parse_loc(String txt, bin_stream bs) {
			String c[] = txt.split(",");
			long city = Long.parseLong(cvs_str(c[0]));// 国家编码(4位) + 城市编码(6位)
			bs.save(city);// city
			bs.save(cvs_str(c[1]));// country
			bs.save(cvs_str(c[2]));// region
			bs.save(cvs_str(c[3])); //cityName
			bs.save(cvt(Double.parseDouble(c[5])));// latitude
			bs.save(cvt(Double.parseDouble(c[6])));// longitude

			return city;
		}

		loc load_from_txt(InputStream stm) throws IOException {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1 << 20);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stm));
			bin_stream bs = new bin_stream(PAGE_SIZE);
			bin_stream bs2 = new bin_stream(PAGE_SIZE);

			int p_size, i = 0;
			String s;
			for (i = 0; null != (s = reader.readLine()); i++) {
				p_size = bs.size();
				bs2.ppos(0);
				long city = parse_loc(s, bs2);
				if (bs.size() == 0) {
					bs.save((byte) 0);// 当前页存储的数据个数
					bs.save(city);// 当前页起始的城市编码
				}

				bs.save(bs2.to_bytes());

				if (bs.size() <= PAGE_SIZE)
					continue;

				bs.ppos(p_size);
				write_page(bo, bs, i, PAGE_SIZE);

				bs.ppos(0);
				bs.save((byte) 0);// 当前页存储的数据个数
				bs.save(city);// 当前页起始的城市编码
				bs.save(bs2.to_bytes());
				i = 0;
				parse_loc(s, bs2);
			}

			if (bs.size() > 0)
				write_page(bo, bs, i, PAGE_SIZE);

			this.g_loc = bo.toByteArray();
			return this;
		}

		public entry find(long code) {
			int s = 0, e = g_loc.length / PAGE_SIZE, m = e / 2;
			long page_loc;
			while (s != m) {
				page_loc = get_page_loc(m);
				if (page_loc < code)
					s = m;
				else if (page_loc > code)
					e = m;
				else
					break;

				m = (s + e) / 2;
			}

			return find_in_page(code, m);
		}

		final long get_page_loc(int pos) {
			return get_long(g_loc, pos * PAGE_SIZE + 1);
		}

		final String cvs_str(String s) {
			if (s.length() > 0)
				return s.substring(1, s.length() - 1);

			return "";
		}

		final entry find_in_page(long city, int page_index) {
			bin_stream bs = new bin_stream(g_loc);
			bs.gpos(page_index * PAGE_SIZE);
			int entry_count = bs.c_int(bs.load_byte());// 页面存储个数
			long page_city = bs.load_long();// 页面起始城市编码
			long loc_city = -1;
			if (city < page_city)
				return null;

			for (int i = 0; i < entry_count; i++) {
				int loc_len = bs.off_load();
				int gpos = bs.gpos();
				loc_city = bs.load_long();
				if (city > loc_city) {
					bs.gpos(gpos + loc_len);
				} else if (city == loc_city) {
					break;
				} else {
					return null;
				}
			}
			entry ret = new entry();
			ret.city = city;
			ret.country = bs.load_string();
			ret.region = bs.load_string();
			ret.cityname = bs.load_string();
			ret.latitude = cvt(bs.load_int());
			ret.longitude = cvt(bs.load_int());
			return ret;
		}

		public static class entry {
			public long city;
			// 加上城市名称 wujw 2016/06/08
			public String country, region, cityname;
			public double latitude, longitude;
		}

		public void save_to_bin(OutputStream bos) throws IOException {
			write_int(bos, this.g_loc.length);
			bos.write(this.g_loc);
		}

		public void load_from_bin(InputStream ios) throws IOException {
			int len = read_int(ios);
			byte[] b = new byte[len];
			ios.read(b);
			this.g_loc = b;
		}
	}

	public static class ip {
		static final int PAGE_SIZE = 32;
		byte[] g_ip = null; // IP地址

		public static class entry {

			public entry(long ip1, long ip2, long code) {
				ip_start = ip1;
				ip_end = ip2;
				this.code = code;
			}

			public String toString() {
				return "IP1=" + ip_start + ",IP2=" + ip_end + ",CODE=" + code;
			}

			public long ip_start, ip_end;
			public long code;
		}

		private long parse_ip(String s, bin_stream bs, long ip) {
			String c[] = s.split(",");
			// c[0] ipfrom c[1] ipto c[2] code

			long ip1 = Long.parseLong(c[0].substring(1, c[0].length() - 1));
			long ip2 = Long.parseLong(c[1].substring(1, c[1].length() - 1));
			long code = Long.parseLong(c[2].substring(1, c[2].length() - 1));

			if (ip == 0) {
				bs.save((byte) 0);
				bs.save((int) ip1);
			} else {
				bs.off_save((int) (0XFFFFFFFFL & (ip1 - ip)));
			}

			bs.off_save((int) (0XFFFFFFFFL & (ip2 - ip1)));
			bs.save(code);
			return ip2;
		}

		private ip load_ip_from_txt(InputStream stm) throws IOException {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1 << 20);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stm));
			bin_stream bs = new bin_stream(PAGE_SIZE);
			long ip = 0, cip = 0;
			int p_size, i = 0;
			String s;
			for (i = 0; null != (s = reader.readLine()); i++) {
				p_size = bs.size();
				cip = parse_ip(s, bs, ip);
				if (bs.size() <= PAGE_SIZE) {
					ip = cip;
					continue;
				}

				bs.ppos(p_size);
				write_page(bo, bs, i, PAGE_SIZE);

				bs.ppos(0);
				i = 0;
				ip = parse_ip(s, bs, 0);
			}

			if (bs.size() > 0)
				write_page(bo, bs, i, PAGE_SIZE);

			g_ip = bo.toByteArray();

			return this;
		}

		final long get_page_ip(int pos) {
			return 0xFFFFFFFFL & get_int(g_ip, pos * PAGE_SIZE + 1);
		}

		final entry find_in_page(long ip, int page_index) {
			bin_stream bs = new bin_stream(g_ip);
			bs.gpos(page_index * PAGE_SIZE);
			int entry_count = bs.c_int(bs.load_byte());
			long ip1 = bs.c_long(bs.load_int());
			long ip2 = ip1 + bs.off_load();
			long code = bs.load_long();
			for (int i = 0; i < entry_count; i++) {
				if (ip1 <= ip && ip <= ip2)
					return new entry(ip1, ip2, code);

				if (ip < ip1)
					return null;

				ip1 = ip2 + bs.off_load();
				ip2 = ip1 + bs.off_load();
				code = bs.load_long();
			}

			return null;
		}

		public entry find(long ip) {
			int s = 0, e = g_ip.length / PAGE_SIZE, m = e / 2;
			long page_ip;
			while (s != m) {
				page_ip = get_page_ip(m);
				if (page_ip < ip)
					s = m;
				else if (page_ip > ip)
					e = m;
				else
					break;

				m = (s + e) / 2;
			}

			return find_in_page(ip, m);
		}

		public entry find(String ip) {
			String[] c = ip.split("\\.");
			if (c == null || c.length != 4) {
				/*
				 * add lining 2015-07-05 begin POC测试修改, 格式错误返回null
				 */
				// throw new tms_exception("IP地址格式错误：’", ip, "'");
				return null;
				/* add lining 2015-07-05 end */
			}
			long lip = (int) (Integer.parseInt(c[0]) & 0xFF);
			lip = (lip << 8) | (int) (Integer.parseInt(c[1]) & 0xFF);
			lip = (lip << 8) | (int) (Integer.parseInt(c[2]) & 0xFF);
			lip = (lip << 8) | (int) (Integer.parseInt(c[3]) & 0xFF);

			return find(lip);
		}

		public void save_to_bin(OutputStream bos) throws IOException {
			write_int(bos, this.g_ip.length);
			bos.write(this.g_ip);
		}

		public void load_from_bin(InputStream ios) throws IOException {
			int len = read_int(ios);
			byte[] b = new byte[len];
			ios.read(b);
			this.g_ip = b;
		}
	}

	public static class card {
		static final int PAGE_SIZE = 128;
		byte[] g_card = null; // 身份证号前6位
		linear<entry> list = new linear<ip_cache.card.entry>(
				new Comparator<entry>() {
					@Override
					public int compare(entry o1, entry o2) {
						return comp_tool.comp(o1.card, o2.card);
					}
				}, 1 << 12);

		public static class entry {

			public entry(int card, long code) {
				this.card = card;
				this.code = code;
			}

			public String toString() {
				return "CARD=" + card + ",CODE=" + code;
			}

			public int card;
			public long code;
		}

		/**
		 * 将当前行的数据保存到分页(bin_stream)中
		 * 
		 * @param s
		 *            当前行数据
		 * @param bs
		 *            分页, 分页存储格式：[数据个数,起始号码,和起始号码的差值,和起始号码的差值。。。。]
		 * @param mobile
		 *            当前分页的起始号码段(手机号前7位)
		 * @return 分页中起始号码
		 */
		private int parse_card(String s, bin_stream bs, int card) {
			String c[] = s.split(",");
			// c[0] card c[1] code

			int _card_ = Integer.parseInt(c[0]);
			long _code_ = Long.parseLong(c[1].substring(1, c[1].length() - 1));

			if (card == 0) {
				bs.save((byte) 0);
				bs.save(_card_);
				card = _card_;
			}

			bs.off_save(_card_ - card);
			bs.save(_code_);
			return card;
		}

		private card load_card_from_txt(InputStream stm) throws IOException {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1 << 20);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stm));
			bin_stream bs = new bin_stream(PAGE_SIZE);
			int card = 0;// 分页中的起始号码
			int p_size, i = 0;
			String s;
			for (i = 0; null != (s = reader.readLine()); i++) {
				p_size = bs.size();
				card = parse_card(s, bs, card);
				if (bs.size() <= PAGE_SIZE) {
					continue;
				}

				bs.ppos(p_size);
				write_page(bo, bs, i, PAGE_SIZE);

				bs.ppos(0);
				i = 0;
				card = parse_card(s, bs, 0);
			}

			if (bs.size() > 0)
				write_page(bo, bs, i, PAGE_SIZE);

			g_card = bo.toByteArray();

			return this;
		}

		private void conver_entry() {
			bin_stream bs = new bin_stream(g_card);
			int page_size = g_card.length / PAGE_SIZE;
			for (int page_index = 0; page_index < page_size; page_index++) {
				bs.gpos(page_index * PAGE_SIZE);
				int entry_count = bs.c_int(bs.load_byte());
				int page_start = bs.load_int();// 本页的起始号段
				int val;// 和本页起始号码段的差值
				long code;// 城市代码
				for (int i = 0; i < entry_count; i++) {
					val = bs.off_load();
					code = bs.load_long();
					list.add(new entry(page_start + val, code));
				}
			}
			list.sort();
		}

		public entry find(String card) {
			if (card == null || (card.length() != 15 && card.length() != 18)) {
				/*
				 * add lining 2015-07-05 begin POC测试修改, 格式错误返回null
				 */
				// throw new tms_exception("身份证号格式错误：'", card, "'");
				return null;
				/* add lining 2015-07-05 end */
			}
			return list.get(new entry(Integer.parseInt(card.substring(0, 6)),
					-1));
		}

		public void save_to_bin(OutputStream bos) throws IOException {
			write_int(bos, this.g_card.length);
			bos.write(this.g_card);
		}

		public void load_from_bin(InputStream ios) throws IOException {
			int len = read_int(ios);
			byte[] b = new byte[len];
			ios.read(b);
			this.g_card = b;
			conver_entry();
		}
	}

	public static class mobile {
		
		static final int PAGE_SIZE = 128;
		
		byte[] g_mobile = null; // 手机号前7位

		public static class entry {
			public entry(int mobile, long code) {
				this.mobile = mobile;
				this.code = code;
			}

			public String toString() {
				return "MOBILE=" + mobile + ",CODE=" + code;
			}

			public int mobile;
			public long code;
		}

		/**
		 * 将当前行的数据保存到分页(bin_stream)中
		 * 
		 * @param s
		 *            当前行数据
		 * @param bs
		 *            分页, 分页存储格式：[数据个数,起始号码,和起始号码的差值,和起始号码的差值。。。。]
		 * @param mobile
		 *            当前分页的起始号码段(手机号前7位)
		 * @return 分页中起始号码
		 */
		private int parse_mobile(String s, bin_stream bs, int mobile) {
			String c[] = s.split(",");
			// c[0] mobile c[1] code

			int _mobile_ = Integer.parseInt(c[0]);
			long _code_ = Long.parseLong(c[1].substring(1, c[1].length() - 1));

			if (mobile == 0) {
				bs.save((byte) 0);
				bs.save(_mobile_);
				mobile = _mobile_;
			}

			bs.off_save(_mobile_ - mobile);
			bs.save(_code_);
			return mobile;
		}

		private mobile load_mobile_from_txt(InputStream stm) throws IOException {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1 << 20);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stm));
			bin_stream bs = new bin_stream(PAGE_SIZE);
			int mobile = 0;// 分页中的起始号码
			int p_size, i = 0;
			String s;
			for (i = 0; null != (s = reader.readLine()); i++) {
				p_size = bs.size();
				mobile = parse_mobile(s, bs, mobile);
				if (bs.size() <= PAGE_SIZE) {
					continue;
				}

				bs.ppos(p_size);
				write_page(bo, bs, i, PAGE_SIZE);

				bs.ppos(0);
				i = 0;
				mobile = parse_mobile(s, bs, 0);
			}

			if (bs.size() > 0)
				write_page(bo, bs, i, PAGE_SIZE);

			g_mobile = bo.toByteArray();

			return this;
		}

		final int get_page_mobile(int pos) {
			return get_int(g_mobile, pos * PAGE_SIZE + 1);
		}

		final entry find_in_page(int mobile, int page_index) {
			bin_stream bs = new bin_stream(g_mobile);
			bs.gpos(page_index * PAGE_SIZE);
			int entry_count = bs.c_int(bs.load_byte());
			int page_start = bs.load_int();// 本页的起始号段
			int dis = mobile - page_start;// 所要查询的手机号段和本页起始号段之间的差值
			int val;// 和本页起始号码段的差值
			long code;// 城市代码
			for (int i = 0; i < entry_count; i++) {
				val = bs.off_load();
				code = bs.load_long();
				if (val == dis)
					return new entry(mobile, code);
				if (val > dis)
					return null;
			}
			return null;
		}

		public entry find(int mobile) {
			int s = 0, e = g_mobile.length / PAGE_SIZE, m = e / 2;
			int page_card;
			while (s != m) {
				page_card = get_page_mobile(m);
				if (page_card < mobile)
					s = m;
				else if (page_card > mobile)
					e = m;
				else
					break;
				m = (s + e) / 2;
			}
			return find_in_page(mobile, m);
		}

		public entry find(String mobile) {
			if (mobile == null || mobile.length() != 11) {
				/*
				 * add lining 2015-07-05 begin POC测试修改, 格式错误返回null
				 */
				// throw new tms_exception("手机号格式错误：'", mobile, "'");
				return null;
				/* add lining 2015-07-05 end */
			}
			return find(Integer.parseInt(mobile.substring(0, 7)));
		}

		public void save_to_bin(OutputStream bos) throws IOException {
			write_int(bos, this.g_mobile.length);
			bos.write(this.g_mobile);
		}

		public void load_from_bin(InputStream ios) throws IOException {
			int len = read_int(ios);
			byte[] b = new byte[len];
			ios.read(b);
			this.g_mobile = b;
		}
	}

	ip m_ip;
	loc m_loc;
	card m_card;
	mobile m_mobile;

	public ip_cache() {
		m_ip = new ip();
		m_loc = new loc();
		m_card = new card();
		m_mobile = new mobile();
	}

	static void fconv(String ip_fname, String cd_fname, String card_fname,
			String mb_fname, String bin_fname) throws IOException {
		OutputStream bos = new FileOutputStream(bin_fname);

		initdb(ip_fname, cd_fname, card_fname, mb_fname, bos);

		bos.close();
	}

	static void initdb(String ip_fname, String cd_fname, String card_fname,
			String mb_fname, OutputStream lob_stream) throws IOException {
		// All:38945, ip+city:37769, ip+city+card:38903
		InputStream bis = null;
		new ip().load_ip_from_txt(bis = new FileInputStream(ip_fname))
				.save_to_bin(lob_stream);
		bis.close();
		new loc().load_from_txt(bis = new FileInputStream(cd_fname))
				.save_to_bin(lob_stream);
		bis.close();
		new card().load_card_from_txt(bis = new FileInputStream(card_fname))
				.save_to_bin(lob_stream);
		bis.close();
		new mobile().load_mobile_from_txt(bis = new FileInputStream(mb_fname))
				.save_to_bin(lob_stream);
		bis.close();
	}

	static public ip_cache load_from_db() {
		data_source ds = new data_source();
		String lob_sql = "select DATA from " + dao_ip.Instance().curr_name()
				+ " where NAME='IPData'";
		batch_stmt_jdbc lob_stmt = new batch_stmt_jdbc(ds, String.format(
				lob_sql, "TMS_MGR_LOB"), null);
		ResultSet lob_res = null;
		try {
			lob_res = lob_stmt.query(null);
			InputStream in = null;
			if (lob_res.next())
				in = lob_res.getBinaryStream(1);
			ip_cache ret = new ip_cache();
			ret.load_from_bin(in);
			return ret;
		} catch (SQLException e) {
			log.error(null, e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (lob_res != null)
					lob_res.close();
			} catch (SQLException e) {
				log.error(null, e);
			}
			lob_res = null;
			lob_stmt.close();
		}

		return null;
	}

	static public ip_cache load_from_text() {
		ip_cache ret = new ip_cache();
		InputStream bis = null;
		try {
			ret.m_ip.load_ip_from_txt(bis = new FileInputStream("c:\\ip.txt"));
			bis.close();
			ret.m_loc.load_from_txt(bis = new FileInputStream("c:\\city.txt"));
			bis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	static public ip_cache load_from_bin() {
		ip_cache ret = new ip_cache();
		InputStream bis = null;
		try {
			ret.load_from_bin(bis = new FileInputStream("c:\\1.bin"));
			bis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public ip.entry find_ip_entry(String ip0) {
		return m_ip.find(ip0);
	}

	public ip.entry find_ip_entry(long ip0) {
		return m_ip.find(ip0);
	}

	public loc.entry find_loc_entry(int type, Object... p) {
		long code = -1;
		if (type == ip_cache.LOCATE_TYPE_IP) {
			ip.entry e = m_ip.find((String) p[0]);
			if (e != null)
				code = e.code;
		} else if (type == ip_cache.LOCATE_TYPE_CARD) {
			card.entry e = m_card.find((String) p[0]);
			if (e != null)
				code = e.code;
		} else if (type == ip_cache.LOCATE_TYPE_MOBILE) {
			mobile.entry e = m_mobile.find((String) p[0]);
			if (e != null)
				code = e.code;
		}
		if (code == -1)
			return null;
		return m_loc.find(code);
	}

	public card.entry find_card_entry(String card) {
		return m_card.find(card);
	}

	public String get_isp(String ip0) {
		return null;
	}

	public String get_city(int type, Object... p) {
		loc.entry le = find_loc_entry(type, p);
		if (le == null)
			return null;

		return "" + le.city;
	}

	public String get_region(int type, Object... p) {
		loc.entry le = find_loc_entry(type, p);
		if (le == null)
			return null;

		return le.region;
	}

	public String get_country(int type, Object... p) {
		loc.entry le = find_loc_entry(type, p);
		if (le == null)
			return null;

		return le.country;
	}

	// 返回城市名 added by wujw 2016/06/08
	public String get_cityname(int type, Object... p) {
		loc.entry le = find_loc_entry(type, p);
		if (le == null)
			return null;

		return le.cityname;
	}

	// 返回全路径的地名，如“中国-广东-深圳” added by wujw 2016/06/08
	public String get_addrname(int type, Object... p) {
		String fullname = "";
		loc.entry le = find_loc_entry(type, p);
		if (le == null)
			return null;

		String cityName = le.cityname;
		db_region dbRegion = db_cache.get().region().get_map(le.region);
		String regionName = (null != dbRegion ? dbRegion.regionName : null);

		db_country dbCountry = db_cache.get().country().get_map(le.country);
		String countryName = (null != dbCountry ? dbCountry.countryName : null);
		fullname = countryName + "-" + regionName + "-" + cityName;

		return fullname;
	}

	static int get_int(byte[] c, int pos) {
		int ret = 0xff & c[pos];
		ret = (ret << 8) | (0xff & c[pos + 1]);
		ret = (ret << 8) | (0xff & c[pos + 2]);
		ret = (ret << 8) | (0xff & c[pos + 3]);
		return ret;
	}

	static long get_long(byte[] c, int pos) {
		long ret = 0xff & c[pos];
		ret = (ret << 8) | (0xff & c[pos + 1]);
		ret = (ret << 8) | (0xff & c[pos + 2]);
		ret = (ret << 8) | (0xff & c[pos + 3]);
		ret = (ret << 8) | (0xff & c[pos + 4]);
		ret = (ret << 8) | (0xff & c[pos + 5]);
		ret = (ret << 8) | (0xff & c[pos + 6]);
		ret = (ret << 8) | (0xff & c[pos + 7]);
		return ret;
	}

	static private final void write_int(OutputStream stm, int i)
			throws IOException {
		stm.write(0xFF & (i >> 24));
		stm.write(0xFF & (i >> 16));
		stm.write(0xFF & (i >> 8));
		stm.write(0xFF & (i));
	}

	static private final int read_int(InputStream stm) throws IOException {
		int ret = 0xFF & stm.read();
		ret = (ret << 8) | (0xFF & stm.read());
		ret = (ret << 8) | (0xFF & stm.read());
		ret = (ret << 8) | (0xFF & stm.read());

		return ret;
	}

	static private final void write_page(ByteArrayOutputStream bo,
			bin_stream page, int entry_count, int PAGE_SIZE) throws IOException {
		while (page.size() < PAGE_SIZE)
			page.save((byte) 0);

		page.ppos(0);
		page.save((byte) (0xFF & entry_count));

		page.ppos(PAGE_SIZE);
		bo.write(page.to_bytes());
	}

	public final ip_cache load_from_bin(InputStream stm) throws IOException {
		m_ip.load_from_bin(stm);
		m_loc.load_from_bin(stm);
		m_card.load_from_bin(stm);
		m_mobile.load_from_bin(stm);
		return this;
	}

	public final void save_to_bin(OutputStream stm) throws IOException {
		m_ip.save_to_bin(stm);
		m_loc.save_to_bin(stm);
	}

	static void test(ip_cache ipc, long ip, long code) {
		ip.entry c = ipc.find_ip_entry(ip);
		if (c == null) {
			System.out.println("find ip:" + ip + ":" + long2ip(ip));
			return;
		}
		if (code != c.code)
			System.out.println("find error:" + ip + ":" + long2ip(ip));

		c = null;

		loc.entry le = ipc.m_loc.find(code);
		if (le == null) {
			System.out.println("find loc:" + ip + ":" + long2ip(ip));
			return;
		}

		if (code != le.city)
			System.out.println("find lid error:" + ip + ":" + long2ip(ip)
					+ ", city:" + le.city + ", code: " + code);
		le = null;

	}

	/*
	 * static void test(ip_cache ipc, int type, String pam, int code) {
	 * loc.entry e = ipc.find_loc_entry(type, pam); if (e == null) {
	 * System.out.println("not find: [type=" + type + ", param=" + pam + "]");
	 * return; } if (e.loc_id != code) { System.out.println("find error: [type="
	 * + type + ", param=" + pam + "]"); } }
	 */

	static void test() {
		try {
			long mm = System.currentTimeMillis();
			//F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\1-ip.csv"
			fconv("E:\\higinet_space\\tms-web\\work\\upload__19fcf941_15c2eaf6a56__8000_00000004.tmp",
					"F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\2-city.csv",
					"F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\3-card.csv",
					"F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\4-mobile.csv",
					"F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\1.bin");
//			fconv("F:\\workfiles\\higinet_TMS\\deployment\\V4.2\\地理相关信息库文件\\GeoIPCity-134-Blocks.csv",
//					"F:\\workfiles\\higinet_TMS\\deployment\\V4.2\\地理相关信息库文件\\tms_mgr_city.csv",
//					"F:\\workfiles\\higinet_TMS\\deployment\\V4.2\\地理相关信息库文件\\card.csv",
//					"F:\\workfiles\\higinet_TMS\\deployment\\V4.2\\地理相关信息库文件\\moblie.csv",
//					"F:\\workfiles\\higinet_TMS\\deployment\\V4.2\\地理相关信息库文件\\1.bin");
			// System.out.println("parse time:" + (System.currentTimeMillis() -
			// mm));

			mm = System.currentTimeMillis();
			ip_cache ipc = new ip_cache()
					.load_from_bin(new FileInputStream(
							"F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\1.bin"));
//			ip_cache ipc = new ip_cache()
//			.load_from_bin(new FileInputStream(
//					"F:\\workfiles\\higinet_TMS\\deployment\\V4.2\\地理相关信息库文件\\1.bin"));
			System.out
					.println("load time:" + (System.currentTimeMillis() - mm));
			System.out.println("ip page count:"
					+ (ipc.m_ip.g_ip.length / ip_cache.ip.PAGE_SIZE));
			System.out.println("loc page count:"
					+ (ipc.m_loc.g_loc.length / ip_cache.loc.PAGE_SIZE));
			System.out.println("card page count:"
					+ (ipc.m_card.g_card.length / ip_cache.card.PAGE_SIZE));
			System.out.println("card entry count:" + ipc.m_card.list.size());
			System.out
					.println("mobile page count:"
							+ (ipc.m_mobile.g_mobile.length / ip_cache.mobile.PAGE_SIZE));
			System.out.println(ipc.find_ip_entry(3758096128L));
			cn.com.higinet.tms35.core.cache.ip_cache.ip.entry e = ipc
					.find_ip_entry("72.137.202.122");
			System.out.println(e);
			cn.com.higinet.tms35.core.cache.ip_cache.loc.entry lc = ipc.m_loc
					.find(e.code);
			System.out.println(lc.city);

			mm = System.currentTimeMillis();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(
									"F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\1-ip.csv")));
			String s;
			int i;
			for (i = 0; null != (s = reader.readLine()); i++) {
				String c[] = s.split(",");
				long ip1 = Long.parseLong(c[0].substring(1, c[0].length() - 1));
				long ip2 = Long.parseLong(c[1].substring(1, c[1].length() - 1));
				long code = Long
						.parseLong(c[2].substring(1, c[2].length() - 1));

				test(ipc, ip1, code);
				test(ipc, ip2, code);
			}

			System.out
					.println((System.currentTimeMillis() - mm) / (1. * i * 2));

			System.out.println("find count per msec:" + 1. * i * 2
					/ (System.currentTimeMillis() - mm));
			/*
			 * BufferedReader reader = new BufferedReader(new
			 * InputStreamReader(new FileInputStream(
			 * "F:\\workfiles\\higinet_TMS\\SVN\\99bill\\部署\\IP地址库\\card.csv")));
			 * int i; String s; for (i = 0; null != (s = reader.readLine());
			 * i++) { String c[] = s.split(","); String card = c[0] +
			 * "198709220014"; int code = Integer.parseInt(c[1]);
			 * 
			 * test(ipc, ip_cache.LOCATE_TYPE_CARD, card, code); }
			 * System.out.println("find count per msec:" + 1. * i * 2 /
			 * (System.currentTimeMillis() - mm));
			 */

			/*
			 * BufferedReader reader = new BufferedReader(new
			 * InputStreamReader(new FileInputStream( "e:\\mobile.txt"))); int
			 * i; String s; for (i = 0; null != (s = reader.readLine()); i++) {
			 * String c[] = s.split(","); String mobile = c[0] + "3459"; int
			 * code = Integer.parseInt(c[1]);
			 * 
			 * test(ipc, ip_cache.LOCATE_TYPE_MOBILE, mobile, code); }
			 * System.out.println("find count per msec:" + 1. * i * 2 /
			 * (System.currentTimeMillis() - mm));
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// testStart.init();
		test();
	}

	static volatile ip_cache g_ipc;

	public static void reset() {
		g_ipc = null;
	}

	public static ip_cache Instance() {
		if (g_ipc != null)
			return g_ipc;

		synchronized (ip_cache.class) {
			if (g_ipc == null)
				// return g_ipc = load_from_text();
				return g_ipc = load_from_db();
		}

		return g_ipc;
	}

	static public void remote_test() {
		ip_cache ipc = ip_cache.Instance();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream("c:\\ip.txt")));
			String s;
			for (; null != (s = reader.readLine());) {
				String c[] = s.split(",");
				long ip1 = Long.parseLong(c[0].substring(1, c[0].length() - 1));
				long ip2 = Long.parseLong(c[1].substring(1, c[1].length() - 1));
				long id = Long.parseLong(c[2].substring(1, c[2].length() - 1));

				test(ipc, ip1, id);
				test(ipc, ip2, id);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 把long->ip地址
	 * 
	 * @param iplong
	 * @return String
	 */
	public static String long2ip(long iplong) {
		return new StringBuilder().append(((iplong >> 24) & 0xff)).append('.')
				.append((iplong >> 16) & 0xff).append('.')
				.append((iplong >> 8) & 0xff).append('.')
				.append((iplong & 0xff)).toString();
	}
}
