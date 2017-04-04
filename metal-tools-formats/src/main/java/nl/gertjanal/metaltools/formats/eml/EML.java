package nl.gertjanal.metaltools.formats.eml;

import static io.parsingdata.metal.Shorthand.and;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.gtNum;
import static io.parsingdata.metal.Shorthand.ltNum;
import static io.parsingdata.metal.Shorthand.or;
import static io.parsingdata.metal.Shorthand.rep;
import static io.parsingdata.metal.Shorthand.seq;

import io.parsingdata.metal.encoding.ByteOrder;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.token.Token;

public class EML {

	private static final Encoding LE = new Encoding(ByteOrder.LITTLE_ENDIAN);

	private static final Token CRLF = def("crlf", 2, eq(con("\r\n")));

	private static final Expression ASCII = and(gtNum(con(31)), ltNum(con(166)));

	private static final Expression FIELD = or(
		and(gtNum(con(32)), ltNum(con(58))),
		and(gtNum(con(58)), ltNum(con(127))));

	public static final Token FORMAT = seq("message/rfc822", LE,
		rep(
			seq("field",
				defz("name", FIELD),
				def("semicolon", 2, eq(con(": "))),
				defz("value", ASCII),
				CRLF)),
		CRLF);

	private static Token defz(final String name, final Expression predicate) {
		return new Defz(name, predicate, null);
	}
}
