/*
 * Copyright 2013-2016 Netherlands Forensic Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.parsingdata.metal.formats.vhdx;

import static io.parsingdata.metal.Shorthand.add;
import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.div;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.gtNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.mul;
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.not;
import static io.parsingdata.metal.Shorthand.offset;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.formats.vhdx.Constants.GUID;
import static io.parsingdata.metal.formats.vhdx.Constants.UINT32;
import static io.parsingdata.metal.formats.vhdx.Constants.UINT64;
import static io.parsingdata.metal.formats.vhdx.Constants.UINT8;

import io.parsingdata.metal.encoding.ByteOrder;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.token.Token;

/**
 * Metal tokens for VHDX Log Sequence.
 * Based on VHDX Format Specification v1.00: https://www.microsoft.com/en-us/download/details.aspx?id=34750
 * TODO VHDX logs should be replayed when vhdx file write was aborted or incomplete
 *
 * @author Netherlands Forensic Institute.
 */
public class Log {
    private static final Token LOG_ENTRY_HEADER = seq("header",
        def("LogEntryHeaderAnchor", 0), // used for ref

        // The Signature field must be 0x65676F6C (“loge” as ASCII).
        def("Signature", UINT32, eq(con("loge"))),

        // The Checksum field is a CRC-32C hash computed over the entire entry specified by the EntryLength field,
        // with the Checksum field taking the value of zero during the computation of the checksum value.
        def("Checksum", UINT32), //, crc32c(-UINT32, 2097408, UINT32)),

        // The EntryLength field specifies the total length of the entry in bytes. The value must be a multiple of 4 KB.
        def("EntryLength", UINT32),

        // The Tail field is the byte offset of the beginning log entry of a sequence ending with this entry.
        // The value must be a multiple of 4 KB. A tail entry could point to itself as would be the case when a log is initialized.
        def("Tail", UINT32),

        // The SequenceNumber field is a 64-bit number incremented between each log entry. It must be larger than zero.
        def("SequenceNumber", UINT64, gtNum(con(0))),

        // The DescriptorCount field specifies the number of descriptors that are contained in this log entry. The value may be zero.
        def("DescriptorCount", UINT32),

        nod("Reserved", con(UINT32)),

        // The LogGuid field contains the LogGuid value in the file header that was present when this log entry was written.
        // When replaying, if this LogGuid do not match the LogGuid field in the file header, this entry must not be considered valid.
        def("LogGuid", GUID),

        // The FlushedFileOffset field stores the VHDX file size in bytes that must be at least as large as the size of the VHDX
        // file at the time the log entry was written. The file size specified in the log entry must have been stable on the host disk
        // such that, even in the case of a system power failure, a non-corrupted VHDX file will be at least as large as the size specified
        // by the log entry. Before shrinking a file while the log is in use, a parser must write the target size to a log entry and flush
        // the entry so that the update is stable on the log on the host disk storage media; this will ensure that the VHDX file is not
        // treated as truncated during log replay. A parser should write the largest possible value that satisfies these requirements.
        // The value must be a multiple of 1 MB.
        def("FlushedFileOffset", UINT64),

        // The LastFileOffset field stores a file size in bytes that all allocated file structures fit into, at the time the log entry
        // was written. A parser should write the smallest possible value that satisfies these requirements. The value must be a multiple of 1 MB.
        def("LastFileOffset", UINT64));

    private static final Token LOG_DATA_SECTOR = seq(
        // The DataSignature field must be 0x61746164 (“data” as ASCII).
        def("DataSignature", UINT32, eq(con("data"))),

        // The SequenceHigh field must contain the 4 most significant bytes of the SequenceNumber field of the associated entry.
        def("SequenceHigh", UINT32),

        // The Data field contains the raw data associated with the update, bytes 8 through 4091, inclusive.
        // Bytes 0 through 7 and 4092 through 4096 are stored in the data descriptor, in the LeadingBytes and TrailingBytes fields, respectively.
        def("Data", UINT8 * 4084), // UINT8   Data[4084];

        // The SequenceLow field must contain the 4 least significant bytes of the SequenceNumber field of the associated entry.
        def("SequenceLow", UINT32));

    private static final Token LOG_ZERO_DESCRIPTOR = seq("zeroDescriptor",
        // The ZeroSignature field must be 0x6F72657A (“zero” as ASCII).
        def("ZeroSignature", UINT32, eq(con("zero"))),

        nod("Reserved", con(UINT32)),

        // The ZeroLength field specifies the length of the section to zero. The value must be a multiple of 4 KB.
        def("ZeroLength", UINT64),

        // The FileOffset field specifies the file offset to which zeroes must be written. The value must be a multiple of 4 KB.
        def("FileOffset", UINT64),

        // The SequenceNumber field must match the SequenceNumber field of the log entry’s header.
        def("SequenceNumber", UINT64));

    private static final Encoding LE = new Encoding(ByteOrder.LITTLE_ENDIAN);

    private static final Token LOG_DATA_DESCRIPTOR = seq("dataDescriptor",
        // The DataSignature field must be 0x63736564 (“desc” as ASCII).
        def("DataSignature", UINT32, eq(con("desc"))),

        // The TrailingBytes field contains the 4 trailing bytes that were removed from the update when it was converted to a data sector.
        // These trailing bytes must be restored before the data sector is written to its final location on disk.
        def("TrailingBytes", UINT32),

        // The LeadingBytes field contains the first 8 bytes that were removed from the update when it was converted to a data sector.
        // These leading bytes must be restored before the data sector is written to its final location on disk.
        def("LeadingBytes", UINT64),

        // The FileOffset field specifies the file offset to which the data described by this descriptor must be written.
        // The value must be a multiple of 4 KB.
        def("FileOffset", UINT64, LE),

        // The SequenceNumber field must match the SequenceNumber field of the entry’s header.
        def("SequenceNumber", UINT64),

        sub("dataSector",
            LOG_DATA_SECTOR,
            add(
                offset(last(ref("LogEntryHeaderAnchor"))), // Start of this table
                // data.offset = parentTable.offset + ((((parentTable.currentEntry.offset - parentTable.offset - parentTable.header.size) / parentTable.entry.size) + 1) * page size)
                mul(
                    add(
                        div(
                            sub(
                                sub(
                                    offset(last(ref("DataSignature"))), // Start of this entry - start of this table
                                    offset(last(ref("LogEntryHeaderAnchor")))),
                                con(64)), // Minus size of table header
                            con(32)), // Divided by table entry size -> gives table entry #
                        con(1)), // Data starts at table header offset + 1 page
                    con(0x1000))))); // Multiplied by data page size

    private static final Token LOG = seq(
        LOG_ENTRY_HEADER,
        repn(
            cho(
                LOG_ZERO_DESCRIPTOR,
                LOG_DATA_DESCRIPTOR),
            last(ref("DescriptorCount"))));

    public static final Token LOGS = pre(
        sub("log",
        LOG,
            last(ref("header.LogOffset"))),
        not(eqNum(last(ref("header.LogGuid")), con(0))));
}
