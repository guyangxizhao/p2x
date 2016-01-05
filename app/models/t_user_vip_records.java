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
public class t_user_vip_records extends Model {

	private static final long serialVersionUID = 6419239081288143366L;

	public long user_id;
	
	public Date time;
	
	public Date start_time;
	
	public Date expiry_date;
	
	public double service_fee;
	
	public boolean status;

}
