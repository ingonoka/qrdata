/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.qcat

import com.ingonoka.hexutils.toHexChunked
import com.ingonoka.qrdata.crypto.versionToAlgorithmName
import com.ingonoka.qrdata.transcode.EmvPoiData
import com.ingonoka.utils.AsciiString
import com.ingonoka.utils.formatTime
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Class representing QR tickets
 *
 */
@Serializable
data class Ticket(
    /**
     * POI Data as defined by EMV for the Application template (tag = 0x61)
     *
     * EMV® QR Code Specification for Payment Systems Consumer-Presented Mode
     * Version 1.1, November 2020
     */
    val poiData: EmvPoiData? = null,

    /******************************************************************************************************
     *                                     Mandatory data elements
     ******************************************************************************************************/


    /**
     * 32 bit long unsigned integer 0x0..0xFFFFFFFF
     */
    val ticketId: Long,
    /**
     * Ticket Creator ID (M), Unsigned Integer, 16 bit, Binary, Hex
     * The ticket creator is the organization that is authorized to create tickets and that will be
     * liable for the fare amount when the ticket is accepted by an AFCS provider.
     */
    val creatorId: Int,
    /**
     * Time of ticket creation (M), Timestamp
     * Time at which the ticket was created.  The ticket validity and QR refreshment periods are always interpreted
     * with this time as the base.
     */

    val creationTime: Instant,
    /**
     * Ticket Validity Period (O), Unsigned Integer, 32 bit, Binary
     * Time period in seconds from the effective time if present ot the time of ticket creation after which the
     * ticket is not valid anymore.
     */

    val validityPeriod: Duration,


    /******************************************************************************************************
     *                                     Optional data elements
     ******************************************************************************************************/


    /**
     * Ticket Validity Domain (O), Unsigned Integer, 16 bit
     * Identifies the public transport facility on which the ticket is valid. Ticket domain
     * identifiers are either one of the default values or assigned by the ticket issuer in which case
     * they are unique only in combination with the ticket issuer ID. See <<Ticket Validity Domains>>
     *
     * IMPROVEMENT Make this a list so that multiple domains can be included in a QR code ticket
     */
    val validityDomains: List<Int>? = null,

    /**
     * Transport Operator ID (O), Unsigned Integer, 32 bit, Binary, hex
     * The identifier of transport operator for which the ticket is valid. There could be more than one
     * transport operator TLV in the QR code.  Operators can be grouped and assigned a Ticket Validity Domain to avoid
     * including too many operator IDs.
     */
    val transportOperatorIds: List<Int>? = null,

    /**
     * Ticket Effective Time (O) Unsigned Integer, 32 bit, Binary
     * Time period in seconds from the time of ticket creation after which the ticket is valid.
     * The default of 0 means that the ticket is valid from the time of creation.
     */
    val effectiveTime: Instant? = null,
    /**
     * Refresh Time (O), Timestamp
     * Time after which the ticket needs to be refreshed with a new refresh time and signature.
     * A value of `0` or if the field is not included means that the QR ticket is static.
     */
    val refreshTime: Instant? = null,
    /**
     * Ticket type (O), Unsigned Integer, 16 bit, Binary, Hex
     * Indicates a special processing rule that will be applied when calculating the fare.
     */
    val ticketTypes: List<Int>? = null,
    /**
     * Account identifier ©, ASCII
     * The account identifier provides information about the passenger's account with the funding provider.
     * This account will be debited according to the fare table and ticketing rules. The account number actually
     * be created as a token that is valid only for a certain time or for a certain transaction.
     * Backend system should therefore nor rely on this identifier to group transactions.
     * Must be present in post-paid tickets
     */
    val accountId: AsciiString? = null,

    /**
     * Boarding Station (O), Unsigned Integer, 32 bit, Binary, hex
     * The identifier of the boarding station or stop.
     */
    val boardingStation: Long? = null,
    /**
     * Destination Station (O), Unsigned Integer, 32 bit, Binary, hex
     * The identifier of the destination station or stop.
     */
    val destinationStation: Long? = null,
    /**
     * Vehicle ID (O), Unsigned Integer, 32 bit, Binary, hex
     * The identifier of the vehicle for the ticket is valid (e.g., bus number).
     */
    val vehicleId: Long? = null,
    /**
     * Route ID (O), Unsigned Integer, 32 bit, Binary, hex
     * The id of the route for which the ticket is valid (e.g., bus number).
     */
    val routeId: Long? = null,

    /**
     * Seat Number (O) ASCII - max 5 characters
     * The identifier for a particular seat that has been reserved for the passenger presenting this ticket.
     * The format and meaning are operator or AFCS provider-specific.
     */
    val seatNumber: AsciiString? = null,
    /**
     * Seat Class (O) ASCII - max 5 characters
     * The class a particular seat that has been reserved for the passenger presenting this ticket.
     * The format and meaning are operator or AFCS provider-specific.
     */
    val seatClass: AsciiString? = null,
    /**
     * Maximum Authorized Amount (O), Unsigned Integer, 32 bit, Binary, hex
     * Amount in Centavos.  If the fare amount is known when the passenger starts the trip,
     * this field will be checked and the QR code rejected if the fare is higher than the maximum
     * authorized amount.  If the fare is not known at boarding time, the maximum remaining fare on the
     * trip must be lower than the amount in this field.  The funding provider may earmark this amount in
     * the passenger's account and release the unused funds after the AFCS provider provided the correct
     * fare amount.
     */
    val maxAuthorizedAmount: Long? = null,
    /**
     * The key identifier is used to distinguish multiple public key certificates assigned to a single QR Issuer.
     * It corresponds to the Common Name (CN) in the Issuer's certificate.  If present, the value in this field
     * and the CN of the issuer certificate that is used to validate the signature must match.  If this field
     * is not present, the terminal will ignore the CN and use any certificate with the Ticket Creator's ID.
     */
    val signatureKeyIdentifier: AsciiString? = null,
    /**
     * The terminal identifier identifies the device that "produced" the QR ticket.  Validation terminals should
     * always check the terminal ID, if present, together with the ticket ID and creation time to ensure that
     * the same ticket is not used twice. The terminal ID should be unique in the ticket creator fleet of devices
     * to the extent that the validation terminal is able to distinguish between two tickets with the
     * same ticket identifier.
     */
    val terminalIdentifier: AsciiString? = null,

    /**
     * The funding source type identifies the type of funding source that was used to pay for the ticket.
     * Depending on the funding source type, the account identifier can be used to provide more details on
     * the funding source. For example, if the funding source type is "Transpo card" then the account identifier
     * could contain the CAN of the transpo card.
     */
    val fundingSourceType: Int? = null,

    /**
     * The participant ID of the funding source provider. The meaning of the value in this field depends on the
     * funding source type. For example, if the funding source type is a bank card, then the value would represent
     * a BIN, in case of a Transpo™ card, the value may represent the IIN of the stored value card.
     */
    val fundingSourceProvider: AsciiString? = null,


    /**
     * The QcatSignature
     */
    val signatureVersion: Int? = null,
    val qcatSignature: ByteArray? = null

) {

    init {

        require(
            seatNumber == null || (seatNumber.s.length <= 5 && seatNumber.s.all { it.code in ' '.code..'~'.code })
        )
        require(
            seatClass == null || (seatClass.s.length <= 5 && seatClass.s.all { it.code in ' '.code..'~'.code })
        )

        require(signatureKeyIdentifier == null || signatureKeyIdentifier.s.all { char ->
            char in '0'..'9' || char in 'a'..'z' || char in 'A'..'Z' || char == '_' || char == '-'
        })

        require(terminalIdentifier == null || terminalIdentifier.s.all { char ->
            char in '0'..'9' || char in 'a'..'z' || char in 'A'..'Z' || char == '_' || char == '-'
        })

        require(fundingSourceProvider == null || fundingSourceProvider.s.all { char ->
            char in '0'..'9' || char in 'a'..'z' || char in 'A'..'Z' || char == '_' || char == '-'
        })


    }

    /**
     * Compare two tickets.
     *
     * The tickets are the same of [ticketId], [creationTime], [creatorId], [validityPeriod] and [terminalIdentifier]
     * match. If [terminalIdentifier] is present in one ticket then it must be present in the other ticket as well.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ticket) return false

        if (ticketId != other.ticketId) return false
        if (creationTime != other.creationTime) return false
        if (creatorId != other.creatorId) return false
        if (validityPeriod != other.validityPeriod) return false
        if (terminalIdentifier != other.terminalIdentifier) return false
        return true
    }

    /**
     * Compare two tickets (all properties must match).
     *
     * Only compare the signature if [compareSignature] is true (default).
     *
     * If one or both tickets have no signature property, then the signature will be ignored for comparison purposes.
     *
     * The [refreshTime] is not included in the comparison, because two tickets that only differ in the
     * refreshTime are considered identical.  In other words, the refreshTime is a property of the QR code
     * and not the ticket.
     */
    fun isSame(other: Ticket, compareSignature: Boolean = true): Boolean {

        if (this != other) return false

        if (poiData != other.poiData) return false

        if (validityDomains?.toSet() != other.validityDomains?.toSet()) return false
        if (effectiveTime != other.effectiveTime) return false
        if (transportOperatorIds?.toSet() != other.transportOperatorIds?.toSet()) return false
        if (accountId != other.accountId) return false
        if (ticketTypes?.toSet() != other.ticketTypes?.toSet()) return false
        if (boardingStation != other.boardingStation) return false
        if (destinationStation != other.destinationStation) return false
        if (vehicleId != other.vehicleId) return false
        if (routeId != other.routeId) return false
        if (seatNumber != other.seatNumber) return false
        if (seatClass != other.seatClass) return false
        if (maxAuthorizedAmount != other.maxAuthorizedAmount) return false
        if (signatureKeyIdentifier != other.signatureKeyIdentifier) return false
        if (fundingSourceType != other.fundingSourceType) return false
        if (fundingSourceProvider != other.fundingSourceProvider) return false

        if (signatureVersion != other.signatureVersion) return false

        if (compareSignature)
            if (qcatSignature != null &&
                other.qcatSignature != null &&
                !qcatSignature.contentEquals(other.qcatSignature)
            ) return false

        return true
    }

    /**
     * Hash code.  Only uses the properties that identify a ticket uniquely: [ticketId], [creationTime], [creatorId],
     * [validityPeriod] and [terminalIdentifier]
     */
    override fun hashCode(): Int {
        var result = poiData.hashCode()
        result = 31 * result + ticketId.hashCode()
        result = 31 * result + creatorId
        result = 31 * result + creationTime.hashCode()
        result = 31 * result + validityPeriod.hashCode()
        result = 31 * result + (terminalIdentifier?.s?.encodeToByteArray()?.contentHashCode() ?: 0)
        return result
    }

    /**
     * Hash code.  Uses all properties.
     *
     * The refreshTime is not included in the hash code calculation, because two tickets that only differ in the
     * refreshTime are considered identical.  In other words, the refreshTime is a property of the QR code
     * and not the ticket.
     */
    fun contentHash(): Int {
        var result = hashCode().toLong()
        result = 31 * result + (validityDomains?.toTypedArray()?.contentHashCode() ?: 0)
        result = 31 * result + (transportOperatorIds?.toTypedArray()?.contentHashCode() ?: 0)
        result = 31 * result + (effectiveTime?.hashCode() ?: 0)
        result = 31 * result + (ticketTypes?.toTypedArray()?.contentHashCode() ?: 0)
        result = 31 * result + (accountId?.s?.hashCode() ?: 0)
        result = 31 * result + (boardingStation ?: 0)
        result = 31 * result + (destinationStation ?: 0)
        result = 31 * result + (vehicleId ?: 0)
        result = 31 * result + (routeId ?: 0)
        result = 31 * result + (seatNumber?.s?.hashCode() ?: 0)
        result = 31 * result + (seatClass?.s?.hashCode() ?: 0)
        result = 31 * result + (maxAuthorizedAmount ?: 0)
        result = 31 * result + (fundingSourceType ?: 0)
        result = 31 * result + (fundingSourceProvider?.s?.hashCode() ?: 0)
        result = 31 * result + (signatureVersion ?: 0)
        result = 31 * result + (qcatSignature?.contentHashCode() ?: 0)
        return result.toInt()
    }

    /**
     * Description for display and logging purposes
     *
     */
    fun stringify(): String {

        val adfNameString =
            poiData?.adfName?.toHexChunked(printable = true, lineNums = false, columns = 8)
        val appPanString =
            poiData?.appPan?.toHexChunked(printable = true, lineNums = false, columns = 8)

        val pesoAmount = maxAuthorizedAmount?.let {
            val pesos = "${it / 100}"
            val centavos = "${it % 100}".padStart(2, '0')
            "PHP $pesos.$centavos"
        }

        val validityDomainsString = validityDomains
            ?.joinToString(",", "[", "]") { id ->
                qcatValidityDomain(id)
                    .map { "$it (id: $id)" }
                    .getOrElse { _: Throwable -> id.toString() }
            }

        val ticketTypesString = ticketTypes
            ?.joinToString(",", "[", "]") { id ->
                qcatTicketTypes(id)
                    .map { "$it (id: $id)" }
                    .getOrElse { _: Throwable -> id.toString() }
            }
            ?: "${qcatTicketTypes(standardTypeId).getOrThrow()} (id: ${standardTypeId}) (Default)"

        val fundingSourceTypeString = fundingSourceType?.let { id ->
            qcatFundingSourceTypes(id)
                .map { it.displayName }
                .getOrElse { _: Throwable -> id.toString() }
        }


        val transportOperatorIdsString = transportOperatorIds?.joinToString(",", "[", "]")

        val tz = TimeZone.of("+08:00")

        val effectiveTimeString = effectiveTime?.formatTime(tz)
            ?: "${creationTime.formatTime(tz)} (Default)"

        val signatureVersionString = signatureVersion?.let { "$it (${versionToAlgorithmName[it]})" }
            ?: "1 (${versionToAlgorithmName[1]}) (Default)"

        return buildString {
            append(
                """
                |EMV POI Data:
                |   ADF Name:                       $adfNameString
                |   Application PAN:                $appPanString
                |Ticket:
                |   Mandatory:
                |       Id:                         $ticketId
                |       Creator:                    $creatorId
                |       Creation time :             ${creationTime.formatTime(tz)}
                |       Validity period :           $validityPeriod
                |   Optional:
                |       Terminal ID:                ${terminalIdentifier?.s ?: "Not provided"}
                |       Validity domain :           ${validityDomainsString ?: "Not provided"}
                |       Transport Operator IDs:     ${transportOperatorIdsString ?: "Not provided"}
                |       Effective time:             $effectiveTimeString
                |       Account ID :                ${accountId?.s ?: "Not provided"}
                |       refreshTime:                ${
                    refreshTime?.formatTime(
                        tz
                    ) ?: "static (Default)"
                }
                |       Ticket type :               $ticketTypesString
                |       Boarding station :          ${boardingStation ?: "Not provided"}
                |       Destination station :       ${destinationStation ?: "Not provided"}
                |       Vehicle ID :                ${vehicleId ?: "Not provided"}
                |       Route ID :                  ${routeId ?: "Not provided"}
                |       Seat number:                ${seatNumber?.s ?: "Not provided"}
                |       Seat class:                 ${seatClass?.s ?: "Not provided"}
                |       Max authorized amount:      ${pesoAmount ?: "Not provided"}
                |       Funding Source Type:        ${fundingSourceTypeString ?: "Not provided"}
                |       Funding Source Provider:    ${fundingSourceProvider?.s ?: "Not provided"}
                |       Key identifier:             ${signatureKeyIdentifier?.s ?: "Not provided"}
                |       Signature:
                |           V: $signatureVersionString
                |           S: ${
                    qcatSignature?.toHexChunked(columns = 16)?.lines()
                        ?.joinToString("\n              ")
                }
                |""".trimMargin()
            )
        }
    }
}