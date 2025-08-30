/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import kotlin.jvm.JvmInline

/**
 * A marker for strings that represent a context for the
 * scanning of a TLV structure. The tag name, permitted length and data format of
 * a TLV depends on the standard and the context.
 */
@JvmInline
value class Context(val name: String)