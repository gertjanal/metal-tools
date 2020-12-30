package nl.gertjanal.metaltools.formats.mp4;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseState;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

class MP4Test {

	@Test
	void testMP4() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/mp4/big_buck_bunny_720p_1mb.mp4");
		final Optional<ParseState> result = MP4.FORMAT.parse(environment);
		assertTrue(result.isPresent());
	}
}
