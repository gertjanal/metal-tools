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

import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.expression.value.GUID.guid;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.GUID;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UINT32;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UINT64;
import static nl.gertjanal.metaltools.formats.vhdx.Metadata.METADATA_TABLE_HEADER;

import io.parsingdata.metal.token.Token;

/**
 * Metal tokens for VHDX Region. Based on VHDX Format Specification v1.00:
 * https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Gertjan Al.
 */
public class Region {
	public static final String FILE_OFFSET_NAME = "region.bat.FileOffset";

	private static final Token REGION_TABLE_HEADER = seq("regionTableHeader",
		// The Signature field must be 0x69676572 (“regi” as ASCII).
		def("Signature", UINT32, eq(con("regi"))),

		// The Checksum field is a CRC-32C hash over the entire 64 KB table,
		// with the Checksum field taking the value of zero during the
		// computation of the checksum value.
		def("Checksum", UINT32),

		// The EntryCount field specifies the number of valid entries to follow.
		// This must be less than or equal to 2047.
		def("EntryCount", UINT32),
		nod("Reserved", con(UINT32)));

	private static Token regionTableEntry(final boolean resolveData) {
		return cho(
			regionTableEntry("bat", "2dc27766-f623-4200-9d64-115e9bfd4a08", nod(con(0))),
			regionTableEntry("metadata", "8b7ca206-4790-4b9a-b8fe-575f050f886e",
				resolveData ? METADATA_TABLE_HEADER : nod(con(0))),
			nod(con(GUID + UINT64 + UINT32 + UINT32))); // Unknown region,
														// allowed according to
														// spec
	}

	private static Token regionTableEntry(final String name, final String guid, final Token token) {
		return seq(name,
			// The Guid field specifies a 128-bit identifier for the object and
			// must be unique within the table.
			def("Guid", GUID, eq(guid(guid))),

			// The FileOffset and Length fields specify the 64-bit byte offset
			// and 32-bit byte length of the object within the file.
			// The values must be a multiple of 1 MB, and FileOffset must be at
			// least 1 MB.
			// All objects within the table must be non-overlapping, not only
			// with respect to each other but with respect to the log (defined
			// in the headers) and payload and sector bitmap blocks (defined in
			// the BAT).
			def("FileOffset", UINT64),
			def("Length", UINT32),

			// The Required field specifies whether this region must be
			// recognized by the parser in order to load the VHDX file.
			// If this field’s value is 1 and the parser does not recognize this
			// region, the parser must refuse to load the VHDX file.
			def("Required", UINT32, eqNum(con(1))),

			sub(
				token,
				last(ref(name + ".FileOffset"))));
	}

	public static Token region() {
		return region(true);
	}

	public static Token oldRegion() {
		return region(false);
	}

	private static Token region(final boolean resolveData) {
		return seq(
			REGION_TABLE_HEADER,
			repn(regionTableEntry(resolveData), last(ref("regionTableHeader.EntryCount"))));
	}
}
