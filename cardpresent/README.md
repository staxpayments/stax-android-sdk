# Omni Cardpresent


## <a name="installation">Installation</a>

Add `com.staxpayments:cardpresent` from Maven Central. The [root README](../README.md#installation) shows the steps and the current version.


## <a name="getting-started">Getting Started</a>

In order to take mobile reader payments, you will need the following:

* **Ephemeral Omni API Key**: Using your Omni API key, you will need to create an ephemeral token. The cardpresent codebase does not store your token, so you'll need to pass one in every time you create the `Omni` object. The [Stax API docs](https://docs.staxpayments.com) show how to create an ephemeral token
* **App Name**: A name for your app!
* **Mobile Reader**: A Fattmerchant-provided mobile reader


## Initialize

Create an `InitParams` with your app context, your application, and an ephemeral API key. Then call `Omni.initialize`. The [root README](../README.md#getting-started) shows the code in Kotlin and Java.

After the completion runs, use `Omni.shared()` to get the `Omni` instance.

## Pairing the Reader
**Miura M010**

1. Turn the Miura M010 mobile reader on and wait until the display shows "MIURA SYSTEMS" along with a bluetooth indicator.
2. Press and hold the bluetooth indicator button until it flashes rapidly (this lets you know it is in discovery mode)
3. Find the Miura on your Android device's list of bluetooth devices and pair it

**BBPOS Chipper 2XBT**

1. Turn the BBPOS Chipper 2XBT mobile reader on by holding the power button
2. Wait until the LED indicator repeatedly flashes blue (this lets you know it is in discovery mode)
3. In the Android device's list of bluetooth devices, find the BBPOS device. Note that the mobile reader will appear twice; once with a phone icon and another time with a Bluetooth icon. Choose the one with the phone icon and pair.


## Connect a Mobile Reader

After you pair the reader, find it and connect to it with the SDK. These samples use `com.fattmerchant.android.Omni`.

First, search for the available readers.

```kotlin
Omni.shared()?.getAvailableReaders { readers ->
    // readers is a List<MobileReader>
}
```

Then connect to the reader that you choose.

```kotlin
Omni.shared()?.getAvailableReaders { readers ->
    val mobileReader = readers.firstOrNull() ?: return@getAvailableReaders
    Omni.shared()?.connectReader(mobileReader, onConnected = { reader ->
        // The reader is connected.
    }, onFail = { error ->
        // The SDK could not connect the reader. error is a message.
    })
}
```

## Subscribing to Transaction Updates

To receive transaction updates, set a `TransactionUpdateListener` on `Omni` before you take a payment. The listener receives updates such as these:

- `PromptInsertSwipeCard`: the reader waits for a card.
- `CardSwiped` or `CardInserted`: the customer swiped or inserted a card.
- `Authorizing`: the payment is being authorized.
- `PromptProvideSignature`: the payment needs a signature. The SDK sends this update only when you set a `signatureProvider`.

Each update is a `TransactionUpdate` with a `value` and a `userFriendlyMessage`. Use `value` to identify the event. Some updates, such as `CardInserted`, have no `userFriendlyMessage`. If the message is not `null`, you can show it to the user.

```kotlin
Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
        println(transactionUpdate.value)
        transactionUpdate.userFriendlyMessage?.let { message ->
            // Show message to the user.
        }
    }
}
```

## Providing a Signature

If a transaction needs a signature, the SDK calls the `SignatureProviding` that you set on `Omni`. Set it before you take a payment. Give the signature to `completion` as a Base64 string. If you do not set a `signatureProvider`, the SDK continues the payment with no signature.

```kotlin
Omni.shared()?.signatureProvider = object : SignatureProviding {
    override fun signatureRequired(completion: (String) -> Unit) {
        val base64EncodedSignature = captureSignatureAsBase64()
        completion(base64EncodedSignature)
    }
}
```

## Taking a Payment

Set the `transactionUpdateListener` and the `signatureProvider` before you call `takeMobileReaderTransaction`. The SDK reads both when the payment starts. If you change them later, the change does not apply to that payment.

To take a payment, create a `TransactionRequest` and pass it to `takeMobileReaderTransaction`. `Amount(cents = 5000)` is $50.00. The completion also receives a declined payment. Give the goods only when `transaction.success == true`.

```kotlin
val amount = Amount(cents = 5000)
val request = TransactionRequest(amount)

Omni.shared()?.takeMobileReaderTransaction(request, { transaction ->
    if (transaction.success == true) {
        // The payment is approved.
    } else {
        // The payment is declined.
    }
}) { error ->
    // The payment failed. error.message is a summary, and error.detail has the cause.
}
```

## Refunding a Payment

To refund a payment, get the `Transaction` from the [Stax API](https://docs.staxpayments.com). Then call `refundMobileReaderTransaction`.

> You can refund a transaction only on the device that took the payment.

```kotlin
Omni.shared()?.refundMobileReaderTransaction(transaction, { refund ->
    // The refund is approved.
}) { error ->
    // The refund failed. error.message is a summary, and error.detail has the cause.
}
```
