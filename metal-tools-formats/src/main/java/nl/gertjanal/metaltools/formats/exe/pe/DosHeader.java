package nl.gertjanal.metaltools.formats.exe.pe;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eqStr;
import static io.parsingdata.metal.Shorthand.seq;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.LONG;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.POINTER;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.SHORT;

import io.parsingdata.metal.token.Token;

/**
 * DOS header.
 *
 * @author Gertjan Al.
 */
public class DosHeader {

	static final Token DOS_HEADER = seq("DosHeader",
		def("signature", 2, eqStr(con("MZ"))),
		def("lastsize", SHORT),
		def("nblocks", SHORT),
		def("nreloc", SHORT),
		def("hdrsize", SHORT),
		def("minalloc", SHORT),
		def("maxalloc", SHORT),
		def("*ss", POINTER),
		def("*sp", POINTER),
		def("checksum", SHORT),
		def("*ip", POINTER),
		def("*cs", POINTER),
		def("relocpos", SHORT),
		def("noverlay", SHORT),
		def("reserved1", SHORT * 4),
		def("oem_id", SHORT),
		def("oem_info", SHORT),
		def("reserved2", SHORT * 10),
		def("e_lfanew", LONG));
}
