package uz.checkout.sdk;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class V1ClientTest {

    @Test
    void requiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new V1Client(""));
    }

    @Test
    void createPaymentSendsCorrectRequest() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\",\"payment\":{\"_id\":1}}");
        V1Client client = new V1Client("test_key_123", transport);

        JSONObject result = client.createPayment(50000, Map.of("description", "test order"));

        assertEquals("success", result.getString("status"));
        FakeTransport.Call call = transport.calls.get(0);
        assertEquals("https://checkout.uz/api/v1/create_payment", call.url);
        assertEquals("test_key_123", call.token);
        JSONObject sentBody = new JSONObject(call.jsonBody);
        assertEquals(50000, sentBody.getInt("amount"));
        assertEquals("test order", sentBody.getString("description"));
        assertFalse(sentBody.has("webhook_url"));
    }

    @Test
    void optionalNullParamsAreOmitted() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\"}");
        V1Client client = new V1Client("test_key_123", transport);

        client.createPayment(1000);

        JSONObject sentBody = new JSONObject(transport.calls.get(0).jsonBody);
        assertEquals(1, sentBody.keySet().size());
        assertEquals(1000, sentBody.getInt("amount"));
    }

    @Test
    void errorResponseThrowsCheckoutApiException() {
        FakeTransport transport = new FakeTransport(401, "{\"error\":\"Unauthorized: Invalid or missing Bearer token\"}");
        V1Client client = new V1Client("test_key_123", transport);

        CheckoutApiException ex = assertThrows(CheckoutApiException.class, client::getBalance);
        assertEquals(401, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Unauthorized"));
    }

    @Test
    void networkErrorThrowsCheckoutApiException() {
        FakeTransport transport = FakeTransport.throwing(new java.io.IOException("boom"));
        V1Client client = new V1Client("test_key_123", transport);

        assertThrows(CheckoutApiException.class, client::getBalance);
    }

    @Test
    void payViaCardSendsCorrectBody() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\"}");
        V1Client client = new V1Client("test_key_123", transport);

        client.payViaCard(5, "8600123412341234", "1230");

        FakeTransport.Call call = transport.calls.get(0);
        assertEquals("https://checkout.uz/api/v1/pay_via_card", call.url);
        JSONObject sentBody = new JSONObject(call.jsonBody);
        assertEquals(5, sentBody.getInt("order_id"));
        assertEquals("8600123412341234", sentBody.getString("card_number"));
        assertEquals("1230", sentBody.getString("card_expiry"));
    }

    @Test
    void statusPaymentByUuid() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\"}");
        V1Client client = new V1Client("test_key_123", transport);

        client.statusPaymentByUuid("abc-123");

        JSONObject sentBody = new JSONObject(transport.calls.get(0).jsonBody);
        assertEquals("abc-123", sentBody.getString("uuid"));
    }
}
