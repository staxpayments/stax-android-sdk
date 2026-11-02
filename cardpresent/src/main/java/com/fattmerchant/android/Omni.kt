package com.fattmerchant.android

import android.content.Context
import com.fattmerchant.omni.AccessoryHelper
import com.fattmerchant.omni.Environment
import com.fattmerchant.omni.UsbAccessoryListener
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.networking.OmniApi
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.launch
import com.fattmerchant.omni.Omni as CommonOmni
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository as CommonMobileReaderDriverRepo

/**
 * Communicates with the Omni platform and bluetooth mobile readers
 *
 * ## Usage
 * ```
 * // First, initialize Omni using InitParams
 * val initParams = InitParams(myAppContext, apiKey)
 * Omni.initialize(initParams, {
 *   // Omni is now initialized and ready!
 *
 * }, { omniException ->
 *
 * )
 * ```
 *
 * Once initialized, you can use methods like `Omni.shared().getAvailableReaders`
 *
 * @see InitParams
 * @property omniApi
 */
class Omni internal constructor(omniApi: OmniApi) : CommonOmni(omniApi) {

    /**
     * Thrown when Omni failed to initialize
     *
     * @param message describes what went wrong
     * */
    class InitializationError(message: String? = null) : Exception(message)

    override var mobileReaderDriverRepository: CommonMobileReaderDriverRepo = MobileReaderDriverRepository()

    companion object {

        private var sharedInstance: Omni? = null

        /**
         * Prepares [Omni] for usage
         *
         * @param params an [InitParams] instance that has all the necessary info for initialization
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(
            params: InitParams,
            completion: () -> Unit,
            error: (OmniException) -> Unit,
            usbListener: UsbAccessoryListener? = null
        ) {
            val paramMap = mutableMapOf(
                "apiKey" to params.apiKey,
                "appContext" to params.appContext,
                "appId" to params.appId
            )

            params.application?.let {
                paramMap["application"] = it
            }

            initialize(
                paramMap,
                params.environment,
                completion,
                error,
                usbListener
            )
        }

        /**
         * Prepares [Omni] for usage
         *
         * @param params a [Map] containing the necessary information to initialize Omni
         * @param completion block to execute once completed
         *
         * @throws InitializationError if you don't pass an apiKey
         */
        fun initialize(
            params: Map<String, Any>,
            environment: Environment,
            completion: () -> Unit,
            error: (OmniException) -> Unit,
            usbListener: UsbAccessoryListener? = null
        ) = initialize(params, environment, completion, error, usbListener, engine = null)

        internal fun initialize(
            params: Map<String, Any>,
            environment: Environment,
            completion: () -> Unit,
            error: (OmniException) -> Unit,
            usbListener: UsbAccessoryListener?,
            engine: HttpClientEngine?
        ) {
            // Init the API
            val omniApi = OmniApi(engine)

            omniApi.environment = environment

            omniApi.token = params["apiKey"] as? String ?: ""

            omniApi.userAgent = sdkUserAgent(params["appContext"] as? Context)

            // Create the shared Omni object
            val omni = Omni(omniApi)
            omni.usbAccessoryListener = usbListener
            sharedInstance = omni

            // Init com.fattmerchant.omni
            omni.coroutineScope.launch {
                omni.initialize(params, {
                    completion()
                }) {
                    error(it)
                }
            }
        }

        fun shared(): Omni? = sharedInstance
    }
}
