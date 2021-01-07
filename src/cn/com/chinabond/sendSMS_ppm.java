package cn.com.chinabond;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

//import java.util.Date;
import java.util.LinkedList;
import java.util.List;
//import java.text.SimpleDateFormat;

@SuppressWarnings("deprecation")
public class sendSMS_ppm {
	/*
	 * userid ,ts=System.curren tTimeMillis() ,sign, mobile ,msgcontent
	 * userid=�û����&ts=1476235100217&sign=md5(userid + ts +
	 * apikey)&mobile=���͵��ֻ���&msgcontent=��������&time=����ʱ��&extnum=�·���չ��
	 * http://ip:port/api/sms/send?userid=100001&ts=1476235100217&sign=54449b
	 * b492c2ea2592ea3eceef3d0b47&mobile=13800000000,135000000&msgcontent=hello&time
	 * =20110115105822&extnum=1001
	 */

	/**
	 * @author yangyang
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

		/*
		 * @parm args:args[1] firstgrp, args[2]:sms_msg
		 */
		try {
			//read sms platform environment from init.properties
			ResourceBundle conf = ResourceBundle.getBundle("cn/com/chinabond/init.properties");
			String host = conf.getString("host");
			String port = conf.getString("port");
			String userid = conf.getString("userid");
			String apikey = conf.getString("apikey");
			String extnum = conf.getString("extnum");
			
			//read firestgrp from event and extract phone number from phone.properties under the current folder
			String firstgrp = args[0];
			//String firstgrp = "AIX";
			
			Properties prop = new Properties();
			//FileInputStream fis = new FileInputStream("D:\\phone.properties");
			FileInputStream fis = new FileInputStream("sendSMS_ppm_34757220.txt");
			prop.load(fis);
			
			//String phonestr = "13263171110";
			
			String phonestr = prop.getProperty(firstgrp);
			fis.close();
			try {
				//String file="C:\\Users\\CCDC\\eclipse-workspace\\NewSMSPlatform\\bin\\cn\\com\\chinabond\\sendSMS_ppm_34757220.txt";
				String file=args[1];
				FileInputStream fd = new FileInputStream(file);
	            InputStreamReader reader = new InputStreamReader(fd,"UTF-8"); //����"GBK"�����ļ����Զ�����������У��ĳ�"UTF-8"����
	            BufferedReader br = new BufferedReader(reader);
	            String msgcontent="";
	            if((msgcontent = br.readLine()) == null){
	            	msgcontent ="null";
	            }
	            
	            br.close();
	            reader.close();
				File preDeleteFile = new File(file);
				preDeleteFile.deleteOnExit();
				
			
				//System.out.println("now"+msgcontent.replaceAll("��", "#").replaceAll("��", "#"));
				//call sms http api
				String result = callSMS(host, port, userid, apikey, phonestr, extnum, msgcontent.trim());
				System.out.print("event proceced with "+ result+"\n");
	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
			
			//read args[1] for sms text file
			
			/*//FileInputStream sms_fis = new FileInputStream(args[1]);
			FileInputStream sms_fis = new FileInputStream(file);
			byte buff[] = new byte[1024];
			sms_fis.read(buff);
			String msgcontent = new String(buff);
			sms_fis.close();
			*/
			
			
		
	}

	public static String callSMS(String host, String port, String userid, String apikey, String mobile, String extnum,
			String msgcontent) throws ClientProtocolException, IOException {
		String result = "";

		// ��ʼ����API�ӿ�ʱ��
		long ts = System.currentTimeMillis();
		// 32bit md5 value for userid+ts+apikey
		String sign = DigestUtils.md5Hex(userid + ts + apikey);
		
		
		//SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		//String currentTs = df.format(new Date());
		// System.out.println(df.format(new Date())); //new Date()Ϊ��ȡ��ǰϵͳʱ��

		// construct parameter
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("userid", String.valueOf(userid)));
		params.add(new BasicNameValuePair("sign", String.valueOf(sign)));
		params.add(new BasicNameValuePair("ts", String.valueOf(ts)));
		params.add(new BasicNameValuePair("apikey", String.valueOf(apikey)));
		params.add(new BasicNameValuePair("mobile", String.valueOf(mobile)));
		params.add(new BasicNameValuePair("msgcontent", String.valueOf("�����ƽ̨��"+msgcontent.replaceAll("��", "#").replaceAll("��", "#"))));
		//params.add(new BasicNameValuePair("time", String.valueOf(currentTs)));
		//params.add(new BasicNameValuePair("extnum", String.valueOf(extnum)));

		// encode uri with utf-8
		String paramString = URLEncodedUtils.format(params, "utf-8");

		String url = "http://" + host + ":" + port + "/api/sms/send?" + paramString;
		System.out.println("url:" + url);

		@SuppressWarnings("resource")
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {

			HttpResponse response = client.execute(httpGet);
			int httpStatusCode = response.getStatusLine().getStatusCode();
			if (httpStatusCode != 200) {
				throw new RuntimeException("Failed:HTTP error code:" + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String output = br.readLine();
			System.out.println(output);
			result = output;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
			httpGet.reset();
		}
		return result;

	}

}
