package cn.com.higinet.tms35.manage.query.common.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 自定义查询模型实体
 * @author liining
 *
 */
public class Custom {
	/**
     * 页面title设置，为空时，
     * 使用自定义查询TMS_COM_QUERY.QUERY_DESC设置
     * 
     */
	private String pageTitle;
	/**
     * 外层包裹jcl.ui.Box的title设置，
     * 设置此值 的话会在显示信息外包裹一层box
     * 
     */
	private String wrapTitle;
	/**
	 * 宽度设置
	 */
	private String width;
	/**
     * 自定义查询的用途
     *  query:普通查询
     *  entity:实体查询
     * 
     */
	private Custom.Direct direct = Custom.Direct.QUERY;
	/**
	 * 查询显示效果，当direct=query时，默认为list不可变
	 * 当direct=entity时，默认是list，可编辑
	 * list:grid组件列表
	 * detail:详细信息
	 * wirelist:table列表(带边线)
	 * nowirelist:table列表(不带边线)
	 * combttab:组合标签页
	 * combtpage:组合竖状页面
	 */
	private Custom.Effect effect = Custom.Effect.LIST;
	/**
     * 查询语句设置
     * 
     */
	private Stmt stmt;
	/**
     * 数据库表、字段信息设置
     * 
     */
	private Dbdata dbData;
	/**
     * 查询条件定义
     * 
     */
	private Cond cond;
	/**
     * 定义查询工具条中的按钮
     * 
     */
	private Set<Toolbar> toolbar = new LinkedHashSet<Toolbar>();
	/**
     * 查询结果配置
     * 
     */
	private Result result;
	/**
     * 页面处理函数的定义
     * 
     */
	private Script script;
	
	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getWrapTitle() {
		return wrapTitle;
	}

	public void setWrapTitle(String wrapTitle) {
		this.wrapTitle = wrapTitle;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public Custom.Direct getDirect() {
		return direct;
	}
	
	public void setDirect(String direct) {
		this.direct = Custom.Direct.fromValue(direct);
	}

	public Custom.Effect getEffect() {
		return effect;
	}

	public void setEffect(String effect) {
		this.effect = Custom.Effect.fromValue(effect);
	}

	public Stmt getStmt() {
		return stmt;
	}

	public void setStmt(Stmt stmt) {
		this.stmt = stmt;
	}

	public Dbdata getDbData() {
		return dbData;
	}

	public void setDbData(Dbdata dbData) {
		this.dbData = dbData;
	}

	public Cond getCond() {
		return cond;
	}

	public void setCond(Cond cond) {
		this.cond = cond;
	}

	public Set<Toolbar> getToolbar() {
		return toolbar;
	}

	public void setToolbar(Set<Toolbar> toolbar) {
		this.toolbar = toolbar;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public static enum Direct {

        QUERY("query", "普通查询"),
        ENTITY("entity", "实体查询");
        private final String value;
        private final String text;
        private static Map<String, Custom.Direct> constants = new HashMap<String, Custom.Direct>();

        static {
            for (Custom.Direct c: Custom.Direct.values()) {
                constants.put(c.value, c);
            }
        }

        private Direct(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() {
            return this.value;
        }
        
        public String getText(){
        	return this.text;
        }

        public static Custom.Direct fromValue(String value) {
            Custom.Direct constant = constants.get(value);
            /*if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }*/
            return constant;
        }

    }
	
	public static enum Effect {
        LIST("list", "grid组件列表"),
        DETAIL("detail", "详细信息"),
        WIRELIST("wirelist", "有边线的table列表"),
        NOWIRELIST("nowirelist", "无边线的table列表"),
        COMBTTAB("combttab", "组合页面(标签形式)"),
        COMBTPAGE("combtpage", "组合页面(竖状形式)");
        private final String value;
        private final String text;
        private static Map<String, Custom.Effect> constants = new HashMap<String, Custom.Effect>();

        static {
            for (Custom.Effect c: Custom.Effect.values()) {
                constants.put(c.value, c);
            }
        }

        private Effect(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() {
            return this.value;
        }
        
        public String getText(){
        	return this.text;
        }

        public static Custom.Effect fromValue(String value) {
            Custom.Effect constant = constants.get(value);
            /*if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }*/
            return constant;
        }

    }
}
