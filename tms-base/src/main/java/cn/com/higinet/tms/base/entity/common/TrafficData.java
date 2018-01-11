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
	private Double score;

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
	private String text41;
	private String text42;
	private String text43;
	private String text44;
	private String text45;
	private String text46;
	private String text47;
	private String text48;
	private String text49;
	private String text50;

	private Double num1;
	private Double num2;
	private Double num3;
	private Double num4;
	private Double num5;
	private Double num6;
	private Double num7;
	private Double num8;
	private Double num9;
	private Double num10;
	private Double num11;
	private Double num12;
	private Double num13;
	private Double num14;
	private Double num15;
	private Double num16;
	private Double num17;
	private Double num18;
	private Double num19;
	private Double num20;
	private Double num21;
	private Double num22;
	private Double num23;
	private Double num24;
	private Double num25;
	private Double num26;
	private Double num27;
	private Double num28;
	private Double num29;
	private Double num30;
	private Double num31;
	private Double num32;
	private Double num33;
	private Double num34;
	private Double num35;
	private Double num36;
	private Double num37;
	private Double num38;
	private Double num39;
	private Double num40;
	private Double num41;
	private Double num42;
	private Double num43;
	private Double num44;
	private Double num45;
	private Double num46;
	private Double num47;
	private Double num48;
	private Double num49;
	private Double num50;

}
