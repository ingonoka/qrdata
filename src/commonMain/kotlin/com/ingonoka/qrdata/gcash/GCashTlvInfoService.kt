/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.gcash

import com.ingonoka.qrdata.transcode.*

/**
 * TLV Info provider for GCash.
 */
class GCashTlvInfoService : TlvInfoService {

    override fun getTlvInfoOrNull(tag: Int, context: Context): TlvInfo? =
        getTlvInfo(tag, context).getOrElse { null }

    override fun getTlvInfo(tag: Int, context: Context): Result<TlvInfo> = runCatching {
        when {

            context.name == ("APPLICATION TEMPLATE_GCASH") -> when (tag) {
                0x4F -> TlvInfo(0x4F, "ADF Name", false, List<TLV>::asciiStringifier)
                0x5A -> TlvInfo(0x5A, "Application PAN", false, List<TLV>::asciiStringifier)
                0x63 -> TlvInfo(0x63, "Application Specific Transparent Template", true, null)
                else -> null

            }

            context.name == "APPLICATION SPECIFIC TRANSPARENT TEMPLATE_GCASH" -> when (tag) {
                0xC1 -> TlvInfo(0xC1, "Ticket ID", false, List<TLV>::asciiStringifier)
                0xC2 -> TlvInfo(0xC2, "Ticket Creator ID", false, List<TLV>::uniqueIdStringifier)
                0xC4 -> TlvInfo(0xC4, "Validity Period", false, List<TLV>::timeStampStringifier)
                0xC8 -> TlvInfo(0xC8, "Refresh Time", false, List<TLV>::timeStampStringifier)
                0xDE -> TlvInfo(0xDE, "Signature", false, List<TLV>::tlvStringifier)
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
        (standard == Standard.EMV_CUSTOMER) && context in listOf(
            Context("APPLICATION SPECIFIC TRANSPARENT TEMPLATE_GCASH"),
            Context("APPLICATION TEMPLATE_GCASH")
        )

}