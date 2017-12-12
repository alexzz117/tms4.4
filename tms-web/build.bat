@echo off
set CURRENT_PATH=%cd%
set JAVA_HOME=%JAVA_HOME%
set ANT_HOME=%ANT_HOME%
set CLASSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\jre\lib\rt.jar;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\ant-nodeps.jar;%ANT_HOME%\lib\ant-launcher.jar

%JAVA_HOME%/bin/java -cp %CLASSPATH% org.apache.tools.ant.Main -f %CURRENT_PATH%/build.xml %1 unpack

pause;