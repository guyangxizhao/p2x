package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 部门账单管理--坏账会员管理
 */

@Entity
public  class v_user_for_message extends Model {
	
	private static final long serialVersionUID = -7660501316640419188L;
	public long user_id;
	public String name;
	public String photo;
	public String note_name;
}