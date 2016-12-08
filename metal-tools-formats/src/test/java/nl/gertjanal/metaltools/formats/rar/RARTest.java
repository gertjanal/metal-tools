package nl.gertjanal.metaltools.formats.rar;

import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.util.EncodingFactory.le;

import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

public class RARTest {

	@Test
	public void testRAR4() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/rar/example4.x.rar");
		final ParseResult result = RAR.FORMAT.parse(environment, le());
		assertTrue(result.succeeded);
	}

	@Test
	public void testRAR5() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/rar/example5.40.rar");
		final ParseResult result = RAR.FORMAT.parse(environment, le());
		assertTrue(result.succeeded);
	}
}
