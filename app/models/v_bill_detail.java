package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;

@Entity
public class v_bill_detail extends Model {

	private static final long serialVersionUID = -8570332726260389413L;
	public int status;
	public long user_id;
	public Date repayment_time;
	public Date produce_bill_time;//审核时间
	public String repayment_type;
	public double apr;
	public double user_amount;//balance` + `e`.`freeze
	public double user_balance;//balance
	public String user_name;
	public long bid_id;
	public String bid_title;
	public double loan_amount;//借款金额
	public int loan_periods;//借款周期
	public int current_period;//当期期数
	public double current_pay_amount;//当期待还（repayment_corpus` + `a`.`repayment_interest`) + `a`.`overdue_fine
	public double loan_principal_interest;//本次借款本息合计：repayment_corpus` + `t1`.`repayment_interest`+`t1`.`overdue_fine
	public int has_payed_periods;//已还期数
	public String bill_number;//Z
	
	
	@Transient
	public String sign;

	@Transient
	public String sign2;
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
	public String getSign2() {
		return Security.addSign(this.bid_id, Constants.BID_ID_SIGN);
	}
	
}
