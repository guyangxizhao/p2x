package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 管理员权限
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:36:16
 */
@Entity
public class t_rights_of_supervisor extends Model {
	private static final long serialVersionUID = 7427431830752251412L;
	public long supervisor_id;
	public long right_id;
}
