/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata

import com.ingonoka.qrdata.qcat.Ticket
import com.ingonoka.utils.AsciiString
import kotlinx.datetime.Instant
import kotlin.test.*
import kotlin.time.Duration

class TicketTest {

    @Test
    fun testEquals() {

        val ticket = Ticket(
            ticketId = 1,
            creatorId = 1,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H")
        )

        val ticket1 = Ticket(
            ticketId = 1,
            creatorId = 1,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H")
        )

        assertEquals(ticket, ticket1)
    }

    @Test
    fun testIsSame() {
        val ticket = Ticket(
            ticketId = 1,
            creatorId = 1,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H"),
            terminalIdentifier = AsciiString("abc"),
            seatNumber = AsciiString("3A")
        )

        val ticket1 = Ticket(
            ticketId = 1,
            creatorId = 1,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H"),
            terminalIdentifier = AsciiString("abc"),
            seatNumber = AsciiString("3B")
        )

        assertEquals(ticket, ticket1)
        assertFalse(ticket.isSame(ticket1))
    }

    @Test
    fun testContentHashCode() {
        val ticket = Ticket(
            ticketId = 1,
            creatorId = 1,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H"),
            terminalIdentifier = AsciiString("abc"),
            seatNumber = AsciiString("3A")
        )

        val ticket1 = Ticket(
            ticketId = 1,
            creatorId = 1,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H"),
            terminalIdentifier = AsciiString("abc"),
            seatNumber = AsciiString("3B")
        )

        assertEquals(ticket.hashCode(), ticket1.hashCode())
        assertEquals(ticket.hashCode(), ticket.hashCode())

        assertNotEquals(ticket.contentHash(), ticket1.contentHash())


    }

    @Test
    fun testHashCode() {

        val ticketA = Ticket(
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

        val ticketB = Ticket(
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

        assertEquals(ticketA.hashCode(), ticketA.hashCode())
        assertEquals(ticketA.hashCode(), ticketB.hashCode())
        assertEquals(ticketA.contentHash(), ticketA.contentHash())
        assertEquals(ticketA.contentHash(), ticketB.contentHash())
    }

    @Test
    fun testStringify() {
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
            qcatSignature = byteArrayOf(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0
            )
        )

        val output = ticket.stringify()

        println(output)

        assertTrue { output.contains("Ticket:") }
        assertTrue { output.contains("   Mandatory:") }
        assertTrue { output.contains("       Id:                         644382") }
        assertTrue { output.contains("       Creator:                    123") }
        assertTrue {
            output.contains(
                "       Creation time :             2020-07-15T08:39:09+08:00"
            )
        }
        assertTrue { output.contains("       Validity period :           10h") }
        assertTrue { output.contains("Optional:") }
        assertTrue { output.contains("    Terminal ID:                1234567890123456") }
        assertTrue {
            output.contains(
                "    Validity domain :           [All Manila Light Rail Systems (id: 1),LRT1 Only (id: 2)]"
            )
        }
        assertTrue { output.contains("    Transport Operator IDs:     [23,56]") }
        assertTrue { output.contains("    Effective time:             2020-07-15T08:39:09+08:00") }
        assertTrue { output.contains("    Account ID :                1234567890123456") }
        assertTrue { output.contains("    refreshTime:                2020-07-15T08:39:39+08:00") }
        assertTrue {
            output.contains(
                "    Ticket type :               [Standard (id: 1),Senior citizen (id: 2)]"
            )
        }
        assertTrue { output.contains("    Boarding station :          23") }
        assertTrue { output.contains("    Destination station :       17") }
        assertTrue { output.contains("    Vehicle ID :                345") }
        assertTrue { output.contains("    Route ID :                  4") }
        assertTrue { output.contains("    Seat number:                3D") }
        assertTrue { output.contains("    Seat class:                 ECO") }
        assertTrue { output.contains("    Max authorized amount:      PHP 30.00") }
        assertTrue { output.contains("    Key identifier:             Not provided") }
        assertTrue { output.contains("    Funding Source Type:        Cash") }
        assertTrue { output.contains("    Funding Source Provider:    6378") }

        assertTrue { output.contains("    Signature:") }
        assertTrue { output.contains("        V: 1 (SHA256withRSA)") }
        assertTrue {
            output.contains(
                "        S: 00   01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06    ................"
            )
        }
        assertTrue {
            output.contains(
                "           01   01 02 03 04 05 06 07 08 09 00                      .........."
            )
        }

    }

    @Test
    fun testStringifyMinimal() {
        val ticket = Ticket(
            ticketId = 644382,
            creatorId = 123,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H")
        )

        val output = ticket.stringify()

        println(output)

        assertTrue { output.contains("Ticket:") }
        assertTrue { output.contains("   Mandatory:") }
        assertTrue { output.contains("       Id:                         644382") }
        assertTrue { output.contains("       Creator:                    123") }
        assertTrue {
            output.contains(
                "       Creation time :             2020-07-15T08:39:09+08:00"
            )
        }
        assertTrue { output.contains("       Validity period :           10h") }
        assertTrue { output.contains("Optional:") }
        assertTrue { output.contains("    Terminal ID:                Not provided") }
        assertTrue { output.contains("    Validity domain :           Not provided") }
        assertTrue { output.contains("    Transport Operator IDs:     Not provided") }
        assertTrue {
            output.contains(
                "    Effective time:             2020-07-15T08:39:09+08:00 (Default)"
            )
        }
        assertTrue { output.contains("    Account ID :                Not provided") }
        assertTrue { output.contains("    refreshTime:                static (Default)") }
        assertTrue { output.contains("    Ticket type :               Standard (id: 1) (Default)") }
        assertTrue { output.contains("    Boarding station :          Not provided") }
        assertTrue { output.contains("    Destination station :       Not provided") }
        assertTrue { output.contains("    Vehicle ID :                Not provided") }
        assertTrue { output.contains("    Route ID :                  Not provided") }
        assertTrue { output.contains("    Seat number:                Not provided") }
        assertTrue { output.contains("    Seat class:                 Not provided") }
        assertTrue { output.contains("    Max authorized amount:      Not provided") }
        assertTrue { output.contains("    Key identifier:             Not provided") }
        assertTrue { output.contains("    Funding Source Type:        Not provided") }
        assertTrue { output.contains("    Funding Source Provider:    Not provided") }

        assertTrue { output.contains("    Signature:") }
        assertTrue { output.contains("        V: 1 (SHA256withRSA) (Default)") }
        assertTrue { output.contains("        S: null") }
    }

    @Test
    fun testInvalidSeatNumber() {

        val ticket = Ticket(
            ticketId = 644382,
            creatorId = 123,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H")
        )

        // Too long
        assertFails { ticket.copy(seatNumber = AsciiString("123456")) }
        // Invalid character
        assertFails { ticket.copy(seatNumber = AsciiString("1234ü")) }

    }

    @Test
    fun testInvalidSeatClass() {

        val ticket = Ticket(
            ticketId = 644382,
            creatorId = 123,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H")
        )

        // Too long
        assertFails { ticket.copy(seatClass = AsciiString("123456")) }
        // Invalid character
        assertFails { ticket.copy(seatClass = AsciiString("1234ü")) }
    }

    @Test
    fun testInvalidSignatureKeyIdentifier() {

        val ticket = Ticket(
            ticketId = 644382,
            creatorId = 123,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H")
        )

        // Invalid character
        assertFails { println(ticket.copy(seatClass = AsciiString("1234ü"))) }
    }

    @Test
    fun testInvalidFundingSourceprovider() {

        val ticket = Ticket(
            ticketId = 644382,
            creatorId = 123,
            creationTime = Instant.fromEpochSeconds(1594773549),
            validityPeriod = Duration.parse("PT10H")
        )

        // Invalid character
        assertFails { ticket.copy(fundingSourceProvider = AsciiString("1234~")) }
    }


}