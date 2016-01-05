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
public class v_user_unverified_info extends Model {

	private static final long serialVersionUID = -8282223925105004425L;
	public Date register_time;
	public String name;
	public double credit_score;
	public String mobile;
	public String email;
	public boolean is_allow_login;
	public double user_amount;
	public double recharge_amount;
	public long invest_count;
	public double invest_amount;
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