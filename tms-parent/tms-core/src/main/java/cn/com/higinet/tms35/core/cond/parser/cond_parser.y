%{
package cn.com.higinet.tms35.core.cond.parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.core.cond.yl.yyfparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


%}

// parser name
%name cond_parser

// class definition
{
	static Logger log = LoggerFactory.getLogger(cond_parser.class);
	protected cond_lexer lexer;				// the lexical analyser
	public    node       ret;
}

// constructor
{
	// do nothing
}

// attribute type
%union {
	public node   node;

	// copy method
	public void yycopy(yyattribute source, boolean move) {
		YYSTYPE yy = (YYSTYPE)source;
		node  = yy.node;
		if (move) {
			yy.node=null;
		}
	}
}

// nonterminals
//%type <value> line

// tokens
%left OR
%left AND
%left IN,NOTIN
%left '=','>','<',GE,LE,NE
%left '+', '-'
%left '*', '/', '%'
%left '(', ')'
%left '[', ']'
%token ID CONST
%right UMINUS

// keywords
%%
line
	: expr					{ret=$1.node;}
	;
	
expr
	: expr '=' expr				{$$.node=new node(op.eq_);$$.node.add($1.node,$3.node);}
	| expr '>' expr				{$$.node=new node(op.gt_);$$.node.add($1.node,$3.node);}
	| expr '<' expr				{$$.node=new node(op.lt_);$$.node.add($1.node,$3.node);}
	| expr NE  expr				{$$.node=new node(op.ne_);$$.node.add($1.node,$3.node);}
	| expr GE  expr				{$$.node=new node(op.ge_);$$.node.add($1.node,$3.node);}
	| expr LE  expr				{$$.node=new node(op.le_);$$.node.add($1.node,$3.node);}
	| expr OR  expr				{$$.node=new node(op.or_);$$.node.add($1.node,$3.node);}
	| expr AND expr				{$$.node=new node(op.and_);$$.node.add($1.node,$3.node);}
	| expr IN expr				{$$.node=new node(op.in_);   $$.node.add($1.node,$3.node);}
	| expr NOTIN expr			{$$.node=new node(op.notin_);$$.node.add($1.node,$3.node);}
	| expr '+' expr				{$$.node=new node(op.add_);$$.node.add($1.node,$3.node);}
	| expr '-' expr				{$$.node=new node(op.sub_);$$.node.add($1.node,$3.node);}
	| expr '*' expr				{$$.node=new node(op.mul_);$$.node.add($1.node,$3.node);}
	| expr '/' expr				{$$.node=new node(op.div_);$$.node.add($1.node,$3.node);}
	| expr '%' expr				{$$.node=new node(op.mod_);$$.node.add($1.node,$3.node);}
	| '(' expr ')'				{$$.yycopy($2,true); }
	| '(' expr error			{$$.yycopy($2,false); log.error("err1:"+$2.node+"-->missing ')'?");yyerrok();if(true)throw new tms_exception_lexer("syntax error:"+lexer.get_err_pos());}
	| CONST  					{$$.yycopy($1,true); }
	| CONST	ID					{$$.yycopy($1,false); log.error("err2:"+$1.node+"-->"+$2.node);yyerrok();if(true)throw new tms_exception_lexer("syntax error:"+lexer.get_err_pos());}
//	| error expr 				{$$.node=new node(op.mod_);$$.node.add($1.node,$2.node); log.error("err3:"+$1.node+" missing operator-->"+$2.node);yyerrok();if(true)throw new tms_exception_lexer("syntax error:"+lexer.get_err_pos());}
	| ID 						{$$.yycopy($1,true); }
	| func						{$$.yycopy($1,true); }
	| stat						{$$.yycopy($1,true); }
	| '-' expr %prec UMINUS		{$$.node=new node(op.neg_);$$.node.add($2.node);}

	;


func
	:ID '(' ')'					{$$.node=new node(op.func_,$1.node.name);}
	|ID '(' param ')'			{$$.node=new node(op.func_,$1.node.name);$$.node.child=$3.node.child;}
	;


stat
	:ID '[' ID ']'			{$$.node=new node(op.stat_,$1.node.name);
							 $$.node.add($3.node);$3.node.m_op=op.stat_param_;}
	|stat '(' id_list ')'	{$$.yycopy($1,true); $$.node.add($3.node.child);}
	;
	
id_list
	:ID						{ $$.node=new node(op.param_); $$.node.add($1.node);}
	|id_list ',' ID			{ $$.yycopy($1,true); $$.node.add($3.node);}
	;
	
param
	:param ',' expr				{ $$.yycopy($1,true); $$.node.add($3.node);}
	|expr						{ $$.node=new node(op.param_); $$.node.add($1.node);}
	;

%%


	/////////////////////////////////////////////////////////////////////////////
	// cond_parser commands

	public boolean create() {

		lexer = new cond_lexer();
		if (!yycreate(lexer)) {
			return false;
		}
		if (!lexer.create(this)) {
			return false;
		}
		return true;	// success
	}


	// ///////////////////////////////////////////////////////////////////////////
	// calc_parser commands

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
		lexer.m_src=input;
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
		yyerror("�﷨����"+this.lexer.get_err_pos());
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

		java.io.ByteArrayOutputStream bos=new ByteArrayOutputStream();

		ret= new cond_parser().build_(input, new java.io.OutputStreamWriter(bos), false);
		error.append(bos.toString());

		return ret;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// main

	public static void main(String args[])
	{
		StringBuffer sb=new StringBuffer();
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

