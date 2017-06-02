package nl.gertjanal.metaltools.formats.exe.pe;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

public class ExePeTest {

	@Test
	public void testExePeX86() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/exe/pe/x86/notepad.exe");
		final ParseResult result = EXE.FORMAT.parse(environment, null);
		assertTrue(result.succeeded);
	}

	@Test
	public void testExePeX64() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/exe/pe/x64/notepad.exe");
		final ParseResult result = EXE.FORMAT.parse(environment, null);
		assertTrue(result.succeeded);
	}
}
