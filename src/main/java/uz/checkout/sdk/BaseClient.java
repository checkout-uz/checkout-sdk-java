package uz.checkout.sdk;

import org.json.JSONObject;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared request/response handling for all CHECKOUT.UZ API clients. Every
 * endpoint accepts a JSON body over POST and replies with a JSON body -
 * any non-2xx HTTP status is treated as an error.
 */
public abstract class BaseClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    protected final String baseUrl;
    private final String token;
    private final HttpTransport transport;

    protected BaseClient(String baseUrl, String token) {
        this(baseUrl, token, new DefaultHttpTransport(DEFAULT_TIMEOUT));
    }

    /** Package-visible constructor used by tests to inject a fake {@link HttpTransport}. */
    BaseClient(String baseUrl, String token, HttpTransport transport) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("An API token is required");
        }
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.token = token;
        this.transport = transport;
    }

    private static String stripTrailingSlash(String s) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '/') end--;
        return s.substring(0, end);
    }

    protected JSONObject post(String methodName, Map<String, Object> params) {
        return post(methodName, params, Collections.emptyMap());
    }

    protected JSONObject post(String methodName, Map<String, Object> params, Map<String, String> extraHeaders) {
        String url = baseUrl + "/" + methodName;

        Map<String, Object> body = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) body.put(entry.getKey(), entry.getValue());
        }
        String jsonBody = new JSONObject(body).toString();

        HttpTransport.Response response;
        try {
            response = transport.send(url, token, jsonBody, extraHeaders);
        } catch (Exception e) {
            throw new CheckoutApiException("Network error while calling " + methodName + ": " + e.getMessage(), e);
        }

        JSONObject data = null;
        if (response.body != null && !response.body.isEmpty()) {
            try {
                data = new JSONObject(response.body);
            } catch (Exception ignored) {
                // Non-JSON body - leave data null, handled below.
            }
        }

        if (response.statusCode < 200 || response.statusCode >= 300) {
            String message = "Unknown error";
            if (data != null) {
                message = data.optString("error", data.optString("message", message));
            }
            throw new CheckoutApiException(message, response.statusCode, response.body);
        }

        return data != null ? data : new JSONObject();
    }
}
