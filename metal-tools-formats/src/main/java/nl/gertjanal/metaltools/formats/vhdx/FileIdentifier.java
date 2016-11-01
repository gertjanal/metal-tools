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
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.not;
import static io.parsingdata.metal.Shorthand.seq;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UINT64;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UTF_16;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.token.Token;

/**
 * Metal tokens for VHDX File Identifier. Based on VHDX Format Specification
 * v1.00: https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Gertjan Al.
 */
public class FileIdentifier {
	public static final Token FILE_IDENTIFIER = seq("fileIdentifier",
		// The Signature field must be 0x656C696678646876 (“vhdxfile” as ASCII).
		def("Signature", UINT64, eq(con("vhdxfile"))),

		// The Creator field contains a UTF-16 string describing the parser that
		// created the VHDX file.
		// This field may not be null terminated. This field is optional for the
		// parser to fill in during the
		// creation of the VHDX file to identify, uniquely, the creator of the
		// VHDX file.
		// Parsers must not use this field as a mechanism to influence parser
		// behavior; it only exists for diagnostic purposes.
		// A parser must write File Type Identifier structure when the file is
		// created and must validate the
		// Signature field when loading a VHDX file. The parser must not
		// overwrite any data in the first 64 KB of
		// the file after the file has been created.
		nullTerminated("Creator", 256, UTF_16),

		// The space between file identifier data and 64 KB alignment boundary
		// for the file identifier structure is reserved.
		nod(con(0x10000 - 256 - UINT64)));

	/**
	 * Create a cho for every possibility with def(size) + def(total - size) to
	 * get a null terminated String. For example 'title\u0000\u0000\u0000', a
	 * def("title") and def("nod") will be returned.
	 *
	 * @param name
	 *            Name to set on the String token.
	 * @param size
	 *            Total byte size
	 * @param encoding
	 *            Encoding of the String
	 * @return cho() of all possible def(size) + def(total - size)
	 */
	private static Token nullTerminated(final String name, final int size, final Encoding encoding) {
		int bytesPerChar = 1;
		if (encoding != null && encodingContains(encoding, "-16")) {
			bytesPerChar = 2;
		}
		final List<Token> seqs = new ArrayList<>();
		for (int i = 0; i < size; i += bytesPerChar) {
			seqs.add(seq(
				def(name, i, not(eqNum(con(0))), encoding),
				def("nod", size - i, eqNum(con(0)), encoding)));
		}
		return cho(seqs.toArray(new Token[0]));
	}

	private static boolean encodingContains(final Encoding encoding, final String value) {
		return encoding.getCharset() != null && encoding.getCharset().displayName(Locale.ROOT).contains(value);
	}
}
