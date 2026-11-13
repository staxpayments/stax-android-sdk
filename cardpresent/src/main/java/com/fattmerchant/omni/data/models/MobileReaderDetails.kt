package com.fattmerchant.omni.data.models

open class MobileReaderDetails : Model {
    override var id: String? = null
    open var nmi: NMIDetails? = null

    class NMIDetails {
        var securityKey: String = ""
    }
}
