package nl.gertjanal.metaltools.formats.exe.pe;

import static io.parsingdata.metal.Shorthand.add;
import static io.parsingdata.metal.Shorthand.and;
import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.eqNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.mul;
import static io.parsingdata.metal.Shorthand.not;
import static io.parsingdata.metal.Shorthand.offset;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.encoding.ByteOrder.LITTLE_ENDIAN;
import static io.parsingdata.metal.encoding.Sign.SIGNED;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.LONG;
import static nl.gertjanal.metaltools.formats.exe.pe.Sizes.SHORT;
import static nl.gertjanal.metaltools.formats.exe.pe.Language.LANGUAGE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ImmutableList;
import io.parsingdata.metal.data.ParseGraph;
import io.parsingdata.metal.data.ParseItem;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.value.ConstantFactory;
import io.parsingdata.metal.expression.value.OptionalValue;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.token.Token;

public class ResourceDirectoryTable extends Token {

	public static final Token RESOURCE_DIRECTORY_TABLE = seq("ResourceDirectoryTable",
		def("ResourceDirectoryRoot", 0),
		new ResourceDirectoryTable());

	private static final Encoding UTF_16 = new Encoding(SIGNED, StandardCharsets.UTF_16LE, LITTLE_ENDIAN);

	private static final Token RESOURCE_DIRECTORY_STRING = seq(
		def("length", 2),
		def("name", mul(last(ref("length")), con(2)), null, UTF_16));

	private static final Token DATA_POINTER = seq("data",
		def("DataRva", 4),
		def("Size", 4),
		def("CodePage", 4),
		def("Reserved", 4, eqNum(con(0))),
		sub(
			def("data", last(ref("Size"))),
			sub(
				last(ref("DataRva")),
				last(ref("VirtualAddressPointerToRawDataDelta")))));

	private ResourceDirectoryTable() {
		super("Table", null);
	}

	/**
	 * Dirty hack; struct is created every time on parseImpl to avoid name clashes.
	 *
	 * @return New token to read directory
	 */
	private Token struct() {
		final Token entry = seq(
			def("DataEntryOffset", LONG),
			pre(
				sub(DATA_POINTER, add(offset(last(ref("ResourceDirectoryRoot"))), last(ref("DataEntryOffset")))),
				not(hasHighBit(last(ref("DataEntryOffset"))))),
			pre(
				sub(this, add(offset(last(ref("ResourceDirectoryRoot"))), stripHighBit(last(ref("DataEntryOffset"))))),
				hasHighBit(last(ref("DataEntryOffset")))));

		final Token entryWithName = seq("name",
			def("NamePointer", LONG),
			sub(RESOURCE_DIRECTORY_STRING, add(offset(last(ref("ResourceDirectoryRoot"))), stripHighBit(last(ref("NamePointer"))))),
			entry);

		// Add UUID as the references have scoping issues
		final String numberOfNamedEntries = "NumberOfNamedEntries" + UUID.randomUUID();
		final String numberOfIdEntries = "NumberOfNamedEntries" + UUID.randomUUID();

		final Token entryWithID = seq("id",
			pre(
				cho(
					def("CURSOR", 4, eq(con(1))),
					def("BITMAP", 4, eq(con(2))),
					def("ICON", 4, eq(con(3))),
					def("MENU", 4, eq(con(4))),
					def("DIALOG", 4, eq(con(5))),
					def("STRING", 4, eq(con(6))),
					def("FONTDIR", 4, eq(con(7))),
					def("FONT", 4, eq(con(8))),
					def("ACCELERATORS", 4, eq(con(9))),
					def("RCDATA", 4, eq(con(10))),
					def("MESSAGETABLE", 4, eq(con(11))),
					def("GROUP_CURSOR", 4, eq(con(12))),
					def("GROUP_ICON", 4, eq(con(14))),
					def("VERSION", 4, eq(con(16))),
					def("DLGINCLUDE", 4, eq(con(17))),
					def("PLUGPLAY", 4, eq(con(19))),
					def("VXD", 4, eq(con(20))),
					def("ANICURSOR", 4, eq(con(21))),
					def("ANIICON", 4, eq(con(22))),
					def("HTML", 4, eq(con(23))),
					def("MANIFEST", 4, eq(con(24))),
					def("ID", 4)),
				eqNum(depth("dir"), con(1)) // depth 1 == root == types definitions
			), pre(
				def("ID", 4),
				eqNum(depth("dir"), con(2))),
			pre(
				LANGUAGE,
				eqNum(depth("dir"), con(3))),
			entry);

		return seq("dir",
			def("Characteristics", LONG),
			def("Date", LONG),
			def("MajorVersion", SHORT),
			def("MinorVersion", SHORT),
			def(numberOfNamedEntries, SHORT),
			def(numberOfIdEntries, SHORT),
			repn("EntryWithName", entryWithName, last(ref(numberOfNamedEntries))),
			repn("EntryWithId", entryWithID, last(ref(numberOfIdEntries))));
	}

	@Override
	protected ParseResult parseImpl(final String scope, final Environment env, final Encoding enc) throws IOException {
		return struct().parse(env, enc);
	}

	/**
	 * Strip high bit from 32 bits int value.
	 *
	 * @param value integer to strip
	 * @return <code>value - 0x80000000</code>
	 */
	private static ValueExpression stripHighBit(final ValueExpression value) {
		return sub(value, con(0x80000000));
	}

	/**
	 * Check if the 32 bits int value has high bit <code>0x80000000</code> set.
	 *
	 * @param value integer to check
	 * @return true if <code>value & 0x80000000 == 0x80000000</code>
	 */
	private static Expression hasHighBit(final ValueExpression value) {
		return eqNum(and(value, con(0x80000000)), con(0x80000000));
	}

	/**
	 * Count the occurrence of all tokens with name {@code scope} up to this {@link ValueExpression}.
	 *
	 * @param scope name of the token
	 * @return number of occurrences
	 */
	public static ValueExpression depth(final String scope) {
		return new ValueExpression() {

			@Override
			public ImmutableList<OptionalValue> eval(final Environment env, final Encoding enc) {
				ParseGraph last = env.order;
				int i = 0;
				for (ParseItem head = last.head; head != null && head.isGraph(); last = head.asGraph(), head = last.head) {
					if (head.getDefinition().name.equals(scope)) {
						i++;
					}
				}
				return ImmutableList.create(OptionalValue.of(ConstantFactory.createFromNumeric(i, enc)));
			}
		};
	}
}
