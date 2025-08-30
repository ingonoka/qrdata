/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.qcat

data class FundingSource(val id: Int, val shortName: String, val displayName: String)

fun qcatFundingSourceTypes(id: Int): Result<FundingSource> = runCatching {
    mapOf(
        0 to FundingSource(0,"RESERVED","Reserved"),
        1 to FundingSource(1,"CASH","Cash"),
        2 to FundingSource(2,"BANK_CARD_PRESENT", "Bank card, card present"),
        3 to FundingSource(3,"BANK_CARD_NOT_PRESENT","Bank card, card not present"),
        4 to FundingSource(4,"EWALLET_PRESENT","E-Wallet, customer present"),
        5 to FundingSource(5,"EWALLET_NOT_PRESENT","E-Wallet, customer not present"),
        6 to FundingSource(6,"BANK_ACCOUNT","Bank account"),
        7 to FundingSource(7, "TRANSPO,Transpo", "stored value card")
    ).getValue(id)
}
