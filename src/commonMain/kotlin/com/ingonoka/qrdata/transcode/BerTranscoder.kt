/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.qrdata.utils.Reporter
import com.ingonoka.utils.ReadBuffer
import com.ingonoka.utils.WriteBuffer

/**
 * Decode the byte array wrapped into a [ReadBuffer] into a [TLV] Structure using
 * ASN.1 BER encoding rules.
 */
fun ReadBuffer.decodeBerToTlvStructure(reporter: Reporter): Result<List<TLV>> = runCatching {

    val tagAndValue: MutableList<TLV> = mutableListOf()

    while (hasBytesLeftToRead()) {

        val tag = readIntByte().getOrThrow()

        val length = readTlvLength().getOrThrow()

        val value = peekByteArray(length).getOrThrow()

        val isConstructed = (tag and 0x20) == 0x20

        val children = if (isConstructed) {
            decodeBerToTlvStructure(reporter).getOrThrow()
        } else {
            seekBy(length)
            listOf()
        }
        tagAndValue.add(TLV(tag, length, value, children))
    }

    tagAndValue

}

/**
 * Encode a [TLV] Structure into the byte array wrapped into a [ReadBuffer]  using
 * ASN.1 BER encoding rules.
 */
fun List<TLV>.encodeToBer(buf: WriteBuffer, reporter: Reporter): Result<ReadBuffer> = runCatching {

    forEach { tlv ->
        buf.write(tlv.tag, 0)
        buf.writeTlvLength(tlv.length)
        if(tlv.children.isNotEmpty()) {
            tlv.children.encodeToBer(buf, reporter).getOrThrow()
        } else {
            buf.write(tlv.value)
        }
    }

    buf.toReadBuffer()
}
