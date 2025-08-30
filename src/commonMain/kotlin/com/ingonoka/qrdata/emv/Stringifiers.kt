/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

@file:Suppress("RedundantSuppression", "RedundantSuppression", "RedundantSuppression", "RedundantSuppression",
    "RedundantSuppression", "RedundantSuppression"
)

package com.ingonoka.qrdata.emv

import com.ingonoka.qrdata.transcode.Context
import com.ingonoka.qrdata.transcode.Standard
import com.ingonoka.qrdata.transcode.TLV
import com.ingonoka.qrdata.utils.Reporter

/**
 * Interpret value as integer representing the code for
 * the POI initiation (EMV)
 */
@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
internal fun List<TLV>.pointOfInitiationStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    if (tlv.length == 2) {
        when (val indicator = tlv.value.decodeToString().toInt()) {
            11 -> "Static"
            12 -> "Dynamic"
            else -> {
                reporter.addReport("POI identifier should be '11' or '12'. Is: $indicator")
                "Unknown"
            }
        }
    } else {
        reporter.addReport(
            "POI Indicator in EMV Merchant QR data must be 2 characters long. Is: ${tlv.length}"
        )
        "Error ${reporter.getMessageNumber()}"
    }