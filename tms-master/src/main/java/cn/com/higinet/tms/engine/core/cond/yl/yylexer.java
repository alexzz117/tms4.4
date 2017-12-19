/************************************************************
yylexer.java
This file can be freely modified for the generation of
custom code.

Copyright (c) 1999-2003 Bumble-Bee Software Ltd.
************************************************************/

package cn.com.higinet.tms.engine.core.cond.yl;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public abstract class yylexer extends yyobject {
	// yylex return values
	public static final int YYEOF = 0;		// end of file

	// yystate
	public static final class yystate {
		public yystate(int def, int base, int match) {
			yydef = (short)def;
			yybase = base;
			yymatch = (short)match;
		}
		public final short yydef;			// default state
		public final int yybase;			// base
		public final short yymatch;			// action associated with state
	}

	// yytables
	public static class yytables {
		public yytables() {
			yymatch = null;
			yystate = null;
			yybackup = null;
		}
		public short yymatch[];
		public yystate yystate[];
		public boolean yybackup[];
	}

	// protected
	// left context
	protected int yystart;					// current start state
	protected boolean yyeol;				// whether an end-of-line '\n' has been seen
	protected boolean yyoldeol;				// previous end-of-line value

	// text buffer
	protected int yystatebuf[];				// state buffer
	protected int yysstatebuf[];			// initial state buffer
	protected char yystext[];				// initial text buffer
	protected int yystext_size;				// initial text buffer size
	protected int yytext_max;				// maximum size of text the text buffer

	// unput buffer
	protected int yyunputbuf[];				// unput buffer
	protected int yysunputbuf[];			// initial unput buffer
	protected int yysunput_size;			// initial unput buffer size
	protected int yyunput_max;				// maximum size of the unput buffer
	protected int yyunputindex;				// unput buffer position

	// actions
	protected boolean yymoreflg;			// concatenate matched strings
	protected boolean yyrejectflg;			// yyreject called from an action
	protected boolean yyreturnflg;			// return from an action

	// public 
	public yyparser yyparserref;			// reference to the attached parser

	// buffer flags
	public boolean yytextgrow;				// whether text buffer is allowed to grow
	public boolean yyunputgrow;				// whether unput buffer is allowed to grow

	// streams
	public boolean yyoutflush;				// whether output stream should be flushed
	public boolean yyerrflush;				// whether error stream should be flushed
	public InputStreamReader yyin;			// input text stream
	public OutputStreamWriter yyout;		// output text stream
	public OutputStreamWriter yyerr;		// error stream

	// matched string
	public char yytext[];					// text buffer
	public int yyleng;						// matched string length
	public int yylineno;					// current line number

	// tables
	protected short yymatch[];				// match array
	protected yystate yystate[];			// state array
	protected boolean yybackup[];			// backup array

	// debugging
	public boolean yydebug;					// whether debug information should be output
	public boolean yydebugflush;			// whether debug output should be flushed
	public OutputStreamWriter yydebugout;	// debug output file

	public abstract int yylex();

	// constructor
	public yylexer() {
		yystart = 0;
		yyeol = true;
		yyoldeol = true;
		yystatebuf = null;
		yysstatebuf = null;
		yystext = null;
		yystext_size = 0;
		yytext_max = 0;
		yyunputbuf = null;
		yysunputbuf = null;
		yysunput_size = 0;
		yyunput_max = 0;
		yyunputindex = 0;
		yymoreflg = false;
		yyrejectflg = false;
		yyreturnflg = false;
		yyparserref = null;
		yytextgrow = true;
		yyunputgrow = true;
		yyoutflush = true;
		yyerrflush = true;
		yyin = new InputStreamReader(System.in);
		yyout = new OutputStreamWriter(System.out);
		yyerr = new OutputStreamWriter(System.err);
		yytext = null;
		yyleng = 0;
		yylineno = 1;

		// tables
		yymatch = null;
		yystate = null;
		yybackup = null;

		// debugging
		yydebug = false;
		yydebugflush = true;
		yydebugout = new OutputStreamWriter(System.out);
	}

	// helper methods
	protected static final boolean yyback(final short p[], int index, int action) {
		while (p[index] != 0) {
			if (p[index++] == action) {
				return true;
			}
		}
		return false;
	}

	// instance methods
	public final boolean yycreate(yyparser parserref) {
		yyparserref = parserref;

		// allocate the memory if necessary
		yystext = new char[yystext_size];
		yysstatebuf = new int[yystext_size];
		yysunputbuf = new int[yysunput_size];

		// assign any other variables
		yytext = yystext;
		yystatebuf = yysstatebuf;
		yyunputbuf = yysunputbuf;

		// makes sure we are ready to go
		yyreset();

		return true;
	}

	public final boolean yycreate() {
		return yycreate(null);
	}

	public final void yydestroy() {
		yycleanup();
		yystext = null;
		yytext = null;
		yysstatebuf = null;
		yystatebuf = null;
		yysunputbuf = null;
		yyunputbuf = null;
	}

	// general functions
	public final void yycleanup() {
		yytext = yystext;
		yystatebuf = yysstatebuf;
		yyunputbuf = yysunputbuf;

		yyleng = 0;
		yyunputindex = 0;
	}

	public final void yyreset() {
		yyleng = 0;
		yylineno = 1;
		yyunputindex = 0;
		yymoreflg = false;
		yyrejectflg = false;
		yyeol = true;
		yyoldeol = true;
		yystart = 0;
	}

	public final boolean yysettextsize(int size) {
		if (yytext.length != size) {
			if (size < yyleng) {
				return false;
			}

			if (yytext_max != 0) {
				if (size > yytext_max) {
					return false;
				}
			}

			// allocate
			char text[];
			int statebuf[];
			if (size <= yystext_size) {
				text = yystext;
				statebuf = yysstatebuf;
			}
			else {
				text = new char[size];
				statebuf = new int[size];
			}

			// copy
			if (text != yytext) {
				int n = yymin(size, yytext.length);
				for (int i = 0; i < n; i++) {
					text[i] = yytext[i];
				}
			}
			if (statebuf != yystatebuf) {
				int n = yymin(size, yystatebuf.length);
				for (int i = 0; i < n; i++) {
					statebuf[i] = yystatebuf[i];
				}
			}

			// assign
			yytext = text;
			yystatebuf = statebuf;
		}
		return true;
	}

	public final boolean yysetunputsize(int size) {
		if (yyunputbuf.length != size) {
			if (size < yyunputindex) {
				return false;
			}

			if (yyunput_max != 0) {
				if (size > yyunput_max) {
					return false;
				}
			}

			// allocate
			int unputbuf[];
			if (size <= yysunput_size) {
				unputbuf = yysunputbuf;
			}
			else {
				unputbuf = new int[size];
			}

			// copy
			if (unputbuf != yyunputbuf) {
				int n = yymin(size, yyunputbuf.length);
				for (int i = 0; i < n; i++) {
					unputbuf[i] = yyunputbuf[i];
				}
			}

			// assign
			yyunputbuf = unputbuf;
		}
		return true;
	}

	// service methods
	public int yygetchar() {
		try {
			return yyin.read();
		}
		catch (IOException e) {
			return -1;
		}
	}

	public int yyinput() {
		int ch;
		if (yyunputindex > 0) {
			ch = yyunputbuf[--yyunputindex];
		}
		else {
			ch = yygetchar();
		}
		if (ch == '\n') {
			yylineno++;
		}

		// debugging
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("input: \'");
				yydebugoutput(ch);
				if (ch >= 0) {
					String string = yygetcharhexstring(ch);
					yydebugoutput("\' (0x" + string + ")" + yygetlineseparator());
				}
				else {
					yydebugoutput("\' (" + String.valueOf(ch) + ")" + yygetlineseparator());
				}
			}
		}

		return ch;
	}

	public void yyoutput(int ch) {
		// debugging
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("output: \'");
				yydebugoutput(ch);
				String string = yygetcharhexstring(ch);
				yydebugoutput("\' (0x" + string + ")" + yygetlineseparator());
			}
		}

		try {
			yyout.write((char)ch);
			if (yyoutflush) {
				yyout.flush();
			}
		}
		catch (IOException e) {
			// do nothing
		}
	}

	public void yyunput(int ch) {
		// check unput buffer size
		if (yyunputindex == yyunputbuf.length) {
			do {
				if (yyunputgrow) {
					int size;
					if (yyunputbuf.length != 0) {
						size = yyunputbuf.length * 2;
						if (size / 2 != yyunputbuf.length) {		// overflow check
							size = yyunput_max;
						}
					}
					else {
						size = 100;
					}
					if (yyunput_max != 0) {
						if (size > yyunput_max) {
							size = yyunput_max;
						}
					}
					if (size > yyunputbuf.length) {
						if (yysetunputsize(size)) {
							break;
						}
					}
				}
				yyunputoverflow();
				System.exit(1);
			}
			while (false);
		}

		yyunputbuf[yyunputindex++] = ch;

		// check line number
		if (ch == '\n') {
			yylineno--;
		}

		// debugging
		if (YYDEBUG) {
			if (yygetglobaldebug() || yydebug) {
				yydebugoutput("unput: \'");
				yydebugoutput(ch);
				if (ch >= 0) {
					String string = yygetcharhexstring(ch);
					yydebugoutput("\' (0x" + string + ")" + yygetlineseparator());
				}
				else {
					yydebugoutput("\' (" + String.valueOf(ch) + ")" + yygetlineseparator());
				}
			}
		}
	}

	public int yywrap() {
		return 1;
	}

	public void yytextoverflow() {
		String text = new String("lexer text buffer overflow (" +
			String.valueOf(yytext.length) + ")");
		yyerror(text);
	}

	public void yyunputoverflow() {
		String text = new String("lexer unput buffer overflow (" +
			String.valueOf(yyunputbuf.length) + ")");
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

	public abstract int yyaction(int action);

	// action methods
	public final void yyecho() {
		for (int i = 0; i < yyleng; i++) {
			yyoutput(yytext[i]);
		}
	}

	public final void yyless(int length) {
		while (yyleng > length) {
			yyunput(yytext[--yyleng]);
		}
		if (yyleng > 0) {
			yyeol = yytext[yyleng - 1] == '\n';
		}
		else {
			yyeol = yyoldeol;
		}
	}

	public final void yybegin(int state) {
		yystart = state;
	}

	public final void yymore() {
		yymoreflg = true;
	}

	public final void yynewline(boolean newline) {
		yyeol = newline;
	}

	public final void yyreject() {
		yyrejectflg = true;
	}

	public final int yyunputcount() {
		return yyunputindex;
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
				// do nothing
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

	protected final void yydmatch(int expr) {
		if (yygetglobaldebug() || yydebug) {
			yydebugoutput("match: \"");
			for (int i = 0; i < yyleng; i++) {
				yydebugoutput(yytext[i]);
			}

			yydebugoutput("\", ");
			yydebugoutput(String.valueOf(expr));
			yydebugoutput(yygetlineseparator());
		}
	}

	protected final void yydebugoutput(int ch) {
		switch (ch) {
		case -1:
			yydebugoutput("EOF");
			break;
		case '\n':
			yydebugoutput("\\n");
			break;
		case '\t':
			yydebugoutput("\\t");
			break;
		case '\b':
			yydebugoutput("\\b");
			break;
		case '\r':
			yydebugoutput("\\r");
			break;
		case '\f':
			yydebugoutput("\\f");
			break;
		case '\\':
			yydebugoutput("\\");
			break;
		case '\'':
			yydebugoutput("\\\'");
			break;
		case '\"':
			yydebugoutput("\\\"");
			break;
		default:
			if (Character.isDefined((char)ch) && !Character.isISOControl((char)ch)) {
				char string[] = new char[1];
				string[0] = (char)ch;
				yydebugoutput(new String(string));
			}
			else {
				char string[] = new char[6];
				string[0] = '\\';
				string[1] = 'u';
				for (int i = 0; i < 4; i++) {
					int digit = ch >> 4 * (3 - i) & 0x0f;
					if (digit < 10) {
						digit += '0';
					}
					else {
						digit += 'a' - 10;
					}
					string[2 + i] = (char)digit;
				}
				yydebugoutput(new String(string));
			}
			break;
		}
	}

	protected final String yygetcharhexstring(int ch) {
		String string = Integer.toHexString(ch);
		int n = 4 - string.length();
		if (n > 0) {
			char pad[] = new char[n];
			for (int i = 0; i < n; i++) {
				pad[i] = '0';
			}
			string = new String(pad) + string;
		}
		return string;
	}
}
