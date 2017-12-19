package cn.com.higinet.tms.engine.core.cache;

import java.lang.reflect.Field;
import java.util.Comparator;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;

public class class_field
{
	static class field
	{
		field(String n)
		{
			name = n;
		}

		public field(String name2, Field field2)
		{
			name=name2;
			field=field2;
			// TODO Auto-generated constructor stub
		}

		String name;
		Field field;
	}

	static final java.util.Comparator<field> comp = new Comparator<field>()
	{
		public int compare(field o1, field o2)
		{
			return o1.name.compareTo(o2.name);
		}
	};

	linear<field> m_field = new linear<field>(comp);

	public void add_field(Field f)
	{
		m_field.insert(new field(f.getName(), f));
	}

	public void set(Object o, String fd_name, String s)
	{
		int index = m_field.index(new field(fd_name));
		if (index < 0)
			throw new tms_exception("class:" + o.getClass().getCanonicalName() + "have not field["
					+ fd_name + "]");

		try
		{
			m_field.get_uncheck(index).field.set(o, s);
		}
		catch (IllegalArgumentException e)
		{
			throw new tms_exception(e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			throw new tms_exception(e.getMessage());
		}
	}

	public String get(Object o, String fd_name)
	{
		int index = m_field.index(new field(fd_name));
		if (index < 0)
			throw new tms_exception("class:" + o.getClass().getCanonicalName() + "have not field["
					+ fd_name + "]");

		try
		{
			return str_tool.to_str(m_field.get_uncheck(index).field.get(o));
		}
		catch (IllegalArgumentException e)
		{
			throw new tms_exception(e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			throw new tms_exception(e.getMessage());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}
}
