package cn.com.higinet.tms.manager.modules.common.parser;

import cn.com.higinet.tms.engine.core.cond.yl.yyflexer;

// line 1 ".\\cond_lex.l"

/////////////////////////////////////////////////////////////////////////////
// cond_lex

public class cond_lex extends yyflexer {
	// line 11 ".\\cond_lex.l"

	// Attributes

	// line 44 "cond_lex.java"
	public cond_lex() {
		yytables();
		// line 16 ".\\cond_lex.l"

		// do nothing

		// line 51 "cond_lex.java"
	}

	public static final int INITIAL = 0;

	protected static yyftables yytables = null;

	public final int yyaction(int action) {
		// line 26 ".\\cond_lex.l"

		cond_par.YYSTYPE yylval = (cond_par.YYSTYPE) yyparserref.yylvalref;

		// line 63 "cond_lex.java"
		yyreturnflg = true;
		switch (action) {
		case 1: {
			// line 34 ".\\cond_lex.l"
			yylval.value = new String(yytext, 0, yyleng);
			return cond_par.INTEGER;
			// line 70 "cond_lex.java"
		}
		case 2: {
			// line 36 ".\\cond_lex.l"
			return yytext[0];
			// line 76 "cond_lex.java"
		}
		case 3: {
			// line 40 ".\\cond_lex.l"

			// line 82 "cond_lex.java"
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

			final short match[] = { 0 };
			yytables.yymatch = match;

			final yytransition transition[] = { new yytransition(0, 0),
					new yytransition(4, 1), new yytransition(4, 1),
					new yytransition(0, 0), new yytransition(4, 1),
					new yytransition(4, 1), new yytransition(5, 5),
					new yytransition(5, 5), new yytransition(5, 5),
					new yytransition(5, 5), new yytransition(5, 5),
					new yytransition(5, 5), new yytransition(5, 5),
					new yytransition(5, 5), new yytransition(5, 5),
					new yytransition(5, 5), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(4, 1),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(0, 0),
					new yytransition(0, 0), new yytransition(5, 1),
					new yytransition(5, 1), new yytransition(5, 1),
					new yytransition(5, 1), new yytransition(5, 1),
					new yytransition(5, 1), new yytransition(5, 1),
					new yytransition(5, 1), new yytransition(5, 1),
					new yytransition(5, 1) };
			yytables.yytransition = transition;

			final yystate state[] = { new yystate(0, 0, 0),
					new yystate(-3, -8, 0), new yystate(1, 0, 0),
					new yystate(0, 0, 2), new yystate(0, 0, 3),
					new yystate(0, -42, 1) };
			yytables.yystate = state;

			final boolean backup[] = { false, false, false, false };
			yytables.yybackup = backup;
		}
	}

	// line 42 ".\\cond_lex.l"

	public boolean create(cond_par parser) {

		return yycreate(parser);
	}
}
