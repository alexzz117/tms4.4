package cn.com.higinet.tms.base.entity.offline;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.alibaba.fastjson.annotation.JSONField;

import cn.com.higinet.tms.base.constant.Text;
import cn.com.higinet.tms.base.entity.common.EntityBase;
import lombok.Data;

@Entity
@Data
public class cmc_code implements EntityBase<cmc_code> {
	private static final long serialVersionUID = 1L;

	@Id
	@Text("编码ID")
	@JSONField(name = "codeid")
	@Column(name = "code_id")
	private String codeId;

	@Text("")
	@JSONField(name = "categoryid")
	@Column(name = "category_id")
	private String categoryId;

	@Text("代码健")
	@JSONField(name = "codekey")
	@Column(name = "code_key")
	private String codeKey;

	@Text("代码值")
	@JSONField(name = "codevalue")
	@Column(name = "code_value")
	private String codeValue;

	@Text("顺序")
	@JSONField(name = "onum")
	@Column(name = "onum")
	private Long onum;

	@Text("描述信息")
	@JSONField(name = "info")
	@Column(name = "info")
	private String info;
}
