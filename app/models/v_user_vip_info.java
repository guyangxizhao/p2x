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
public class v_user_vip_info  extends Model {
	
	private static final long serialVersionUID = -8404182296261484047L;
	public Date register_time;
	public String name;
	public double credit_score;
	public String email;
	public String mobile;
	public String mobile1;
	public String mobile2;
	public boolean is_allow_login;
	public double user_amount;
	public Date vip_expiry_date;
	public double recharge_amount;
	public long invest_count;
	public double invest_amount;
	public long bid_count;
	public double bid_amount;
	public long audit_item_count;
	public String credit_level_image_filename;
	public Long order_sort;
	
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