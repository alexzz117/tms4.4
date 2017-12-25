/************************************************************
yyparser.java
This file can be freely modified for the generation of
custom code.

Copyright (c) 1999-2003 Bumble-Bee Software Ltd.
************************************************************/

package cn.com.higinet.tms35.core.cond.yl;
import java.io.*;

public abstract class yyparser extends yyobject {
	// yyparse return values
	public static final int YYEXIT_SUCCESS = 0;
	public static final int YYEXIT_FAILURE = 1;

	// common tokens
	public static final int YYTK_ALL = -1;		// match all tokens
	public static final int YYTK_END = 0;		// $end token
	public static final int YYTK_ERROR = 65536;	// error token

	// action types
	public static final int YYAT_SHIFT = 0;		// shift action
	public static final int YYAT_REDUCE = 1;	// reduce action
	public static final int YYAT_ERROR = 2;		// error
	public static final int YYAT_ACCEPT = 3;	// accept
	public static final int YYAT_DEFAULT = 4;	// default state

	// nonterminals
	public static final int YYNT_ALL = -1;		// match all nonterminals

	// states
	public static final int YYST_ERROR = -1;	// goto error

	// yyreduction
	public static final class yyreduction {
		public yyreduction(int nonterm, int length, int action) {
			yynonterm = (short)nonterm;
			yylength = (short)length;
			yyaction = (short)action;
		}
		public final short yynonterm;			// the rhs symbol
		public final short yylength;			// number of symbols on lhs
		public final short yyaction;			// the user action
	}

	// debugging
	public static final class yysymbol {
		public yysymbol(final String name, int token) {
			yyname = name;
			yytoken = token;
		}
		public final String yyname;				// symbol name
		public final int yytoken;				// symbol token
	}

	// attribute
	public abstract class yyattribute {
		public abstract void yycopy(yyattribute source, boolean move);
	}

	// yytables
	public static class yytables {
		public yytables() {
			yyreduction = null;
			yydestructorref = null;
			yysymbol = null;
			yyrule = null;
		}
		public yyreduction yyreduction[];
		public short yydestructorref[];
		public yysymbol yysymbol[];
		public String yyrule[];
	}

	// protected:
	// stack
	protected short yystackref[];			// (state) stack
	protected short yysstackref[];			// static (state) stack
	protected yyattribute yyattributestackref[];	// attribute stack
	protected yyattribute yysattributestackref[];	// static attribute stack
	protected int yysstack_size;			// initial number of elements in stack
	protected int yystack_max;				// maximum size of the stack
	protected int yytop;					// the current top of the stack
	protected yyattribute yyvalref;			// attribute for $$

	// lookahead token
	protected boolean yylookahead;			// whether current lookahead token is valid
	protected int yychar;					// current lookahead token

	// error recovery
	protected boolean yywipeflg;			// whether to "wipe" stack on abort
	protected boolean yypopflg;				// popping symbols during error recovery
	protected int yyskip;					// error recovery token shift counter

	// actions
	protected boolean yyexitflg;			// whether yymexit called
	protected boolean yyretireflg;			// whether yymretire called
	protected boolean yyerrorflg;			// whether yymforceerror called
	protected int yyexitcode;				// yymexit exit code
	protected int yyretirecode;				// yymretire exit code
	protected int yyerrorpop;				// how many error transitions to pop

	// public
	public yylexer yylexerref;				// pointer to the attached lexical analyser

	public boolean yystackgrow;				// whether stack can grow
	public yyattribute yylvalref;			// current token attribute
	public boolean yyerrflush;				// whether error stream should be flushed
	public OutputStreamWriter yyerr;		// error output file
	public int yyerrorcount;				// how many syntax errors have occurred

	// tables
	protected yyreduction yyreduction[];	// reduction array
	protected short yydestructorref[];		// destructor array

	// debugging
	public boolean yydebug;					// whether debug information should be output
	public boolean yydebugstack;			// whether stack debug information should be output
	public boolean yydebugflush;			// whether debug output should be flushed
	public OutputStreamWriter yydebugout;	// debug output file

	protected yysymbol yysymbol[];			// symbol array
	protected String yyrule[];				// rule array

	protected abstract yyattribute yynewattribute();
	protected abstract void yyaction(int action);

	// constructor
	public yyparser() {
		yystackref = null;
		yysstackref = null;
		yyattributestackref = null;
		yysattributestackref = null;
		yysstack_size = 0;
		yystack_max = 0;
		yytop = -1;
		yyvalref = null;
		yylookahead = false;
		yychar = -1;
		yywipeflg = true;
		yypopflg = false;
		yyskip = 0;
		yyexitflg = false;
		yyretireflg = false;
		yyerrorflg = false;
		yyexitcode = 0;
		yyretirecode = 0;
		yyerrorpop = 0;
		yylexerref = null;
		yystackgrow = true;
		yylvalref = null;
		yyerrflush = true;
		yyerr = new OutputStreamWriter(System.err);
		yyerrorcount = 0;

		// tables
		yyreduction = null;
		yydestructorref = null;

		// debugging
		yydebug = false;
		yydebugstack = false;
		yydebugflush = true;
		yydebugout = new OutputStreamWriter(System.out);
		yysymbol = null;
		yyrule = null;
	}

	// utility methods
	protected final void yypop(int num) {
		yytop -= num;

		// debugging
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				if (num > 0) {
					yydebugoutput("pop ");
					yydebugoutput(String.valueOf(num));
					yydebugoutput(" state(s)");
					if (yytop >= 0) {
						yydebugoutput(" uncovering state ");
						yydebugoutput(String.valueOf(yystackref[yytop]));
					}
					yydebugoutput(yygetlineseparator());
				}
			}
		}
	}

	protected final void yysetskip(int skip) {
		// debugging
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				if (skip > 0) {
					if (yyskip == 0) {
						yydebugoutput("entering error recovery" + yygetlineseparator());
					}
				}
				else {
					if (yyskip > 0) {
						yydebugoutput("leaving error recovery" + yygetlineseparator());
					}
				}
			}
		}

		yyskip = skip;
	}

	protected final boolean yypush(int state) {
		yytop++;		// increment first
		if (yytop == yystackref.length) {
			do {
				if (yystackgrow) {
					int size;
					if (yystackref.length != 0) {
						size = yystackref.length * 2;
						if (size / 2 != yystackref.length) {		// overflow check
							size = yystack_max;
						}
					}
					else {
						size = 100;
					}
					if (yystack_max != 0) {
						if (size > yystack_max) {
							size = yystack_max;
						}
					}
					if (size > yystackref.length) {
						if (yysetstacksize(size)) {
							break;
						}
					}
				}
				yytop--;

				// debugging
				if (YYDEBUG) {
					if (yygetglobaldebug() || yydebug) {
						yydebugoutput("stack overflow" + yygetlineseparator());
					}
				}

				yystackoverflow();
				return false;
			}
			while (false);
		}
		yystackref[yytop] = (short)state;

		// debugging
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("push state ");
				yydebugoutput(String.valueOf(state));
				if (yytop > 0) {
					yydebugoutput(" covering state ");
					yydebugoutput(String.valueOf(yystackref[yytop - 1]));
				}
				yydebugoutput(yygetlineseparator());

				// output stack contents
				if (yygetglobaldebugstack() || yydebugstack) {
					yydebugoutput(yygetlineseparator() + "stack");

					StringBuffer buffer = new StringBuffer(128);
					buffer.append(yygetlineseparator() + "     +");
					int i;
					for (i = 0; i < 10; i++) {
						buffer.append(' ');
						String string = String.valueOf(i);
						int length = string.length();
						if (length > 5) {
							length = 5;
						}
						for (int j = 0; j < 5 - length; i++) {
							buffer.append(' ');
						}
						buffer.append(string);
					}
					yydebugoutput(buffer.toString());

					int rows = 1;
					if (yytop >= 0) {
						rows += yytop / 10;
					}
					for (i = 0; i < rows; i++) {
						buffer.setLength(0);

						buffer.append(yygetlineseparator() + " ");
						String string = String.valueOf(10 * i);
						int length = string.length();
						if (length > 5) {
							length = 5;
						}
						for (int j = 0; j < 5 - length; j++) {
							buffer.append(' ');
						}
						buffer.append(string);

						for (int j = 0; j < 10; j++) {
							int index = 10 * i + j;
							if (index <= yytop) {
								string = String.valueOf(yystackref[index]);
							}
							else {
								string = new String("-");
							}
							length = string.length();
							if (length > 5) {
								length = 5;
							}
							for (int k = 0; k < 5 - length; k++) {
								buffer.append(' ');
							}
							buffer.append(string);
						}
						yydebugoutput(buffer.toString());
					}
					yydebugoutput(yygetlineseparator() + yygetlineseparator());
				}
			}
		}

		return true;
	}

	protected final short yypeek() {
		return yystackref[yytop];
	}

	// instance methods
	public final boolean yycreate(yylexer lexerref) {
		yylexerref = lexerref;

		// stack
		yysstackref = new short[yysstack_size];
		yysattributestackref = new yyattribute[yysstack_size];
		for (int i = 0; i < yysstack_size; i++) {
			yysattributestackref[i] = yynewattribute();
		}
		yystackref = yysstackref;
		yyattributestackref = yysattributestackref;

		// yylval
		yylvalref = yynewattribute();

		// yyval ($$)
		yyvalref = yynewattribute();

		return true;
	}

	public final boolean yycreate() {
		return yycreate(null);
	}

	public final void yydestroy() {
		yycleanup();
		yysstackref = null;
		yystackref = null;
		yysattributestackref = null;
		yyattributestackref = null;

		yylvalref = null;
		yyvalref = null;
	}

	// general methods
	public final void yydestructpop(int num) {
		if (yydestructorref != null) {
			while (num > 0) {
				short state = yypeek();
				int action = yydestructorref[state];
				if (action != -1) {
					// user actions in here
					yyvalref.yycopy(yyattributestackref[yytop], true);
					yypop(1);
					yyaction(action);
				}
				else {
					yypop(1);
				}
				num--;
			}
		}
		else {
			yypop(num);
		}
	}

	public final int yyparse() {
		int n = yysetup();
		if (n != 0) {
			return n;
		}
		return yywork();
	}

	public final void yycleanup() {
		yystackref = yysstackref;
		yyattributestackref = yysattributestackref;

		yytop = -1;
	}

	public final boolean yysetstacksize(int size) {
		if (yystackref.length != size) {
			if (size <= yytop) {
				return false;
			}

			if (yystack_max != 0) {
				if (size > yystack_max) {
					return false;
				}
			}

			// allocate
			short stackref[];
			yyattribute attributestackref[];
			if (size <= yysstack_size) {
				stackref = yysstackref;
				attributestackref = yysattributestackref;
			}
			else {
				stackref = new short[size];
				attributestackref = new yyattribute[size];
				int n = yymin(size, yystackref.length);
				for (int i = n; i < size; i++) {
					attributestackref[i] = yynewattribute();
				}
			}

			// copy
			if (stackref != yystackref) {
				int n = yymin(size, yystackref.length);
				for (int i = 0; i < n; i++) {
					stackref[i] = yystackref[i];
				}
			}
			if (attributestackref != yyattributestackref) {
				int n = yymin(size, yystackref.length);
				for (int i = 0; i < n; i++) {
					attributestackref[i] = yyattributestackref[i];
				}
			}

			// assign
			yystackref = stackref;
			yyattributestackref = attributestackref;
		}
		return true;
	}

	public final int yysetup() {
		// initialise variables
		yytop = -1;
		yylookahead = false;
		yyskip = 0;
		yyerrorcount = 0;
		yychar = -1;
		yypopflg = false;

		// push initial state onto the stack
		if (!yypush(0)) {
			if (YYDEBUG) {
				yydabort();
			}
			return 1;
		}
		return 0;
	}

	public final void yywipe() {
		yydestructpop(yytop + 1);
		yydestructclearin();
	}

	public abstract int yywork();
	public abstract void yydestructclearin();

	// service methods
	public int yygettoken() {
		return yylexerref.yylex();
	}

	public void yydiscard(int token) {
		// do nothing
	}

	public void yysyntaxerror() {
		yyerror("syntax error");
	}

	public void yystackoverflow() {
		String text = new String("parser stack overflow (" +
			String.valueOf(yystackref.length) + ")");
		yyerror(text);
	}

	public void yyerror(String text) {
		try {
			yyerr.write(text);
			yyerr.write(yygetlineseparator());
			if (yyerrflush) {
				yyerr.flush();
			}
		}
		catch (IOException e) {
			// do nothing
		}
	}

	// action methods
	public final void yysetin(int token) {
		if (token < 0) {
			token = 0;
		}
		yychar = token;
		yylookahead = true;
	}

	public final boolean yyunclearin() {
		if (!yylookahead && yychar != -1) {
			yylookahead = true;
			return true;
		}
		return false;
	}

	public final void yyabort() {
		yyexit(1);
	}

	public final void yyaccept() {
		yyexit(0);
	}

	public final void yyclearin() {
		yylookahead = false;
	}

	public final void yyerrok() {
		yysetskip(0);
	}

	public final void yyexit(int exitcode) {
		yyexitflg = true;
		yyexitcode = exitcode;
	}

	public final void yyforceerror() {
		yythrowerror(0);
	}

	public final boolean yypopping() {
		return yypopflg;
	}

	public final boolean yyrecovering() {
		return yyskip > 0;
	}

	public final void yyretire(int retirecode) {
		yyretireflg = true;
		yyretirecode = retirecode;
	}

	public final void yythrowerror(int pop) {
		yyerrorflg = true;
		yyerrorpop = pop;
	}

	public final void yythrowerror() {
		yythrowerror(0);
	}

	// debugging functions
	protected final String yytokenstring(int token) {
		int i = 0;
		while (yysymbol[i].yyname != null) {
			if (yysymbol[i].yytoken == token) {
				return yysymbol[i].yyname;
			}
			i++;
		}
		return "illegal-token";
	}

	protected final void yydgettoken(int token) {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				String tokenstring = yytokenstring(token);

				yydebugoutput("get token ");
				yydebugoutput(tokenstring);
				yydebugoutput(" (");
				yydebugoutput(String.valueOf(token));
				yydebugoutput(")" + yygetlineseparator());
			}
		}
	}

	protected final void yydshift(int token) {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				String tokenstring = yytokenstring(token);

				yydebugoutput("shift token ");
				yydebugoutput(tokenstring);
				yydebugoutput(" (");
				yydebugoutput(String.valueOf(token));
				yydebugoutput(")" + yygetlineseparator());
			}
		}
	}

	protected final void yydreduce(int rule) {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("reduce rule ");
				yydebugoutput(yyrule[rule]);
				yydebugoutput(" (");
				yydebugoutput(String.valueOf(rule));
				yydebugoutput(")" + yygetlineseparator());
			}
		}
	}

	protected final void yydsyntaxerror() {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("syntax error" + yygetlineseparator());
			}
		}
	}

	protected final void yydaccept() {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("accept" + yygetlineseparator());
			}
		}
	}

	protected final void yydabort() {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("abort" + yygetlineseparator());
			}
		}
	}

	protected final void yyddiscard(int token) {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				String tokenstring = yytokenstring(token);

				yydebugoutput("discard token ");
				yydebugoutput(tokenstring);
				yydebugoutput(" (");
				yydebugoutput(String.valueOf(token));
				yydebugoutput(")" + yygetlineseparator());
			}
		}
	}

	protected final void yydexit(int exitcode) {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("exit with code ");
				yydebugoutput(String.valueOf(exitcode));
				yydebugoutput(yygetlineseparator());
			}
		}
	}

	protected final void yydthrowerror(int errorpop) {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("throw error and pop ");
				yydebugoutput(String.valueOf(errorpop));
				yydebugoutput(" error handling state(s)" + yygetlineseparator());
			}
		}
	}

	protected final void yydretire(int retirecode) {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("retire with code ");
				yydebugoutput(String.valueOf(retirecode));
				yydebugoutput(yygetlineseparator());
			}
		}
	}

	protected final void yydattemptrecovery() {
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("attempting error recovery" + yygetlineseparator());
			}
		}
	}

	// debugging methods
	protected native final void OutputDebugString(final String string);
	protected native final void Sleep(int milliseconds);

	protected final void yydebugoutput(final String string) {
		if (yydebugout != null) {
			try {
				yydebugout.write(string);
				if (yygetglobaldebugflush() || yydebugflush) {
					yydebugout.flush();
				}
			}
			catch (IOException e) {
				// do nohting
			}
		}
		else {
			if (YYWIN32) {
				if (yygetwin32loaded()) {
					try {
						OutputDebugString(string);
						Sleep(0);
					}
					catch (UnsatisfiedLinkError e) {
						// do nothing
					}
				}
			}
		}
	}
}
