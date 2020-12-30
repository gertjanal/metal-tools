package nl.gertjanal.metaltools.formats.mp4;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseState;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

public class MP4Test {

	@Test
	public void testMP4() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/mp4/big_buck_bunny_720p_1mb.mp4");
		final Optional<ParseState> result = MP4.FORMAT.parse(environment);
		assertTrue(result.isPresent());
	}
}
