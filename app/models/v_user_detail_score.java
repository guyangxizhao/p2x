package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 信用积分明细
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:27:06
 */
@Entity
public class v_user_detail_score extends Model {

	private static final long serialVersionUID = 2333748796075376143L;
	public double audit_score;
	public double repayment_score;
	public double loan_score;
	public double invest_score;
	public double overdue_score;
}
