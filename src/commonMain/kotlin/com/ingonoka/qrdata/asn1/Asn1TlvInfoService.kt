/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.asn1

import com.ingonoka.qrdata.transcode.*

class Asn1TlvInfoService : TlvInfoService {

    override fun getTlvInfoOrNull(tag: Int, context: Context): TlvInfo? =
        getTlvInfo(tag, context).getOrElse { null }

    override fun isTlvInfoProviderFor(standard: Standard, context: Context): Boolean = standard == Standard.ASN1

    override fun getTlvInfo(tag: Int, context: Context): Result<TlvInfo> = runCatching {

        when (tag) {
            2 -> TlvInfo(2, "Number", false, List<TLV>::largeIntegerStringifier)
            0x30 -> TlvInfo(0x30, "Sequence", true, null)
            else -> throw NoSuchElementException(
                "${this::class.simpleName} does not provide info for tag $tag in context:${context.name}"
            )
        }
    }

}