package nl.gertjanal.metaltools.formats.mp4;

import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.util.EncodingFactory.le;

import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

public class MP4Test {

	@Test
	public void testMP4() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/mp4/big_buck_bunny_720p_1mb.mp4");
		final ParseResult result = MP4.FORMAT.parse(environment, le());
		assertTrue(result.succeeded);
	}
}
