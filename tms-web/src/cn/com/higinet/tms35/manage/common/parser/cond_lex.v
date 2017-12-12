#############################################################################
#                     U N R E G I S T E R E D   C O P Y
# 
# You are on day 1 of your 30 day trial period.
# 
# This file was produced by an UNREGISTERED COPY of Parser Generator. It is
# for evaluation purposes only. If you continue to use Parser Generator 30
# days after installation then you are required to purchase a license. For
# more information see the online help or go to the Bumble-Bee Software
# homepage at:
# 
# http://www.bumblebeesoftware.com
# 
# This notice must remain present in the file. It cannot be removed.
#############################################################################

#############################################################################
# cond_lex.v
# Lex verbose file generated from cond_lex.l.
# 
# Date: 12/10/10
# Time: 12:02:32
# 
# ALex Version: 2.07
#############################################################################


#############################################################################
# Expressions
#############################################################################

    1  [0-9]+

    2  [^ \t\r\n\f]

    3  [ \t\r\n\f]


#############################################################################
# States
#############################################################################

state 1
	INITIAL

	0x0000 - 0x0008 (9)      goto 3
	0x0009 - 0x000a (2)      goto 4
	0x000b                   goto 3
	0x000c - 0x000d (2)      goto 4
	0x000e - 0x001f (18)     goto 3
	0x0020                   goto 4
	0x0021 - 0x002f (15)     goto 3
	0x0030 - 0x0039 (10)     goto 5
	0x003a - 0xffff (65478)  goto 3


state 2
	^INITIAL

	0x0000 - 0x0008 (9)      goto 3
	0x0009 - 0x000a (2)      goto 4
	0x000b                   goto 3
	0x000c - 0x000d (2)      goto 4
	0x000e - 0x001f (18)     goto 3
	0x0020                   goto 4
	0x0021 - 0x002f (15)     goto 3
	0x0030 - 0x0039 (10)     goto 5
	0x003a - 0xffff (65478)  goto 3


state 3
	match 2


state 4
	match 3


state 5
	0x0030 - 0x0039 (10)     goto 5

	match 1


#############################################################################
# Summary
#############################################################################

1 start state(s)
3 expression(s), 5 state(s)


#############################################################################
# End of File
#############################################################################
