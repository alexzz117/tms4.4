package cn.com.higinet.tms.engine.comm;

import java.util.HashSet;
import java.util.TreeSet;

import cn.com.higinet.tms.engine.comm.zpage.comparator;

public final class ztree<E>
{
	int page_padding;
	int page_size;
	int page_count;
	comparator<E> compare;
	zpage<zpage<E>> buf = null;
	boolean replace_old;

	final class index
	{
		public index(int p, int index, int pos)
		{
			ipage = p;
			ie = index;
		}

		public String toString()
		{
			return "{" + ipage + "," + ie + "," + pos + "}";
		}

		int ipage;
		int ie;
		int pos;
	}

	public ztree<E> Clone()
	{
		ztree<E> ret = new ztree<E>(this.compare, this.replace_old, this.page_count,
				this.page_size, this.page_padding);
		ret.buf = this.buf.Clone();
		for (int i = 0; i < this.buf.size(); i++)
			ret.buf.set(i, ret.buf.get(i).Clone());

		return ret;

	}

	public ztree(final comparator<E> compare, boolean replace_old, int page_count, int page_size,
			int page_padding)
	{
		this.compare = compare;
		this.replace_old = replace_old;
		this.page_size = zpage.max(page_size, 10);
		this.page_count = zpage.max(page_count, 10);
		this.page_padding = page_padding;
		buf = new zpage<zpage<E>>(new zpage.comparator<zpage<E>>()
		{
			public int compare(zpage<E> e, Object o)
			{
				if (e.isempty())
					return -1;

				return compare.compare(e.get(0), o);
			}

		}, this.page_count, 100);

		buf.add(new zpage<E>(compare, this.page_size, this.page_padding));
	}

	public ztree(final comparator<E> compare, boolean replace_old, int page_count, int page_size)
	{
		this(compare, replace_old, page_count, page_size, 90);
	}

	public E add(E el)
	{
		int i = page_index(el);

		zpage<E> p = buf.get(i);
		int pi = p.find(el);
		if (pi >= 0)
		{
			E ret = p.get(pi);
			if (replace_old)
				p.set(pi, el);

			return ret;
		}

		pi = -1 - pi;
		p.add(pi, el);
		if (p.isfull())
		{
			if (buf.isfull())
				grow();
			zpage<E> next = p.split();
			buf.add(i + 1, next);
		}

		return null;
	}

	private void grow()
	{
		zpage<zpage<E>> tmp = new zpage<zpage<E>>(new zpage.comparator<zpage<E>>()
		{
			public int compare(zpage<E> e, Object o)
			{
				if (e.isempty())
					return -1;

				return compare.compare(e.get(0), o);
			}

		}, this.page_count * 3 / 2 + 1, 100);

		tmp.copy(buf);
		page_count = tmp._page_size;
		buf = tmp;
	}

	public void clear()
	{
		buf.clear();
		buf.add(new zpage<E>(compare, page_size, page_padding));
	}

	private final index start_index = new index(0, 0, 0);

	public E get(int index)
	{
		index i = pos2index(index);
		if (i == null)
			return null;

		return buf.get(i.ipage).get(i.ie);
	}

	public E get(E el)
	{
		index i = find(el);
		return buf.get(i.ipage).get(i.ie);
	}

	public E remove(int index)
	{
		index i = pos2index(index);
		if (i == null)
			return null;
		return remove(i);
	}

	public E remove(E e)
	{
		index i = find(e);
		if (i == null || i.ie < 0)
			return null;

		E ret = remove(i);
		return ret;
	}

	public void remove_if(func_if<E> func)
	{
		for (int i = 0, len = buf.size(); i < len; i++)
			buf.get(i).remove_if(func);

		this.balance_all();
	}

	public int size()
	{
		return size(0, buf.size());
	}

	public int max_size()
	{
		return buf.max_size() * (page_size * page_padding / 100);
	}

	public int max_page_size()
	{
		return page_size * page_padding / 100;
	}

	public int max_page_count()
	{
		return buf.max_size();
	}

	public boolean isempty()
	{
		return size() == 0;
	}

	public int page_count()
	{
		return buf.size();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(128);
		sb.append(size()).append(",").append(page_count).append(",").append(buf._begin).append(",")
				.append(buf._end).append("\n");
		for (int i = 0; i < buf.size(); i++)
			sb.append(i).append(",").append(buf.get(i)).append('\n');
		return sb.toString();
	}

	private index find(E el)
	{
		int p = page_index(el);
		int i = buf.get(p).find(el);

		return new index(p, i, -1);
	}

	private int size(int start_index, int count)
	{
		int size = 0;
		for (int e = start_index + count; start_index < e;)
			size += buf.get(start_index++).size();

		return size;
	}

	private E remove(index i1)
	{
		if (i1.ie < 0)
			return null;

		zpage<E> page = buf.get(i1.ipage);

		E ret = page.remove(i1.ie);
		if (page.isempty() && buf.size() > 1)
			buf.remove(i1.ipage);
		else if (page.size() < lower_page_size())
		{
			for (int i = i1.ipage - 1; i >= 0 && buf.get(i).size() + page.size() < max_page_size(); i--)
			{
				page = page.merge(buf.get(i));
				buf.set(i + 1, page);
				buf.remove(i);
				i1.ipage = i;
			}
			for (int i = i1.ipage + 1; i < buf.size()
					&& buf.get(i).size() + page.size() < max_page_size();)
			{
				page = page.merge(buf.get(i));
				buf.set(i - 1, page);
				buf.remove(i);
			}
		}

		return ret;
	}

	final private int lower_page_size()
	{
		return page_size >> 2;
	}

	private void balance_all()
	{
		int s = 0;
		int e = buf.size() - 1;

		zpage<E> p1, p2;
		for (; s < e;)
		{
			p1 = buf.get(s);
			p2 = buf.get(s + 1);
			if (p1.size() < lower_page_size() && p1.size() + p2.size() < this.max_page_size())
			{
				p1 = p1.merge(p2);
				buf.set(s, p1);
				buf.remove(s + 1);
				e--;
				continue;
			}
			s++;
		}
	}

	private int page_index(E el)
	{
		int i = buf.find(el);
		if (i < 0)
			i = -2 - i;
		if (i < 0)
			i = 0;

		return i;
	}

	private index pos2index(int index)
	{
		return pos2index(start_index, index);
	}

	private index pos2index(index from, int count)
	{
		int i = count;
		int p = from.ipage;
		int t = from.ie + i - buf.get(p).size();
		if (t < 0)
			return new index(p, from.ie + i, from.pos + count);

		i = t;
		p++;
		for (int len = page_count(); p < len; p++)
		{
			t = i - buf.get(p).size();
			if (t < 0)
				return new index(p, i, from.pos + count);
			i = t;
		}

		return new index(p - 1, buf.get(p - 1).size(), from.pos + count);// 最后一个位置,END
	}

	static java.util.Random r = new java.util.Random();

	static int rnd(int n)
	{
		return r.nextInt(n);
	}

	static int rnd2(int n)
	{
		double rand = Math.abs(r.nextDouble());
		rand = Math.pow(rand, 0.3);

		return (int) (rand * 10000);
	}

	static void test1()// 插入测试
	{
		r.setSeed(0);
		int N = 1000;
		int N2 = 100;
		clock t = new clock();
		ztree<Integer> p = new ztree<Integer>(new comparator<Integer>()
		{
			public int compare(Integer o1, Object o2)
			{
				return o1 - (Integer) o2;
			}
		}, false, 10, 20, 50);

		t.reset();
		int max_size = p.max_size();
		for (int n = 0; n < N; n++)
		{
			int x = 0;
			int i = 0;
			for (; x < max_size / 2; x++)
			{
				p.add(x);
			}

			for (; i < N2; i++, x++)
			{
				int sz = p.size();
				p.add(x);
				if (p.size() != sz + 1)
				{
					System.out.println("add size err.");
				}
				p.remove(new Integer(i));
			}

			for (; i < x; i++)
				p.remove(new Integer(i));

			if (p.size() != 0)
			{
				System.out.println("size!=0:" + p.size());
			}

			p.clear();
			for (; x < max_size; x++)
			{
				int sz = p.size();
				p.add(x);
				if (p.size() != sz + 1)
				{
					System.out.println("add size err.");
				}
			}
			p.clear();
		}
		t.pin(p.max_size() + "," + 1L * N * N2 / t.count());

	}

	static void test3()
	{
		r.setSeed(0);
		int N = 10000;
		int N2 = 1;
		clock t = new clock();
		ztree<Integer> p = new ztree<Integer>(new comparator<Integer>()
		{
			public int compare(Integer o1, Object o2)
			{
				return o1 - (Integer) o2;
			}
		}, false, 10, 20);

		t.reset();
		int max_size = p.max_size();
		for (int n = 0; n < N; n++)
		{
			int x = 0;
			for (x = 0; x < max_size; x++)
				p.add(rnd(max_size));

			for (x = 0; x < max_size; x++)
			{
				Integer i = p.remove(0);
				if (i == null)
					break;
				p.add(i);
				Integer i2 = p.get(i);
				if (!i.equals(i2))
				{
					System.out.println("error.");
				}
				int sz = p.size();
				p.remove(i2);
				if (sz - 1 != p.size())
				{
					System.out.println("error size.");
				}

			}

			for (x = 0; x < max_size; x++)
				p.add(rnd(max_size));

			for (x = 0; x < max_size; x++)
			{
				Integer i = p.remove(p.size() - 1);
				if (i == null)
					break;
				p.add(i);
				Integer i2 = p.get(i);
				if (!i.equals(i2))
				{
					System.out.println("error.");
				}
				int sz = p.size();
				p.remove(i2);
				if (sz - 1 != p.size())
				{
					System.out.println("error size.");
				}
			}

			for (x = 0; x < max_size; x++)
				p.add(rnd(max_size));

			for (x = 0; x < max_size; x++)
			{
				Integer i = p.remove(p.size() * 5 / 10);
				if (i == null)
					break;
				p.add(i);
				Integer i2 = p.get(i);
				if (!i.equals(i2))
				{
					System.out.println("error.");
				}
				int sz = p.size();
				p.remove(i2);
				if (sz - 1 != p.size())
				{
					System.out.println("error size.");
				}
			}

			for (x = 0; x < max_size; x++)
				p.add(rnd(max_size));

			for (x = 0; x < max_size; x++)
			{
				Integer i = p.remove(p.size() / 10);
				if (i == null)
					break;
				p.add(i);
				Integer i2 = p.get(i);
				if (!i.equals(i2))
				{
					System.out.println("error.");
				}
				int sz = p.size();
				p.remove(i2);
				if (sz - 1 != p.size())
				{
					System.out.println("error size.");
				}
			}

			for (x = 0; x < max_size; x++)
				p.add(rnd(max_size));

			for (x = 0; x < max_size; x++)
			{
				Integer i = p.remove(p.size() * 9 / 10);
				if (i == null)
					break;
				p.add(i);
				Integer i2 = p.get(i);
				if (!i.equals(i2))
				{
					System.out.println("error.");
				}
				int sz = p.size();
				p.remove(i2);
				if (sz - 1 != p.size())
				{
					System.out.println("error size.");
				}
			}

			for (x = 0; x < max_size; x++)
				p.add(rnd(max_size));

			for (x = 0; x < max_size; x++)
			{
				Integer i = p.remove(p.size() * rnd(100) / 100);
				if (i == null)
					break;
				p.add(i);
				Integer i2 = p.get(i);
				if (!i.equals(i2))
				{
					System.out.println("error.");
				}
				int sz = p.size();
				p.remove(i2);
				if (sz - 1 != p.size())
				{
					System.out.println("error size.");
				}
			}

		}
		t.pin(max_size + "," + 5L * max_size * N * N2 / t.count());

	}

	static void tree_test()
	{
		// test1();
		test3();
	}

	static int C1 = 20000, C2 = 100000;

	static void load()
	{
//		SqlRowSet rs = bean.get(dao_base.class).query(
//				"select rownum,t.stat_id,t.stat_param from tms_run_stat t where rownum<=100000");

		// for (int i = 0; i < C2; i++)
		// {
		// rs.next();
		// st_list.add(new stat_req(rs.getInt("stat_id"), rs
		// .getString("stat_param"), null, 0, null));
		// }
	}

	static void test_hash()
	{
		int N = 1 << 14;
		{
			ztree<String> t = new ztree<String>(new comparator<String>()
			{
				public int compare(String o1, Object o2)
				{
					return o1.compareTo(str_tool.to_str(o2));
				}
			}, false, 10, 1024);
			clock c = new clock();
			for (int i = 0; i < N; i++)
				t.add(i+"user");

			c.pin("tree put");
			for (int i = 0; i < N; i++)
				t.add(i+"user");

			c.pin("tree get");
		}

		{
			HashSet<String> h = new HashSet<String>(1<<14, 0.75f);
			clock c = new clock();
			for (int i = 0; i < N; i++)
				h.add(i+"user");

			c.pin("hash put");

			for (int i = 0; i < N; i++)
				h.contains(i+"user");

			c.pin("hash get");
		}
		{
			TreeSet<String> h = new TreeSet<String>();
			clock c = new clock();
			for (int i = 0; i < N; i++)
				h.add(i+"user");

			c.pin("treeset put");

			for (int i = 0; i < N; i++)
				h.contains(i+"user");

			c.pin("treeset get");
		}
	}

	public static void main(String args[])
	{
		test_hash();
		//tree_test();
		// RunStart.init();
		// load();
		// save_2file("c:\\1.txt");
		// timer t = new timer();
		// test4();
		// t.pin("map");
		// test5();
		// t.pin("btree");
	}
}
