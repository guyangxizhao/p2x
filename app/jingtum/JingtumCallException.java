package jingtum;

/**
 * <p>井通接口调用异常类</p>
 * 
 * @author liushuang
 */
public class JingtumCallException extends Throwable {

    public JingtumCallException() {
    	super();
    }

    public JingtumCallException(String s) {
    	super(s);
    }

    public JingtumCallException(String message, Throwable cause) {
        super(message, cause);
    }
 
    public JingtumCallException(Throwable cause) {
        super(cause);
    }
}
