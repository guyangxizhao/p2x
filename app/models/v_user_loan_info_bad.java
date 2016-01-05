package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.CreditLevel;
import constants.Constants;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.Security;

/**
 * 我的会员账单--坏账会员管理
 */

@Entity
public  class v_user_loan_info_bad extends Model {
	
	private static final long serialVersionUID = -4081592875804706722L;
	public long supervisor_id;
	public String type;
	public String name;
	public Date register_time;
	public double user_amount;
	public Date last_login_time;
	public int bid_count;
	public double bid_amount;
	public int invest_count;
	public double invest_amount;
	public int bid_loaning_amount;
	public int bid_repayment_amount;
	public int overdue_bill_count;
	public int bad_bid_count;
	public String credit_level_image_filename;
	public double bad_bid_amount;
	
	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
	
	@Transient
	public String filename;
	
	public String getFilename() {
		return CreditLevel.queryUserCreditLevel(this.id, new ErrorInfo()).imageFilename;
	}
}