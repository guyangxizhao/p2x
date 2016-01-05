package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 省市
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:22:14
 */
@Entity
public class t_dict_ad_provinces extends Model {
	private static final long serialVersionUID = 1421640379059201605L;
	public String name;
}
