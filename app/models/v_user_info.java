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
public class v_user_info extends Model {
	
	private static final long serialVersionUID = -4739574654725583858L;
	public String name;
	public Date register_time;
	public double score;
	public String email;
	public String mobile;
	public String mobile1;
	public String mobile2;
	public boolean is_activation;
	public boolean is_add_base_info;
	public boolean is_blacklist;
	public double user_amount;
	public Date last_login_time;
	public boolean is_allow_login;
	public long invest_count;
	public double invest_amount;
	public long bid_count;
	public double bid_amount;
	public long recharge_amount;
	public long audit_item_count;
	public String credit_level_image_filename;
	public Long order_sort;

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