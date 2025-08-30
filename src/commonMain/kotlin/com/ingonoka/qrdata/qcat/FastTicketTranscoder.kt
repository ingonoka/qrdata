/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.qcat

import com.ingonoka.hexutils.toHex
import com.ingonoka.qrdata.crypto.signatureLength
import com.ingonoka.qrdata.transcode.EmvPoiData
import com.ingonoka.qrdata.transcode.Encoding
import com.ingonoka.qrdata.transcode.TLV
import com.ingonoka.qrdata.transcode.decodeToTlvStructure
import com.ingonoka.qrdata.transcode.writeTlvLength
import com.ingonoka.qrdata.utils.Reporter
import com.ingonoka.utils.*
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val emvPayloadIndicator: ByteArray = byteArrayOf(0x85.b, 0x05, 0x43, 0x50, 0x56, 0x30, 0x31)
val qcatPayloadIndicator: ByteArray = byteArrayOf(0x4F, 0x06, 0x51, 0x43, 0x41, 0x54, 0x30, 0x31)

fun ByteArray.decodeTicket(reporter: Reporter) = BufferImpl.wrap(this)
    .decodeToTlvStructure(Encoding.BER, reporter)
    .map {
        it.toTicket().getOrThrow()
    }

fun String.decodeTicket(reporter: Reporter) = BufferImpl.wrap(fromBase64())
    .decodeToTlvStructure(Encoding.BER, reporter)
    .map {
        it.toTicket().getOrThrow()
    }

fun List<TLV>.toTicket(): Result<Ticket> = runCatching {

    var poiData: EmvPoiData?

    var ticketId: Long? = null
    var creatorId: Int? = null
    var creationTimestamp: Instant? = null
    var validityPeriod: Duration? = null

    val validityDomains = mutableListOf<Int>()
    var effectiveTime: Instant? = null
    var refreshTime: Instant? = null
    val ticketTypes = mutableListOf<Int>()
    var accountId: AsciiString? = null
    var boardingStation: Long? = null
    var destinationStation: Long? = null
    var vehicleId: Long? = null
    var routeId: Long? = null
    var seatNumber: AsciiString? = null
    var seatClass: AsciiString? = null
    var maxAuthorizedAmount: Long? = null
    var signatureKeyIdentifier: AsciiString? = null
    var terminalIdentifier: AsciiString? = null
    var signatureVersion: Int? = null
    var qcatSignature: ByteArray? = null
    val operators = ArrayList<Int>()

    var fundingSourceType: Int? = null
    var fundingSourceProvider: AsciiString? = null

    val sequence = this.asIterable().iterator().withIndex()

    sequence.next().value.readEmvPayLoadIndicator().getOrThrow()

    sequence.next().value.readApplicationTemplateTagAndLength().getOrThrow()

    poiData = sequence.readEmvPoiData().getOrThrow()

    sequence.next().value.readTransparentTemplateTagAndLength().getOrThrow()

    done@ while (sequence.hasNext()) {

        val (index, tlv) = sequence.next()
        val tag = tlv.tag
        val length = tlv.length
        val buf = tlv.value.buffer()

        when (tag) {

            0xC1 -> ticketId = buf.readLong(length)
                .getOrElse { throw Exception("Ticket Id read failure.") }

            0xC2 -> creatorId = buf.readInt(length)
                .getOrElse { throw Exception("Creator ID read failure.") }

            0xC3 -> creationTimestamp = buf.readLong(length)
                .map { Instant.fromEpochSeconds(it) }
                .getOrElse { throw Exception("Creation time read failure.") }

            0xC4 -> validityPeriod = buf.readLong(length)
                .map { it.seconds }
                .getOrElse { throw Exception("Validity period read failure.") }

            0xc5 -> validityDomains.add(
                buf.readInt(length).getOrElse { throw Exception("Validity period read failure.") })

            0xC6 -> operators.add(
                buf.readInt(length).getOrElse { throw Exception("Operator ID read failure.") })

            0xC7 -> effectiveTime = buf.readLong(length)
                .map { Instant.fromEpochSeconds(it) }
                .getOrElse { throw Exception("Effective time read failure.") }

            0xC8 -> refreshTime = buf.readLong(length)
                .map { Instant.fromEpochSeconds(it) }
                .getOrElse { throw Exception("Refresh time read failure.") }

            0xC9 -> ticketTypes.add(
                buf.readInt(length).getOrElse { throw Exception("Ticket  type read failure.") })

            0xCA -> accountId = buf.readString(length)
                .getOrElse { throw Exception("Account ID read failure") }

            0xCB -> boardingStation = buf.readLong(length)
                .getOrElse { throw Exception("Boarding station read failure.") }

            0xCC -> destinationStation = buf.readLong(length)
                .getOrElse { throw Exception("Destination station read failure.") }

            0xCD -> vehicleId = buf.readLong(length)
                .getOrElse { throw Exception("Vehicle ID read failure.") }

            0xCE -> routeId = buf.readLong(length)
                .getOrElse { throw Exception("Route ID read failure.") }

            0xCF -> seatNumber = buf.readString(length)
                .getOrElse { throw Exception("Seat Number read failure") }

            0xD0 -> seatClass = buf.readString(length)
                .getOrElse { throw Exception("Seat Class read failure") }

            0xD1 -> maxAuthorizedAmount = buf.readLong(length)
                .getOrElse { throw Exception("Max Auth Amount read failure.") }

            0xD2 -> signatureKeyIdentifier = buf.readString(length)
                .getOrElse { throw Exception("Signature Key ID read failure") }

            0xD3 -> terminalIdentifier = buf.readString(length)
                .getOrElse { throw Exception("Terminal ID read failure") }

            0xD4 -> fundingSourceType = buf.readInt(length)
                .getOrElse { throw Exception("Funding Src type read failure.") }

            0xD5 -> fundingSourceProvider = buf.readString(length)
                .getOrElse { throw Exception("Funding Src Provider read failure") }

            0xDE -> {

//                val lengthOfDataToBeSigned = position - startOfQcatPayload

                signatureVersion = buf.readIntByte()
                    .getOrElse { throw Exception("Signature Version read failure.") }

                qcatSignature = buf.readByteArray(length - 1)
                    .getOrElse { throw Exception("Signature read failure") }

//                seekTo(startOfQcatPayload)
//                val dataToBeSigned =
//                    readByteArray(lengthOfDataToBeSigned).getValueOrThrow { "Data to be signed read failure." }

                println("Signature not verified (creator ID: $creatorId)")


            }

            else -> {
                throw Exception("Unknown tag in QCAT payload data: $tag at index $index")
            }
        }
    }

    if (sequence.hasNext()) {
        throw Exception("Data contains tlv objects after the QCAT signature")
    }

    fun missingMandatedFields(): List<String>? {

        val l = buildList {

            if (ticketId == null) add("Ticket ID")
            if (creatorId == null) add("Creator ID")
            if (creationTimestamp == null) add("Creation Timestamp")
            if (validityPeriod == null) add("Validity Period")
        }

        return l.ifEmpty { null }
    }

    missingMandatedFields()?.apply {
        throw InvalidQcatBerValue(
            "Mandatory field(s) missing: ${joinToString()}"
        )
    }

    Ticket(
        poiData,
        ticketId!!,
        creatorId!!,
        creationTimestamp!!,
        validityPeriod!!,
        if (validityDomains.isNotEmpty()) validityDomains.toList() else null,
        if (operators.isNotEmpty()) operators.toList() else null,
        effectiveTime,
        refreshTime,
        if (ticketTypes.isNotEmpty()) ticketTypes.toList() else null,
        accountId,
        boardingStation,
        destinationStation,
        vehicleId,
        routeId,
        seatNumber,
        seatClass,
        maxAuthorizedAmount,
        signatureKeyIdentifier,
        terminalIdentifier,
        fundingSourceType,
        fundingSourceProvider,
        signatureVersion,
        qcatSignature
    )

}

/**
 * Read the EMV payload indicator from a [ReadIntBuffer
 *
 * @return [Result] without value
 */
internal fun TLV.readEmvPayLoadIndicator(): Result<Unit> = runCatching {

    if (tag != 0x85 || length != 5 ||
        !value.contentEquals(byteArrayOf(0x43, 0x50, 0x56, 0x30, 0x31))
    ) {
        throw Exception(
            """
                    |First TLV object should be an EMV Payload indicator.
                    |Actual tag:  ${tag.toHex()}
                """.trimMargin()
        )
    }
}

/**
 * Read tag and length of Application Template from [ReadBuffer]
 *
 * @return The length of the Application template.
 */
internal fun TLV.readApplicationTemplateTagAndLength(): Result<Int> = runCatching {

    if (tag != 0x61) throw Exception(
        "Application Template  Tag should be 0x61, but is ${tag.toHex()}"
    )

    length

}

/**
 * Read tag and length of Transparent Application Template from [ReadBuffer]
 *
 * @return New position in receiver, which is the start of the value portion of a Transparent Application template.
 */
internal fun TLV.readTransparentTemplateTagAndLength(): Result<Int> = runCatching {

    if (tag != 0x63) throw Exception("Transparent Tag should be 0x63, but is ${tag.toHex("0x")}")
    length

}

internal fun Iterator<IndexedValue<TLV>>.readEmvPoiData(): Result<EmvPoiData> = runCatching {

    var adfName: ByteArray? = null
    var appPan: ByteArray? = null


    while (hasNext()) {

        val tlv: TLV = next().value

        when (tlv.tag) {
            0x4F -> adfName = tlv.value
            0x5A -> appPan = tlv.value
            else -> break
        }
    }

    EmvPoiData(adfName = adfName, appPan = appPan)
}

class InvalidQcatBerValue(msg: String) : Exception(msg)
class InvalidValue(msg: String, e: Throwable) : Exception(msg, e)

/**
 * Encode the ticket into BER encoded string of bytes.
 *
 * If the ticket already contains a signature, then no new signature is created.  The existing signature is not validated.
 *
 * If the repository is `null` then no signature will be created.
 *
 * A new signature is created if all the following is true:
 * - the signatureVersion field is not null
 * - the signature is null or empty
 *
 */
fun Ticket.encode(): Result<ReadBuffer> =

    try {

        val payloadBuffer = BufferImpl.empty()
        writeBerEncodedPayloadInto(payloadBuffer).getOrThrow()

        // Placeholder signature
        val signatureData = Random(0).nextBytes(signatureLength.getOrElse(signatureVersion) { 0 })

        if (signatureVersion != null && signatureData.isNotEmpty()) {
            payloadBuffer.writeBerEncodedSignature(signatureVersion, signatureData).getOrThrow()
        }

        val transparentTemplateBuffer = BufferImpl.empty(2048)
        transparentTemplateBuffer.write(qcatPayloadIndicator)
        transparentTemplateBuffer.writeTlv(0x63, payloadBuffer.toByteArray())


        val completeData = BufferImpl.empty(2048)
        completeData.write(emvPayloadIndicator)
        completeData.writeTlv(0x61, transparentTemplateBuffer.toByteArray())

        Result.success(completeData.toReadBuffer())

    } catch (e: Exception) {

        Result.failure(Exception("Failed to encode ticket.", e))
    }

fun WriteBuffer.writeBerEncodedSignature(signatureVersion: Int, signature: ByteArray) =
    runCatching {
        val buf = BufferImpl.empty()
        buf.writeIntByte(signatureVersion)
        buf.write(signature)

        writeTlv(0xDE, buf.toByteArray())
            .getOrElse { throw Exception("Failed writing of TLV/BER encoded signature") }
    }

/**
 * Write a BER encoded TLV object into the [WriteBuffer]. The [tag] must already be provided in BER encoded format. The
 * length of the [value] will be used for the length portion.  The [value] will be copied as is.
 *
 * @receiver [WriteBuffer] Buffer the data will be written into
 * @return [Result]
 */
internal fun WriteBuffer.writeTlv(tag: Int, value: ByteArray?) = runCatching {

    value?.let {
        write(tag, 1).getOrThrow()

        writeTlvLength(it.size).getOrThrow()

        write(it)
    }
}

fun Ticket.writeBerEncodedPayloadInto(destination: WriteBuffer) = runCatching {

    destination.apply {

        writeTlv(0xC1, ticketId.toBytes(0))
            .getOrElse { throw Exception("Failed to write ticketId", it) }

        writeTlv(0xC2, creatorId.toBytes(0))
            .getOrElse { throw Exception("Failed to write creatorId", it) }

        writeTlv(0xC3, creationTime.epochSeconds.toBytes(0))
            .getOrElse { throw Exception("Failed to write creationTimestamp", it) }

        writeTlv(0xC4, validityPeriod.inWholeSeconds.toBytes(0))
            .getOrElse { throw Exception("Failed to write validityPeriod", it) }

        writeIds(ValidityDomainTagBer, validityDomains)
            .getOrElse { throw Exception("Failed to write Validity Domains", it) }

        writeIds(TransportOperatorIdTagBer, transportOperatorIds)
            .getOrElse { throw Exception("Failed to write Transport Operator IDs", it) }

        writeTlv(0xC7, effectiveTime?.epochSeconds?.toBytes(0))
            .getOrElse { throw Exception("Failed to write effective Time", it) }

        writeTlv(0xC8, refreshTime?.epochSeconds?.toBytes(0))
            .getOrElse { throw Exception("Failed to write refresh time", it) }

        writeIds(0xC9, ticketTypes)
            .getOrElse { throw Exception("Failed to write ticket Types IDs", it) }

        writeTlv(0xCA, accountId?.toBytes())
            .getOrElse { throw Exception("Failed to write account ID", it) }

        writeTlv(0xCB, boardingStation?.toBytes(0))
            .getOrElse { throw Exception("Failed to write boarding station ID", it) }

        writeTlv(0xCC, destinationStation?.toBytes(0))
            .getOrElse { throw Exception("Failed to write destination station ID", it) }

        writeTlv(0xCD, vehicleId?.toBytes(0))
            .getOrElse { throw Exception("Failed to write vehicle ID", it) }

        writeTlv(0xCE, routeId?.toBytes(0))
            .getOrElse { throw Exception("Failed to write route ID", it) }

        writeTlv(0xCF, seatNumber?.toBytes())
            .getOrElse { throw Exception("Failed to write seat number", it) }

        writeTlv(0xD0, seatClass?.toBytes())
            .getOrElse { throw Exception("Failed to write seat class", it) }

        writeTlv(0xD1, maxAuthorizedAmount?.toBytes(0))
            .getOrElse { throw Exception("Failed to write max authorized amount", it) }

        writeTlv(0xD2, signatureKeyIdentifier?.toBytes())
            .getOrElse { throw Exception("Failed to write signature key ID", it) }

        writeTlv(0xD3, terminalIdentifier?.toBytes())
            .getOrElse { throw Exception("Failed to write terminal ID", it) }

        writeTlv(0xD4, fundingSourceType?.toBytes(0))
            .getOrElse { throw Exception("Failed to write max funding source type ID", it) }

        writeTlv(0xD5, fundingSourceProvider?.toBytes())
            .getOrElse { throw Exception("Failed to write funding source provider", it) }
    }
}

private fun WriteBuffer.writeIds(tag: Int, validityDomains: List<Int>?) = runCatching {
    val buf = BufferImpl.empty(4)

    validityDomains?.forEach { domainId ->
        buf.reset()
        buf.write(domainId, 0)
        writeTlv(tag, buf.toByteArray())
    }
}

const val ValidityDomainTagBer = 0xC5
const val TransportOperatorIdTagBer = 0xC6