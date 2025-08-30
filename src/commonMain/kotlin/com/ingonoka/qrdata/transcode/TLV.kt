/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.hexutils.toHex
import com.ingonoka.hexutils.toHexChunked1
import com.ingonoka.qrdata.crypto.Crc
import com.ingonoka.qrdata.crypto.versionToAlgorithmName
import com.ingonoka.qrdata.qcat.qcatFundingSourceTypes
import com.ingonoka.qrdata.qcat.qcatTicketTypes
import com.ingonoka.qrdata.qcat.qcatValidityDomain
import com.ingonoka.qrdata.utils.NullReporter
import com.ingonoka.qrdata.utils.Reporter
import com.ingonoka.utils.BufferImpl
import com.ingonoka.utils.buildByteArray
import com.ingonoka.utils.collectExceptionMessages
import kotlin.math.max

/**
 * Representation of a TLV object.
 *
 * @property tag The tag number.
 * @property length The length of the value.
 * @property value The value as [ByteArray]
 * @property children For constructed TLV, the TLV objects of the TLV container.
 */
data class TLV(val tag: Int, val length: Int, val value: ByteArray, val children: List<TLV>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TLV) return false

        if (tag != other.tag) return false
        if (length != other.length) return false
        if (!value.contentEquals(other.value)) return false
        if (children != other.children) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag
        result = 31 * result + length
        result = 31 * result + value.contentHashCode()
        result = 31 * result + children.hashCode()
        return result
    }
}

/**
 * Find the first TLV with [tag] and return [TLV.value] as [ByteArray or null if not found].
 *
 * Goes down into the tree of constructed TLVs
 */
internal fun List<TLV>.findValueForTag(tag: Int): ByteArray? {
    forEach { tlv ->
        if (tlv.tag == tag) return tlv.value
        if (tlv.children.isNotEmpty()) {
            val result = tlv.children.findValueForTag(tag)
            if (result != null) return result
        }
    }
    return null
}

/**
 * Apply [block] to every TLV object in the list.
 */
internal fun List<TLV>.visitAllTlv(block: List<TLV>.(tlv: TLV) -> Unit) {
    forEach {
        block(it)
        if (it.children.isNotEmpty()) {
            it.children.visitAllTlv(block)
        }
    }
    return
}

/**
 * Find all values for [tag].
 */
internal fun List<TLV>.findAll(tag: Int): List<ByteArray> = buildList {
    this@findAll.visitAllTlv { if (it.tag == tag) add(it.value) }
}

@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
internal fun List<TLV>.internalPayloadFormatStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String = if (tlv.length == 2) {
    "Version ${tlv.value.decodeToString().toInt()}"
} else {
    reporter.addReport(
        "Payload Format Indicator in EMV Merchant QR data must be 2 characters long. Is: ${tlv.length}"
    )
    "Error ${reporter.getMessageNumber()}"
}

@Suppress("SpellCheckingInspection")
internal fun TLV.getUniqueId(
    standard: Standard,
    context: Context,
    reporter: Reporter
): Result<String> =

    try {
        val id = BufferImpl.wrap(value)
            .readInt(value.size)
            .onFailure {
                reporter.addReport(
                    "Unique identifiers must be numbers. Is: ${value.decodeToString()}"
                )
            }
            .getOrThrow()

        val s = when (standard) {

            Standard.EMV_CUSTOMER -> when (context.name) {

                "APPLICATION SPECIFIC TRANSPARENT TEMPLATE_GCASH" -> when (tag) {
                    0xC2 -> when (id) {
                        0x0001 -> "GCash Mobile App"
                        else -> throw Exception(
                            "No name for ID ${id.toHex("0x")} for tag ${
                                tag.toHex(
                                    "0x"
                                )
                            } in $standard/$context"
                        )
                    }

                    else -> throw Exception(
                        "No IDs for tag ${tag.toHex("0x")} in $standard/$context"
                    )
                }

                "APPLICATION SPECIFIC TRANSPARENT TEMPLATE_QCAT01" -> when (tag) {
                    0xC2 -> when (id) {
                        0x0106 -> "AF Payments"
                        else -> throw Exception(
                            "No name for ID ${id.toHex("0x")} for tag ${
                                tag.toHex(
                                    "0x"
                                )
                            } in $standard/$context"
                        )
                    }

                    0xC6 -> when (id) {
                        0x0106 -> "AF Payments"
                        else -> throw Exception(
                            "No name for ID ${id.toHex("0x")} for tag ${
                                tag.toHex(
                                    "0x"
                                )
                            } in $standard/$context"
                        )
                    }

                    0xC5 -> qcatValidityDomain(id).getOrElse {
                        throw Exception(
                            "No name for ID ${id.toHex("0x")} for tag ${
                                tag.toHex("0x")
                            } in $standard/$context"
                        )
                    }

                    0xC9 -> qcatTicketTypes(id).getOrElse {
                        throw Exception(
                            "No name for ID ${id.toHex("0x")} for tag ${
                                tag.toHex("0x")
                            } in $standard/$context"
                        )
                    }

                    0xCB, 0xCC -> when (id) {
                        1 -> "Baclaran"
                        2 -> "EDSA"
                        3 -> "Libertad"
                        4 -> "Gil Puyat"
                        5 -> "Vito Cruz"
                        6 -> "Quirino"
                        7 -> "Pedro Gil"
                        8 -> "UN Avenue"
                        9 -> "Central Terminal"
                        10 -> "Carriedo"
                        11 -> "Doroteo Jose"
                        12 -> "Bambang"
                        13 -> "Tayuman"
                        14 -> "Blumentritt"
                        15 -> "Abad Santos"
                        16 -> "R. Papa"
                        17 -> "5th Avenue"
                        18 -> "Monumento"
                        19 -> "Balintawak"
                        20 -> "Fernando Poe Jr."
                        21 -> "Redemptorist"
                        22 -> "MIA Road"
                        23 -> "PITX (Parañaque Integrated Terminal Exchange)"
                        24 -> "Asiaworld"
                        25 -> "Dr. Santos"
                        else -> throw Exception(
                            "No name for ID ${id.toHex("0x")} for tag ${
                                tag.toHex(
                                    "0x"
                                )
                            } in $standard/$context"
                        )
                    }

                    0xD4 -> qcatFundingSourceTypes(id)
                        .map { it.displayName }.getOrElse {
                            throw Exception(
                                "No name for ID ${id.toHex("0x")} for tag ${
                                    tag.toHex("0x")
                                } in $standard/$context"
                            )
                        }

                    else -> throw Exception(
                        "No IDs for tag ${tag.toHex("0x")} in $standard/$context"
                    )
                }

                else -> throw Exception("No Unique IDs for $context in $standard")
            }

            Standard.EMV_MERCHANT -> throw Exception("No Unique IDs for $standard")
            Standard.ASN1 -> throw UnsupportedOperationException("Do not have unique identifiers for ASN.1")
        }

        Result.success(s)

    } catch (e: Exception) {
        reporter.addReport(e.message ?: e::class.simpleName ?: "Exception")
        Result.failure(e)
    }

@Suppress("UnusedReceiverParameter")
internal fun List<TLV>.uniqueIdStringifier(
    tlv: TLV,
    standard: Standard,
    context: Context,
    reporter: Reporter
): String = tlv.getUniqueId(standard, context, reporter).getOrThrow()

@Suppress("unused")
internal fun List<TLV>.emvCrcStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String {
    val computed = Crc().compute(collectRawDataForClc().toIntArray())
    val actual = tlv.value.decodeToString().toInt(16).toString(16)
    return "Actual: $actual / Computed: ${computed.toString(16)}"
}

@Suppress("unused", "UnusedReceiverParameter")
internal fun List<TLV>.alipayDataStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String {
    val index = tlv.value.indexOfLast { it == '/'.code.toByte() }
    val (participant, account) = if (index != -1) {
        tlv.value.decodeToString().let {
            val splitAt = it.lastIndexOf('/')
            Pair(it.take(splitAt + 1), it.drop(splitAt + 1))
        }
    } else {
        Pair("?", "?")
    }
    return "Participant: $participant / Account: $account"
}

@Suppress("unused", "UnusedReceiverParameter")
internal fun List<TLV>.qcatSignatureStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): String {
    val version = tlv.value.first().toInt()
    return "Version: $version (${versionToAlgorithmName[version]})"
}

typealias StringifyFunction = List<TLV>.(TLV, Standard, Context, Reporter) -> String

@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
fun List<TLV>.asciiStringifier(tlv: TLV, standard: Standard, context: Context, reporter: Reporter) =
    tlv.value.decodeToString()

/**
 * String formatters for the value of a TLV object.
 *
 * The formatters are assigned to ADF names and tags, because the interpretation of the [TLV.value]
 * for the same [TLV.tag] may be different for different ADFs.
 */
@Suppress("UnusedReceiverParameter")
fun List<TLV>.getValueStringifier(
    tlv: TLV, standard: Standard, context: Context, reporter: Reporter
): Result<StringifyFunction> =

    TlvInfoServiceRegistry.getService(standard, context)
        .mapCatching { service ->
            service
                .getTlvInfo(tlv.tag, context)
                .map { it.stringifier }
                .getOrThrow()
        }
        .mapCatching {
            if (it == null) {
                reporter.addReport(
                    "No stringifier for value of tag \"${tlv.tag.toHex("0x")}\" in  $standard: $context"
                )
                throw NoSuchElementException("No stringifier for $standard: $context")
            } else {
                it
            }
        }


/*    val stringifier: StringifyFunction? = when (standard) {

        Standard.EMV_CUSTOMER -> when {
            context.name == "APPLICATION SPECIFIC TRANSPARENT TEMPLATE_GCASH" -> when (tlv.tag) {
                0xC1 -> List<TLV>::asciiStringifier
                0xC2 -> List<TLV>::uniqueIdStringifier
                0xC4 -> List<TLV>::timeStampStringifier
                0xC8 -> List<TLV>::timeStampStringifier
                0xDE -> List<TLV>::tlvStringifier
                else -> null
            }


            context.name == "APPLICATION SPECIFIC TRANSPARENT TEMPLATE_QCAT01" -> when (tlv.tag) {
                0xC1 -> List<TLV>::numberStringifier
                0xC2 -> List<TLV>::uniqueIdStringifier
                0xC4 -> List<TLV>::durationStringifier
                0xC5 -> List<TLV>::uniqueIdStringifier
                0xC3 -> List<TLV>::timeStampStringifier
                0xC6 -> List<TLV>::uniqueIdStringifier
                0xC7 -> List<TLV>::timeStampStringifier
                0xC8 -> List<TLV>::timeStampStringifier
                0xC9 -> List<TLV>::uniqueIdStringifier
                0xCB -> List<TLV>::uniqueIdStringifier
                0xCC -> List<TLV>::uniqueIdStringifier
                0xD1 -> List<TLV>::pesoStringifier
                0xD4 -> List<TLV>::uniqueIdStringifier
                0xDE -> List<TLV>::qcatSignatureStringifier

                else -> null
            }

            context.name == "ROOT" -> when (tlv.tag) {
                0x02 -> List<TLV>::largeIntegerStringifier
                0x85 -> List<TLV>::asciiStringifier
                else -> null
            }

            context.name.startsWith("APPLICATION TEMPLATE") -> when (tlv.tag) {
                0x4F -> List<TLV>::asciiStringifier
                0x5A -> List<TLV>::asciiStringifier
                else -> null
            }

            else -> null
        }

        Standard.EMV_MERCHANT -> when {
            context.name == "ROOT" -> when (tlv.tag) {
                0 -> List<TLV>::internalPayloadFormatStringifier
                1 -> List<TLV>::pointOfInitiationStringifier
                52 -> List<TLV>::mccStringifier
                53 -> List<TLV>::currencyStringifier
                58 -> List<TLV>::countryStringifier
                59 -> List<TLV>::asciiStringifier
                60 -> List<TLV>::asciiStringifier
                61 -> List<TLV>::asciiStringifier
                63 -> List<TLV>::emvCrcStringifier
                else -> null
            }

            context.name == "PROPRIETARY MERCHANT INFO TEMPLATE" -> when (tlv.tag) {
                0 -> List<TLV>::asciiStringifier
                else -> null
            }

            context.name == "UNRESERVED TEMPLATE" -> when (tlv.tag) {
                0 -> List<TLV>::asciiStringifier
                else -> null
            }

            context.name == "UNRESERVED TEMPLATE_com.alipay" -> when (tlv.tag) {
                1 -> List<TLV>::alipayDataStringifier
                else -> null
            }

            context.name.startsWith("PROPRIETARY_MERCHANT_INFO_") -> when (tlv.tag) {
                0 -> List<TLV>::asciiStringifier
                else -> List<TLV>::asciiStringifier
            }

            context.name == "ADDITIONAL DATA FIELD TEMPLATE" -> when (tlv.tag) {
                0 -> List<TLV>::asciiStringifier
                3 -> List<TLV>::asciiStringifier
                5 -> List<TLV>::asciiStringifier
                7 -> List<TLV>::asciiStringifier
                else -> null
            }

            else -> null
        }
    }

    return if (stringifier != null) {
        Result.success(stringifier)
    } else {
        reporter.addReport(
            "No stringifier for value of tag \"${tlv.tag.toHex("0x")}\" in  $standard: $context"
        )
        Result.failure(NoSuchElementException("No stringifier for $standard: $context"))
    }
}*/

internal fun List<TLV>.stringifyValue(
    tlv: TLV,
    standard: Standard,
    context: Context,
    reporter: Reporter
): Result<String> =
    getValueStringifier(tlv, standard, context, reporter).mapCatching {
        it.invoke(this, tlv, standard, context, reporter)
    }

enum class Encoding {
    BER, ASCII
}

enum class Standard {
    ASN1, EMV_CUSTOMER, EMV_MERCHANT
}

const val UNKNOWN_TAG_NAME: String = "No Tag Name"

internal fun getTagName(tag: Int, standard: Standard, context: Context, reporter: Reporter): Result<String> =
    TlvInfoServiceRegistry.getService(standard, context)
        .onFailure {
            reporter.addReports(it.collectExceptionMessages())
        }
        .mapCatching { service ->
            service.getTlvInfo(tag, context)
                .map { it.name }
                .getOrElse { UNKNOWN_TAG_NAME }
        }

private fun StringBuilder.appendTag(tag: Int) {
    this.append("T:")
    this.append(tag.toHex("0x"))
    this.append(",")
    this.append(tag.toString(10).padEnd(3))
}

private fun StringBuilder.appendLength(length: Int) {
    this.append("L:")
    this.append(length.toHex("0x"))
    this.append(",")
    this.append(length.toString(10).padEnd(3))
}

private fun StringBuilder.appendTagName(
    tag: Int, maxNameLength: Int, standard: Standard, context: Context, reporter: Reporter
) {
    getTagName(tag, standard, context, reporter)
        .onSuccess { this.append(it.padEnd(maxNameLength)) }
        .onFailure {
            reporter.addReports(it.collectExceptionMessages())
        }
}

fun TLV.nextContext(standard: Standard, context: Context): Context = when (standard) {
    Standard.EMV_CUSTOMER -> when {
        context.name == "ROOT" -> when (tag) {
            0x61 -> Context("APPLICATION TEMPLATE")
            else -> context
        }

        context.name.startsWith("APPLICATION TEMPLATE") -> when (tag) {
            0x4F -> Context("APPLICATION TEMPLATE_${value.decodeToString()}")
            0x63 -> if (context.name.startsWith("APPLICATION TEMPLATE_")) {
                val adf = context.name.substringAfter('_')
                Context("APPLICATION SPECIFIC TRANSPARENT TEMPLATE_$adf")
            } else {
                Context("APPLICATION SPECIFIC TRANSPARENT TEMPLATE")
            }

            else -> context
        }

        context.name == "APPLICATION SPECIFIC TRANSPARENT TEMPLATE" -> when (tag) {

            else -> context
        }

        context.name.startsWith("APPLICATION SPECIFIC TRANSPARENT TEMPLATE_") -> when (tag) {
            else -> context
        }

        else -> context
    }

    Standard.EMV_MERCHANT -> when {
        context.name == "ROOT" -> when (tag) {
            in 26..51 -> Context("PROPRIETARY MERCHANT INFO TEMPLATE")
            62 -> Context("ADDITIONAL DATA FIELD TEMPLATE")
            80 -> Context("UNRESERVED TEMPLATE")
            else -> context
        }

        context.name == "PROPRIETARY MERCHANT INFO TEMPLATE" -> when (tag) {
            0 -> Context("PROPRIETARY_MERCHANT_INFO_${value.decodeToString()}")
            else -> context
        }

        context.name == "UNRESERVED TEMPLATE" -> when (tag) {
            0 -> Context("UNRESERVED TEMPLATE_${value.decodeToString()}")
            else -> context
        }

        context.name.startsWith("UNRESERVED TEMPLATE_") -> context

        context.name.startsWith("PROPRIETARY_MERCHANT_INFO_") -> context

        context.name == "ADDITIONAL DATA FIELD TEMPLATE" -> context

        else -> context
    }

    Standard.ASN1 -> context
}

private fun List<TLV>.maxNameLength(standard: Standard, context: Context): Int =
    fold(Pair(context, 0)) { acc, tlv ->
        if (tlv.children.isEmpty()) {
            val (currentContext, length) = acc
            val tagName = getTagName(tlv.tag, standard = standard, currentContext, NullReporter)
                .getOrElse { UNKNOWN_TAG_NAME }
            val newLength = max(length, tagName.length)
            val newContext = tlv.nextContext(standard, currentContext)
            Pair(newContext, newLength)
        } else {
            acc
        }
    }.second

fun appendTagLengthName(
    tlv: TLV, standard: Standard, context: Context, rawTlv: Boolean, maxNameLength: Int, reporter: Reporter
) = buildString {
    appendTag(tlv.tag)
    append(' ')
    appendLength(tlv.length)
    append(' ')
    if (!rawTlv) appendTagName(tlv.tag, maxNameLength, standard, context, reporter)
}

fun List<TLV>.collectRawDataForClc(): List<Int> = buildByteArray {
    this@collectRawDataForClc.forEach { tlv ->
        write(tlv.tag.toString(10).padStart(2, '0'))
        write(tlv.length.toString(10).padStart(2, '0'))
        if (tlv.tag != 63) write(tlv.value)
    }
}.getOrThrow()

fun List<TLV>.stringify(
    indent: String = "",
    standard: Standard,
    context: Context,
    hexDump: Boolean = true,
    rawTlv: Boolean = false,
    reporter: Reporter = NullReporter
): String = buildString {

    var currentContext = context

    val maxNameLength = if (!rawTlv) maxNameLength(standard, currentContext) else 0

    this@stringify.forEach { tlv ->
        this.append(indent)
        if (tlv.children.isNotEmpty()) {
            this.appendLine(
                appendTagLengthName(tlv, standard, currentContext, rawTlv, maxNameLength, reporter)
            )
            this.append(
                tlv.children.stringify(
                    "$indent    ",
                    standard,
                    tlv.nextContext(standard, currentContext),
                    hexDump,
                    rawTlv,
                    reporter
                )
            )
        } else {
            val startLength = length
            val addPrintableCharacters = hexDump && !rawTlv && stringifyValue(
                tlv, standard, currentContext, NullReporter
            ).isFailure
            this.append(appendTagLengthName(tlv, standard, currentContext, rawTlv, maxNameLength, reporter))
            if (hexDump) {
                this.append(" => ")
                val valueIndent = "".padEnd(indent.length + length - startLength)
                this.append(
                    tlv.value.toHexChunked1(
                        indent = valueIndent, columns = 8, printable = addPrintableCharacters,
                        lineNums = false,
                        firstLineIndent = false
                    )
                )
            }
            if (!rawTlv) {
                stringifyValue(tlv, standard, currentContext, reporter)
                    .onSuccess {
                        this.append(" => ")
                        val valueIndent = "".padEnd(length - lastIndexOf("\n"))
                        this.append(
                            if (it.contains('\n')) {
                                it.replace("\n", "\n$valueIndent")
                            } else {
                                it
                            }
                        )
                    }
            }
            appendLine()

            currentContext = tlv.nextContext(standard, currentContext)
        }
    }
}
