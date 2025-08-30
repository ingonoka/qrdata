/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.qcat

const val standardTypeId: Int = 1

fun qcatTicketTypes(id: Int): Result<String> = runCatching {
    when (id) {
        1 -> "Standard"
        2 -> "Senior citizen"
        3 -> "Person with disability"
        4 -> "LRT1 Employee Card"
        5 -> "LRT2 Employee Card"
        6 -> "MRT3 Employee Card"
        7 -> "AFCS Employee Card"
        17 -> "Student"
        else -> throw NoSuchElementException()
    }
}
