package uz.checkout.sdk;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class V2ClientTest {

    @Test
    void createPaymentWithIdempotencyKeySetsHeader() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\"}");
        V2Client client = new V2Client("test_key_123", transport);

        client.createPayment(50000, Map.of("expireTime", 7200, "idempotencyKey", "order-42"));

        FakeTransport.Call call = transport.calls.get(0);
        assertEquals("https://checkout.uz/api/v2/create_payment", call.url);
        JSONObject sentBody = new JSONObject(call.jsonBody);
        assertEquals(50000, sentBody.getInt("amount"));
        assertEquals(7200, sentBody.getInt("expire_time"));
        assertEquals("order-42", call.extraHeaders.get("Idempotency-Key"));
    }

    @Test
    void createPaymentWithoutIdempotencyKeyOmitsHeader() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\"}");
        V2Client client = new V2Client("test_key_123", transport);

        client.createPayment(1000);

        assertTrue(transport.calls.get(0).extraHeaders.isEmpty());
    }

    @Test
    void getHistorySendsFilters() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\",\"data\":[]}");
        V2Client client = new V2Client("test_key_123", transport);

        client.getHistory(Map.of("limit", 20, "offset", 10, "status", "paid", "dateFrom", "2026-01-01"));

        JSONObject sentBody = new JSONObject(transport.calls.get(0).jsonBody);
        assertEquals(20, sentBody.getInt("limit"));
        assertEquals(10, sentBody.getInt("offset"));
        assertEquals("paid", sentBody.getString("status"));
        assertEquals("2026-01-01", sentBody.getString("date_from"));
    }

    @Test
    void cancelPayment() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\",\"order_status\":\"canceled\"}");
        V2Client client = new V2Client("test_key_123", transport);

        JSONObject result = client.cancelPayment(123);

        FakeTransport.Call call = transport.calls.get(0);
        assertEquals("https://checkout.uz/api/v2/cancel_payment", call.url);
        assertEquals(123, new JSONObject(call.jsonBody).getInt("order_id"));
        assertEquals("canceled", result.getString("order_status"));
    }

    @Test
    void refundPaymentErrorThrows() {
        FakeTransport transport = new FakeTransport(409, "{\"error\":\"More than 22 hours have passed\"}");
        V2Client client = new V2Client("test_key_123", transport);

        CheckoutApiException ex = assertThrows(CheckoutApiException.class, () -> client.refundPayment(123));
        assertEquals(409, ex.getStatusCode());
    }

    @Test
    void getFiscal() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\",\"fiscal\":{}}");
        V2Client client = new V2Client("test_key_123", transport);

        client.getFiscal(123);

        FakeTransport.Call call = transport.calls.get(0);
        assertEquals("https://checkout.uz/api/v2/get_fiscal", call.url);
        assertEquals(123, new JSONObject(call.jsonBody).getInt("order_id"));
    }

    @Test
    void profileClientUsesDifferentCredentialAgainstSameBaseUrl() {
        FakeTransport transport = new FakeTransport(200, "{\"status\":\"success\",\"shops\":[]}");
        ProfileClient profile = new ProfileClient("profile_key_xyz", transport);

        profile.getShops();

        FakeTransport.Call call = transport.calls.get(0);
        assertEquals("https://checkout.uz/api/v2/get_shops", call.url);
        assertEquals("profile_key_xyz", call.token);
    }

    @Test
    void profileClientRequiresKey() {
        assertThrows(IllegalArgumentException.class, () -> new ProfileClient(""));
    }
}
