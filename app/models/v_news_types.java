package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_news_types extends Model {
	private static final long serialVersionUID = -1581625937696071429L;
	public String name;
	public long parent_id;
	public boolean status;
	public int _order;
	public int counts;
}
