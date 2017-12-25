package cn.com.higinet.tms.engine.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


// TODO: Auto-generated Javadoc
/**
 * 过滤统计线程日志
 */
public class cmd_filter implements cmd {

	private static final String OPTION_ADD = "add";

	private static final String OPTION_DELETE = "delete";

	private static final String OPTION_CLEAR = "clear";

	private static final String TYPE_KEY = "key";

	private static final String TYPE_THREAD = "thread";

	/**
	 * @see cn.com.higinet.tms.engine.stat.net.cmd#on_cmd(java.lang.String[], java.io.BufferedReader, java.io.BufferedWriter)
	 */
	public int on_cmd(String[] param, BufferedReader in, BufferedWriter out) throws IOException {
		return 0;
	}

	/**
	 * ./filter.sh ip port option[add,delete,clear] type[key,thread] value 
	 * 参数顺序严格要求，目前并没有做严格判断。
	 *
	 * @param args the arguments
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			throw new Exception("Wrong parameters.\"./filter.sh ip port option[add,delete,clear] type[key,thread] value \"");
		}
		Socket socket = new Socket();
		String ip = args[0];
		int port = Integer.valueOf(args[1]);
		String option = args[2];
		String type = args[3];
		String value = args[4];
		BufferedWriter out = null;
		BufferedReader in = null;
		try {
			socket.connect(new InetSocketAddress(ip, port), 1000);
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			out.write(new StringBuilder("FILTER ").append(option).append(" ").append(type).append(" ").append(value).append("\n").toString());
			out.flush();
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			System.out.println(in.readLine());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
