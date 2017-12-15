/************************************************************
yyfparser.java
This file can be freely modified for the generation of
custom code.

Copyright (c) 1999-2003 Bumble-Bee Software Ltd.
************************************************************/

package cn.com.higinet.tms35.core.cond.yl;

public abstract class yyfparser extends yyparser {
	// yystateaction
	public static final class yystateaction {
		public yystateaction(int base, boolean lookahead, int type, int sr) {
			yybase = base;
			yylookahead = lookahead;
			yytype = (byte)type;
			yysr = (short)sr;
		}
		public final int yybase;				// base
		public final boolean yylookahead;		// lookahead needed
		public final byte yytype;				// action to perform
		public final short yysr;				// shift/reduce
	}

	// yytokenaction
	public static final class yytokenaction {
		public yytokenaction(int check, int type, int sr) {
			yycheck = (short)check;
			yytype = (byte)type;
			yysr = (short)sr;
		}
		public final short yycheck;				// check
		public final byte yytype;				// action type
		public final short yysr;				// shift/reduce
	}

	// yystategoto
	public static final class yystategoto {
		public yystategoto(int base, int def) {
			yybase = (short)base;
			yydef = (short)def;
		}
		public final short yybase;				// base
		public final short yydef;				// default state
	}

	// yynontermgoto
	public static final class yynontermgoto {
		public yynontermgoto(int check, int next) {
			yycheck = (short)check;
			yynext = (short)next;
		}
		public final short yycheck;				// check
		public final short yynext;				// next state
	}

	// yytokendest
	public static final class yytokendest {
		public yytokendest(int check, int action) {
			yycheck = (short)check;
			yyaction = (short)action;
		}
		public final short yycheck;				// check
		public final short yyaction;			// action
	}

	// yyftables
	public static final class yyftables extends yytables {
		public yyftables() {
			yystateaction = null;
			yytokenaction = null;
			yystategoto = null;
			yynontermgoto = null;
			yytokendestref = null;
			yytokendestbaseref = null;
		}
		public yystateaction yystateaction[];
		public yytokenaction yytokenaction[];
		public yystategoto yystategoto[];
		public yynontermgoto yynontermgoto[];
		public yytokendest yytokendestref[];
		public int yytokendestbaseref[];
	}

	// tables
	protected yystateaction yystateaction[];
	protected yytokenaction yytokenaction[];
	protected yystategoto yystategoto[];
	protected yynontermgoto yynontermgoto[];
	protected yytokendest yytokendestref[];
	protected int yytokendestbaseref[];

	// constructor
	public yyfparser() {
		// tables
		yystateaction = null;
		yytokenaction = null;
		yystategoto = null;
		yynontermgoto = null;
		yytokendestref = null;
		yytokendestbaseref = null;
	}

	// methods
	public final int yywork() {
		int errorpop = 0;
		while (true) {
			byte type;
			short sr;
			short state = yypeek();			// get top state
			while (true) {
				if (yystateaction[state].yylookahead) {
					int index;
					if (!yylookahead) {
						yychar = yygettoken();
						if (yychar < 0) {
							yychar = 0;
						}
						yylookahead = true;
						if (YYDEBUG) {
							yydgettoken(yychar);
						}
					}
					index = yystateaction[state].yybase + yychar;
					if (index >= 0 && index < yytokenaction.length) {
						if (yytokenaction[index].yycheck == state) {
							type = yytokenaction[index].yytype;
							sr = yytokenaction[index].yysr;
							break;		// escape from loop
						}
					}
				}

				type = yystateaction[state].yytype;
				sr = yystateaction[state].yysr;
				if (type != YYAT_DEFAULT) {
					break;		// escape from loop
				}
				state = sr;
			}

			// action
			switch (type) {
			case YYAT_SHIFT:
				if (YYDEBUG) {
					yydshift(yychar);
				}
				if (yyskip > 0) {
					yysetskip(yyskip - 1);
				}
				if (!yypush(sr)) {
					if (YYDEBUG) {
						yydabort();
					}
					if (yywipeflg) {
						yywipe();	// clean up
					}
					return 1;
				}
				yyattributestackref[yytop].yycopy(yylvalref, true);
				yylookahead = false;
				continue;		// go to top of while loop
			case YYAT_REDUCE:
				if (YYDEBUG) {
					yydreduce(sr);
				}
				yyretireflg = false;
				if (yyreduction[sr].yyaction != -1) {
					// user actions in here
					if (yyreduction[sr].yylength > 0) {
						yyvalref.yycopy(yyattributestackref[yytop + 1 - yyreduction[sr].yylength], false);
					}

					yyerrorflg = false;
					yyexitflg = false;
					yyaction(yyreduction[sr].yyaction);

					// check for special user requected actions
					if (yyexitflg) {
						if (YYDEBUG) {
							yydexit(yyexitcode);
						}
						return yyexitcode;
					}
					if (yyerrorflg) {
						errorpop = yyerrorpop;
						if (YYDEBUG) {
							yydthrowerror(yyerrorpop);
						}
						yyerrorcount++;
						break;		// go to error handler
					}
				}

				yypop(yyreduction[sr].yylength);
				{
					state = yypeek();       // get top state
					short next;

					int nonterm = yyreduction[sr].yynonterm;
					while (true) {
						int index = yystategoto[state].yybase + nonterm;
						if (index >= 0 && index < yynontermgoto.length) {
							if (yynontermgoto[index].yycheck == state) {
								next = yynontermgoto[index].yynext;
								break;
							}
						}

						next = yystategoto[state].yydef;
						if (next == -1) {
							break;
						}
						state = next;
					}

					if (!yypush(next)) {
						if (YYDEBUG) {
							yydabort();
						}
						// clean up
						if (yydestructorref != null) {
							int action = yydestructorref[next];
							if (action != -1) {
								yyaction(action);
							}
						}
						if (yywipeflg) {
							yywipe();	// clean up
						}
						return 1;
					}
				}
				if (yyreduction[sr].yyaction != -1) {
					yyattributestackref[yytop].yycopy(yyvalref, true);
				}
				if (yyretireflg) {
					if (YYDEBUG) {
						yydretire(yyretirecode);
					}
					return yyretirecode;
				}
				continue;		// go to top of while loop
			case YYAT_ERROR:
				if (YYDEBUG) {
					yydsyntaxerror();
				}
				if (yyskip == 0) {
					yyerrorcount++;
					yysyntaxerror();
				}
				break;		// go to error handler
			default:
				if (YYDEBUG) {
					yydaccept();
				}
				return 0;
			}

			// error handler
			if (yyskip < 3 || errorpop > 0) {
				if (YYDEBUG) {
					yydattemptrecovery();
				}
				yypopflg = false;		// clear flag
				while (true) {
					state = yypeek();			// get top state
					while (true) {
						if (yystateaction[state].yylookahead) {
							int index = yystateaction[state].yybase + YYTK_ERROR;
							if (index >= 0 && index < yytokenaction.length) {
								if (yytokenaction[index].yycheck == state) {
									type = yytokenaction[index].yytype;
									sr = yytokenaction[index].yysr;
									break;		// escape from loop
								}
							}
						}

						type = yystateaction[state].yytype;
						sr = yystateaction[state].yysr;
						if (type != YYAT_DEFAULT) {
							break;		// escape from loop
						}
						state = sr;
					}

					if (type == YYAT_SHIFT) {
						if (errorpop <= 0) {
							if (YYDEBUG) {
								yydshift(YYTK_ERROR);
							}
							if (!yypush(sr)) {
								if (YYDEBUG) {
									yydabort();
								}
								if (yywipeflg) {
									yywipe();	// clean up
								}
								return 1;
							}
							yysetskip(3);		// skip 3 erroneous characters
							break;
						}
						errorpop--;
					}

					yypopflg = true;

					// clean up any symbol attributes
					if (yydestructorref != null) {
						state = yypeek();
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
					}
					else {
						yypop(1);
					}

					if (yytop < 0) {
						if (YYDEBUG) {
							yydabort();
						}
						if (yywipeflg) {
							yywipe();	// clean up
						}
						return 1;
					}
				}
			}
			else {
				if (yylookahead) {
					if (yychar != 0) {
						if (YYDEBUG) {
							yyddiscard(yychar);
						}
						yydiscard(yychar);

						// clean up any token attributes
						if (yytokendestbaseref != null) {
							for (int i = 0; i < yytokendestbaseref.length; i++) {
								int index = yytokendestbaseref[i] + yychar;
								if (index >= 0 && index < yytokendestref.length) {
									if (yytokendestref[index].yycheck == i) {
										// user actions in here
										yyvalref.yycopy(yylvalref, false);

										int action = yytokendestref[index].yyaction;
										yyaction(action);

										yylvalref.yycopy(yyvalref, true);
									}
								}
							}
						}

						yylookahead = false;	// skip erroneous character
					}
					else {
						if (YYDEBUG) {
							yydabort();
						}
						if (yywipeflg) {
							yywipe();	// clean up
						}
						return 1;
					}
				}
			}
		}
	}

	public final void yydestructclearin() {
		if (yylookahead) {
			// clean up any token attributes
			if (yytokendestbaseref != null) {
				for (int i = 0; i < yytokendestbaseref.length; i++) {
					int index = yytokendestbaseref[i] + yychar;
					if (index >= 0 && index < yytokendestref.length) {
						if (yytokendestref[index].yycheck == i) {
							// user actions in here
							yyvalref.yycopy(yylvalref, false);

							int action = yytokendestref[index].yyaction;
							yyaction(action);

							yylvalref.yycopy(yyvalref, true);
						}
					}
				}
			}
			yylookahead = false;
		}
	}
}
