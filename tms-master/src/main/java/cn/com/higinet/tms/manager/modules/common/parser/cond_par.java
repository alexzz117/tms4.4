package cn.com.higinet.tms.manager.modules.common.parser;

/****************************************************************************
 *                     U N R E G I S T E R E D   C O P Y
 *
 * You are on day 1 of your 30 day trial period.
 *
 * This file was produced by an UNREGISTERED COPY of Parser Generator. It is
 * for evaluation purposes only. If you continue to use Parser Generator 30
 * days after installation then you are required to purchase a license. For
 * more information see the online help or go to the Bumble-Bee Software
 * homepage at:
 *
 * http://www.bumblebeesoftware.com
 *
 * This notice must remain present in the file. It cannot be removed.
 ****************************************************************************/

/****************************************************************************
 * cond_par.java
 * Java source file generated from cond_par.y.
 *
 * Date: 12/10/10
 * Time: 12:02:32
 *
 * AYACC Version: 2.07
 ****************************************************************************/

// line 1 ".\\cond_par.y"

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;

import cn.com.higinet.tms35.core.cond.yl.yyfparser;


// line 39 "cond_par.java"

/////////////////////////////////////////////////////////////////////////////
// cond_par

public class cond_par extends yyfparser {
	// line 16 ".\\cond_par.y"

	cond_lex lexer; // the lexical analyser
	String[] conds;

	// line 52 "cond_par.java"
	public cond_par() {
		yytables();
		// line 23 ".\\cond_par.y"

		// line 58 "cond_par.java"
	}

	public class YYSTYPE extends yyattribute {
		// line 26 ".\\cond_par.y"

		public String value;

		// copy method
		public void yycopy(yyattribute source, boolean move) {
			YYSTYPE yy = (YYSTYPE) source;
			value = yy.value;
			if (move) {
				yy.value = null;
			}
		}

		// line 75 "cond_par.java"
	}

	public static final int INTEGER = 65537;

	protected final YYSTYPE yyattribute(int index) {
		YYSTYPE attribute = (YYSTYPE) yyattributestackref[yytop + index];
		return attribute;
	}

	protected final yyattribute yynewattribute() {
		return new YYSTYPE();
	}

	protected final YYSTYPE[] yyinitdebug(int count) {
		YYSTYPE a[] = new YYSTYPE[count];
		for (int i = 0; i < count; i++) {
			a[i] = (YYSTYPE) yyattributestackref[yytop + i - (count - 1)];
		}
		return a;
	}

	protected static yyftables yytables = null;

	public final void yyaction(int action) {
		switch (action) {
		case 0: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(2);
			}
			// line 53 ".\\cond_par.y"
			((YYSTYPE) yyvalref).value = yyattribute(1 - 1).value;
			exit(0, "");
			// line 108 "cond_par.java"
		}
			break;
		case 1: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(3);
			}
			// line 54 ".\\cond_par.y"
			((YYSTYPE) yyvalref).value = yyattribute(1 - 2).value;
			exit(0, "");
			// line 119 "cond_par.java"
		}
			break;
		case 2: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(3);
			}
			// line 55 ".\\cond_par.y"
			((YYSTYPE) yyvalref).value = yyattribute(1 - 2).value;
			exit(0, "");
			// line 130 "cond_par.java"
		}
			break;
		case 3: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(3);
			}
			// line 56 ".\\cond_par.y"
			exit(-1, "���ʽ����");
			// line 141 "cond_par.java"
		}
			break;
		case 4: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(4);
			}
			// line 60 ".\\cond_par.y"

			if (yyattribute(1 - 3).value == null
					&& yyattribute(3 - 3).value == null)
				((YYSTYPE) yyvalref).value = null;
			else if (yyattribute(1 - 3).value == null)
				((YYSTYPE) yyvalref).value = yyattribute(3 - 3).value;

			else if (yyattribute(3 - 3).value == null)
				((YYSTYPE) yyvalref).value = yyattribute(1 - 3).value;
			else
				((YYSTYPE) yyvalref).value = factor(yyattribute(1 - 3).value
						+ " or " + yyattribute(3 - 3).value);

			// line 162 "cond_par.java"
		}
			break;
		case 5: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(4);
			}
			// line 71 ".\\cond_par.y"

			if (yyattribute(1 - 3).value == null
					&& yyattribute(3 - 3).value == null)
				((YYSTYPE) yyvalref).value = null;

			else if (yyattribute(1 - 3).value == null)
				((YYSTYPE) yyvalref).value = yyattribute(3 - 3).value;

			else if (yyattribute(3 - 3).value == null)
				((YYSTYPE) yyvalref).value = yyattribute(1 - 3).value;
			else
				((YYSTYPE) yyvalref).value = factor(yyattribute(1 - 3).value
						+ " and " + yyattribute(3 - 3).value);

			// line 184 "cond_par.java"
		}
			break;
		case 6: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(3);
			}
			// line 83 ".\\cond_par.y"

			if (yyattribute(2 - 2).value == null)
				((YYSTYPE) yyvalref).value = null;
			else
				((YYSTYPE) yyvalref).value = "not" + yyattribute(2 - 2).value
						+ "";

			// line 200 "cond_par.java"
		}
			break;
		case 7: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(4);
			}
			// line 89 ".\\cond_par.y"

			if (yyattribute(2 - 3).value == null)
				((YYSTYPE) yyvalref).value = null;
			else
				((YYSTYPE) yyvalref).value = factor(yyattribute(2 - 3).value);

			// line 216 "cond_par.java"
		}
			break;
		case 8: {
			YYSTYPE yya[];
			if (YYDEBUG) {
				yya = yyinitdebug(2);
			}
			// line 96 ".\\cond_par.y"

			int idx = Integer.parseInt(yyattribute(1 - 1).value);
			if (idx > conds.length)
				exit(-1, "所提供的条件数组比表达式中出现的少,相关提示：'"
						+ yyattribute(1 - 1).value + "'");
			else if (conds[idx - 1] == null)
				((YYSTYPE) yyvalref).value = null;
			else {
				((YYSTYPE) yyvalref).value = factor(conds[idx - 1]);
			}

			// line 237 "cond_par.java"
		}
			break;
		default:
			break;
		}
	}

	protected final void yytables() {
		yysstack_size = 100;
		yystack_max = 0;

		yycreatetables();
		yysymbol = yytables.yysymbol;
		yyrule = yytables.yyrule;
		yyreduction = yytables.yyreduction;
		yytokenaction = yytables.yytokenaction;
		yystateaction = yytables.yystateaction;
		yynontermgoto = yytables.yynontermgoto;
		yystategoto = yytables.yystategoto;
		yydestructorref = yytables.yydestructorref;
		yytokendestref = yytables.yytokendestref;
		yytokendestbaseref = yytables.yytokendestbaseref;
	}

	public static synchronized final void yycreatetables() {
		if (yytables == null) {
			yytables = new yyftables();

			if (YYDEBUG) {
				final yysymbol symbol[] = { new yysymbol("$end", 0),
						new yysymbol("\'\\n\'", 10),
						new yysymbol("\'\\r\'", 13), new yysymbol("\'!\'", 33),
						new yysymbol("\'(\'", 40), new yysymbol("\')\'", 41),
						new yysymbol("\'*\'", 42), new yysymbol("\'+\'", 43),
						new yysymbol("error", 65536),
						new yysymbol("INTEGER", 65537), new yysymbol(null, 0) };
				yytables.yysymbol = symbol;

				final String rule[] = { "$accept: line", "line: expr",
						"line: expr \'\\r\'", "line: expr \'\\n\'",
						"line: error \'\\n\'", "expr: expr \'+\' expr",
						"expr: expr \'*\' expr", "expr: \'!\' expr",
						"expr: \'(\' expr \')\'", "expr: INTEGER" };
				yytables.yyrule = rule;
			}

			final yyreduction reduction[] = { new yyreduction(0, 1, -1),
					new yyreduction(1, 1, 0), new yyreduction(1, 2, 1),
					new yyreduction(1, 2, 2), new yyreduction(1, 2, 3),
					new yyreduction(2, 3, 4), new yyreduction(2, 3, 5),
					new yyreduction(2, 2, 6), new yyreduction(2, 3, 7),
					new yyreduction(2, 1, 8) };
			yytables.yyreduction = reduction;

			final yytokenaction tokenaction[] = {
					new yytokenaction(6, YYAT_SHIFT, 10),
					new yytokenaction(13, YYAT_SHIFT, 1),
					new yytokenaction(17, YYAT_SHIFT, 4),
					new yytokenaction(6, YYAT_SHIFT, 11),
					new yytokenaction(8, YYAT_SHIFT, 14),
					new yytokenaction(8, YYAT_SHIFT, 12),
					new yytokenaction(8, YYAT_SHIFT, 13),
					new yytokenaction(16, YYAT_SHIFT, 12),
					new yytokenaction(13, YYAT_SHIFT, 2),
					new yytokenaction(5, YYAT_ACCEPT, 0),
					new yytokenaction(3, YYAT_SHIFT, 9),
					new yytokenaction(0, YYAT_SHIFT, 3),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(6, YYAT_SHIFT, 12),
					new yytokenaction(6, YYAT_SHIFT, 13) };
			yytables.yytokenaction = tokenaction;

			final yystateaction stateaction[] = {
					new yystateaction(-65525, true, YYAT_DEFAULT, 13),
					new yystateaction(0, false, YYAT_DEFAULT, 13),
					new yystateaction(0, false, YYAT_DEFAULT, 13),
					new yystateaction(0, true, YYAT_ERROR, 0),
					new yystateaction(0, false, YYAT_REDUCE, 9),
					new yystateaction(9, true, YYAT_ERROR, 0),
					new yystateaction(-10, true, YYAT_REDUCE, 1),
					new yystateaction(0, false, YYAT_REDUCE, 7),
					new yystateaction(-37, true, YYAT_ERROR, 0),
					new yystateaction(0, false, YYAT_REDUCE, 4),
					new yystateaction(0, false, YYAT_REDUCE, 3),
					new yystateaction(0, false, YYAT_REDUCE, 2),
					new yystateaction(0, false, YYAT_DEFAULT, 13),
					new yystateaction(-32, true, YYAT_DEFAULT, 17),
					new yystateaction(0, false, YYAT_REDUCE, 8),
					new yystateaction(0, false, YYAT_REDUCE, 6),
					new yystateaction(-35, true, YYAT_REDUCE, 5),
					new yystateaction(-65535, true, YYAT_ERROR, 0) };
			yytables.yystateaction = stateaction;

			final yynontermgoto nontermgoto[] = { new yynontermgoto(0, 5),
					new yynontermgoto(0, 6), new yynontermgoto(13, 16),
					new yynontermgoto(12, 15), new yynontermgoto(2, 8),
					new yynontermgoto(1, 7) };
			yytables.yynontermgoto = nontermgoto;

			final yystategoto stategoto[] = { new yystategoto(-1, -1),
					new yystategoto(3, -1), new yystategoto(2, -1),
					new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(1, -1),
					new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1) };
			yytables.yystategoto = stategoto;

			yytables.yydestructorref = null;

			yytables.yytokendestref = null;

			yytables.yytokendestbaseref = null;
		}
	}

	// line 109 ".\\cond_par.y"

	private String factor(String v) {
		return "(" + v + ")";
	}

	void exit(int c, String err) {
		yyerror(err);
		yyexitcode = c;
		yyexitflg = true;
	}

	public static void main(String args[]) throws Exception {
		String[] c = new String[4];
		c[0] = "A";
		//c[1] = "f2>3";
		c[2] = "F";
		c[3] = "C3";

		String s = parser("1*2*4", c);
		System.out.println(s);

		c[0] = null;
		c[1] = null;
		c[2] = "f3=2";

		s = parser("(1*2)*3", c);
		System.out.println(s);

		c[0] = null;
		c[1] = null;
		c[2] = "f3=2";

		s = parser("(1*2)*!3", c);
		System.out.println(s);

	}

	static public String parser(String input, String[] conds) throws Exception {
		return parser(input, conds, false);
	}

	static public String parser(String input, String[] conds, boolean debug)
			throws Exception {
		int ret = 0;
		cond_par parser = new cond_par();
		parser.yydebug = debug;
		ByteArrayOutputStream err = null;
		if (parser.create(input, conds, debug)) {
			parser.yyerr = new java.io.OutputStreamWriter(
					err = new java.io.ByteArrayOutputStream(256));
			ret = parser.yyparse();
		}

		if (ret != 0)
			throw new Exception(err.toString());

		if (debug)
			System.out.println(((YYSTYPE) parser.yyvalref).value);

		return ((YYSTYPE) parser.yyvalref).value;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// calc_parser commands

	public boolean create(String input, String[] conds, boolean debug) {
		cond_lex lexer = new cond_lex();
		if (!yycreate(lexer)) {
			return false;
		}
		if (!lexer.create(this)) {
			return false;
		}

		this.conds = conds;

		lexer.yyin = new InputStreamReader(new StringBufferInputStream(input));
		lexer.yydebug = debug;
		return true; // success
	}
}
