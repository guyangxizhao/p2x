package utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;
import play.Logger;
import play.Play;
public class FormUtil {
	
	/**
	 * 读取指定的key文件
	 * @return
	 */
	public static String geterKeyFile(){     
		
		String path = Play.getFile("/conf/").getAbsolutePath()+Play.configuration.getProperty("chinapnr_merKeyFile", "");
		Logger.info("geterKeyFile--------------------------%s", path);
		
		return path;
	}
	
	/**
	 * 构造提交表单HTML数据
	 * 
	 * @param sParaTemp
	 *            请求参数数组
	 * @param gateway
	 *            网关地址
	 * @param strMethod
	 *            提交方式。两个值可选：post、get
	 * @param strButtonName
	 *            确认按钮显示文字
	 * @return 提交表单HTML文本
	 * @throws Exception 
	 */
	public static String buildLoginForm(Map<String, String> sParaTemp,
			String gateway, String strMethod, String strButtonName) throws Exception {
		// 待请求参数数组
		Set<String> keys = sParaTemp.keySet();
		StringBuffer sbHtml = new StringBuffer();
		sbHtml.append("<form id=\"editForm\" name=\"editForm\" action=\"");
		sbHtml.append(gateway);
		sbHtml.append("\" method=\"");
		sbHtml.append(strMethod);
		sbHtml.append("\">");
		for (String name : keys) {
			String value = sParaTemp.get(name);
			sbHtml.append("<input type=\"hidden\" name=\"" + name
					+ "\" value=\"" + value + "\"/>");
			
			//判断必填的请求参数不能为空
			if(!("Version".equals(name)||"CmdId".equals(name)||"MerCustId".equals(name))){
				if(value==null||value==""){
					throw new Exception(value+"不能为空");
				}
			}
		}
		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName
				+ "\" style=\"display:none;\"></form>");
		sbHtml.append("<script>document.forms['editForm'].submit();</script>");

		return sbHtml.toString();
	}
	
	/**
	 * 构造提交表单HTML数据
	 * 
	 * @param sParaTemp
	 *            请求参数数组
	 * @param gateway
	 *            网关地址
	 * @param strMethod
	 *            提交方式。两个值可选：post、get
	 * @param strButtonName
	 *            确认按钮显示文字
	 * @return 提交表单HTML文本
	 * @throws Exception 
	 */
	public static String buildForm(Map<String, String> sParaTemp,
			String gateway, String strMethod, String strButtonName) throws Exception {
		// 待请求参数数组
		Set<String> keys = sParaTemp.keySet();

		StringBuffer sbHtml = new StringBuffer();
		sbHtml.append("<form id=\"editForm\" name=\"editForm\" action=\"");
		sbHtml.append(gateway);
		sbHtml.append("\" method=\"");
		sbHtml.append(strMethod);
		sbHtml.append("\">");
		for (String name : keys) {
			String value = sParaTemp.get(name);
			//Logger.info("汇付：" + name +"^^^^^^^^" +value);
			sbHtml.append("<input type=\"hidden\" name=\"" + name
					+ "\" value=\"" + value + "\"/>");
			
			//判断必填的请求参数不能为空
			if(!("Version".equals(name)||"CmdId".equals(name)||"MerCustId".equals(name))){
				if(value==null||value==""){
					throw new Exception(value+"不能为空");
				}
			}
		}
		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName
				+ "\" style=\"display:none;\"></form>");
		sbHtml.append("<script>document.forms['editForm'].submit();</script>");

		return sbHtml.toString();
	}
	
	
	/*public static String buildHtmlForm(Map<String, String> sParaTemp, String gateway, String method) {
    	StringBuffer htmlBuf = new StringBuffer();
    	htmlBuf.append("<html>");
		htmlBuf.append(" <head><title>sender</title></head>");
		htmlBuf.append(" <body>");
		try {
			htmlBuf.append(buildForm(sParaTemp,gateway,method,"提交"));
		} catch (Exception e) {
			Logger.error("FormUtil.buildHtmlForm："+e.getMessage());
		}
		htmlBuf.append(" </body>");
		htmlBuf.append("</html>");
		sParaTemp=null;
		gateway=null;
		method=null;
    	return htmlBuf.toString();
    }
	*/
	   public static String goUrl(String msg,String url){
			return "<script language=javascript>alert('"+msg+"');window.location.href='"+url+"';</script>";
		}
	   
}
