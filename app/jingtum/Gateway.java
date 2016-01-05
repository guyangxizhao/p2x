package jingtum;

import com.shove.security.Encrypt;


/**
 * 井通银关
 * 
 * @author liushuang
 */
public class Gateway {
	private String address = null;
	
	private String secret = null;
	
	private static Gateway gateway = null;
	
	private Gateway() {
		
	}
	
	public static final Gateway getGateway() {
		if (gateway == null)
			gateway = new Gateway();
		
		return gateway;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(Encrypt.decryptSES("ildGUgxeVQmdTtN3kJfsd4pQRU4KTkkKlEjEdpSX7HyBUFBOAU9cCaV76nSQleJ/", "ZAQwsxCdeRFV1234"));
			System.out.println(Encrypt.encryptSES("2025-06-16 23:59:00;101.200.233.140;1;1;1;1;0", "ZAQwsxCdeRFV1234"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
