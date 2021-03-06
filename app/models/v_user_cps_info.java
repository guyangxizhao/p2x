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
public class v_user_cps_info extends Model {
	
	private static final long serialVersionUID = 896341550913150856L;
	public Date register_time;
	public String name;
	public double credit_score;
	public String email;
	public String mobile;
	public String mobile1;
	public String mobile2;
	public boolean is_allow_login;
	public boolean vip_status;
	public long recommend_user_id;
	public String recommend_user_name;
	public boolean is_blacklist;
	public double user_amount;
	public double recharge_amount;
	public double invest_amount;
	public double bid_amount;
	public double commission_amount;
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