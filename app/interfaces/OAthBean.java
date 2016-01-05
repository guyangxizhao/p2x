package interfaces;

import java.io.Serializable;

public class OAthBean implements Serializable{
	private static final long serialVersionUID = -7811832524875171664L;
	/**
	 *访问令牌 
	 */
	public String access_token;
	/**
	 * 用户ID
	 */
	public String client_id;
	/**
	 * 开放接口ID
	 */
	public String openid;
}
