package com.fattmerchant.android.chipdna

import com.creditcall.chipdnamobile.ParameterValues

internal enum class SaleOutcome { APPROVED, NOT_APPROVED, RESULT_NOT_KNOWN }

internal fun saleOutcome(transactionResult: String?): SaleOutcome = when (transactionResult) {
    ParameterValues.Approved -> SaleOutcome.APPROVED
    ParameterValues.Unknown -> SaleOutcome.RESULT_NOT_KNOWN
    else -> SaleOutcome.NOT_APPROVED
}
