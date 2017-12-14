/****************************************************************************
 *                     U N R E G I S T E R E D   C O P Y
 * 
 * You are on day 176 of your 30 day trial period.
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
 * cond_parser.java
 * Java source file generated from cond_parser.y.
 * 
 * Date: 10/11/13
 * Time: 17:30:35
 * 
 * AYACC Version: 2.07
 ****************************************************************************/

// line 1 ".\\cond_parser.y"

package cn.com.higinet.tms35.core.cond.parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.core.cond.yl.yyfparser;

// line 43 "cond_parser.java"

/////////////////////////////////////////////////////////////////////////////
// cond_parser

@SuppressWarnings("unused")
public class cond_parser extends yyfparser
{
	// line 20 ".\\cond_parser.y"

	static Logger log = LoggerFactory.getLogger(cond_parser.class);
	protected cond_lexer lexer; // the lexical analyser
	public node ret;

	// line 56 "cond_parser.java"
	public cond_parser()
	{
		yytables();
		// line 27 ".\\cond_parser.y"

		// do nothing

		// line 63 "cond_parser.java"
	}

	public class YYSTYPE extends yyattribute
	{
		// line 32 ".\\cond_parser.y"

		public node node;

		// copy method
		public void yycopy(yyattribute source, boolean move)
		{
			YYSTYPE yy = (YYSTYPE) source;
			node = yy.node;
			if (move)
			{
				yy.node = null;
			}
		}

		// line 80 "cond_parser.java"
	}

	public static final int OR = 65537;
	public static final int AND = 65538;
	public static final int IN = 65539;
	public static final int NOTIN = 65540;
	public static final int GE = 65541;
	public static final int LE = 65542;
	public static final int NE = 65543;
	public static final int ID = 65544;
	public static final int CONST = 65545;
	public static final int UMINUS = 65546;

	protected final YYSTYPE yyattribute(int index)
	{
		YYSTYPE attribute = (YYSTYPE) yyattributestackref[yytop + index];
		return attribute;
	}

	protected final yyattribute yynewattribute()
	{
		return new YYSTYPE();
	}

	protected final YYSTYPE[] yyinitdebug(int count)
	{
		YYSTYPE a[] = new YYSTYPE[count];
		for (int i = 0; i < count; i++)
		{
			a[i] = (YYSTYPE) yyattributestackref[yytop + i - (count - 1)];
		}
		return a;
	}

	protected static yyftables yytables = null;

	public final void yyaction(int action)
	{
		switch (action)
		{
		case 0:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(2);
			}
			// line 63 ".\\cond_parser.y"
			ret = yyattribute(1 - 1).node;
			// line 122 "cond_parser.java"
		}
			break;
		case 1:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 67 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.eq_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 133 "cond_parser.java"
		}
			break;
		case 2:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 68 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.gt_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 144 "cond_parser.java"
		}
			break;
		case 3:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 69 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.lt_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 155 "cond_parser.java"
		}
			break;
		case 4:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 70 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.ne_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 166 "cond_parser.java"
		}
			break;
		case 5:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 71 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.ge_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 177 "cond_parser.java"
		}
			break;
		case 6:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 72 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.le_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 188 "cond_parser.java"
		}
			break;
		case 7:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 73 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.or_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 199 "cond_parser.java"
		}
			break;
		case 8:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 74 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.and_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 210 "cond_parser.java"
		}
			break;
		case 9:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 75 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.in_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 221 "cond_parser.java"
		}
			break;
		case 10:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 76 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.notin_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 232 "cond_parser.java"
		}
			break;
		case 11:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 77 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.add_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 243 "cond_parser.java"
		}
			break;
		case 12:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 78 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.sub_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 254 "cond_parser.java"
		}
			break;
		case 13:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 79 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.mul_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 265 "cond_parser.java"
		}
			break;
		case 14:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 80 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.div_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 276 "cond_parser.java"
		}
			break;
		case 15:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 81 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.mod_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 3).node, yyattribute(3 - 3).node);
			// line 287 "cond_parser.java"
		}
			break;
		case 16:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 82 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(2 - 3), true);
			// line 298 "cond_parser.java"
		}
			break;
		case 17:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 83 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(2 - 3), false);
			log.error("err1:" + yyattribute(2 - 3).node + "-->missing ')'?");
			yyerrok();
			throw new tms_exception_lexer("syntax error:" + lexer.get_err_pos());
			// line 309 "cond_parser.java"
		}
		case 18:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(2);
			}
			// line 84 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 1), true);
			// line 320 "cond_parser.java"
		}
			break;
		case 19:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(3);
			}
			// line 85 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 2), false);
			log.error("err2:" + yyattribute(1 - 2).node + "-->" + yyattribute(2 - 2).node);
			yyerrok();
			throw new tms_exception_lexer("syntax error:" + lexer.get_err_pos());
			// line 331 "cond_parser.java"
		}
		case 20:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(2);
			}
			// line 87 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 1), true);
			// line 342 "cond_parser.java"
		}
			break;
		case 21:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(2);
			}
			// line 88 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 1), true);
			// line 353 "cond_parser.java"
		}
			break;
		case 22:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(2);
			}
			// line 89 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 1), true);
			// line 364 "cond_parser.java"
		}
			break;
		case 23:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(3);
			}
			// line 90 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.neg_);
			((YYSTYPE) yyvalref).node.add(yyattribute(2 - 2).node);
			// line 375 "cond_parser.java"
		}
			break;
		case 24:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 96 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.func_, yyattribute(1 - 3).node.name);
			// line 386 "cond_parser.java"
		}
			break;
		case 25:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(5);
			}
			// line 97 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.func_, yyattribute(1 - 4).node.name);
			((YYSTYPE) yyvalref).node.child = yyattribute(3 - 4).node.child;
			// line 397 "cond_parser.java"
		}
			break;
		case 26:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(5);
			}
			// line 102 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.stat_, yyattribute(1 - 4).node.name);
			((YYSTYPE) yyvalref).node.add(yyattribute(3 - 4).node);
			yyattribute(3 - 4).node.m_op = op.stat_param_;
			// line 409 "cond_parser.java"
		}
			break;
		case 27:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(5);
			}
			// line 104 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 4), true);
			((YYSTYPE) yyvalref).node.add(yyattribute(3 - 4).node.child);
			// line 420 "cond_parser.java"
		}
			break;
		case 28:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(2);
			}
			// line 108 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.param_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 1).node);
			// line 431 "cond_parser.java"
		}
			break;
		case 29:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 109 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 3), true);
			((YYSTYPE) yyvalref).node.add(yyattribute(3 - 3).node);
			// line 442 "cond_parser.java"
		}
			break;
		case 30:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(4);
			}
			// line 113 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).yycopy(yyattribute(1 - 3), true);
			((YYSTYPE) yyvalref).node.add(yyattribute(3 - 3).node);
			// line 453 "cond_parser.java"
		}
			break;
		case 31:
		{
			YYSTYPE yya[];
			if (YYDEBUG)
			{
				yya = yyinitdebug(2);
			}
			// line 114 ".\\cond_parser.y"
			((YYSTYPE) yyvalref).node = new node(op.param_);
			((YYSTYPE) yyvalref).node.add(yyattribute(1 - 1).node);
			// line 464 "cond_parser.java"
		}
			break;
		default:
			break;
		}
	}

	protected final void yytables()
	{
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

	public static synchronized final void yycreatetables()
	{
		if (yytables == null)
		{
			yytables = new yyftables();

			if (YYDEBUG)
			{
				final yysymbol symbol[] = { new yysymbol("$end", 0), new yysymbol("\'%\'", 37),
						new yysymbol("\'(\'", 40), new yysymbol("\')\'", 41),
						new yysymbol("\'*\'", 42), new yysymbol("\'+\'", 43),
						new yysymbol("\',\'", 44), new yysymbol("\'-\'", 45),
						new yysymbol("\'/\'", 47), new yysymbol("\'<\'", 60),
						new yysymbol("\'=\'", 61), new yysymbol("\'>\'", 62),
						new yysymbol("\'[\'", 91), new yysymbol("\']\'", 93),
						new yysymbol("error", 65536), new yysymbol("OR", 65537),
						new yysymbol("AND", 65538), new yysymbol("IN", 65539),
						new yysymbol("NOTIN", 65540), new yysymbol("GE", 65541),
						new yysymbol("LE", 65542), new yysymbol("NE", 65543),
						new yysymbol("ID", 65544), new yysymbol("CONST", 65545),
						new yysymbol("UMINUS", 65546), new yysymbol(null, 0) };
				yytables.yysymbol = symbol;

				final String rule[] = { "$accept: line", "line: expr", "expr: expr \'=\' expr",
						"expr: expr \'>\' expr", "expr: expr \'<\' expr", "expr: expr NE expr",
						"expr: expr GE expr", "expr: expr LE expr", "expr: expr OR expr",
						"expr: expr AND expr", "expr: expr IN expr", "expr: expr NOTIN expr",
						"expr: expr \'+\' expr", "expr: expr \'-\' expr", "expr: expr \'*\' expr",
						"expr: expr \'/\' expr", "expr: expr \'%\' expr", "expr: \'(\' expr \')\'",
						"expr: \'(\' expr error", "expr: CONST", "expr: CONST ID", "expr: ID",
						"expr: func", "expr: stat", "expr: \'-\' expr", "func: ID \'(\' \')\'",
						"func: ID \'(\' param \')\'", "stat: ID \'[\' ID \']\'",
						"stat: stat \'(\' id_list \')\'", "id_list: ID",
						"id_list: id_list \',\' ID", "param: param \',\' expr", "param: expr" };
				yytables.yyrule = rule;
			}

			final yyreduction reduction[] = { new yyreduction(0, 1, -1), new yyreduction(1, 1, 0),
					new yyreduction(2, 3, 1), new yyreduction(2, 3, 2), new yyreduction(2, 3, 3),
					new yyreduction(2, 3, 4), new yyreduction(2, 3, 5), new yyreduction(2, 3, 6),
					new yyreduction(2, 3, 7), new yyreduction(2, 3, 8), new yyreduction(2, 3, 9),
					new yyreduction(2, 3, 10), new yyreduction(2, 3, 11),
					new yyreduction(2, 3, 12), new yyreduction(2, 3, 13),
					new yyreduction(2, 3, 14), new yyreduction(2, 3, 15),
					new yyreduction(2, 3, 16), new yyreduction(2, 3, 17),
					new yyreduction(2, 1, 18), new yyreduction(2, 2, 19),
					new yyreduction(2, 1, 20), new yyreduction(2, 1, 21),
					new yyreduction(2, 1, 22), new yyreduction(2, 2, 23),
					new yyreduction(3, 3, 24), new yyreduction(3, 4, 25),
					new yyreduction(4, 4, 26), new yyreduction(4, 4, 27),
					new yyreduction(5, 1, 28), new yyreduction(5, 3, 29),
					new yyreduction(6, 3, 30), new yyreduction(6, 1, 31) };
			yytables.yyreduction = reduction;

			final yytokenaction tokenaction[] = { new yytokenaction(9, YYAT_SHIFT, 14),
					new yytokenaction(67, YYAT_SHIFT, 3), new yytokenaction(67, YYAT_SHIFT, 4),
					new yytokenaction(3, YYAT_SHIFT, 11), new yytokenaction(9, YYAT_SHIFT, 30),
					new yytokenaction(9, YYAT_SHIFT, 15), new yytokenaction(9, YYAT_SHIFT, 16),
					new yytokenaction(58, YYAT_SHIFT, 14), new yytokenaction(9, YYAT_SHIFT, 17),
					new yytokenaction(54, YYAT_SHIFT, 1), new yytokenaction(9, YYAT_SHIFT, 18),
					new yytokenaction(57, YYAT_SHIFT, 59), new yytokenaction(58, YYAT_SHIFT, 15),
					new yytokenaction(58, YYAT_SHIFT, 16), new yytokenaction(54, YYAT_SHIFT, 2),
					new yytokenaction(58, YYAT_SHIFT, 17), new yytokenaction(35, YYAT_SHIFT, 55),
					new yytokenaction(58, YYAT_SHIFT, 18), new yytokenaction(64, YYAT_SHIFT, 24),
					new yytokenaction(64, YYAT_SHIFT, 25), new yytokenaction(64, YYAT_SHIFT, 26),
					new yytokenaction(64, YYAT_SHIFT, 27), new yytokenaction(64, YYAT_SHIFT, 28),
					new yytokenaction(9, YYAT_SHIFT, 19), new yytokenaction(9, YYAT_SHIFT, 20),
					new yytokenaction(9, YYAT_SHIFT, 21), new yytokenaction(52, YYAT_SHIFT, 56),
					new yytokenaction(29, YYAT_SHIFT, 51), new yytokenaction(47, YYAT_SHIFT, 14),
					new yytokenaction(52, YYAT_SHIFT, 57), new yytokenaction(58, YYAT_SHIFT, 19),
					new yytokenaction(58, YYAT_SHIFT, 20), new yytokenaction(58, YYAT_SHIFT, 21),
					new yytokenaction(47, YYAT_SHIFT, 15), new yytokenaction(47, YYAT_SHIFT, 16),
					new yytokenaction(46, YYAT_SHIFT, 14), new yytokenaction(47, YYAT_SHIFT, 17),
					new yytokenaction(39, YYAT_SHIFT, 14), new yytokenaction(47, YYAT_SHIFT, 18),
					new yytokenaction(38, YYAT_SHIFT, 14), new yytokenaction(46, YYAT_SHIFT, 15),
					new yytokenaction(46, YYAT_SHIFT, 16), new yytokenaction(39, YYAT_SHIFT, 15),
					new yytokenaction(46, YYAT_SHIFT, 17), new yytokenaction(38, YYAT_SHIFT, 15),
					new yytokenaction(46, YYAT_SHIFT, 18), new yytokenaction(12, YYAT_SHIFT, 35),
					new yytokenaction(39, YYAT_SHIFT, 18), new yytokenaction(11, YYAT_SHIFT, 32),
					new yytokenaction(38, YYAT_SHIFT, 18), new yytokenaction(8, YYAT_SHIFT, 29),
					new yytokenaction(47, YYAT_SHIFT, 19), new yytokenaction(47, YYAT_SHIFT, 20),
					new yytokenaction(47, YYAT_SHIFT, 21), new yytokenaction(3, YYAT_SHIFT, 12),
					new yytokenaction(5, YYAT_ACCEPT, 0), new yytokenaction(45, YYAT_SHIFT, 14),
					new yytokenaction(4, YYAT_SHIFT, 13), new yytokenaction(46, YYAT_SHIFT, 19),
					new yytokenaction(46, YYAT_SHIFT, 20), new yytokenaction(46, YYAT_SHIFT, 21),
					new yytokenaction(45, YYAT_SHIFT, 15), new yytokenaction(45, YYAT_SHIFT, 16),
					new yytokenaction(44, YYAT_SHIFT, 14), new yytokenaction(45, YYAT_SHIFT, 17),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(45, YYAT_SHIFT, 18),
					new yytokenaction(34, YYAT_SHIFT, 53), new yytokenaction(44, YYAT_SHIFT, 15),
					new yytokenaction(44, YYAT_SHIFT, 16), new yytokenaction(34, YYAT_SHIFT, 54),
					new yytokenaction(44, YYAT_SHIFT, 17), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(44, YYAT_SHIFT, 18), new yytokenaction(66, YYAT_SHIFT, 26),
					new yytokenaction(66, YYAT_SHIFT, 27), new yytokenaction(66, YYAT_SHIFT, 28),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(45, YYAT_SHIFT, 19), new yytokenaction(45, YYAT_SHIFT, 20),
					new yytokenaction(45, YYAT_SHIFT, 21), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(33, YYAT_SHIFT, 14),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(44, YYAT_SHIFT, 19),
					new yytokenaction(44, YYAT_SHIFT, 20), new yytokenaction(44, YYAT_SHIFT, 21),
					new yytokenaction(33, YYAT_SHIFT, 15), new yytokenaction(33, YYAT_SHIFT, 16),
					new yytokenaction(6, YYAT_SHIFT, 14), new yytokenaction(33, YYAT_SHIFT, 17),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(33, YYAT_SHIFT, 18),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(6, YYAT_SHIFT, 15),
					new yytokenaction(6, YYAT_SHIFT, 16), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(6, YYAT_SHIFT, 17), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(6, YYAT_SHIFT, 18), new yytokenaction(65, YYAT_SHIFT, 26),
					new yytokenaction(65, YYAT_SHIFT, 27), new yytokenaction(65, YYAT_SHIFT, 28),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(33, YYAT_SHIFT, 19), new yytokenaction(33, YYAT_SHIFT, 20),
					new yytokenaction(33, YYAT_SHIFT, 21), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(6, YYAT_SHIFT, 19),
					new yytokenaction(6, YYAT_SHIFT, 20), new yytokenaction(6, YYAT_SHIFT, 21),
					new yytokenaction(61, YYAT_SHIFT, 31), new yytokenaction(61, YYAT_SHIFT, 22),
					new yytokenaction(61, YYAT_SHIFT, 23), new yytokenaction(61, YYAT_SHIFT, 24),
					new yytokenaction(61, YYAT_SHIFT, 25), new yytokenaction(61, YYAT_SHIFT, 26),
					new yytokenaction(61, YYAT_SHIFT, 27), new yytokenaction(61, YYAT_SHIFT, 28),
					new yytokenaction(68, YYAT_SHIFT, 22), new yytokenaction(68, YYAT_SHIFT, 23),
					new yytokenaction(68, YYAT_SHIFT, 24), new yytokenaction(68, YYAT_SHIFT, 25),
					new yytokenaction(68, YYAT_SHIFT, 26), new yytokenaction(68, YYAT_SHIFT, 27),
					new yytokenaction(68, YYAT_SHIFT, 28), new yytokenaction(62, YYAT_SHIFT, 22),
					new yytokenaction(62, YYAT_SHIFT, 23), new yytokenaction(62, YYAT_SHIFT, 24),
					new yytokenaction(62, YYAT_SHIFT, 25), new yytokenaction(62, YYAT_SHIFT, 26),
					new yytokenaction(62, YYAT_SHIFT, 27), new yytokenaction(62, YYAT_SHIFT, 28),
					new yytokenaction(60, YYAT_SHIFT, 22), new yytokenaction(60, YYAT_SHIFT, 23),
					new yytokenaction(60, YYAT_SHIFT, 24), new yytokenaction(60, YYAT_SHIFT, 25),
					new yytokenaction(60, YYAT_SHIFT, 26), new yytokenaction(60, YYAT_SHIFT, 27),
					new yytokenaction(60, YYAT_SHIFT, 28), new yytokenaction(63, YYAT_SHIFT, 23),
					new yytokenaction(63, YYAT_SHIFT, 24), new yytokenaction(63, YYAT_SHIFT, 25),
					new yytokenaction(63, YYAT_SHIFT, 26), new yytokenaction(63, YYAT_SHIFT, 27),
					new yytokenaction(63, YYAT_SHIFT, 28), new yytokenaction(50, YYAT_SHIFT, 14),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(50, YYAT_SHIFT, 15), new yytokenaction(50, YYAT_SHIFT, 16),
					new yytokenaction(49, YYAT_SHIFT, 14), new yytokenaction(50, YYAT_SHIFT, 17),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(50, YYAT_SHIFT, 18),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(49, YYAT_SHIFT, 15),
					new yytokenaction(49, YYAT_SHIFT, 16), new yytokenaction(48, YYAT_SHIFT, 14),
					new yytokenaction(49, YYAT_SHIFT, 17), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(49, YYAT_SHIFT, 18), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(48, YYAT_SHIFT, 15), new yytokenaction(48, YYAT_SHIFT, 16),
					new yytokenaction(43, YYAT_SHIFT, 14), new yytokenaction(48, YYAT_SHIFT, 17),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(48, YYAT_SHIFT, 18),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(43, YYAT_SHIFT, 15),
					new yytokenaction(43, YYAT_SHIFT, 16), new yytokenaction(42, YYAT_SHIFT, 14),
					new yytokenaction(43, YYAT_SHIFT, 17), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(43, YYAT_SHIFT, 18), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(42, YYAT_SHIFT, 15), new yytokenaction(42, YYAT_SHIFT, 16),
					new yytokenaction(41, YYAT_SHIFT, 14), new yytokenaction(42, YYAT_SHIFT, 17),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(42, YYAT_SHIFT, 18),
					new yytokenaction(-1, YYAT_ERROR, 0), new yytokenaction(41, YYAT_SHIFT, 15),
					new yytokenaction(41, YYAT_SHIFT, 16), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(41, YYAT_SHIFT, 17), new yytokenaction(-1, YYAT_ERROR, 0),
					new yytokenaction(41, YYAT_SHIFT, 18) };
			yytables.yytokenaction = tokenaction;

			final yystateaction stateaction[] = { new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(-37, true, YYAT_REDUCE, 21),
					new yystateaction(-65487, true, YYAT_REDUCE, 19),
					new yystateaction(55, true, YYAT_ERROR, 0),
					new yystateaction(54, true, YYAT_DEFAULT, 60),
					new yystateaction(0, false, YYAT_REDUCE, 22),
					new yystateaction(10, true, YYAT_REDUCE, 23),
					new yystateaction(-37, true, YYAT_DEFAULT, 61),
					new yystateaction(0, false, YYAT_REDUCE, 24),
					new yystateaction(7, true, YYAT_DEFAULT, 54),
					new yystateaction(-65498, true, YYAT_ERROR, 0),
					new yystateaction(0, false, YYAT_REDUCE, 20),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(0, false, YYAT_DEFAULT, 54),
					new yystateaction(-65517, true, YYAT_ERROR, 0),
					new yystateaction(0, false, YYAT_REDUCE, 17),
					new yystateaction(0, false, YYAT_REDUCE, 18),
					new yystateaction(0, false, YYAT_REDUCE, 25),
					new yystateaction(47, true, YYAT_DEFAULT, 62),
					new yystateaction(26, true, YYAT_ERROR, 0),
					new yystateaction(-77, true, YYAT_ERROR, 0),
					new yystateaction(0, false, YYAT_REDUCE, 16),
					new yystateaction(0, false, YYAT_REDUCE, 14),
					new yystateaction(2, true, YYAT_REDUCE, 12),
					new yystateaction(0, true, YYAT_REDUCE, 13),
					new yystateaction(0, false, YYAT_REDUCE, 15),
					new yystateaction(150, true, YYAT_REDUCE, 4),
					new yystateaction(143, true, YYAT_REDUCE, 2),
					new yystateaction(136, true, YYAT_REDUCE, 3),
					new yystateaction(26, true, YYAT_DEFAULT, 63),
					new yystateaction(19, true, YYAT_DEFAULT, 64),
					new yystateaction(-2, true, YYAT_DEFAULT, 65),
					new yystateaction(-9, true, YYAT_DEFAULT, 66),
					new yystateaction(129, true, YYAT_REDUCE, 6),
					new yystateaction(122, true, YYAT_REDUCE, 7),
					new yystateaction(115, true, YYAT_REDUCE, 5),
					new yystateaction(0, false, YYAT_REDUCE, 29),
					new yystateaction(-15, true, YYAT_ERROR, 0),
					new yystateaction(0, false, YYAT_REDUCE, 26),
					new yystateaction(-31, true, YYAT_DEFAULT, 67),
					new yystateaction(0, false, YYAT_REDUCE, 27),
					new yystateaction(0, false, YYAT_REDUCE, 28),
					new yystateaction(-65533, true, YYAT_ERROR, 0),
					new yystateaction(-30, true, YYAT_DEFAULT, 68),
					new yystateaction(0, false, YYAT_REDUCE, 30),
					new yystateaction(-65398, true, YYAT_REDUCE, 1),
					new yystateaction(-65419, true, YYAT_ERROR, 0),
					new yystateaction(-65405, true, YYAT_REDUCE, 32),
					new yystateaction(-65392, true, YYAT_REDUCE, 8),
					new yystateaction(-65521, true, YYAT_REDUCE, 9),
					new yystateaction(-65439, true, YYAT_REDUCE, 10),
					new yystateaction(-65467, true, YYAT_REDUCE, 11),
					new yystateaction(-65543, true, YYAT_ERROR, 0),
					new yystateaction(-65412, true, YYAT_REDUCE, 31) };
			yytables.yystateaction = stateaction;

			final yynontermgoto nontermgoto[] = { new yynontermgoto(54, 58),
					new yynontermgoto(54, 7), new yynontermgoto(54, 8), new yynontermgoto(11, 33),
					new yynontermgoto(0, 5), new yynontermgoto(0, 6), new yynontermgoto(29, 52),
					new yynontermgoto(11, 34), new yynontermgoto(28, 50),
					new yynontermgoto(27, 49), new yynontermgoto(26, 48),
					new yynontermgoto(25, 47), new yynontermgoto(24, 46),
					new yynontermgoto(23, 45), new yynontermgoto(22, 44),
					new yynontermgoto(21, 43), new yynontermgoto(20, 42),
					new yynontermgoto(19, 41), new yynontermgoto(18, 40),
					new yynontermgoto(17, 39), new yynontermgoto(16, 38),
					new yynontermgoto(15, 37), new yynontermgoto(14, 36), new yynontermgoto(2, 10),
					new yynontermgoto(1, 9) };
			yytables.yynontermgoto = nontermgoto;

			final yystategoto stategoto[] = { new yystategoto(3, 54), new yystategoto(22, 54),
					new yystategoto(21, 54), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(1, 54), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(20, 54), new yystategoto(19, 54), new yystategoto(18, 54),
					new yystategoto(17, 54), new yystategoto(16, 54), new yystategoto(15, 54),
					new yystategoto(14, 54), new yystategoto(13, 54), new yystategoto(12, 54),
					new yystategoto(11, 54), new yystategoto(10, 54), new yystategoto(9, 54),
					new yystategoto(8, 54), new yystategoto(7, 54), new yystategoto(6, 54),
					new yystategoto(1, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(-2, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1), new yystategoto(0, -1), new yystategoto(0, -1),
					new yystategoto(0, -1) };
			yytables.yystategoto = stategoto;

			yytables.yydestructorref = null;

			yytables.yytokendestref = null;

			yytables.yytokendestbaseref = null;
		}
	}

	// line 117 ".\\cond_parser.y"

	// ///////////////////////////////////////////////////////////////////////////
	// cond_parser commands

	public boolean create()
	{

		lexer = new cond_lexer();
		if (!yycreate(lexer))
		{
			return false;
		}
		if (!lexer.create(this))
		{
			return false;
		}
		return true; // success
	}

	private node build_(String input)
	{
		return build_(input, null, false);
	}

	private node build_(String input, OutputStreamWriter error, boolean debug)
	{
		lexer = new cond_lexer();
		if (!yycreate(lexer))
		{
			return null;
		}
		if (!lexer.create(this))
		{
			return null;
		}

		lexer.yyin = new InputStreamReader(new java.io.ByteArrayInputStream(input.getBytes()));
		lexer.m_src = input;
		if (error != null)
		{
			lexer.yydebugout = error;
			lexer.yyerr = error;
			yydebugout = error;
			yyerr = error;
		}
		lexer.yydebug = debug;
		yydebug = debug;

		if (yyparse() != 0)
			return null;

		return ret;
	}

	@Override
	public void yysyntaxerror()
	{
		yyerror("语法错误：" + this.lexer.get_err_pos());
		// TODO Auto-generated method stub
		super.yysyntaxerror();
	}

	public static String Symbol(int id)
	{
		yysymbol[] ys = yytables.yysymbol;
		if (ys == null)
			return "";
		for (int i = 0; i < ys.length - 1; i++)
		{
			if (id == ys[i].yytoken)
				return ys[i].yyname;
		}

		return "";
	}

	public static node build(String input)
	{
		node ret = new cond_parser().build_(input);
		if (ret != null)
			return ret;

		return new cond_parser().build_(input, null, true);
	}

	public static node build(String input, StringBuffer error)
	{
		node ret = new cond_parser().build_(input);
		if (ret != null)
			return ret;

		java.io.ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ret = new cond_parser().build_(input, new java.io.OutputStreamWriter(bos), false);
		error.append(bos.toString());

		return ret;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// main

	public static void main(String args[])
	{
		StringBuffer sb = new StringBuffer();
		System.out.println(cond_parser.build("S1[field1]"));
		System.out.println(cond_parser.build("S1[field1](field2,field3)"));
		/*
		 * integer 1234 double 1234.11 double 123.0e11 string "" string
		 * "asdfj;a;slkjf\t\t\t\d\n" datetime #2011-1-1 12:5:31dt datetime
		 * #2011-1-1dt time #12:5:31t timespan #12Y timespan #3m timespan #12D
		 * timespan #12H timespan #12M timespan #12S
		 */

		// prepare1_test("#2013-3-1dt-#2013-1-1dt > #60D/2 AND A>0 AND 10*10-2>3*5");
		// prepare1_test("200 > 10000 AND A>0");
		// prepare1_test("T01::S2(Field1,Field2,Field3,Field4)<>1");
		// prepare1_test("S1>S2 and T01::S1>#200D OR V1 > #2011-1-1dt and day(TRAN_DATE)=day(S3) and T01::S2(Field1,Field2,Field3,Field4)<>1");
		// prepare1_test("1>2 AND V1 > #2011-1-1 12:0:0dt");
	}
}
