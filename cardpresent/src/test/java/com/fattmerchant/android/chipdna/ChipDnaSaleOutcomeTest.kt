package com.fattmerchant.android.chipdna

import com.creditcall.chipdnamobile.ParameterValues
import org.junit.Assert.assertEquals
import org.junit.Test

class ChipDnaSaleOutcomeTest {
    @Test fun approvedIsApproved() =
        assertEquals(SaleOutcome.APPROVED, saleOutcome(ParameterValues.Approved))

    @Test fun declinedIsNotApproved() =
        assertEquals(SaleOutcome.NOT_APPROVED, saleOutcome(ParameterValues.Declined))

    @Test fun unknownIsResultNotKnown() =
        assertEquals(SaleOutcome.RESULT_NOT_KNOWN, saleOutcome(ParameterValues.Unknown))

    @Test fun nullIsNotApproved() =
        assertEquals(SaleOutcome.NOT_APPROVED, saleOutcome(null))

    @Test fun partiallyApprovedKeepsTodaysBehavior() =
        assertEquals(SaleOutcome.NOT_APPROVED, saleOutcome(ParameterValues.PartiallyApproved))
}
