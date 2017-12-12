@echo off&setlocal enabledelayedexpansion
rem denpend copy
cls

set DEPEND_CONF=depend.conf
set CURRENT_PATH=%cd%

echo.
echo =====================================
echo.
echo    depend copy.
echo.
echo    DEPEND_LIB_HOME=%DEPEND_LIB_HOME%
echo    CURRENT_PATH=%CURRENT_PATH%
echo.
echo =====================================

if not defined DEPEND_LIB_HOME (
	echo DEPEND_LIB_HOME is not defined.
	goto exit
)

if not exist "%DEPEND_LIB_HOME%" (
	echo DEPEND_LIB_HOME is not exist.
)

if not exist "%DEPEND_CONF%" (
	echo configfile miss: 
	echo   "%CURRENT_PATH%\%DEPEND_CONF%" is not exist.
) else (
    for /f "eol=# tokens=1,2* delims=> " %%i in (%DEPEND_CONF%) do (
		set aa=%%i
		set bb=%%j
		set aa=!aa:/=\!
		set bb=!bb:/=\!
		echo !aa! ^> !bb!
		copy "%DEPEND_LIB_HOME%\!aa!" "%CURRENT_PATH%\!bb!" > nul
		if !errorlevel!==0 (echo ok. )
	)
)

:exit
echo finish.
pause
