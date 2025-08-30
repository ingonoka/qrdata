/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.crypto

import kotlin.test.Test
import kotlin.test.assertEquals


class CrcTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun compute() {
        val testQRData =
            "00020101021228650011ph.ppmi.p2m0111BNORPHMMXXX031091821299080410918212990805033115204531153036085406623.405802PH5917SM STORE CLARK QR6008PAMPANGA62370011ph.ppmi.p2m050624268907081330493088530012ph.ppmi.qrph0133116510000007~102024082741921413746304"
                .toCharArray().map { c -> c.code }

        val crc = Crc().compute(testQRData.toIntArray())
        assertEquals(0xd094, crc)

    }
}