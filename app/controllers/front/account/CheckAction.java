package controllers.front.account;






import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.sun.xml.internal.messaging.saaj.soap.StringDataContentHandler;

import play.libs.Codec;
import play.mvc.Before;
import business.BackstageSet;
import business.TemplateEmail;
import business.User;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.SMSUtil;
import constants.Constants;
import constants.IPSConstants.IpsCheckStatus;
import controllers.interceptor.FInterceptor;

public class CheckAction extends FInterceptor {
	@Before(only = {"front.account.FundsManage.recharge",
			"front.account.InvestAccount.auditmaticInvest",
			"front.account.FundsManage.withdrawal"
			})
	public static void checkIpsAcct(){
		User user = User.currUser();
		
		if(null == user) {
			LoginAndRegisterAction.login();
		}
		
		if(Constants.IPS_ENABLE && (user.getIpsStatus() != IpsCheckStatus.IPS)){
			CheckAction.approve();
		}
	}
	
	/**
	 * ips认证
	 */
	public static void approve() {
		render();
	}
	
	/**
	 * ips认证(弹框)
	 */
	public static void check() {
		int status = User.currUser().getIpsStatus();
		
		switch (status) {
		case IpsCheckStatus.NONE:
			checkEmail();
			break;
		case IpsCheckStatus.EMAIL:
			checkEmailSuccess();
			break;
		case IpsCheckStatus.REAL_NAME:
			checkMobile();
			break;
		case IpsCheckStatus.MOBILE:
			createIpsAcct();
			break;
		case IpsCheckStatus.IPS:
			checkSuccess();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 邮箱认证
	 */
	public static void checkEmail() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.NONE) {
			check();
		}
		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		TemplateEmail.activeEmail(user, error);
		String email = user.email;
		String emailUrl = EmailUtil.emailUrl(email);
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		render(email, emailUrl, phone, qq1, qq2);
	}
	
	/**
	 * 发送激活邮件
	 */
	public static void sendActiveEmail() {
		ErrorInfo error = new ErrorInfo();
		
		if (User.currUser().getIpsStatus() != IpsCheckStatus.NONE) {
			error.code = -1;
			error.msg = "非法请求";
			
			renderJSON(error);
		}
		
		User user = User.currUser();
		TemplateEmail.activeEmail(user, error);
		
		if (error.code >= 0) {
			error.msg = "激活邮件发送成功！";
		}
		
		renderJSON(error);
	}
	
	/**
	 * 邮箱认证成功
	 */
	public static void checkEmailSuccess() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAIL) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		render(phone, qq1, qq2);
	}
	
	/**
	 * 实名认证页面
	 */
	public static void checkRealName() {
		User user=User.currUser();
		
		render(user);
	}
	

	/**
	 * 实名认证
	 * @throws  
	 */
	public static void doCheckRealName() {
		
		String realName=params.get("realName");
		String idNumber=params.get("idNumber");
	    String years1=idNumber.substring(6,10);
		String months2=idNumber.substring(10,12);
		String days3=idNumber.substring(12,14);
		String brithday=years1+"-"+months2+"-"+days3;
	    //根据身份证判断性别
	    String sex= idNumber.substring(16, 17);
	    
	     if(Integer.parseInt(sex)%2==0){
	        sex = "2";
		}else{
			sex ="1";
      }
	
	 	User user = User.currUser();
	 	
		JSONObject json=new JSONObject();
		ErrorInfo error = new ErrorInfo();
//		if (user.getIpsStatus() != IpsCheckStatus.EMAIL) {
//			check();
//		}
		
		flash.put("realName", realName);
		flash.put("idNumber", idNumber);
	
		if (StringUtils.isBlank(realName)) {
			error.code=-1;
			error.msg="真实姓名不能为空";
			json.put("error",error );
			renderJSON(json);
	}
		
		if (StringUtils.isBlank(idNumber)) {
			error.code=-1;
			error.msg="身份证不能为空";
			json.put("error",error );
			renderJSON(json);
	} 
	 try {
			 user.saveBirthday(sex,brithday);
			 user.checkRealName(realName, idNumber, error);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	      json.put("error",error );
		  renderJSON(json);
	}
	
	/**
	 * 手机认证页面
	 */
	public static void checkMobile() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.REAL_NAME) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String companyName = set.companyName; // 公司名称
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		String randomId = Codec.UUID(); 
		boolean checkMsgCode = Constants.CHECK_MSG_CODE;
		
		render(companyName, phone, qq1, qq2,randomId,checkMsgCode);
	}
	
	/**
	 * 发送短信验证码
	 * @param mobile
	 */
	public static void sendCode(String mobile) {
		ErrorInfo error = new ErrorInfo();
		flash.put("mobile", mobile);
		
		if(StringUtils.isBlank(mobile) ) {
			flash.error("手机号码不能为空");
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			flash.error("请输入正确的手机号码");
		}
		
		SMSUtil.sendCode(mobile, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
		}
		
		flash.put("isSending", true);
		
		checkMobile();
	}
	
	/**
	 * 手机认证
	 * @param mobile
	 * @param code
	 */
	public static void doCheckMobile(String mobile, String code) {
		User user = User.currUser();
		if (user.getIpsStatus() != IpsCheckStatus.REAL_NAME) {
			check();
		}
		
		flash.put("mobile", mobile);
		flash.put("code", code);
		
		if (StringUtils.isBlank(mobile)) {
			flash.error("手机号不能为空");
			
			checkMobile();
		}
		
		if (StringUtils.isBlank(code)) {
			flash.error("验证码不能为空");
			
			checkMobile();
		}
		
		ErrorInfo error = new ErrorInfo();
		user.checkMoible(mobile, code, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			checkMobile();
		}
		
		createIpsAcct();
	}
	
	/**
	 * 资金托管开户页面
	 */
	public static void createIpsAcct() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.MOBILE) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		render(phone, qq1, qq2);
	}
	
	/**
	 * 认证成功
	 */
	public static void checkSuccess() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.IPS) {
			check();
		}
		
		render();
	}
}
