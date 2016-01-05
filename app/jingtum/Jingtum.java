package jingtum;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.params.HttpParams;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import business.Withdraw;

import com.jingtum.currency.Currency;

import constants.Constants;
import p2x.jingtum.https.APICallClient;
import play.db.jpa.JPA;
import play.mvc.Scope.Session;
import ytx.org.apache.http.HttpEntity;
import ytx.org.apache.http.HttpResponse;
import ytx.org.apache.http.NameValuePair;
import ytx.org.apache.http.client.HttpClient;
import ytx.org.apache.http.client.entity.UrlEncodedFormEntity;
import ytx.org.apache.http.client.methods.HttpGet;
import ytx.org.apache.http.client.methods.HttpPost;
import ytx.org.apache.http.client.utils.URLEncodedUtils;
import ytx.org.apache.http.entity.StringEntity;
import ytx.org.apache.http.impl.client.DefaultHttpClient;
import ytx.org.apache.http.message.BasicNameValuePair;
import ytx.org.apache.http.util.EntityUtils;
import models.t_jingtum_transfer_details;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 井通接口调用类
 * 
 * @author liushuang
 */
public class Jingtum {
	private static final Logger L = Logger.getLogger(Jingtum.class);
	
	/** 井通API调用客户端 */
	private APICallClient apiClient = null;
	
	public Jingtum() {
		// 初始化资源
		apiClient = new APICallClient();
	}
	
	/**
	 * 注册钱包
	 * 
	 * @return 钱包地址及钱包密钥
	 * 
	 * @throws JingtumCallException 调用井通接口失败时抛出该异常
	 */
	public String[] register() throws JingtumCallException {
		L.debug("注册钱包");
		JSONObject json = apiClient.apiCall("new_wallet");
		
		if (!json.getBoolean("success")) {
			throw new JingtumCallException("接口调用失败\n"
					.concat(json.getString("content")));
		}
		
		json = json.getJSONObject("wallet");
		String[] result = {
			json.getString("address"),
			json.getString("secret")
		};
		L.debug(result[0]);
		L.debug(result[1]);
		
		return result;
	}
	

	//借款-申请
	
	
	//转帐到钱包
	/**
	 * 通过井通转账
	 * @param from_address 转账人账号
	 * @param secret 转账人私钥
	 * @param trsfer_amount 转账金额
	 * @param to_address 接收人账号
	 * @param fromCurrency   原始币种
	 * @param toCurrency  目标币种
	 * @return from_address to_address trsfer_amount success client_resource_id hash state result date fee
	 * @throws JingtumCallException 
	 */
	public String[] transferToBorrowers(String from_address,String secret,String trsfer_amount,String to_address,String fromCurrency,String toCurrency) {
		//为了减少操作，再每次转账前 都进行一次本币种的信任
		String counterparty="";
		if("SWT".equals(fromCurrency)){
			counterparty="";
		}else if(counterparty == "" || counterparty==null){
		//	counterparty=Constants.CNY_COUNTERPARTY;
			counterparty="jBciDE8Q3uJjf111VeiUNM775AMKHEbBLS";
		}
		setTrust(from_address,secret,Constants.DEFAULT_TRUST_LIMIT, fromCurrency,counterparty);
		
		JSONObject json = apiClient.apiCall("payment",
				from_address,//转账账号
				secret, //转账账号的私钥
				Double.parseDouble(trsfer_amount)+1+"",//默认加1
				fromCurrency,//原币种
				toCurrency,//转换币种
				to_address,//转账地址
				trsfer_amount,//转账金额
				counterparty,
				counterparty);
		String client_resource_id="";
		String hash="";
		String state="";
		String results="";
		String date="";
		String fee="";
		
		if(json.containsKey("client_resource_id")){
			client_resource_id=json.containsKey("msg") ? json.getString("msg"):"";
		}
		if(json.containsKey("hash")){
			hash=json.getString("hash");
		}
		if(json.containsKey("state")){
			state=json.getString("state");
		}
		if(json.containsKey("result")){
			results=json.getString("result");
		}
		if(json.containsKey("date")){
			date=json.getString("date");
		}
		if(json.containsKey("fee")){
			fee=json.getString("fee");
		}
		String[] result = {
				from_address,
				to_address,
				trsfer_amount,
				json.getString("success"),//一定存在
				client_resource_id,
				hash,
				state,
				results,
				date,
				fee
		};
		t_jingtum_transfer_details retrans=new t_jingtum_transfer_details();
		try {
			retrans.from_address=result[0];
			retrans.to_address=result[1];
			retrans.trsfer_amount=result[2];
			retrans.from_currency=fromCurrency;
			retrans.success=result[3];
			retrans.client_resource_id=result[4];
			retrans.hash=result[5];
			retrans.state=result[6];
			retrans.result=result[7];
			retrans.date=result[8];
			retrans.fee=result[9];
			retrans.create_time=new Date();
			retrans.save();//保存转账日志
		} catch (Exception e) {
			L.error("保存转账记录错误:"+JSONObject.fromObject(retrans).toString());
			L.error("保存转账记录错误:"+e.getMessage());
		}
		return result;
	}
	
	/**
	 * 新注册用户激活钱包
	 * @param address 钱包地址
	 * @return from_address to_address trsfer_amount success client_resource_id hash state result date fee
	 * @throws JingtumCallException 
	 */
	public String[] activity(String address){
		return transferToBorrowers(
				Constants.P2X_ADDRESS,//p2x银关
				Constants.P2X_SECRET,//p2x私钥
				Constants.ACTIVITY_AOMOUNT,
				address,//需要激活地址
				Constants.ACTIVITY_CURRENCY,
				Constants.ACTIVITY_CURRENCY
			);
	}
	
	/**
	 * 余额查询接口，井通审核接口
	 * 井通的标审核，主要是看是否发行融资通进行状态判定
	 * @param address
	 * @return
	 * @throws JingtumCallException
	 */
	public boolean getJingTumStatus(String address) throws JingtumCallException{
		JSONObject json = apiClient.apiCall("balance", "jp64GVM12GPjh9S1Df3yE3114i4FHDjso2",address);
		if(!json.getBoolean("success")){
			if(json.containsKey("message")){
				throw new JingtumCallException("井通审核接口调用失败\n"
						.concat(json.getString("message")));
			}
			else{
				throw new JingtumCallException("井通审核接口调用失败\n"
						.concat("发生服务器端错误"));
			}
		}
		JSONArray  tmp=json.getJSONArray("balances");
		return tmp.toString().indexOf("currency") >= 0 ;//存在此属性，则说明有值，不存在说此次返回为【[]】;
	}
	/**
	 * 
	* @Title: getJingTumList 
	* @Description: 获取标的持有人列表
	* 参数类型说明
	*	balances Array 余额数组
	*		value String 余额
	*		currency String 货币名称，三个字母或是40 个字符的字符串【标地址 bid_address】
	*		counterparty String 货币发行方【标的持有人地址】
	* @param @param address
	* @param @return
	* @param @throws JingtumCallException    
	* @return List<Map<String, String>> key[value,currency,counterparty]
	* @throws
	 */
	public List<Map<String, String>> getJingTumList(String bid_address) throws JingtumCallException{
		JSONObject json = apiClient.apiCall("balance", "jp64GVM12GPjh9S1Df3yE3114i4FHDjso2",bid_address);
		if(!json.getBoolean("success")){
			if(json.containsKey("message")){
				throw new JingtumCallException("井通审核接口调用失败\n"
						.concat(json.getString("message")));
			}
			else{
				throw new JingtumCallException("井通审核接口调用失败\n"
						.concat("发生服务器端错误"));
			}
		}
		JSONArray list=json.getJSONArray("balances");
		List<Map<String, String>> listmap=new ArrayList<Map<String,String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> map=new HashMap<String, String>();
			map.put("value",JSONObject.fromObject(list.getString(i)).getString("value"));
			map.put("currency",JSONObject.fromObject(list.getString(i)).getString("currency"));
			map.put("counterparty",JSONObject.fromObject(list.getString(i)).getString("counterparty"));
			listmap.add(map);
		}
		return listmap;
	}
	//申请融资通
	/**
	 * 向井通平台申请融资通
	 * @param bid_id
	 * @return
	 * @throws JingtumCallException
	 */
	public String[]  forJingTumBid(String bid_id,String amount) throws JingtumCallException{
		Date startDate=new Date();
		String p2xtum_id=Currency.GenerateP2XTum(startDate, null);
		//JSONObject json = apiClient.apiCall("balance", p2xtum_id);
		//发送p2x通申请
		JSONObject json=forP2xtum(bid_id,p2xtum_id,amount);
		if(!json.getBoolean("status")){
			throw new JingtumCallException("融资通申请接口调用失败\n"
						.concat(json.getString("message")));
			
		}else{
			String[] result= {
					bid_id,
					p2xtum_id,
					Long.toString(startDate.getTime())
			};
			return result;
		}
	}
	
	/**
	 * 设置信任
	 * @param address   需要设置信任的钱包地址
	 * @param secret	钱包密钥
	 * @param limit     信任额度  输入为null  或者“”  均使用默认Constants.DEFAULT_TRUST_LIMIT
	 * @param currency  信任币种  输入为null  或者“”  均使用默认Constants.CURRENCY_P2X
	 * @param counterparty  货币发行方
	 */
	public  boolean setTrust(String address,String secret,String limit,String currency,String counterparty) {
		JSONObject json = apiClient.apiCall("set_trust",address,secret,
				limit == null||limit == ""?Constants.DEFAULT_TRUST_LIMIT:limit,
				currency == null || currency == "" ? Constants.CURRENCY_P2X : currency,
			    counterparty);
		if(json != null){
			if(json.containsKey("success")){
				if(json.getBoolean("success")){
					return true;
				}else{
					try {
						throw new JingtumCallException("设置信任 接口调用失败\n"+json.toString());
					} catch (JingtumCallException e) {
					}
				}
			}else{//返回格式不对，应为 接口调用出错
				try {
					throw new JingtumCallException("设置信任 接口调用失败\n 返回值中不存在[success]字段");
				} catch (JingtumCallException e) {
				}
			}
		}else{//返回对象为null  应为接口调用出错
			try {
				throw new JingtumCallException("设置信任 接口调用失败\n 返回值为null");
			} catch (JingtumCallException e) {
			}
		}
		return  false;
	}
	
	/**
	 * 投资人 投标   成功标【使用 CNY ——> 购买 指定 融资通】
	 * @param address  购买方
	 * @param secret   购买方密钥
	 * @param trsfer_amount  购买金额
	 * @param to_address  出售方 【默认：p2x】
	 * @param currency   币种
	 * @param counterparty  货币发行方
	 * @return
	 */
	public boolean buyRZTong(String address,String secret,String trsfer_amount,String to_address,String currency,String counterparty){
		
		//1.将用户的CNY转入p2x银关
		//无需信任
		String[] json=this.transferToBorrowers(address, secret, trsfer_amount, Constants.P2X_ADDRESS, Constants.CURRENCY_CNY, Constants.CURRENCY_CNY);
		//2.井通将融资通 转入 用户账号【】
		String[] tmp=this.transferToBorrowers(Constants.P2X_ADDRESS,Constants.P2X_SECRET, trsfer_amount, address, currency,currency);
		if(tmp[3].contains("true")){
			return true;
		}else{
			//String[] result=this.transferToBorrowers(Constants.P2X_ADDRESS, Constants.P2X_SECRET, trsfer_amount, address, Constants.CURRENCY_CNY,Constants.CURRENCY_CNY);
			//多次返还用户CNY 失败后 如何操作
			return false;
		}
	}
	
	/**
	 * 投标失败  返还 钱款
	 * @param address
	 * @param secret
	 * @param trsfer_amount
	 * @param to_address 
	 * @param currency   标地址
	 * @return
	 */
	public boolean restoreRZTong(String address,String secret,String trsfer_amount,String currency){
		//1.将用户持有的标的额度 收归到 p2x 银关
		//无需信任
		String[] json=this.transferToBorrowers(address, secret, trsfer_amount, Constants.P2X_ADDRESS, currency, currency);
		//2.p2x 银关将对应的CNY 返还给用户
		//无需信任
		String[] tmp=this.transferToBorrowers(Constants.P2X_ADDRESS,Constants.P2X_SECRET, trsfer_amount, address,Constants.CURRENCY_CNY,Constants.CURRENCY_CNY);
		if(tmp[3].contains("true")){
			return true;
		}else{
			//String[] result=this.transferToBorrowers(Constants.P2X_ADDRESS, Constants.P2X_SECRET, trsfer_amount, address, Constants.CURRENCY_CNY,Constants.CURRENCY_CNY);
			//多次返还用户CNY 失败后 如何操作
			return false;
		}
	}
	
	/**
	 * 精通充值结果的 消息通知【向井通】
	 * @return
	 */
	public static boolean sendCallback(){
		try {
			DefaultHttpClient httpclient=new DefaultHttpClient();

			HttpGet get=new HttpGet(Constants.TONG_CALLBACK_URL);
			get.getParams().setParameter("success", true);
			HttpResponse response = httpclient.execute(get);
			
			if(200 == response.getStatusLine().getStatusCode()){
				return true;
			}else{
				HttpPost post=new HttpPost(Constants.TONG_CALLBACK_URL);//post
				StringEntity pstr=new StringEntity("{'success':true}","utf-8");
				post.setEntity(pstr);
				
				httpclient=new DefaultHttpClient();
				HttpResponse presponse = httpclient.execute(post);
				
				if(200 == presponse.getStatusLine().getStatusCode()){
					return true;
				}else{
					return false;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 向井通提交 提现申请
	 * @author houyaoshan
	 * @param withdraw
	 * @return
	 */
	public JSONObject  getWithdraw(Withdraw withdraw){
		 // 创建默认的httpClient实例.
		 
		  HttpClient httpclient = new DefaultHttpClient();
		  HttpPost httppost = new HttpPost(Constants.JING_D_URL);
		  List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		  
		  formparams.add(new BasicNameValuePair("p0_cmd",withdraw.p0_cmd));
		  formparams.add(new BasicNameValuePair("p1_custom", withdraw.p1_custom));
		  formparams.add(new BasicNameValuePair("p2_batch", withdraw.p2_batch));
		  formparams.add(new BasicNameValuePair("p3_order", withdraw.p3_order));
		  formparams.add(new BasicNameValuePair("p4_bank_code",withdraw.p4_bank_code));
		  formparams.add(new BasicNameValuePair("p5_bank_name",  withdraw.p5_bank_name));
		  formparams.add(new BasicNameValuePair("p6_branch_name",withdraw.p6_branch_name));
		  formparams.add(new BasicNameValuePair("p7_amount",withdraw.p7_amount));
		  formparams.add(new BasicNameValuePair("p8_account_name", withdraw.p8_account_name));
		  formparams.add(new BasicNameValuePair("p9_account_number",withdraw.p9_account_number));
		  formparams.add(new BasicNameValuePair("pa_account_type", withdraw.pa_account_type));
		  formparams.add(new BasicNameValuePair("pb_province", withdraw.pb_province));
		  formparams.add(new BasicNameValuePair("pc_city", withdraw.pc_city));
		  formparams.add(new BasicNameValuePair("pd_fee_type", withdraw.pd_fee_type));
		  formparams.add(new BasicNameValuePair("pe_email", withdraw.pe_email));
		  formparams.add(new BasicNameValuePair("pf_mobile", withdraw.pf_mobile));
		  formparams.add(new BasicNameValuePair("pg_remark", withdraw.pg_remark));
		  formparams.add(new BasicNameValuePair("ph_source", withdraw.ph_source));
		  formparams.add(new BasicNameValuePair("pi_target", withdraw.pi_target));
		  formparams.add(new BasicNameValuePair("pj_tx", withdraw.pj_tx));
		  formparams.add(new BasicNameValuePair("hmac", withdraw.getHmac(withdraw)));
		  System.out.println("hmac:"+withdraw.getHmac(withdraw));
		  UrlEncodedFormEntity uefEntity;
		  JSONObject json=new JSONObject();
		  try {
			  uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			  httppost.setEntity(uefEntity);
			  HttpEntity es=httppost.getEntity();
			  System.out.println("params:"+EntityUtils.toString(es,"UTF-8"));
			  HttpResponse response = httpclient.execute(httppost);
			  HttpEntity entity = response.getEntity();
			  String obj=EntityUtils.toString(entity, "UTF-8");
			  json=JSONObject.fromObject(obj);
			  return json;
		} catch (Exception e) {
			json.put("r1_code", "-1");
			json.put("r6_error_msg", "本地服务调用接口时，发生错误");
			return json;
		}
	}
	
	
	/**
	 * 模拟接口
	 * @param bidid
	 * @param p2xtum_address
	 * @return
	 */
	private static  JSONObject forP2xtum(String bidid,String p2xtum_address,String amount){
		JSONObject json=new JSONObject();
		json.put("status", true);
		json.put("message", "对不起申请融资通错误");
		return json;
	}
	
	public static void main(String[] args) {
		Jingtum jing=new  Jingtum();
//		boolean bool=jing.setTrust("jfBQin2GtTjRoYKbuCqugrMDgDxR4Jbzh","sn4eZSAcDRVuTzc3pz41nvAVtmr98","", "CNY", Constants.CNY_COUNTERPARTY);
//		boolean bool=sendCallback();
		System.out.println(URLEncoder.encode("招商银行广渠路支行"));
		System.out.println();
	}
	
}
