/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.emv

import com.ingonoka.qrdata.transcode.Context
import com.ingonoka.qrdata.transcode.Standard
import com.ingonoka.qrdata.transcode.TLV
import com.ingonoka.qrdata.transcode.TlvInfo
import com.ingonoka.qrdata.transcode.TlvInfoService
import com.ingonoka.qrdata.transcode.asciiStringifier
import com.ingonoka.qrdata.transcode.countryStringifier
import com.ingonoka.qrdata.transcode.currencyStringifier
import com.ingonoka.qrdata.transcode.emvCrcStringifier
import com.ingonoka.qrdata.transcode.internalPayloadFormatStringifier
import com.ingonoka.qrdata.transcode.mccStringifier

/**
 * TLV Info provider for EMV Merchant-presented tags.
 */
class EmvMpTlvInfoService : TlvInfoService {

    override fun isTlvInfoProviderFor(standard: Standard, context: Context): Boolean =
        (standard == Standard.EMV_MERCHANT) &&
                context in listOf(
                    Context("ROOT"),
                    Context("PROPRIETARY MERCHANT INFO TEMPLATE"),
                    Context("UNRESERVED TEMPLATE"),
                    Context("ADDITIONAL DATA FIELD TEMPLATE"),

                )

    override fun getTlvInfo(tag: Int, context: Context): Result<TlvInfo> = runCatching {

        when {
            context == Context("ROOT") -> when (tag) {
                0 -> TlvInfo(tag, "Payload Format Indicator", false, List<TLV>::internalPayloadFormatStringifier)
                1 -> TlvInfo(tag, "Point of Initiation Method", false, List<TLV>::pointOfInitiationStringifier)
                in 26..51 -> TlvInfo(tag, "Proprietary Merchant Account Information", true, null)
                52 -> TlvInfo(tag, "Merchant Category Code", false, List<TLV>::mccStringifier)
                53 -> TlvInfo(tag, "Transaction Currency", false, List<TLV>::currencyStringifier)
                58 -> TlvInfo(tag, "Country Code", false, List<TLV>::countryStringifier)
                59 -> TlvInfo(tag, "Merchant Name", false, List<TLV>::asciiStringifier)
                60 -> TlvInfo(tag, "Merchant City", false, List<TLV>::asciiStringifier)
                61 -> TlvInfo(tag, "Postal Code", false, List<TLV>::asciiStringifier)
                62 -> TlvInfo(tag, "Additional Data Field Template", true, null)
                63 -> TlvInfo(tag, "CRC", false, List<TLV>::emvCrcStringifier)
                in 80..99 -> TlvInfo(tag, "Unreserved Template", true, null)
                else -> throw NoSuchElementException(
                    "${this::class.simpleName} does not provide info for tag $tag in context:${context.name}"
                )
            }

            context.name == "PROPRIETARY MERCHANT INFO TEMPLATE" -> when (tag) {
                0 -> TlvInfo(0, "Globally Unique Identifier", false, List<TLV>::asciiStringifier)
                else -> throw NoSuchElementException(
                    "${this::class.simpleName} does not provide info for tag $tag in context:${context.name}"
                )
            }

            context.name == "UNRESERVED TEMPLATE" -> when (tag) {
                0 -> TlvInfo(0, "Globally Unique Identifier", false, List<TLV>::asciiStringifier)
                else -> throw NoSuchElementException(
                    "${this::class.simpleName} does not provide info for tag $tag in context:${context.name}"
                )
            }

//            context.name.startsWith("PROPRIETARY_MERCHANT_INFO_") -> when (id) {
//                0 -> TlvInfo(0, "Globally Unique Identifier", false, List<TLV>::asciiStringifier)
//                else -> TlvInfoProprietary
//            }

            context.name == "ADDITIONAL DATA FIELD TEMPLATE" -> when (tag) {
                0 -> TlvInfo(0, "Unique Id", false, List<TLV>::asciiStringifier)
                3 -> TlvInfo(3, "Store Label", false, List<TLV>::asciiStringifier)
                5 -> TlvInfo(5, "Reference Label", false, List<TLV>::asciiStringifier)
                7 -> TlvInfo(7, "Terminal Label", false, List<TLV>::asciiStringifier)
                else -> throw NoSuchElementException(
                    "${this::class.simpleName} does not provide info for tag $tag in context:${context.name}"
                )
            }


            else -> throw NoSuchElementException(
                "${this::class.simpleName} does not provide info for context: ${context.name}"
            )
        }
    }

    override fun getTlvInfoOrNull(tag: Int, context: Context): TlvInfo? = getTlvInfo(tag, context).getOrElse { null }

}