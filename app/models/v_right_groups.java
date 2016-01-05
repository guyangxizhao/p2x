package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 权限组
 * @author md005
 */
@Entity
public class v_right_groups extends Model {
	private static final long serialVersionUID = 8021637669273773800L;
	public String name;
	public String description;
	public String right_modules;
	public long supervisor_count;
}
