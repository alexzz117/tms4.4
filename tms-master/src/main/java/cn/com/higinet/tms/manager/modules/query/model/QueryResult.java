package cn.com.higinet.tms.manager.modules.query.model;

import java.util.List;

public class QueryResult {
	/**
	 * 页面初始化脚本，执行grid之前
	 */
	private String init_script;
	/**
	 * 页面查询结果字段配置数据
	 */
	private List<Column> columns;
	/**
	 * 查询结果初始化grid的参数名
	 */
	private String ds_params;
	/**
	 * 查询结果回调脚本函数
	 */
	private String ds_callback;
	/**
	 * 查询结果是否去除重复数据
	 */
	private boolean distinct_display;
	/**
	 * 显示方式
	 * list|detail
	 * list：列表展示
	 * detail：详细信息展示
	 */
	private String display_type;

	/**
	 * 查询结果是否分页
	 */
	private boolean paging;
	/**
	 * 分页条数
	 */
	private int page_size;

	public String getInit_script() {
		return init_script;
	}

	public void setInit_script(String init_script) {
		this.init_script = init_script;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public String getDs_params() {
		return ds_params;
	}

	public void setDs_params(String ds_params) {
		this.ds_params = ds_params;
	}

	public String getDs_callback() {
		return ds_callback;
	}

	public void setDs_callback(String ds_callback) {
		this.ds_callback = ds_callback;
	}

	public boolean isDistinct_display() {
		return distinct_display;
	}

	public void setDistinct_display(boolean distinct_display) {
		this.distinct_display = distinct_display;
	}

	public String getDisplay_type() {
		return display_type;
	}

	public void setDisplay_type(String display_type) {
		this.display_type = display_type;
	}

	public boolean isPaging() {
		return paging;
	}

	public void setPaging(boolean paging) {
		this.paging = paging;
	}

	public int getPage_size() {
		return page_size;
	}

	public void setPage_size(int page_size) {
		this.page_size = page_size;
	}

	public class Column {
		/**
		 * 引用数据字段名
		 */
		private String rec_filed;
		/**
		 * 数据字段
		 */
		private String fd_name;
		/**
		 * 前台显示时的列宽
		 */
		private int width;
		/**
		 * 结果列是否隐藏
		 */
		private boolean hide;
		/**
		 * 根据此字段排序
		 * asc、desc
		 */
		private String orderby;
		/**
		 * 链接打开方式,0为弹出，1为页面内嵌打开
		 */
		private int open_link;
		/**
		 * 此字段的处置配置
		 */
		private Handle handle;

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

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public boolean isHide() {
			return hide;
		}

		public void setHide(boolean hide) {
			this.hide = hide;
		}

		public String getOrderby() {
			return orderby;
		}

		public void setOrderby(String orderby) {
			this.orderby = orderby;
		}

		public int getOpen_link() {
			return open_link;
		}

		public void setOpen_link(int open_link) {
			this.open_link = open_link;
		}

		public Handle getHandle() {
			return handle;
		}

		public void setHandle(Handle handle) {
			this.handle = handle;
		}

		public class Handle {
			/**
			 * 查询sql中的处置语句
			 */
			private String db;

			/**
			 * 前台页面脚本处置语句
			 */
			private String script;

			/**
			 * java方法处置语句
			 */
			private String method;

			public String getDb() {
				return db;
			}

			public void setDb(String db) {
				this.db = db;
			}

			public String getScript() {
				return script;
			}

			public void setScript(String script) {
				this.script = script;
			}

			public String getMethod() {
				return method;
			}

			public void setMethod(String method) {
				this.method = method;
			}
		}
	}
}
