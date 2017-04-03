package nl.gertjanal.metaltools.formats.eml;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

public class EMLTest {

	@Test
	public void testEML() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/eml/email.eml");
		final ParseResult result = EML.FORMAT.parse(environment, null);
		assertTrue(result.succeeded);
	}
}
