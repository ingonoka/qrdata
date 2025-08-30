/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

@file:Suppress("RedundantSuppression", "RedundantSuppression", "RedundantSuppression", "RedundantSuppression",
    "RedundantSuppression", "RedundantSuppression", "RedundantSuppression", "RedundantSuppression",
    "RedundantSuppression", "RedundantSuppression", "RedundantSuppression", "RedundantSuppression",
    "RedundantSuppression", "RedundantSuppression", "RedundantSuppression"
)

package com.ingonoka.qrdata.transcode

import com.ingonoka.qrdata.utils.Reporter
import com.ingonoka.utils.BufferImpl
import com.ingonoka.utils.buffer
import com.ingonoka.utils.collectExceptionMessages
import com.ingonoka.utils.formatTime
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.seconds

@Suppress( "UNUSED_PARAMETER", "UnusedReceiverParameter")
/**
 * Interpret value of TLV as [Long] which is the number of epoch seconds.
 * Format as Date/Time according to `yyyy-MM-dd'T'HH:mm:ssxx`.
 *
 * @see formatTime
 * @return string representation of this timestamp
 */
internal fun List<TLV>.timeStampStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    Instant.fromEpochSeconds(BufferImpl.wrap(tlv.value).readLong(tlv.value.size).getOrThrow())
        .formatTime(TimeZone.of("+08:00"))

/**
 * Interpret value of TLV as [Long] which is the number of seconds.
 * Format as Duration according to ISO standard.
 *
 * @return ISO-8601 based string representation of this duration
 */
@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
internal fun List<TLV>.durationStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    BufferImpl.wrap(tlv.value)
        .readLong(tlv.value.size)
        .map { it.seconds }
        .onFailure { reporter.addReport("${it::class.simpleName}: ${it.message ?: ""}") }
        .map { it.toIsoString() }
        .getOrElse { "" }

/**
 * Interpret value of TLV as [Long] number.
 *
 * @return Base 10 string representation of this number
 */
@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
internal fun List<TLV>.numberStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    BufferImpl.wrap(tlv.value)
        .readLong(tlv.value.size).map { it.toString(10) }
        .onFailure { reporter.addReport("${it::class.simpleName}: ${it.message ?: ""}") }
        .getOrElse { "" }

/**
 * Interpret value of TLV as [Long] number, representing
 * centavos.
 *
 * @return return string such as "PHP xxxxx.xx"
 */
@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
internal fun List<TLV>.pesoStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    BufferImpl.wrap(tlv.value)
        .readLong(tlv.value.size)
        .map {
            val pesos = "${it / 100}"
            val centavos = "${it % 100}".padStart(2, '0')
            "PHP $pesos.$centavos"
        }
        .onFailure { reporter.addReport("${it::class.simpleName}: ${it.message ?: ""}") }
        .getOrElse { "" }

@Suppress("unused", "UnusedReceiverParameter")
internal fun List<TLV>.mccStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    merchantCategories[tlv.value.decodeToString().toInt()] ?: "Unknown"

@Suppress("unused", "UnusedReceiverParameter")
internal fun List<TLV>.currencyStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    currencies[tlv.value.decodeToString().toInt()]?.name ?: "Unknown"

@Suppress("unused", "UnusedReceiverParameter")
internal fun List<TLV>.countryStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String =
    tlv.value.decodeToString().let { twoLetter ->
        countryCodes.values.find {
            it.twoLetter == twoLetter
        }
    }?.name ?: "Unknown"

@Suppress("unused", "UnusedReceiverParameter")
internal fun List<TLV>.largeIntegerStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String {
    val bigInt = BigInteger.fromByteArray(tlv.value, Sign.POSITIVE)
    return bigInt.toString()
}

@Suppress("unused", "UnusedReceiverParameter")
internal fun List<TLV>.tlvStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String = tlv.value.buffer()
    .decodeToTlvStructure(Encoding.BER, reporter)
    .onFailure { reporter.addReports(it.collectExceptionMessages()) }
    .mapCatching {
        it.stringify(
            "", Standard.ASN1, Context("ROOT"), hexDump = false, rawTlv = false,
            reporter
        )
    }
    .getOrElse { it.toString() }