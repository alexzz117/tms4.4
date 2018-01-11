package cn.com.higinet.tms.base.entity.common;

import javax.persistence.Id;

import com.alibaba.fastjson.annotation.JSONField;

import cn.com.higinet.tms.base.constant.Text;
import lombok.Data;

@Data
public class TrafficData {

	@Id
	@Text("txnCode")
	@JSONField(name = "txnCode")
	private String txnCode;

	@Text("chanCode")
	@JSONField(name = "chanCode")
	private String chanCode;

	@Text("sessionId")
	@JSONField(name = "sessionId")
	private String sessionId;

	@Text("txnId")
	@JSONField(name = "txnId")
	private String txnId;

	@Text("txnType")
	@JSONField(name = "txnType")
	private String txnType;

	@Text("userId")
	@JSONField(name = "userId")
	private String userId;

	@Text("deviceId")
	@JSONField(name = "deviceId")
	private String deviceId;

	@Text("score")
	@JSONField(name = "score")
	private Float score;

	@Text("ssss")
	@JSONField(name = "txnCode")
	private String ipAddress;

	@Text("counTryCode")
	@JSONField(name = "counTryCode")
	private String counTryCode;

	@Text("regionCode")
	@JSONField(name = "regionCode")
	private String regionCode;

	@Text("cityCode")
	@JSONField(name = "cityCode")
	private String cityCode;

	@Text("ispCode")
	@JSONField(name = "ispCode")
	private String ispCode;

	@Text("txntTme")
	@JSONField(name = "txntTme")
	private Long txntTme;

	@Text("createTime")
	@JSONField(name = "createTime")
	private Long createTime;

	@Text("finishTime")
	@JSONField(name = "finishTime")
	private Long finishTime;

	@Text("txnStatus")
	@JSONField(name = "txnStatus")
	private Integer txnStatus;

	@Text("batchNo")
	@JSONField(name = "batchNo")
	private String batchNo;

	@Text("hitrulenum")
	@JSONField(name = "hitrulenum")
	private Integer hitrulenum;

	@Text("disposal")
	@JSONField(name = "disposal")
	private String disposal;

	@Text("ssss")
	@JSONField(name = "txnCode")
	private String isCorrect;

	@Text("isMain")
	@JSONField(name = "isMain")
	private String isMain;

	@Text("confirmRisk")
	@JSONField(name = "confirmRisk")
	private String confirmRisk;

	@Text("isEval")
	@JSONField(name = "isEval")
	private String isEval;

	@Text("modelId")
	@JSONField(name = "modelId")
	private String modelId;

	@Text("isModelRisk")
	@JSONField(name = "isModelRisk")
	private String isModelRisk;

	@Text("isRisk")
	@JSONField(name = "isRisk")
	private String isRisk;

	@Text("trigrulenum")
	@JSONField(name = "trigrulenum")
	private Integer trigrulenum;

	@Text("psStatus")
	@JSONField(name = "psStatus")
	private String psStatus;

	@Text("strategy")
	@JSONField(name = "strategy")
	private Long strategy;

	private String text1;
	private String text2;
	private String text3;
	private String text4;
	private String text5;
	private String text6;
	private String text7;
	private String text8;
	private String text9;
	private String text10;
	private String text11;
	private String text12;
	private String text13;
	private String text14;
	private String text15;
	private String text16;
	private String text17;
	private String text18;
	private String text19;
	private String text20;
	private String text21;
	private String text22;
	private String text23;
	private String text24;
	private String text25;
	private String text26;
	private String text27;
	private String text28;
	private String text29;
	private String text30;
	private String text31;
	private String text32;
	private String text33;
	private String text34;
	private String text35;
	private String text36;
	private String text37;
	private String text38;
	private String text39;
	private String text40;

	private Float num1;
	private Float num2;
	private Float num3;
	private Float num4;
	private Float num5;
	private Float num6;
	private Float num7;
	private Float num8;
	private Float num9;
	private Float num10;
	private Float num11;
	private Float num12;
	private Float num13;
	private Float num14;
	private Float num15;
	private Float num16;
	private Float num17;
	private Float num18;
	private Float num19;
	private Float num20;
	private Float num21;
	private Float num22;
	private Float num23;
	private Float num24;
	private Float num25;
	private Float num26;
	private Float num27;
	private Float num28;
	private Float num29;
	private Float num30;
	private Float num31;
	private Float num32;
	private Float num33;
	private Float num34;
	private Float num35;
	private Float num36;
	private Float num37;
	private Float num38;
	private Float num39;
	private Float num40;

}
