package cn.com.higinet.tms.base.entity.offline;

import cn.com.higinet.tms.base.entity.common.EntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class cmc_code extends EntityBase<cmc_code> {

	private static final long serialVersionUID = 1L;
	private String code_id; //编码ID
	private String category_id;//编码分类编号
	private String code_key; //代码健
	private String code_value; //代码值
	private Long onum; //顺序
	private String info; //描述信息
}
