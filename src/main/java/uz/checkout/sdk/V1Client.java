package uz.checkout.sdk;

import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client for the original CHECKOUT.UZ merchant API ({@code /api/v1}).
 * Authenticated with a shop's API key (found in the shop's settings on
 * checkout.uz), sent as {@code Authorization: Bearer <apiKey>}.
 */
public class V1Client extends BaseClient {

    public static final String BASE_URL = "https://checkout.uz/api/v1";

    public V1Client(String apiKey) {
        super(BASE_URL, apiKey);
    }

    V1Client(String apiKey, HttpTransport transport) {
        super(BASE_URL, apiKey, transport);
    }

    /** Create a new payment invoice. {@code amount} is in UZS (1000-10,000,000). */
    public JSONObject createPayment(double amount) {
        return createPayment(amount, Collections.emptyMap());
    }

    /**
     * Create a new payment invoice. Accepted {@code options} keys:
     * {@code description} (String), {@code webhookUrl} (String), {@code returnUrl} (String).
     */
    public JSONObject createPayment(double amount, Map<String, Object> options) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("amount", amount);
        params.put("description", options.get("description"));
        params.put("webhook_url", options.get("webhookUrl"));
        params.put("return_url", options.get("returnUrl"));
        return post("create_payment", params);
    }

    /** Look up an invoice by its numeric id. */
    public JSONObject statusPaymentById(long id) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", id);
        return post("status_payment", params);
    }

    /** Look up an invoice by its uuid (pay_token). */
    public JSONObject statusPaymentByUuid(String uuid) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("uuid", uuid);
        return post("status_payment", params);
    }

    /** Return the shop's current balance per currency. */
    public JSONObject getBalance() {
        return post("get_balance", Collections.emptyMap());
    }

    /** Return the shop's 10 most recent invoices (newest first). */
    public JSONObject getHistory() {
        return getHistory(10);
    }

    /** Return the shop's most recent invoices (newest first). */
    public JSONObject getHistory(int limit) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("limit", limit);
        return post("get_history", params);
    }

    /** Return aggregate order count / total income for the shop. */
    public JSONObject getStats() {
        return post("get_stats", Collections.emptyMap());
    }

    /** List the payment methods enabled for this shop. */
    public JSONObject getPaymentMethods() {
        return post("get_payment_methods", Collections.emptyMap());
    }

    /** Start a direct card payment (Uzcard/Humo) for a pending invoice. Sends an SMS code to the cardholder. */
    public JSONObject payViaCard(long orderId, String cardNumber, String cardExpiry) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("order_id", orderId);
        params.put("card_number", cardNumber);
        params.put("card_expiry", cardExpiry);
        return post("pay_via_card", params);
    }

    /** Confirm a card payment started with {@link #payViaCard} using the SMS code the cardholder received. */
    public JSONObject confirmCardPayment(String paymentToken, String smsCode) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("payment_token", paymentToken);
        params.put("sms_code", smsCode);
        return post("confirm_card_payment", params);
    }
}
