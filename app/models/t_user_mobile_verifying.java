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
public class t_user_mobile_verifying extends Model {

	private static final long serialVersionUID = 3706813824936215623L;

	public long user_id;
	
	public Date time;

	public String mobile;
	
	public String verify_code;


}
