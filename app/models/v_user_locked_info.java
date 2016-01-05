package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.CreditLevel;
import constants.Constants;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.Security;

@Entity
public class v_user_locked_info extends Model {

	private static final long serialVersionUID = -1758019994190845323L;
	public Date lock_time;
	public Date register_time;
	public String name;
	public double credit_score;
	public String email;
	public String mobile;
	public String mobile1;
	public String mobile2;
	public double user_amount;
	public boolean is_blacklist;
	public double recharge_amount;
	public long invest_count;
	public long invest_amount;
	public long bid_count;
	public double bid_amount;
	public long overdue_bill_count;
	public double repayment_amount;
	public String credit_level_image_filename;
	
	@Transient
	public String sign;//加密ID
	
	@Transient
	public String filename;
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
	
	public String getFilename() {
		return CreditLevel.queryUserCreditLevel(this.id, new ErrorInfo()).imageFilename;
	}
}