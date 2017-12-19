package cn.com.higinet.tms.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class StringTest {
	@Test
	public void stringTest() {
		String a = "|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|ceb.cp.netbank.auth.postAuth|^||^||^||^||^||^|0.0|^||^|0|^|0|^||^||^||^||^||^||^||^|0|^||^||^||^||^|843|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|00087774176420151028|^|5044820000|^||^||^||^||^||^||^||^|0|^||^||^|0.0|^||^||^||^||^||^||^||^||^||^||^||^||^|1|^||^|0.0|^||^||^|0|^||^|perlogin2|^||^|1446000274000|^||^||^|0|^|0|^|0|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|463|^||^||^||^|5044820000|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|2|^||^||^||^||^||^||^||^|202.86.185.242|^||^||^|-1.0|^||^||^||^|1596735023|^||^||^||^||^||^||^||^||^||^||^|1505151180943|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|CH01|^||^||^||^|T0501|^||^||^||^||^||^|0.0|^|0.0|^||^|0.0|^|0.0|^||^||^||^||^|1|^||^||^||^||^||^||^||^||^||^||^|1|^||^||^|0|^||^||^||^|0|^|0|^|0|^|1|^|0|^|1|^|0|^|0|^||^||^||^||^||^||^||^||^||^||^||^||^|0|^|0|^||^||^||^|0|^||^||^||^||^||^||^||^||^||^||^|2|^||^||^||^||^||^||^||^||^||^||^||^||^|1|^|0|^|1|^|1|^||^||^||^||^||^|CMCC1|^||^|1422834720000|^||^||^||^||^||^|5044|^||^||^|5|^||^||^||^||^||^||^|0|^||^|0|^|0|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|1505151180801|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|21|^||^||^||^||^||^|netbank.checkPreAuthRules|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|PS01|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|1|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|0.0|^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^||^|netbank_auth|^||^|1|^||^||^||^||^||^||^||^||^||^||^||^||^||^|";
		List<String> s = Lists.newArrayList(Splitter.on("|^|") //设置拆分字符串
				.split(a));
		System.out.println(s.size());
	}

	@Test
	public void stringTest2() {
		String a = "1|^|1|^|0|^||^|1|^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|ceb.cp.netbank.transfer.khzz|^|HTTP.BROWSER|^||^||^||^||^||^||^||^||^||^||^||^|400000.0|^||^||^||^|1|^||^|300000.0|^||^|1|^|843|^||^|0.034482758620689655|^||^|0.0|^||^||^|FP050103|^||^|300000.0|^||^||^||^||^||^||^||^||^||^||^||^|00087774254720151028|^|5044820000|^||^||^|0|^|1596735023|^|2|^||^||^||^||^||^||^|400000.0|^|0|^||^||^|1|^||^|400000.0|^|1|^||^||^|1|^||^|1|^||^||^||^||^||^||^|FP050103|^||^|1446000365000|^||^|0|^||^||^||^||^|0|^||^||^||^||^||^|1|^||^|153751.5741985646|^||^||^||^||^|khzz|^||^||^||^|213333.33333333334|^|khzz|^|300000.0|^|1.40625|^||^|1.0|^|463|^|400000.0|^||^|000877742547|^|5044820000|^|0|^|0.117997232964887|^|836|^|2|^||^|0|^|6214922100266091|^|649|^||^||^|649|^|2|^|0|^|3|^|0|^|0|^|0|^||^|640000.0|^||^|0.081081081081081|^|2|^|0|^|1|^|0|^|2|^|2015-02-02|^||^||^||^||^||^||^|202.86.185.242|^||^||^|-1.0|^||^||^||^|1596735023|^||^||^|WT|^||^||^||^||^||^||^||^|1505151180956|^||^|6214922100266091|^||^||^||^||^||^|1|^|0|^||^||^|门县|^||^|0.0|^||^|0|^|CH01|^|0|^||^|0|^|T020103|^||^||^||^|0|^|0|^||^||^|FP050103|^||^||^||^||^||^||^||^||^||^||^|0|^|0|^|0|^||^||^|0.0|^|1|^||^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|442500.0|^|1156400.0|^|850000.0|^|243314.2857142857|^||^|400000.0|^|7903400.0|^|29|^|283333.3333333333|^|192927.4181034483|^|546152.0|^|3|^||^||^|272531.0344827586|^|0|^||^||^||^||^||^||^||^||^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|2|^||^||^||^||^||^||^||^||^||^||^||^|CMCC1|^||^|1422834720000|^||^||^||^|CNY|^||^|5044|^|8013396654|^||^|3|^|2015-10-28 02:54:05|^||^||^||^||^||^||^||^||^||^|0|^||^|0|^||^||^||^|0|^|0|^||^|FP050103|^|1|^||^|0|^|1|^|1|^|0|^|0|^|1|^|0|^|0|^|0|^|1|^|6212261202024324551|^||^|1156400.0|^|0|^||^||^|837|^|0|^|0|^|5417438.0|^|400000.0|^|200000.0|^|0|^|0|^|154783.94285714286|^|0|^|0|^|546152.0|^|0|^|2|^|0|^|0|^|0|^|870|^|0|^|0|^|0|^|0|^||^||^|450000.0|^|300000.0|^|400000.0|^|300000.0|^||^|200000.0|^|225000.0|^|9|^|400000.0|^|200000.0|^|1505151180822|^|300000.0|^|2|^|0|^|0|^||^|0|^|1679245.0|^||^|1|^|ceb.cp.netbank.transfer.khzz|^|442500.0|^|37|^|247520.0|^|100000.0|^|1237600.0|^|0|^|186582.77777777778|^|496800.0|^|2|^|0|^|0|^|2|^||^|0|^|1|^|9|^|6228450320007509910|^||^|0|^|479600.0|^|214034.0|^|2140340.0|^||^||^||^|1|^||^|2|^|1|^||^||^||^||^||^||^|2|^||^|400000.0|^||^||^|200000.0|^||^||^||^||^||^|7|^||^|1703200.0|^||^|5423856.0|^|1|^|243314.2857142857|^||^||^||^||^||^|33|^|0|^||^|1|^||^|1|^|netbank.checkPreTransactionRules|^|0|^|0|^|0.043478260869565216|^||^|1|^|1|^|0|^|0|^||^|0|^||^||^||^||^||^|442500.0|^|146590.7027027027|^||^||^||^|546152.0|^|2|^||^|450000.0|^||^||^||^|300000.0|^||^|250|^||^||^||^||^|225000.0|^||^||^||^|PS01|^|100000.0|^||^|2|^||^||^|2|^||^||^|0|^|0|^||^|0|^|0|^||^|0|^|0|^||^||^||^||^|179460.552|^||^||^||^|4.4865138E7|^|7|^|300000.0|^|243314.2857142857|^||^|1703200.0|^|3|^|1|^||^|22|^|2|^||^|37|^||^||^|6646250.0|^||^||^|546152.0|^||^|442500.0|^||^|1|^|0|^|400000.0|^|0|^|0.045454545454545456|^||^|1|^||^|1|^||^||^||^||^||^|1|^||^|0.0|^|0|^||^||^|179460.552|^|35|^|4.4865138E7|^|250|^|1000000.0|^|2|^|5423856.0|^|288967.39130434784|^||^||^|648100.0|^|4.4759161E7|^|1703200.0|^|146590.7027027027|^|1140000.0|^|0|^|7|^|2|^|300000.0|^|1.401646467383687|^|232|^|2|^|netbank_transfer_khzz|^||^||^|216033.33333333334|^|0|^|1|^|0.0|^|0|^||^|0|^||^|0|^||^|2|^||^|0|^|";
		List<String> s = Lists.newArrayList(Splitter.on("|^|") //设置拆分字符串
				.split(a));
		System.out.println(s.size());
	}

	@Test
	public void stringTest3() {
		String a = "1|^|1|^|0|^||^|1|^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|ceb.cp.netbank.transfer.khzz|^|HTTP.BROWSER|^||^||^||^||^||^||^||^||^||^||^||^|400000.0|^||^||^||^|1|^||^|300000.0|^||^|1|^|843|^||^|0.034482758620689655|^||^|0.0|^||^||^|FP050103|^||^|300000.0|^||^||^||^||^||^||^||^||^||^||^||^|00087774254720151028|^|5044820000|^||^||^|0|^|1596735023|^|2|^||^||^||^||^||^||^|400000.0|^|0|^||^||^|1|^||^|400000.0|^|1|^||^||^|1|^||^|1|^||^||^||^||^||^||^|FP050103|^||^|1446000365000|^||^|0|^||^||^||^||^|0|^||^||^||^||^||^|1|^||^|153751.5741985646|^||^||^||^||^|khzz|^||^||^||^|213333.33333333334|^|khzz|^|300000.0|^|1.40625|^||^|1.0|^|463|^|400000.0|^||^|000877742547|^|5044820000|^|0|^|0.117997232964887|^|836|^|2|^||^|0|^|6214922100266091|^|649|^||^||^|649|^|2|^|0|^|3|^|0|^|0|^|0|^||^|640000.0|^||^|0.081081081081081|^|2|^|0|^|1|^|0|^|2|^|2015-02-02|^||^||^||^||^||^||^|202.86.185.242|^||^||^|-1.0|^||^||^||^|1596735023|^||^||^|WT|^||^||^||^||^||^||^||^|1505151180956|^||^|6214922100266091|^||^||^||^||^||^|1|^|0|^||^||^|门县|^||^|0.0|^||^|0|^|CH01|^|0|^||^|0|^|T020103|^||^||^||^|0|^|0|^||^||^|FP050103|^||^||^||^||^||^||^||^||^||^||^|0|^|0|^|0|^||^||^|0.0|^|1|^||^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|442500.0|^|1156400.0|^|850000.0|^|243314.2857142857|^||^|400000.0|^|7903400.0|^|29|^|283333.3333333333|^|192927.4181034483|^|546152.0|^|3|^||^||^|272531.0344827586|^|0|^||^||^||^||^||^||^||^||^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|2|^||^||^||^||^||^||^||^||^||^||^||^|CMCC1|^||^|1422834720000|^||^||^||^|CNY|^||^|5044|^|8013396654|^||^|3|^|2015-10-28 02:54:05|^||^||^||^||^||^||^||^||^||^|0|^||^|0|^||^||^||^|0|^|0|^||^|FP050103|^|1|^||^|0|^|1|^|1|^|0|^|0|^|1|^|0|^|0|^|0|^|1|^|6212261202024324551|^||^|1156400.0|^|0|^||^||^|837|^|0|^|0|^|5417438.0|^|400000.0|^|200000.0|^|0|^|0|^|154783.94285714286|^|0|^|0|^|546152.0|^|0|^|2|^|0|^|0|^|0|^|870|^|0|^|0|^|0|^|0|^||^||^|450000.0|^|300000.0|^|400000.0|^|300000.0|^||^|200000.0|^|225000.0|^|9|^|400000.0|^|200000.0|^|1505151180822|^|300000.0|^|2|^|0|^|0|^||^|0|^|1679245.0|^||^|1|^|ceb.cp.netbank.transfer.khzz|^|442500.0|^|37|^|247520.0|^|100000.0|^|1237600.0|^|0|^|186582.77777777778|^|496800.0|^|2|^|0|^|0|^|2|^||^|0|^|1|^|9|^|6228450320007509910|^||^|0|^|479600.0|^|214034.0|^|2140340.0|^||^||^||^|1|^||^|2|^|1|^||^||^||^||^||^||^|2|^||^|400000.0|^||^||^|200000.0|^||^||^||^||^||^|7|^||^|1703200.0|^||^|5423856.0|^|1|^|243314.2857142857|^||^||^||^||^||^|33|^|0|^||^|1|^||^|1|^|netbank.checkPreTransactionRules|^|0|^|0|^|0.043478260869565216|^||^|1|^|1|^|0|^|0|^||^|0|^||^||^||^||^||^|442500.0|^|146590.7027027027|^||^||^||^|546152.0|^|2|^||^|450000.0|^||^||^||^|300000.0|^||^|250|^||^||^||^||^|225000.0|^||^||^||^|PS01|^|100000.0|^||^|2|^||^||^|2|^||^||^|0|^|0|^||^|0|^|0|^||^|0|^|0|^||^||^||^||^|179460.552|^||^||^||^|4.4865138E7|^|7|^|300000.0|^|243314.2857142857|^||^|1703200.0|^|3|^|1|^||^|22|^|2|^||^|37|^||^||^|6646250.0|^||^||^|546152.0|^||^|442500.0|^||^|1|^|0|^|400000.0|^|0|^|0.045454545454545456|^||^|1|^||^|1|^||^||^||^||^||^|1|^||^|0.0|^|0|^||^||^|179460.552|^|35|^|4.4865138E7|^|250|^|1000000.0|^|2|^|5423856.0|^|288967.39130434784|^||^||^|648100.0|^|4.4759161E7|^|1703200.0|^|146590.7027027027|^|1140000.0|^|0|^|7|^|2|^|300000.0|^|1.401646467383687|^|232|^|2|^|netbank_transfer_khzz|^||^||^|216033.33333333334|^|0|^|1|^|0.0|^|0|^||^|0|^||^|0|^||^|2|^||^|0|^|";
		String[] s = StringUtils.split(a, "|^|");
		System.out.println(s.length);
	}

	@Test
	public void write() throws FileNotFoundException, IOException {
		byte[] bytes = "1|^|1|^|0|^||^|1|^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|ceb.cp.netbank.transfer.khzz|^|HTTP.BROWSER|^||^||^||^||^||^||^||^||^||^||^||^|400000.0|^||^||^||^|1|^||^|300000.0|^||^|1|^|843|^||^|0.034482758620689655|^||^|0.0|^||^||^|FP050103|^||^|300000.0|^||^||^||^||^||^||^||^||^||^||^||^|00087774254720151028|^|5044820000|^||^||^|0|^|1596735023|^|2|^||^||^||^||^||^||^|400000.0|^|0|^||^||^|1|^||^|400000.0|^|1|^||^||^|1|^||^|1|^||^||^||^||^||^||^|FP050103|^||^|1446000365000|^||^|0|^||^||^||^||^|0|^||^||^||^||^||^|1|^||^|153751.5741985646|^||^||^||^||^|khzz|^||^||^||^|213333.33333333334|^|khzz|^|300000.0|^|1.40625|^||^|1.0|^|463|^|400000.0|^||^|000877742547|^|5044820000|^|0|^|0.117997232964887|^|836|^|2|^||^|0|^|6214922100266091|^|649|^||^||^|649|^|2|^|0|^|3|^|0|^|0|^|0|^||^|640000.0|^||^|0.081081081081081|^|2|^|0|^|1|^|0|^|2|^|2015-02-02|^||^||^||^||^||^||^|202.86.185.242|^||^||^|-1.0|^||^||^||^|1596735023|^||^||^|WT|^||^||^||^||^||^||^||^|1505151180956|^||^|6214922100266091|^||^||^||^||^||^|1|^|0|^||^||^|门县|^||^|0.0|^||^|0|^|CH01|^|0|^||^|0|^|T020103|^||^||^||^|0|^|0|^||^||^|FP050103|^||^||^||^||^||^||^||^||^||^||^|0|^|0|^|0|^||^||^|0.0|^|1|^||^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|442500.0|^|1156400.0|^|850000.0|^|243314.2857142857|^||^|400000.0|^|7903400.0|^|29|^|283333.3333333333|^|192927.4181034483|^|546152.0|^|3|^||^||^|272531.0344827586|^|0|^||^||^||^||^||^||^||^||^||^||^||^||^|0|^||^||^||^||^||^||^||^||^||^|2|^||^||^||^||^||^||^||^||^||^||^||^|CMCC1|^||^|1422834720000|^||^||^||^|CNY|^||^|5044|^|8013396654|^||^|3|^|2015-10-28 02:54:05|^||^||^||^||^||^||^||^||^||^|0|^||^|0|^||^||^||^|0|^|0|^||^|FP050103|^|1|^||^|0|^|1|^|1|^|0|^|0|^|1|^|0|^|0|^|0|^|1|^|6212261202024324551|^||^|1156400.0|^|0|^||^||^|837|^|0|^|0|^|5417438.0|^|400000.0|^|200000.0|^|0|^|0|^|154783.94285714286|^|0|^|0|^|546152.0|^|0|^|2|^|0|^|0|^|0|^|870|^|0|^|0|^|0|^|0|^||^||^|450000.0|^|300000.0|^|400000.0|^|300000.0|^||^|200000.0|^|225000.0|^|9|^|400000.0|^|200000.0|^|1505151180822|^|300000.0|^|2|^|0|^|0|^||^|0|^|1679245.0|^||^|1|^|ceb.cp.netbank.transfer.khzz|^|442500.0|^|37|^|247520.0|^|100000.0|^|1237600.0|^|0|^|186582.77777777778|^|496800.0|^|2|^|0|^|0|^|2|^||^|0|^|1|^|9|^|6228450320007509910|^||^|0|^|479600.0|^|214034.0|^|2140340.0|^||^||^||^|1|^||^|2|^|1|^||^||^||^||^||^||^|2|^||^|400000.0|^||^||^|200000.0|^||^||^||^||^||^|7|^||^|1703200.0|^||^|5423856.0|^|1|^|243314.2857142857|^||^||^||^||^||^|33|^|0|^||^|1|^||^|1|^|netbank.checkPreTransactionRules|^|0|^|0|^|0.043478260869565216|^||^|1|^|1|^|0|^|0|^||^|0|^||^||^||^||^||^|442500.0|^|146590.7027027027|^||^||^||^|546152.0|^|2|^||^|450000.0|^||^||^||^|300000.0|^||^|250|^||^||^||^||^|225000.0|^||^||^||^|PS01|^|100000.0|^||^|2|^||^||^|2|^||^||^|0|^|0|^||^|0|^|0|^||^|0|^|0|^||^||^||^||^|179460.552|^||^||^||^|4.4865138E7|^|7|^|300000.0|^|243314.2857142857|^||^|1703200.0|^|3|^|1|^||^|22|^|2|^||^|37|^||^||^|6646250.0|^||^||^|546152.0|^||^|442500.0|^||^|1|^|0|^|400000.0|^|0|^|0.045454545454545456|^||^|1|^||^|1|^||^||^||^||^||^|1|^||^|0.0|^|0|^||^||^|179460.552|^|35|^|4.4865138E7|^|250|^|1000000.0|^|2|^|5423856.0|^|288967.39130434784|^||^||^|648100.0|^|4.4759161E7|^|1703200.0|^|146590.7027027027|^|1140000.0|^|0|^|7|^|2|^|300000.0|^|1.401646467383687|^|232|^|2|^|netbank_transfer_khzz|^||^||^|216033.33333333334|^|0|^|1|^|0.0|^|0|^||^|0|^||^|0|^||^|2|^||^|0|^|"
				.getBytes();
		File f = new File("I:\\testFile.lstn");
		if (f.exists()) {
			f.delete();
		}
		int len = 1000000;
		int i = 0;
		try (FileOutputStream fo = new FileOutputStream(f);) {
			while (i < len) {
				for (int x = 0; x < 2000; x++) {
					i++;
					fo.write(bytes);
					if (i != len - 1) {
						fo.write(System.lineSeparator().getBytes());
					}
				}
				fo.flush();
				System.out.println("i=" + i);
			}
		}
	}
}
