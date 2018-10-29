package urbantrees.spaklingscience.at.urbantrees.http;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 208/10/26
 */
public class HttpHeader {

    private String key;

    private String value;

    public HttpHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
