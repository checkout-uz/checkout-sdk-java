package uz.checkout.sdk;

import java.util.Map;

/**
 * Low-level transport abstraction used by every client. The default
 * implementation ({@link DefaultHttpTransport}) uses {@link java.net.http.HttpClient};
 * tests inject a fake implementation instead of making real network calls.
 */
public interface HttpTransport {
    Response send(String url, String token, String jsonBody, Map<String, String> extraHeaders) throws Exception;

    final class Response {
        public final int statusCode;
        public final String body;

        public Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
