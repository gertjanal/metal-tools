package nl.gertjanal.metaltools.formats.rar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseState;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

class RARTest {

	@Test
	void testRAR4() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/rar/example4.x.rar");
		final Optional<ParseState> result = RAR.FORMAT.parse(environment);
		assertTrue(result.isPresent());
	}
}
