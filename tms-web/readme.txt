�����ļ���ŵĽ���취��

LINUXϵͳ
1.�½�һ�����õ��ļ���Ŀ¼
��/tms/profiles/tms-web/properties
��Ҫ��ŵ������ļ������Ŀ¼��
2.ȷ��sypay-applicationContext.xml��������
<context:property-placeholder location="classpath:profiles/tms-web/properties/*.properties" ignore-unresolvable="true" />
3.��tomcat/binĿ¼���½�һ���ļ�setenv.sh,ϵͳ���Զ���������ļ�
ע����/tms/profiles/tms-web/propertiesǰ���/tms/,�����ļ��Ѿ�ָ�������Ŀ¼
CLASSPATH=/tms/:$CLASSPATH



WINDOWϵͳ
window��������setenv.sh/setenv.bat�ƺ��������ã����þ���·�����
��sypay-applicationContext.xml��������ע��ȥ��
<context:property-placeholder location="file:d:/workspace_sfpay/profiles/tms-web/properties/*.properties" ignore-unresolvable="true" />
���ע��
<!--<context:property-placeholder location="classpath:profiles/tms-web/properties/*.properties" ignore-unresolvable="true" />-->


