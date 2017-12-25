package cn.com.higinet.tms.engine.core.concurrent.cache;

import java.util.concurrent.ConcurrentMap;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

/*
 * 该类进行短时间的缓存，目前仅仅用于交易数据，缓存数据只在数据生成到数据存储到数据库之前存在
 * 该类存在的作用在于，为了防止交易二次评估过程读取交易数据时，数据还没有写入数据库 
 * 
 * */
public class tm_cache<K, V>
{
	ConcurrentMap<K, V> m_delegate;

	// static class wei implements Weigher<tm_stamp>
	// {
	//
	// public int weightOf(tm_stamp arg0)
	// {
	// return (int)(tm_tool.lctm_ms()-arg0.timestamp());
	// }
	// }

	public tm_cache(int max_size)
	{
		m_delegate = new ConcurrentLinkedHashMap.Builder<K, V>()//
				.initialCapacity(max_size)//
				.maximumWeightedCapacity(max_size).build();
	}

	final public V put(K k, V v)
	{
		return m_delegate.put(k, v);
	}

	public V putIfAbsent(K key, V value)
	{
		return m_delegate.putIfAbsent(key, value);
	}

	final public V get(K k)
	{
		return m_delegate.get(k);
	}

	final public V remove(K key)
	{
		return m_delegate.remove(key);
	}
	
	final public void clear()
	{
		m_delegate.clear();
	}
}
