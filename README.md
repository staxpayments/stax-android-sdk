# Stax Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.staxpayments/cardpresent)](https://central.sonatype.com/artifact/com.staxpayments/cardpresent)

The Stax Android SDK takes card-present payments with a Stax card reader and tokenizes card and bank payment methods. Your app sends the token to your server, so your server never receives card or bank numbers.

* [Requirements](#requirements)
* [Installation](#installation)
* [Getting Started](#getting-started)
* [Testing](#testing)
* [Taking a payment with a card reader](cardpresent/README.md)

## <a name="requirements">Requirements</a>

* Android API 29 (Android 10) or later

## <a name="installation">Installation</a>

The SDK is on Maven Central as `com.staxpayments:cardpresent`.

1. Make sure that `mavenCentral()` is in your repositories.

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

2. Add the SDK to your module's `build.gradle.kts`.

```kotlin
dependencies {
    implementation("com.staxpayments:cardpresent:2.7.5")
}
```

## <a name="getting-started">Getting Started</a>

To accept a payment, your app collects the payment details from the customer and tokenizes them. Then your app sends the token to your server, and your server uses the Stax API to run the transaction.

The samples use these imports. The SDK has 2 classes named `Omni`. Import `com.fattmerchant.android.Omni`.

```kotlin
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.Environment
import com.fattmerchant.omni.data.models.BankAccount
import com.fattmerchant.omni.data.models.CreditCard
```

Java also needs `import kotlin.Unit;`.

#### Initialize

Create an ephemeral API key on your server for each session. The SDK does not store the key.

##### Kotlin
```kotlin
val params = InitParams(
    appContext = applicationContext,
    application = application,
    apiKey = ephemeralApiKey,
    environment = Environment.LIVE
)

Omni.initialize(params, completion = {
    // Omni is ready. Use Omni.shared() from here.
}, error = { error ->
    // Omni could not start. error.message is a summary, and error.detail has the cause.
})
```

##### Java
```java
InitParams params = new InitParams(
        application,               // appContext
        application,               // application
        ephemeralApiKey,           // apiKey
        Environment.LIVE.INSTANCE, // environment
        "appid"                    // appId
);

Omni.Companion.initialize(params, () -> {
    // Omni is ready. Use Omni.Companion.shared() from here.
    return Unit.INSTANCE;
}, error -> {
    // Omni could not start. error.getMessage() is a summary, and error.getDetail() has the cause.
    return Unit.INSTANCE;
}, null);
```

#### Collect the payment details

Put the customer's details in a `CreditCard` or a `BankAccount`.

##### Kotlin
```kotlin
val creditCard = CreditCard(
    personName = "Joan Parsnip",
    cardNumber = "4111111111111111",
    cardExp = "1230",
    addressZip = "32822"
)

val bankAccount = BankAccount(
    personName = "Jim Parsnip",
    bankType = "savings",
    bankAccount = "9876543210",
    bankRouting = "021000021",
    addressZip = "32822"
)
```

##### Java

Java must pass every constructor argument. Pass `null` for an optional field that you do not have.

```java
CreditCard creditCard = new CreditCard(
        "Joan Parsnip",     // personName
        "4111111111111111", // cardNumber
        "1230",             // cardExp
        "32822",            // addressZip
        null,               // address1
        null,               // address2
        null,               // addressCity
        null,               // addressState
        null,               // customerId
        null,               // note
        null,               // phone
        null                // email
);

BankAccount bankAccount = new BankAccount(
        "Jim Parsnip",      // personName
        "savings",          // bankType
        "business",         // bankHolderType
        "9876543210",       // bankAccount
        "021000021",        // bankRouting
        "32822",            // addressZip
        null,               // bankName
        null,               // address1
        null,               // address2
        null,               // addressCity
        null,               // addressState
        null,               // customerId
        null,               // note
        null,               // phone
        null                // email
);
```

#### Get a payment method token

Call `tokenize` with the card or the bank account. The completion receives a `PaymentMethod`, and its `id` is the token.

##### Kotlin
```kotlin
Omni.shared()?.tokenize(creditCard, { paymentMethod ->
    // Send paymentMethod.id to your server.
}) { error ->
    // Tokenization failed. error.message is a summary, and error.detail has the cause.
}
```

##### Java
```java
Omni omni = Omni.Companion.shared();
if (omni != null) {
    omni.tokenize(creditCard, paymentMethod -> {
        // Send paymentMethod.getId() to your server.
        return Unit.INSTANCE;
    }, error -> {
        // Tokenization failed. error.getMessage() is a summary, and error.getDetail() has the cause.
        return Unit.INSTANCE;
    });
}
```

#### Use the token

Your server receives the token and creates a transaction with the Stax API.

## <a name="testing">Testing</a>

To try tokenization without real payment details, use the test card or the test bank account.

##### Kotlin
```kotlin
val creditCard = CreditCard.testCreditCard()
val bankAccount = BankAccount.testBankAccount()
```

##### Java
```java
CreditCard creditCard = CreditCard.Companion.testCreditCard();
BankAccount bankAccount = BankAccount.Companion.testBankAccount();
```

To test a failure, use the failing test card or the failing test bank account.

##### Kotlin
```kotlin
val failingCreditCard = CreditCard.failingTestCreditCard()
val failingBankAccount = BankAccount.failingTestBankAccount()
```

##### Java
```java
CreditCard failingCreditCard = CreditCard.Companion.failingTestCreditCard();
BankAccount failingBankAccount = BankAccount.Companion.failingTestBankAccount();
```

You can also use these test numbers in a `CreditCard` or a `BankAccount`.

#### Credit card numbers

| Card Type | Good Card | Bad Card |
|---------|--------------------|-----------|
|VISA|4111111111111111|4012888888881881|
|Mastercard|5555555555554444|5105105105105100|
|Amex|378282246310005|371449635398431|
|Discover|6011111111111117|6011000990139424|
|JCB|3569990010030400|3528327757705979|
|Diners Club|30569309025904|30207712915383|

#### Bank routing and account numbers

* Routing: 021000021
* Account: 9876543210

To test a failing bank account, use this routing number with any other account number.
