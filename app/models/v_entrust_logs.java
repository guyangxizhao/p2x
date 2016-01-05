package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 *  资金托管请求/回调参数信息
 *
 * @author hys
 * @createDate  2015年4月9日 上午9:01:51
 *
 */
@Entity
public class v_entrust_logs extends Model {

	private static final long serialVersionUID = 1017004452584803675L;
	public long user_id;  //p2p平台用户id
	public String user_name;  //p2p平台用户名称
	public long mer_bill_no;  //操作流水号
	public String type;  //操作类型
	public Date time;  //请求时间
	public String request_detail;  //请求详细参数
	public Date res_time;  //回调时间
	public String callback_detail;  //回调详细参数

}
