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
public class t_user_blacklist extends Model {

	private static final long serialVersionUID = 2955927113045745116L;
	public long user_id;
	public Date time;
	public long bid_id;
	public long black_user_id;
	public String reason;

	
}
