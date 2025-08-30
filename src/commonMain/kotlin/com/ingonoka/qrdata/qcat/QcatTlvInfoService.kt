/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.qcat

import com.ingonoka.qrdata.transcode.*

/**
 * TLV Info provider for QCAT tags.
 */
class QcatTlvInfoService : TlvInfoService {

    override fun getTlvInfoOrNull(tag: Int, context: Context): TlvInfo? =
        getTlvInfo(tag, context).getOrElse { null }

    override fun getTlvInfo(tag: Int, context: Context): Result<TlvInfo> = runCatching {

        when {
            context == Context("APPLICATION TEMPLATE_QCAT01") -> when (tag) {
                0x4F -> TlvInfo(0x4F, "ADF Name", false, List<TLV>::asciiStringifier)
                0x5A -> TlvInfo(0x5A, "Application PAN", false, List<TLV>::asciiStringifier)
                0x63 -> TlvInfo(0x63, "Application Specific Transparent Template", true, null)
                else -> null
            }

            context == Context("APPLICATION SPECIFIC TRANSPARENT TEMPLATE_QCAT01") -> when (tag) {
                0xC1 -> TlvInfo(0xC1, "Ticket ID", false, List<TLV>::numberStringifier)
                0xC2 -> TlvInfo(0xC2, "Ticket Creator ID", false, List<TLV>::uniqueIdStringifier)
                0xC3 -> TlvInfo(0xC3, "Creation Time", false, List<TLV>::timeStampStringifier)
                0xC4 -> TlvInfo(0xC4, "Validity Period", false, List<TLV>::durationStringifier)
                0xC5 -> TlvInfo(0xC5, "Validity Domain", false, List<TLV>::uniqueIdStringifier)
                0xC6 -> TlvInfo(0xC6, "Operator", false, List<TLV>::uniqueIdStringifier)
                0xC7 -> TlvInfo(0xC7, "Effective Time", false, List<TLV>::timeStampStringifier)
                0xC8 -> TlvInfo(0xC8, "Refresh Time", false, List<TLV>::timeStampStringifier)
                0xC9 -> TlvInfo(0xC9, "Ticket Type", false, List<TLV>::uniqueIdStringifier)
                0xCA -> TlvInfo(0xCA, "Account ID", false, List<TLV>::asciiStringifier)
                0xCB -> TlvInfo(0xCB, "Boarding Station", false, List<TLV>::uniqueIdStringifier)
                0xCC -> TlvInfo(0xCC, "Destination Station", false, List<TLV>::uniqueIdStringifier)
                0xCD -> TlvInfo(0xCD, "Vehicle ID", false, List<TLV>::asciiStringifier)
                0xCE -> TlvInfo(0xCE, "Route ID", false, List<TLV>::asciiStringifier)
                0xCF -> TlvInfo(0xCF, "Seat Number", false, List<TLV>::asciiStringifier)
                0xD0 -> TlvInfo(0xD0, "Seat Class", false, List<TLV>::asciiStringifier)
                0xD1 -> TlvInfo(0xD1, "Max Auth Amount", false, List<TLV>::pesoStringifier)
                0xD2 -> TlvInfo(0xD2, "Signature key ID", false, List<TLV>::asciiStringifier)
                0xD3 -> TlvInfo(0xD3, "Terminal ID", false, List<TLV>::asciiStringifier)
                0xD4 -> TlvInfo(0xD4, "Funding Source Type", false, List<TLV>::uniqueIdStringifier)
                0xD5 -> TlvInfo(0xD5, "Funding Source Provider", false, List<TLV>::numberStringifier)
                0xDE -> TlvInfo(0xDE, "Signature", false, List<TLV>::qcatSignatureStringifier)

                else -> null
            }

            else -> throw NoSuchElementException(
                "${this::class.simpleName} does not provide info for context: ${context.name}"
            )
        } ?: throw NoSuchElementException(
            "${this::class.simpleName} does not provide info for tag $tag in context:${context.name}"
        )
    }

    override fun isTlvInfoProviderFor(standard: Standard, context: Context): Boolean =
        (standard == Standard.EMV_CUSTOMER) &&
                context in listOf(
            Context("APPLICATION SPECIFIC TRANSPARENT TEMPLATE_QCAT01"),
            Context("APPLICATION TEMPLATE_QCAT01")
        )
}