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
# cond_par.v
# YACC verbose file generated from cond_par.y.
# 
# Date: 12/10/10
# Time: 12:02:32
# 
# AYACC Version: 2.07
#############################################################################


##############################################################################
# Rules
##############################################################################

    0  $accept : line $end

    1  line : expr
    2       | expr '\r'
    3       | expr '\n'
    4       | error '\n'

    5  expr : expr '+' expr
    6       | expr '*' expr
    7       | '!' expr
    8       | '(' expr ')'
    9       | INTEGER


##############################################################################
# States
##############################################################################

state 0
	$accept : . line $end

	'!'  shift 1
	'('  shift 2
	error  shift 3
	INTEGER  shift 4

	line  goto 5
	expr  goto 6


state 1
	expr : '!' . expr

	'!'  shift 1
	'('  shift 2
	INTEGER  shift 4

	expr  goto 7


state 2
	expr : '(' . expr ')'

	'!'  shift 1
	'('  shift 2
	INTEGER  shift 4

	expr  goto 8


state 3
	line : error . '\n'

	'\n'  shift 9


state 4
	expr : INTEGER .  (9)

	.  reduce 9


state 5
	$accept : line . $end  (0)

	$end  accept


state 6
	line : expr .  (1)
	line : expr . '\r'
	line : expr . '\n'
	expr : expr . '+' expr
	expr : expr . '*' expr

	'\n'  shift 10
	'\r'  shift 11
	'*'  shift 12
	'+'  shift 13
	.  reduce 1


state 7
	expr : expr . '+' expr
	expr : expr . '*' expr
	expr : '!' expr .  (7)

	.  reduce 7


state 8
	expr : expr . '+' expr
	expr : expr . '*' expr
	expr : '(' expr . ')'

	')'  shift 14
	'*'  shift 12
	'+'  shift 13


state 9
	line : error '\n' .  (4)

	.  reduce 4


state 10
	line : expr '\n' .  (3)

	.  reduce 3


state 11
	line : expr '\r' .  (2)

	.  reduce 2


state 12
	expr : expr '*' . expr

	'!'  shift 1
	'('  shift 2
	INTEGER  shift 4

	expr  goto 15


state 13
	expr : expr '+' . expr

	'!'  shift 1
	'('  shift 2
	INTEGER  shift 4

	expr  goto 16


state 14
	expr : '(' expr ')' .  (8)

	.  reduce 8


state 15
	expr : expr . '+' expr
	expr : expr '*' expr .  (6)
	expr : expr . '*' expr

	.  reduce 6


state 16
	expr : expr '+' expr .  (5)
	expr : expr . '+' expr
	expr : expr . '*' expr

	'*'  shift 12
	.  reduce 5


##############################################################################
# Summary
##############################################################################

10 token(s), 3 nonterminal(s)
10 grammar rule(s), 18 state(s)


##############################################################################
# End of File
##############################################################################
