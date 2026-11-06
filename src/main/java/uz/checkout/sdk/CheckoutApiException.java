package uz.checkout.sdk;

/**
 * Thrown whenever the CHECKOUT.UZ API returns an HTTP error status or a
 * network error occurs. {@link #getStatusCode()} is 0 if the request
 * never reached the server.
 */
public class CheckoutApiException extends RuntimeException {

    private final int statusCode;
    private final String rawResponse;

    public CheckoutApiException(String message, int statusCode, String rawResponse) {
        super(message);
        this.statusCode = statusCode;
        this.rawResponse = rawResponse;
    }

    public CheckoutApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.rawResponse = null;
    }

    /** The HTTP status code returned by the API (0 if the request never reached the server). */
    public int getStatusCode() {
        return statusCode;
    }

    /** The raw JSON response body, if any was returned. */
    public String getRawResponse() {
        return rawResponse;
    }
}
