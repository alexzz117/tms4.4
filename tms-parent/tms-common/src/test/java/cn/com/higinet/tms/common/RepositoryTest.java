package cn.com.higinet.tms.common;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.initializer.ConfigInitializer;
import cn.com.higinet.tms.common.repository.Attribute;
import cn.com.higinet.tms.common.repository.CachedDataRepository;
import cn.com.higinet.tms.common.repository.Metadata;
import cn.com.higinet.tms.common.repository.PersistedDataRepository;
import cn.com.higinet.tms.common.repository.StructuredData;
import cn.com.higinet.tms.common.repository.converters.ConvertValueException;
import cn.com.higinet.tms.common.repository.converters.Converter;
import cn.com.higinet.tms.common.repository.persistence.AbstractSimpleRDBMSPersistence;
import cn.com.higinet.tms.common.util.ObjectUtils;

public class RepositoryTest {
	private static CacheManager cm = new CacheManager();

	private static final String testKey = "test1";

	private static final Metadata meta;

	private static Map<String, Converter<?>> converters = new HashMap<String, Converter<?>>();
	private static CachedDataRepository cache = new CachedDataRepository();
	private static PersistedDataRepository persist = new PersistedDataRepository();
	static {
		cm.setEnvInitializer(new ConfigInitializer());
		cm.start();
		meta = new Metadata();
		meta.setStructureName("wxtest");
		Attribute attr = new Attribute();
		attr.setPersistenceName("A");
		attr.setName("A");
		attr.setDefaultValue("1");
		attr.setNullable(true);
		attr.setOuterType(String.class);
		attr.setPrimary(true);
		attr.setLimitedLength(10);
		attr.setPersistedType(Types.INTEGER);
		meta.addAttributes(attr.getName(), attr);
		attr = ObjectUtils.clone(attr);
		attr.setPersistenceName("B");
		attr.setName("B");
		attr.setOuterType(Integer.class);
		attr.setDefaultValue("2");
		attr.setPersistedType(Types.INTEGER);
		meta.addAttributes(attr.getName(), attr);
		attr = ObjectUtils.clone(attr);
		attr.setPersistenceName("C");
		attr.setName("C");
		attr.setOuterType(Integer.class);
		attr.setDefaultValue("3");
		attr.setOuterConverter("testC");//设置转换器
		attr.setPersistedType(Types.VARCHAR);
		meta.addAttributes(attr.getName(), attr);
		converters.put("testC", new Converter<Integer>() {
			@Override
			public Integer convert(String value,Attribute attr) throws ConvertValueException {
				if ("aaa".equals(value)) {
					return 444;
				}
				return 123;
			}
		});
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void init() throws Exception {
		persist.setCacheManager(cm);
		persist.setMetadata(meta);
		persist.setConverters(converters);
		persist.setPersistence(new AbstractSimpleRDBMSPersistence() {

			@Override
			protected int executeSql(String sql, List<Object> params, int[] paramTypes) throws Exception {
				System.out.println(sql);
				return 0;
			}

			@Override
			protected Map<String, String> executeQuery(String sql, List<Object> params, int[] paramTypes) throws Exception {
				System.out.println(sql);
				return null;
			}

		});
		persist.initialize();
		cache.setCacheManager(cm);
		cache.setMetadata(meta);
		cache.initialize();
		cache.setConverters(converters);
	}

	@Test
	public void cacheRepositorySaveTest() throws Exception {
		StructuredData t1 = cache.createNewData(testKey);
		t1.setData("A", "111");
		t1.setData("B", "222");
		t1.setData("C", "1");
		cache.save(t1);
	}

	@Test
	public void cacheRepositoryGetTest() throws Exception {
		StructuredData t1 = cache.get(testKey);
		System.out.println(t1.getValue("A"));
		System.out.println(t1.getValue("B"));
		System.out.println(t1.getValue("C"));
	}

	@Test
	public void cacheRepositoryDeleteTest() throws Exception {
		StructuredData t1 = cache.delete(testKey);
		System.out.println(t1.getValue("A"));
		System.out.println(t1.getValue("B"));
		System.out.println(t1.getValue("C"));
	}

	@Test
	public void persistRepositorySaveTest() throws Exception {
		StructuredData t1 = persist.createNewData(testKey);
		t1.setData("A", "444");
		t1.setData("B", "222");
		t1.setData("C", "1");
		persist.save(t1);
	}

	@Test
	public void persistRepositoryGetTest() throws Exception {
		StructuredData t1 = persist.get(testKey);
		System.out.println(t1.getValue("A"));
		System.out.println(t1.getValue("B"));
		System.out.println(t1.getValue("C"));
	}

	@Test
	public void persistRepositoryDeleteTest() throws Exception {
		StructuredData t1 = persist.delete(testKey);
		System.out.println(t1.getValue("A"));
		System.out.println(t1.getValue("B"));
		System.out.println(t1.getValue("C"));
	}
}
