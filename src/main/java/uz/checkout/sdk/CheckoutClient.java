package uz.checkout.sdk;

/**
 * Top-level entry point for the CHECKOUT.UZ merchant API. Holds a single
 * shop API key and exposes both API versions:
 *
 * <pre>{@code
 * CheckoutClient client = new CheckoutClient("shop_api_key");
 * client.v1.createPayment(50000);
 * client.v2.createPayment(50000, Map.of("expireTime", 7200));
 * }</pre>
 *
 * <p>For the account-level (not shop-level) v2 endpoints
 * ({@code get_profile}/{@code get_shops}/{@code get_profile_stats}), use
 * {@link ProfileClient} instead - they use a different credential.
 */
public class CheckoutClient {

    public final V1Client v1;
    public final V2Client v2;

    public CheckoutClient(String apiKey) {
        this.v1 = new V1Client(apiKey);
        this.v2 = new V2Client(apiKey);
    }
}
