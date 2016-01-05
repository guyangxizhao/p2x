package controllers.front.account;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import models.t_dict_ad_citys;
import models.t_users;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.shove.security.Encrypt;

import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.With;
import utils.CaptchaUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.Security;
import business.BackstageSet;
import business.SecretQuestion;
import business.TemplateEmail;
import business.User;
import constants.Constants;
import constants.Templets;
import controllers.AddCheck;
import controllers.BaseController;
import controllers.Check;
import controllers.DSecurity;
import controllers.interceptor.FInterceptor;

@With({FInterceptor.class,DSecurity.class})
public class BasicInformation extends BaseController {

	//-------------------------------基本资料-------------------------
	
	/**
	 * 基本信息
	 */
	public static void basicInformation(){
		User user = User.currUser();
//		user.id = User.currUser().id;
//		String randomId = Codec.UUID(); 
		
//		ErrorInfo error = new ErrorInfo();
//		
//		if(error.code < 0) {
//			render(user, Constants.ERROR_PAGE_PATH_FRONT);
//		} 
//		
//		List<t_dict_ad_citys> cityList = null;
//		if(flash.get("province") != null) {
//			cityList = User.queryCity(Integer.parseInt(flash.get("province")));
//		}else {
//			cityList = User.queryCity(user.provinceId);
//		}
//		
//		List<t_user_vip_records> vipRecords = Vip.queryVipRecord(user.id, error);
//		
//		if(error.code < 0) {
//			render(user, Constants.ERROR_PAGE_PATH_FRONT);
//		}
//		
//		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
//		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
//		boolean ipsEnable = constants.Constants.IPS_ENABLE;
//		boolean checkMsgCode = Constants.CHECK_MSG_CODE;
//		
//		Object cars = Cache.get("cars");
//		Object provinces = Cache.get("provinces");
		Object educations = Cache.get("educations");
		Object houses = Cache.get("houses");
		Object maritals = Cache.get("maritals");
		Object industries = Cache.get("industries");
		render(user, educations, houses, maritals, industries);
	}
	
	/**
	 * 根据省获得市联动
	 */
	public static void getCity(long provinceId){
		List<t_dict_ad_citys> cityList = User.queryCity(provinceId);
		JSONArray json = JSONArray.fromObject(cityList);
//		json.put("cityList", cityList);
		renderJSON(json);
	}
	

	
	/**
	 * 保存基本信息
	 */
	public static void saveInformation(String nickname, int education,
			String graduate, int marital, String familyAddress, int house,
			int industry, double income) {
		flash.put("nickname", nickname);
		flash.put("education", education);
		flash.put("graduate", graduate);
		flash.put("marital", marital);
		flash.put("familyAddress", familyAddress);
		flash.put("house", house);
		flash.put("industry", industry);
		flash.put("income", income);

		User user = User.currUser();
		User newUser = new User();
		newUser.setId(user.id);
		newUser.name = nickname;
		newUser.educationId = education;
		newUser.graduate = graduate;
		newUser.maritalId = marital;
		newUser.houseId = house;
		newUser.familyAddress = familyAddress;
		newUser.industry = industry;
		newUser.income = income;
		
		ErrorInfo error = new ErrorInfo();
		user.edit(newUser, error);

		if (error.code < 0) {
			flash.error(error.msg);
			basicInformation();
		}
		
		long userId = user.id;
		User.removeCurrUser();
		newUser = new User();
		newUser.setInformation(newUser.refreshUserInfo(userId));
		User.setCurrUser(newUser);
		
		flash.success(error.msg);
		basicInformation();
	}
	
	/**
	 * 弹出重置安保问题的页面
	 */
	public static void setSafeQuestionModify(){
		
		render();
	}
	
	/**
	 * vip详情
	 */
	public static void vipDetail(){
		BackstageSet options = BackstageSet.getCurrentBackstageSet();
		
		JSONObject json = new JSONObject();
		json.put("test", options);

		renderJSON(options);
	}
	
	/**
	 * 设置安全问题
	 */
	public static void setSafeQuestion(){
		User user = User.currUser();
		Logger.info("设置安全问题："+user.isSecretSet);
		List<SecretQuestion> questions = SecretQuestion.queryUserQuestion();
		render(user,questions);
	}
	
	/**
	 * 校验安全问题
	 */
	@AddCheck(Constants.IS_AJAX)
	public static void verifySafeQuestion(String questionName1, String questionName2, 
			String questionName3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		user.verifySafeQuestion(questionName1, questionName2, questionName3, error);
		
		JSONObject json = new JSONObject();
		 
		json.put("encryString", flash.get("encryString"));
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 保存安全问题
	 */
	@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void saveSafeQuestion(String encryString, long secretQuestion1, 
			long secretQuestion2, long secretQuestion3, String answer1, 
			String answer2, String answer3){
		if( secretQuestion1 == 0 || 
			secretQuestion2 == 0 || 
			secretQuestion3 == 0 ||
			StringUtils.isBlank(answer1) ||
			StringUtils.isBlank(answer2) ||
			StringUtils.isBlank(answer3) ||
			secretQuestion1 == secretQuestion2 ||
			secretQuestion1 == secretQuestion3 ||
			secretQuestion2 == secretQuestion3 ||
			answer1.length() > 50 ||
			answer2.length() > 50 ||
			answer3.length() > 50 
		
		){
			flash.error("答案不能为空，且长度需在1~50之间!");
			
			setSafeQuestion();
		}
		
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		
		user.secretQuestionId1 = secretQuestion1;
		user.secretQuestionId2 = secretQuestion2;
		user.secretQuestionId3 = secretQuestion3;
		user.answer1 = answer1;
		user.answer2 = answer2;
		user.answer3 = answer3;
		
		user.updateSecretQuestion(true, error);
		
		flash.error(error.msg);
		
		String fromPage = params.get("fromPage");
		
		if (StringUtils.isBlank(fromPage)) {
			setSafeQuestion();
		}
		
		if (fromPage.equals("modifyEmail")) {
			modifyEmail();
		}
		
		if (fromPage.equals("modifyPassword")) {
			modifyPassword();
		}
		
		if (fromPage.equals("modifyMobile")) {
			modifyMobile();
		}
		
		setSafeQuestion();
	}
	
	//得到安全问题
	public static void getSafeQuestion(){
		render();
	}
	
	/**
	 * 通过邮箱重置安全问题
	 */
	public static void resetSafeQuestion(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 4;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.SECRET_QUESTION);
		String url = Constants.RESET_QUESTION_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		
		
		TemplateEmail.sendEmail(0, user.email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			resetSafeQuestion();
		}
		
//		EmailUtil.emailResetSecretQuestion(user.name, user.email, error);
//		String emailUrl = EmailUtil.emailUrl(user.email);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		json.put("emailUrl", EmailUtil.emailUrl(user.email));
		
		renderJSON(json);
	}
	
	/**
	 * 重置安全问题页面
	 */
	public static void resetQuestion(String sign){
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.SECRET_QUESTION, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		String name = User.queryUserNameById(id, error);
		
		List<SecretQuestion> questions = SecretQuestion.queryUserQuestion();
		
		render(loginOrRegister, name, sign, questions);
	}
		
	/**
	 * 通过邮件重置后，保存安全问题答案
	 */
	public static void saveSafeQuestionByEmail(String sign, long secretQuestion1, 
			long secretQuestion2, long secretQuestion3, String answer1, 
			String answer2, String answer3){
		ErrorInfo error = new ErrorInfo();
		
		if( secretQuestion1 == 0 || 
			secretQuestion2 == 0 || 
			secretQuestion3 == 0 ||
			StringUtils.isBlank(answer1) ||
			StringUtils.isBlank(answer2) ||
			StringUtils.isBlank(answer3) ||
			secretQuestion1 == secretQuestion2 ||
			secretQuestion1 == secretQuestion3 ||
			secretQuestion2 == secretQuestion3 ||
			answer1.length() > 50 ||
			answer2.length() > 50 ||
			answer3.length() > 50  
			){
			error.code = -1;
			error.msg = "请填写正确的问题和答案!";
			
			flash.error(error.msg);
			
			AccountHome.home();
		}
		
		long id = Security.checkSign(sign, Constants.SECRET_QUESTION, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		User user = new User();
		user.id = id;
		
		user.secretQuestionId1 = secretQuestion1;
		user.secretQuestionId2 = secretQuestion2;
		user.secretQuestionId3 = secretQuestion3;
		user.answer1 = answer1;
		user.answer2 = answer2;
		user.answer3 = answer3;
		
		user.updateSecretQuestion(false, error);
		
		flash.error(error.msg);
		
		AccountHome.home();
	}
	/**
	 * 修改绑定邮箱第一部
	 * 验证旧邮箱
	 */
	public static void verificationEmail(){
	
	    ErrorInfo error=new ErrorInfo();
		User user=User.currUser();
		TemplateEmail.verificationEmail(user, error);
		JSONObject json=new JSONObject();
		if(error.code==1){
		error.code=1;
		error.msg="验证邮件已发送成功，请去邮箱完成下一步注册";
		}
		json.put("error", error);
		renderJSON(json);
}
	
	/**
	 * 保存邮箱
	 */
	public static void saveEmail(String email){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		User user = new User();
		user.id = User.currUser().id;
		if(!email.equals(User.currUser().email)||User.currUser()==null){
			User.isEmailExist(email, error);
			if(error.code<0){
				json.put("error", error);
				renderJSON(json);
			}
			
		}
		user.email = email;
	    user.isEmailVerified=User.currUser().isEmailVerified;
	    if(!user.email.equals(email)||user.email==null){
	    	user.isEmailExist(email, error);
	    	if(error.code<0){
	    	json.put("error", error);
			renderJSON(json);
	    	}
	    }
		User.currUser().email=email;
	    user.savenewemail();
	   if(user.isEmailVerified) {
			error.code = -1;
			error.msg = "你的邮箱已激活，无需再次激活";
			json.put("error", error);
			renderJSON(json);
		}
	 
		TemplateEmail.activeEmail(user, error);
	
		String emailUrl = EmailUtil.emailUrl(email);
		error.code=1;
		error.msg="激活邮件已成功发送";
		json.put("error", error);
		json.put("emailUrl", emailUrl);
		
		renderJSON(json);
	}
	

	/**
	 * 修改邮箱
	 */
	public static void modifyEmail() {
		User user = User.currUser();
		String type=params.get("type");
	    flash.put("type", type);
		render(user);
	}
	
	/**
	 * 绑定邮箱（回答安全问题）
	 */
	public static void bindEmail(String answer1, String answer2, String answer3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		user.verifySafeQuestion(answer1, answer2, answer3, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyEmail();
		}
 		
		render(user);
	}
	
	/**
	 * 修改密码
	 */
	public static void modifyPassword(){
		
		User user = User.currUser();
		String type=params.get("type");
		if(type==null||type.equals("")||type==""){
			type="0";
		}
		flash.put("type", type);
//		if(!user.isSecretSet) {
//			flash.error("您还没有设置安全问题，为了保障您的安全，请先设置安全问题");
//			flash.put("fromPage", "modifyPassword");
//			setSafeQuestion();
//		}
	
		String randomId = Codec.UUID();
		boolean checkMsgCode = Constants.CHECK_MSG_CODE;
		
		render(user,randomId,checkMsgCode);
	}
	
	/**
	 * 保存密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	//@Check({Constants.VERIFY_SAFE_QUESTION,Constants.IS_AJAX})
	public static void savePassword(){
		String oldPassword=params.get("oldPassword"); 
		String newPassword1=params.get("newPassword1");
		String newPassword2=params.get("newPassword2");
		String encryString=params.get("encryString");
        User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if(oldPassword.equalsIgnoreCase(newPassword1)){
			JSONObject json = new JSONObject();
			json.put("error", "新密码与原密码一样，请重新输入");
			renderJSON(json);
		}
		
		user.editPassword(oldPassword, newPassword1, newPassword2, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 保存支付密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	//@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void editPayPassword(){
		String oldPayPassword=params.get("oldPayPassword"); 
		String newPayPassword1=params.get("newPayPassword1");
		String newPayPassword2=params.get("newPayPassword2");
		String encryString=params.get("encryString");
		
		if(oldPayPassword.equalsIgnoreCase(newPayPassword1)){
			JSONObject json = new JSONObject();
			json.put("error", "新密码与原密码一样，请重新输入");
			renderJSON(json);
		}
		
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		user.editPayPassword(oldPayPassword, newPayPassword1, newPayPassword2, error);
		
		json.put("error", error);
		renderJSON(json);
		
	}
	
	/**
	 * 添加支付密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	//@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void savePayPassword(){
	String newPayPassword1=params.get("newPayPassword1");
		String newPayPassword2=params.get("newPayPassword2");
		String encryString=params.get("encryString");
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		user.addPayPassword(true, newPayPassword1, newPayPassword2, error);
		json.put("error", error);

		renderJSON(json);
	}
	/**
	 * 忘记支付密码
	 */
	public static void resetpayPassword(){
		User user=User.currUser();
		render(user);
	}
	/**
	 * 通过手机修改支付密码
	 */
   public static void resetpayPasswordbyMobile(){
	   User user = User.currUser();
	   if(!user.isMobileVerified){
		  flash.put("moberror", "请您先去绑定手机");
		  resetpayPassword();
	   }
	   render(); 
   }
	/**
	 * 重置支付密码
	 * @param code
	 * @param newPayPassword1
	 * @param newPayPassword2
	 */
	@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void resetPayPassword(String code, String newPayPassword1, 
			String newPayPassword2, String encryString) {
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(Constants.CHECK_CODE) {
			String cCode = String.valueOf(Cache.get(user.mobile));
			
			if(cCode == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";
				
				return;
			}
			
			if(!code.equals(cCode)) {
				error.code = -1;
				error.msg = "手机验证错误";
				
				return;
			}
		}
		
		user.addPayPassword(false, newPayPassword1, newPayPassword2, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
	

	/**
	 * 修改手机
	 */
	public static void modifyMobile(){
        User user = User.currUser();
		String type=params.get("type");
		if(type==null||type.equals("")||type==""){
			type="0";
		}
		flash.put("type", type);
	    render(user);
	}
	
	/**
	 * 保存手机号码
	 * @param code
	 * @param mobile
	 */
	public static void saveMobile(String code, String mobile,String type, String randomID) {
		
	    ErrorInfo error = new ErrorInfo();
	    JSONObject json = new JSONObject();
	    if(type!=null&&type.equals("Mobile2")){
	    	if(!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
	    		
			    json.put("success", -1);
			    renderJSON(json);
		    }
	    	   flash.put("mobile",mobile);
	    	  json.put("success", 1);
			   renderJSON(json);
		};
		String serveCode = session.get("codeSMS");
		if(serveCode==null||serveCode.equals("")){
			error.code = -1;
			error.msg = "请点击发送验证码";
			json.put("error", error);
		    renderJSON(json);
		}
		long limit = BackstageSet.getCurrentBackstageSet().smsLimit;
		long sTime = Long.valueOf(session.get("stime"));
		if (System.currentTimeMillis() - sTime > limit) {
			error.code = -1;
			error.msg = "验证码已失效，请重新点击发送验证码";
			json.put("error", error);
		    renderJSON(json);
		 }
		if (!serveCode.equals(code)) {
			error.code = -1;
			error.msg = "手机验证错误";
			json.put("error", error);
		    renderJSON(json);
		 }
		User user = new User();
		user.id = User.currUser().id;
		String mobile2=User.currUser().mobile;
		if(!mobile.equals(mobile2)||mobile2==null){
		 User.isMobileExist(mobile, error);
		 if(error.code==-2){
		    json.put("error", error);
			 renderJSON(json);
		 }
		}
	     user.mobile = mobile;
		user.editMobile(code, error);
		
		
		json.put("error", error);
		
		renderJSON(json);
	} 
   /**
   * 修改绑定手机验证旧的绑定手机
   */
  public static void oldMobile(String code){
	    JSONObject json = new JSONObject();
		ErrorInfo error = new ErrorInfo();
		String serveCode = session.get("codeSMS");
		if(serveCode==null||serveCode.equals("")){
			error.code = -1;
			error.msg = "请点击发送验证码";
			json.put("error", error);
		    renderJSON(json);
		}
		long limit = BackstageSet.getCurrentBackstageSet().smsLimit;
		long sTime = Long.valueOf(session.get("stime"));
	
	 //验证超时
		if (System.currentTimeMillis() - sTime > limit) {
			error.code = -1;
			error.msg = "验证码已失效，请重新点击发送验证码";
			json.put("error", error);
			renderJSON(json);
		 }
		//验证码输入有误
		if (!serveCode.equals(code)) {
			error.code = -1;
			error.msg = "手机验证错误";
			json.put("error", error);
			renderJSON(json);
		 }  
		error.code = 0;
		json.put("error", error);
		renderJSON(json);
  }
	/**
	 * 绑定手机
	 */
	public static void bindMobile(String answer1, String answer2, String answer3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		String randomId = Codec.UUID();
		boolean checkMsgCode = Constants.CHECK_MSG_CODE;
		
		user.verifySafeQuestion(answer1, answer2, answer3, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyMobile();
		}
 		
		render(user, randomId, checkMsgCode);
	}
	
    public static void payPasswordbyPhone(){
      JSONObject json=new JSONObject();
      ErrorInfo error=new ErrorInfo();
	  String password=params.get("password");
	  String code=params.get("code");
	  String serveCode = session.get("codeSMS");
	  long limit = BackstageSet.getCurrentBackstageSet().smsLimit;
	  long sTime = Long.valueOf(session.get("stime"));
	
	 //验证超时
		if (System.currentTimeMillis() - sTime > limit) {
			error.code = -1;
			error.msg = "验证码已失效，请重新点击发送验证码";
			json.put("error", error);
			renderJSON(json);
		 }
		if (!serveCode.equals(code)) {
			error.code = -1;
			error.msg = "手机验证错误";
			json.put("error", error);
			renderJSON(json);
		 } 
		User user=User.currUser();
		if(!user.password.equals(Encrypt.MD5(password+Constants.ENCRYPTION_KEY,Charset.forName("utf-8")))){
			error.code = -1;
			error.msg = "登录密码验证有误";
			json.put("error", error);
			renderJSON(json);
		}
		error.code = 1;
		error.msg = "正在跳到设置交易密码界面";
		json.put("error", error);
		renderJSON(json);
}
	
	/**
	 * 保存重置密码
	 */
	public static void savePayPasswordByEmail(String sign, String password, String confirmPassword) {
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		String loginpassword =params.get("loginpassword");
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		User user = new User();
		user.id = id;
		if(!user.password.equals(Encrypt.MD5(loginpassword+Constants.ENCRYPTION_KEY,Charset.forName("utf-8")))){
			flash.put("error", "登录密码不正确,请输入正确的登录密码");
			resetDelPassword(sign);
		}
		user.updatePayPasswordByEmail(password, confirmPassword, error);
		user.queryUserById(id, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			resetDelPassword(sign);
		}
		
		flash.error(error.msg);
		
		modifyPassword();
	}
	
	public static void resetDelPassword(String sign){
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyPassword();
		}
		
		String name = User.queryUserNameById(id, error);
		
		render(loginOrRegister, name,sign);
	}

	/**
	 * 发送重置交易密码邮件
	 */
	public static void resetPayPasswordByEmail(String email) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
        User user = User.currUser();
        if(email==null){
            if(!user.isEmailVerified){
            	error.code=-3;
        	    error.msg="您没有绑定邮箱，请您先去绑定邮箱";
        	    json.put("error", error);
        	    renderJSON(json);
             }
        	 email=user.email;
         }
       
		flash.put("email", email);


		if (StringUtils.isBlank(email)) {
			 json.put("error", error);
     	    renderJSON(json);
		}

		if (!RegexUtils.isEmail(email)) {
			 json.put("error", error);
     	    renderJSON(json);
		}

	    if (error.code < 0) {
			
			json.put("error", error);
     	    renderJSON(json);
		}
	

		User.isEmailExist(email, error);

		if (error.code != -2) {
			 json.put("error", error);
     	    renderJSON(json);
		}
		
	//	t_users user = User.queryUserByEmail(email, error);

	
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = Templets.E_FIND_DELPWD_EMAL;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PAY_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, email, tEmail.title, content, error);
       
		if (error.code < 0) {
			
			json.put("error", error);
     	    renderJSON(json);
		}
	
		
        error.code=1;
        error.msg="邮件已经发送成功";
		json.put("error", error);
 	    renderJSON(json);
		modifyPassword();
	}
	/**
	 * 跳转到安全信息界面
	 */
	public static void safetyInformation(){
		User user = User.currUser();
	
	    render(user);
	}
}
