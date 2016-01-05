package controllers.front.account;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jingtum.Jingtum;
import models.t_content_news;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_audit_items;
import models.t_dict_banks;
import models.t_dict_payment_gateways;
import models.t_user_bank_accounts;
import models.t_user_over_borrows;
import models.t_user_recharge_details;
import models.v_credit_levels;
import models.v_user_account_statistics;
import models.v_user_audit_items;
import models.v_user_deals_record;
import models.v_user_detail_credit_score_audit_items;
import models.v_user_detail_credit_score_invest;
import models.v_user_detail_credit_score_loan;
import models.v_user_detail_credit_score_normal_repayment;
import models.v_user_detail_credit_score_overdue;
import models.v_user_detail_score;
import models.v_user_details;
import models.v_user_withdrawals;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.mvc.With;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.GopayUtils;
import utils.JPAUtil;
import utils.JsonDateValueProcessor;
import utils.PageBean;
import utils.Security;
import utils.ServiceFee;
import business.AuditItem;
import business.BackstageSet;
import business.Bid;
import business.CreditLevel;
import business.News;
import business.Optimization.UserOZ;
import business.OverBorrow;
import business.Payment;
import business.User;
import business.UserAuditItem;
import business.UserBankAccounts;
import business.Vip;
import business.YeepayPayment;

import com.shove.Convert;

import constants.Constants;
import constants.Constants.PayType;
import constants.Constants.RechargeType;
import constants.IPSConstants;
import constants.OptionKeys;
import controllers.BaseController;
import controllers.SubmitCheck;
import controllers.SubmitOnly;
import controllers.SubmitRepeat;
import controllers.front.bid.BidAction;

@With({CheckAction.class, SubmitRepeat.class})
public class FundsManage extends BaseController {

	//-------------------------------资金管理-------------------------
	/**
	 * 账户信息
	 */
	public static void accountInformation(){
		User user = User.currUser();
		long userId = user.id;
		
		ErrorInfo error = new ErrorInfo();
		v_user_account_statistics accountStatistics = User.queryAccountStatistics(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		UserOZ accountInfo = new UserOZ(userId);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<v_user_details> userDetails = User.queryUserDetail(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 3,error);
		
		boolean isIps = Constants.IPS_ENABLE;
		
		render(user, accountStatistics, accountInfo, userDetails, userBanks, backstageSet, content, news, isIps);
	}
	
	/**
	 * 添加银行账号
	 */
	public static void addBank(String addBankName, String addAccount, String addAccountName){
		User user = User.currUser();
		
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts bankUser =  new UserBankAccounts();
		
		bankUser.userId = user.id;
		bankUser.bankName = addBankName;
		bankUser.account = addAccount;
		bankUser.accountName = addAccountName;
		
		bankUser.addUserBankAccount(error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 修改银行卡信息
	 */
	public static void bankCardup(){
	  User user = User.currUser();
	  
	  UserBankAccounts util = new UserBankAccounts();
	  
	  t_user_bank_accounts bankAccount = util.findUserBankCard(user.id);
	  
	  Object banks = Cache.get("banks");
	  String openBank = null;
	  for (t_dict_banks d : (List<t_dict_banks>) banks) {
		if (d.id == bankAccount.open_bank_id) {
			openBank = d.name;
			break;
			}
		}
		String openProvince = null;
		Object provinces = Cache.get("provinces");
		for (t_dict_ad_provinces d : (List<t_dict_ad_provinces>) provinces) {
			if (d.id == bankAccount.open_province) {
				openProvince = d.name;
				break;
			}
		}
		
		String openCity = null;
		Object city = Cache.get("citys");
		for (t_dict_ad_citys d : (List<t_dict_ad_citys>) city) {
			if (d.id == bankAccount.open_city) {
				openCity = d.name;
				break;
			}
		}
	  flash.put("openCity", openCity);
	  flash.put("openProvince", openProvince);
	  flash.put("provinces", provinces);
	  flash.put("openBank", openBank);
	  flash.put("bankAccount", bankAccount);
	  flash.put("banks", banks);
	  flash.put("type", true);
	  bankCard();
	}
	
	/**
	 * 用户银行卡信息
	 */
	public static void bankCard() {
	
		String type=params.get("type");
		
		User user = User.currUser();
		boolean edit = false;
		UserBankAccounts util = new UserBankAccounts();
		t_user_bank_accounts bankAccount = null;
		if ((type!=null && type.equals("add"))||(flash.get("type")!=null&&flash.get("type").equals("add"))) {
			Object provinces = Cache.get("provinces");
			Object banks = Cache.get("banks");
			edit = true;
			render(user, edit, banks, provinces,type);
		}
		String bankIds=params.get("bankId");
		if(bankIds==null||bankIds.equals("")){
			bankIds=flash.get("bankId");
		}
	   
		/*查看银行卡信息*/
	 if((type!=null&&type.equals("select"))||(flash.get("type")!=null&&flash.get("type").equals("select"))){
		  
		     Long bankId=Long.parseLong(bankIds);
		     if (flash.get("success") == null || "true".equals(flash.get("success"))) {
			    bankAccount = util.findUserBankCardbyID(bankId);
		      }
		     
	        String openBank = null;
		    Object dict = Cache.get("banks");
		    for (t_dict_banks d : (List<t_dict_banks>) dict) {
			  if (d.id == bankAccount.open_bank_id) {
				openBank = d.name;
				break;
				}
            }
		    
		    String openProvince = null;
		    dict = Cache.get("provinces");
		    for (t_dict_ad_provinces d : (List<t_dict_ad_provinces>) dict) {
			   if (d.id == bankAccount.open_province) {
				 openProvince = d.name;
				 break;
			   }
		    }
		    
		    String openCity = null;
		    dict = Cache.get("citys");
		    for (t_dict_ad_citys d : (List<t_dict_ad_citys>) dict) {
			   if (d.id == bankAccount.open_city) {
				  openCity = d.name;
				  break;
			      }
		    }
		         edit=false;
		        type="select";
		       flash.put("type", false);
		       render(user, bankAccount, openBank, openProvince, openCity,edit,type);
	}
	/*删除银行卡*/
	if((type!=null)&&type.equals("delete")){
		 UserBankAccounts.deleteBankAccount(Long.parseLong(bankIds));
		 String currPage=params.get("currPage");
		 String pageSize=params.get("pageSize");
		BankCardItem(Integer.parseInt(currPage), Integer.parseInt(pageSize));
		
	}
   /*更新银行卡*/
	if((type!=null && type.equals("update"))||(flash.get("type")!=null&&flash.get("type").equals("true"))){
	  
	     Long bankId=Long.parseLong(bankIds);
	      bankAccount = util.findUserBankCardbyID(bankId);
		  Object banks = Cache.get("banks");
		  String openBank = null;
		  for (t_dict_banks d : (List<t_dict_banks>) banks) {
			if (d.id == bankAccount.open_bank_id) {
				openBank = d.name;
				break;
				}
			}
			String openProvince = null;
			Object provinces = Cache.get("provinces");
			for (t_dict_ad_provinces d : (List<t_dict_ad_provinces>) provinces) {
				if (d.id == bankAccount.open_province) {
					openProvince = d.name;
					break;
				}
			}
			
			String openCity = null;
			Object city = Cache.get("citys");
			for (t_dict_ad_citys d : (List<t_dict_ad_citys>) city) {
				if (d.id == bankAccount.open_city) {
					openCity = d.name;
					break;
				}
			}
		
           type="update";
		  render( openCity,openProvince,provinces,provinces,openBank,bankAccount,banks,type,user);
}	
 /*删除银行卡信息*/



//		
//		String openBank = null;
//		Object dict = Cache.get("banks");
//		for (t_dict_banks d : (List<t_dict_banks>) dict) {
//			if (d.id == bankAccount.open_bank_id) {
//				openBank = d.name;
//				break;
//			}
//		}
//		
//		String openProvince = null;
//		dict = Cache.get("provinces");
//		for (t_dict_ad_provinces d : (List<t_dict_ad_provinces>) dict) {
//			if (d.id == bankAccount.open_province) {
//				openProvince = d.name;
//				break;
//			}
//		}
//		
//		String openCity = null;
//		dict = Cache.get("citys");
//		for (t_dict_ad_citys d : (List<t_dict_ad_citys>) dict) {
//			if (d.id == bankAccount.open_city) {
//				openCity = d.name;
//				break;
//			}
//		}
//		
		//render(user, bankAccount, openBank, openProvince, openCity);
	}
	
	public static void upBankCard(){
		long sTime=0;
		sTime = Long.valueOf(session.get("stime"));
		long limit = BackstageSet.getCurrentBackstageSet().smsLimit;
		String bankAccountId=params.get("bankID");
		if(sTime == 0){
			flash.put("bankId", Long.parseLong(bankAccountId));
			flash.put("type", true);
			flash.put("success", "false");
			flash.put("errorMsg", "短信发送失败请稍后重试");
			
		}
		if (System.currentTimeMillis() - sTime > limit) {
			flash.put("bankId", Long.parseLong(bankAccountId));
			flash.put("type", true);
			flash.put("success", "false");
			flash.put("errorMsg", "验证超时");
			bankCard();
		}
		
		String chkCode = params.get("chkCode");
		String serveCode = session.get("codeSMS");
		if (chkCode != null && !chkCode.equals(serveCode)) {
			flash.put("bankId", Long.parseLong(bankAccountId));
			flash.put("type", true);
			flash.put("success", "false");
			flash.put("errorMsg", "短信验证码不正确");
			bankCard();
		}	
		
		
		String bankId = params.get("bank");
		String provinceId = params.get("province");
		String cityId = params.get("city");
		String branch = params.get("branch");
		String account = params.get("account");
		
		if(cityId == null||cityId.equals("")){
		
		UserBankAccounts util = new UserBankAccounts();
		t_user_bank_accounts bankAccount = util.findUserBankCardbyID(Long.parseLong(bankAccountId));
		cityId=bankAccount.open_city+"";
		
		}

        
		User user = User.currUser();
		
		t_user_bank_accounts bankInfo = new t_user_bank_accounts();
		bankInfo.open_bank_id = Integer.valueOf(bankId);
		bankInfo.open_province = Integer.valueOf(provinceId);
		bankInfo.open_city = Integer.valueOf(cityId);
		bankInfo.bank_name = branch;
		bankInfo.account = account;
		bankInfo.user_id = user.id;
		bankInfo.account_name = user.realityName;
		bankInfo.verified = true;
		bankInfo.time = new Date(); 
		bankInfo.sta=1;
		bankInfo.save();
		UserBankAccounts.deleteBankAccount(Long.parseLong(bankAccountId));
		flash.put("bankId", bankInfo.id);
		flash.put("type", "select");
		bankCard();
	}
	
	public static void bindBankCard() {
		long sTime=0;
		sTime = Long.valueOf(session.get("stime"));
		long limit = BackstageSet.getCurrentBackstageSet().smsLimit;
		if (System.currentTimeMillis() - sTime > limit) {
			flash.put("success", false);
			flash.put("type", "add");
			flash.put("errorMsg", "验证超时");
			bankCard();
		}
		
		String chkCode = params.get("chkCode");
		String serveCode = session.get("codeSMS");
		if (chkCode != null && !chkCode.equals(serveCode)) {
			flash.put("success", false);
			flash.put("type", "add");
			flash.put("errorMsg", "短信验证码不正确");
			bankCard();
		}
		
		String bankId = params.get("bank");
		String provinceId = params.get("province");
		String cityId = params.get("city");
		String branch = params.get("branch");
		String account = params.get("account");
		
		flash.put("bank", bankId);
		flash.put("province", provinceId);
		flash.put("city", cityId);
		flash.put("branch", branch);
		flash.put("account", account);
		
		User user = User.currUser();
		
		t_user_bank_accounts bankInfo = new t_user_bank_accounts();
		bankInfo.open_bank_id = Integer.valueOf(bankId);
		bankInfo.open_province = Integer.valueOf(provinceId);
		bankInfo.open_city = Integer.valueOf(cityId);
		bankInfo.bank_name = branch;
		bankInfo.account = account;
		bankInfo.user_id = user.id;
		bankInfo.account_name = user.realityName;
		bankInfo.verified = true;
		bankInfo.time = new Date(); 
		bankInfo.sta=1;
		boolean r = bankInfo.create();
		flash.put("success", r);
		flash.put("bankId", bankInfo.id);
		flash.put("type", "select");
		bankCard();
	}
	
	//保存银行账号
	public static void saveBank(){
		render();
	}
	
	/**
	 * 编辑银行账号
	 */

	public static void editBank(long editAccountId, String editBankName, String editAccount, String editAccountName){

		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		UserBankAccounts userAccount = new UserBankAccounts();
		
		userAccount.bankName = editBankName;
		userAccount.account = editAccount;
		userAccount.accountName = editAccountName;

		userAccount.editUserBankAccount(editAccountId, user.id, error);

		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	public static void BankCardItem(int currPage,int pageSize){
		User user=User.currUser();
		PageBean<UserBankAccounts> page=UserBankAccounts.queryPageAllBankAccount(user.id, currPage, pageSize);
		render(page);
	}
	
	/**
	 * 删除银行账号
	 */
	public static void deleteBank(long accountId){
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts.deleteUserBankAccount(User.currUser().id, accountId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 我的信用等级
	 */
	public static void myCredit(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		v_user_detail_score creditScore = User.queryCreditScore(user.id);

		List<t_user_over_borrows> overBorrows = OverBorrow.queryUserOverBorrows(user.id, error);
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}

//		double creditInitialAmount = BackstageSet.queryCreditInitialAmount();
		double creditInitialAmount = BackstageSet.getCurrentBackstageSet().initialAmount;
		
		
		render(user,creditScore,overBorrows,creditInitialAmount);
	}
	
	/**
	 * 信用积分明细(成功借款)
	 */
	public static void creditDetailLoan(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_loan> page = User.queryCreditDetailLoan(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(审核资料)
	 */
	public static void creditDetailAuditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_audit_items> page = User.queryCreditDetailAuditItem(user.id, currPage, 0, key, error);
		
//		if(error.code < 0){
//			renderJSON(error);
//		}
		
		render(page);
	}
	
	/**
	 * 信用积分明细(成功投标)
	 */
	public static void creditDetailInvest(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_invest> page = User.queryCreditDetailInvest(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(正常还款)
	 * @param key
	 */
	public static void creditDetailRepayment(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_normal_repayment> page = User.queryCreditDetailRepayment(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(逾期扣分)
	 * @param key
	 */
	public static void creditDetailOverdue(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_overdue> page = User.queryCreditDetailOverdue(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 查看信用等级规则
	 */
	public static void viewCreditRule(){
		ErrorInfo error = new ErrorInfo();
		List<v_credit_levels> CreditLevels = CreditLevel.queryCreditLevelList(error);
		
		render(CreditLevels);
	}
	
	/**
	 * 查看信用积分规则
	 */
	public static void creditintegral(){
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		long auditItemCount = AuditItem.auditItemCount();
		
		ErrorInfo error = new ErrorInfo();

		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(backstageSet, auditItemCount, amountKey);
	}
	
	/**
	 * 查看科目积分规则
	 */
	public static void creditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_dict_audit_items> page = AuditItem.queryEnableAuditItems(key, currPage, 0, error); // 审核资料
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(page, amountKey);
	}
	
	/**
	 * 审核资料
	 */
	
	/**
	 * 审核资料积分明细（信用积分规则弹窗）
	 */
	public static void auditItemScore(String keyword, String currPage, String pageSize) {
		ErrorInfo error = new ErrorInfo();
		PageBean<AuditItem> page = AuditItem.queryAuditItems(currPage, pageSize, keyword, true, error);
		
		render(page, error);
	}
	
	//申请超额借款
	public static void applyOverBorrow(){
		render();
	}

	//提交申请
	public static void submitApply(){
		render();
	}
	
	/**
	 * 查看超额申请详情
	 */
	public static void viewOverBorrow(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 查看超额申请详情(IPS)
	 */
	public static void viewOverBorrowIps(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 提交资料
	 */  
	public static void userAuditItem(long overBorrowId, long useritemId, long auditItemId, String filename){
		
		ErrorInfo error = new ErrorInfo();

		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.id = useritemId;
		item.auditItemId = auditItemId;
		item.imageFileName = filename;
		item.overBorrowId = overBorrowId;
		item.createUserAuditItem(error);

		JSONObject json = new JSONObject();
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 充值
	 */
	@SubmitCheck
	public static void recharge(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay"+user.id);
		double fee =0;
		if(null != map) {
			fee = (Double) map.get("fee");
		}
			
		
		if (Constants.IPS_ENABLE) {
			
			List<Map<String, Object>> bankList = null;
			String version = BackstageSet.getCurrentBackstageSet().entrustVersion;
			
			if("1.0".equals(version)) {
				bankList = Payment.getBankList(error);
			}
			
			render("@front.account.FundsManage.rechargeIps",user, bankList, version);
		}
		
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		render(user, payType,fee);
	}
	
	/**
	 * app充值
	 */
	public static void rechargeApp(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		if (Constants.IPS_ENABLE) {
			List<Map<String, Object>> bankList = Payment.getBankList(error);
			
			render("@front.account.FundsManage.rechargeIps",user, bankList);
		}
		
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		render(user, payType);
	}
	
	/**
	 * app确认充值
	 */
	public static void submitRechargeApp(int type, double money, int bankType){
		ErrorInfo error = new ErrorInfo();
		
		if (Constants.IPS_ENABLE) {
			String bankCode = params.get("bankCode");
			
			if (money <= 0) {
				flash.error("请输入正确的充值金额");
				rechargeApp();
			}
			
			if (StringUtils.isBlank(bankCode) || bankCode.equals("0")) {
				flash.error("请选择充值银行");
				rechargeApp();
			}
			
			Map<String, String> args = Payment.doDpTrade(money, bankCode, error);
			
			render("@front.account.PaymentAction.doDpTrade", args);
		}
		
		flash.put("type", type);
		flash.put("money", money);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			flash.error("请选择正确的充值方式");
			rechargeApp();
		}
		
		if(money == 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp();
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp();
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, RechargeType.Normal, true, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				rechargeApp();
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, true, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 支付vip，资料审核等服务费
	 */
	public static void rechargePay() {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay"+user.id);
		
		if(null == map) 
			renderText("请求过时或已提交!");
			
		double fee = (Double) map.get("fee");
		double amount = 0;
		boolean isPay = false;
		
		if (Constants.IPS_ENABLE) {
			if (Constants.PAY_TYPE_VIP == PayType.INDEPENDENT) {
				amount = user.balanceDetail.user_amount2;
			} else if(Constants.PAY_TYPE_VIP == PayType.SHARED){
				/* 是否是共享资金托管 */
				isPay = true;
			}
		} else {
			amount = user.balanceDetail.user_amount;
		}
		
		render(user, payType, fee, amount, isPay);
	}
	
	/**
	 * 支付发标保证金
	 */
	@SubmitCheck
	public static void rechargePayIps(){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>)Cache.get("rechargePayIps"+user.id);
		
		if(null == map || map.size() == 0)
			renderText("请求超时!");
		
		double fee = (Double) map.get("fee");
		
		List<Map<String, Object>> bankList = null; 
		String version = BackstageSet.getCurrentBackstageSet().entrustVersion; 

		if("1.0".equals(version)) { 
			bankList = Payment.getBankList(error); 
		} 
		
		render("@front.account.FundsManage.rechargePayIps",user, bankList, fee);
	}
	
	/**
	 * 确认充值
	 */
	@SubmitOnly
	public static void submitRecharge(int type, double money, int bankType){
		ErrorInfo error = new ErrorInfo();
		if (Constants.IPS_ENABLE) {
			String bankCode = params.get("bankCode");
			
			if (money <= 0 || money > Constants.MAX_VALUE) {
				flash.error("充值金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
				recharge();
			}
			
			if ((StringUtils.isBlank(bankCode) || bankCode.equals("0")) && "1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
				flash.error("请选择充值银行");
				recharge();
			}
			
			Map<String, String> args = Payment.doDpTrade(money, bankCode, error);
			
			render("@front.account.PaymentAction.doDpTrade", args);
		}
		if(type<1 || type >4) {
			flash.error("请选择正确的充值方式");
			recharge();
		}
		
		if(money <= 0 || money > Constants.MAX_VALUE) {
			flash.error("充值金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
			recharge();
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			flash.error("请输入正确的充值金额");
			recharge();
		}
		
//		flash.put("type", type);
//		flash.put("money", money);
//		flash.put("bankType",bankType);
//		
//		/**
//		 * 非资金托管模式下，纪录此次充值
//		 */
//		Payment.doRecharge(money, error);//15++
//		
		
		if(type == 4) {
			Map<String, String> args = User.jingPay(moneyDecimal, bankType, RechargeType.Normal, false, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			render("@front.account.FundsManage.submitRecharge4",args);
		}
		if(type == 3) {
			Map<String, String> args = User.yeePay(moneyDecimal, bankType, RechargeType.Normal, false, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			render("@front.account.FundsManage.submitRecharge3",args);
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, RechargeType.Normal, false, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render(args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, false, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 确认支付
	 */
	public static void submitRechargePay(int type, int bankType, boolean isUse){
		ErrorInfo error = new ErrorInfo();
		flash.put("type", type);
		flash.put("bankType",bankType);
		
		if(type<1 || type >4) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
		double fee = (Double) map.get("fee");
		int rechargeType = (Integer) map.get("rechargeType");
		double amount = 0;
		
		if (Constants.IPS_ENABLE) {
			if (Constants.PAY_TYPE_VIP == PayType.INDEPENDENT) {
				amount = user.balanceDetail.user_amount2;
			}
		} else {
			amount = user.balanceDetail.user_amount;
		}
		
		double money = isUse ? (fee - amount) : fee;
		
		if(money <= 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		if(type == 4){
			Map<String, String> args = User.jingPay(moneyDecimal, bankType, rechargeType, false, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			render("@front.account.FundsManage.submitRecharge4",args);
		}
		if(type == 3) {
			Map<String, String> args = User.yeePay(moneyDecimal, bankType, rechargeType, false, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			render("@front.account.FundsManage.submitRecharge3",args);
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, rechargeType, false, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, rechargeType, false, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				rechargePay();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 环迅回调
	 */
	public static void callback(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		int isSuccess=0;
		try {
			obj = t_user_recharge_details.find(sql, billno).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		//User.setCurrUser(Long.parseLong(obj.toString())); // 更新缓存中的用户对象，session发生了变法
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种

		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info,isSuccess);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info,isSuccess);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info,isSuccess);
		} 
		
		User.recharge(billno, Double.parseDouble(amount), error);
		int rechargeType = Convert.strToInt(billno.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				
				JPAUtil.executeUpdate(error, "update t_bids set status = status - 10 where id = ?", bid.id);
				
				if (error.code < 0) {
					flash.error(error.msg);
					
					render(error);
				}
				
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				flash.error(error.msg);
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				flash.put("msg", error.msg);
				 
				/* 页面需要的返回数据 */
				if(bid.id > 0){
					flash.put("no", OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error) + bid.id);
					flash.put("title", bid.title);
					DecimalFormat myformat = new DecimalFormat();
					myformat.applyPattern("##,##0.000");
					flash.put("amount", myformat.format(bid.amount));
					flash.put("status", bid.status);
				}
				
				BidAction.applyNow(bid.productId, error.code, 1);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付资料审核费成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}
		 
		if(error.code < 0) {
			 flash.error(error.msg);
			 render(error);
		}
		
		if("1".equals(attach)) {
			rechargeApp();
		}
		
		info = "交易成功";
		isSuccess=1;
		render(info,isSuccess);
	}
	
	/**
	 * 环迅回调（异步）
	 */
	public static void callbackSys(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, billno).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		//User.setCurrUser(Long.parseLong(obj.toString())); // 更新缓存中的用户对象，session发生了变法
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种

		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info);
		} 
		
		User.recharge(billno, Double.parseDouble(amount), error);
		int rechargeType = Convert.strToInt(billno.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				
				return;
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		}
	}
	
	/**
	 * 国付宝回调
	 */
	public static void gCallback(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String goodsName,String goodsDetail
			,String buyerName,String buyerContact,String merRemark1,String merRemark2,String signValue) {
		Logger.info("---------------国付宝充值同步回调start！------------");
		int isSuccess=0;//默认失败
 		
		ErrorInfo error = new ErrorInfo();
		String info = "";
		
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			
			info = "验证失败，支付失败！";
			render(info,isSuccess);
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			info = "支付失败！";
			render(info,isSuccess);
		}
		
		if ("9999".equals(respCode)) {
			isSuccess=1;
			info = "订单处理中，请耐心等待！";
			render(info,isSuccess);
		}
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				flash.error(error.msg);
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				flash.put("msg", error.msg);
				 
				/* 页面需要的返回数据 */
				if(bid.id > 0){
					flash.put("no", OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error) + bid.id);
					flash.put("title", bid.title);
					DecimalFormat myformat = new DecimalFormat();
					myformat.applyPattern("##,##0.000");
					flash.put("amount", myformat.format(bid.amount));
					flash.put("status", bid.status);
				}
				
				BidAction.applyNow(bid.productId, error.code, 1);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付资料审核费成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}
		 
		if(error.code < 0) {
			 flash.error(error.msg);
			 render(error);
		}
		
		if("1".equals(merRemark1)) {
			rechargeApp();
		}
		
		info = "交易成功";
		isSuccess=1;
		render(info,isSuccess);
	}
	
	/**
	 * 国付宝回调（异步）
	 */
	public static void gCallbackSys(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String goodsName,String goodsDetail
			,String buyerName,String buyerContact,String merRemark1,String merRemark2,String signValue) {
		
		Logger.info("---------------国付宝充值异步回调start！------------");
		
		ErrorInfo error = new ErrorInfo();
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			Logger.info("---------------验证失败，支付失败！------------");
			return ;
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			Logger.info("---------------支付失败！------------");
			return ;
		}
		
		if ("9999".equals(respCode)) {
			Logger.info("---------------订单处理中，请耐心等待！------------");
			return ;
		}
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				
				return;
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		}
	}
	

	/**
	 * 
	 * @param hmac
	 * @param r0_Cmd
	 * @param r1_Code    充值结果  0 | 1
	 * @param r2_TrxId
	 * @param r3_Amt
	 * @param r4_Cur
	 * @param r5_Pid
	 * @param r6_Order
	 * @param r7_Uid
	 * @param r8_MP
	 * @param r9_BType
	 * 
	 * ↓↓↓↓↓↓↓↓↓↓ 以下为可选参数 不参与hamc 计算
	 * 
	 * @param rb_BankId   支付通道编码
	 * @param ro_BankOrderId 银行订单号 
	 * @param rp_PayDate 支付成功时间
	 * @param rq_CardNo  神州行充值卡序列号
	 * @param ru_Trxtime 交易结果通知时间
	 * @param rq_SourceFee  用户手续费
	 * @param rq_TargetFee  商户手续费
	 */
	public static void yCallBack(String hmac,String r0_Cmd, String r1_Code,
			String r2_TrxId, String r3_Amt,String r4_Cur,
			String r5_Pid, String r6_Order, String r7_Uid,
			String r8_MP, String r9_BType, //以上为参与hmac 验证的参数 ，以下为不参与的参数
			String rb_BankId,String ro_BankOrderId,
			String rp_PayDate,String rq_CardNo,
			String ru_Trxtime,String rq_SourceFee,String rq_TargetFee){
		ErrorInfo error=new ErrorInfo();
		int isSuccess=0;
		t_dict_payment_gateways yee = User.gateway(Constants.YEE_GATEWAY, error);
		boolean result=YeepayPayment.verifyCallback(hmac,yee.account, r0_Cmd, r1_Code, r2_TrxId, r3_Amt, r4_Cur, r5_Pid, r6_Order, r7_Uid, r8_MP, r9_BType, yee._key);
		String info="";
		if(result){// hmac 验证通关过
			if("1".equals(r1_Code)){// 充值成功
				User.recharge(r6_Order, Double.parseDouble(r3_Amt), error);
				info="充值成功";
				isSuccess=1;
				render(info,isSuccess);
			}else{
				info="充值平台 返回 : 充值失败";
				render(info,isSuccess);
			}
		}else{
			 info="充值回调  过程被篡改， 请通知管理员";
			 render(info,isSuccess);
		}
	}
	
	
	public static void jCallback(String r0_cmd, String r1_code, String r2_custom, 
			String r3_order, String r4_account, String r5_amount,
			String r6_currency, String r7_product, String r8_ext, 
			String r9_pass, String ra_need,String rb_pdate,
			String rc_ndate,String rd_ufee,String re_bfee,String hmac) {
		ErrorInfo error = new ErrorInfo();
		t_dict_payment_gateways jing = User.gateway(Constants.JING_GATEWAY, error);
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		String info="";
		int isSuccess=0;
		//验证签名
		boolean bool=Payment.jingPayCBValidIng(r0_cmd, r1_code, r2_custom, r3_order, r4_account, r5_amount, r6_currency, r7_product, r8_ext, r9_pass, ra_need,Constants.JING_HMAC_KEY);
		if(bool){
			try {
				obj = t_user_recharge_details.find(sql, r3_order).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				error.msg = "根据pay_number查询用户ID出现错误!";
				return ;
			}
			if(obj == null){
				error.code = -1;
				error.msg = "根据pay_number查询用户ID出现错误!";
				
				return ;
			}
			User.recharge(r3_order, Double.parseDouble(r5_amount), error);
			/**
			 * 向井通回调发送消息
			 */
			for (int i = 0; i <3; i++) {
				if(Jingtum.sendCallback()){
					break;
				}else{
					continue;
				}
			}
			
			info = "交易成功";
			isSuccess=1;
			render(info,isSuccess);
		}else{
			error.code=-1;
			error.msg="银行返回信息错误，请联系管理员";
			render(error,isSuccess);
		}
	}
	
	
	/**
	 *  yeepay 支付回调 异步
	 * @param hmac
	 * @param p1_MerId
	 * @param r0_Cmd
	 * @param r1_Code
	 * @param r2_TrxId
	 * @param r3_Amt
	 * @param r4_Cur
	 * @param r5_Pid
	 * @param r6_Order
	 * @param r7_Uid
	 * @param r8_MP
	 * @param r9_BType
	 * @param keyValue
	 */
	public static void yCallBackSys(String hmac, String p1_MerId,
			String r0_Cmd, String r1_Code, String r2_TrxId, String r3_Amt,
			String r4_Cur, String r5_Pid, String r6_Order, String r7_Uid,
			String r8_MP, String r9_BType, String keyValue){
		
	}
	
	/**
	 * 提现
	 */
	@SubmitCheck
	public static void withdrawal(){
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		String type = params.get("type");
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String beginTime = params.get("startDate");
		String endTime = params.get("endDate");
		
		double amount = User.queryRechargeIn(user.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		double withdrawalAmount = user.balance - amount;
		
		if (!Constants.IS_WITHDRAWAL_INNER) {
			withdrawalAmount -= ServiceFee.withdrawalFee(withdrawalAmount);
		}
		
		//最多提现金额上限
		double maxWithDrawalAmount = Constants.MAX_VALUE;
		
		if(withdrawalAmount < 0) {
			withdrawalAmount = 0;
		}
		
		List<UserBankAccounts> banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		
		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
				beginTime, endTime, currPage, pageSize, error);
		boolean ipsEnable = Constants.IPS_ENABLE;
		
		render(user, withdrawalAmount, maxWithDrawalAmount, banks, page, ipsEnable);
	}
	
	/**
	 * 根据选择的银行卡id查询其信息
	 */
	public static void QueryBankInfo(long id){
		JSONObject json = new JSONObject();
		
		UserBankAccounts bank = new UserBankAccounts();
		bank.setId(id);
		
		json.put("bank", bank);
		
		renderJSON(json);
	}
	
	
//	/**
//	 * 提现记录
//	 */
//	public static void withdrawalRecord() {
//		User user = User.currUser();
//		
//		String type = params.get("type");
//		String currPage = params.get("currPage");
//		String pageSize = params.get("pageSize");
//		String beginTime = params.get("startDate");
//		String endTime = params.get("endDate");
//		
//		ErrorInfo error = new ErrorInfo();
//		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
//				beginTime, endTime, currPage, pageSize, error);
//		
//		render(page);
//	}
	
	//申请提现
	public static void applyWithdrawal(){
		render();
	}
	
	/**
	 * 确认提现
	 */
	@SubmitOnly
	public static void submitWithdrawal(double amount, long bankId, String payPassword, int type, String ipsSelect){
		ErrorInfo error = new ErrorInfo();
		boolean flag = false;
		
		if(StringUtils.isNotBlank(ipsSelect) && ipsSelect.equals("1")) {
			flag = true;
		}
		if(amount <= 0) {
			flash.error("请输入提现金额");
			error.code=-1;
			//withdrawal();
			render("@front.account.FundsManage.withdrawalResultHTML",error);
		}
		
		if(amount > Constants.MAX_VALUE) {
			flash.error("已超过最大充值金额" +Constants.MAX_VALUE+ "元");
			error.code=-1;
			//withdrawal();
			render("@front.account.FundsManage.withdrawalResultHTML",error);
		}
		
		if (!(Constants.IPS_ENABLE && flag)) {
			if(StringUtils.isBlank(payPassword)) {
				flash.error("请输入交易密码");
				error.code=-1;
				//withdrawal();
				render("@front.account.FundsManage.withdrawalResultHTML",error);
			}
			
			if(type !=1 && type != 2) {
				flash.error("传入参数有误");
				error.code=-1;
				//withdrawal();
				render("@front.account.FundsManage.withdrawalResultHTML",error);
			}
			
			if(bankId <= 0) {
				flash.error("请选择提现银行");
				error.code=-1;
				//withdrawal();
				render("@front.account.FundsManage.withdrawalResultHTML",error);
			}
		}
		
		User user = new User();
		user.id = User.currUser().id;
		long withdrawalId = user.withdrawal(amount, bankId, payPassword, type, flag, error);
		if(Constants.IPS_ENABLE && flag) {//Ips提现返回页未处理
			if(error.code < 0) {
				flash.error(error.msg);
				
				withdrawal();
			}
			
			Map<String, String> args= Payment.doDwTrade(withdrawalId, amount, error);
			
			if (error.code < 0) {
				flash.error(error.msg);
				
				withdrawal();
			}
			
			render("@front.account.PaymentAction.doDwTrade", args);
		}
		
		flash.error(error.msg);
		//withdrawal();
		render("@front.account.FundsManage.withdrawalResultHTML",error);
	}
	//提现结果静态页面跳转
	public static void withdrawalResultHTML(){
		render();
	}
	//转账
	public static void transfer(){
		render();
	}
	
	//确认转账
	public static void submitTransfer(){
		render();
	}
	
	/**
	 * 交易记录
	 */
	public static void dealRecord(int type, String beginTime, String endTime, int currPage, int pageSize){
		User user = User.currUser();
		//	账户信息预览
		UserOZ warmPrompt = new UserOZ(user.id);
		PageBean<v_user_deals_record> page = User.queryUserDealRecord(user.id, type, beginTime, endTime,currPage, pageSize);
		render(warmPrompt,page);
	}
	
	//交易详情
	public static void dealDetails(){
		render();
	}
	
	/**
	 * 导出交易记录
	 */
	public static void exportDealRecords(){
		ErrorInfo error = new ErrorInfo();
		
    	List<v_user_deals_record> details = User.queryAllDealRecord(error);
    	
    	if (error.code < 0) {
			renderText("下载数据失败");
		}
    	
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	JSONArray arrDetails = JSONArray.fromObject(details, jsonConfig);
    	
    	for (Object obj : arrDetails) {
			JSONObject detail = (JSONObject)obj;
			int type = detail.getInt("type");
			double amount = detail.getDouble("amount");
			String name=detail.getString("name");
			detail.put("type", name);
			switch (type) {
			case 1:
				detail.put("type", name);
				detail.put("inAmount", amount);
				detail.put("outAmount", "");
				break;
			case 2:
				detail.put("type", name);
				detail.put("inAmount", "");
				detail.put("outAmount", amount);
				break;
			case 3:
				detail.put("type", name);
				detail.put("inAmount", "");
				detail.put("outAmount", amount);
			case 4:
				detail.put("type", name);
				detail.put("inAmount", "");
				detail.put("outAmount", amount);
			case 5:
				detail.put("type", name);
				detail.put("inAmount", "");
				detail.put("outAmount", amount);
			default:
				detail.put("type", name);
				detail.put("inAmount", "");
				detail.put("outAmount", "");
				break;
			}
		}
    	
    	File file = ExcelUtils.export(
    			"交易记录", 
    			arrDetails,
				new String[] {"时间","交易类型", "收入", "支出", "账户总额", "可用余额"}, 
				new String[] {"time", "type","inAmount", "outAmount", "user_balance", "balance"});
    	
    	renderBinary(file, "交易记录.xls");
	}
	
	/**
	 * 资金托管测试页面
	 */
	public static void payment() {
		render();
	}
	
	/**
	 * 我的支付账号
	 */
	public static void payAccount() {
		String src = Constants.HTTP_PATH;
		
		if (-1 == IPSConstants.CurrentGateWay || 0 == IPSConstants.CurrentGateWay) {
			
			src += "/public/images/s_linklogo.png";
			
			render(src);
		}
		
		switch (IPSConstants.CurrentGateWay) {
		case IPSConstants.GATE_WAY_GUO:
			src += "/public/images/gfb_logo.png";
			break;
			
		case IPSConstants.GATE_WAY_IPS:
			src += "/public/images/s_linklogo.png";
			break;
			
		case IPSConstants.GATE_WAY_LOAN:
			src += "/public/images/sq_logo.png";
			break;
			
		case IPSConstants.GATE_WAY_PNR:
			src += "/public/images/hf_logo.png";
			break;
			
		case IPSConstants.GATE_WAY_YEE:
			src += "/public/images/yb_logo.png";
			break;
			
		default:
			src += "/public/images/s_linklogo.png";
			break;
		}
		
		render(src);
	}
	
	/**
	 * 支付账号登陆
	 */
	public static void loginAccount() {
		
		Map<String, String> args = Payment.loginAccount();
		
		
		render("@front.account.PaymentAction.loginAccount", args);
	}
	
	/**
	 * 查看(异步)
	 */
	public static void showitem(String mark, String signUserId){
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1){
			renderText(error.msg);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = userId;
		item.mark = mark;
		
		render(item);
	}
}
