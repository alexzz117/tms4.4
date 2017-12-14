package cn.com.higinet.tms.manager.modules.query.model;

import java.util.List;

public class QueryCond {
	/**
	 * 查询条件区域，每行显示几个查询条件
	 */
	private int line_conds;

	/**
	 * 初始页面是否显示查询区域
	 */
	private boolean qfrom_display;

	/**
	 * 查询条件组合表达式
	 */
	private String expression;

	/**
	 * 查询条件配置项
	 */
	private List<Cond> conds;

	public int getLine_conds() {
		return line_conds;
	}

	public void setLine_conds(int line_conds) {
		this.line_conds = line_conds;
	}

	public boolean isQfrom_display() {
		return qfrom_display;
	}

	public void setQfrom_display(boolean qfrom_display) {
		this.qfrom_display = qfrom_display;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public List<Cond> getConds() {
		return conds;
	}

	public void setConds(List<Cond> conds) {
		this.conds = conds;
	}

	public class Cond {
		/**
		 * 查询条件统一名称
		 * 覆盖子查询条件名称
		 */
		private String name;
		/**
		 * 查询条件的标题
		 */
		private String title;
		/**
		 * 查询条件存在多个控件时，
		 * 控件之间的连接符
		 */
		private String join;
		/**
		 * 查询条件存在多个控件时，
		 * 控件之间值的组合表达式
		 * 无表达式，则需要全部填写或都不填写
		 */
		private String expression;

		/**
		 * 子查询条件，即当前查询条件中控件的配置项
		 */
		private List<SubCond> subconds;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getJoin() {
			return join;
		}

		public void setJoin(String join) {
			this.join = join;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

		public List<SubCond> getSubconds() {
			return subconds;
		}

		public void setSubconds(List<SubCond> subconds) {
			this.subconds = subconds;
		}

		public class SubCond {
			/**
			 * 页面控件的id
			 * 例如：<input id="">
			 */
			private String id;

			/**
			 * 页面控件的name
			 * 例如：<input name="">
			 */
			private String name;

			/**
			 * 引用的数据字段名
			 */
			private String rec_filed;

			/**
			 * sql语句中所用的数据字段名
			 */
			private String fd_name;

			/**
			 * 页面控件的title
			 * 例如：<input title="">
			 */
			private String title;

			/**
			 * 查询条件的操作符
			 * 用于自动组装SQL时查询条件的拼装
			 */
			private String oper;

			/**
			 * 查询条件的当前值
			 * 由前台传递过来的值
			 */
			private String current_value;

			/**
			 * 查询条件的默认值
			 * 初始化页面时，设置控件的值
			 */
			private String default_value;

			/**
			 * 控件的脚本触发
			 * 例如：onclick=jsmethod(this)
			 */
			private String script_excite;

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getRec_filed() {
				return rec_filed;
			}

			public void setRec_filed(String rec_filed) {
				this.rec_filed = rec_filed;
			}

			public String getFd_name() {
				return fd_name;
			}

			public void setFd_name(String fd_name) {
				this.fd_name = fd_name;
			}

			public String getTitle() {
				return title;
			}

			public void setTitle(String title) {
				this.title = title;
			}

			public String getOper() {
				return oper;
			}

			public void setOper(String oper) {
				this.oper = oper;
			}

			public String getCurrent_value() {
				return current_value;
			}

			public void setCurrent_value(String current_value) {
				this.current_value = current_value;
			}

			public String getDefault_value() {
				return default_value;
			}

			public void setDefault_value(String default_value) {
				this.default_value = default_value;
			}

			public String getScript_excite() {
				return script_excite;
			}

			public void setScript_excite(String script_excite) {
				this.script_excite = script_excite;
			}
		}
	}
}
