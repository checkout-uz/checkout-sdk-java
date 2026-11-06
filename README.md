# checkout-sdk-java

[CHECKOUT.UZ](https://checkout.uz) merchant API'si uchun rasmiy Java kutubxonasi — bitta paket orqali **v1** va **v2** versiyalarining ikkalasiga ham kirish imkoni.

```java
CheckoutClient client = new CheckoutClient("kassangizning_api_kaliti");
JSONObject payment = client.v2.createPayment(50000, Map.of("description", "Buyurtma #1"));
System.out.println(payment.getJSONObject("payment").getString("_url")); // shu havolani mijozga yuboring
```

## Talablar

- **Java 11+** (o'rnatilgan `java.net.http.HttpClient`'dan foydalanadi, tashqi HTTP kutubxonasi shart emas)
- Yagona dependency: [`org.json:json`](https://mvnrepository.com/artifact/org.json/json) (JSON kodlash/dekodlash uchun)

## O'rnatish

Hozircha Maven Central'da emas. `pom.xml`'ingizga qo'shish uchun GitHub Packages yoki JitPack'dan foydalaning, yoki manba kodni to'g'ridan-to'g'ri qo'shing:

```bash
git clone https://github.com/checkout-uz/checkout-sdk-java.git
cd checkout-sdk-java
mvn install
```

Keyin o'z loyihangizga qo'shing:

```xml
<dependency>
    <groupId>uz.checkout</groupId>
    <artifactId>checkout-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API kalitini qayerdan olish mumkin

1. [checkout.uz](https://checkout.uz)ga kiring → **Kassalar** → kerakli kassani oching → **Sozlamalar**.
2. U yerda ko'rsatilgan **API KEY**ni nusxalang.
3. Bu kalitni hech kimga bermang — faqat server tomonida (backend) ishlating.

## Tezkor boshlash

```java
import uz.checkout.sdk.CheckoutClient;
import uz.checkout.sdk.CheckoutApiException;
import org.json.JSONObject;
import java.util.Map;

CheckoutClient client = new CheckoutClient("kassangizning_api_kaliti");

try {
    JSONObject payment = client.v2.createPayment(50000, Map.of(
        "description", "Buyurtma #1",
        "expireTime", 3600   // ixtiyoriy, soniyada, standart 1 soat
    ));
    JSONObject p = payment.getJSONObject("payment");
    System.out.println(p.getString("_url"));   // mijozni shu to'lov sahifasiga yo'naltiring
    System.out.println(p.getString("_uuid"));  // buni saqlab qo'ying - to'lovning doimiy ID'si
} catch (CheckoutApiException e) {
    System.err.println("Xatolik (" + e.getStatusCode() + "): " + e.getMessage());
}
```

Mijoz to'lov qilgandan keyin, checkout.uz sizning `webhookUrl`'ingizga xabar yuboradi — yoki `statusPaymentByUuid` orqali o'zingiz tekshirishingiz mumkin:

```java
JSONObject status = client.v2.statusPaymentByUuid(payment.getJSONObject("payment").getString("_uuid"));
System.out.println(status.getJSONObject("data").getString("status")); // "pending" / "paid" / "canceled" / "reversed"
```

## v1 va v2 farqi

```java
client.v1.createPayment(50000);
client.v2.createPayment(50000, Map.of("expireTime", 7200, "idempotencyKey", "order-42"));
```

v2 v1'ning hamma imkoniyatlari ustiga quyidagilarni qo'shadi:

- `idempotencyKey` (`createPayment`da) — xavfsiz qayta urinish
- `expireTime` — invoys qancha vaqt ochiq turishini belgilash
- `getHistory(Map.of("offset", ..., "status", ..., "dateFrom", ..., "dateTo", ...))` — sahifalash va filtrlash
- `getStats()` — endi `by_provider` taqsimot bilan
- `cancelPayment(orderId)` — hali "kutilmoqda" holatidagi invoysni bekor qilish
- `refundPayment(orderId)` — muvaffaqiyatli to'lovni qaytarish
- `getFiscal(orderId)` — fiskal chekni olish

## Ixtiyoriy parametrlar haqida

Java'da named/optional parametrlar yo'qligi sababli, ixtiyoriy argumentlar `Map<String, Object>` orqali uzatiladi (masalan `Map.of("description", "...", "webhookUrl", "...")`). Har bir metodning Javadoc izohida qaysi kalitlar qo'llab-quvvatlanishi aniq ko'rsatilgan.

## Hisob darajasidagi metodlar (`ProfileClient`)

```java
import uz.checkout.sdk.ProfileClient;

ProfileClient profile = new ProfileClient("profil_api_kalitingiz");
JSONObject shops = profile.getShops();
JSONObject stats = profile.getProfileStats(Map.of("dateFrom", "2026-01-01"));
```

## Xatoliklarni qayta ishlash

Har bir metod muvaffaqiyatsizlikda `CheckoutApiException` (`RuntimeException`) chiqaradi:

```java
try {
    client.v2.refundPayment(123);
} catch (CheckoutApiException e) {
    System.out.println(e.getStatusCode() + " " + e.getMessage());
}
```

## To'liq API hujjatlari

- v1: https://checkout.uz/api-docs/v1
- v2: https://checkout.uz/api-docs/v2

## Ishlab chiqish (development)

```bash
mvn test
```

Barcha testlar HTTP transportini soxtalashtiradi (`FakeTransport`) — hech biri haqiqiy checkout.uz API'siga murojaat qilmaydi.

## Litsenziya

MIT
