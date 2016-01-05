package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 资金托管操作日志信息
 *
 * @author hys
 * @createDate  2015年3月6日 下午4:19:55
 *
 */
@Entity
public class t_entrust_logs extends Model{
	private static final long serialVersionUID = -549182329215055105L;
	public Date time;  //创建时间
	public long user_id;  //p2p平台用户id
	public String user_name;  //p2p平台用户名称
	public long mer_bill_no;  //操作流水号
	public String type;  //操作类型
	public String request_detail;  //请求详细参数

}
