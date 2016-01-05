package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:21:05
 */
@Entity
public class t_dict_secret_questions extends Model {
	private static final long serialVersionUID = -1045886527556327413L;
	public String name;
	public String type;
	public int use_count;
	public boolean is_use;
}
