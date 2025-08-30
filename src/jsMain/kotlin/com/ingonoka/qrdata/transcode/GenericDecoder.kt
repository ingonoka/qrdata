/*
 * Copyright (c) 2025. Ingo Noka
 * This file belongs to project qrdata.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.ingonoka.qrdata.transcode

import com.ingonoka.hexutils.hexToListOfInt
import com.ingonoka.qrdata.utils.MemoryReporter
import com.ingonoka.utils.*

/**
 * Function to show content of QR code data. The function can be used in
 * a Javascript
 *
 * ```javascript
 * <script src="/js/qrdata.js"></script>
 *
 * <script>
 * function analyzeData() {
 *         // Get the input data and format
 *         const data = document.getElementById('dataInput').value.trim();
 *         // Get selected encoding
 *         const encoding = document.querySelector('input[name="encoding"]:checked').value;
 *         // Get selected tlv encoding
 *         const tlv_encoding = document.querySelector('input[name="tlv_encoding"]:checked').value;
 *         // Get selected standard
 *         const standard = document.querySelector('input[name="standard"]:checked').value;
 *         // Get additional options
 *         const hexDump = document.querySelector('input[name="hex_dump"]').checked;
 *         const tlvOnly = document.querySelector('input[name="tlv_only"]').checked;
 *
 *         const analysisResult = qrdata.com.ingonoka.qrdata.transcode.stringifyData(
 *                                     encoding,
 *                                     tlv_encoding,
 *                                     standard,
 *                                     hexDump,
 *                                     tlvOnly,
 *                                     data
 *                                 )
 *
 *         // Display the results
 *         document.getElementById('analysisOutput').innerText = analysisResult;
 *  }
 * </script>
 *
 * ```
 *
 * The [format] for the input string can be "base64", "hex" or "ascii".
 *
 * The [tlvEncoding] can be "ber" or "ascii_emv".
 *
 * The [standard] is  "emv_customer", "emv_merchant" or "asn1".
 *
 * The result includes a hex dum for each TLV object if [hexDump] is true.
 *
 * Names and pretty printing of TLV values is omitted if [rawTlv] is true.
 *
 *
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun stringifyData(
    format: String,
    tlvEncoding: String,
    standard: String,
    hexDump: Boolean,
    rawTlv: Boolean,
    data: String
): String {
    val reporter = MemoryReporter()

    return try {

        val buf = when (format) {
            "base64" -> data.fromBase64()
            "hex" -> data.hexToListOfInt()
            "ascii" -> data.encodeToByteArray().toListOfInt()
            else -> return "String encoding not recognized: $format"
        }.buffer()

        val reporter = MemoryReporter()

        val tlvList = when (tlvEncoding) {
            "ber" -> buf.decodeToTlvStructure(Encoding.BER, reporter)
            "ascii_emv" -> buf.decodeToTlvStructure(Encoding.ASCII, reporter)
            else -> Result.failure(Exception("TLV encoding format not recognized: $tlvEncoding"))
        }.getOrThrow()

        val stringified = when (standard) {
            "emv_customer" -> tlvList.stringify(
                "", Standard.EMV_CUSTOMER, Context("ROOT"), hexDump, rawTlv, reporter
            )

            "emv_merchant" -> tlvList.stringify(
                "", Standard.EMV_MERCHANT, Context("ROOT"), hexDump, rawTlv, reporter
            )
            "asn1" -> tlvList.stringify(
                "", Standard.ASN1, Context("ROOT"), hexDump, rawTlv, reporter
            )

            else -> "Standard not recognized."
        }

        "$stringified\n${reporter.getReports().joinToString("\n")}"

    } catch (e: Exception) {
        reporter.addReports(e.collectExceptionMessages())
        reporter.getReports().joinToString("\n")
    }
}