package cn.com.higinet.tms.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;

import cn.com.higinet.tms.common.util.ByteUtils;
import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;

public class SerializeTest extends BaseConcurrentTest {
	private static final int size = 200;

	@Test
	public void listJsonEncode() {
		List<Object> b = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			if (i % 3 == 0)
				b.add(null);
			else
				b.add(i + "");
		}
		//		b.add(0, ",\"b\",\"c\",\"d\"");
		int len = 0;
		for (Object s : b) {
			len += s != null ? s.toString().length() : 0;
		}
		System.out.println("fjsonlist_len_original:" + len);
		len = ByteUtils.object2JsonBytes(b).length;
		System.out.println("fjsonlist_len_json:" + len);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.object2JsonBytes(b);
		}
		long t = c.countMillis();
		System.out.println("fjsonlist:" + DEFAULT_TIMES / t * 1000);
		System.out.println("fjsonlist:" + t);
	}

	@Test
	public void listJsonDecode() {
		List<Object> b = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			if (i % 3 == 0)
				b.add(null);
			else
				b.add(i + "");
		}
		byte[] data = ByteUtils.object2JsonBytes(b);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.jsonBytes2Object(ArrayList.class, data);
		}
		long t = c.countMillis();
		System.out.println("fjson_list_encode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("fjson_list_encode_cost:" + t);
	}

	@Test
	public void listBCEncode() throws IOException {
		List<Object> b = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			if (i % 3 == 0)
				b.add(null);
			else
				b.add(i + "");
		}
		byte[] bs = ByteUtils.list2bytes(b);
		System.out.println("bc_len:" + bs.length);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.list2bytes(b);
		}
		long t = c.countMillis();
		System.out.println("bc_list_encode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("bc_list_encode_cost:" + t);
	}

	@Test
	public void listBCDecode() throws IOException {
		List<Object> b = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			if (i % 3 == 0)
				b.add(null);
			else
				b.add(i + "");
		}
		byte[] bs = ByteUtils.list2bytes(b);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.bytes2List(bs);
		}
		long t = c.countMillis();
		System.out.println("bc_list_decode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("bc_list_decode_cost:" + t);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void listKryoEncode() throws IOException {
		List<Object> b = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			if (i % 3 == 0)
				b.add(null);
			else
				b.add(i + "");
		}
		byte[] data = ByteUtils.object2KryoBytes(b);
		System.out.println("kryo_list_len:" + data.length);
		b = ByteUtils.KryoBytes2Object(ArrayList.class, data);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.object2KryoBytes(b);
		}
		long t = c.countMillis();
		System.out.println("kryo_list_encode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("kryo_list_encode_cost:" + t);
	}

	@Test
	public void listKryoDecode() throws IOException {
		List<Object> b = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			if (i % 3 == 0)
				b.add(null);
			else
				b.add(i + "");
		}
		byte[] data = ByteUtils.object2KryoBytes(b);
		System.out.println("kryo_list_len:" + data.length);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.KryoBytes2Object(ArrayList.class, data);
		}
		long t = c.countMillis();
		System.out.println("kryo_list_decode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("kryo_list_decode_cost:" + t);
	}

	@Test
	public void mapKryoEncode() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < size; i++)
			map.put(String.valueOf(i), "111111111111");
		byte[] data = ByteUtils.object2KryoBytes(map);
		System.out.println("kryo_Map_len:" + data.length);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.object2KryoBytes(map);
		}
		long t = c.countMillis();
		System.out.println("kryo_Map_encode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("kryo_Map_encode_cost:" + t);
	}

	@Test
	public void mapKryoDecode() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < size; i++)
			map.put(String.valueOf(i), "111111111111");
		byte[] data = ByteUtils.object2KryoBytes(map);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.KryoBytes2Object(HashMap.class, data);
		}
		long t = c.countMillis();
		System.out.println("kryo_Map_decode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("kryo_Map_decode_cost:" + t);
	}

	@Test
	public void mapJsonEncode() throws IOException {
		Map<String, String> b = new HashMap<String, String>();
		for (int i = 0; i < size; i++)
			b.put(String.valueOf(i), "111111111111");
		System.out.println("kryo_Map_len:" + ByteUtils.object2JsonBytes(b).length);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.object2JsonBytes(b);
		}
		long t = c.countMillis();
		System.out.println("json_Map_encode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("json_Map_encode_cost:" + t);
	}

	@Test
	public void mapJsonDecode() throws IOException {
		Map<String, String> b = new HashMap<String, String>();
		for (int i = 0; i < size; i++)
			b.put(String.valueOf(i), "111111111111");
		byte[] data = ByteUtils.object2JsonBytes(b);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			ByteUtils.jsonBytes2Object(HashMap.class, data);
		}
		long t = c.countMillis();
		System.out.println("kryo_Map_decode_tps:" + DEFAULT_TIMES / t * 1000);
		System.out.println("kryo_Map_decode_cost:" + t);
	}

	@SuppressWarnings("unchecked")
	//	@Test
	public void kryoMapTest() throws IOException {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		kryo.setRegistrationRequired(false);
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < size; i++)
			map.put(String.valueOf(i), "111111111111");
		byte[] data = ByteUtils.object2KryoBytes(map);
		System.out.println("kryo_Map_len:" + data.length);
		map = (HashMap<String, String>) ByteUtils.KryoBytes2Object(HashMap.class, data);
		System.out.println(map);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void kryoObjectTest() throws IOException {
		Tes t = new Tes();
		t.setA("a");
		t.setB("b");
		byte[] b = ByteUtils.object2KryoBytes(t);
		System.out.println(ByteUtils.KryoBytes2Object(Tes.class, b));
	}

	public static class Tes {
		
		private transient String a;

		private String b;

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}

		public String toString() {
			return a + " " + b;
		}
	}
}
