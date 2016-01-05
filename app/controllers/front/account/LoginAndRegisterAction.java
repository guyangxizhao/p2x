package controllers.front.account;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_users;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.Transactional;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Router;
import play.mvc.With;
import sms.SendTemplateSMS;
import utils.CaptchaUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.MobileUtil;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import business.BackstageSet;
import business.BottomLinks;
import business.TemplateEmail;
import business.TemplateSms;
import business.User;
import constants.Constants;
import constants.IPSConstants.IpsCheckStatus;
import constants.Templets;
import controllers.BaseController;
import controllers.DSecurity;

/**
 * 
 * @author liuwenhui
 * 
 */
@With(DSecurity.class)
public class LoginAndRegisterAction extends BaseController {

	/**
	 * 跳转到登录页面
	 */
	public static void login() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		
 		render(loginOrRegister);
	}
	/**
	 * 激活邮箱
	 */
	public static void activeEmail(String sign) {
	
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		User user = User.currUser();
	    if(user==null){
	    user=new User();
	    long id = Security.checkSign(sign, Constants.ACTIVE, Constants.VALID_TIME, error);
	    user.id=id;
	    }
		t_users t_user = User.queryUserByUserId2(user.id, error);
		user.name=t_user.name;
	    if(t_user.var_email==null&&t_user.email.equals(t_user.name)){
	       t_user.var_email=t_user.email;
	     }
		user.email=t_user.var_email;
		
	  if(user.email==null||user.email.equals("")){
		  error.code=-1;
		  error.msg="获取邮箱时出错,此处激活邮箱失败";
		  flash.put("error", error);
		  renderJSON(json);
		
	  }
	   if(user.editEmail(error) < 0) {
			json.put("error", error);
			flash.put("error", error);
			renderJSON(json);
		}
		user.isEmailVerified=true;
	
		User.setCurrUser(user);
		json.put("error",error);
		//json.add(EmailUtil.emailUrl(user.email));
	     renderJSON(json);
	}
	
	/**
	 * 更新新邮箱
	 */
	public static void modifyEmailNext(String sign){
		ErrorInfo error = new ErrorInfo();
        long id = Security.checkSign(sign, Constants.ACTIVE, Constants.VALID_TIME, error);
        t_users t_user = new t_users();
		t_user=User.queryUserByUserId(id, error);
		User user=new User();
		user.name=t_user.name;
		user.email=t_user.var_email;
		user.password=t_user.password;
		User.setCurrUser(user);
        render(user);
     }
	
	public static void logout() {
		User user = User.currUser();

		if (user == null) {
			LoginAndRegisterAction.login();
		}

		ErrorInfo error = new ErrorInfo();

		user.logout(error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		login();
	}

	/**
	 * 包含登录页面的登录
	 */
	public static void logining() {
	
		business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
		Map<String,java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks.currentBottomlinks();
		   
		if(null != currBackstageSet){
		  Cache.delete("backstageSet");//清除系统设置缓存
		}
		   
		if(null != bottomLinks){
		  Cache.delete("bottomlinks");//清除底部连接缓存
		}
		ErrorInfo error = new ErrorInfo();

		String name = params.get("name");
		String url = request.headers.get("referer").value();
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");
		flash.put("name", name);
		flash.put("password", password);
		flash.put("code", code);
		if (StringUtils.isBlank(name)) {
			flash.error("请输入用户名");
			redirect(url);
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			redirect(url);
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			redirect(url);

		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			redirect(url);
		}

		if(!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			redirect(url);
		}
		User user = new User();
		user.name=name;
		 if (user.id < 0) {
			flash.error("该用户名不存在");
			redirect(url);
		}
		
		

		if (user.login(password,false, error) < 0) {
			flash.error(error.msg);
			redirect(url);
		}
		
		if (Constants.LOGIN.equalsIgnoreCase(url)) {
			if(Constants.IPS_ENABLE && (user.getIpsStatus() != IpsCheckStatus.IPS)){
				CheckAction.approve();
			}
			
			AccountHome.home();
		}

		redirect(Router.reverse("front.account.AccountHome.home").url);
	}

	/**
	 * 登录页面登录logining
	 */
	public static void topLogin() {
		ErrorInfo error = new ErrorInfo();

		String userName = params.get("name");
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");

		flash.put("name", userName);
		flash.put("password", password);
		flash.put("code", code);

		if (StringUtils.isBlank(userName)) {
			flash.error("请输入用户名");
			login();
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			login();
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			login();

		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			login();
		}

		if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			login();
		}

		User user = new User();
		user.name = userName;

		if (user.login(password,false, error) < 0) {
			flash.error(error.msg);
			login();
		}

		AccountHome.home();
	}

	/**
	 * 跳转到注册页面
	 */
	public static void register() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

//		ErrorInfo error = new ErrorInfo();
//		String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		String step = flash.get("step");
		if (step == null)
			step = "1";

		render(loginOrRegister, step);
	}

	/**
	 * 跳转到用户协议页面
	 */
	public static void agreement() {
 		render();
	}
	
	/**
	 * 获取验证码并返回页面
	 */
	public static void codeReturn(String codeImg) {
		String randomID = (String) Cache.get(codeImg);

		JSONObject json = new JSONObject();
		json.put("randomID", randomID);

		renderJSON(json);
	}
	
	/**
	 * 手机注册用户
	 */
	public static void registering_tel() {
		/*预防夸张伪造请求过滤*/
		ErrorInfo error = new ErrorInfo();
//		JSONObject json = new JSONObject();
		
		String step = params.get("step");
		if ("1".equals(step)) {
			String name = params.get("userName");
			String phone = params.get("mobile");
			String password = params.get("password");
			String randomID = (String) Cache.get(params.get("randomID"));
			String code = params.get("code");
			
			flash.put("userName", name);
			flash.put("jzphone", phone);
			flash.put("password", password);
			flash.put("code", code);
			
			if (StringUtils.isBlank(name)) {
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "请填写用户名";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}
		
			if (StringUtils.isBlank(password)) {
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "请输入密码";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}

			if ("1".equals(step)) {
				if (StringUtils.isBlank(code)) {
					String [] staff=new String[2];
					staff[0] = "0";
					staff[1] = "请输入验证码";
//					json.put("error", staff);
//					json.put("success", "");
//					renderJSON(json);
					
					flash.put("errorMsg", staff[1]);
					register();
				}
			}

			if (!RegexUtils.isValidUsername(name)) {
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "请填写符合要求的用户名";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}

			if (!RegexUtils.isMobileNum(phone)) {
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "请填写正确的手机号码";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}

			if (!RegexUtils.isValidPassword(password)) {
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "请填写符合要求的密码";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}

			if (!code.equalsIgnoreCase(randomID)) {
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "验证码输入有误";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}

			//验证用户是否被注册过
			if(User.isNameExist(name, error) == -2){
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "此用户已存在";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}

			//验证邮箱是否被注册过
			if(User.isMobileExist(phone, error) == -2){
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "此手机号码已被注册";
//				json.put("error", staff);
//				json.put("success", "");
//				renderJSON(json);
				
				flash.put("errorMsg", staff[1]);
				register();
			}
			
			if (error.code < 0) {
				flash.put("errorMsg", error.msg);
				register();
			}
			
			User user = new User();
			user.time = new Date();
			user.name = name;
			user.password = password;
			user.telephone=phone;
			
			user.register(error);

			if (error.code < 0) {
				flash.put("errorMsg", error.msg);
				register();
			}
			
			flash.put("step", "2");
			register();
		} else if ("2".equals(step)) {
			// 获取前台的输入的手机验证码
			String phoneCode = params.get("recommended");
			String serveCode = session.get("codeSMS");
			if(serveCode==null||serveCode.equals("")){
				String [] staff=new String[2];
				staff[0] = "0";
				staff[1] = "请发送手机验证码";
				flash.put("step", "2");
				flash.put("errorMsg", staff[1]);
				register();
			}		
			long sTime = Long.valueOf(session.get("stime"));
			long limit = BackstageSet.getCurrentBackstageSet().smsLimit;
			
			//手机短信验证
			if ("2".equals(step)) {
				
			
				
				if (System.currentTimeMillis() - sTime > limit) {
					// timeout
					String [] staff=new String[2];
					staff[0] = "0";
					staff[1] = "验证超时";
//					json.put("error", staff);
//					json.put("success", "");
//					render(json);
					
					flash.put("errorMsg", staff[1]);
					register();
				}
				
				if (!serveCode.equals(phoneCode)) {
					// validate code not matched7
					String [] staff=new String[2];
					staff[0] = "0";
					staff[1] = "手机验证码输入不正确";
//					json.put("error", staff);
//					json.put("success", "");
//					render(json);
					flash.put("step", "2");
					flash.put("errorMsg", staff[1]);
					register();
				}
			}
			if (User.updateMobileVerified(User.currUser().id)) {
				User.currUser().isMobileVerified=true;
				flash.put("regType", "tel");
				registerSuccess();
			} else {
				flash.put("step", "2");
				flash.put("errorMsg", "验证手机号码失败");
				register();
			}
		}
	}
	
	/**
	 * 手机下发短信验证码
	 */
	public static void templateSMS_send(){
		SendTemplateSMS sms = new SendTemplateSMS();
		JSONObject json = new JSONObject();

		String phone = params.get("phone");// 获取前台输入的手机号

		// 100000-1000000以内的随机数
		int codeSMS = (int) (Math.random() * 900000 + 100000);
		
		Logger.debug("sms code: %s", codeSMS);
		HashMap<String, Object> result = sms.sendTemplateSMS(phone, codeSMS);
		json.put("status", result.get("statusCode"));
		if ("000000".equals(result.get("statusCode"))) {
			json.put("success", true);
			session.put("codeSMS", codeSMS);
			session.put("stime", System.currentTimeMillis());
		} else {
			json.put("success", false);
		}
		
		renderJSON(json);
	}
	
	/**
	 * 验证注册
	 */
	@Transactional(readOnly = false) 
	public static void registering() {
		/*预防夸张伪造请求过滤*/
		checkAuthenticity();
		
		String name = params.get("userName");
		String email = params.get("email").toLowerCase();
		String password = params.get("password");
		String randomID = (String) Cache.get(params.get("randomID"));
		String code = params.get("code");

		flash.put("userName", name);
		flash.put("email", email);
		flash.put("password", password);
		flash.put("code", code);
		
//		JSONObject json = new JSONObject();
	
		if (StringUtils.isBlank(name)) {
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "请填写用户名";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}
	
		if (StringUtils.isBlank(password)) {
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "请输入密码";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}
		if (StringUtils.isBlank(code)) {
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "请输入验证码";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}
//
//		if (!RegexUtils.isValidUsername(name)) {
//			String [] staff=new String[2];
//			staff[0] = "0";
//			staff[1] = "请填写符合要求的用户名";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
//		}

		if (!RegexUtils.isEmail(email)) {
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "请填写正确的邮箱地址";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}

		if (!RegexUtils.isValidPassword(password)) {
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "请填写符合要求的密码";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}

		if (!code.equalsIgnoreCase(randomID)) {
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "验证码输入有误";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}

		ErrorInfo error = new ErrorInfo();
		//验证用户是否被注册过
		if(User.isNameExist(name, error) == -2){
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "此用户已存在";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}

		//验证邮箱是否被注册过
		if(User.isEmailExist(email, error) == -2){
			String [] staff=new String[2];
			staff[0] = "0";
			staff[1] = "此邮箱已被注册";
//			json.put("error", staff);
//			json.put("success", "");
//			renderJSON(json);
			
			flash.put("errorMsg", staff[1]);
			register();
		}
		if (error.code < 0) {
			flash.put("errorMsg", error.msg);
			register();
		}

		
		User user = new User();

		user.time = new Date();
		user.name = name;
		user.password = password;
		user.email = email;

		
		user.register(error);
		
		if (error.code < 0) {
			flash.put("errorMsg", error.msg);
			register();
		}
		
		// 发送激活邮件
		TemplateEmail.activeEmail(user, error);
		if (error.code < 0) {
			Logger.error("激活邮件发送失败");
			flash.put("errorMsg", "激活邮件发送失败");
			register();
		}
		
		flash.put("regType", "mail");
		registerSuccess();
	}

	/**
	 * 生成验证码图片
	 * 
	 * @param id
	 */
	public static void getImg(String id) {
		Images.Captcha captcha = CaptchaUtil.CaptchaImage(id);
		
		if(captcha == null){
			return;
		}
		
		renderBinary(captcha);
	}

	/**
	 * 刷新验证码
	 */
	public static void setCode() {
		String randomID = CaptchaUtil.setCaptcha();

		JSONObject json = new JSONObject();
		json.put("img", randomID);
		renderJSON(json.toString());
	}

	/**
	 * 校验验证码
	 */
	public static void checkCode(String randomId, String code) {

		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		if (StringUtils.isBlank(code)) {
			error.code = -1;
			error.msg = "请输入验证码";

			json.put("error", error);
			renderJSON(json);
		}

		if (StringUtils.isBlank(randomId)) {
			error.code = -1;
			error.msg = "请刷新验证码";

			json.put("error", error);
			renderJSON(json);
		}

		String radomCode = CaptchaUtil.getCode(randomId);

		if (!code.equalsIgnoreCase(radomCode)) {
			error.code = -1;
			error.msg = "验证码错误";

			json.put("error", error);
			renderJSON(json);
		}

		json.put("error", error);
		renderJSON(json);
	}

	/**
	 * 验证邮箱是否已存在
	 * 
	 * @param email
	 */
	public static void hasEmailExist(String email) {
		ErrorInfo error = new ErrorInfo();
		
		email = email.toLowerCase();
		User.isEmailExist(email, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json.toString());

	}

	/**
	 * 验证用户名是否已存在
	 * 
	 * @param name
	 */
	public static void hasNameExist(String name) {
		ErrorInfo error = new ErrorInfo();

		User.isNameExist(name, error);

		JSONObject json = new JSONObject();
		json.put("error", error);

		renderJSON(json.toString());
	}

	/**
	 * 验证手机号码是否已存在
	 * 
	 * @param name
	 */
	public static void hasMobileExist(String telephone) {
		ErrorInfo error = new ErrorInfo();

//		User user = new User();
		int nameIsExist = User.isMobileExist(telephone, error);

		JSONObject json = new JSONObject();
		json.put("result", nameIsExist);

		renderJSON(json.toString()); 
	}

	/**
	 * 底部链接信息查询
	 * 
	 * @param name
	 */
	public static void buttomLinks(String num) {
		/* 初始化底部链接 */
		List<BottomLinks> result = BottomLinks.queryFrontBottomLinks(num);
		JSONObject json = new JSONObject();
		json.put("result", result);
		renderJSON(json.toString());
	}

//	/**
//	 * 查询平台设置信息
//	 */
//	public static void systemInfo() {
//		ErrorInfo error = new ErrorInfo();
//
//		BackstageSet info = new BackstageSet();
//		List<BackstageSet> result = info.querySystemInfo(error);
//		JSONObject json = new JSONObject();
//		json.put("result", result);
//		renderJSON(json.toString());
//
//	}

	/**
	 * 注册跳转到成功页面
	 */
	public static void registerSuccess() {
		User user = User.currUser();
		if (user == null) {
			login();
		}
		
		if (Constants.IPS_ENABLE) {
			CheckAction.approve();
		}

		if (user.isEmailVerified) {
			AccountHome.home();
		}

		
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

		render(loginOrRegister);
	}

	/**
	 * 激活帐号
	 * 
	 * @param sign
	 */
	public static void accountActivation(String sign) {
		ErrorInfo error = new ErrorInfo();

		long id = Security.checkSign(sign, Constants.ACTIVE, Constants.VALID_TIME, error);

		if (error.code < 0) {
			flash.error(error.msg);
			login();
		}

		User user = new User();
		user.id = id;
        user.isEmailVerified=false;
        User.setCurrUser(user);
		//user.activeEmail(error);
		
		if (Constants.IPS_ENABLE) {
			if (error.code < 0) {
				flash.error(error.msg);

				login();
			}
			
			
			if(StringUtils.isNotBlank(user.ipsAcctNo)) {
				user.logout(error);
				flash.error(error.msg);

				login();
			}
			
			
			CheckAction.approve();
		} else {
			if (error.code < 0) {
				flash.error(error.msg);
				render();
			}
		
			user = User.currUser();
			render(user);
		}
	}
	
	/**
	 * 通过邮箱找回用户名
	 */
	public static void findBackUsernameByEmail() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 通过邮箱找回用户名后跳转到成功页面
	 */
	public static void emailSuccess() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

		String email = flash.get("email");
		if (email == null) {

			login();
		}

		String emailUrl = EmailUtil.emailUrl(email);
		render(loginOrRegister, emailUrl);
	}

	/**
	 * 发送找回用户名邮件
	 */
	public static void saveUsernameByEmail(String email, String code,
			String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("email", email);

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			findBackUsernameByEmail();
		}

		if (StringUtils.isBlank(email)) {
			flash.error("请输入邮箱地址");
			findBackUsernameByEmail();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请输入正确的邮箱地址");
			findBackUsernameByEmail();
		}

		if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			flash.error("验证码错误");
			findBackUsernameByEmail();
		}

		User.isEmailExist(email, error);

		if (error.code != -2) {
			flash.error("对不起，该邮箱没有注册");
			findBackUsernameByEmail();
		}

		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByEmail();
		}

		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 1;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

		String content = new String(tEmail.content);
		
		String url = Constants.LOGIN ;
		
		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_EMAIL, email);
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByEmail();
		}
		
		flash.error("邮件发送成功");
		flash.put("emailUrl", EmailUtil.emailUrl(email));

		login();
	}

	/**
	 * 通过手机找回用户名
	 */
	public static void findBackUsernameByTele() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 通过手机找回用户名后跳转到成功页面
	 */
	public static void teleSuccess() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 发送找回用户的短信
	 */
	public static void saveUsernameByTele(String mobile, String code,
			String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("mobile", mobile);
		flash.put("code", code);

		if (StringUtils.isBlank(mobile)) {
			flash.error("请输入手机号码");
			findBackUsernameByTele();
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			findBackUsernameByTele();
		}

		if(Cache.get(randomID) == null) {
			flash.error("请刷新验证码");
			findBackUsernameByTele();
		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			findBackUsernameByTele();
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			flash.error("请输入正确的手机号码");
			findBackUsernameByTele();
		}

		if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			findBackUsernameByTele();
		}

		User.isMobileExist(mobile, error);

		if (error.code != -2) {
			flash.error("该手机号码不存在或未绑定");
			findBackUsernameByTele();
		}

		MobileUtil.mobileFindUserName(mobile, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByTele();
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		TemplateSms sms = new TemplateSms();
		sms.id = Templets.S_FIND_USERNAME;
		
		t_users user = User.queryUserByMobile(mobile, error);
		
		String content = sms.content;
		
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);

		SMSUtil.sendSMS(mobile, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByTele();
		}

		flash.put("code", "");
		flash.error(error.msg);

		login();
	}

	/**
	 * 通过手机或邮箱找回密码
	 */
	public static void findPassword(){
		JSONObject json=new JSONObject();
		String name=params.get("account");
    	String randomID=params.get("randomID");
    	String codetc=params.get("codetc");
    	String regType=params.get("regType");
    	ErrorInfo error=new ErrorInfo();
    
    	flash.put("name",name);
    	if((User.isEmailExist(name, error)!=-2)&&(User.isMobileNotExist(name, error)!=0)){
    	    json.put("success","该用户不存在，请您先去注册");
			renderJSON(json);			
		}
    	//输入的验证码不对
    	if (!codetc.equalsIgnoreCase(Cache.get(randomID).toString())) {
    		json.put("success", "验证码输入不正确");
    		renderJSON(json);
		}
    	//通过手机
    	if(regType.equals("tel")){
    	User user=new User();
    	user.id=user.queryIdByMobile(name, error);
 
    	if(!user.isMobileVerified){//手机未激活
    	json.put("success", "您的手机未激活");
    	renderJSON(json);
    	}
        flash.put("errors", "2");
        json.put("success", "0");
   	    renderJSON(json);
    	}
    	//通过邮箱
    	if(regType.equals("mail")){
    	 User user=new User();
    	 user.id=user.queryIdByEmail(name, error);
    	
    	 if(!user.isEmailVerified){
    	 json.put("success", "您的邮箱未激活");
    	 renderJSON(json); 
    	 }
         flash.put("errors", "3");
         json.put("success", "1");
         renderJSON(json);
    	}
    	json.put("success", "系统错误请稍后重试");
		renderJSON(json);
		
	}
	/**
	 * 通过手机短信找回密码
	 */
    public static void  findPasswordByTele(){
    	
    	JSONObject json = new JSONObject();
    	String name=params.get("name");
    	String errorcode=params.get("error");//判断状态
    	ErrorInfo error = new ErrorInfo();
    	flash.put("name", name);
  
    	if(errorcode.equals("2")){
    	String codetc=params.get("smsCode");
    	String code=session.get("codeSMS");
       //验证用户是否被注册过
       if(User.isMobileNotExist(name, error)!=0){
    	    flash.put("errors", "2");
			json.put("success","该用户不存在，请您先去注册");
			renderJSON(json);			
		}
		//验证码有效期超时
    	long sTime = Long.valueOf(session.get("stime"));
    	long limit = BackstageSet.getCurrentBackstageSet().smsLimit;
    
    	
    	if(System.currentTimeMillis()-sTime>limit){
    		flash.put("errors", "2");
    	    json.put("success", "验证码超时...");
    	    renderJSON(json);
    	}
    	//输入的验证码不对
    	if(!codetc.equals(code)){
    		flash.put("errors", "2");
    		json.put("success", "验证码输入不正确");
    		renderJSON(json);
    	}
    	 
    	 flash.put("errors", "3");
    	 json.put("success", "1");
    	 renderJSON(json);
    	  
       }else{
    	String password=params.get("password");
//    	User user=new User();
    	User.updatePasswordByMobileApp(name, password, error);
    	if(error.code==-3){
    		json.put("success", error.msg);
    		renderJSON(json);
    	}
    	 flash.put("errors", "3");
    	 json.put("success", "0");
    	 renderJSON(json);
       }
     }
    
    /**
     * 通过邮箱找回密码
     */
    public static void findPasswordByEmail(){
        JSONObject json=new JSONObject();
		
		String email=params.get("account");
		String code=params.get("code");
		String randomID=params.get("randomID");
		ErrorInfo error=new ErrorInfo();
		flash.put("email", email);

		if (StringUtils.isBlank(code)) {
			 json.put("success", "请输入验证码");
			 flash.error("请输入邮箱地址");
		
			renderJSON(json);
		}

		if (StringUtils.isBlank(email)) {
			json.put("success", "请输入邮箱地址");
			flash.error("请输入邮箱地址");
			
			renderJSON(json);
		}

		if (!RegexUtils.isEmail(email)) {
			json.put("success", "请输入正确的邮箱地址");
			flash.error("请输入正确的邮箱地址");
		
			renderJSON(json);
		}

		if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			json.put("success", "验证码错误");
			flash.error("验证码错误");
			
			renderJSON(json);
		}

		User.isEmailExist(email, error);

		if (error.code != -2) {
			json.put("success", "对不起，该邮箱没有注册");
			flash.error("对不起，该邮箱没有注册");
	
			renderJSON(json);
		}
		
		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
		    json.put("success", "邮件发送失败");
			renderJSON(json);
		}
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 3;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PASSWORD_EMAIL + sign;

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
			flash.error(error.msg);
			
			json.put("success", "邮件发送失败");
			renderJSON(json);
		}
		
//		EmailUtil.emailFindUserName(email, error);
//
//		if (error.code < 0) {
//			flash.error("邮件发送失败，请重新发送");
//			resetPasswordByEmail();
//		}

		flash.put("email", "");
		flash.put("code", "");
		flash.error("邮件发送成功");
		flash.put("emailUrl", EmailUtil.emailUrl(email));
		json.put("success", "0");
		renderJSON(json);

		
	}

	/**
	 * 通过手机重置密码
	 */
	public static void resetPasswordByMobile() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		String randomId = Codec.UUID();
		boolean checkMsgCode = Constants.CHECK_MSG_CODE;
		
		render(loginOrRegister, randomId, checkMsgCode);
	}

	/**
	 * 保存重设的密码
	 */
	public static void savePasswordByMobile(String mobile, String code,
			String password, String confirmPassword, String randomID, String captcha) {
		ErrorInfo error = new ErrorInfo();
        
		User.updatePasswordByMobile(mobile, code, password, confirmPassword,
				randomID, captcha, error);

		if (error.code < 0) {
			flash.put("mobile", mobile);
			flash.put("code", code);
			flash.put("password", password);
			flash.put("confirmPassword", confirmPassword);
			flash.put("captcha", captcha);

			flash.error(error.msg);
			
			resetPasswordByMobile();
		}

		flash.error(error.msg);

		login();
	}

	/**
	 * 通过邮箱重置密码
	 */
	public static void resetPasswordByEmail() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 发送重置密码邮件
	 */
	public static void sendResetEmail(String email, String code, String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("email", email);

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			resetPasswordByEmail();
		}

		if (StringUtils.isBlank(email)) {
			flash.error("请输入邮箱地址");
			resetPasswordByEmail();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请输入正确的邮箱地址");
			resetPasswordByEmail();
		}

		if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			flash.error("验证码错误");
			resetPasswordByEmail();
		}

		User.isEmailExist(email, error);

		if (error.code != -2) {
			flash.error("对不起，该邮箱没有注册");
			resetPasswordByEmail();
		}
		
		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByEmail();
		}
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 3;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PASSWORD_EMAIL + sign;

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
			flash.error(error.msg);
			findBackUsernameByEmail();
		}
		
//		EmailUtil.emailFindUserName(email, error);
//
//		if (error.code < 0) {
//			flash.error("邮件发送失败，请重新发送");
//			resetPasswordByEmail();
//		}

		flash.put("email", "");
		flash.put("code", "");
		flash.error("邮件发送成功");
		flash.put("emailUrl", EmailUtil.emailUrl(email));

		login();
	}
	
	/**
	 * 跳转到重置密码页面
	 */
	public static void resetPassword(String sign) {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			login();
		}
		
		String name = User.queryUserNameById(id, error);
		
		render(loginOrRegister, name,sign);
	}
	
	/**
	 * 保存重置密码
	 */
	public static void savePasswordByEmail(String sign, String password, String confirmPassword) {
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			login();
		}
		
		User user = new User();
		user.id = id;
		user.updatePasswordByEmail(password, confirmPassword, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			resetPassword(sign);
		}
		
		flash.error(error.msg);
		
		login();
	}
	
	/**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobile(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
        
        if (Constants.CHECK_MSG_CODE) {
			if (StringUtils.isBlank(captcha)) {
				
				error.code = -1;
				error.msg = "请输入图形验证码";
				
				json.put("error", error);
				
				renderJSON(json);
			}
			
			if (StringUtils.isBlank(randomID)) {
				
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(captcha)) {
				
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
  		}
        
        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, error);

            if (error.code < 0) {

                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);

        json.put("error", error);

        renderJSON(json);
    }
    
    /**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobileIsExists(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
        
        if (Constants.CHECK_MSG_CODE) {
			if (StringUtils.isBlank(captcha)) {
				
				error.code = -1;
				error.msg = "请输入图形验证码";
				
				json.put("error", error);
				
				renderJSON(json);
			}
			
			if (StringUtils.isBlank(randomID)) {
				
	        	error.code = -1;
	        	error.msg = "请刷新验证码";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(captcha)) {
				
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
  		}
        
        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, error);

            if (error.code != -2) {

                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);

        json.put("error", error);

        renderJSON(json);
    }
}
