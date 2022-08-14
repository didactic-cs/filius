package filius.exception;

@SuppressWarnings("serial")
public class NoValidDhcpResponseException extends Exception {

    public NoValidDhcpResponseException(String msg) {
        super(msg);
    }

}
