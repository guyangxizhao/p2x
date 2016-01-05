package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.CreditLevel;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.ServiceFee;

/**
 * 逾期的借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 上午10:47:08
 */
@Entity
public class v_bid_overdue extends Model {
	private static final long serialVersionUID = 7648617124982906841L;
	public Date time;
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public String credit_level_image_filename;
	public Double amount;
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
	public Integer repayment_count;
	public Integer overdue_count;
	public Date mark_overdue_time;
	@Transient
	public Double capital_interest_sum;
	
	@Transient
	public String filename;
	
	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repayment_type_id);
		
		return this.amount + rate;
	}
	
	public String getFilename() {
		return CreditLevel.queryUserCreditLevel(this.user_id, new ErrorInfo()).imageFilename;
	}
}