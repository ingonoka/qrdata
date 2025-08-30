/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.utils

/**
 * Collector of messages.
 *
 * Is used for functions that want to return useful information in addition
 * to the main result of the function.
 */
interface Reporter: MutableList<String> {
    fun addReport(report: String)
    fun addReports(report: List<String>)
    fun getReports(): List<String>
    fun getMessageNumber(): Int
    fun reset()
}

/**
 * A [Reporter] that does nothing.
 */
object NullReporter: Reporter, MutableList<String> by mutableListOf() {

    override fun addReport(report: String) {}

    override fun addReports(report: List<String>) {}

    override fun getReports(): List<String> = this

    override fun getMessageNumber(): Int = 0

    override fun reset() {}
}

/**
 * A reporter that collects messages in memory.
 *
 * Messages are lost when an object of this class is destroyed.
 */
class MemoryReporter: Reporter, MutableList<String> by mutableListOf() {
    override fun addReport(report: String) { add(report) }
    override fun addReports(report: List<String>) { addAll(report) }
    override fun getReports(): List<String> = this
    override fun getMessageNumber(): Int = size
    override fun reset() { removeAll { true } }
}