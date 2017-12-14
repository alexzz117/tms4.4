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
* cond_lexer.java
* Java source file generated from cond_lexer.l.
* 
* Date: 10/11/13
* Time: 17:30:35
* 
* ALex Version: 2.07
****************************************************************************/

// line 1 ".\\cond_lexer.l"

package cn.com.higinet.tms35.core.cond.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.core.cond.yl.yyflexer;

// line 39 "cond_lexer.java"

/////////////////////////////////////////////////////////////////////////////
// cond_lexer

public class cond_lexer extends yyflexer {
// line 16 ".\\cond_lexer.l"

	static Logger log = LoggerFactory.getLogger(cond_lexer.class);
	private int m_line,m_line_pos,m_position;
	public String m_src;

// line 52 "cond_lexer.java"
	public cond_lexer() {
		yytables();
// line 23 ".\\cond_lexer.l"

	m_position=0;
	m_line=0;
	m_line_pos=0;
	// do nothing

// line 62 "cond_lexer.java"
	}

	public static final int INITIAL = 0;

	protected static yyftables yytables = null;

	public final int yyaction(int action) {
// line 53 ".\\cond_lexer.l"

		// extract yylval for use later on in actions
		cond_parser.YYSTYPE yylval = (cond_parser.YYSTYPE)yyparserref.yylvalref;

// line 75 "cond_lexer.java"
		yyreturnflg = true;
		switch (action) {
		case 1:
			{
// line 63 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '='; 
// line 82 "cond_lexer.java"
			}
		case 2:
			{
// line 64 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '>'; 
// line 88 "cond_lexer.java"
			}
		case 3:
			{
// line 65 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '<'; 
// line 94 "cond_lexer.java"
			}
		case 4:
			{
// line 66 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.GE; 
// line 100 "cond_lexer.java"
			}
		case 5:
			{
// line 67 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.LE; 
// line 106 "cond_lexer.java"
			}
		case 6:
			{
// line 68 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.NE; 
// line 112 "cond_lexer.java"
			}
		case 7:
			{
// line 69 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.NE; 
// line 118 "cond_lexer.java"
			}
		case 8:
			{
// line 70 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '+'; 
// line 124 "cond_lexer.java"
			}
		case 9:
			{
// line 71 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '-'; 
// line 130 "cond_lexer.java"
			}
		case 10:
			{
// line 72 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '*'; 
// line 136 "cond_lexer.java"
			}
		case 11:
			{
// line 73 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '/'; 
// line 142 "cond_lexer.java"
			}
		case 12:
			{
// line 74 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '%'; 
// line 148 "cond_lexer.java"
			}
		case 13:
			{
// line 75 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '('; 
// line 154 "cond_lexer.java"
			}
		case 14:
			{
// line 76 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return ')'; 
// line 160 "cond_lexer.java"
			}
		case 15:
			{
// line 77 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return '['; 
// line 166 "cond_lexer.java"
			}
		case 16:
			{
// line 78 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return ']'; 
// line 172 "cond_lexer.java"
			}
		case 17:
			{
// line 79 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return ','; 
// line 178 "cond_lexer.java"
			}
		case 18:
			{
// line 81 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.const_,op.string_,new String(yytext, 1, yyleng-2)); return cond_parser.CONST; 
// line 184 "cond_lexer.java"
			}
		case 19:
			{
// line 82 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.const_,op.datetime_,date_tool.parse(new String(yytext, 1, yyleng-3)).getTime()); return cond_parser.CONST; 
// line 190 "cond_lexer.java"
			}
		case 20:
			{
// line 83 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.const_,op.time_,date_tool.parse(new String(yytext, 1, yyleng-2),date_tool.FMT_T).getTime()); return cond_parser.CONST; 
// line 196 "cond_lexer.java"
			}
		case 21:
			{
// line 84 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;yylval.node = timespan(); return cond_parser.CONST; 
// line 202 "cond_lexer.java"
			}
		case 22:
			{
// line 85 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;yylval.node = int_lite(); return cond_parser.CONST; 
// line 208 "cond_lexer.java"
			}
		case 23:
			{
// line 86 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;yylval.node = double_lite(); return cond_parser.CONST; 
// line 214 "cond_lexer.java"
			}
		case 24:
			{
// line 90 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.AND; 
// line 220 "cond_lexer.java"
			}
		case 25:
			{
// line 91 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.OR; 
// line 226 "cond_lexer.java"
			}
		case 26:
			{
// line 92 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.IN; 
// line 232 "cond_lexer.java"
			}
		case 27:
			{
// line 93 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.NOTIN; 
// line 238 "cond_lexer.java"
			}
		case 28:
		case 29:
			{
// line 97 ".\\cond_lexer.l"
 m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.id_,new String(yytext, 0, yyleng)); return cond_parser.ID; 
// line 245 "cond_lexer.java"
			}
		case 30:
			{
// line 103 ".\\cond_lexer.l"
	if(yytext[0]=='\n')
								{
								m_line++;
								m_line_pos=0;
								}
							else
							{
								m_position+=yyleng;m_line_pos+=yyleng;
								}
						
// line 260 "cond_lexer.java"
			}
			break;
		case 31:
			{
// line 114 ".\\cond_lexer.l"
invalidCharacter();
// line 267 "cond_lexer.java"
			}
			break;
		default:
			break;
		}
		yyreturnflg = false;
		return 0;
	}

	protected final void yytables() {
		yystext_size = 100;
		yysunput_size = 100;
		yytext_max = 0;
		yyunput_max = 0;

		yycreatetables();
		yymatch = yytables.yymatch;
		yytransition = yytables.yytransition;
		yystate = yytables.yystate;
		yybackup = yytables.yybackup;
	}

	public static synchronized final void yycreatetables() {
		if (yytables == null) {
			yytables = new yyftables();

			final short match[] = {
				0
			};
			yytables.yymatch = match;

			final yytransition transition[] = {
				new yytransition(0, 0),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(4, 1),
				new yytransition(4, 1),
				new yytransition(3, 1),
				new yytransition(4, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(4, 1),
				new yytransition(5, 1),
				new yytransition(6, 1),
				new yytransition(7, 1),
				new yytransition(3, 1),
				new yytransition(8, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(9, 1),
				new yytransition(10, 1),
				new yytransition(11, 1),
				new yytransition(12, 1),
				new yytransition(13, 1),
				new yytransition(14, 1),
				new yytransition(15, 1),
				new yytransition(16, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(17, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(18, 1),
				new yytransition(19, 1),
				new yytransition(20, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(21, 1),
				new yytransition(99, 98),
				new yytransition(99, 98),
				new yytransition(99, 98),
				new yytransition(99, 98),
				new yytransition(99, 98),
				new yytransition(99, 98),
				new yytransition(45, 21),
				new yytransition(23, 1),
				new yytransition(55, 37),
				new yytransition(61, 45),
				new yytransition(0, 98),
				new yytransition(69, 62),
				new yytransition(24, 1),
				new yytransition(25, 1),
				new yytransition(62, 47),
				new yytransition(74, 69),
				new yytransition(46, 23),
				new yytransition(47, 24),
				new yytransition(48, 25),
				new yytransition(95, 93),
				new yytransition(95, 93),
				new yytransition(95, 93),
				new yytransition(57, 40),
				new yytransition(0, 82),
				new yytransition(57, 40),
				new yytransition(26, 1),
				new yytransition(3, 1),
				new yytransition(27, 1),
				new yytransition(3, 1),
				new yytransition(0, 93),
				new yytransition(3, 1),
				new yytransition(21, 1),
				new yytransition(0, 78),
				new yytransition(41, 18),
				new yytransition(42, 18),
				new yytransition(83, 78),
				new yytransition(83, 78),
				new yytransition(97, 94),
				new yytransition(45, 21),
				new yytransition(23, 1),
				new yytransition(55, 37),
				new yytransition(61, 45),
				new yytransition(71, 66),
				new yytransition(69, 62),
				new yytransition(24, 1),
				new yytransition(25, 1),
				new yytransition(62, 47),
				new yytransition(74, 69),
				new yytransition(46, 23),
				new yytransition(47, 24),
				new yytransition(48, 25),
				new yytransition(57, 55),
				new yytransition(0, 98),
				new yytransition(57, 55),
				new yytransition(98, 96),
				new yytransition(88, 84),
				new yytransition(51, 53),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(3, 1),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(60, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(59, 59),
				new yytransition(0, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(59, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 59),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(44, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(22, 74),
				new yytransition(0, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(22, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 74),
				new yytransition(0, 77),
				new yytransition(0, 81),
				new yytransition(0, 103),
				new yytransition(31, 85),
				new yytransition(101, 100),
				new yytransition(65, 51),
				new yytransition(65, 51),
				new yytransition(65, 51),
				new yytransition(65, 51),
				new yytransition(65, 51),
				new yytransition(65, 51),
				new yytransition(0, 88),
				new yytransition(0, 30),
				new yytransition(0, 33),
				new yytransition(0, 33),
				new yytransition(0, 51),
				new yytransition(43, 20),
				new yytransition(51, 50),
				new yytransition(0, 104),
				new yytransition(0, 64),
				new yytransition(97, 106),
				new yytransition(0, 105),
				new yytransition(29, 5),
				new yytransition(93, 91),
				new yytransition(32, 85),
				new yytransition(72, 92),
				new yytransition(94, 110),
				new yytransition(90, 88),
				new yytransition(90, 88),
				new yytransition(90, 88),
				new yytransition(90, 88),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(87, 81),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(108, 105),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 51),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 88),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(33, 85),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(106, 103),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(49, 33),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(82, 77),
				new yytransition(82, 81),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(106, 105),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(58, 58),
				new yytransition(39, 58),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(28, 58),
				new yytransition(0, 0),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(28, 58),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(0, 56),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(56, 56),
				new yytransition(0, 0),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(56, 56),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(60, 60),
				new yytransition(0, 0),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(60, 60),
				new yytransition(78, 73),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(64, 73),
				new yytransition(34, 7),
				new yytransition(35, 7),
				new yytransition(35, 7),
				new yytransition(36, 7),
				new yytransition(36, 7),
				new yytransition(36, 7),
				new yytransition(36, 7),
				new yytransition(36, 7),
				new yytransition(36, 7),
				new yytransition(36, 7),
				new yytransition(52, 73),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(52, 73),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(52, 73),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(52, 73),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(38, 17),
				new yytransition(52, 73),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(17, 17),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(52, 73),
				new yytransition(0, 0),
				new yytransition(40, 17),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(0, 0),
				new yytransition(40, 17),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(63, 49),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(70, 63),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(79, 75),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(75, 70),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(50, 34),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(81, 76),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(53, 35),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(0, 0),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(85, 79),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(54, 36),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(66, 65),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(84, 83),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(73, 67),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(89, 86),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(86, 87),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(68, 68),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(92, 89),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(91, 90),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(67, 54),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(96, 95),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(28, 28),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(100, 99),
				new yytransition(102, 101),
				new yytransition(102, 101),
				new yytransition(102, 101),
				new yytransition(102, 101),
				new yytransition(102, 101),
				new yytransition(102, 101),
				new yytransition(103, 101),
				new yytransition(103, 101),
				new yytransition(103, 101),
				new yytransition(103, 101),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(105, 102),
				new yytransition(76, 71),
				new yytransition(76, 71),
				new yytransition(76, 71),
				new yytransition(76, 71),
				new yytransition(76, 71),
				new yytransition(76, 71),
				new yytransition(77, 71),
				new yytransition(77, 71),
				new yytransition(77, 71),
				new yytransition(77, 71),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(37, 38),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(109, 107),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(107, 108),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109),
				new yytransition(110, 109)
			};
			yytables.yytransition = transition;

			final yystate state[] = {
				new yystate(0, 0, 0),
				new yystate(74, 1, 0),
				new yystate(1, 0, 0),
				new yystate(0, 0, 31),
				new yystate(0, 0, 30),
				new yystate(0, 346, 31),
				new yystate(30, 0, 31),
				new yystate(0, 687, 31),
				new yystate(0, 0, 12),
				new yystate(0, 0, 13),
				new yystate(0, 0, 14),
				new yystate(0, 0, 10),
				new yystate(0, 0, 8),
				new yystate(0, 0, 17),
				new yystate(0, 0, 9),
				new yystate(38, 0, 31),
				new yystate(0, 0, 11),
				new yystate(58, 719, 22),
				new yystate(0, 39, 3),
				new yystate(0, 0, 1),
				new yystate(0, 340, 2),
				new yystate(74, -5, 28),
				new yystate(74, 0, 28),
				new yystate(74, 5, 28),
				new yystate(74, 5, 28),
				new yystate(74, 3, 28),
				new yystate(0, 0, 15),
				new yystate(0, 0, 16),
				new yystate(58, 993, 28),
				new yystate(0, 0, 6),
				new yystate(85, 387, 0),
				new yystate(85, 0, 0),
				new yystate(0, 0, 18),
				new yystate(-30, 386, 0),
				new yystate(53, 838, 0),
				new yystate(53, 858, 0),
				new yystate(53, 883, 0),
				new yystate(38, 6, 23),
				new yystate(0, 1043, 0),
				new yystate(56, 0, 0),
				new yystate(58, 46, 28),
				new yystate(0, 0, 5),
				new yystate(0, 0, 7),
				new yystate(0, 0, 4),
				new yystate(59, 0, 29),
				new yystate(74, 8, 28),
				new yystate(74, 0, 26),
				new yystate(74, -3, 28),
				new yystate(74, 0, 25),
				new yystate(85, 741, 0),
				new yystate(64, 344, 0),
				new yystate(65, 342, 0),
				new yystate(0, 0, 21),
				new yystate(54, 65, 0),
				new yystate(67, 973, 0),
				new yystate(68, 75, 0),
				new yystate(59, 524, 28),
				new yystate(68, 0, 0),
				new yystate(74, 449, 23),
				new yystate(-56, 129, 28),
				new yystate(0, 599, 29),
				new yystate(74, 0, 24),
				new yystate(74, 5, 28),
				new yystate(85, 751, 0),
				new yystate(73, 359, 0),
				new yystate(66, 893, 0),
				new yystate(92, 51, 0),
				new yystate(64, 913, 0),
				new yystate(0, 943, 23),
				new yystate(74, 4, 28),
				new yystate(85, 773, 0),
				new yystate(0, 1033, 0),
				new yystate(0, 0, 20),
				new yystate(0, 677, 0),
				new yystate(-28, 257, 27),
				new yystate(85, 761, 0),
				new yystate(81, 848, 0),
				new yystate(-80, 375, 0),
				new yystate(83, 54, 0),
				new yystate(85, 828, 0),
				new yystate(82, 0, 0),
				new yystate(-80, 376, 0),
				new yystate(87, -26, 20),
				new yystate(84, 903, 0),
				new yystate(0, 77, 0),
				new yystate(-30, 375, 0),
				new yystate(92, 923, 0),
				new yystate(92, 933, 0),
				new yystate(90, 364, 0),
				new yystate(92, 953, 0),
				new yystate(91, 963, 0),
				new yystate(110, 376, 0),
				new yystate(0, 294, 0),
				new yystate(95, 38, 0),
				new yystate(0, -12, 0),
				new yystate(96, 983, 0),
				new yystate(0, 63, 0),
				new yystate(0, 0, 19),
				new yystate(99, 19, 0),
				new yystate(100, 1003, 0),
				new yystate(110, 331, 0),
				new yystate(0, 1013, 0),
				new yystate(105, 1023, 0),
				new yystate(-104, 377, 0),
				new yystate(108, 303, 0),
				new yystate(-104, 396, 0),
				new yystate(104, 289, 0),
				new yystate(110, 1053, 0),
				new yystate(110, 1063, 0),
				new yystate(110, 1073, 0),
				new yystate(0, 311, 0)
			};
			yytables.yystate = state;

			final boolean backup[] = {
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false,
				false
			};
			yytables.yybackup = backup;
		}
	}
// line 115 ".\\cond_lexer.l"

	// ///////////////////////////////////////////////////////////////////////////
	// cond_lexer attribute commands

	protected node double_lite()
	{
		String string = new String(yytext, 0, yyleng);
		try
		{
			Double number = new Double(string);
			if (number.isInfinite())
			{
				log.error("number too big");
			}

			return new node(op.const_, op.double_, number);
		}
		catch (NumberFormatException e)
		{
			throw new tms_exception_lexer("浮点数常量不合法："+string);
		}
	}

	protected node int_lite()
	{
		String string = new String(yytext, 0, yyleng);
		try
		{
			Long number = new Long(string);

			return new node(op.const_, op.long_, number);
		}
		catch (NumberFormatException e)
		{
			throw new tms_exception_lexer("整数常量不合法："+string);
		}
	}

	protected node timespan()
	{
		String string = new String(yytext, 1, yyleng - 2);
		try
		{
			Long number = new Long(string);

			switch (yytext[yyleng - 1])
			{
			case 'Y':
				return new node(op.const_, op.span_, (long)1000 * 60 * 60 * 24 * 365.2425 * number.longValue());
			case 'm':
				return new node(op.const_, op.span_, (long)1000 * 60 * 60 * 24 * (365.2425 / 12)
						* number.longValue());
			case 'D':
				return new node(op.const_, op.span_, (long)1000 * 60 * 60 * 24 * number.longValue());
			case 'H':
				return new node(op.const_, op.span_, (long)1000 * 60 * 60 * number.longValue());
			case 'M':
				return new node(op.const_, op.span_, (long)1000 * 60 * number.longValue());
			case 'S':
				return new node(op.const_, op.span_, (long)1000 * number.longValue());
			}

			return null;
		}
		catch (NumberFormatException e)
		{
			throw new tms_exception_lexer("时间段常量不合法："+string);
		}
	}

	protected void invalidCharacter() {
		String e = "非法字符:"+get_err_pos();
		throw new tms_exception_lexer(e); 
	}
	
	public String get_err_pos()
	{
		return "[行=" + (m_line+1) + ":列=" //
				+ (m_line_pos+1) + "]'" + m_src.substring(0, m_position) //
				+ "<<-当前位置->>" + m_src.substring(m_position) + "'";
	}

	public boolean create(cond_parser parser)
	{
		return yycreate(parser);
	}
}

