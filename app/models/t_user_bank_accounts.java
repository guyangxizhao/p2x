package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_bank_accounts extends Model {
	private static final long serialVersionUID = -3844217715220164641L;
	public long user_id;
	public Date time;
	public String bank_name;
	public String account;
	public String account_name;
	public boolean verified;
	public Date verify_time;
	public long verify_supervisor_id;
	public int open_province;
	public int open_city;
	public int open_bank_id;
	public int sta;
	
	public t_user_bank_accounts() {
		super();
	}
	
	public t_user_bank_accounts(long id, int open_bank_id, int open_province,
			int open_city, String bank_name, String account) {
		this.id = id;
		this.open_bank_id = open_bank_id;
		this.open_province = open_province;
		this.open_city = open_city;
		this.bank_name = bank_name;
		this.account = account;
	}
  
	public t_user_bank_accounts(long id, int open_bank_id, int open_province,
			int open_city, String bank_name, String account, int sta, String account_name) {
		this.id = id;
		this.open_bank_id = open_bank_id;
		this.open_province = open_province;
		this.open_city = open_city;
		this.bank_name = bank_name;
		this.account = account;
		this.sta=sta;
		this.account_name=account_name;
	}
}
