package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 消息发送模板
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午3:51:27
 */
@Entity
public class t_message_email_templates extends Model {
	private static final long serialVersionUID = -794688506635644292L;
	public Date time;
	public String scenarios;
	public String title;
	public String content;
	public double size;
	public boolean status;
}
