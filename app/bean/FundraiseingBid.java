package bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import business.CreditLevel;
import constants.Constants;
import utils.ErrorInfo;
import utils.Security;
import utils.ServiceFee;

@Entity
public class FundraiseingBid implements Serializable{
	private static final long serialVersionUID = 8235485909705113206L;
	@Id
	public Long id;
	public String bid_no;
	public String title;
	public Date real_invest_expire_time;
	public Long user_id;
	public String user_name;
	public Double amount;
	public Double has_invested_amount;
	public String small_image_filename;
	public Double apr;
	public Integer period_unit;
	public Integer period;
	public Date time;
	public Integer status;
	public Double loan_schedule;
	public String credit_level_image_filename;
	public Integer full_days;
	public Integer product_item_count;
	public Integer user_item_count_true;
	public Integer repaymentId;
	public Integer user_item_count_false;
	@Transient
	public Double capital_interest_sum;
	
	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repaymentId);
		
		return this.amount + rate;
	}
	
	@Transient
	public String sign;
	
	public String getSign(){
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	@Transient
	public String filename;
	
	public String getFilename() {
		return CreditLevel.queryUserCreditLevel(this.user_id, new ErrorInfo()).imageFilename;
	}
}
