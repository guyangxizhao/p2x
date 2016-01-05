package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import business.Bid.Purpose;
import business.Bid.Repayment;
import constants.Constants;
import constants.SQLTempletes;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import models.t_bids;
import models.t_bill_invests;
import models.v_bill_invest;
import models.v_bill_invest_detail;

public class BillInvests implements Serializable{
	private static final long serialVersionUID = 2886622569352251679L;
	public long id;
	private long _id;
	
	public long userId;
	public long bidId;
	public int period;
	public String title;
	public Date receiveTime;
	public double receiveCorpus;
	public double receiveInterest;
	public int status;
	public double overdueFine;
	public Date realReceiveTime;
	public double realReceiveCorpus;
	public double realReceiveInterest;
	
	public Bid bid;
	
	public void setId(long id){
		t_bill_invests invest = t_bill_invests.findById(id);
		
		if(invest.id < 0 || invest == null){
			this._id = -1;
			return;
		}
		
		this._id = invest.id;
		this.userId = invest.user_id;
		this.bidId = invest.bid_id;
		this.period = invest.periods;
		this.title = invest.title;
		this.receiveTime = invest.receive_time;
		this.receiveCorpus = invest.receive_corpus;
		this.receiveInterest = invest.receive_interest;
		this.status = invest.status;
		this.overdueFine = invest.overdue_fine;
		this.realReceiveCorpus = invest.real_receive_corpus;
		this.realReceiveInterest = invest.real_receive_interest;
		
		bid = new Bid();
   	    bid.id = invest.bid_id;
	}
	
	public long getId(){
		
		return this._id;
	}
	
	/**
	 * 查询我所有的理财账单
	 * @param error
	 * @return
	 */
	public static List<v_bill_invest> queryMyAllInvestBills(ErrorInfo error) {
		error.clear();
		
		List<v_bill_invest> bills = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST);
		sql.append(" and c.id = ? group by a.id");
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
            query.setParameter(1, User.currUser().id);
            bills = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询我所有的理财账单:" + e.getMessage());
			error.code = -1;
			error.msg = "查询我所有的理财账单失败!";
			
			return null;
		}
		
		return bills;
	}
	
	/**
 	 * 查询我的理财账单
 	 * @param userId
 	 * @param info
 	 * @param currPage
 	 * @return
 	 */
 	public static PageBean<v_bill_invest> queryMyInvestBills(int payType, int isOverType,
			int keyType, String keyStr, int currPageStr, long userId, ErrorInfo error){
        error.clear();
		
 		int count = 0;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		
        Map<String,Object> conditionMap = new HashMap<String, Object>();
 		List<v_bill_invest> bills = new ArrayList<v_bill_invest>();
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST);

		List<Object> params = new ArrayList<Object>();
 		
 		if((payType < 0) || (payType > 2)){
 			payType = 0;
 		}
 		
 		if((isOverType < 0) || (isOverType > 2)){
 			isOverType = 0;
 		}
 		
 		if((keyType < 0) || (keyType > 3)){
 			keyType = 0;
 		}
 		
 		if(currPageStr != 0){
 			currPage = currPageStr;
 		}
 		
 		if(StringUtils.isNotBlank(keyStr)) {
			sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
			params.add("%"+keyStr.trim()+"%");
		}
 		
 		sql.append(SQLTempletes.LOAN_INVESTBILL_RECEIVE[payType]);
 		sql.append(SQLTempletes.LOAN_INVESTBILL_OVDUE[isOverType]);
 		sql.append("and c.id = ?");
 		params.add(userId);
 		
 		conditionMap.put("payType", payType);
		conditionMap.put("isOverType", isOverType);
		conditionMap.put("keyType", keyType);
		conditionMap.put("key", keyStr);
		
		try {
			sql.append(" group by id");
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bills = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单失败";
			
			return null;
		}
		
		PageBean<v_bill_invest> page = new PageBean<v_bill_invest>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = bills;
		
		return page;
 	}
 	/**
 	 * 理财页面我的理财账单
 	 * @param userId
 	 * @param info
 	 * @param currPage
 	 * @return
 	 */
 	public static PageBean<v_bill_invest> queryMyInvestBills(int payType, int isOverType,
			int keyType, String keyStr, int currPageStr, long userId, ErrorInfo error,String type){
        error.clear();
 		int count = 0;
 		int currPage = 1;
 		int pageSize = Constants.FIVE;
 		
        Map<String,Object> conditionMap = new HashMap<String, Object>();
 		List<v_bill_invest> bills = new ArrayList<v_bill_invest>();
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST);
		if(type.equals("Payment")){
		sql.append("and b.status= 4 ");
		}
		if(type.equals("Bid")){
			sql.append("and (b.status=2 or b.status=3) ");	
		}
		if(type.equals("Settle")){
			sql.append("and b.status= 5 ");
		}

		List<Object> params = new ArrayList<Object>();
 		
 		if((payType < 0) || (payType > 2)){
 			payType = 0;
 		}
 		
 		if((isOverType < 0) || (isOverType > 2)){
 			isOverType = 0;
 		}
 		
 		if((keyType < 0) || (keyType > 3)){
 			keyType = 0;
 		}
 		
 		if(currPageStr != 0){
 			currPage = currPageStr;
 		}
 		
 		if(StringUtils.isNotBlank(keyStr)) {
			sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
			params.add("%"+keyStr.trim()+"%");
		}
 		sql.append(SQLTempletes.LOAN_INVESTBILL_RECEIVE[payType]);
 		sql.append(SQLTempletes.LOAN_INVESTBILL_OVDUE[isOverType]);
 		sql.append("and c.id = ?");
 		params.add(userId);
 		
 		conditionMap.put("payType", payType);
		conditionMap.put("isOverType", isOverType);
		conditionMap.put("keyType", keyType);
		conditionMap.put("key", keyStr);
		
		try {
			sql.append(" group by id");
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bills = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单失败";
			
			return null;
		}
		
		PageBean<v_bill_invest> page = new PageBean<v_bill_invest>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = bills;
		
		return page;
 	}
 	
 	/**
 	 * 我的账单详情
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static v_bill_invest_detail queryMyInvestBillDetails(long id, long userId, ErrorInfo error){
 		error.clear();
		
 		v_bill_invest_detail investDetail = new v_bill_invest_detail();
 		List<v_bill_invest_detail> v_bill_invest_detail_list = null;
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST_DETAIL);
		sql.append(" and a.id = ? and a.user_id = ?");
		
 		try {
 			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest_detail.class);
            query.setParameter(1, id);
            query.setParameter(2, userId);
            query.setMaxResults(1);
            v_bill_invest_detail_list = query.getResultList();
            
            if(v_bill_invest_detail_list.size() > 0){
            	investDetail = v_bill_invest_detail_list.get(0);
            }
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单详情时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
		}
		 
		if(null == investDetail){
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
			
		}
		
		error.code = 1;
 		return investDetail;
 	}
 	
 	/**
 	 * 我的账单详情
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static v_bill_invest_detail queryMyInvestBillDetails(long id, ErrorInfo error){
 		error.clear();
		
 		v_bill_invest_detail investDetail = new v_bill_invest_detail();
 		List<v_bill_invest_detail> v_bill_invest_detail_list = null;
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST_DETAIL);
		sql.append(" and a.id = ?");
		
 		try {
 			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest_detail.class);
            query.setParameter(1, id);
            query.setMaxResults(1);
            v_bill_invest_detail_list = query.getResultList();
            
            if(v_bill_invest_detail_list.size() > 0){
            	investDetail = v_bill_invest_detail_list.get(0);
            }
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单详情时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
		}
		 
		if(null == investDetail){
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
			
		}
		
		error.code = 1;
 		return investDetail;
 	}
 	
 	/**
 	 * 我的理财账单——-历史收款情况
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static PageBean<t_bill_invests> queryMyInvestBillReceivables(long bidId,long userId, long investId, int currPage, int pageSize, ErrorInfo error){
 		error.clear();
 		
 		String sql = "select new t_bill_invests(id as id,title as title, SUM(receive_corpus + receive_interest + ifnull(overdue_fine,0)) as receive_amount, " +
 				"status as status, receive_time as  receive_time, real_receive_time as real_receive_time )" +
 				"from t_bill_invests where bid_id = ? and user_id = ? and invest_id = ? group by id";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();
		PageBean<t_bill_invests> page = new PageBean<t_bill_invests>();
		page.pageSize = Constants.FIVE;
		page.currPage = Constants.ONE;
		
		if(currPage != 0){
			page.currPage = currPage;
		}
		
		if(pageSize != 0){
			page.pageSize = pageSize;
		}
		
		try {
			page.totalCount = (int) t_bill_invests.count("bid_id = ? and user_id = ? and invest_id = ?", bidId, userId, investId);
			investBills = t_bill_invests.find(sql, bidId, userId, investId).fetch(page.currPage, page.pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		
		page.page = investBills;

		return page;
 	}
 	
 	/**
 	 * 我的理财账单——-根据标的ID和投资人ID查询还款记录
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static List<t_bill_invests> queryMyInvestBillReceivablesBid(long bidId,long userId, ErrorInfo error){
 		error.clear();
 		String sql = "SELECT new t_bill_invests(id AS id,title AS title,status AS status, receive_time AS  receive_time,(receive_corpus+receive_interest) AS receive_amount,real_receive_time AS real_receive_time)" +
 				" FROM t_bill_invests WHERE bid_id = ? AND user_id = ? order by receive_time asc";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();

		try {
			investBills = t_bill_invests.find(sql, bidId, userId).fetch();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		

		return investBills;
 	}
	public static t_bill_invests queryInvestsByBillId(long id){
		
        t_bill_invests t=null;
         String sql = "select new t_bill_invests(id,user_id, bid_id, invest_id,"+
			" mer_bill_no, title, receive_time,"+
			" receive_corpus, overdue_fine,"+
			"receive_interest, status, periods,"+
			"real_receive_time, real_receive_corpus,"+
			"real_receive_interest) from t_bill_invests where id = ?";
			t = t_bill_invests.find(sql,id).first();
	        return t;
		
}
/**
* 查同一借款标投标记录
*/
 public static PageBean<t_bill_invests> queryPageById(long bidId,long userId, int currPage, int pageSize, ErrorInfo error){
   String sql = "select new t_bill_invests(id,user_id, bid_id, invest_id,"+
				" mer_bill_no, title, receive_time,"+
				" receive_corpus, overdue_fine,"+
				"receive_interest, status, periods,"+
				"real_receive_time, real_receive_corpus,"+
				"real_receive_interest) from t_bill_invests where user_id = ? and bid_id = ?"; 
	   PageBean<t_bill_invests> page = new PageBean<t_bill_invests>();
	   page.currPage=1;
	   page.pageSize=2;
	   if(currPage!=0){
		 page.currPage=currPage;
	   }
	   List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();
	   page.totalCount = (int) t_bill_invests.count("bid_id = ? and user_id = ?", bidId, userId);
	   investBills=t_bill_invests.find(sql, userId,bidId).fetch(page.currPage, page.pageSize);
	   page.page=investBills;
	   return page;
	   
	   
 }
 
 /**
 * 已投标中的项目
 */
  public static PageBean<t_bids> queryPageById2(long userId, int currPage, int pageSize, ErrorInfo error){
	  error.clear();
		String sql = "select new t_bids(t.id,t.title,t.amount,t.period, t.apr,"+
			"t.status,  t.loan_schedule,  t.has_invested_amount,"+
			 "t.period_unit)  from t_bids t , t_invests d  where  t.id=d.bid_id and d.user_id= ? and (t.status = 3 or t.status = 2) group by t.id";
		  PageBean<t_bids> page = new PageBean<t_bids>();
		  page.currPage=1;
		  page.pageSize=2;
		  if(currPage!=0){
				 page.currPage=currPage;
			   }
		  List<t_bids> investBills = new ArrayList<t_bids>();
		//  page.totalCount =  Integer.parseInt(t_bids.find("select count(DISTINCT(t.id))  from t_bids t , t_bill_invests d where  t.id=d.bid_id and d.user_id= ? and (t.status = 3 or t.status = 2)",userId).first().toString());  
		  EntityManager em = JPA.em();
          Query query = em.createNativeQuery("select count(DISTINCT(t.id))  from t_bids t , t_bill_invests d where  t.id=d.bid_id and d.user_id= ? and (t.status = 3 or t.status = 2)");
          query.setParameter(1, userId);
          page.totalCount=Integer.parseInt(query.getSingleResult().toString());
          
		 
		  investBills=t_bids.find(sql,userId).fetch(page.currPage, page.pageSize);
		  page.page=investBills;
		   return page;
//		try {
//			obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, bidId).getSingleResult();
//		} catch (Exception e) {
//			Logger.error("查询标的信息失败：%s", e.getMessage());
//			error.code = -1;
//			error.msg = "查询标的信息失败";
//			
//			return null;
//		}
//		
//		if(obj == null || obj.length < 8) {
//			error.code = -1;
//			error.msg = "查询标的信息失败";
//			
//			return null;
//		}
//		
//		
//		Bid bid = new Bid();
//		Purpose purpose=new Purpose();
//		purpose.name=purpose.queryPurpose(((Integer) obj[3]).longValue(), error);
//		Repayment repayment=new Repayment();
//		repayment=Repayment.queryRepaymentById(Long.parseLong(obj[12].toString()), error);
//		purpose.id=((Integer) obj[3]).longValue();
//		bid.id = ((BigInteger) obj[0]).longValue();
//		bid.userId = ((BigInteger) obj[1]).longValue();
//		bid.bidNo =  obj[2] == null ? null : obj[2].toString();
//		bid.purpose=purpose;
//		bid.amount =  ((BigDecimal) obj[4]).doubleValue();
//		bid.hasInvestedAmount = ((BigDecimal) obj[5]).doubleValue();
//		bid.serviceFees = ((BigDecimal) obj[6]).doubleValue();
//		bid.apr = ((BigDecimal) obj[7]).doubleValue();
//		bid.title=obj[8].toString();
//		bid.amount=((BigDecimal) obj[9]).doubleValue();
//		bid.strPeriodUnit=((Integer)obj[10]).toString();
//		bid.repaymentTime=(Date) obj[11];
//		bid.repayment=repayment;
//		bid.averageInvestAmount=((BigDecimal) obj[13]).doubleValue();
//		bid.loanSchedule=((BigDecimal) obj[14]).doubleValue();
//      bid.minInvestAmount=((BigDecimal) obj[15]).doubleValue();
//      bid.minAllowInvestAmount=((BigDecimal) obj[15]).doubleValue();
//      bid.status=Integer.parseInt(obj[16].toString());
//      bid.product=new Product();
//      bid.product.setId(Long.parseLong(obj[17].toString()));
//      User user=new User();
//      user=User.queryUserbyIdisDetail(bid.userId);
//		error.code = 1;
		
		
 }
}
