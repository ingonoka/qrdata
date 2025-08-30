/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.utils.ReadBuffer
import com.ingonoka.utils.WriteBuffer

/**
 * Interpret the bytes in [ReadBuffer] as a BER encoded length component of a TLV object
 *
 * @return Length as [Int]
 */
internal fun ReadBuffer.readTlvLength(): Result<Int> {

    val firstByte = readIntByte().getOrThrow()

    val numBytes = if ((firstByte and 0x80) == 0x80) firstByte and 0x7F else 0

    if (numBytes >= bytesLeftToRead()) return Result.failure(Exception("Byte array too short for length encoding"))

    if (numBytes > 3) return Result.failure(Exception("Number of bytes for TLV length too big. Support up to 3.  Is: $numBytes"))

    val tlvLength = when (numBytes) {
        0 -> firstByte and 0x7F
        1 -> readIntByte().map { it and 0xFF }.getOrThrow()
        2 -> readIntByte().map { it and 0xFF shl 8 }.getOrThrow() or
                readIntByte().map { it and 0xFF }.getOrThrow()

        3 -> readIntByte().map { (it and 0xFF) shl 16 }.getOrThrow() or
                readIntByte().map { it and 0xFF shl 8 }.getOrThrow() or
                readIntByte().map { it and 0xFF }.getOrThrow()

        else -> throw IllegalStateException()
    }

    return Result.success(tlvLength)
}

/**
 * Write the length of a TLV object into [WriteBuffer] using ASN.1 BER rules.
 * Return the number of bytes written, wrapped in a [Result]
 */
internal fun WriteBuffer.writeTlvLength(length: Int): Result<Int> =

    when {
        length < 0 -> Result.failure(Exception("Length of a TLV must be positive. Is $length"))

        length < 0x80 -> write(length, 1)

        length < 0x100 -> {
            write(0x81, 1)
            write(length, 1)
        }

        length < 0x10000 -> {
            write(0x82, 1)
            write(length / 0x100, 1)
            write(length % 0x100, 1)
        }

        length < 0x1000000 -> {
            write(0x83, 1)
            write(length / 0x10000, 1)
            write(length / 0x100, 1)
            write(length % 0x100, 1)
        }

        else -> Result.failure(Exception("Length of a TLV must be less than 0x1000000. Is $length"))
    }