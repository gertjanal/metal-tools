package nl.gertjanal.metaltools.formats;

import static io.parsingdata.metal.Shorthand.add;
import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.not;
import static io.parsingdata.metal.Shorthand.offset;
import static io.parsingdata.metal.Shorthand.or;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.rep;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.str;
import static io.parsingdata.metal.Shorthand.sub;

import io.parsingdata.metal.encoding.ByteOrder;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.token.Token;

public class MP4 {

	private static final Encoding BE = new Encoding(ByteOrder.BIG_ENDIAN);
	private static final Encoding LE = new Encoding(ByteOrder.LITTLE_ENDIAN);

	public static final Token ATOM = atom(null);
	public static final Token ATOM8 = atom8(null);
	public static final Token ATOM16 = atom16(null);

	private static Token atom16(final String name) {
		return seq(name == null ? "atom" : "atom." + name,
			def("anchor", 0),
			def("marker", 4, eqNum(con(1))),
			def("name", 4, name(name)),
			def("size", 8));
	}

	private static Token atom(final String name) {
		return cho(
			atom8(name),
			atom16(name));
	}

	private static Token atom8(final String name) {
		return cho(name == null ? "atom" : "atom." + name,
			seq(
				def("anchor", 0),
				def("eof", 4, eqNum(con(0))), // TODO size up to end of file, including token
				def("name", 4, name(name))),
			seq(
				def("anchor", 0),
				def("size", 4, not(eqNum(con(1)))),
				def("name", 4, name(name))));
	}

	private static Expression name(final String name) {
		return name == null ? null : eq(con(name, LE));
	}

	private static final Expression IS_CONTAINER_ATOM = or(
		// search these atoms for child atoms (e.g. don't skip with nod())
		eq(last(ref("name")), con("moov", LE)),
		or(
			eq(last(ref("name")), con("trak", LE)),
			or(
				eq(last(ref("name")), con("mdia", LE)),
				or(
					eq(last(ref("name")), con("minf", LE)),
					eq(last(ref("name")), con("stbl", LE))))));

	public static final Token STSC = seq(
		atom8("stsc"),
		def("stsc.sub.anchor", 0),
		nod(sub(last(ref("seek.atom.size")), sub(offset(last(ref("atom.stsc.anchor"))), offset(last(ref("atom.anchor")))))));

	public static final Token STCO = seq(
		atom8("stco"),
		def("stco.sub.anchor", 0),
		nod(sub(last(ref("seek.atom.size")), sub(offset(last(ref("atom.stco.anchor"))), offset(last(ref("atom.anchor")))))));

	public static final Token STSD = seq(
		atom8("stsd"),
		def("stsd.sub.anchor", 0),
		nod(sub(last(ref("seek.atom.size")), sub(offset(last(ref("atom.stsd.anchor"))), offset(last(ref("atom.anchor")))))),

		sub(
			seq(
				def("num_entries", 4),
				repn(
					cho(
						seq(
							cho(
								atom8("avc1"), // H.264
								atom("mp4v")), // MPEG-4 Video
							nod(con(0x4e)),

							cho(
								seq(
									atom8("avcC"),
									nod(con(6)), // TODO check if the +6 is always correct
									def("sps_length", 2),
									def("sps", last(ref("sps_length"))), // Sequence Parameter Set
									nod(con(1)), // TODO check if this byte must always be skipped
									def("pps_length", 2),
									def("pps", last(ref("pps_length"))) // Picture Parameter Set
								),
								seq(
									atom8("esds"),
									nod(con(25)), // TODO check if the +25 is always correct
									def("header_length", 1)))),
						ATOM),
					last(ref("num_entries")))),
			add(con(4), offset(last(ref("stsd.sub.anchor"))))));

	public static final Token FORMAT = rep(
		cho(
			STSD,
			STSC,
			seq(
				str("seek", ATOM16),
				pre(
					nod(sub(last(ref("seek.atom.size")), con(16))),
					not(IS_CONTAINER_ATOM))),
			seq(
				str("seek", ATOM8),
				pre(
					nod(sub(last(ref("seek.atom.size")), con(8))),
					not(IS_CONTAINER_ATOM)))),
		BE);
}
