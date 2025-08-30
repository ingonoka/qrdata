/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.qrph

import com.ingonoka.qrdata.transcode.*

/**
 * TLV Info provider for Alipay.
 */
class QrPhTlvInfoService : TlvInfoService {

    override fun getTlvInfoOrNull(tag: Int, context: Context): TlvInfo? =
        getTlvInfo(tag, context).getOrElse { null }

    override fun getTlvInfo(tag: Int, context: Context): Result<TlvInfo> = runCatching {
        when {

            context.name == "PROPRIETARY_MERCHANT_INFO_ph.ppmi.p2m" -> when (tag) {
                1 -> TlvInfo(1, "Account SWIFT code", false, List<TLV>::asciiStringifier)
                3 -> TlvInfo(3, "Account Number", false, List<TLV>::asciiStringifier)
                else -> TlvInfoProprietary
            }


            else -> throw NoSuchElementException(
                "${this::class.simpleName} does not provide info for context: ${context.name}"
            )
        }
    }


    override fun isTlvInfoProviderFor(standard: Standard, context: Context): Boolean =
        (standard == Standard.EMV_MERCHANT) && context in listOf(
            Context("PROPRIETARY_MERCHANT_INFO_ph.ppmi.p2m")
        )

}