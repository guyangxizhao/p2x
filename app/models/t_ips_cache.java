package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 请求缓存信息
 * @author Administrator
 *
 */
@Entity
public class t_ips_cache extends Model {
	private static final long serialVersionUID = -132907293416530352L;
	public Date time;
	public long p_mer_bill_no;
	public String cache_info;
}