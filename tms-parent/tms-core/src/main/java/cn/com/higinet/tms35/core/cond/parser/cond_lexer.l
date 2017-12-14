%{
package cn.com.higinet.tms35.core.cond.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.core.cond.yl.yyflexer;
%}

// lexical analyser name
%name cond_lexer

// class definition
{
	static Logger log = LoggerFactory.getLogger(cond_lexer.class);
	private int m_line,m_line_pos,m_position;
	public String m_src;
}

// constructor
{
	m_position=0;
	m_line=0;
	m_line_pos=0;
	// do nothing
}

// macros
exponent	([Ee][+-]?[0-9]+)
nonascii	[^\0-\177]
unicode		\\[0-9a-f]{1,6}(\r\n|[ \n\r\t\f])?
escape		{unicode}|\\[^\n\r\f0-9a-f]
nl			\n|\r\n|\r|\f
datetime	#[1-9][0-9]{3}-[0-1]?[0-9]-[0-3]?[0-9]([ ]{1}[0-2]?[0-9]\:[0-5]?[0-9](\:[0-5]?[0-9](.[0-9]{1,3})?)?)?dt
time		#[0-2]?[0-9]\:[0-5]?[0-9](\:[0-5]?[0-9](.[0-9]{1,3})?)?t



string1		\"([^\n\r\f\\"]|\\{nl}|{nonascii}|{escape})*\"
string		{string1}
integer		[0-9]+
double_lite [0-9]*\.[0-9]+
double		{double_lite}|({double_lite}|{integer}){exponent}
number		{integer}|{double}
span		#[0-9]+[YmDHMS]
id			({nonascii}|[A-Za-z_0-9])+(:({nonascii}|[A-Za-z_0-9])+)?


%%

%{
		// extract yylval for use later on in actions
		cond_parser.YYSTYPE yylval = (cond_parser.YYSTYPE)yyparserref.yylvalref;
%}

// return expressions
%return

// operators

"="							{ m_position+=yyleng;m_line_pos+=yyleng;return '='; }
">"							{ m_position+=yyleng;m_line_pos+=yyleng;return '>'; }
"<"							{ m_position+=yyleng;m_line_pos+=yyleng;return '<'; }
">="						{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.GE; }
"<="						{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.LE; }
"!="						{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.NE; }
"<>"						{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.NE; }
"+"							{ m_position+=yyleng;m_line_pos+=yyleng;return '+'; }
"-"							{ m_position+=yyleng;m_line_pos+=yyleng;return '-'; }
"*"							{ m_position+=yyleng;m_line_pos+=yyleng;return '*'; }
"/"							{ m_position+=yyleng;m_line_pos+=yyleng;return '/'; }
"%"							{ m_position+=yyleng;m_line_pos+=yyleng;return '%'; }
"("							{ m_position+=yyleng;m_line_pos+=yyleng;return '('; }
")"							{ m_position+=yyleng;m_line_pos+=yyleng;return ')'; }
"["							{ m_position+=yyleng;m_line_pos+=yyleng;return '['; }
"]"							{ m_position+=yyleng;m_line_pos+=yyleng;return ']'; }
","							{ m_position+=yyleng;m_line_pos+=yyleng;return ','; }

{string}					{ m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.const_,op.string_,new String(yytext, 1, yyleng-2)); return cond_parser.CONST; }
{datetime}					{ m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.const_,op.datetime_,date_tool.parse(new String(yytext, 1, yyleng-3)).getTime()); return cond_parser.CONST; }
{time}						{ m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.const_,op.time_,date_tool.parse(new String(yytext, 1, yyleng-2),date_tool.FMT_T).getTime()); return cond_parser.CONST; }
{span}						{ m_position+=yyleng;m_line_pos+=yyleng;yylval.node = timespan(); return cond_parser.CONST; }
{integer}					{ m_position+=yyleng;m_line_pos+=yyleng;yylval.node = int_lite(); return cond_parser.CONST; }
{double}					{ m_position+=yyleng;m_line_pos+=yyleng;yylval.node = double_lite(); return cond_parser.CONST; }


// keywords
[aA][nN][dD]				{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.AND; }
[oO][rR]					{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.OR; }
[iI][nN]					{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.IN; }
[nN][oO][tT][iI][nN]		{ m_position+=yyleng;m_line_pos+=yyleng;return cond_parser.NOTIN; }

// id
{id}					|
[A-Za-z_][A-Za-z_0-9:]*	{ m_position+=yyleng;m_line_pos+=yyleng;yylval.node = new node(op.id_,new String(yytext, 0, yyleng)); return cond_parser.ID; }


// white space
%break

[ \t\f\n]					{	if(yytext[0]=='\n')
								{
								m_line++;
								m_line_pos=0;
								}
							else
							{
								m_position+=yyleng;m_line_pos+=yyleng;
								}
						}

.							{invalidCharacter();}
%%

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
			return null;
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
			return null;
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
			return null;
		}
	}

	protected void invalidCharacter() {
		String e = "�Ƿ��ַ�:"+get_err_pos();
		throw new tms_exception_lexer(e); 
	}
	
	public String get_err_pos()
	{
		return "[��=" + (m_line+1) + ":��=" //
				+ (m_line_pos+1) + "]'" + m_src.substring(0, m_position) //
				+ "<<-��ǰλ��->>" + m_src.substring(m_position) + "'";
	}

	public boolean create(cond_parser parser)
	{
		return yycreate(parser);
	}
}

