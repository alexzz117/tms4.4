/************************************************************
yycparser.java
This file can be freely modified for the generation of
custom code.

Copyright (c) 1999-2003 Bumble-Bee Software Ltd.
************************************************************/

package cn.com.higinet.tms.engine.core.cond.yl;

public abstract class yycparser extends yyparser {
	// yyctokenaction
	public static final class yyctokenaction {
		public yyctokenaction(int token, int type, int sr) {
			yytoken = token;
			yytype = (byte)type;
			yysr = (short)sr;
		}
		public final int yytoken;				// lookahead token
		public final byte yytype;				// action to perform
		public final short yysr;				// state to shift/production to reduce
	}

	// yycnontermgoto
	public static final class yycnontermgoto {
		public yycnontermgoto(int nonterm, int next) {
			yynonterm = (short)nonterm;
			yynext = (short)next;
		}
		public final short yynonterm;			// nonterminal
		public final short yynext;				// next state
	}

	// yyctokendest
	public static final class yyctokendest {
		public yyctokendest(int token, int action) {
			yytoken = token;
			yyaction = (short)action;
		}
		public final int yytoken;				// token
		public final short yyaction;			// the user action
	}

	// yyctables
	public static final class yyctables extends yytables {
		public yyctables() {
			yycstateaction = null;
			yyctokenaction = null;
			yycstategoto = null;
			yycnontermgoto = null;
			yyctokendestref = null;
		}
		public short yycstateaction[];
		public yyctokenaction yyctokenaction[];
		public short yycstategoto[];
		public yycnontermgoto yycnontermgoto[];
		public yyctokendest yyctokendestref[];
	}

	// tables
	protected short yycstateaction[];
	protected yyctokenaction yyctokenaction[];
	protected short yycstategoto[];
	protected yycnontermgoto yycnontermgoto[];
	protected yyctokendest yyctokendestref[];

	// constructor
	public yycparser() {
		// tables
		yycstateaction = null;
		yyctokenaction = null;
		yycstategoto = null;
		yycnontermgoto = null;
		yyctokendestref = null;
	}

	// methods
	public final int yywork() {
		int errorpop = 0;
		while (true) {
			short state = yypeek();			// get top state

			int index = yycstateaction[state];
			while (true) {
				if (yyctokenaction[index].yytoken == YYTK_ALL) {
					if (yyctokenaction[index].yytype == YYAT_DEFAULT) {
						state = yyctokenaction[index].yysr;
						index = yycstateaction[state];
						continue;
					}
					break;
				}

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
				if (yyctokenaction[index].yytoken == yychar) {
					break;
				}
				index++;
			}
			byte type = yyctokenaction[index].yytype;
			short sr = yyctokenaction[index].yysr;

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

					int nonterm = yyreduction[sr].yynonterm;
					index = yycstategoto[state];
					while (true) {
						if (yycnontermgoto[index].yynonterm == -1) {
							if (yycnontermgoto[index].yynext != -1) {
								state = yycnontermgoto[index].yynext;
								index = yycstategoto[state];
								continue;
							}
							break;
						}
						if (yycnontermgoto[index].yynonterm == nonterm) {
							break;
						}
						index++;
					}
					short next = yycnontermgoto[index].yynext;

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
			if (yyskip < 3 || yyerrorpop > 0) {
				if (YYDEBUG) {
					yydattemptrecovery();
				}
				yypopflg = false;		// clear flag
				while (yytop >= 0) {
					state = yypeek();			// get top state
					index = yycstateaction[state];
					while (true) {
						if (yyctokenaction[index].yytoken == YYTK_ALL) {
							if (yyctokenaction[index].yytype == YYAT_DEFAULT) {
								state = yyctokenaction[index].yysr;
								index = yycstateaction[state];
								continue;
							}
							break;
						}
						if (yyctokenaction[index].yytoken == YYTK_ERROR) {
							break;
						}
						index++;
					}
					type = yyctokenaction[index].yytype;
					sr = yyctokenaction[index].yysr;

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
						if (yyctokendestref != null) {
							int i = 0;
							while (yyctokendestref[i].yytoken != -1) {
								if (yyctokendestref[i].yytoken == yychar) {
									// user actions in here
									yyvalref.yycopy(yylvalref, false);

									yyaction(yyctokendestref[i].yyaction);

									yylvalref.yycopy(yyvalref, true);
									break;
								}
								i++;
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
			if (yyctokendestref != null) {
				int i = 0;
				while (yyctokendestref[i].yytoken != -1) {
					if (yyctokendestref[i].yytoken == yychar) {
						// user actions in here
						yyvalref.yycopy(yylvalref, false);

						yyaction(yyctokendestref[i].yyaction);

						yylvalref.yycopy(yyvalref, true);
						break;
					}
					i++;
				}
			}
			yylookahead = false;
		}
	}
}
