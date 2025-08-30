/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.qrdata.utils.NullReporter
import com.ingonoka.qrdata.utils.Reporter
import com.ingonoka.utils.ReadBuffer
import com.ingonoka.utils.WriteBuffer
import com.ingonoka.utils.buffer
import com.ingonoka.utils.collectExceptionMessages

private val merchantInfoTags = 26..51
private const val additionalDataFieldTemplateTag = 62
private const val merchantInfoLanguageTemplateTag = 64
private const val unreservedTemplateTag = 80
private val emvMpTemplateTags = listOf(
    merchantInfoTags, additionalDataFieldTemplateTag, merchantInfoLanguageTemplateTag,
    unreservedTemplateTag
)

/**
 * The EMV MP encoding has no mechanism to determine from the value of the [tag] whether the value
 * represents a template or a simple value.  Therefore, we have to check and explicit list
 * with this function.
 *
 */
internal fun isMerchantEmvTemplateTag(tag: Int) = emvMpTemplateTags.any {
    when (it) {
        is Int -> tag == it
        is IntRange -> tag in it
        else -> throw IllegalStateException()
    }
}

/**
 * Read bytes from the [ReadBuffer] and interpret as EMV MP encoded list of TLVs. The [reporter]
 * may contain additional errors or warnings.
 *
 * Tags are encoded as two numerical characters UTF-8 encoded in two bytes. Values go from `00` to `99`.
 *
 * Length is encoded the same as tags.
 *
 * The value of a simple TLV is a string of UTF-8 encoded ascii characters.
 *
 * The value of a template contains TLV objects.
 *
 * @see isMerchantEmvTemplateTag
 *
 */
fun ReadBuffer.decodeEmvMpToTlvStructure(reporter: Reporter = NullReporter): Result<List<TLV>> =
    runCatching {

        val tagAndValue: MutableList<TLV> = mutableListOf()

        while (hasBytesLeftToRead()) {

            val pos = position

            val tag = peekByteArray(2)
                .mapCatching {
                    it.decodeToString().toInt()
                }
                .onSuccess { seekBy(2) }
                .onFailure {
                    reporter.addReport("Reading tag at position: $pos")
                    reporter.addReports(it.collectExceptionMessages(true))
                }
                .getOrNull() ?: break

            val length = peekByteArray(2)
                .mapCatching {
                    it.decodeToString().toInt()
                }
                .onSuccess { seekBy(2) }
                .onFailure {
                    reporter.addReport("Reading length at position: $position")
                    reporter.addReports(it.collectExceptionMessages(true))
                    seekTo(pos)
                }
                .getOrNull() ?: break

            val value = peekByteArray(length)
                .onFailure {
                    reporter.addReport("Reading value at position: $position")
                    reporter.addReports(it.collectExceptionMessages(true))
                }
                .getOrNull() ?: break

            val isConstructed = isMerchantEmvTemplateTag(tag)

            val children = if (isConstructed) {
                value.buffer().decodeEmvMpToTlvStructure(reporter)
                    .onFailure {
                        reporter.addReports(it.collectExceptionMessages(true))
                        seekTo(pos)
                    }
                    .onSuccess {
                        seekBy(length)
                    }
                    .recoverCatching { listOf() }
                    .getOrNull() ?: break
            } else {
                seekBy(length)
                listOf()
            }
            tagAndValue.add(TLV(tag, length, value, children))
        }

        tagAndValue

    }

/**
 * Encode a [TLV] structure into a byte array wrapped into a [ReadBuffer] using
 * EMV merchant-presented encoding rules.
 *
 * @see decodeEmvMpToTlvStructure
 */
fun List<TLV>.encodeToEmvMp(buf: WriteBuffer, reporter: Reporter): Result<ReadBuffer> =
    runCatching {

        forEach { tlv ->
            if (tlv.tag > 99) {
                throw IllegalArgumentException(
                    "Tag larger than 99 cannot be encoded using EMV MP rules. Was: ${tlv.tag}"
                )
            } else {
                buf.write(tlv.tag.toString(10).padStart(2, '0'))
            }
            buf.write(tlv.tag.toString(10).padStart(2, '0'))
            if (tlv.length > 99) {
                throw IllegalArgumentException(
                    "Length larger than 99 cannot be encoded using EMV MP rules. Was: ${tlv.length}"
                )
            } else {
                buf.write(tlv.length.toString(10).padStart(2, '0'))
            }
            if (tlv.children.isNotEmpty()) {
                tlv.children.encodeToEmvMp(buf, reporter)
            } else {
                if (tlv.length != tlv.value.size) {
                    throw IllegalArgumentException(
                        "Size of value not the same as TLV length. Was size ${tlv.value.size} vs. ${tlv.length}"
                    )
                } else {
                    buf.write(tlv.value)
                }
            }
        }

        buf.toReadBuffer()

    }
