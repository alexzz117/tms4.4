/************************************************************
yyflexer.java
This file can be freely modified for the generation of
custom code.

Copyright (c) 1999-2003 Bumble-Bee Software Ltd.
************************************************************/

package cn.com.higinet.tms35.core.cond.yl;

public abstract class yyflexer extends yylexer {
	// yytransition
	public static final class yytransition {
		public yytransition(int next, int check) {
			yynext = (short)next;
			yycheck = (short)check;
		}
		public final short yynext;			// next state on transition
		public final short yycheck;			// check
	}

	// yyftables
	public static final class yyftables extends yytables {
		public yyftables() {
			yytransition = null;
		}
		public yytransition yytransition[];
	}

	// tables
	protected yytransition yytransition[];	// transition array

	// constructor
	public yyflexer() {
		// tables
		yytransition = null;
	}

	// methods
	public final int yylex() {
		while (true) {
			int state = 1 + yystart;
			if (yyeol) {
				state++;
			}

			// yymore
			if (yymoreflg) {
				yymoreflg = false;		// clear flag
			}
			else {
				yyleng = 0;
				yyoldeol = yyeol;
			}
			int oldleng = yyleng;

			// look for a string
			do {
				// get input character (look ahead character)
				int ch = yyinput();
				if (ch == -1) {
					break;
				}

				// check for possible overflow
				if (yyleng == yytext.length) {
					do {
						if (yytextgrow) {
							int size;
							if (yytext.length != 0) {
								size = yytext.length * 2;
								if (size / 2 != yytext.length) {		// overflow check
									size = yytext_max;
								}
							}
							else {
								size = 100;
							}
							if (yytext_max != 0) {
								if (size > yytext_max) {
									size = yytext_max;
								}
							}
							if (size > yytext.length) {
								if (yysettextsize(size)) {
									break;
								}
							}
						}
						yytextoverflow();
						System.exit(1);
					}
					while (false);
				}

				// look for a transition
				do {
					int index = yystate[state].yybase + ch;
					if (index >= 0 && index < yytransition.length) {
						if (yytransition[index].yycheck == state) {
							state = yytransition[index].yynext;
							break;	// found a transition
						}
					}
					state = yystate[state].yydef;
					if (state < 0) {
						if (ch >= 0 && ch <= 0xffff) {
							state = -state;
						}
						else {
							state = 0;
						}
						break;		// default transition
					}
				}
				while (state != 0);

				int leng = yyleng;		// slightly more efficient
				yytext[leng] = (char)ch;
				yystatebuf[leng] = state;
				leng++;
				yyleng = leng;
			}
			while (state != 0 && (yystate[state].yydef != 0 || yystate[state].yybase != 0));

			// now find a match
			if (yyleng > oldleng) {
				int rejectmatch = 0;
				while (true) {
					int match = yystate[yystatebuf[yyleng - 1]].yymatch;
					if (rejectmatch != 0) {
						if (match < 0) {
							int index = -match;
							do {
								match = yymatch[index++];
							}
							while (match > 0 && match <= rejectmatch);
						}
						else {
							if (match == rejectmatch) {
								match = 0;
							}
						}
						rejectmatch = 0;
					}
					else {
						if (match < 0) {
							match = yymatch[-match];
						}
					}
					if (match > 0) {
						// check for backup
						if (yybackup[match]) {
							while (yyleng > oldleng) {
								int index = yystate[yystatebuf[yyleng - 1]].yymatch;
								if (index < 0) {
									if (yyback(yymatch, -index, -match)) {
										break;	// found an expression
									}
								}
								yyleng--;
								yyunput(yytext[yyleng]);
							}
						}
						if (YYDEBUG) {
							yydmatch(match);
						}
						yyrejectflg = false;		// clear flag
						int rejectleng = yyleng;

						if (yyleng > 0) {
							yyeol = yytext[yyleng - 1] == '\n';
						}
						else {
							yyeol = yyoldeol;
						}

						// perform user action
						int token = yyaction(match);

						if (yyreturnflg) {
							return token;
						}
						if (!yyrejectflg) {
							break;
						}
						if (rejectleng == yyleng) {
							rejectmatch = match;
						}
					}
					else if (yyleng > oldleng + 1) {
						yyleng--;
						yyunput(yytext[yyleng]);
					}
					else {
						yyeol = yytext[0] == '\n';
						yyoutput(yytext[0]);	// non-matched character
						break;
					}
				}
			}
			else {
				// handles <<EOF>> rules
				int index = 0;
				int match = yystate[state].yymatch;
				if (match < 0) {
					index = -match;
					match = yymatch[index++];
				}
				while (match > 0) {
					if (YYDEBUG) {
						yydmatch(match);
					}
					yyrejectflg = false;		// clear flag

					// perform user action
					int token = yyaction(match);

					if (yyreturnflg) {
						return token;
					}
					if (!yyrejectflg) {
						break;
					}

					if (index == 0) {
						break;
					}
					match = yymatch[index++];
				}

				if (yywrap() != 0) {
					yyoldeol = true;
					yyeol = true;
					yystart = 0;
					return 0;			// eof reached
				}
			}
		}
	}
}
