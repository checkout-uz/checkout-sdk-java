package uz.checkout.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Records every call it receives and returns a canned response - no real network calls. */
final class FakeTransport implements HttpTransport {

    static final class Call {
        final String url;
        final String token;
        final String jsonBody;
        final Map<String, String> extraHeaders;

        Call(String url, String token, String jsonBody, Map<String, String> extraHeaders) {
            this.url = url;
            this.token = token;
            this.jsonBody = jsonBody;
            this.extraHeaders = extraHeaders;
        }
    }

    final List<Call> calls = new ArrayList<>();
    private final int statusCode;
    private final String responseBody;
    private Exception toThrow;

    FakeTransport(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    static FakeTransport throwing(Exception e) {
        FakeTransport t = new FakeTransport(0, null);
        t.toThrow = e;
        return t;
    }

    @Override
    public Response send(String url, String token, String jsonBody, Map<String, String> extraHeaders) throws Exception {
        calls.add(new Call(url, token, jsonBody, extraHeaders));
        if (toThrow != null) throw toThrow;
        return new Response(statusCode, responseBody);
    }
}
