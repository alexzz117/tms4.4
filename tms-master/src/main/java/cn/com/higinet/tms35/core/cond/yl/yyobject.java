/************************************************************
yyobject.java
This file can be freely modified for the generation of
custom code.

Copyright (c) 1999-2003 Bumble-Bee Software Ltd.
************************************************************/

package cn.com.higinet.tms35.core.cond.yl;

public class yyobject {
	// constants
	public static final boolean YYDEBUG = true;
	public static final boolean YYWIN32 = false;

	// debug global variables
	private static boolean yyglobaldebug;		// whether debug information should be output
	private static boolean yyglobaldebugstack;	// whether stack debug information should be output
	private static boolean yyglobaldebugflush;	// whether debug output should be flushed

	private static boolean yywin32loaded;		// whether the ylwin32 library has loaded

	protected static String yylineseparator;	// line separator

	// class initialisation
	static {
		yyglobaldebug = false;
		yyglobaldebugstack = false;
		yyglobaldebugflush = false;

		// connect to the ylwin32 dll
		yywin32loaded = false;
		if (YYWIN32) {
			try {
				System.loadLibrary("ylwin32");
				yywin32loaded = true;
			}
			catch (SecurityException e) {
				// do nothing
			}
			catch (UnsatisfiedLinkError e) {
				// do nothing
			}
		}

		// get the line separator
		try {
			yylineseparator = System.getProperty("line.separator");
		}
		catch (SecurityException e) {
			yylineseparator = new String("\n");
		}
	}

	// methods
	public static synchronized final boolean yygetglobaldebug() {
		return yyglobaldebug;
	}

	public static synchronized final void yysetglobaldebug(boolean globaldebug) {
		yyglobaldebug = globaldebug;
	}

	public static synchronized final boolean yygetglobaldebugstack() {
		return yyglobaldebugstack;
	}

	public static synchronized final void yysetglobaldebugstack(boolean globaldebugstack) {
		yyglobaldebugstack = globaldebugstack;
	}

	public static synchronized final boolean yygetglobaldebugflush() {
		return yyglobaldebugflush;
	}

	public static synchronized final void yysetglobaldebugflush(boolean globaldebugflush) {
		yyglobaldebugflush = globaldebugflush;
	}

	public static synchronized final boolean yygetwin32loaded() {
		return yywin32loaded;
	}

	public static synchronized final void yysetwin32loaded(boolean win32loaded) {
		yywin32loaded = win32loaded;
	}

	// helper methods
	protected static final int yymin(int x, int y) {
		return x < y ? x : y;
	}

	protected String yygetlineseparator() {
		return yylineseparator;
	}
}
