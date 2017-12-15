package cn.com.higinet.tms35.core.concurrent;

import java.util.concurrent.ConcurrentMap;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

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
	
	final public V del_row(K key)
	{
		return m_delegate.remove(key);
	}
}
