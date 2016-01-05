package controllers.supervisor.bidManager;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;

import java.io.File;

import javax.persistence.Query;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import business.Bid;
import business.BidQuestions;
import business.Bill;
import business.Invest;
import business.Payment;
import business.Product;
import business.ProductAuditItem;
import business.StationLetter;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import constants.Constants;
import constants.IPSConstants;
import controllers.supervisor.SupervisorController;
import models.t_bid_jingtum;
import models.t_user_report_users;
import models.v_bid_JingTum;
import models.v_bid_auditing;
import models.v_bid_bad;
import models.v_bid_fundraiseing;
import models.v_bid_not_through;
import models.v_bid_overdue;
import models.v_bid_repaymenting;
import models.v_bid_repayment;
import models.v_bill_detail;
import models.v_bill_loan;
import models.v_bill_repayment_record;
import models.v_invest_records;
import models.v_user_audit_items;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

/**
 * 平台借款标 Action
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-25 上午08:43:32
 */
public class BidPlatformAction extends SupervisorController {

	/**
	 * 获取 参数值
	 * @param pageBean 当前模板PageBean
	 * @return String [] 参数值
	 */
	public static String [] getParameter(PageBean pageBean, String userId){
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String condition = params.get("condition"); // 条件
		String keyword = params.get("keyword"); // 关键词
		String startDate = params.get("startDate"); // 开始时间
		String endDate = params.get("endDate"); // 结束时间
		String orderIndex = params.get("orderIndex"); // 排序索引
		String orderStatus = params.get("orderStatus"); // 升降标示
		
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		
		/* ""/null:标示非用户ID查询  */
		return new String[]{userId, condition, keyword, startDate, endDate, orderIndex, orderStatus};
	}
	
	/**
	 * 审核中的借款标列表
	 */
	public static void auditingList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_auditing> pageBean = new PageBean<v_bid_auditing>();
		pageBean.page = Bid.queryBidAuditing(pageBean, error, getParameter(pageBean, null));
		render(pageBean);
	}
	
	/**
	 *井通审核中列表
	 */
	public static void reimbursementList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_JingTum> pageBean = new PageBean<v_bid_JingTum>();
		pageBean.page = Bid.queryJingTumState(pageBean, error, getParameter(pageBean, null));
		render(pageBean);
	}
	
	/**
	 *借款中的借款标列表
	 */
	public static void fundraiseingList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.page = Bid.queryBidFundraiseing(pageBean, Constants.V_FUNDRAISEING, error, getParameter(pageBean, null));
		
		render(pageBean);
	}

	/**
	 *满标待放款
	 */
	public static void fullList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.page = Bid.queryBidFundraiseing(pageBean, Constants.V_FULL, error, getParameter(pageBean, null));

		render(pageBean);
	}

	/**
	 *还款中的借款标列表
	 */
	public static void repaymentingList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.page = Bid.queryBidRepaymenting(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, 0, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){
			
			List<v_bid_repaymenting> list = pageBean.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
			}
			
			File file = ExcelUtils.export("还款中的借款标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "本息合计", "还款期限", "还款方式", "已还期数",
			"逾期账单"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time", "capital_interest_sum",
			"period", "repayment_type_name", "repayment_count",
			"overdue_count"});
			   
			renderBinary(file, System.currentTimeMillis() + ".xls");
		}
		
		render(pageBean);
	}

	/**
	 *逾期的借款标
	 */
	public static void overdueList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_overdue> pageBean = new PageBean<v_bid_overdue>();
		pageBean.page = Bid.queryBidOverdue(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){
			
			List<v_bid_overdue> list = pageBean.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
			}
			
			File file = ExcelUtils.export("逾期的借款标",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "本息合计", "已还期数",
			"逾期账单数量", "开始逾期时间"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time", "capital_interest_sum",
			"repayment_count", "overdue_count", "mark_overdue_time"});
			   
			renderBinary(file, System.currentTimeMillis() + ".xls");
		}
		
		render(pageBean);
	}

	/**
	 *已完成的借款标列表的搜索
	 */
	public static void repaymentList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.page = Bid.queryBidRepayment(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, 0, error, getParameter(pageBean, null));
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_bid_repayment> list = pageBean.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
			}
			
			File file = ExcelUtils.export("已完成的借款标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "已还期数",
			"最后还款时间", "逾期账单数量"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time",
			"repayment_count", "last_repay_time", "overdue_count"});
			   
			renderBinary(file, System.currentTimeMillis() + ".xls");
		}
		
		render(pageBean);
	}

	/**
	 *未通过的借标列表款
	 */
	public static void notThroughList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_not_through> pageBean = new PageBean<v_bid_not_through>();
		pageBean.page = Bid.queryBidNotThrough(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, error, getParameter(pageBean, null));
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_bid_not_through> list = pageBean.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				
				DecimalFormat df = new DecimalFormat("#0.0");
				double percent = 0.0;
				int productItem = bid.getInt("product_item_count");
				int userItem = bid.getInt("user_item_count_true");
				if(productItem == 0 || userItem / productItem >= 1){
					percent = 100.0;
				}else{
					percent = (userItem * 100.0 ) / productItem;
				}
				String auditStatus = df.format(percent) + "%";
				
				String strStatus = "";
				int status = bid.getInt("status");
				switch (status) {
				case -1:
					strStatus = "审核不通过";
					break;
				case -2:
					strStatus = "借款中不通过";
					break;
				case -3:
					strStatus = "放款不通过";
					break;
				case -4:
					strStatus = "流标";
					break;
				case -5:
					strStatus = "撤销";
					break;
				default:
					break;
				}
				
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("audit_status", auditStatus);  //审核进度
				bid.put("status", strStatus); 
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
			}
			
			File file = ExcelUtils.export("未通过的借款标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "申请时间", "失败时间", "须审核科目", "已提交科目", "审核进度",
			"当前状态"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "time","audit_time", "product_item_count",
			"user_item_count_true", "audit_status", "status"});
			   
			renderBinary(file, System.currentTimeMillis() + ".xls");
		}
		
		render(pageBean);
	}

	/**
	 *坏账借款标列表
	 */
	public static void badList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_bad> pageBean = new PageBean<v_bid_bad>();
		pageBean.page = Bid.queryBidBad(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, 0, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){
			
			List<v_bid_bad> list = pageBean.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
			}
			
			File file = ExcelUtils.export("坏账借款标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "已还期数",
			"最后还款时间", "逾期账单数", "逾期时长", "坏账操作时间"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time","repayment_count",
			"last_repay_time", "overdue_count", "overdue_length", "mark_bad_time"});
			   
			renderBinary(file, System.currentTimeMillis() + ".xls");
		}
		
		render(pageBean);
	}
	
	/**
	 * 审核中的借款标详情
	 */
	public static void auditingDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SHZ;
		bid.id = bidId;
		
		render(bid);
	}
	/**
	 * 井通审核中的借款
	 * @param bidId
	 */
	public static void jingtumingDetail(long bidId){
		
		if(0 == bidId) render();
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SHZ;
		bid.id = bidId;
		try {
			t_bid_jingtum jing=t_bid_jingtum.find("bid_id",bidId).first();
			bid.bidNo=jing.bid_address;//借用bidNo 存放 标再井通的地址， 用于审核
		} catch (Exception e) {
			Logger.error("查询井通资源号", e.getMessage());
		}
		render(bid);
	}
	
	/**
	 * 筹款中的借款标详情
	 */
	public static void fundraiseingDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_JKZ;
		bid.id = bidId;
		try {
			t_bid_jingtum jing=t_bid_jingtum.find("bid_id",bidId).first();
			bid.bidNo=jing.bid_address;//借用bidNo 存放 标再井通的地址， 用于审核
		} catch (Exception e) {
			Logger.error("查询井通资源号", e.getMessage());
		}
		render(bid);
	}
	
	/**
	 * 满标的借款标详情
	 */
	public static void fullDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_MBZ;
		bid.id = bidId;
		try {
			t_bid_jingtum jing=t_bid_jingtum.find("bid_id",bidId).first();
			bid.bidNo=jing.bid_address;//借用bidNo 存放 标再井通的地址， 用于审核
		} catch (Exception e) {
			Logger.error("查询井通资源号", e.getMessage());
		}
		render(bid);
	}
	
	/**
	 * 借款成功(还款中的、已完成的、逾期的、坏账的借款标详情)的标详情
	 */
	public static void loanSucceedDetail(long bidId, int type, int falg){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = falg;
		bid.id = bidId;
		render(bid, type, falg);
	}
	
	/**
	 * 借款失败,初核不通过、借款中不通过、流标、撤销、放款审核不通过详情
	 */
	public static void notThroughDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SBZ;
		bid.id = bidId;
		
		render(bid);
	}
	
	/*public static void userItemsList(int currPage, String signUserId, long productId, int status) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem(currPage + "", null, userId, error, null, null, null, productId + "", null);
		Product product = new Product();
		product.bidDetail = true;
		product.id = productId;
		
		render(pageBean, product, status);
	}*/
	
	/**
	 * 资料列表
	 */
	public static void userItemsList(String signUserId, long productId, int status, String mark) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		List<v_user_audit_items> items = UserAuditItem.queryUserAuditItem(userId, productId, error);
		List<ProductAuditItem> requiredAuditItem = ProductAuditItem.queryAuditByProductMark(mark, false, Constants.NEED_AUDIT);

		render(items, requiredAuditItem, status);
	}
	
	/**
	 * 查询借款标的所有提问记录异步分页方法
	 */
	public static void bidQuestion(int currPage, long bidId){
		
		if(0 == bidId) render();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<BidQuestions> pageBean = BidQuestions.queryQuestion(currPage, 1, bidId, "", Constants.SEARCH_ALL, -1, error);
		
		render(pageBean, error);
	}

	/**
	 * 投标记录
	 */
	public static void bidRecord(int currPage, long bidId) {
		
		if(0 == bidId) render();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		pageBean.currPage = currPage;
		pageBean.page = Invest.bidInvestRecord(pageBean, bidId, error);
		
		render(pageBean);
	}
	
	/**
	 * 历史记录
	 */
	public static void historyDetail(Date time, String signUserId) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		Map<String, String> historySituationMap = User.historySituation(userId,error);// 借款者历史记录情况
		
		render(time, historySituationMap);
	}
	
	/**
	 * 举报记录
	 */
	public static void reportRecord(int currPage, String signUserId){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		PageBean<t_user_report_users> pageBean = new PageBean<t_user_report_users>();
		pageBean.currPage = currPage;
		pageBean.page = User.queryBidRecordByUser(pageBean, userId, error);
		
		render(pageBean);
	}
	
	/**
	 * 还款情况
	 */
	public static void repaymentSituation(int currPage, long bidId){

		if(0 == bidId) render();

		ErrorInfo error = new ErrorInfo();
		PageBean<v_bill_loan> pageBean = new PageBean<v_bill_loan>();
		pageBean.currPage = currPage;
		pageBean.page = Bill.queryMyLoanBills(pageBean, -1, bidId, error);

		render(pageBean);
	}
	
	/**
	 * 还款情况详情
	 */
	public static void repaymentSituationDetail(int currPage, long billId) { 
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		v_bill_detail billDetail = Bill.queryBillDetails(billId, user.id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);

		render(billDetail, page);
	}

	/**
	 * 管理员给用户发送站内信
	 */
	public static void sendMessages(String signUserId, String title, String content) {
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
	
		if(userId < 1)
			renderText(error.msg);
		
		if (StringUtils.isBlank(title) || StringUtils.isBlank(content))
			renderText("数据有误!");

		StationLetter letter = new StationLetter();
		letter.senderSupervisorId = Supervisor.currSupervisor().id;
		letter.receiverUserId = userId;
		letter.title = title;
		letter.content = content;
		letter.sendToUserBySupervisor(error);
		
		renderText(error.msg);
	}
	
	/**
	 * 设置优质标
	 */
	public static void siteQuality(long bidId, boolean status) {

		if(0 == bidId) renderText("设置出错!");
		
		ErrorInfo error = new ErrorInfo();
		Bid.editQuality(bidId, status, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 设置"火"标
	 */
	public static void siteHot(long bidId, boolean status) {

		if(0 == bidId) renderText("设置出错!");
		
		ErrorInfo error = new ErrorInfo();
		Bid.editHot(bidId, status, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 *  审核中->提前借款 【失效】
	 */
	public static void auditToadvanceLoan(String sign) {
		/* 解密BidId */
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.auditToadvanceLoan(error);
		flash.error(error.msg); 
		
		auditingList();
	} 
	
	/**
	 *  审核中->井通审核中
	 */
	public static void auditTojingtum(String sign) {
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}

		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.auditToFundraise(error);
		flash.error(error.msg); 
			
		auditingList();
	}
	
	/** 
	 * 井通审核->筹款中 
	 */
	public static void jingtumToFundraise(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			reimbursementList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			if(bid.hasInvestedAmount == bid.amount)
				fullList();
			
			fundraiseingList();
		}
		
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.jingtumToFundraise(error);
		flash.error(error.msg); 
			
		if(bid.hasInvestedAmount == bid.amount){
			fullList();
		}
		fundraiseingList();
	}
	
	/** 
	 * 审核中->审核不通过【失效】
	 */
	public static void auditToNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}

		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.auditToNotThrough(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_S, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg);
			
		auditingList();
	}
	
	/** 
	 * 井通审核中->借款中不通过
	 */
	public static void jingtumToNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			reimbursementList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			if(bid.hasInvestedAmount == bid.amount)
				fullList();
			
			fundraiseingList();
		}
		
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.jingtumToNotThrough(error);
		
		if(Constants.IPS_ENABLE) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_B, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg); 
			
		if(bid.hasInvestedAmount == bid.amount)
			fullList();
		
		fundraiseingList();
	}
	
	/**
	 *  筹款中->借款中不通过 
	 */
	public static void fundraiseToPeviewNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.fundraiseToPeviewNotThrough(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_I, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		/* 修改井通审核状态 为0【不通过】*/
		 StringBuffer update = new StringBuffer();
		 update.append("update t_bid_jingtum set  result = 0 , status = 0 where bid_id = ");
		 update.append(bidId);
		 Query rs = JPA.em().createQuery(update.toString());
		 int change=rs.executeUpdate();
		 if(change < 1){
			JPA.setRollbackOnly();
			Logger.error("修改井通审核状态失败");
		 }else{
			Logger.info("修改井通审核状态成功");
			//修改标的审核状态
		 }
		
		flash.error(error.msg); 
			
		fundraiseingList();
	}
	
	/** 
	 * 满标->待放款 
	 */
	public static void fundraiseToEaitLoan(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.fundraiseToEaitLoan(error);
		flash.error(error.msg); 
			
		fullList();
	}
	
	/**
	 *  满标->放款不通过 
	 */
	public static void fundraiseToLoanNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.fundraiseToLoanNotThrough(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_M, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg); 
			
		fullList();
	}
}
