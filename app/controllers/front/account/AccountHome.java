package controllers.front.account;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_bills;
import models.t_content_news;
import models.t_dict_audit_items;
import models.t_products;
import models.t_user_events;
import models.v_bid_auditing;
import models.v_bid_fundraiseing;
import models.v_bid_not_through;
import models.v_bid_release_funds;
import models.v_bid_repayment;
import models.v_bid_repaymenting;
import models.v_bid_wait_verify;
import models.v_bids;
import models.v_bill_detail;
import models.v_bill_loan;
import models.v_bill_recently_pending;
import models.v_bill_repayment_record;
import models.v_front_all_debts;
import models.v_invest_records;
import models.v_user_account_statistics;
import models.v_user_attention_info;
import models.v_user_audit_items;
import models.v_user_for_details;
import models.v_user_for_personal;
import models.v_product_audit_items;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.FileUtil;
import utils.JsonDateValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import bean.QualityBid;
import business.AuditItem;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.CreditLevel;
import business.Debt;
import business.Invest;
import business.News;
import business.Optimization.BidOZ;
import business.Optimization.UserOZ;
import business.OverBorrow;
import business.Payment;
import business.StationLetter;
import business.User;
import business.UserAuditItem;
import business.Vip;
import constants.Constants;
import constants.Constants.PayType;
import constants.Constants.RechargeType;
import constants.IPSConstants;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.SubmitCheck;
import controllers.SubmitOnly;
import controllers.SubmitRepeat;
import controllers.interceptor.FInterceptor;

/**
 * 
 * @author cp
 *
 */
@With({FInterceptor.class, SubmitRepeat.class})
public class AccountHome extends BaseController {
	/**
	 * 我的账户的首页
	 */
	public static void home() {
		User user = User.currUser();
		
		ErrorInfo error = new ErrorInfo();
		
		int unReadMsgCount = StationLetter.queryUserUnreadMsgsCount(user.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.BORROWING_TECHNIQUES, 3, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }
		
		if (Constants.IPS_ENABLE && user.getIpsStatus() != IpsCheckStatus.IPS) {
			CheckAction.approve();
		}
		
		UserOZ warmPrompt = new UserOZ(user.id);
//        warmPrompt.userId = user.id;
        
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
        
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		
		int infoCompletion = calInfoCompletion(user);
		int safeLevel = calSafeLevel(user);
		render(user, warmPrompt,unReadMsgCount,news,backstageSet, content, creditLevels, infoCompletion, safeLevel);
	}
	
	private static int calInfoCompletion(User user) {
		int completion = 0;
		
		if (StringUtils.isNotBlank(user.realityName)) {
			completion += 10;
		}
		if (StringUtils.isNotBlank(user.mobile) && user.isMobileVerified) {
			completion += 10;
		}
		if (StringUtils.isNotBlank(user.email) && user.isEmailVerified) {
			completion += 10;
		}
		if (user.educationId > 0) {
			completion += 10;
		}
		if (StringUtils.isNotBlank(user.graduate)) {
			completion += 10;
		}
		if (user.maritalId > 0) {
			completion += 10;
		}
		if (StringUtils.isNotBlank(user.familyAddress)) {
			completion += 10;
		}
		if (user.houseId > 0) {
			completion += 10;
		}
		if (user.industry > 0) {
			completion += 10;
		}
		if (user.income > 0) {
			completion += 10;
		}
		
		return completion;
	}
	
	private static int calSafeLevel(User user) {
		int safeLevel = 0;
		
		if (StringUtils.isNotBlank(user.realityName)) {
			safeLevel += 30;
		}
		if (StringUtils.isNotBlank(user.mobile) && user.isMobileVerified) {
			safeLevel += 30;
		}
		if (StringUtils.isNotBlank(user.email) && user.isEmailVerified) {
			safeLevel += 20;
		}
		if (StringUtils.isNotBlank(user.payPassword)) {
			safeLevel += 20;
		}
		
		return safeLevel;
	}
	
	/**
	 * 申请超额借款页面
	 */
	public static void applyForOverBorrowInit() {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if (OverBorrow.haveAuditingOverBorrow(user.id, error) && 0 == error.code) {
			error.code = -1;
			error.msg = "您还有未审核的超额借款申请，不能再次申请";
			
			renderJSON(error);
		}
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render();
	}
	
	/**
	 * 申请超额借款
	 * @param amount
	 * @param reason
	 * @param jsonAuditItems
	 */
	public static void applyForOverBorrow(int amount, String reason, String jsonAuditItems) {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		JSONArray jsonArray = JSONArray.fromObject(jsonAuditItems);
		List<Map<String,String>> auditItems = (List)jsonArray;
		
		new OverBorrow().applyFor(user, amount, reason, auditItems, error);
		
		renderJSON(error);
	}
	
	/**
	 * 选择超额借款审核资料页面
	 */
	public static void selectAuditItemsInit() {
		User user = User.currUser();
		long userId = user.id;
		ErrorInfo error = new ErrorInfo();
		
		List<AuditItem> auditItems = UserAuditItem.queryAuditItemsOfOverBorrow(userId, error);
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(auditItems);
	}
	
	/**
	 * 跳转到上传资料页
	 */
	public static void auditMaterialsUpload(String sign, String signItemId,
			String name, String description, String type, String status,String productId) {
		render(sign, signItemId, name, description, type, status,productId);
	}
	
	/**
	 * 跳转到上传成功页
	 */
	public static void uploadSuccess(String productId) {
		String status = "1";
		render("@front.account.AccountHome.auditMaterialsUpload", null, null, null, null, null, status,productId);
	}
	
	/**
	 * 提交审核资料页面
	 */
	public static void submitAuditItemInit(String mark) {
		t_dict_audit_items auditItem = null;
		
		try {
			auditItem = t_dict_audit_items.find("mark = ?", mark).first();
		} catch (Exception e) {
			Logger.error("查询资料:" + e.getMessage());
			
			renderText("数据库异常");
		}
		 
		render(auditItem);
	}
	
	/**
	 * 申请超额借款页面ips
	 */
	public static void applyForOverBorrowWin() {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if (OverBorrow.haveAuditingOverBorrow(user.id, error) && 0 == error.code) {
			error.code = -1;
			error.msg = "您还有未审核的超额借款申请，不能再次申请";
			
			renderJSON(error);
		}
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render();
	}
	
	/**
	 * 选择超额借款审核资料页面ips
	 */
	public static void selectAuditItemsWin() {
		User user = User.currUser();
		long userId = user.id;
		ErrorInfo error = new ErrorInfo();
		
		List<AuditItem> auditItems = UserAuditItem.queryAuditItemsOfOverBorrow(userId, error);
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(auditItems);
	}
	
	/**
	 * 提交审核资料页面ips
	 */
	public static void submitAuditItemWin(String mark) {
		t_dict_audit_items auditItem = null;
		
		try {
			auditItem = t_dict_audit_items.find("mark = ?", mark).first();
		} catch (Exception e) {
			Logger.error("查询资料:" + e.getMessage());
			
			renderText("数据库异常");
		}
		
		render(auditItem);
	}
	
	/**
	 * 优质标推荐
	 */
	public static void queryQualityBids() {
		ErrorInfo error = new ErrorInfo();
		int size = Constants.QUALITY_BID_COUNT;
		List<QualityBid> qualityBids= BidOZ.queryQualityBid(size, error);
		
		render(qualityBids);
	}
	
	/**
	 * 优质债权推荐
	 */
	public static void queryQualityDebts() {
		
		ErrorInfo error = new ErrorInfo();
		List<v_front_all_debts> qualityBids= Debt.queryQualityDebtTransfers(error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		     
		render(qualityBids);
	}
	
	/**
	 * 我关注的人
	 */
	public static void myAttentionUser(int currPage, int pageSize) {
		User user = User.currUser();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_attention_info> page= User.queryAttentionUsers(user.id, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(page,user);
	}
	
	/**
	 * 我关注的人--修改备注名跳转页面
	 */
	public static void attentionUserNote(long id){
		render(id);
	}
	
	/**
	 * 我关注的人--修改发送站内信跳转页面
	 */
	public static void attentionUsersStation(String sign, String userName){
		render(sign, userName);
	}
	
	/**
	 * 我关注的人(AJAX)
	 */
	public static void AttentionUsers(String sign, int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		User user = User.currUser();
		
		if(userId < 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<v_user_attention_info> page= User.queryAttentionUsers(userId, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}
	
	/**
	 * 借款列表(AJAX)
	 */
	public static void loanList(String sign) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(error);
		}
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		if(userId < 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<v_bids> page= Bid.queryBidByUser(userId, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}
	
	/**
	 * 最新动态(AJAX)
	 */
	public static void currentEvents(String sign) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(error);
		}
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		if(userId < 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<t_user_events> page = User.queryUserEvnets(userId, error, currPage, pageSize);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}
	
	/**
	 * 投资记录(AJAX)
	 */
	public static void investRecords(String sign) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(error);
		}
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		if(userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<v_invest_records> page = Invest.queryInvestRecords(userId, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}

	
	/**
	 * 修改备注名
	 */
	public static void setNoteName(long id, String noteName) {
		ErrorInfo error = new ErrorInfo();
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		User.updateAttentionUser(id, noteName, error);
		
		JSONObject json = new JSONObject();
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 上传图像
	 */
	public static void uploadPhoto(File imgFile) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> fileInfo = FileUtil.uploadFile(imgFile, 1, error);
		
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		String filename = (String) fileInfo.get("fileName");
		//String filename = fileExt.substring(0, fileExt.lastIndexOf("."));
		
		User user = new User();
		user.id = User.currUser().id;
		user.photo = filename;
		user.editPhoto(error);
		
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		JSONObject json = new JSONObject();
		json.put("filename", Constants.BASE_URL+filename);
		json.put("error", error);
		
		renderText(json.toString());
	}
	
	/**
	 * 关注用户
	 */
	public static void attentionUser(String sign) {
		
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		
		long attentionUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		if(attentionUserId <= 0) {
			error.code = -1;
			error.msg = "传入数据有误";
			
			json.put("error", error.msg);
			
			renderJSON(json);
		}
		
		if(attentionUserId == user.id){
			
			error.msg = "您不能关注自己";
			json.put("error", error.msg);
			
			renderJSON(json);
		}
		
		User.attentionUser(user.id, attentionUserId, error);
		
		json.put("error", error.msg);
		
		renderJSON(json);
	}
	
	/**
	 * 取消关注用户
	 * @param id 关注表中的id主键
	 */
	public static void cancelAttentionUser(long id) {
		ErrorInfo error = new ErrorInfo();
		/*long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}*/
		
		JSONObject json = new JSONObject();
		
		if(id <= 0) {
			error.code = -1;
			error.msg = "传入数据有误";
			
			json.put("error", error.msg);
			
			renderJSON(json);
		}
		
		User.cancelAttentionUser(id, error);
		
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 用户详情
	 * @param userId
	 */
	public static void userDetail(String sign) {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(user, error);
		}
		
		if(userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(user, error);
		}
		
		v_user_for_personal userInfo = User.queryUserInformation(userId, error);
		
		if(error.code < 0) {
			render(user, error);
		}
		
		render(user, userInfo);
	}
	
	/**
	 * 根据申请会员时间，计算所需要的钱
	 * @param time
	 */
	public static void vipMoney(String time) {
		int serviceTime = Integer.parseInt(time);
		Vip vip = new Vip();
		vip.serviceTime = serviceTime;
		
		ErrorInfo error = new ErrorInfo();
		double amount = vip.vipMoney(error);
		
		JSONObject json = new JSONObject();
		//json.put("error", error);
		json.put("amount", amount);
		
		renderJSON(json);
	}
	
	/**
	 * 申请会员
	 */
	public static void vipApply(Integer serviceTime, Integer type) {
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(serviceTime == null) {
			error.code = -1;
			error.msg = "请输入申请时间";
			
			json.put("error", error);
		}
		
		if(user.vipStatus && (1 != type)) {
			error.code = -1;
			error.msg = "您已经是VIP了!";
			
			json.put("error", error);
			
			renderJSON(json);	
		}
		
		Vip vip = new Vip();
		vip.serviceTime = serviceTime;
		vip.renewal(user, error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
		}
		
		json.put("error", error);
		
		renderJSON(json);	
		
	}
	
	/**
	 * 发送站内信
	 * @param receiverUserId
	 * @param title
	 * @param content
	 */
	public static void sendMessage(String sign, String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(StringUtils.isBlank(title)) {
			error.msg = "标题不能为空";
			json.put("error", error);
			renderJSON(json);
		}
		
		long receiverUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			error.msg = "非法请求";
			json.put("error", error);
			renderJSON(json);
		}
		
		User user = User.currUser();
		
		if(receiverUserId == user.id){
			error.msg = "您不能给自己发送站内信";
			json.put("error", error);
			renderJSON(json);
		}
		
		StationLetter message = new StationLetter();
		
		message.senderUserId = user.id;
		message.receiverUserId = receiverUserId;
		message.title = title;
		message.content = content;
		
		message.sendToUserByUser(error); 
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	
	
	/**
	 * 我的站内信
	 */
	public static void myMessages() {
		ErrorInfo error = new ErrorInfo();
		int unreadSystemMsgCount = StationLetter.queryUserUnreadSystemMsgsCount(1, error);
		int unreadInboxMsgCount = StationLetter.queryUserUnreadInboxMsgsCount(1, error);
		render(unreadSystemMsgCount,unreadInboxMsgCount);
	}
	
	//-----------------------------------借款子账户--------------------------------------
	
	/**
	 * 借款子账户首页
	 */
	public static void loanAccount(){
		User user = User.currUser();
		
		ErrorInfo error = new ErrorInfo();
		v_user_account_statistics accountStatistics = User.queryAccountStatistics(user.id, error);
		
		if(error.code < 0) 
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.BORROWING_TECHNIQUES, 3, error);
		
		if(error.code < 0) 
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		/* 最新筹款中满标倒计时提醒 */
		List<v_bid_fundraiseing> fundraiseingBid = Bid.queryFundraiseingBid(user.id, error);
		
		/* 账单提醒 */
		List<v_bill_recently_pending> recentlyRepayBills = Bill.queryRecentlyBills(error);
		
		if(null == fundraiseingBid)
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		/* 最新欠缺资料的借款标 */
		List<v_bids> toSubmitItemBid = Bid.queryToSubmitItemBid(user.id, error);
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		if(null == toSubmitItemBid)
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		render(user, accountStatistics, news, fundraiseingBid, toSubmitItemBid, recentlyRepayBills,backstageSet, content);
	}
	
	/**
	 * 我的借款账单
	 */

	public static void myLoanBills(int payType, int isOverType, int keyType, String key, int currPage,String sign){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		PageBean<v_bill_loan> page = Bill.queryMyLoanBills(user.id, bidId,payType, isOverType, keyType, key, currPage, 0, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(page);
	}
	/*
	 * 借款管理-我的借款页面跳转
	 */
	public static void myLoans(){
		render();
	}
	/**
	 * 下载数据(我的借款账单)
	 */
	public static void exportLoanBills() {
		ErrorInfo error = new ErrorInfo();
		
    	List<v_bill_loan> bills = Bill.queryMyAllLoanBills(error);
    	
    	if (error.code < 0) {
			renderText("下载数据失败");
		}
    	
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	JSONArray arrBills = JSONArray.fromObject(bills, jsonConfig);
    	
    	for (Object obj : arrBills) {
			JSONObject bill = (JSONObject)obj;
			int isOverdue = bill.getInt("is_overdue");
			int status = bill.getInt("status");
			bill.put("is_overdue", (isOverdue == 0) ? "未逾期" : "逾期");
			bill.put("status", (status == -1 || status == -2) ? "未还款" : "已还款");
		}
    	
    	File file = ExcelUtils.export(
    			"我的借款账单", 
    			arrBills,
				new String[] {"账单标题", "本期需还款金额", "是否逾期", "还款状态", "到期还款时间", "实际还款时间"}, 
				new String[] {"title", "repayment_amount", "is_overdue", "status", "repayment_time", "real_repayment_time"});
    	
    	renderBinary(file, System.currentTimeMillis() + ".xls");
    }
	
	/**
	 * 从理财收款标跳转到借款账单详情
	 * @param sign
	 */
	public static void toInvestBill(String sign){
		
        ErrorInfo error = new ErrorInfo();
		
		long investId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Long billId = Invest.queryBillByInvestId(investId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		String id = Security.addSign(billId, Constants.BILL_ID_SIGN);
		
		loanBidDetails(id, 1);
	}
	
	/**
	 *理财收款标借款账单详情
	 */
	public static void loanBidDetails(String billId, int currPage) { 
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		User user = User.currUser();
		
		v_bill_detail billDetail = Bill.queryBillDetails(id, user.id, error);
		
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(billDetail, page, backSet, user);
	}
	
	/**
	 * 我的借款账单详情
	 */
	@SubmitCheck
	public static void loanBillDetails(String billId, int currPage) { 
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		boolean isDealPassword = false;
		try {
			isDealPassword = t_products.find("select is_deal_password from t_products where id = (select product_id from t_bids where id = (select bid_id from t_bills where id = ?))", id).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		v_bill_detail billDetail = Bill.queryBillDetails(id, user.id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		int checkPeriod = Bill.checkPeriod(billDetail.bid_id, billDetail.current_period);
		
		render(billDetail, page, backSet, user, isDealPassword, checkPeriod);
	}
	
	/**
	 * 我的借款账单详情
	 */
	@SubmitCheck
	public static void loanBillDetailByBid(String bidId, int currPage) { 
		ErrorInfo error = new ErrorInfo();
		int type=0;
		Bid bid = new Bid();
		bid.bidDetail = true;
		long b_id = Security.checkSign(bidId, Constants.BID_ID_SIGN, 3600, error);
		
		bid.id = b_id;
		t_bills  bill= t_bills.find(" status in(-1,-2) and bid_id=? ", b_id).first();;
		if(bill==null){//如果账单都换完，查询最后一期账单展示
			bill= t_bills.find(" bid_id=? order by id desc", b_id).first();
		}

		boolean isDealPassword = false;
		try {
			isDealPassword = t_products.find("select is_deal_password from t_products where id = (select product_id from t_bids where id = (select bid_id from t_bills where id = ?))", bill.id).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		
		v_bill_detail billDetail = Bill.queryBillDetails(bill.id, user.id, error);
		Double  outstand_amount_sum=Bill.queryBillOutstand_amount_sum(billDetail.bid_id+"", billDetail.user_id+"", error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(billDetail.user_id, bid.mark);
		int checkPeriod = Bill.checkPeriod(billDetail.bid_id, billDetail.current_period);
		if(billDetail.loan_periods==billDetail.has_payed_periods){
			type=1;
		}
		render(billDetail, page, backSet, user, isDealPassword, checkPeriod,uItems,bid,type,outstand_amount_sum);
	}
	// 还款
	public static void repayment(long billId){
		
		
		render();
	}
	
	/**
	 * 确认还款
	 */
	@SubmitOnly
	public static void submitRepayment(String payPassword, double amount, String bidId,String billId){
		ErrorInfo error = new ErrorInfo();
		long bill_id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			loanBillDetailByBid(bidId, 1);
		}
		
		User user = User.currUser();
		
		boolean isDealPassword = false;
		
		try {
			
			isDealPassword = t_products.find("select is_deal_password from t_products where id = (select product_id from t_bids where id = (select bid_id from t_bills where id = ?))", bill_id).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "还款失败";
			flash.error(error.msg);
			
			loanBillDetailByBid(bidId, 1);
		}
		
		if (isDealPassword) {
			int code = user.verifyPayPassword(payPassword, error);
			if(code < 0){
				flash.error(error.msg);
				loanBillDetailByBid(bidId, 1);
			}
		}
		
		Bill bill = new Bill();
		bill.setId(bill_id);
		/* 2014-12-29 限制还款需要从第一期逐步开始还款 */
		if(bill.checkPeriod(bill.bidId, bill.periods) > 0){
			flash.error("请您从第一期逐次还款!");
			loanBillDetailByBid(bidId, 1);
		}
		
		/*本金垫付还款*/
		if (Constants.IPS_ENABLE && bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			String pMerBillNo = Bill.getRepaymentBillNo(error, bill_id);
			Map<String,String> args = Payment.compensateRepayment(pMerBillNo, bill_id, error);
			if(error.code == 100) {
				render("front/account/PaymentAction/loan.html" ,args);
			}
			flash.error(error.msg);
			loanBillDetailByBid(bidId, 1);
		}
		
		Map<String, List<Map<String, Object>>> mapList = bill.repayment(user.id, error);
		/* 余额不足则提示跳转去充值页面，故此不能把错误值放入flash.error */
		if(error.code == Constants.BALANCE_NOT_ENOUGH) {
			flash.put("notEnough", -999);
			loanBillDetailByBid(bidId, 1);
		}
		
		if(error.code < 0){
			flash.error(error.msg);
			loanBillDetailByBid(bidId, 1);
		}
		
		if(Constants.IPS_ENABLE) {
			Map<String, String> args = Payment.repaymentNewTrade(mapList, bill_id, error);
			
			if(error.code < 0){
				flash.error(error.msg);
				loanBillDetailByBid(bidId, 1);
			}
			
			render("@front.account.PaymentAction.repaymentNewTrade", args);
		}
		
		flash.error("还款成功");
		loanBillDetailByBid(bidId, 1);
	}
	
	/**
	 * 待验证的借款标列表
	 */
	public static void waitVerifyBids(String currPage, String pageSize, String condition, String keyword){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_wait_verify> pageBean = new PageBean<v_bid_wait_verify>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		
		pageBean.page = Bid.queryBidWaitVerify(pageBean, error, userId, condition, keyword, "", "", "");

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_FRONT);  

		render(pageBean);
	}
	
	/**
	 * 审核中的借款标列表(包括审核中的借款和井通审核中的借款)
	 */
	public static void auditingLoanBids(String currPage, String pageSize, String condition, String keyword) {
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_auditing> pageBean = new PageBean<v_bid_auditing>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		
		pageBean.page = Bid.queryBidAuditingToFront (pageBean, error, userId, condition, keyword, "", "", "");

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_FRONT);  

		render(pageBean);
	}
	
	/**
	 * 等待满标的借款标列表
	 */
	public static void loaningBids(String currPage, String pageSize, String condition, String keyword){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidFundraiseing(pageBean, -1, error, userId, condition, keyword, "", "", "");
		
		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_FRONT);  

		render(pageBean);
	}
	
	/**
	 * 待放款的借款标列表
	 */
	public static void readyReleaseBid(String currPage, String pageSize, String condition, String keyword){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_release_funds> pageBean = new PageBean<v_bid_release_funds>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryReleaseFunds(0, pageBean, Constants.BID_EAIT_LOAN, error, userId, condition, keyword, "", "", "");
		
		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_FRONT);  

		render(pageBean);
	}
	
	/**
	 * 借款标详情
	 */
	public static void bidDetail(String bidId,int currPage){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		long  bid_Id = Security.checkSign(bidId, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
	//	long b_id = Security.checkSign(bidId, Constants.BID_ID_SIGN, 3600, error);
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bid_Id;
		
		/* 如果非当前用户操作 */
		if(bid.userId  != User.currUser().id)
			render(Constants.ERROR_PAGE_PATH_FRONT); 
			
		v_bill_detail billDetail = Bill.queryBillDetails(bid_Id, user.id, error);
		//历史还款情况
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(bid_Id, currPage, 0, error);
		Map<String, String> historySituationMap = User.historySituation(bid.userId, error);// 借款者历史记录情况
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bid.userId, bid.mark); // 用户正对产品上传的资料集合
		//List<UserBankAccounts> banks = null;
		//if(Constants.BID_ADVANCE_LOAN == bid.status || Constants.BID_FUNDRAISE == bid.status)
		//	banks = UserBankAccounts.queryUserAllBankAccount(User.currUser().id); // 用户银行卡列表
		render(bid,billDetail, uItems,page,historySituationMap);
	}
	
	/**
	 * 投标记录
	 */
	public static void bidRecord(int currPage, long bidId) {
		if(0 == bidId) 
			render();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.PAGE_SIZE;
		pageBean.page = Invest.bidInvestRecord(pageBean, bidId, error);
		
		render(pageBean);
	}
	
	/**
	 * 查看资料
	 */
	public static void showitem(String mark){
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.mark = mark;
		
		render(item);
	}
	
	
	/**
	 * 审核中->撤销
	 */
	public static void auditToRepeal(String sign){
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingLoanBids("", "", "", "");
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		if(bid.userId != User.currUser().id)
			return;
		
		bid.auditToRepeal(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg);
		
		auditingLoanBids("", "", "", "");
	}
	
	/**
	 * 提前借款->撤销
	 */
	public static void advanceLoanToRepeal(String sign){
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			loaningBids("", "", "", "");
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		if(bid.userId != User.currUser().id)
			return;
		
		bid.advanceLoanToRepeal(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_F, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg);
		
		loaningBids("", "", "", "");
	}
	
	/**
	 * 筹款中->撤销
	 */
	public static void fundraiseToRepeal(String sign){
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			loaningBids("", "", "", "");
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		if(bid.userId != User.currUser().id)
			return;
		
		bid.fundraiseToRepeal(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_N, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg);
		
		loaningBids("", "", "", "");
	}
	
	/**
	 * 还款中的借款标列表
	 */
	public static void repaymentBids(){
		ErrorInfo error = new ErrorInfo();
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String condition = params.get("condition");
		String keyword = params.get("keyword");
		String userid = User.currUser().id + "";
		
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidRepaymenting(0, pageBean, 0, error, userid, condition, keyword, "", "", "");
		render(pageBean);
	}
	
	//提前还款
	public static void prepayment(){
		render();
	}
	
	//确认提前还款
	public static void submitPrepayment(){
		render();
	}
	
	/**
	 * 已成功的借款标列表
	 */
	public static void successBids(){
		ErrorInfo error = new ErrorInfo();

		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String condition = params.get("condition");
		String keyword = params.get("keyword");
		String userid = User.currUser().id + "";
		
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error, userid, condition, keyword, "", "", "");

		render(pageBean);
	}
	
	/**
	 * 失败的借款标列表
	 */
	public static void failBids() {
		ErrorInfo error = new ErrorInfo();

		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String condition = params.get("condition");
		String keyword = params.get("keyword");
		String userid = User.currUser().id + "";
		
		PageBean<v_bid_not_through> pageBean = new PageBean<v_bid_not_through>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidNotThrough(0, pageBean, error, userid, condition, keyword, "", "", "");
			
		render(pageBean);
	}
	
	/**
	 * 审核资料认证
	 */
	@Deprecated
	public static void auditMaterials(String currPage, String pageSize,
			String status, String startDate, String endDate, String productId,
			String productType) {
		return ;
	}
	
	/**
	 * 审核资料认证
	 * @param currPage
	 * @param pageSize
	 * @param status
	 * @param startDate
	 * @param endDate
	 * @param productId
	 * @param productType
	 */
	public static void auditMaterialsIPS(String currPage, String pageSize,
			String status, String startDate, String endDate, String productId,
			String productType) {
		ErrorInfo error = new ErrorInfo();
		long userId = User.currUser().id;
		
		/* 产品列表 */
//		List<Product> products = Product.queryProductNames(true, error);
		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem("1", "100", userId, error, status, "", "", productId, "1");
		PageBean<v_user_audit_items> nonNessaryBean = UserAuditItem.queryUserAuditItem("1", "100", userId, error, status, "", "",productId, "0");
		productId=productId==null?"":productId;
		render(pageBean, nonNessaryBean,productId);
	}
	
	/**
	 * 借款管理-借款申请查询-提交资料-审核资料列表(过时方法，转为之前的auditMaterialsIPS)
	 * @param currPage
	 * @param pageSize
	 * @param status
	 * @param startDate
	 * @param endDate
	 * @param productId
	 * @param productType
	 */
	@Deprecated
	public static void auditMaterialsIPSList(Integer productId,String status) {
		ErrorInfo error = new ErrorInfo();
		long userId = User.currUser().id;
		User user=User.currUser();
		List<v_product_audit_items> nessary= new ArrayList<v_product_audit_items>();
		List<v_product_audit_items> nonNessary= new ArrayList<v_product_audit_items>();
		
		List<v_product_audit_items> productList= UserAuditItem.queryProductAuditItem(productId,1, error);
		List<v_product_audit_items> nonNessaryProduct= UserAuditItem.queryProductAuditItem(productId,0, error);
		PageBean<v_user_audit_items> userAuditsPageBean = UserAuditItem.queryUserAuditItem("1","100",userId,error, status,"","",productId+"","");
		
		System.out.println("size:"+userAuditsPageBean.page.size()+"=="+productList.size()+"---"+nonNessaryProduct.size());
		v_product_audit_items productItems=null;
		v_user_audit_items userItems=null;
		
		/*for (int i = 0; i <productList.size(); i++) {
			productItems=productList.get(i);
			for (int j = 0; j < userAuditsPageBean.page.size(); j++) {
				userItems=userAuditsPageBean.page.get(j);
				if(status==null|| "10".equals(status)){
					if(userItems.audit_item_id==productItems.audit_item_id){
						productItems.user_id=userItems.user_id;
						productItems.status=userItems.status;
					}
				}
			}
			nessary.add(productItems);
		}
		for (int i = 0; i <nonNessaryProduct.size(); i++) {
			productItems=nonNessaryProduct.get(i);
			for (int j = 0; j < userAuditsPageBean.page.size(); j++) {
				userItems=userAuditsPageBean.page.get(j);
				if(status==null|| "10".equals(status)){
					if(userItems.audit_item_id==productItems.audit_item_id){
						productItems.description=userItems.description;
						productItems.user_id=userItems.user_id;
						productItems.status=userItems.status;
					}
				}
			}
			nonNessary.add(productItems);
			System.out.println("fffffff;"+nonNessary.size()+"ddd:"+nessary.size());
		}*/
		render(nessary, nonNessary,userAuditsPageBean,productId,status,user);
	}
	
	/**
	 * 查看审核资料
	 */
	public static void auditMaterialsSameItem(String mark){
		long userId = User.currUser().id;
		
		UserAuditItem item = new UserAuditItem();
		item.userId = userId;
		item.mark = mark;
		
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> items = UserAuditItem.querySameAuditItem(userId, item.auditItemId, error);
		
		if(null == items) {
			flash.error(error.msg);
			
//			auditMaterials(null, null, null, null, null, null, null);
		}
		render(item, items);
	}
	
	/**
	 * 删除用户资料
	 */
	@Deprecated
	public static void deleteAuditItem(String sign){
		return ;
	}
	
	/**
	 * 提交资料(异步)
	 */  
	@Deprecated
	public static void createUserAuditItem(String sign, String signItemId, String items, double size){
		return ;
	}
	
	/**
	 * 上传资料(资金托管-异步)
	 */  
	public static void createUserAuditItemIPS(String sign, String signItemId, String items, double size){
		ErrorInfo error = new ErrorInfo();
		long userItemId = Security.checkSign(sign, Constants.USER_ITEM_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userItemId < 1)
			renderJSON(error);
		
		long itemId = Security.checkSign(signItemId, Constants.ITEM_ID_SIGN, Constants.VALID_TIME, error);
		
		if(itemId < 1)
			renderJSON(error);
		
		if(StringUtils.isBlank(items)){
			error.msg = "数据有误!";
			
			renderJSON(error);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.id = userItemId;

		if(item.id < 1) {
			error.msg = "资料标示项已过期，请刷新页面!";
			
			renderJSON(error);
		}
		
		item.imageFileNames = items;
		item.createUserAuditItem(error);
		
		JSONObject json = new JSONObject();
		json.put("msg", error.msg);
		json.put("status", item.status);
		json.put("time", DateUtil.dateToString1(item.time));
		
		renderJSON(json);
	}
	
	/**
	 * 查询用户上传未付款的资料信息
	 */
	public static void queryUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> map = UserAuditItem.queryUploadItems(User.currUser().id, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		if (map == null || (Long) map.get("count") == 0) {
			error.code = -1;
			error.msg = "请先上传资料再提交";
			
			renderJSON(error);
		}
		
		map.put("code", 0);
		
		renderJSON(map);
	}
	
	/**
	 * 提交已上传的资料
	 */
	public static void submitUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> info = UserAuditItem.queryUploadItems(User.currUser().id, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			auditMaterialsIPS("1", "10", "", "", "", "", "");
		}
		
		/* 2014-11-18把单个提交修改为组提交 */
		double balance = 0;
		double fees = (Double) info.get("fees");
		User user = User.currUser();
		v_user_for_details details = user.balanceDetail;
		
		if(null == details) {
			flash.error("查询用户资金出现错误!");
			
			auditMaterialsIPS("1", "10", "", "", "", "", "");
		}
		
		if(Constants.IPS_ENABLE){
			switch (Constants.PAY_TYPE_ITEM) {
			//平台内部进行转账
			case PayType.INNER:
				flash.error("资金托管模式下，不能以平台内部进行转账的方式支付资料审核费");

				auditMaterialsIPS("1", "10", "", "", "", "", "");
				//通过独立普通网关
			case PayType.INDEPENDENT:
				balance = details.user_amount2;
				
				if(fees > balance){ // 钱不够
					error.code = Constants.BALANCE_NOT_ENOUGH;
					error.msg = "对不起，您可用余额不足";
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("rechargeType", RechargeType.UploadItems);
					map.put("fee", fees);
					Cache.set("rechargePay" + user.id, map, IPSConstants.CACHE_TIME);
					flash.put("code", Constants.BALANCE_NOT_ENOUGH);
					
					auditMaterialsIPS("1", "10", "", "", "", "", "");
				} else { // 钱够
					UserAuditItem.submitUploadedItems(user.id, balance, error);
					flash.error(error.msg);
					
					auditMaterialsIPS("1", "10", "", "", "", "", "");
				}
			//通过共享资金托管账户网关
			case PayType.SHARED:
			//资金托管网关
			case PayType.IPS:
				balance = details.user_amount;
				
				if (fees > balance) {
					flash.put("msg", "对不起，您可用余额不足支付资料审核费");
					flash.put("code", Constants.BALANCE_NOT_ENOUGH);
					
					auditMaterialsIPS("1", "10", "", "", "", "", "");
				}
				
				if (fees > 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map = new HashMap<String, Object>();
					map.put("rechargeType", RechargeType.UploadItems);
					map.put("fee", fees);
					Cache.set("rechargePay"+ user.id, map, IPSConstants.CACHE_TIME);
					flash.put("code", Constants.BALANCE_PAY_ENOUGH);
					flash.put("msg", "请支付资料审核费");
					
					auditMaterialsIPS("1", "10", "", "", "", "", "");
				}

				UserAuditItem.submitUploadedItems(user.id, balance, error);
				flash.error(error.msg);
				
				auditMaterialsIPS("1", "10", "", "", "", "", "");
			}
		}else{
			balance = details.user_amount;
			
			if(fees > balance){
				error.code = Constants.BALANCE_NOT_ENOUGH;
				error.msg = "对不起，您可用余额不足";
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rechargeType", RechargeType.UploadItems);
				map.put("fee", fees);
				Cache.set("rechargePay" + user.id, map, IPSConstants.CACHE_TIME);
				flash.put("code", Constants.BALANCE_NOT_ENOUGH);
				flash.put("msg", "请支付资料审核费");
				
				auditMaterialsIPS("1", "10", "", "", "", "", "");
			} else {
				UserAuditItem.submitUploadedItems(user.id, balance, error);
				flash.error(error.msg);
				
				auditMaterialsIPS("1", "10", "", "", "", "", "");
			}
		}
		
		AccountHome.auditMaterialsIPS("1", "10", null, null, null, null, null);
	}
	
	/**
	 * 清空用户上传未付款的资料
	 */
	public static void clearUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		UserAuditItem.clearUploadedItems(User.currUser().id, error);
		flash.error(error.msg);
		
		AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
	}
	
	//vip会员优先计划
	public static void vipFirst(){
		render();
	}
	
	/* 2014-11-15 */
	/**
	 * 借款合同
	 */
	public static void pact(String sign, int type){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1)
			renderText(error.msg);
		
	    long userId = User.currUser().id;
		String pact = Bid.queryPact(bidId, userId);
		
		render(pact, type);
	}

	/**
	 * 居间服务协议
	 */
	public static void intermediaryAgreement(String sign, int type){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1)
			renderText(error.msg);
		
	    long userId = User.currUser().id;
		String intermediaryAgreement = Bid.queryIntermediaryAgreement(bidId, userId);
		
		render(intermediaryAgreement, type);
	}
	
	/**
	 * 保障涵
	 */
	public static void guaranteeBid(String sign, int type){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1)
			renderText(error.msg);
		
	    long userId = User.currUser().id;
		String guaranteeBid = Bid.queryGuaranteeBid(bidId, userId);
		
		render(guaranteeBid, type);
	}
}
