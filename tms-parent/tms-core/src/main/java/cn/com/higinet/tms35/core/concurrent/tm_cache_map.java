package cn.com.higinet.tms35.core.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cn.com.higinet.tms35.comm.tm_stamp;

@SuppressWarnings({"unchecked", "rawtypes"})
public class tm_cache_map<K, V extends tm_stamp> implements Map<K, V>
{
	static final int DEFAULT_INITIAL_CAPACITY = 32;
	static final int MAXIMUM_CAPACITY = 1 << 30;
	static final float DEFAULT_LOAD_FACTOR = 0.75f;
	Entry[] table;
	int size;
	int threshold;
	final float loadFactor;
	int modCount;
	boolean replaceOld;

	public tm_cache_map(boolean replaceOld, int initialCapacity, float loadFactor)
	{
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity)
			capacity <<= 1;

		this.loadFactor = loadFactor;
		threshold = (int) (capacity * loadFactor);
		table = new Entry[capacity];
		this.replaceOld = replaceOld;
	}

	public tm_cache_map(int initialCapacity, boolean replaceOld)
	{
		this(replaceOld, initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public tm_cache_map()
	{
		this.replaceOld = true;
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new Entry[DEFAULT_INITIAL_CAPACITY];
	}

	static int hash(int h)
	{
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	static int indexFor(int h, int length)
	{
		return h & (length-1);
	}

	public int size()
	{
		return size;
	}

	public boolean isEmpty()
	{
		return size == 0;
	}

	public V get(Object key)
	{
		if (key == null)
			return null;

		int hash = hash(key.hashCode());
		for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
				return e.value;
		}
		return null;
	}

	public boolean containsKey(Object key)
	{
		return getEntry(key) != null;
	}

	final Entry<K, V> getEntry(Object key)
	{
		if (key == null)
			return null;
		int hash = hash(key.hashCode());
		for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || (key.equals(k))))
				return e;
		}
		return null;
	}

	public V put(K key, V value)
	{
		if (key == null)
			return null;

		int hash = hash(key.hashCode());
		int i = indexFor(hash, table.length);
		for (Entry<K, V> e = table[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				V oldValue = e.value;
				if (replaceOld)
					e.value = value;
				return oldValue;
			}
		}

		modCount++;
		addEntry(hash, key, value, i);
		return null;
	}

	void resize(int newCapacity)
	{
		Entry[] oldTable = table;
		int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		Entry[] newTable = new Entry[newCapacity];
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * loadFactor);
	}

	/**
	 * Transfers all entries from current table to newTable.
	 */
	void transfer(Entry[] newTable)
	{
		Entry[] src = table;
		int newCapacity = newTable.length;
		for (int j = 0; j < src.length; j++)
		{
			Entry<K, V> e = src[j];
			if (e != null)
			{
				src[j] = null;
				do
				{
					Entry<K, V> next = e.next;
					int i = indexFor(e.hash, newCapacity);
					e.next = newTable[i];
					newTable[i] = e;
					e = next;
				} while (e != null);
			}
		}
	}

	public void putAll(Map<? extends K, ? extends V> m)
	{
		int numKeysToBeAdded = m.size();
		if (numKeysToBeAdded == 0)
			return;

		if (numKeysToBeAdded > threshold)
		{
			int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
			if (targetCapacity > MAXIMUM_CAPACITY)
				targetCapacity = MAXIMUM_CAPACITY;
			int newCapacity = table.length;
			while (newCapacity < targetCapacity)
				newCapacity <<= 1;
			if (newCapacity > table.length)
				resize(newCapacity);
		}

		for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i
				.hasNext();)
		{
			Map.Entry<? extends K, ? extends V> e = i.next();
			put(e.getKey(), e.getValue());
		}
	}

	public V remove(Object key)
	{
		if (key == null)
			return null;

		Entry<K, V> e = removeEntryForKey(key);
		return (e == null ? null : e.value);
	}

	public void remove_timeouted(long time)
	{
		modCount++;
		final Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
		{
			Entry<K, V> p = tab[i];
			if(p==null)
				continue;
			for(Entry<K, V> e = p.next;e != null;)
			{
				if (e.getValue().is_timeout(time))
				{
					size--;
					p.next = e.next;
				}
				else
				{
					p=e;
				}
				e=p.next;
			}
			
			if(tab[i].getValue().is_timeout(time))
			{
				size--;
				tab[i]=tab[i].next;
			}
		}
		
//		int size2=0;
//		for (int i = 0; i < tab.length; i++)
//		{
//			Entry<K, V> p = tab[i];
//			while(p!=null)
//			{
//				size2++;
//				p=p.next;
//			}
//		}
//		
//		if(size2!=size)
//		{
//			System.out.println("size error.");
//		}

	}

	final Entry<K, V> removeEntryForKey(Object key)
	{
		int hash = hash(key.hashCode());
		int i = indexFor(hash, table.length);
		Entry<K, V> prev = table[i];
		Entry<K, V> e = prev;

		while (e != null)
		{
			Entry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || (key.equals(k))))
			{
				modCount++;
				size--;
				if (prev == e)
					table[i] = next;
				else
					prev.next = next;

				return e;
			}
			prev = e;
			e = next;
		}

		return e;
	}

	public void clear()
	{
		modCount++;
		Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			tab[i] = null;
		size = 0;
	}

	public boolean containsValue(Object value)
	{
		if (value == null)
			return containsNullValue();

		Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			for (Entry e = tab[i]; e != null; e = e.next)
				if (value.equals(e.value))
					return true;
		return false;
	}

	private boolean containsNullValue()
	{
		Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			for (Entry e = tab[i]; e != null; e = e.next)
				if (e.value == null)
					return true;
		return false;
	}

	static class Entry<K, V extends tm_stamp> implements Map.Entry<K, V>
	{
		final K key;
		V value;
		Entry<K, V> next;
		final int hash;

		Entry(int h, K k, V v, Entry<K, V> n)
		{
			value = v;
			next = n;
			key = k;
			hash = h;
		}

		public final K getKey()
		{
			return key;
		}

		public final V getValue()
		{
			return value;
		}

		public final V setValue(V newValue)
		{
			V oldValue = value;
			value = newValue;
			return oldValue;
		}

		public final boolean equals(Object o)
		{
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry e = (Map.Entry) o;
			Object k1 = getKey();
			Object k2 = e.getKey();
			if (k1 == k2 || (k1 != null && k1.equals(k2)))
			{
				Object v1 = getValue();
				Object v2 = e.getValue();
				if (v1 == v2 || (v1 != null && v1.equals(v2)))
					return true;
			}
			return false;
		}

		public final int hashCode()
		{
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		public final String toString()
		{
			return getKey() + "=" + getValue();
		}
	}

	void addEntry(int hash, K key, V value, int bucketIndex)
	{
		Entry<K, V> e = table[bucketIndex];
		table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
		if (size++ >= threshold)
			resize(2 * table.length);
	}

	int capacity()
	{
		return table.length;
	}

	float loadFactor()
	{
		return loadFactor;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		throw new UnsupportedOperationException();
	}

	public Set<K> keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Collection<V> values()
	{
		throw new UnsupportedOperationException();
	}
}
