package controllers.front.account;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;



import chinapnr.SecureLink;

import com.shove.Convert;

import business.User;
import play.Logger;
import play.mvc.With;
import utils.ErrorInfo;
import utils.FormUtil;
import utils.NumberUtil;
import constants.Constants;
import constants.Templets;
import controllers.BaseController;
import controllers.interceptor.FInterceptor;

/**
 * 登陆汇付
 */
@With(FInterceptor.class)
public class ChinaPnrPayment extends BaseController {
	
//	public static final String pnrURL = Constants.CHINAPNR_GATEWAY;
//	
//	
//	/**
//	 * 汇付登录
//	 * @param cmdId  消息类型 
//	 * @param usrCustId  用户客户号
//	 */
//	public static void loginHF(String cmdId, String usrCustId) {
//		ErrorInfo error = new ErrorInfo();
//		User user = User.currUser();
//		// 判段用户是否是汇付用户
//		boolean Success = User.isIPSacctNoExist(user.id, error);
//		if(Success){
//			Map<String, String> map = new HashMap<String, String>();
//			map.put("Version", Constants.CHINAPNR_VERSION);
//			map.put("CmdId", "UserLogin");
//			map.put("MerCustId", Constants.CHINAPNR_MERCUSTID);
//			map.put("UsrCustId", user.ipsAcctNo);
//			
//			// 拼接为字符串，用于签名验证
//			StringBuffer sb = new StringBuffer();
//			sb.append(map.get("Version")).append(map.get("CmdId")).append(map.get("MerCustId")).append(usrCustId);
//			SecureLink sl = new SecureLink();
//			try {
//				int ret = sl.SignMsg(Constants.CHINAPNR_MERID, FormUtil.geterKeyFile(), sb.toString().getBytes("utf-8"));
//				if (ret != 0) {
//					error.code = -1;
//					error.msg = "发送请求签名错误！";
//				}
//			} catch (UnsupportedEncodingException e) {
//				error.code = -1;
//				error.msg = e.getMessage();
//			}
//			
//			map.put("ChkValue", sl.getChkValue());
//			map.put("url", Constants.CHINAPNR_GATEWAY);
//			try {
//				String subform = FormUtil.buildLoginForm(map, pnrURL+" ", "post", "");
//				renderHtml(subform);
//			} catch (Exception e) {
//				error.code = -1;
//				error.msg = e.getMessage();
//			}
//			
//		} else {
//			renderHtml(FormUtil.goUrl("您还未注册成为汇付会员，请前往注册!", Constants.BASE_URL+"front/account/payAccount"));
//		}
//		
//	}
	
	/**
	 * 企业用户开户
	 */
	/*public static void CorpRegister () {
		ErrorInfo error = new ErrorInfo();
		Logger.info("进入没有？？？？？？？？？？？");
		String version = Constants.CHINAPNR_VERSION;
		String cmdId = "CorpRegister";
		String merCustId = Constants.CHINAPNR_MERCUSTID;
		String busiCode = "1103021615400"; 	// 营业执照编号
		String usrId = "sp2p622888";
		String bgRetUrl = Constants.BASE_URL+"front/account/doCorpRegister";
		Map<String, String> map = new HashMap<String, String>();
		map.put("Version", version);
		map.put("CmdId", cmdId);
		map.put("MerCustId", merCustId);
		map.put("UsrId", usrId);
		map.put("BusiCode", busiCode);
		map.put("BgRetUrl", bgRetUrl);
		
		// 拼接为字符串，用于签名验证
		StringBuffer sb = new StringBuffer();
		sb.append(map.get("Version"))
		.append(map.get("CmdId"))
		.append(map.get("MerCustId"))
		.append(map.get("UsrId"))
		.append(map.get("BusiCode"))
		.append(map.get("BgRetUrl"));
		SecureLink sl = new SecureLink();
		try {
			int ret = sl.SignMsg(Constants.CHINAPNR_MERID, FormUtil.geterKeyFile(), sb.toString().getBytes("utf-8"));
			if (ret != 0) {
				error.code = -1;
				error.msg = "发送请求签名错误！";
			}
		} catch (UnsupportedEncodingException e) {
			error.code = -1;
			error.msg = e.getMessage();
		}
		
		map.put("ChkValue", sl.getChkValue());
		map.put("url", Constants.CHINAPNR_GATEWAY);
		String subform;
		try {
			subform = FormUtil.buildForm(map, pnrURL+" ", "post", "");
			renderHtml(Templets.replaceAllHTML(subform));
		} catch (Exception e) {
			error.code = -1;
			error.msg = "拼接表单失败，"+e.getMessage();
		}
	}*/
	
	/**
	 * 企业开户回调
	 * @return
	 */
	/*public static void doCorpRegister() {
		Map<String, String> map = request.params.allSimple();
		Set<String> keySet = map.keySet();
		Logger.info("开始打印企业开户应答信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		for(String key : keySet){
			Logger.info(key + ":" + map.get(key));
		}
		Logger.info("结束打印企业开户应答信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		
	}*/
	
	/**
	 * 用户账户支付
	 * * ChkValue:{  Version  +CmdId  +  OrdId  +  UsrCustId  +  MerCustId  +  TransAmt+  InAcctId  +InAcctType + RetUrl + BgRetUrl+ MerPriv }
	 * 用户账户支付，只能从用户的专属账户支付给其对应商户的指定账户（由 InAcctId，InAcctType 指定） 。
	 * 商户实现后台方式时候，需要在页面上打印 RECV_ORD_ID_和 OrdId
	 * @param transAmt  交易金额
	 * @param UsrCustId  用户客户号
	 */
	/*public static String usrAcctPay(String transAmt, String UsrCustId) {
		ErrorInfo error = new ErrorInfo();
		String Version = Constants.CHINAPNR_VERSION;
		String CmdId = "UsrAcctPay";  // 消息类型,每一种消息类型代表一种交易
		String MerCustId = Constants.CHINAPNR_MERCUSTID;  // 商户号
		String OrdId = NumberUtil.getBillNo("");  // 订单号
		String MerPriv = "UsrAcctPay";  //私有域
		if (transAmt.indexOf('.') == -1) {
			transAmt += ".00";
		} else {
			transAmt += "000000";
			if(transAmt.substring(transAmt.indexOf(".") + 3,transAmt.indexOf(".") + 6).equals("999")){
				int temp = Integer.parseInt(transAmt.substring(transAmt.indexOf(".") + 2,transAmt.indexOf(".") + 3))+1;
				transAmt = transAmt.substring(0, transAmt.indexOf(".") + 2)+temp;
			}else{
				
				transAmt = transAmt.substring(0, transAmt.indexOf(".") + 3);
			}
		}
		//入账子账户就是类似MDT000001   入账账户类型是MERDT
		String InAcctId = "MDT000001";  //入账子账户
		// BASEDT基本借记户; DEP保证金账户; MERDT专属借记帐户;等
		String InAcctType = "MERDT";   //入账账户类型
		String RetUrl = Constants.BASE_URL+"chinapnr/doUsrAcctPay";
		String BgRetUrl = Constants.BASE_URL+"chinapnr/doUsrAcctPay";
		
		StringBuffer sb = new StringBuffer();
		sb.append(Version)
		.append(CmdId)
		.append(OrdId)
		.append(UsrCustId)
		.append(MerCustId)
		.append(transAmt)
		.append(InAcctId)
		.append(InAcctType)
		.append(RetUrl)
		.append(BgRetUrl)
		.append(MerPriv);
		SecureLink sl = new SecureLink();
		try {
			int ret = sl.SignMsg(Constants.CHINAPNR_MERID, FormUtil.geterKeyFile(), sb.toString().getBytes("utf-8"));
			if (ret != 0) {
				error.code = -1;
				error.msg = "发送请求签名错误！";
			}
		} catch (UnsupportedEncodingException e) {
			error.code = -1;
			error.msg = e.getMessage();
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("Version", Version);
		map.put("CmdId", CmdId);
		map.put("MerCustId", MerCustId);
		map.put("OrdId", OrdId);
		map.put("TransAmt", transAmt);
		map.put("RetUrl", RetUrl);
		map.put("BgRetUrl", BgRetUrl);
		map.put("MerPriv", MerPriv);
		map.put("UsrCustId", UsrCustId);
		map.put("InAcctId", InAcctId);
		map.put("InAcctType", InAcctType);
		map.put("ChkValue", sl.getChkValue());

		return FormUtil.buildHtmlForm(map, pnrURL, "post");	
	}*/
	
	
}
