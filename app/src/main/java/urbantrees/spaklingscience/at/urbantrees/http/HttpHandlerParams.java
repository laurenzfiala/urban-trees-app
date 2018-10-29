package urbantrees.spaklingscience.at.urbantrees.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Params to hand to the {@link HttpHandler}.
 * @see HttpHandler
 * @author Laurenz Fiala
 * @since 2018/06/17
 */
public class HttpHandlerParams {

    private String url;

    private HttpHandlerMethod method;

    private Map<String, String> headers;

    private String contentType = "application/json";

    private String value;

    /**
     * Timeout in milliseconds to wait for
     * the completion of the http-call.
     */
    private int timeout = 30000;

    public HttpHandlerParams(String url, HttpHandlerMethod method, HttpHeader[] headers, String value) {
        this.url = url;
        this.method = method;

        Map<String, String> convHeaders = new HashMap<String, String>();
        for(HttpHeader h : headers) {
            convHeaders.put(h.getKey(), h.getValue());
        }
        this.headers = convHeaders;

        this.value = value;
    }

    public HttpHandlerParams(String url, HttpHandlerMethod method, Map<String, String> headers, String value) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.value = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpHandlerMethod getMethod() {
        return method;
    }

    public void setMethod(HttpHandlerMethod method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
