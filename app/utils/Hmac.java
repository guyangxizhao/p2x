package utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import sun.misc.BASE64Encoder;

public class Hmac {
	/**
	 * 将待加密数据data，通过密钥key，使用hmac-md5算法进行加密，然后返回加密结果。 
	 * 
	 * @author 
	 * @param key
	 *            密钥
	 * @param data
	 *            待加密数据
	 * @return 加密结果
	 * @throws NoSuchAlgorithmException
	 */
	public static String getHmacMd5Bytes(String data,String key){
			try {
				SecretKey sk=new SecretKeySpec(key.getBytes(),"HmacMD5");
				Mac mac=Mac.getInstance(sk.getAlgorithm());
				mac.init(sk);
				byte[] result=mac.doFinal(data.getBytes());
				return new HexBinaryAdapter().marshal(result);
			} catch (Exception e) {
				return null;
			}
	}
}