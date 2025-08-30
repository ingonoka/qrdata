/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.hexutils.hexToBytes
import com.ingonoka.qrdata.qcat.Ticket
import com.ingonoka.qrdata.qcat.encode
import com.ingonoka.qrdata.utils.MemoryReporter
import com.ingonoka.utils.AsciiString
import com.ingonoka.utils.BufferImpl
import com.ingonoka.utils.buffer
import com.ingonoka.utils.fromBase64
import kotlinx.datetime.Instant
import kotlin.test.*
import kotlin.time.Duration


class TLVTest {


    @Test
    fun testEquals() {
        val tlv1 = TLV(
            1, 0, byteArrayOf(), listOf(
                TLV(
                    2, 0, byteArrayOf(), listOf(
                        TLV(3, 0, byteArrayOf(), listOf())
                    )
                )
            )
        )

        val tlv2 = TLV(
            1, 0, byteArrayOf(), listOf(
                TLV(
                    2, 0, byteArrayOf(), listOf(
                        TLV(3, 0, byteArrayOf(), listOf())
                    )
                )
            )
        )

        val tlv3 = TLV(
            1, 0, byteArrayOf(), listOf(
                TLV(
                    2, 0, byteArrayOf(), listOf(
                        TLV(3, 0, byteArrayOf(1), listOf())
                    )
                )
            )
        )



        assertEquals(tlv1, tlv2)
        assertEquals(tlv1.hashCode(), tlv2.hashCode())
        assertNotEquals(tlv1, tlv3)
        assertNotEquals(tlv1.hashCode(), tlv3.hashCode())


    }

    @Test
    fun testFindTag() {

        // GCash Transit ticket July 2025
        @Suppress("SpellCheckingInspection")
        val payload = "hQVDUFYwMWGBl08FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNxwRZ" +
                "HQy0wNzI1MjAyNS0wMDAwMDkzOTA4wgIAAcQEaINZd8gEaIM9a95HMEUCIGWdjueZsFZHMkVQSy/jlqQub" +
                "fpvMk4BsQKeHJuIn5aJAiEAvNpixJCHfUCj6T/8Q8/V3ay0GmNFgAUdDzE+c/Ilny0="

//        println(payload.fromBase64().toHexChunked(lineNums = false, printable = true))

        val reporter = MemoryReporter()

        BufferImpl.wrap(payload.fromBase64()).decodeToTlvStructure(Encoding.BER, reporter)
            .onSuccess {
                // find tag in application template
                var result = it.findValueForTag(0x4F)
                assertContentEquals(byteArrayOf(0x47, 0x43, 0x41, 0x53, 0x48), result)

                // find tag in application transparent template
                result = it.findValueForTag(0xC2)
                assertContentEquals(byteArrayOf(0x00, 0x01), result)

                // do not find non-existent tag
                result = it.findValueForTag(0x4E)
                assertNull(result)

            }
            .getOrThrow()

    }

    /*val ticket = Ticket(
        ticketId = 644382,
        creatorId = 123,
        creationTime = Instant.fromEpochSeconds(1594773549),
        validityPeriod = Duration.parse("PT10H"),
        effectiveTime = Instant.fromEpochSeconds(1594773549),
        boardingStation = 23L,
        destinationStation = 17L,
        maxAuthorizedAmount = 3000,
        refreshTime = Instant.fromEpochSeconds(1594773549 + 30),
        routeId = 4,
        ticketTypes = listOf(1, 2),
        transportOperatorIds = listOf(23, 56),
        vehicleId = 345,
        seatNumber = AsciiString("3D"),
        seatClass = AsciiString("ECO"),
        validityDomains = listOf(1, 2),
        accountId = AsciiString("1234567890123456"),
        terminalIdentifier = AsciiString("1234567890123456"),
        fundingSourceType = 1,
        fundingSourceProvider = AsciiString("6378"),
        signatureVersion = 1,
        qcatSignature = byteArrayOf()
    )*/


    @Test
    fun testFindAll() {


        val ticket = Ticket(
            ticketId = 644382,
            creatorId = 123,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H"),
            effectiveTime = Instant.fromEpochSeconds(1594773549),
            boardingStation = 23L,
            destinationStation = 17L,
            maxAuthorizedAmount = 3000,
            refreshTime = Instant.fromEpochSeconds(1594773549 + 30),
            routeId = 4,
            ticketTypes = listOf(1, 2),
            transportOperatorIds = listOf(23, 56),
            vehicleId = 345,
            seatNumber = AsciiString("3D"),
            seatClass = AsciiString("ECO"),
            validityDomains = listOf(1, 2),
            accountId = AsciiString("1234567890123456"),
            terminalIdentifier = AsciiString("1234567890123456"),
            fundingSourceType = 1,
            fundingSourceProvider = AsciiString("6378"),
            signatureVersion = 1,
            qcatSignature = byteArrayOf()
        )

        val reporter = MemoryReporter()

        ticket.encode()
            .map { buffer ->
                buffer.decodeToTlvStructure(Encoding.BER, reporter).getOrThrow()
            }
            .onSuccess {
                val actual = it.findAll(0xC6)
                val expected = listOf(byteArrayOf(0x17), byteArrayOf(0x38))
                assertTrue { areByteArrayListsEqual(actual, expected) }
                println(it.stringify(
                    standard = Standard.EMV_CUSTOMER, context = Context("ROOT"))
                )
            }
            .getOrThrow()
    }

    private fun areByteArrayListsEqual(list1: List<ByteArray>, list2: List<ByteArray>): Boolean {
        if (list1.size != list2.size) {
            return false
        }
        for (i in list1.indices) {
            if (!list1[i].contentEquals(list2[i])) {
                return false
            }
        }
        return true
    }

    @Test
    fun testGenericAsnStructure() {

        @Suppress("SpellCheckingInspection") val payload =
            "304402205247E8CF1DBAD988D71DB75B7F4C7F82A05C0FA5EB0794000AC10C6BD4F1E9350220633196041D531F3512552157C6FF067E9AD0E10101E63662EA4AB23098EB5B49"

        val reporter = MemoryReporter()

        payload.hexToBytes().buffer().decodeToTlvStructure(Encoding.BER, reporter)
            .onSuccess {
                println(
                    it.stringify(
                        "",
                        Standard.ASN1,
                        Context("ROOT")
                    )
                )
            }


    }
}