package nl.gertjanal.metaltools.formats.exe.pe;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.Util.checkNotNull;
import static nl.gertjanal.metaltools.formats.exe.pe.ResourceDirectoryTable.RESOURCE_DIRECTORY_TABLE;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.LONG;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.SHORT;

import java.io.IOException;

import io.parsingdata.metal.data.ConstantSlice;
import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.data.ParseValue;
import io.parsingdata.metal.data.Slice;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.True;
import io.parsingdata.metal.expression.value.OptionalValue;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.token.Token;

/**
 * Code sections.
 *
 * @author Gertjan Al.
 */
public class CodeSection {

	private static Token let(final String name, final ValueExpression value) {
		return new Let(name, value, null, null);
	}

	private static final Token CODE_SECTION_HEADER = seq(
		def("Name", 8),
		def("VirtualSize", LONG),
		def("VirtualAddress", LONG),
		def("SizeOfRawData", LONG),
		def("PointerToRawData", LONG),
		def("PointerToRelocations", LONG),
		def("PointerToLinenumbers", LONG),
		def("NumberOfRelocations", SHORT),
		def("NumberOfLinenumbers", SHORT),
		def("Characteristics", LONG),
		pre(
			seq(
				let("VirtualAddressPointerToRawDataDelta", sub(last(ref("VirtualAddress")), last(ref("PointerToRawData")))),
				sub(RESOURCE_DIRECTORY_TABLE, last(ref("PointerToRawData")))),
			eq(last(ref("Name")), con(".rsrc\u0000\u0000\u0000"))));

	static final Token CODE_SECTIONS = repn("Code Sections", CODE_SECTION_HEADER, last(ref("NumberOfSections")));

	static class Let extends Token {
		private final String _name;
		private final ValueExpression _value;
		private final Expression _pred;

		public Let(final String name, final ValueExpression value, final Expression pred, final Encoding enc) {
			super(name, enc);
			_name = checkNotNull(name, "name");
			_value = checkNotNull(value, "value");
			_pred = pred == null ? new True() : pred;
		}

		@Override
		protected ParseResult parseImpl(final String scope, final Environment env, final Encoding enc) throws IOException {
			final ImmutableList<OptionalValue> values = _value.eval(env, enc);
			if (values.isEmpty()) {
				return new ParseResult(false, env);
			}

			final Value val = values.head.get();
			final Slice slice = new ConstantSlice(val.getValue());
			final Environment newEnv = new Environment(env.order.add(new ParseValue(_name, this, slice, val.encoding)), env.source, env.offset, env.callbacks);
			return _pred.eval(newEnv, enc) ? new ParseResult(true, newEnv) : new ParseResult(false, env);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(\"" + _name + "\"," + _value + "," + _pred + ",)";
		}
	}
}
