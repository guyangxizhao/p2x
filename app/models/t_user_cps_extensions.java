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
public class t_user_cps_extensions extends Model {

	private static final long serialVersionUID = 1844783708990326418L;
	public Long user_id;
	public Date time;
	public Long recommended_user_id;
	public Boolean reward_type;

	
}