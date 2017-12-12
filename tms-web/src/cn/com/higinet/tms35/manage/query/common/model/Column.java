
package cn.com.higinet.tms35.manage.query.common.model;

import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrWebException;

/**
 * 查询结果字段
 * @author liining
 *
 */
public class Column {
	/**
	 * 数据字段名(必填)
	 * 格式{tab}.{field}、{field}
	 */
	private String fdName;
	/**
	 * 拼写sql时的字段名
	 * 为空时，默认使用fdName
	 */
	private String csName;
	/**
	 * 当前列显示的中文名
	 */
	private String name;
	/**
	 * 当前列宽
	 */
	private int width;
	/**
	 * 排序
	 */
	private Column.Order order;
	/**
	 * 当前列是否显示
	 * 默认为true
	 */
	private boolean show = true;
	/**
	 * 链接打开方式
	 * pop_layer:弹出层
	 * pop_page:弹出页面(默认)
	 * embed_layer:内嵌层
	 * embed_page:内嵌页面
	 */
	private Column.Open open = Column.Open.POP_PAGE;
	/**
	 * 查询结果处理
	 */
	private Handle handle;
	
	public String getFdName() {
		return fdName;
	}

	public void setFdName(String fdName) {
		if(StringUtil.isBlank(fdName)){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[result]->[columns]节点下的[fdName]不能为空");
		}
		this.fdName = fdName;
	}

	public String getCsName() {
		return csName;
	}

	public void setCsName(String csName) {
		this.csName = csName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public Column.Order getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = Column.Order.fromValue(order);
	}

	public Column.Open getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = Column.Open.fromValue(open);
	}

	public Handle getHandle() {
		return handle;
	}

	public void setHandle(Handle handle) {
		this.handle = handle;
	}

	public static enum Open {
		POP_LAYER("pop_layer", "弹出层"), POP_PAGE("pop_page", "弹出页面"), EMBED_LAYER("embed_layer", "内嵌层"), EMBED_PAGE("embed_page", "内嵌页面");
		
		private final String value;
		private final String text;
        private static Map<String, Column.Open> constants = new HashMap<String, Column.Open>();

        static {
            for (Column.Open c: Column.Open.values()) {
                constants.put(c.value, c);
            }
        }

        private Open(String value, String text) {
            this.value = value;
            this.text = text;
        }
        
        public String getValue(){
        	return this.value;
        }
        
        public String getText(){
        	return this.text;
        }

        public static Column.Open fromValue(String value) {
        	Column.Open constant = constants.get(value);
            /*if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }*/
        	return constant;
        }
	}
	
	public static enum Order {
		ASC("asc", "升序"), DESC("desc", "降序");
		
		private final String value;
		private final String text;
        private static Map<String, Column.Order> constants = new HashMap<String, Column.Order>();

        static {
            for (Column.Order c: Column.Order.values()) {
                constants.put(c.value, c);
            }
        }

        private Order(String value, String text) {
            this.value = value;
            this.text = text;
        }
        
        public String getValue(){
        	return this.value;
        }
        
        public String getText(){
        	return this.text;
        }

        public static Column.Order fromValue(String value) {
        	Column.Order constant = constants.get(value);
        	return constant;
        }
	}
}