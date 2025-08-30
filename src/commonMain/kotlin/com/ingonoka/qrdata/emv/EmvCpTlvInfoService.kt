/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.emv

import com.ingonoka.qrdata.transcode.*

/**
 * TLV Info provider for EMV Customer-presented tags.
 */
class EmvCpTlvInfoService : TlvInfoService {

    override fun getTlvInfoOrNull(tag: Int, context: Context): TlvInfo? =
        getTlvInfo(tag, context).getOrElse { null }

    override fun getTlvInfo(tag: Int, context: Context): Result<TlvInfo> = runCatching {
        when {

            context.name == "ROOT" -> when (tag) {
                0x85 -> TlvInfo(0x85, "EMV Payload Format Indicator", false, List<TLV>::asciiStringifier)
                0x61 -> TlvInfo(0x61, "Application Template", true, null)
                else -> null
            }

            context.name.startsWith("APPLICATION TEMPLATE") -> when (tag) {
                0x4F -> TlvInfo(0x4F, "ADF Name", false, List<TLV>::asciiStringifier)
                0x5A -> TlvInfo(0x5A, "Application PAN", false, List<TLV>::asciiStringifier)
                0x63 -> TlvInfo(0x63, "Application Specific Transparent Template", true, null)
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
                context in listOf(Context("ROOT"), Context("APPLICATION TEMPLATE"))

}