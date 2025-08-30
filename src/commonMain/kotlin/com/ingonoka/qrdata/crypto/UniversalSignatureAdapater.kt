/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.crypto

val versionToAlgorithmName = mapOf(
    1 to "SHA256withRSA",
    2 to "SHA1withECDSA",
    3 to "AESCMAC2",
    4 to "AESCMAC4",
    5 to "AESCMAC16"
)

val signatureLength: Map<Int?, Int> = mapOf(
    null to 0,
    1 to 128, //"SHA256withRSA",
    2 to 128, //"SHA1withECDSA",
    3 to 2, // "AESCMAC2",
    4 to 4, //"AESCMAC4",
    5 to 16, //"AESCMAC16"
)