package cn.com.higinet.tms.engine.core.concurrent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class con_map_null <K,V> implements ConcurrentMap<K, V>
{
	
	public V putIfAbsent(K key, V value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean remove(Object key, Object value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean replace(K key, V oldValue, V newValue)
	{
		// TODO Auto-generated method stub
		return false;
	}

	
	public V replace(K key, V value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public void clear()
	{
		
	}

	
	public boolean containsKey(Object key)
	{
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean containsValue(Object value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public V get(Object key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	
	public Set<K> keySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public V put(K key, V value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public void putAll(Map<? extends K, ? extends V> m)
	{
		// TODO Auto-generated method stub
		
	}

	
	public V remove(Object key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public int size()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	
	public Collection<V> values()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String argv[])
	{
		ConcurrentMap<String, String> map= new ConcurrentLinkedHashMap.Builder<String,String>()
											.initialCapacity(100)
											.maximumWeightedCapacity(100)
											.build();
		
		for(int i=0;i<100;i++)
			map.put(""+i, ""+i*i);
		
		map.get("0");
		map.get("1");
		map.put("101", "101");
		for(Map.Entry<String, String> m:map.entrySet())
		{
			System.out.println(m);
		}
	}
}
