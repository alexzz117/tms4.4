%{
package com.richtone.query.parser;

import com.richtone.yl.*;
import com.richtone.query.*;
import java.util.*;
import java.io.*;


%}

// parser name
%name cond_par

// class definition
{
	cond_lex lexer;				// the lexical analyser
	String[] conds;
	
}

// constructor
{
}

%union {
	public String value;

	// copy method
	public void yycopy(yyattribute source, boolean move) {
		YYSTYPE yy = (YYSTYPE)source;
		value = yy.value;
		if (move) {
			yy.value = null;
		}
	}
}

%left '+'
%left '*'
%right '!'

%left error

%type <value> line expr
%token <value> INTEGER 


%%

line
	:
	 expr			{ $$=$1;  exit(0,"");}
	| expr '\r'		{ $$=$1;  exit(0,"");}
	| expr '\n'		{ $$=$1;  exit(0,"");}
	| error '\n'	{ exit(-1,"表达式错误"); }
	;

expr
	: expr '+' expr	{ 
						if($1==null && $3==null)
							$$=null;
						else if($1==null)
							$$=$3;
							
						else if($3==null)
							$$=$1;
						else
						    $$=factor($1+" or "+$3);
					}
	| expr '*' expr	{ 
						if($1==null && $3==null)
							$$=null;
							
						else if($1==null)
							$$=$3;
							
						else if($3==null)
							$$=$1;
						else
						    $$=factor($1+" and "+$3);
					}
	| '!' expr		{ 
						if($2==null) 
							$$=null; 
						else
							$$="not"+$2+"";
					}
	| '(' expr ')'	{
						 if($2==null) 
							$$=null; 
						 else
          				    $$=factor($2);
					}
					
	| INTEGER		{
						int idx=Integer.parseInt($1);
						if(idx>conds.length)
							exit(-1,"所提供的条件数组比表达式中出现的少,相关提示：'"+$1+"'");
						else if(conds[idx-1]==null)
							$$=null;
						else
						{
							$$=factor(conds[idx-1]);
						}
					 }

;	
%%
	private String factor(String v)
	{
		return "("+v+")";
	}

	void  exit(int c,String err)
	{
		yyerror(err);
		yyexitcode=c;
		yyexitflg=true;	
	}
	
	public static void main(String args[]) throws Exception
	{
		String[] c=new String[3];
		c[0]="f1=2";
		c[1]=null;
		c[2]=null;
		
		parser("1*(2+3)",c);


		c[0]=null;
		c[1]=null;
		c[2]="f3=2";
		
		parser("(1*2)*3",c);

		c[0]=null;
		c[1]=null;
		c[2]="f3=2";
		
		parser("(1*2)*!3",c);

	}
	
	static public String parser(String input,String[]conds)throws Exception
	{
		return parser(input,conds,false);
	}
	
	static public String parser(String input,String[]conds,boolean debug)throws Exception
	{
		int ret = 0;
		cond_par parser = new cond_par();
		parser.yydebug=debug;
		ByteArrayOutputStream err=null;
		if (parser.create(input,conds,debug)) {
			parser.yyerr=new java.io.OutputStreamWriter(err=new java.io.ByteArrayOutputStream(256));
			ret = parser.yyparse();
		}
		
		if(ret!=0)
			throw new Exception(err.toString());

		if(debug)
			System.out.println(((YYSTYPE)parser.yyvalref).value);
		
		return ((YYSTYPE)parser.yyvalref).value;
	}


	/////////////////////////////////////////////////////////////////////////////
	// calc_parser commands

	public boolean create(String input,String[] conds,boolean debug) {
		cond_lex lexer = new cond_lex();
		if (!yycreate(lexer)) {
			return false;
		}
		if (!lexer.create(this)) {
			return false;
		}
		
		this.conds=conds;
		
		lexer.yyin=new InputStreamReader(new StringBufferInputStream(input));
		lexer.yydebug=debug;
		return true;	// success
	}
}
