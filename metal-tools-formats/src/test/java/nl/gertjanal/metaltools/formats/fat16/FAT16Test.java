package nl.gertjanal.metaltools.formats.fat16;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseState;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

class FAT16Test {

	@Test
	void testMP4() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/fat16/fat16.img");
		final Optional<ParseState> result = FAT16.FORMAT.parse(environment);
		assertTrue(result.isPresent());
	}
}
