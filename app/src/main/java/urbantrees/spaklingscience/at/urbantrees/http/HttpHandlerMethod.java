package urbantrees.spaklingscience.at.urbantrees.http;

/**
 * Possible HTTP methods used with {@link HttpHandlerParams}.
 * @see HttpHandler
 * @author Laurenz Fiala
 * @since 2018/06/17
 */
public enum HttpHandlerMethod {

    GET (true, false),
    POST (true, true),
    PUT (false, true);

    private final boolean hasInput;
    private final boolean hasOutput;

    HttpHandlerMethod(final boolean hasInput, final boolean hasOutput) {
        this.hasInput = hasInput;
        this.hasOutput = hasOutput;
    }

    public boolean hasInput() {
        return hasInput;
    }

    public boolean hasOutput() {
        return hasOutput;
    }
}
