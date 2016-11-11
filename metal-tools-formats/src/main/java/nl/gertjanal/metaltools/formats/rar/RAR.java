package nl.gertjanal.metaltools.formats.rar;

import static io.parsingdata.metal.Shorthand.and;
import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.not;
import static io.parsingdata.metal.Shorthand.offset;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;

import io.parsingdata.metal.expression.logical.UnaryLogicalExpression;
import io.parsingdata.metal.token.Token;

/**
 * The RAR archive format.
 * RAR 5.0: {@link http://www.rarlab.com/technote.htm}
 * RAR 4.x: {@link http://www.forensicswiki.org/wiki/RAR}
 *
 * @author Netherlands Forensic Institute.
 */
public class RAR {
	public static final int BYTE = 1;
	public static final int UINT16 = 16 / 8;
	public static final int UINT32 = 32 / 8;
	public static final int UINT64 = 64 / 8;

	private static final Token MAIN_HEAD = seq(
		def("HEAD_CRC", UINT16),
		def("HEAD_TYPE", BYTE, eq(con(0x73))),
		def("HEAD_FLAGS", UINT16),
		def("HEAD_SIZE", UINT16),
		def("RESERVED1", UINT16),
		def("RESERVED2", UINT32));

	private static final Token MARKER_BLOCK = seq(
		def("HEAD_CRC", UINT16, eq(con(0x52, 0x61))),
		def("HEAD_TYPE", BYTE, eq(con(0x72))),
		def("HEAD_FLAGS", UINT16, eq(con(0x21, 0x1A))),
		def("HEAD_SIZE", UINT16, eq(con(0x07, 0x00))));

	private static final Token FOOTER = seq(
		def("HEAD_CRC", UINT16, eq(con(0xC4, 0x3D))),
		def("HEAD_TYPE", BYTE, eq(con(0x7b))),
		def("HEAD_FLAGS", UINT16, eq(con(0x00, 0x40))),
		def("HEAD_SIZE", UINT16, eq(con(0x07, 0x00))));

	private static final Token FILE_HEADER = seq(
		def("HEAD_CRC", UINT16),
		def("HEAD_TYPE", BYTE, eq(con(0x74))),
		def("HEAD_FLAGS", UINT16),
		def("HEAD_SIZE", UINT16),
		def("PACK_SIZE", UINT32),
		def("UNP_SIZE", UINT32),
		cho("HOST_OS",
			def("MS DOS", BYTE, eq(con(0))),
			def("OS/2 ", BYTE, eq(con(1))),
			def("Windows", BYTE, eq(con(2))),
			def("Unix", BYTE, eq(con(3))),
			def("Mac OS", BYTE, eq(con(4))),
			def("BeOS", BYTE, eq(con(5)))),
		def("FILE_CRC", UINT32),
		def("FTIME", UINT32),
		def("UNP_VER", BYTE),
		cho("METHOD",
			def("Storing", BYTE, eq(con(0x30))),
			def("Fastest Compression", BYTE, eq(con(0x31))),
			def("Fast Compression", BYTE, eq(con(0x32))),
			def("Normal Compression", BYTE, eq(con(0x33))),
			def("Good Compression", BYTE, eq(con(0x34))),
			def("Best Compression", BYTE, eq(con(0x35)))),
		def("NAME_SIZE", UINT16),
		def("ATTR", UINT32),
		pre(
			seq(
				def("HIGH_PACK_SIZE", UINT32),
				def("HIGH_UNP_SIZE", UINT32)),
			flag(0x100)),
		def("FILE_NAME", last(ref("NAME_SIZE"))),
		pre(
			def("SALT", UINT64),
			flag(0x400)),
		def("Anchor", 0),
		def("EXT_TIME", sub(last(ref("HEAD_SIZE")), sub(offset(last(ref("Anchor"))), offset(last(ref("HEAD_CRC")))))),
		def("FILE", last(ref("PACK_SIZE")))); // eqNum(crc32(self), last(ref("FILE_CRC"))))); only works for Storing files.

	public static final Token FORMAT = cho(
		def("rar 5 signature", 8, eq(con(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00))),
		seq(
			MARKER_BLOCK,
			MAIN_HEAD,
			FILE_HEADER,
			FOOTER
		));

	private static UnaryLogicalExpression flag(final long flag) {
		return not(eqNum(and(last(ref("HEAD_FLAGS")), con(flag)), con(0)));
	}
}
