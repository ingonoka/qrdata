/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import kotlinx.serialization.Serializable

/**
 * The EMV POI data of QR code data that is formatted according to EMV Merchant-presented
 * encoding rules.
 */
@Serializable
data class EmvPoiData(

    /**
     * Identifies the application as described in [ISO 7816-5].
     * The ADF Name may also be referred to as the Application Identifier (AID).
     * The POS system shall maintain a list of applications supported by the POS system
     * identified by their AIDs.
     *
     * Refer to Table 6.1/EMV Book 3
     *
     * F: b
     * T: '4F'
     * L: 5–16
     */
    val adfName: ByteArray? = null,

    /**
     * Valid cardholder account number.
     *
     * Refer to Table 6.1/EMV Book 3
     *
     * F: cn
     * T: '5A'
     * L: var. up to 10
     */
    val appPan: ByteArray? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmvPoiData) return false

        if (!adfName.contentEquals(other.adfName)) return false
        if (!appPan.contentEquals(other.appPan)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = adfName.contentHashCode()
        result = 31 * result + appPan.contentHashCode()
        return result
    }
}