package cn.com.higinet.tms35.comm;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public final class bin_stream {
	private static final int min_buf_size = 256;

	private byte[] _buff = null;

	private int _len = 0;

	private int _pos = 0;

	private int _buff_error = 0;

	public bin_stream() {
		grow(min_buf_size);
	}

	public bin_stream(int len) {
		grow(len);
	}

	public bin_stream(byte[] buff) {
		_buff = buff;
		_len = buff.length;
	}

	public bin_stream(byte[] buff, int len) {
		_buff = buff;
		_len = len;
	}

	public byte[] array() {
		return _buff;
	}

	public byte[] to_bytes() {
		return to_bytes(0);
	}

	public byte[] to_bytes(int skip_len) {
		if (skip_len < 0)
			return null;

		int len = size() - skip_len;
		if (len <= 0)
			return null;

		byte[] ret = new byte[len];
		System.arraycopy(_buff, skip_len, ret, 0, len);

		return ret;

	}

	public boolean good() {
		return this._buff_error == 0;
	}

	public boolean can_read(int len) {
		return good() && can_read(len, _pos);
	}

	public boolean can_read(int len, int pos) {
		return pos + len <= _buff.length;
	}

	boolean check_buff_size(int len) {
		return check_buff_size(len, _pos);
	}

	boolean check_buff_size(int len, int pos) {
		if (pos + len <= _buff.length)
			return true;

		_buff_error = 1;

		return false;
	}

	public int size() {
		return _len;
	}

	public int max_size() {
		return _buff.length;
	}

	public void ppos(int pos) {
		_len = pos;
	}

	public int gpos() {
		return _pos;
	}

	public void gpos(int pos) {
		_pos = pos;
	}

	public void reserve(int len) {
		if (len <= _buff.length)
			return;

		byte[] t = new byte[len];
		System.arraycopy(_buff, 0, t, 0, _len);

		_buff = t;
	}

	void grow(int cap) {
		if (_buff == null) {
			_buff = new byte[Math.max(cap, min_buf_size)];
			return;
		}

		if (cap < _buff.length)
			return;

		int n = Math.max(_buff.length, min_buf_size);

		while (n < cap)
			n += n >> 2;

		reserve(n);
	}

	public bin_stream save(byte b) {
		grow(_len + 1);
		_buff[_len++] = b;

		return this;
	}

	public bin_stream save(short i) {
		grow(_len + 2);
		_buff[_len++] = (byte) ((i >>> 8) & 0xff);
		_buff[_len++] = (byte) (i & 0xff);

		return this;
	}

	public bin_stream save(int i) {
		grow(_len + 4);
		_buff[_len++] = (byte) ((i >>> 24) & 0xff);
		_buff[_len++] = (byte) ((i >>> 16) & 0xff);
		_buff[_len++] = (byte) ((i >>> 8) & 0xff);
		_buff[_len++] = (byte) (i & 0xff);

		return this;
	}

	public bin_stream save(long i) {
		save((int) (i >> 32));
		return save((int) (i & 0xFFFFFFFFL));
	}

	public bin_stream save(float i) {
		return save(Float.floatToIntBits(i));
	}

	public bin_stream save(double i) {
		return save(Double.doubleToLongBits(i));
	}

	public bin_stream save(String s) {
		if (s == null)
			return off_save(Integer.MAX_VALUE);

		try {
			save(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}

	public bin_stream save2(String s) {
		if (s == null)
			return off_save(Integer.MAX_VALUE);

		return save(s.toCharArray());
	}

	public bin_stream save(byte[] s) {
		if (s == null) {
			return off_save(Integer.MAX_VALUE);
		}

		off_save(s.length);
		grow(_len + s.length);
		System.arraycopy(s, 0, _buff, _len, s.length);
		_len += s.length;

		return this;
	}

	public bin_stream save(char[] s) {
		if (s == null)
			return off_save(Integer.MAX_VALUE);

		grow(_len + s.length * 2 + 4);
		off_save(s.length);
		for (int i = 0; i < s.length; i++)
			save((short) s[i]);

		return this;
	}

	public bin_stream off_save(int len) {
		while (len > 0x7F) {
			save((byte) (len & 0x7F));
			len >>= 7;
		}

		save((byte) (len | 0x80));

		return this;
	}

	public final int c_int(byte b) {
		return 0xff & b;
	}

	public final int c_int(short b) {
		return 0xffff & b;
	}

	public final long c_long(int i) {
		return 0xffffffffL & i;
	}

	public byte load_byte() {
		if (!this.check_buff_size(1))
			return -1;

		return _buff[_pos++];
	}

	public short load_short() {
		if (!this.check_buff_size(2))
			return -1;

		int ret = c_int(_buff[_pos++]) << 8;
		return (short) (ret | c_int(_buff[_pos++]));
	}

	public int load_int() {
		int ret = c_int(load_short()) << 16;
		return (ret) | c_int(load_short());
	}

	public long load_long() {
		long ret = c_long(load_int()) << 32;
		return (ret) | c_long(load_int());
	}

	public float load_float() {
		return Float.intBitsToFloat(load_int());
	}

	public double load_double() {
		return Double.longBitsToDouble(load_long());
	}

	public byte[] load_byte_array() {
		int len = off_load();
		if (len == Integer.MAX_VALUE)
			return null;

		byte[] ret = new byte[len];

		if (!this.check_buff_size(len))
			return null;

		System.arraycopy(_buff, _pos, ret, 0, len);
		_pos += len;

		return ret;
	}

	public char[] load_char_array() {
		int len = off_load();
		if (len == Integer.MAX_VALUE)
			return null;

		char[] ret = new char[len];

		if (!this.check_buff_size(len * 2))
			return null;

		for (int i = 0; i < len; i++)
			ret[i] = (char) this.load_short();

		return ret;
	}

	public String load_string() {
		byte[] b = load_byte_array();
		if (b == null)
			return null;

		try {
			return new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String load_string2() {
		char[] b = load_char_array();
		if (b == null)
			return null;

		return String.valueOf(b);
	}

	public int off_load() {
		int ret = 0, c;
		for (int i = 0; i < 5; i++) {
			c = c_int(load_byte());
			if ((c & 0x80) == 0x80) {
				return ret | ((c ^ 0x80) << i * 7);
			}

			ret |= c << (i * 7);
		}

		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Object> b = new ArrayList<Object>();
		for (int i = 0; i < 200; i++) {
			if (i % 3 == 0)
				b.add(null);
			else
				b.add(String.valueOf(i));
		}
		bin_stream bc = new bin_stream();
		for (int i = 0; i < 200; i++) {
			Object o = b.get(i);
			bc.save2(o == null ? null : o.toString());
		}
		for (int i = 0; i < 200; i++) {
			System.out.println(bc.load_string2());
		}
	}

	public boolean load_bool() {
		return this.load_byte() != 0;
	}

	public bin_stream save(boolean b) {
		return this.save((byte) (b ? 1 : 0));
	}
}
