package nl.gertjanal.metaltools.jstree;

import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.util.EncodingFactory.le;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.format.ZIP;
import io.parsingdata.metal.util.InMemoryByteStream;
import nl.gertjanal.metaltools.formats.vhdx.VHDX;

public class JsTreeTest {

    @Test
    public void testGenerateZip() throws Exception {
        final Environment env = environment("/data.zip");
        final ParseResult result = ZIP.FORMAT.parse(env, le());
        assertTrue(result.succeeded);

        final String tree = JsTree.generateJs(result.environment.order);
        System.out.println(tree);
    }

    @Test
    public void testGenerateVHDX() throws Exception {
        final Environment env = environment("/vhdx/NTFSdynamic.vhdx");
        final ParseResult result = VHDX.format(true).parse(env, le());
        assertTrue(result.succeeded);

        final String tree = JsTree.generateJs(result.environment.order);
        System.out.println(tree);
    }

    private Environment environment(final String name) throws IOException, URISyntaxException {
        final byte[] data = IOUtils.toByteArray(getClass().getResourceAsStream(name));
        return new Environment(new InMemoryByteStream(data));
    }
}
