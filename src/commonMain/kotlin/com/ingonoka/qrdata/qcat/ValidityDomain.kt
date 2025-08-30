/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.qcat

/**
 * Return the name of the QCAT validity domain associated with [id].
 *
 * @throws NoSuchElementException if no name for [id] exists.
 */
fun qcatValidityDomain(id: Int): Result<String> = runCatching {
    when (id) {
        1 -> "All Manila Light Rail Systems"
        2 -> "LRT1 Only"
        3 -> "LRT2 Only"
        4 -> "MRT3 Only"
        5 -> "Only non rail operators"
        6 -> "Only buses"
        7 -> "Only Jeepneys"
        8 -> "Only UV Express"
        9 -> "Mini buses"
        else -> throw NoSuchElementException()
    }
}
