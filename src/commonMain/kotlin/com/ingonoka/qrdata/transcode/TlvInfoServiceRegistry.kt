/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.qrdata.alipay.AlipayTlvInfoService
import com.ingonoka.qrdata.asn1.Asn1TlvInfoService
import com.ingonoka.qrdata.emv.EmvCpTlvInfoService
import com.ingonoka.qrdata.emv.EmvMpTlvInfoService
import com.ingonoka.qrdata.gcash.GCashTlvInfoService
import com.ingonoka.qrdata.qcat.QcatTlvInfoService
import com.ingonoka.qrdata.qrph.QrPhTlvInfoService

object TlvInfoServiceRegistry {
    // Using a map to store service providers by their class type
    private val providers = mutableListOf<TlvInfoService>(
        Asn1TlvInfoService(),
        EmvCpTlvInfoService(),
        EmvMpTlvInfoService(),
        GCashTlvInfoService(),
        QcatTlvInfoService(),
        AlipayTlvInfoService(),
        QrPhTlvInfoService()
    )

    // Register a service provider
    fun registerProvider(service: TlvInfoService) {
        providers.add(service)
    }

    /**
     * Get all available services
     */
    fun getServices(): List<TlvInfoService> {
        return providers
    }

    /**
     * Get a specific service that has info for [standard] and [context]
     */
    fun getService(standard: Standard, context: Context): Result<TlvInfoService> = runCatching {
        providers.find { it.isTlvInfoProviderFor(standard, context) }
            ?: throw NoSuchElementException("No TLV info service for $standard / ${context.name}")
    }

}

/**
 * Info about a TLV based on its tag number. Contains [tag], [name], whether it
 * is a template ([isTemplate]) and a [stringifier] function to analyze and print the
 * [TLV.value]
 */
data class TlvInfo(
    val tag: Int,
    val name: String,
    val isTemplate: Boolean,
    val stringifier: StringifyFunction?
)

/**
 * Generic [TlvInfo] for proprietary data.
 */
val TlvInfoProprietary = TlvInfo(-1, "Proprietary", false, null)

/**
 * The Service provides info for TLV such as a name, whether the tag represents
 * a template and what function to use to analyze and create a printable
 * version of the value.
 */
interface TlvInfoService {
    /**
     * Return true if service can rovide info for [standard] and [context]
     */
    fun isTlvInfoProviderFor(standard: Standard, context: Context): Boolean

    /**
     * Get the TLV info for [tag] within [context].
     * If info available wrapped in [Result]. Return [Result.Failure] if no
     * info available.
     *
     */
    fun getTlvInfo(tag: Int, context: Context): Result<TlvInfo>

    /**
     * Get the TLV info for [tag] within [context].
     * Wrapped in [Result]
     * Return null if no info available.
     */
    fun getTlvInfoOrNull(tag: Int, context: Context): TlvInfo?
}