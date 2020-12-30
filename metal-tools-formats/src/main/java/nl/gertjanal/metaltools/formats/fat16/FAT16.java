package nl.gertjanal.metaltools.formats.fat16;

import static java.nio.charset.StandardCharsets.US_ASCII;

import static io.parsingdata.metal.Shorthand.SELF;
import static io.parsingdata.metal.Shorthand.add;
import static io.parsingdata.metal.Shorthand.and;
import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.div;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.mul;
import static io.parsingdata.metal.Shorthand.not;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.rep;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.encoding.ByteOrder.LITTLE_ENDIAN;
import static io.parsingdata.metal.encoding.Sign.UNSIGNED;

import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.value.BinaryValueExpression;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.token.Token;

/**
 * NOTE: This Token is incomplete.
 * Based on http://download.microsoft.com/download/1/6/1/161ba512-40e2-4cc9-843a-923143f3456c/fatgen103.doc
 *
 * @author Gertjan Al
 */
public class FAT16 {
	private static final int BYTE = 1;
	private static final int WORD = 2;
	private static final int DOUBLE_WORD = WORD * 2;
	public static final Encoding ENC = new Encoding(UNSIGNED, US_ASCII, LITTLE_ENDIAN);

	private static final Token BOOT_SECTOR = seq("BootSector",
		def("jmpBoot", 3),
		def("OEMName", 8),
		def("BytsPerSec", WORD),
		def("SecPerClus", BYTE),
		def("RsvdSecCnt", WORD),
		def("NumFATs", BYTE),
		def("RootEntCnt", WORD),
		def("TotSec16", WORD),
		def("Media", BYTE), // f8 = hard disk; floppy disks are f0;
		def("FATSz16", WORD),
		def("SecPerTrk", WORD),
		def("NumHeads", WORD),
		def("HiddSec", DOUBLE_WORD),
		def("TotSec32", DOUBLE_WORD),
		def("DrvNum", BYTE),
		def("Reserved1", BYTE),
		def("BootSig", BYTE),
		def("VolID", DOUBLE_WORD),
		def("VolLab", 11),
		def("FilSysType", 8),
		def("executable_code", 448),
		def("executable_marker", 2, eq(con(0x55, 0xaa))));

	public static final ValueExpression FAT_REGION_REGION = add(
		con(2), // first sector?
		last(ref("RsvdSecCnt")));

	public static final ValueExpression ROOT_DIRECTORY_REGION = add(
		FAT_REGION_REGION,
		mul(
			last(ref("NumFATs")),
			last(ref("FATSz16"))));

	public static final BinaryValueExpression ROOT_DIRECTORY_REGION_START = mul(
		ROOT_DIRECTORY_REGION,
		last(ref("BytsPerSec")));

	public static final ValueExpression DATA_REGION_START = add(
		ROOT_DIRECTORY_REGION,
		div(
			mul(
				last(ref("RootEntCnt")),
				con(32)),
			last(ref("BytsPerSec"))));

	private static final Token SHORT_ENTRY = seq("ShortName",
		cho(
			seq(
				def("Deleted", BYTE, eq(con(0xe5))),
				def("Name", 10, not(eqNum(con(0))))),
			def("Name", 11, not(eqNum(con(0))))),
		cho(
			def("READ_ONLY", BYTE, eq(con(0x01))),
			def("HIDDEN", BYTE, eq(con(0x02))),
			def("SYSTEM", BYTE, eq(con(0x04))),
			def("VOLUME_ID", BYTE, eq(con(0x08))),
			def("DIRECTORY", BYTE, eq(con(0x10))),
			def("ARCHIVE", BYTE, eq(con(0x20))),
			def("LONG_NAME ", BYTE) // TODO ATTR_READ_ONLY | ATTR_HIDDEN | ATTR_SYSTEM | ATTR_VOLUME_ID
		),
		def("NTRes", BYTE),
		def("CrtTimeTenth", BYTE),
		def("CrtTime", WORD),
		def("CrtDate", WORD),
		def("LstAccDate", WORD),
		def("FstClusHI", WORD, eq(con(0x00, 0x00))),
		def("WrtTime", WORD),
		def("WrtDate", WORD),
		def("FstClusLO", WORD),
		def("FileSize", DOUBLE_WORD));

	private static final Token LONG_ENTRY = seq("LongName",
		cho(
			def("Deleted", BYTE, eq(con(0xe5))),

			// TODO masked with 0x40; (repn)
			// see http://www.beginningtoseethelight.org/fat16/index.htm
			// data entry diagram
			def("Ordinal", BYTE, eqNum(and(SELF, con(0x40)), con(0x40)))),
		def("Name1", 10),
		def("Attr", BYTE, eq(con(0x0f))),
		def("Type", BYTE, eq(con(0x00))),
		def("Chksum", BYTE),
		def("Name2", 12),
		def("FstClusLO", WORD, eq(con(0x00, 0x00))),
		def("Name3", 4),
		SHORT_ENTRY);

	private static final Token DIRECTORY_ENTRY = cho(
		LONG_ENTRY,
		SHORT_ENTRY);

	public static final Token FORMAT = seq(ENC,
		BOOT_SECTOR,
		def("FAT_ID", BYTE, eq(last(ref("Media")))),
		def("Remaining", BYTE, eq(con(0xff))),
		cho(
			def("Clean", 2, eq(con(0xff, 0xff))),
			def("Dirty", 2, eq(con(0xff, 0xf7)))),

		sub(
			rep(DIRECTORY_ENTRY),
			ROOT_DIRECTORY_REGION_START),


		// TODO: debug below
		sub(
			rep(DIRECTORY_ENTRY),
			con(0x10200)),

		sub(
			rep(DIRECTORY_ENTRY),
			con(0x10a00)),

		sub(
			rep(DIRECTORY_ENTRY),
			con(0x11200)),

		// file in folder A
		sub(
			def("file in A", 0x25),
			con(0x12a00)),

		// file in folder B
		sub(
			def("file in B", 0x23),
			con(0x14200)),

		// file in folder C
		sub(
			def("file in C", 0x23),
			con(0x15a00)));
}
