package nl.gertjanal.metaltools.formats.eml;

import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.*;
import static io.parsingdata.metal.Shorthand.seq;

import java.util.ArrayList;
import java.util.List;

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
	
	private static Token text(final String name, final Expression predicate) {
		final List<Token> cho = new ArrayList<>();
		for(int i = 255; i >= 2; i--) {
			final Token[] seq = new Token[i];
			for (int j = 0; j < i; j++) {
				seq[j] = def(name + "_char", 1, predicate);
			}
			cho.add(seq(seq));
		}
		return cho(name, cho.toArray(new Token[0]));
	}

	public static final Token FORMAT = seq("message/rfc2822", LE,
		rep(
			seq(
				text("field", FIELD),
				def("semicolon", 2, eq(con(": "))),
				text("value", ASCII),
				CRLF)),
		CRLF);
}
