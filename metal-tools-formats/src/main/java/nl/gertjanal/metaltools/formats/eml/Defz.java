package nl.gertjanal.metaltools.formats.eml;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.data.ParseResult.failure;
import static io.parsingdata.metal.data.ParseResult.success;

import java.io.IOException;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.data.ParseValue;
import io.parsingdata.metal.data.Slice;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.True;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.token.Def;
import io.parsingdata.metal.token.Nod;
import io.parsingdata.metal.token.Token;

/**
 * A {@link Token} that specifies a value to parse in the input.
 * <p>
 * A Def consists of a <code>size</code> (a {@link ValueExpression}, in bytes)
 * and a <code>predicate</code> (an {@link Expression}). The
 * <code>predicate</code> may be <code>null</code>.
 * <p>
 * Parsing will succeed if <code>size</code> evaluates to a single value, if
 * that many bytes are available in the input and if <code>predicate</code>
 * (if present) evaluates to <code>true</code>.
 *
 * @see Expression
 * @see Nod
 * @see ValueExpression
 */
public class Defz extends Def {

	public final Expression predicate;

	public Defz(final String name, final Expression predicate, final Encoding encoding) {
		super(name, con(1), predicate, encoding);
		this.predicate = predicate == null ? new True() : predicate;
	}

	@Override
	protected ParseResult parseImpl(final String scope, final Environment environment, final Encoding encoding) throws IOException {
		int dataSize = 0;
		Environment newEnvironment = environment;
		do {
			newEnvironment = newEnvironment.add(new ParseValue(scope, this, newEnvironment.slice(1), encoding)).seek(newEnvironment.offset + 1);
			dataSize++;
		} while (predicate.eval(newEnvironment, encoding));

		--dataSize;
		final Slice slice = environment.slice(dataSize);
		if (slice.size != dataSize) {
			return failure(environment);
		}
		return success(environment.add(new ParseValue(scope, this, slice, encoding)).seek(environment.offset + dataSize));
	}
}