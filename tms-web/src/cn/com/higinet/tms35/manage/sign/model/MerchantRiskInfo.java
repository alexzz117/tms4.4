package cn.com.higinet.tms35.manage.sign.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import cn.com.higinet.tms35.manage.sign.common.jaxb.JaxbDateAdapter;

/**
 * 
 * @author lining
 * @description 商户风险信息类
 */
@XmlRootElement(name = "PosMerchantsriskinfo")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "map", propOrder = { "mrchNo", "mrchType", "category", "operatingTimeLimit", "registeredCapital", "property", "popularity", "onlineEstimate", "isListedCompany", "isVisited", "haveNegativeMessage", "mrchFrom", "bigChangeInShortTime", "bornOperatingPlaceEqual", "obRegPlaceEqual",
		"busLicenceScopeEqual", "moneyLaunderingRisk", "embezzleFundsRisk", "mrchRiskEvaluate", "createId", "createTime", "updateId", "updateTime" })
public class MerchantRiskInfo {
	@XmlAttribute(name = "type")
	private final String type = "map";// 非xml节点

	private String mrchNo;// 商户号
	private String mrchType;// 商户类型:顺手付商户(SYPAY),POS商户(POS)
	private String category;// 商户类别(引用数据字典:tms.sf.dealer_type)
	private String operatingTimeLimit;// 经营年限
	private String registeredCapital;// 注册资本
	private String property;// 商户性质:国营(STATE-OPERATED),私营(PRIVATELY-OPERATED),合资(JOINT-VENTURE),股份制(JOINT-STOCK),个体工商户(INDIVIDUALLY-OWNED),其他(OTHER)
	private String popularity;// 知名度:非常知名(VERY_FAMOUS),知名(FAMOUS),一般(COMMONLY)
	private String onlineEstimate;// 线上评价:优(GREAT),良(GOOD),一般(COMMONLY),差(BAD)
	private String isListedCompany;// 是否上市公司:是(Y),上市公司子公司(LISTED_CHILD),否(N)
	private String isVisited;// 是否去公司拜访:是(Y),否(N)
	private String haveNegativeMessage;// 负面信息:无(NO),行业存在负面信息(INDUSTRY_HAVE),公司存在负面信息(COMPANY_HAVE)
	private String mrchFrom;// 商户来源:本机构发展(INSTITUTION_DEVELOPE),其他收单机构推荐(ACQUIRER_RECOMMEND),第三方推荐(THIRD_PARTY_RECOMMEND),主动上门要求(INITIATIVE_VISIT)
	private String bigChangeInShortTime;// 短期内有重大信息变更:无变更(NO_CHANGE),基本信息变更(BASIC_CHANGE),法人或股东信息变更(LEGAL_OR_STOCKHOLDER_CHANGE)
	private String bornOperatingPlaceEqual;// 法人出生地与经营地一致:是(Y),否(N)
	private String obRegPlaceEqual;// 开户行所在地与注册所在地一致:是(Y),否(N)
	private String busLicenceScopeEqual;// 经营范围与营业执照相符:是(Y),否(N)
	private String moneyLaunderingRisk;// 洗钱风险:高(HIGH),中(MIDDLE),低(LOW)
	private String embezzleFundsRisk;// 资金挪用风险:高(HIGH),中(MIDDLE),低(LOW)
	private String mrchRiskEvaluate;// 商户风险评价
	private String createId;// 创建人
	private Date createTime;// 创建时间
	private String updateId;// 更新人
	private Date updateTime;// 更新时间

	@XmlElement(name = "mrch_no")
	public String getMrchNo() {
		return mrchNo;
	}

	public void setMrchNo(String mrchNo) {
		this.mrchNo = mrchNo;
	}

	@XmlElement(name = "mrch_type")
	public String getMrchType() {
		return mrchType;
	}

	public void setMrchType(String mrchType) {
		this.mrchType = mrchType;
	}

	@XmlElement(name = "category")
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@XmlElement(name = "operating_time_limit")
	public String getOperatingTimeLimit() {
		return operatingTimeLimit;
	}

	public void setOperatingTimeLimit(String operatingTimeLimit) {
		this.operatingTimeLimit = operatingTimeLimit;
	}

	@XmlElement(name = "registered_capital")
	public String getRegisteredCapital() {
		return registeredCapital;
	}

	public void setRegisteredCapital(String registeredCapital) {
		this.registeredCapital = registeredCapital;
	}

	@XmlElement(name = "property")
	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	@XmlElement(name = "popularity")
	public String getPopularity() {
		return popularity;
	}

	public void setPopularity(String popularity) {
		this.popularity = popularity;
	}

	@XmlElement(name = "online_estimate")
	public String getOnlineEstimate() {
		return onlineEstimate;
	}

	public void setOnlineEstimate(String onlineEstimate) {
		this.onlineEstimate = onlineEstimate;
	}

	@XmlElement(name = "is_listed_company")
	public String getIsListedCompany() {
		return isListedCompany;
	}

	public void setIsListedCompany(String isListedCompany) {
		this.isListedCompany = isListedCompany;
	}

	@XmlElement(name = "is_visited")
	public String getIsVisited() {
		return isVisited;
	}

	public void setIsVisited(String isVisited) {
		this.isVisited = isVisited;
	}

	@XmlElement(name = "have_negative_message")
	public String getHaveNegativeMessage() {
		return haveNegativeMessage;
	}

	public void setHaveNegativeMessage(String haveNegativeMessage) {
		this.haveNegativeMessage = haveNegativeMessage;
	}

	@XmlElement(name = "mrch_from")
	public String getMrchFrom() {
		return mrchFrom;
	}

	public void setMrchFrom(String mrchFrom) {
		this.mrchFrom = mrchFrom;
	}

	@XmlElement(name = "big_change_in_short_time")
	public String getBigChangeInShortTime() {
		return bigChangeInShortTime;
	}

	public void setBigChangeInShortTime(String bigChangeInShortTime) {
		this.bigChangeInShortTime = bigChangeInShortTime;
	}

	@XmlElement(name = "born_operating_place_equal")
	public String getBornOperatingPlaceEqual() {
		return bornOperatingPlaceEqual;
	}

	public void setBornOperatingPlaceEqual(String bornOperatingPlaceEqual) {
		this.bornOperatingPlaceEqual = bornOperatingPlaceEqual;
	}

	@XmlElement(name = "ob_reg_place_equal")
	public String getObRegPlaceEqual() {
		return obRegPlaceEqual;
	}

	public void setObRegPlaceEqual(String obRegPlaceEqual) {
		this.obRegPlaceEqual = obRegPlaceEqual;
	}

	@XmlElement(name = "bus_licence_scope_equal")
	public String getBusLicenceScopeEqual() {
		return busLicenceScopeEqual;
	}

	public void setBusLicenceScopeEqual(String busLicenceScopeEqual) {
		this.busLicenceScopeEqual = busLicenceScopeEqual;
	}

	@XmlElement(name = "money_laundering_risk")
	public String getMoneyLaunderingRisk() {
		return moneyLaunderingRisk;
	}

	public void setMoneyLaunderingRisk(String moneyLaunderingRisk) {
		this.moneyLaunderingRisk = moneyLaunderingRisk;
	}

	@XmlElement(name = "embezzle_funds_risk")
	public String getEmbezzleFundsRisk() {
		return embezzleFundsRisk;
	}

	public void setEmbezzleFundsRisk(String embezzleFundsRisk) {
		this.embezzleFundsRisk = embezzleFundsRisk;
	}

	@XmlElement(name = "mrch_risk_evaluate")
	public String getMrchRiskEvaluate() {
		return mrchRiskEvaluate;
	}

	public void setMrchRiskEvaluate(String mrchRiskEvaluate) {
		this.mrchRiskEvaluate = mrchRiskEvaluate;
	}

	@XmlElement(name = "create_id")
	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId;
	}

	@XmlElement(name = "create_time")
	@XmlJavaTypeAdapter(JaxbDateAdapter.class)
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@XmlElement(name = "update_id")
	public String getUpdateId() {
		return updateId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}

	@XmlElement(name = "update_time")
	@XmlJavaTypeAdapter(JaxbDateAdapter.class)
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}