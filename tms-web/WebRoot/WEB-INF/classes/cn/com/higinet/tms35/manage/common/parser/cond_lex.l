%{
package com.richtone.query.parser;
import com.richtone.yl.*;
%}


// lexical analyser name
%name cond_lex

// class definition
{
	// Attributes
}

// constructor
{
	// do nothing
}

// macros
integer   [0-9]+


%%

%{
		cond_par.YYSTYPE yylval = (cond_par.YYSTYPE)yyparserref.yylvalref;
%}

// return expressions
%return

// number
{integer} { yylval.value=new String(yytext,0,yyleng);return cond_par.INTEGER; }

[^ \t\r\n\f]				{return yytext[0];}
// white space
%break

[ \t\r\n\f]					{}

%%
	public boolean create(cond_par parser) 
	{
	
		return yycreate(parser);
	}
}
