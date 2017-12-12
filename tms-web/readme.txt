属性文件外放的解决办法：

LINUX系统
1.新建一个配置的文件的目录
如/tms/profiles/tms-web/properties
需要外放的配置文件放这个目录下
2.确认sypay-applicationContext.xml配置如下
<context:property-placeholder location="classpath:profiles/tms-web/properties/*.properties" ignore-unresolvable="true" />
3.到tomcat/bin目录下新建一个文件setenv.sh,系统会自动加载这个文件
注意是/tms/profiles/tms-web/properties前面的/tms/,配置文件已经指定后面的目录
CLASSPATH=/tms/:$CLASSPATH



WINDOW系统
window下配置了setenv.sh/setenv.bat似乎不起作用，暂用绝对路径解决
把sypay-applicationContext.xml里面的这个注释去掉
<context:property-placeholder location="file:d:/workspace_sfpay/profiles/tms-web/properties/*.properties" ignore-unresolvable="true" />
这个注释
<!--<context:property-placeholder location="classpath:profiles/tms-web/properties/*.properties" ignore-unresolvable="true" />-->


