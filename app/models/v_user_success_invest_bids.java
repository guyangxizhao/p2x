package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;


@Entity
public class v_user_success_invest_bids extends Model{
	private static final long serialVersionUID = 5041926331506771213L;
	public Long user_id;
	public Long bid_id;
	public Double invest_amount;
	public String name;
	public String title;
	public Double bid_amount;
	public Integer overdue_payback_period;
	public String no;
	public Double receiving_amount;
	public Double apr;
	public Date last_repay_time;
	
	@Transient
	public String sign;

	public String getSign() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	

}
