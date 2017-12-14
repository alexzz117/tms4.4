package cn.com.higinet.tms.manager.modules.common.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Administrator
 *通用工具包中有关字符串处理的工具类
 */
public class StringUtil {
	private static final Log log = LogFactory.getLog(StringUtil.class);
	/**
	 * <p>
	 * 缺省构造函数
	 * </p>
	 */
	public StringUtil() {

	}

	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	/**
	 * turn GBK encoding into ISO8859_1 encoding
	 * 
	 * @param inputstring
	 * @return null if there is any exception
	 */
	public static String gbk2e(String inputstring) {
		if (inputstring == null) {
			return null;
		}

		String outputstring = "";
		try {
			byte[] bytearray = inputstring.getBytes("GBK");
			outputstring = new String(bytearray, "ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			log.error("StringUtil method gbk2e error",e);
		}
		return outputstring;
	}
	/**
	 * turn GBK encoding into ISO8859_1 encoding
	 * 
	 * @param inputstring
	 * @return null if there is any exception
	 */
	public static String gbk2Utf(String inputstring) {
		if (inputstring == null) {
			return null;
		}

		String outputstring = "";
		try {
			byte[] bytearray = inputstring.getBytes("GBK");
			outputstring = new String(bytearray, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("StringUtil method gbk2Utf error",e);
		}
		return outputstring;
	}
	/**
	 * 字符串不为空返回true
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str){
		return !isEmpty(str);
	}
	/**
	 * 如果字符串为空返回
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		return (str == null || "".equals(str));
	}
	
	/**
	 * 判断字符串是否为空
	 * @param value
	 * @return
	 */
	public static boolean isBlank(String value){
        return value == null || "".equals(value.trim());
    }
	
	/**
	 * 判断两个字符串是否相等
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean isEqual(String str1,String str2){
		if(isEmpty(str1)&&isEmpty(str2)){
			return true;
		}else{
			return str1.equals(str2);
		}
	}
	/**
	 * translate default character's code  to GBK
	 * @param inputstring
	 * @return
	 */
    public static String default2GBK(String inputstring){
    	if (inputstring == null) {
			return null;
		}
		String outputstring = "";
		try {
			byte[] bytearray = inputstring.getBytes();
			outputstring = new String(bytearray,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("StringUtil method default2GBK error",e);
		}
		return outputstring;
    }
	/**
	 * turn ISO8859_1 encoding into GBK encoding
	 * 
	 * @param inputstring
	 * @return null if there is any exception
	 */
	public static String e2gbk(String inputstring, boolean isDeCode) {
		if (inputstring == null) {
			return null;
		}

		String outputstring = "";
		if (isDeCode) {
			try {
				outputstring = new String(inputstring.getBytes("8859_1"), "GBK");
			} catch (java.io.UnsupportedEncodingException e) {
				log.error("StringUtil method e2gbk error",e);
			}
		}
		return outputstring;
	}

	/**
	 * replace substring with new string
	 * 
	 * @param inputstr
	 *            source string
	 * @param oldsub
	 *            sub string to be replaced
	 * @param newsub
	 *            string to be replaced with
	 * @return
	 */
	public static String replace(String inputstr, String oldsub, String newsub) {
		if (inputstr == null) {
			return null;
		}
		if (oldsub == null || oldsub.equals("")) {
			return inputstr;
		}
		if (newsub == null) {
			return inputstr;
		}

		String outputstr = "";
		int oldlen = oldsub.length();
		while (true) {
			int position = inputstr.indexOf(oldsub);
			if (position != -1) {
				outputstr += inputstr.substring(0, position) + newsub;
				inputstr = inputstr.substring(position + oldlen);
				continue;
			}
			outputstr += inputstr;
			break;
		}
		return outputstr;
	}

	/**
	 * get count number of index string
	 * 
	 * @param inputstr
	 *            source string
	 * @param indexstr
	 *            index string
	 * @param overlap
	 *            overlap index string counted
	 * @return -1 if inputstr is null
	 */
	public static int indexCount(String inputstr, String indexstr,
			boolean overlap) {
		if (inputstr == null) {
			return -1;
		}
		if (indexstr == null || "".equals(indexstr)) {
			return 0;
		}

		int no = 0;
		int indexlen = indexstr.length();
		while (true) {
			int position = inputstr.indexOf(indexstr);
			if (position != -1) {
				no++;
				if (overlap == true) {
					inputstr = inputstr.substring(position + 1);
				} else {
					inputstr = inputstr.substring(position + indexlen);
				}
				continue;
			}
			break;
		}
		return no;
	}

	/**
	 * split string with substring
	 * 
	 * @param inputstr
	 *            source string
	 * @param div
	 *            split string
	 * @return string array
	 */
	public static String[] split(String inputstr, String div) {
		if (inputstr == null) {
			return null;
		}

		String[] outputstr;
		int arraylength = StringUtil.indexCount(inputstr, div, false);
		if (arraylength == 0) {
			outputstr = new String[1];
			outputstr[0] = inputstr;
			return outputstr;
		}

		outputstr = new String[arraylength + 1];
		int divlength = div.length();
		for (int i = 0; i < arraylength; i++) {
			int position = inputstr.indexOf(div);
			outputstr[i] = inputstr.substring(0, position);
			inputstr = inputstr.substring(position + divlength);
		}
		outputstr[arraylength] = inputstr;
		return outputstr;
	}

	/**
	 * method: str2Array
	 * 
	 * @param input
	 * @param delim
	 * @return return type: String[]
	 */
	public static final String[] str2Array(String input, String delim) {
		if (input == null)
			throw new IllegalArgumentException(
					"StringUtil.str2Array(input,delim),input\u00CA\u00E4\u00C8\u00EB\u00CE\u00AA\u00BF\u00D5");
		ArrayList accountList = new ArrayList();
		StringTokenizer st = new StringTokenizer(input, delim);

		while (st.hasMoreTokens()) {
			String account = st.nextToken();

			accountList.add(account);
		}
		Object[] values = accountList.toArray();
		String[] returnValues = new String[values.length];
		for (int i = 0; i < returnValues.length; i++) {
			returnValues[i] = (String) values[i];
		}
		return returnValues;
	}

	/**
	 * html encode
	 * 
	 * @param str
	 *            source string
	 * @param newline
	 *            new line translate
	 * @param singleQuotes
	 *            singleQuote translate
	 * @return
	 */
	public static String htmlEncode(String str, boolean newline,
			boolean singleQuotes) {
		if (str == null) {
			return null;
		}

		str = doHTML(str);
		if (newline == true) {
			str = StringUtil.replace(str, "\n", "<br>");
		}
		if (singleQuotes == true) {
			str = StringUtil.replace(str, "'", "''");
		}
		return str;
	}

	/**
	 * get position of the substring of No.n
	 * 
	 * @param str
	 *            source string
	 * @param index
	 *            substring
	 * @param n
	 *            No.n
	 * @param caseSensitive
	 *            case sensitive
	 * @return -1 if any exception
	 */
	public static int indexOf(String str, String index, int n,
			boolean caseSensitive) {
		if (str == null || index == null || n <= 0) {
			return -1;
		}

		if (caseSensitive) {
			str = str.toUpperCase();
			index = index.toUpperCase();
		}

		int pos = -1;
		for (int i = 1; i <= n; i++) {
			pos += 1;
			pos = str.indexOf(index, pos);
			if (pos == -1)
				return pos;
		}
		return pos;
	}

	/**
	 * get position of the substring
	 * 
	 * @param str
	 *            source array
	 * @param index
	 *            index array
	 * @return -1 if any exception
	 */
	public static int indexOf(byte[] str, byte[] index) {
		if (str == null || index == null) {
			return -1;
		}

		int strlength = str.length;
		int indexlength = index.length;
		for (int i = 0; i < strlength; i++) {
			if (str[i] != index[0]) {
				continue;
			}
			for (int j = 0; j < indexlength; j++) {
				if (i + j >= strlength) {
					break;
				}
				if (str[i + j] != index[j]) {
					break;
				}
				if (j == indexlength - 1) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * get position of the substring
	 * 
	 * @param str
	 *            source array
	 * @param index
	 *            index array
	 * @param from
	 *            from position
	 * @return -1 if any exception
	 */
	public static int indexOf(byte[] str, byte[] index, int from) {
		if (str == null || index == null) {
			return -1;
		}

		int strlength = str.length;
		int indexlength = index.length;
		if (from >= strlength) {
			return -1;
		}
		if (from < 0) {
			from = 0;
		}
		for (int i = from; i < strlength; i++) {
			if (str[i] != index[0]) {
				continue;
			}
			for (int j = 0; j < indexlength; j++) {
				if (i + j >= strlength) {
					break;
				}
				if (str[i + j] != index[j]) {
					break;
				}
				if (j == indexlength - 1) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * get position of the substring
	 * 
	 * @param str
	 *            source array
	 * @param index
	 *            index string
	 * @return -1 if any exception
	 */
	public static int indexOf(byte[] str, String index) {
		if (str == null || index == null || "".equals(index)) {
			return -1;
		}

		byte[] indexbytes = null;
		indexbytes = index.getBytes();
		return StringUtil.indexOf(str, indexbytes);
	}

	/**
	 * get position of the substring
	 * 
	 * @param str
	 *            source array
	 * @param index
	 *            index string
	 * @param from
	 *            from position
	 * @return -1 if any exception
	 */
	public static int indexOf(byte[] str, String index, int from) {
		if (str == null || index == null || "".equals(index)) {
			return -1;
		}

		byte[] indexbytes = null;
		indexbytes = index.getBytes();
		return StringUtil.indexOf(str, indexbytes, from);
	}

	/**
	 * get position of the substring
	 * 
	 * @param str
	 *            source array
	 * @param index
	 *            index string
	 * @param enc
	 *            encoding of index string
	 * @param from
	 *            from position
	 * @return -1 if any exception
	 */
	public static int indexOf(byte[] str, String index, String enc, int from) {
		if (str == null || index == null || "".equals(index)) {
			return -1;
		}

		byte[] indexbytes = null;
		try {
			indexbytes = index.getBytes(enc);
		} catch (java.io.UnsupportedEncodingException e) {
			return -1;
		}
		return StringUtil.indexOf(str, indexbytes, from);
	}

	/**
	 * get last position of the substring
	 * 
	 * @param str
	 *            source array
	 * @param index
	 *            index string
	 * @return -1 if any exception
	 */
	public static int lastIndexOf(byte[] str, byte[] index) {
		if (str == null || index == null) {
			return -1;
		}

		int strlength = str.length;
		int indexlength = index.length;
		for (int i = strlength - 1; i >= 0; i--) {
			if (str[i] != index[0]) {
				continue;
			}
			for (int j = 0; j < indexlength; j++) {
				if (i + j >= strlength) {
					break;
				}
				if (str[i + j] != index[j]) {
					break;
				}
				if (j == indexlength - 1) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * get last position of the substring
	 * 
	 * @param str
	 *            source array
	 * @param index
	 *            index string
	 * @param from
	 *            from position
	 * @return -1 if any exception
	 */
	public static int lastIndexOf(byte[] str, byte[] index, int from) {
		if (str == null || index == null) {
			return -1;
		}

		int strlength = str.length;
		int indexlength = index.length;
		if (from < 0) {
			return -1;
		}
		if (from > strlength - 1) {
			from = strlength - 1;
		}
		for (int i = from; i >= 0; i--) {
			if (str[i] != index[0]) {
				continue;
			}
			for (int j = 0; j < indexlength; j++) {
				if (i + j >= strlength) {
					break;
				}
				if (str[i + j] != index[j]) {
					break;
				}
				if (j == indexlength - 1) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * get last position of the substring
	 * 
	 * @param str
	 *            str source array
	 * @param index
	 *            index string
	 * @return -1 if any exception
	 */
	public static int lastIndexOf(byte[] str, String index) {
		if (str == null || index == null || "".equals(index)) {
			return -1;
		}

		byte[] indexbytes = null;
		indexbytes = index.getBytes();
		return StringUtil.lastIndexOf(str, indexbytes);
	}

	/**
	 * get last position of the substring
	 * 
	 * @param str
	 *            str source array
	 * @param index
	 *            index string
	 * @param from
	 *            from position
	 * @return -1 if any exception
	 */
	public static int lastIndexOf(byte[] str, String index, int from) {
		if (str == null || index == null || "".equals(index)) {
			return -1;
		}

		byte[] indexbytes = null;
		indexbytes = index.getBytes();
		return StringUtil.lastIndexOf(str, indexbytes, from);
	}

	/**
	 * get last position of the substring
	 * 
	 * @param str
	 *            str source array
	 * @param index
	 *            index string
	 * @param enc
	 *            encoding of index string
	 * @param from
	 *            from position
	 * @return -1 if any exception
	 */
	public static int lastIndexOf(byte[] str, String index, String enc, int from) {
		if (str == null || index == null || "".equals(index)) {
			return -1;
		}

		byte[] indexbytes = null;
		try {
			indexbytes = index.getBytes(enc);
		} catch (java.io.UnsupportedEncodingException e) {
			log.error("StringUtil method lastIndexOf error",e);
		}
		return StringUtil.lastIndexOf(str, indexbytes, from);
	}

	/**
	 * check validility of string
	 * 
	 * @param str
	 * @return true if str is not null and str not equals ""
	 */
	public static boolean isValid(String str) {
		if (str == null || str.equals("")) {
			return false;
		}
		return true;
	}

	/**
	 * get an non exist file
	 * 
	 * @param path
	 *            path of file
	 * @param filename
	 *            default filename
	 * @return
	 */
	public static File getNonExistFile(File path, String filename) {
		if (!path.exists() || !path.isDirectory())
			return null;

		// variables
		int pos = -1;
		int posno = 0;
		String prefix = "";
		String postfix = "";
		String tempprefix = "";
		File newfile = null;

		// init
		pos = filename.lastIndexOf(".");
		if (pos == -1) {
			prefix = filename;
			postfix = "";
		} else {
			prefix = filename.substring(0, pos);
			postfix = filename.substring(pos + 1);
		}

		newfile = new File(path.getPath() + "/" + filename);
		while (newfile.exists()) {
			tempprefix = prefix + posno;
			if (postfix == "")
				filename = tempprefix;
			else
				filename = tempprefix + "." + postfix;
			newfile = new File(path.getPath() + "/" + filename);
			posno++;
		}
		return newfile;
	}

	/**
	 * get next letter with same capitalize
	 * 
	 * @param c
	 * @return a->b,A->B,z->a
	 */
	public static char getNextLetter(char c) {
		char nextLetter = (char) -1;

		if (Character.isLetter(c)) {
			nextLetter = (char) (c + 1);
			if (!Character.isLetter(nextLetter)) {
				nextLetter = (char) (nextLetter - 26);
			}
		}
		return nextLetter;
	}

	/**
	 * get a bigger String than str
	 * 
	 * @param str
	 * @return aa->ab,ZZZ->AAAA
	 */
	public static String getNextString(String str) {
		String nextString = null;

		char[] chars = new char[str.length()];
		str.getChars(0, str.length(), chars, 0);
		boolean add = false;
		char heada = 'a';
		char headA = 'A';

		for (int i = 0; i < chars.length; i++) {
			if (!Character.isLetter(chars[i]))
				return null;
		}

		for (int i = chars.length - 1; i >= 0; i--) {
			char oriChar = chars[i];
			chars[i] = getNextLetter(chars[i]);
			if (chars[i] > oriChar)
				break;
			if (i == 0)
				add = true;
		}

		nextString = new String(chars);
		if (add == true && Character.isLowerCase(chars[0]))
			nextString = heada + nextString;
		if (add == true && Character.isUpperCase(chars[0]))
			nextString = headA + nextString;

		return nextString;
	}

	/**
	 * get package name of the class
	 * 
	 * @param theClass
	 * @return eg:hyzou.tools<br>"" if the class with no package
	 */
	public static String getPackageName(Class theClass) {
		String packageName = "";

		String className = theClass.getName();
		if (className.lastIndexOf(".") != -1)
			packageName = className.substring(0, className.lastIndexOf("."));

		return packageName;
	}

	/**
	 * get length method: getLength
	 * 
	 * @param source
	 * @return return type: int
	 */
	public static int getLength(String source) {
		int len = 0;
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			int highByte = c >>> 8;
			len += highByte == 0 ? 1 : 2;
		}
		return len;
	}

	/**
	 * get class name of the class
	 * 
	 * @param theClass
	 * @return eg:StringUtil
	 */
	public static String getClassName(Class theClass) {
		String className = theClass.getName();
		if (className.lastIndexOf(".") != -1)
			className = className.substring(className.lastIndexOf(".") + 1);

		return className;
	}

	/**
	 * get the absolute file path of the class
	 * 
	 * @param theClass
	 * @return
	 */
	public static String getClassPath(Class theClass) {
		return getClassPath(theClass.getName());
	}

	/**
	 * get the absolute file path of the class
	 * 
	 * @param theClass
	 * @return absolute path of class folder end with char '/'
	 */
	public static String getClassFolderPath(Class theClass) {
		return getClassFolderPath(theClass.getName());
	}

	/**
	 * get the absolute file path of the class
	 * 
	 * @param className
	 * @return
	 */
	public static String getClassPath(String className) {
		String classPath = null;

		if (!className.startsWith("/")) {
			className = "/" + className;
		}
		className = className.replace('.', '/');
		className = className + ".class";

		java.net.URL classUrl = new StringUtil().getClass().getResource(
				className);

		if (classUrl != null)
			classPath = classUrl.getFile().toString();

		return classPath;
	}

	/**
	 * get the absolute file path of the class folder
	 * 
	 * @param className
	 * @return path of class folder end with char '/'
	 */
	public static String getClassFolderPath(String className) {
		String classFolderPath = null;

		String classPath = getClassPath(className);
		if (classPath != null) {
			classFolderPath = classPath.substring(0,
					classPath.lastIndexOf("/") + 1);
		}

		return classFolderPath;
	}

	/**
	 * get next word of value from the index
	 * 
	 * @param value
	 * @param index
	 * @return a chinese word or a complete english word or a group of signs
	 *         without chinese word and space.<br>
	 *         null if value is null or value==""<br>
	 *         null if index<0 or index>=length of value.<br>
	 */
	public static String getNextWord(String value, int index, boolean wordSplit) {
		if (value == null || value.equals("") || index < 0
				|| index >= value.length())
			return null;

		if (wordSplit == true)
			return value.substring(index, index + 1);

		char firstChar = value.charAt(index);
		if (isChinese(firstChar))
			return value.substring(index, index + 1);
		else {
			int spacePos = value.indexOf(" ", index);
			int chinesePos = indexOfChinese(value, index);

			if (spacePos == -1 && chinesePos == -1)
				return value.substring(index);
			if (spacePos == -1)
				return value.substring(index, chinesePos);
			if (chinesePos == -1)
				return value.substring(index, spacePos + 1);

			if (chinesePos < spacePos)
				return value.substring(index, chinesePos);
			return value.substring(index, spacePos + 1);
		}
	}

	/**
	 * if is a chinese word, 255-65535
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		if (c > 255)
			return true;
		return false;
	}

	/**
	 * get the index of nearest chinese word
	 * 
	 * @param value
	 * @param index
	 * @return
	 */
	public static int indexOfChinese(String value, int index) {
		if (value == null || value.equals("") || index < 0
				|| index > value.length())
			return -1;

		for (int i = index; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c > 255)
				return i;
		}

		return -1;
	}

	/**
	 * get binary string of a byte array
	 * 
	 * @param data
	 * @return "" if data is null or length of data equals zero
	 */
	public static String getBinString(byte[] data) {
		if (data == null || data.length == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		int eachData = 0;
		String eachByte;
		for (int i = 0; i < data.length; i++) {
			eachData = data[i];
			if (eachData < 0)
				eachData = eachData + 256;
			eachByte = Integer.toHexString(eachData);
			if (eachByte.length() == 1)
				eachByte = "0" + eachByte;
			sb.append(eachByte);
		}

		return sb.toString();
	}

	/**
	 * get binary data from a binary string
	 * 
	 * @param binStr
	 * @return byte[0] if data is null or length of data equals zero<br>
	 * @throws Exception
	 *             if length of binStr is odd
	 */
	public static byte[] getBinFromString(String binStr) throws Exception {
		if (binStr == null || binStr.length() == 0)
			return new byte[0];

		int length = binStr.length();
		if (length % 2 != 0)
			throw new Exception("invalid length of source string");

		byte[] data = new byte[length / 2];

		for (int i = 0; i < data.length; i++) {
			String s = binStr.substring(i * 2, i * 2 + 2);
			data[i] = (byte) Integer.parseInt(s, 16);
		}

		return data;
	}

	/**
	 * concat array to string, with string of split,null treat as ""
	 * 
	 * @param objs
	 * @param split
	 * @return null if objs is null
	 */
	public static String ArrayToStr(Object[] objs, String split) {
		if (objs == null)
			return null;

		if (split == null)
			split = "";

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < objs.length; i++) {
			if (i < objs.length - 1) {
				if (objs[i] == null)
					result.append(split);
				else
					result.append(objs[i]).append(split);
			} else if (objs[i] != null)
				result.append(objs[i]);
		}
		return result.toString();
	}

	/**
	 * print out rs
	 * 
	 * @param rs
	 * @throws Exception
	 */
	public static void printRS(ResultSet rs) throws Exception {
		ResultSetMetaData rsmd = rs.getMetaData();
		int numCols = rsmd.getColumnCount();

		while (rs.next()) {
			for (int i = 0; i < numCols; i++) {
				log.debug("" + rsmd.getColumnLabel(i + 1) + "=: "
						+ rs.getString(i + 1));
			}
		}
	}

	// public static String ClassFormat(String str)
	// {
	// str.toLowerCase().c`
	// }

	public static String capitalize(String str) {
		if (!isValid(str))
			return str;
		StringBuffer sb = new StringBuffer(str.substring(0, 1).toUpperCase())
				.append(str.substring(1));
		return sb.toString();
	}

	/**
	 * Use to convert to url
	 * 
	 * @param location
	 * @return
	 */
	public static String location2Url(String location) {
		if (location == null)
			return null;
		String url = replace(location, "UNIEAP_URL_INTERROGATION", "?");
		url = replace(url, "UNIEAP_URL_SPLIT_MASK", "&");
		url = replace(url, "UNIEAP_URL_EQUAL_MASK", "=");
		url = replace(url, "UNIEAP_URL_BOOK_MASK", "#");
		url = replace(url, " ", "");
		return url;
	}

	/**
	 * Use to conver to location
	 * 
	 * @param url
	 * @return
	 */
	public static String url2Location(String url) {
		if (url == null)
			return null;
		String location = replace(url, "?", "UNIEAP_URL_INTERROGATION");
		location = replace(location, "&", "UNIEAP_URL_SPLIT_MASK");
		location = replace(location, "=", "UNIEAP_URL_EQUAL_MASK");
		location = replace(location, "#", "UNIEAP_URL_BOOK_MASK");
		return location;
	}

	/**
	 * Use to conver to location
	 * 
	 * @param url
	 * @return
	 */
	public static String getSingleString(String str, String divideFlag) {

		if (str.length() != 0) {
			String[] temp = StringUtil.split(str, divideFlag);
			str = "";
			for (int i = 0; i < temp.length; i++) {
				if (str.length() != 0) {
					String tempStr = str.substring(0, str.length() - 1);
					String[] tempStrArray = StringUtil.split(tempStr,
							divideFlag);
					int j = 0;
					for (; j < tempStrArray.length; j++) {
						if (temp[i].equals(tempStrArray[j])) {
							break;
						}
					}
					if (j == tempStrArray.length) {
						str = str + temp[i] + divideFlag;
					}

				} else {
					str = str + temp[i] + divideFlag;
				}

			}
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	/**
	 * <p>
	 * 字符串中带有单引号的在前面再增加一个单引号，为了组装sql的时候单引号转义
	 * </p>
	 * 
	 * @param orignstr
	 * @return
	 */
	public static String getESCSingleQuotes(String orignstr) {
		String oldstr = orignstr;
		if (oldstr == null || oldstr.equals("") || oldstr.trim().length() <= 0)
			return oldstr;

		String newstr = "";
		int singleQuotesInt = oldstr.indexOf("'");
		int endsite;

		while (singleQuotesInt >= 0) {
			newstr = newstr.concat(oldstr.substring(0, singleQuotesInt + 1))
					.concat("'");
			endsite = oldstr.length();
			oldstr = oldstr.substring(singleQuotesInt + 1, endsite);
			if (oldstr == null || oldstr.equals("")
					|| oldstr.trim().length() <= 0)
				singleQuotesInt = -1;
			else
				singleQuotesInt = oldstr.indexOf("'");
		}
		if (oldstr.length() > 0)
			newstr = newstr.concat(oldstr);

		return newstr;
	}

	/**
	 * <p>
	 * 从页面content中提取图片信息（提取图片的路径和文件名，主要是*.gif文件）
	 * </p>
	 * 
	 * @param content
	 * @return
	 */
	public static String[] getImageUrlInfo(String content) {

		String extractString = "";

		int startPosition = 0;
		int endPosition = 0;
		String remainString = content;

		for (int i = 0; i < remainString.length() && startPosition != -1; i++) {
			startPosition = remainString.indexOf("images");
			if (startPosition != -1) {
				remainString = remainString.substring(startPosition);
				endPosition = remainString.indexOf("\"");
				extractString = extractString
						+ remainString.substring(0, endPosition) + ",";
				remainString = remainString.substring(endPosition);
			}
		}
		if (extractString.length() != 0)
			extractString = extractString.substring(0,
					extractString.length() - 1);
		// System.out.println("extractString===" + extractString);
		return str2Array(extractString, ",");
	}

	/**
	 * <p>
	 * html encode
	 * </p>
	 * 
	 * @param strSource
	 * @return
	 */
	public static String doHTML(String strSource) {
		String strDest = doReplace(strSource, "&", "&amp;");
		strDest = doReplace(strDest, "<", "&lt;");
		strDest = doReplace(strDest, ">", "&gt;");
		strDest = doReplace(strDest, "'", "&apos;");
		strDest = doReplace(strDest, "\"", "&quot;");

		return strDest;
	}

	/**
	 * <p>
	 * replace string
	 * </p>
	 * 
	 * @param strSource
	 * @param strFrom
	 * @param strTo
	 * @return
	 */
	public static String doReplace(String strSource, String strFrom,
			String strTo) {
		if (strSource == null) {
			return null;
		}

		String strDest = "";
		int intFromLen = strFrom.length();
		int intPos;

		while ((intPos = strSource.indexOf(strFrom)) != -1) {
			strDest = strDest + strSource.substring(0, intPos);
			strDest = strDest + strTo;
			strSource = strSource.substring(intPos + intFromLen);
		}
		strDest = strDest + strSource;

		return strDest;
	}

	/**
	 * <p>
	 * 将"0000000xx" ----> "xx"
	 * </p>
	 * 
	 * @param origStr
	 * @return
	 */
	public static String trimHeadZero(String origStr) {
		return replace(origStr, "0", "");
	}

	/**
	 * <p>
	 * 文本转为十六进制值
	 * </p>
	 * 
	 * @param str
	 * @return
	 */
	public static String text2Hex(String str) {
		if (str == null || str.trim().length() == 0)
			return "";
		str = str.trim();
		int strlen = str.length();
		StringBuffer hexString = new StringBuffer((strlen * 3));// 假设输入是UTF-8编码

		for (int i = 0; i < strlen; ++i) {
			int decimal = str.charAt(i); // 取得ASCII值
			String hex = Integer.toHexString(decimal);
			if (decimal <= 255) {
				/**
				 * 标准ASCII、扩展ASCII以及UTF-8的整数都需要补零
				 */
				int trailingZeros = 2 - hex.length();
				for (int j = 0; j < trailingZeros; ++j)
					hexString.append("0");
			} else {
				int trailingZeros = 4 - hex.length();
				for (int k = 0; k < trailingZeros; ++k)
					hexString.append("0");
			}
			hexString.append(hex);
			hexString.append(" ");
		}
		return hexString.toString();
	}

	/**
	 * 
	 * <p>
	 * 当javascript中var变量赋值是html代码中对textarea值里是javabean，而javabean中的字符串带有换行符<br>
	 * 例如 var inhtml="<textarea name='txt1'></textarea>";<br>
	 * txt1.innerText='你好 好好';<br>
	 * 这时候需要把换行符号等转换\r。
	 * </p>
	 * 
	 * @param str
	 * @return
	 */
	public static String textareaHtml(String str) {
		if (str == null) {
			return null;
		}

		str = replace(str, "\"", "&quot;");
		str = replace(str, " ", "&nbsp;");
		str = replace(str, "<", "&lt;");
		str = replace(str, ">", "&gt;");

		// ////System.out.println("yyyyyyyy==="+str.indexOf("\r"));
		str = replace(str, "\r", "");
		str = replace(str, "\n", "\r");
		str = replace(str, "'", "''");
		return str;
	}

	/**
	 * <p>
	 * 当javascript中var变量赋值是html代码中对textarea值里是javabean，而javabean中的字符串带有换行符<br>
	 * 例如 var inhtml="<textarea name='txt1'></textarea>";<br>
	 * txt1.innerText='你好 好好';<br>
	 * 这时候需要把换行符号等转换\r。
	 * </p>
	 * 
	 * @param str
	 * @return
	 */
	public static String textareaVar(String str) {
		if (str == null) {
			return null;
		}

		str = replace(str, "\"", "&quot;");
		str = replace(str, " ", "&nbsp;");
		str = replace(str, "<", "&lt;");
		str = replace(str, ">", "&gt;");
		// ////System.out.println("yyyyyyyy==="+str.indexOf("\r"));
		str = replace(str, "\r", "");
		str = replace(str, "\n", "\\r");
		str = replace(str, "'", "''");
		return str;
	}

	/**
	 * <p>
	 * 得到title<>中内容
	 * </p>
	 * 
	 * @param title
	 * @return
	 */
	public static String getTitleInfo(String title) {

		// String extractString = "";

		int startPosition = 0;
		int endPosition = 0;
		String remainString = title;

		for (int i = 0; i < remainString.length() && startPosition != -1
				&& endPosition != -1; i++) {
			startPosition = remainString.indexOf("<");
			endPosition = remainString.indexOf(">");
			if (startPosition != -1 && endPosition != -1) {
				remainString = remainString.substring(0, startPosition)
						+ remainString.substring(endPosition + 1);
			}
		}
		return remainString;
	}

	/**
	 * <p>
	 * 合并字符串
	 * </p>
	 * 
	 * @param src
	 *            原字符串
	 * @param length
	 *            要求的长度
	 * @param mode
	 *            模式 01=右边补充， 02=左边补充
	 * @param fill
	 *            补充的字符串
	 * @return
	 */
	public static String fill(String src, int length, String mode, String fill) {

		if (src == null || src.length() < 1) {
			return src;
		}

		if (mode == null || (!mode.equals("01") && !mode.equals("02"))) {
			return src;
		}

		if (length < 1) {
			return src;
		}

		if (fill == null) {
			return src;
		}

		StringBuffer buffer = null;

		if (mode.equals("01")) {

			buffer = new StringBuffer(src);

			for (int i = src.length(); i < length; i++) {
				buffer.append(fill);
			}
		} else {

			buffer = new StringBuffer("");

			for (int i = src.length(); i < length; i++) {
				buffer.append(fill);
			}

			buffer.append(src);
		}

		return buffer.toString();
	}
	
	/**
	 * sql 防注入过滤
	 * @param str
	 * @return
	 */
	public static String sqlStrFilter(String str){
		if(str==null || "".equals(str))
			return str;
		
		String re = "";
		re = str.replaceAll("'", "''").trim();
		
		return re;
	}
	
	public static String tempEncoding(String src,String oldName,String newName) throws UnsupportedEncodingException
	{
		return new String(src.getBytes(oldName),newName);
	}
	
	public static String subString(String string,int begin,int end)
	{
		if(begin < 0)
				begin = 0;
		if(end > string.length())
			end = string.length();
		if(begin <= end)
			return string.substring(begin, end);
		else
			return "";
	}
	
	public String subString(String string,int begin)
	{
		if(begin < 0)
			begin = 0;
		if(begin >= string.length())
			return "";
		return string.substring(begin);
	}
	
	public static String parseToString(Object obj){
		if(obj==null){
			return null;
		}else{
			return String.valueOf(obj);
		}
	}
	
	public static long parseToLong(Object obj){
		if(obj==null){
			return 0;
		}else{
			return Long.parseLong(String.valueOf(obj));
		}
	}
	
	public static int parseToInt(Object obj){
		if(obj==null){
			return 0;
		}else{
			return Integer.parseInt(String.valueOf(obj));
		}		
	}
	
	public static Timestamp parseToTimestamp(Object obj){
		if(obj==null){
			return null;
		}else{
			return Timestamp.valueOf(String.valueOf(obj));
		}
	}
	
	public static String cutStrBufferSuffix(StringBuffer sb){
		if(sb == null || sb.length()<=0)
			return "";
		int pos = sb.length();
		if(pos>0 && sb.charAt(pos-1)== ','){
			sb.deleteCharAt(pos-1);
		}
		return sb.toString();
	}
	
		/**
		 * 替换模版文件内容
		 * 替换标志为：${}
		 * @param template 模版原文
		 * @param data<String,String> 定义一组 
		 * @return
		 * @throws Exception
		 */
	    public static String composeTemplate(String template, Map data)
	            throws Exception {
	        String regex = "\\$\\{(.+?)\\}";
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(template);
	        /*
	         * sb用来存储替换过的内容，它会把多次处理过的字符串按源字符串序
	         * 存储起来。
	         */
	        StringBuffer sb = new StringBuffer();
	        while (matcher.find()) {
	            String name = matcher.group(1);//键名
	            String value = (String) data.get(name);//键值
	            if (value == null) {
	                value = "";
	            } else {
	                /*
	                 * 转义内容中的$字符
	                 * 要把 $ 替换成 \$ ，则要使用 \\\\\\& 来替换，因为一个 \ 要使用 \\\ 来进
	                 * 行替换，而一个 $ 要使用 \\$ 来进行替换，因 \ 与  $ 在作为替换内容时都属于
	                 * 特殊字符：$ 字符表示反向引用组，而 \ 字符又是用来转义 $ 字符的。
	                 */
	                value = value.replaceAll("\\$", "\\\\\\$");	                
	            }
	            /*
	             * 另外，我们在这里使用Matcher对象的appendReplacement()方法来进行替换操作，而
	             * 不是使用String对象的replaceAll()或replaceFirst()方法来进行替换操作，因为
	             * 它们都能只能进行一次性简单的替换操作，而且只能替换成一样的内容，而这里则是要求每
	             * 一个匹配式的替换值都不同，所以就只能在循环里使用appendReplacement方式来进行逐
	             * 个替换了。
	             */
	            matcher.appendReplacement(sb, value);
	        }
	        //最后还得要把尾串接到已替换的内容后面去，这里尾串为“，欢迎下次光临！”
	        matcher.appendTail(sb);
	        return sb.toString();
	    }
}
