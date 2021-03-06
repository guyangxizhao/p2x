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
public class v_user_invest_info extends Model {

	private static final long serialVersionUID = -6918920341947972472L;
	public Date register_time;
	public String name;
	public String email;
	public String mobile;
	public String mobile1;
	public String mobile2;
	public boolean is_allow_login;
	public double credit_score;	
	public double user_amount;
	public int master_identity;
	public double recharge_amount;
	public long invest_count;
	public double invest_amount;
	public int invest_receive_count;
	public double receive_amount;
	public long transfer_count;
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