package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.CreditLevel;
import constants.Constants;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.Security;
import utils.ServiceFee;

/**
 * 还款中的借款标列表
 * 
 */
@Entity
public class v_bid_repaymenting extends Model {
	private static final long serialVersionUID = -5220030095369519300L;
	public Date time;
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public String credit_level_image_filename;
	public Long order_sort;
	public Double amount;//未还总金额
	public Double loan_amount;//新增：借款金额
	public Integer product_id;
	public String small_image_filename;
	public String product_name;
	public Integer period_unit;
	public Double apr;
	public Integer period;
	public Integer status;
	public Date audit_time;
	public Integer repayment_type_id;
	public String repayment_type_name;
	public Integer repayment_count;//未还期数
	public Integer overdue_count;//逾期期数
	@Transient
	public Double capital_interest_sum;
	public Date repayment_time;
	public Long manage_supervisor_id;
	public String supervisor_name;
	
	@Transient
	public String sign;//bid表的id签名
	
	@Transient
	public String filename;
	
	public String getSign(){
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repayment_type_id);
		
		return this.amount + rate;
	}
	
	public String getFilename() {
		return CreditLevel.queryUserCreditLevel(this.user_id, new ErrorInfo()).imageFilename;
	}
}
