package nl.gertjanal.metaltools.formats.exe.pe;

import static java.nio.charset.StandardCharsets.US_ASCII;

import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.encoding.ByteOrder.LITTLE_ENDIAN;
import static io.parsingdata.metal.encoding.Sign.UNSIGNED;
import static nl.gertjanal.metaltools.formats.exe.pe.CodeSection.CODE_SECTIONS;
import static nl.gertjanal.metaltools.formats.exe.pe.CoffHeader.COFF_HEADER;
import static nl.gertjanal.metaltools.formats.exe.pe.DosHeader.DOS_HEADER;
import static nl.gertjanal.metaltools.formats.exe.pe.PeHeaders.PE_HEADER;
import static nl.gertjanal.metaltools.formats.exe.pe.PeHeaders.PE_OPTIONAL_HEADER;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.token.Token;

/**
 * Exe PE COFF.
 * Specification can be found at: http://download.microsoft.com/download/9/c/5/9c5b2167-8017-4bae-9fde-d599bac8184a/pecoff_v83.docx
 *
 * @author Gertjan Al.
 */
public class EXE {

	private static final Encoding ENCODING = new Encoding(UNSIGNED, US_ASCII, LITTLE_ENDIAN);

	public static final Token FORMAT = seq(ENCODING,
		DOS_HEADER,
		sub(
			seq(
				PE_HEADER,
				COFF_HEADER,
				PE_OPTIONAL_HEADER,
				CODE_SECTIONS
			),
			last(ref("e_lfanew"))));
}
