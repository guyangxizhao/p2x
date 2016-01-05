package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:20:24
 */
@Entity
public class t_invest_ensure_compensates extends Model {
	private static final long serialVersionUID = -3646627645283081381L;
	public long ensure_plan_id;
	public Date time;
	public long invest_id;
	public double amount;
	public long supervisor_id;
}
