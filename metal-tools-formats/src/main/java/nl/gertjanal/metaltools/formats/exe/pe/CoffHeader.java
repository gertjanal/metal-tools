package nl.gertjanal.metaltools.formats.exe.pe;

import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.seq;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.LONG;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.SHORT;

import io.parsingdata.metal.token.Token;

/**
 * Coff header.
 *
 * @author Gertjan Al.
 */
public class CoffHeader {

	static final Token COFF_HEADER = seq("COFF Header",
		cho(
			def("Intel 386", SHORT, eqNum(con(0x14c))),
			def("x64", SHORT, eqNum(con(0x8664))),
			def("MIPS R3000", SHORT, eqNum(con(0x162))),
			def("MIPS R10000", SHORT, eqNum(con(0x168))),
			def("MIPS little endian WCI v2", SHORT, eqNum(con(0x169))),
			def("old Alpha AXP", SHORT, eqNum(con(0x183))),
			def("Alpha AXP", SHORT, eqNum(con(0x184))),
			def("Hitachi SH3", SHORT, eqNum(con(0x1a2))),
			def("Hitachi SH3 DSP", SHORT, eqNum(con(0x1a3))),
			def("Hitachi SH4", SHORT, eqNum(con(0x1a6))),
			def("Hitachi SH5", SHORT, eqNum(con(0x1a8))),
			def("ARM little endian", SHORT, eqNum(con(0x1c0))),
			def("Thumb", SHORT, eqNum(con(0x1c2))),
			def("Matsushita AM33", SHORT, eqNum(con(0x1d3))),
			def("PowerPC little endian", SHORT, eqNum(con(0x1f0))),
			def("PowerPC with floating point support", SHORT, eqNum(con(0x1f1))),
			def("Intel IA64", SHORT, eqNum(con(0x200))),
			def("MIPS16", SHORT, eqNum(con(0x266))),
			def("Motorola 68000 series", SHORT, eqNum(con(0x268))),
			def("Alpha AXP 64-bit", SHORT, eqNum(con(0x284))),
			def("MIPS with FPU", SHORT, eqNum(con(0x366))),
			def("MIPS16 with FPU", SHORT, eqNum(con(0x466))),
			def("EFI Byte Code", SHORT, eqNum(con(0xebc))),
			def("AMD AMD64", SHORT, eqNum(con(0x8664))),
			def("Mitsubishi M32R little endian", SHORT, eqNum(con(0x9041))),
			def("clr pure MSIL", SHORT, eqNum(con(0xc0ee)))),
		def("NumberOfSections", SHORT),
		def("CreatedOn", LONG),
		def("PointerToSymbolTable", LONG),
		def("NumberOfSymbols", LONG),
		def("SizeOfOptionalHeader", SHORT),
		def("Characteristics", SHORT));
}
