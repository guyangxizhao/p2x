package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:02:24
 */
@Entity
public class t_dict_houses extends Model {
	private static final long serialVersionUID = 1639196492806280039L;
	public String name;
	public String code;
	public String description;
	public boolean is_use;
}
