package urbantrees.spaklingscience.at.urbantrees.http;

/**
 * Result of a {@link HttpHandler} invocation.
 * @see HttpHandler
 * @author Laurenz Fiala
 * @since 2018/06/17
 */
public class HttpHandlerResult {

    private int responseCode;
    private String responseValue;

    public HttpHandlerResult(int responseCode, String responseValue) {
        this.responseCode = responseCode;
        this.responseValue = responseValue;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean isSuccessful() {
        return responseCode == 200;
    }

    public String getResponseValue() {
        return responseValue;
    }

    public static void isSuccessfulElseThrow(HttpHandlerResult result) throws Exception {
        if(result == null || !result.isSuccessful()) {
            throw new Exception("Response status of http call was not 200.");
        }
    }

}
