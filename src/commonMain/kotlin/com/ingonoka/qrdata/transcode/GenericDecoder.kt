/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.qrdata.utils.MemoryReporter
import com.ingonoka.qrdata.utils.Reporter
import com.ingonoka.utils.ReadBuffer
import com.ingonoka.utils.WriteBuffer

private fun ReadBuffer.decodeToTlvStructureSample() {

    val reporter: Reporter = MemoryReporter()

    decodeToTlvStructure(Encoding.BER, reporter)
        .onSuccess { tlvTree: List<TLV> -> TODO() }
        .getOrThrow()
}

/**
 * Read from [ReadBuffer] from current position and interpret bytestream as
 * [TLV] structure encoded with [encoding] standard.
 *
 * @sample com.ingonoka.qrdata.transcode.decodeToTlvStructureSample
 */
fun ReadBuffer.decodeToTlvStructure(encoding: Encoding, reporter: Reporter): Result<List<TLV>> =
    runCatching {

        when (encoding) {
            Encoding.BER -> decodeBerToTlvStructure(reporter)
            Encoding.ASCII -> decodeEmvMpToTlvStructure(reporter)
        }.getOrThrow()

    }

/**
 * Encode a [TLV] structure into a list of bytes wrapped into a [ReadBuffer] using
 * [encoding] rules and record any errors or warnings into [reporter]
 *
 * @see encodeToBer
 * @see encodeToEmvMp
 */
fun List<TLV>.encode(buf: WriteBuffer, encoding: Encoding, reporter: Reporter): Result<ReadBuffer> =
    runCatching {

        when (encoding) {
            Encoding.BER -> encodeToBer(buf, reporter)
            Encoding.ASCII -> encodeToEmvMp(buf, reporter)
        }.getOrThrow()

    }


