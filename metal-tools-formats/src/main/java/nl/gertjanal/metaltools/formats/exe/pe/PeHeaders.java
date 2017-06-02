package nl.gertjanal.metaltools.formats.exe.pe;

import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.eqStr;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.seq;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.CHAR;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.LONG;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.SHORT;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.comparison.ComparisonExpression;
import io.parsingdata.metal.expression.value.OptionalValue;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.token.Token;

public class PeHeaders {
	private static final int TYPE_X86 = 0x10b;
	private static final int TYPE_X64 = 0x20b;
	private static final String PE_TYPE = "peArchitectureType";
	private static final ComparisonExpression IS_X86 = eqNum(last(ref(PE_TYPE)), con(TYPE_X86));
	private static final ValueExpression WORD_SIZE = ifThenElse(IS_X86, con(LONG), con(LONG * 2));

	static final Token PE_HEADER = seq("PE Header",
		def("PE_HEADER", 2, eqStr(con("PE"))),
		def("empty", 2, eq(con(0), con(0))));

	static final Token PE_OPTIONAL_HEADER = seq("PE Optional Header",
		cho(
			def(PE_TYPE, SHORT, eqNum(con(TYPE_X86))), // x86
			def(PE_TYPE, SHORT, eqNum(con(TYPE_X64))), // x64
			def(PE_TYPE, SHORT, eqNum(con(0x107)))), // Rom image
		def("MajorLinkerVersion", CHAR),
		def("MinorLinkerVersion", CHAR),
		def("SizeOfCode", LONG),
		def("SizeOfInitializedData", LONG),
		def("SizeOfUninitializedData", LONG),
		def("AddressOfEntryPoint", LONG),
		def("BaseOfCode", LONG),
		pre(
			// PE32 contains this additional field, which is absent in PE32+, following BaseOfCode.
			def("BaseOfData", LONG),
			IS_X86),

		// The next 21 fields are an extension to the COFF optional header format
		def("ImageBase", WORD_SIZE),
		def("SectionAlignment", LONG),
		def("FileAlignment", LONG),
		def("MajorOSVersion", SHORT),
		def("MinorOSVersion", SHORT),
		def("MajorImageVersion", SHORT),
		def("MinorImageVersion", SHORT),
		def("MajorSubsystemVersion", SHORT),
		def("MinorSubsystemVersion", SHORT),
		def("Win32VersionValue", LONG, eqNum(con(0))),
		def("SizeOfImage", LONG),
		def("SizeOfHeaders", LONG),
		def("Checksum", LONG),
		cho(
			def("Unknown subsystem", SHORT, eqNum(con(0))),
			def("No subsystem required (device drivers and native system processes)", SHORT, eqNum(con(1))),
			def("Windows graphical user interface (GUI) subsystem", SHORT, eqNum(con(2))),
			def("Windows character-mode user interface (CUI) subsystem", SHORT, eqNum(con(3))),
			def("OS/2 CUI subsystem", SHORT, eqNum(con(5))),
			def("POSIX CUI subsystem", SHORT, eqNum(con(7))),
			def("Windows CE system", SHORT, eqNum(con(9))),
			def("Extensible Firmware Interface (EFI) application", SHORT, eqNum(con(10))),
			def("EFI driver with boot services", SHORT, eqNum(con(11))),
			def("EFI driver with run-time services", SHORT, eqNum(con(12))),
			def("EFI ROM image", SHORT, eqNum(con(13))),
			def("Xbox system", SHORT, eqNum(con(14))),
			def("Boot application", SHORT, eqNum(con(16)))),
		def("DLLCharacteristics", SHORT),
		def("SizeOfStackReserve", WORD_SIZE),
		def("SizeOfStackCommit", WORD_SIZE),
		def("SizeOfHeapReserve", WORD_SIZE),
		def("SizeOfHeapCommit", WORD_SIZE),
		def("LoaderFlags", LONG),
		def("NumberOfRvaAndSizes", LONG, eqNum(con(16))),

		dataDirectory("ExportTable"),
		dataDirectory("ImportTable"),
		dataDirectory("ResourceTable"),
		dataDirectory("ExceptionTable"),
		dataDirectory("CertificateTable"),
		dataDirectory("BaseRelocationTable"),
		dataDirectory("DebugTable"),
		dataDirectory("ArchitectureTable"),
		dataDirectory("GlobalPointerRegisterTable"),
		dataDirectory("ThreadLocalStorageTable"),
		dataDirectory("LoadConfigurationTable"),
		dataDirectory("BoundImportTable"),
		dataDirectory("ImportAddressTable"),
		dataDirectory("DelayImportDescriptor"),
		dataDirectory("CLRruntimeHeader"),
		nod(con(LONG * 2)));

	private static Token dataDirectory(final String name) {
		return seq(name,
			def("offset", LONG),
			def("size", LONG));
	}

	private static ValueExpression ifThenElse(final Expression isX86, final ValueExpression ifThen, final ValueExpression ifElse) {
		return new ValueExpression() {

			@Override
			public ImmutableList<OptionalValue> eval(final Environment env, final Encoding enc) {
				if (isX86.eval(env, enc)) {
					return ifThen.eval(env, enc);
				}
				return ifElse.eval(env, enc);
			}
		};
	}
}
