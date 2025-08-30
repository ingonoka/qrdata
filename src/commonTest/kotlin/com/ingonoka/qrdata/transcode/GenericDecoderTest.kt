/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.qrdata.transcode.Standard.EMV_CUSTOMER
import com.ingonoka.qrdata.utils.MemoryReporter
import com.ingonoka.qrdata.utils.NullReporter
import com.ingonoka.utils.BufferImpl
import com.ingonoka.utils.buffer
import com.ingonoka.utils.collectExceptionMessages
import com.ingonoka.utils.fromBase64
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull


class GenericDecoderTest {

    @Test
    fun testSimple() {

        val reporter = MemoryReporter()

        BufferImpl.wrap("010203".encodeToByteArray()).apply {
            val reporter = MemoryReporter()
            decodeEmvMpToTlvStructure(reporter)
                .onSuccess {
                    println(
                        it.stringify(
                            standard = Standard.ASN1,
                            context = Context("ROOT"),
                            reporter = reporter)
                    )
                    reporter.forEach { s ->
                        println(s)
                    }
                }
                .onFailure {
                    println(it)
                    println(it.collectExceptionMessages())
                }
                .getOrThrow()
        }

        println(reporter.getReports().joinToString("\n"))
    }

    @Test
    fun testDecode() {

        // GCash Transit ticket July 2025
        @Suppress("SpellCheckingInspection")
        val payload = "hQVDUFYwMWGBl08FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNxwRZ" +
                "HQy0wNzI1MjAyNS0wMDAwMDkzOTA4wgIAAcQEaINZd8gEaIM9a95HMEUCIGWdjueZsFZHMkVQSy/jlqQub" +
                "fpvMk4BsQKeHJuIn5aJAiEAvNpixJCHfUCj6T/8Q8/V3ay0GmNFgAUdDzE+c/Ilny0="

        @Suppress("SpellCheckingInspection")
        //hQVDUFYwMWGBl08FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNxwRZHQy0wNzI1MjAyNS0wMDAwMDkzOTA4wgIAAcQEaINZd8gEaIM9a95HMEUCIGWdjueZsFZHMkVQSy/jlqQubfpvMk4BsQKeHJuIn5aJAiEAvNpixJCHfUCj6T/8Q8/V3ay0GmNFgAUdDzE+c/Ilny0=

        BufferImpl.wrap(payload.fromBase64()).decodeToTlvStructure(Encoding.BER, NullReporter)
            .onSuccess {
                println(
                    it.stringify(
                        standard = EMV_CUSTOMER, context = Context("ROOT"),
                        hexDump = false, rawTlv = false
                    )
                )
            }
            .getOrThrow()

    }

    @Test
    fun testDecodeQcat() {

        @Suppress("SpellCheckingInspection")
        val payload = ("hQVDUFYwMWFuTwZRQ0FUMDFjZMECEwfCAgEGwwRd6cfcxAMD9IDGA" +
                "gEGyQEBywEBzAEAzgEL0wUzMDI4Mt44AjA1Ahhz3d" +
                "BM7DqCOUzUZjtuQqfDITW3bGlX6G0CGQCoAyQM7ge" +
                "TFZQSU0C9APUeqNLYNUKBwDc=")

        val reporter = MemoryReporter()

        BufferImpl
            .wrap(payload.fromBase64())
            .decodeToTlvStructure(Encoding.BER, reporter)
            .onSuccess {
                println(
                    it.stringify(
                        "", EMV_CUSTOMER, Context("ROOT"), true, rawTlv = false,
                        reporter = reporter
                    )
                )
                println("Errors")
                println(reporter.getReports().mapIndexed { index, string ->
                    "${index + 1}: $string"
                }.joinToString("\n"))
            }
            .getOrThrow()

    }

    @Test
    fun testFindTag() {

        // GCash Transit ticket July 2025
        @Suppress("SpellCheckingInspection")
        val payload = "hQVDUFYwMWGBl08FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNxwRZ" +
                "HQy0wNzI1MjAyNS0wMDAwMDkzOTA4wgIAAcQEaINZd8gEaIM9a95HMEUCIGWdjueZsFZHMkVQSy/jlqQub" +
                "fpvMk4BsQKeHJuIn5aJAiEAvNpixJCHfUCj6T/8Q8/V3ay0GmNFgAUdDzE+c/Ilny0="

//        println(payload.fromBase64().toHexChunked(lineNums = false, printable = true))

        BufferImpl.wrap(payload.fromBase64()).decodeToTlvStructure(Encoding.BER, NullReporter)
            .onSuccess {
                // find tag in application template
                var result = it.findValueForTag(0x4F)
                assertContentEquals(byteArrayOf(0x47, 0x43, 0x41, 0x53, 0x48), result)

                // find tag in application transparent template
                result = it.findValueForTag(0xC2)
                assertContentEquals(byteArrayOf(0x00, 0x01), result)

                // do not find non-existent tag
                result = it.findValueForTag(0x4E)
                assertNull(result)

            }
            .getOrThrow()

    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun testGCashTransitQr() {

        val p1 = buildString {
            append("hQVDUFYwMWGBlk8FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNw")
            append("wRZHQy0wODAxMjAyNS0wMDAwMDg4NjU0wgIAAcQEaIyGmcgEaIxqjd5GMEQCIFJH")
            append("6M8dutmI1x23W39Mf4KgXA+l6weUAArBDGvU8ek1AiBjMZYEHVMfNRJVIVfG/wZ+")
            append("mtDhAQHmNmLqSrIwmOtbSQ==")
        }

        val p2 = buildString {
            append("hQVDUFYwMWGBlk8FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNw")
            append("wRZHQy0wODAxMjAyNS0wMDAwMTE4Mjg3wgIAAcQEaIypacgEaIyNXd5GMEQCIAgm")
            append("KMFDFNGaBHwbjefbGDL6N4hxqHI8Qc1pHxfX9xjEAiA2PtajUjAk3ApPOtNLeGPh")
            append("1ifWGV4pYFV2NHTK6zklpQ==")
        }

        val p3 = buildString {
            append("hQVDUFYwMWGBlk8FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNw")
            append("wRZHQy0wODAxMjAyNS0wMDAwMTE5NzA0wgIAAcQEaIyqb8gEaIyOY95GMEQCIHK9")
            append("R1mdx2U2ev0iDsiOBTHkQcGsC1knu08im/bBI9OCAiA106gu80AoN/lJ8VDERSZx")
            append("0Q7qipuoK9rQj8eY9EvpdQ==")
        }

//        val payload = buildString {
//            append("hQVDUFYwMWGBl08FR0NBU0haGzExMDcwMDQwMDAwMDAwMDAwMDEwODkwMzA5MGNx")
//            append("wRZHQy0wNzI1MjAyNS0wMDAwMDkzOTA4wgIAAcQEaINZd8gEaIM9a95HMEUCIGWd")
//            append("jueZsFZHMkVQSy/jlqQubfpvMk4BsQKeHJuIn5aJAiEAvNpixJCHfUCj6T/8Q8/V")
//            append("3ay0GmNFgAUdDzE+c/Ilny0=")
//        }.fromBase64().buffer()

//        println(payload.toHexChunked(lineNums = false, printable = true))

        val reporter = MemoryReporter()

        p1.fromBase64().buffer().decodeToTlvStructure(Encoding.BER, reporter)
            .onSuccess {
                println(
                    it.stringify(
                        standard = EMV_CUSTOMER, context = Context("ROOT"), reporter = reporter
                    )
                )
                println("Errors")
                println(reporter.getReports().mapIndexed { index, string ->
                    "${index + 1}: $string"
                }.joinToString("\n"))
            }
            .getOrThrow()

        p2.fromBase64().buffer().decodeToTlvStructure(Encoding.BER, reporter)
            .onSuccess {
                println(it.stringify(standard = EMV_CUSTOMER, context = Context("ROOT")))
            }
            .getOrThrow()

        p3.fromBase64().buffer().decodeToTlvStructure(Encoding.BER, reporter)
            .onSuccess {
                println(it.stringify(standard = EMV_CUSTOMER, context = Context("ROOT")))
            }
            .getOrThrow()

//        payload.decodeTlvStructure()
//            .onSuccess {
//                println(it.stringify())
//            }
//            .getOrThrow()
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun testDecodeEmvMerchantQr() {

        val reporter = MemoryReporter()
        val data =
            "00020101021128620011ph.ppmi.p2m0111GXCHPHM2XXX032121702000000551861785205030005204549953036085802PH5913Mi Store 95806006Quezon6104110662640012ph.ppmi.qrph0307C39015V05212170500000725083318550708GEN0000380660010com.alipay0148https://payqr.gcash.com/28101005101488519611265763046D64"
// 00020101021128620011ph.ppmi.p2m0111GXCHPHM2XXX032121702000000551861785205030005204549953036085802PH5913Mi Store 95806006Quezon6104110662640012ph.ppmi.qrph0307C39015V05212170500000725083318550708GEN0000380660010com.alipay0148https://payqr.gcash.com/28101005101488519611265763046D64
        BufferImpl.wrap(data.encodeToByteArray()).decodeEmvMpToTlvStructure()
            .onSuccess {
                println(it.stringify(standard = Standard.EMV_MERCHANT, context = Context("ROOT"), reporter = reporter))
                println(reporter.getReports().joinToString("\n"))
            }
            .getOrThrow()

        BufferImpl.wrap(data.encodeToByteArray()).decodeEmvMpToTlvStructure()
            .onSuccess {
                println(
                    it.stringify(
                        standard = Standard.EMV_MERCHANT, context = Context("ROOT"), hexDump = false
                    )
                )
            }
            .getOrThrow()
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun wrongVersionIndicator() {
        val data =
            "000300101021128620011ph.ppmi.p2m0111GXCHPHM2XXX032121702000000551861785205030005204549953036085802PH5913Mi Store 95806006Quezon6104110662640012ph.ppmi.qrph0307C39015V05212170500000725083318550708GEN0000380660010com.alipay0148https://payqr.gcash.com/28101005101488519611265763046D64"

        BufferImpl.wrap(data.encodeToByteArray()).decodeEmvMpToTlvStructure()
            .onSuccess {
                val reporter = MemoryReporter()
//                println(
//                    it.stringify(
//                        "", Standard.EMV_MERCHANT, Context("ROOT"), true, rawTlv = true, reporter
//                    )
//                )
                println(
                    it.stringify(
                        "", Standard.EMV_MERCHANT, Context("ROOT"), true, rawTlv = false, reporter
                    )
                )
                println("Errors")
                println(reporter.getReports().mapIndexed { index, string ->
                    "${index + 1}: $string"
                }.joinToString("\n"))
            }
            .getOrThrow()
    }
}