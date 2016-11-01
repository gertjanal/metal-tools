/**
 * Copyright 2016 Gertjan Al
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

package nl.gertjanal.metaltools.formats.vhdx;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.seq;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.GUID;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UINT16;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UINT32;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UINT64;

import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.token.Token;

/**
 * Metal tokens for VHDX Header. Based on VHDX Format Specification v1.00:
 * https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Gertjan Al.
 */
public class Header {
	public static Token header(final Expression sequenceNumberPredicate) {
		return seq(
			// The Signature field must be 0x64616568 (“head” as ASCII).
			def("Signature", UINT32, eq(con("head"))),

			// The Checksum field is a CRC-32C hash over the entire 4 KB
			// structure,
			// with the Checksum field taking the value of zero during the
			// computation of the checksum value.
			// TODO VHDX should check crc32c
			def("Checksum", UINT32),

			// The SequenceNumber field is a 64-bit unsigned integer.
			// A header is valid if Signature and Checksum both validate
			// correctly.
			// A header is current if it is the only valid header or if it is
			// valid and its SequenceNumber field
			// is greater than the other header’s SequenceNumber field.
			// The parser must only use data from the current header.
			// If there is no current header, then the VHDX file is corrupt.
			def("SequenceNumber", UINT64, sequenceNumberPredicate),

			// The FileWriteGuid field specifies a 128-bit unique identifier
			// that identifies the file’s contents.
			// On every open of a VHDX file, a parser must change this GUID to a
			// new and unique identifier before
			// the first modification is made to the file, including system and
			// user metadata as well as log playback1.
			// The parser can skip updating this field if the storage media on
			// which the file is stored is read-only,
			// or if the file is opened in read-only mode.
			def("FileWriteGuid", GUID),

			// The DataWriteGuid field specifies a 128-bit unique identifier
			// that identifies the contents of the user
			// visible data. On every open of the VHDX file, a parser must
			// change this field to a new and unique
			// identifier before the first modification is made to user visible
			// data. If the user of the virtual disk
			// can observe the change through a virtual disk read, then the
			// parser must update this field.
			// This includes changing the system and user metadata, raw block
			// data, disk size or any block state
			// transitions that will result in a virtual disk sector read being
			// different from a previous read.
			// Notably, this does not include movement of blocks within a file,
			// because this only changes the physical
			// layout of the file, not the virtual identity.
			def("DataWriteGuid", GUID),

			// The LogGuid field specifies a 128-bit unique identifier used to
			// determine the validity of log entries.
			// If this field is zero, then the log is empty or has no valid
			// entries and must not be replayed.
			// Otherwise, only log entries that contain this identifier in their
			// header are valid log entries.
			// Upon open, the parser must update this field to a new non-zero
			// value before overwriting existing space
			// within the log region.
			def("LogGuid", GUID),

			// The LogVersion field specifies the version of the log format used
			// within the VHDX file.
			// This field must be set to zero. If it is not, the parser must not
			// continue to parse the file unless the
			// LogGuid field is zero, indicating that there is no log to replay.
			def("LogVersion", UINT16, eqNum(con(0))),

			// The Version field specifies the version of the VHDX format used
			// within the VHDX file.
			// This field must be set to 1. If it is not, a parser must not
			// attempt to parse the file using the details
			// from this format specification.
			def("Version", UINT16, eqNum(con(1))),

			// The LogLength and LogOffset fields specify the byte offset in the
			// file and the length of the log.
			// These values must be multiples of 1 MB and LogOffset must be at
			// least 1 MB.
			// The log must not overlap any other structures.
			def("LogLength", UINT32),
			def("LogOffset", UINT64),

			// Reserved
			nod("Reserved", con(4016)));
	}
}
