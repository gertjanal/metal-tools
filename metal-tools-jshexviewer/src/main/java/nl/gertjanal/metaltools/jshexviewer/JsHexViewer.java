/**
 * Copyright 2016 Gertjan Al
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.gertjanal.metaltools.jshexviewer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.write;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import io.parsingdata.metal.data.ByteStreamSource;
import io.parsingdata.metal.data.ParseGraph;
import io.parsingdata.metal.data.ParseItem;
import io.parsingdata.metal.data.ParseValue;
import io.parsingdata.metal.data.Slice;
import io.parsingdata.metal.expression.value.CoreValue;
import io.parsingdata.metal.token.Def;
import io.parsingdata.metal.token.Until;

/**
 * Generate a HTML page to view the Metal ParseGraph in a hex viewer.
 *
 * @author Gertjan Al.
 */
public class JsHexViewer {

	private static final BigInteger COLUMN_COUNT = BigInteger.valueOf(1 << 5);
	private static final Function<ParseValue, Slice> SLICE;
	static {
	    try {
            final Field slice = CoreValue.class.getDeclaredField("slice");
            slice.setAccessible(true);

	        SLICE = (parseValue) -> {
                try {
                    return (Slice)slice.get(parseValue);
                }
                catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            };
	    } catch(final Exception e) {
	        throw new IllegalStateException(e);
	    }
	}

	public static void generate(final ParseGraph graph) throws URISyntaxException, IOException {
		generate(graph, "jsHexViewer");
	}

	public static String generateJs(final ParseGraph graph) throws IOException {
		final Map<Long, LinkedList<Definition>> map = new TreeMap<>();
		step(graph, map);

		return "/* generated by JsHexViewer */" +
			"var columnCountUpdate = " + COLUMN_COUNT.longValue() + ";" +
			"var locationsUpdate = " + map.keySet().toString() + ";" +
			"var dataUpdate = " + writeData(map) + ";";
	}

	public static void generate(final ParseGraph graph, final String fileName) throws URISyntaxException, IOException {
		generate(graph, null, fileName);
	}

	public static void generate(final ParseGraph graph, final InputStream data, final String fileName) throws URISyntaxException, IOException {
		final File root = new File(JsHexViewer.class.getResource("/").toURI());
		generate(graph, data, fileName, root, true);
	}

	public static void generate(final ParseGraph graph, final InputStream data, final String fileName, final File dir, final boolean copyLibs) throws URISyntaxException, IOException {
		// Generate highlights
		try (FileOutputStream fos = new FileOutputStream(new File(dir, fileName + ".js"))) {
			write(generateJs(graph), fos, UTF_8);
		}

		// Save content
		if (data != null) {
			try (FileOutputStream fos = new FileOutputStream(new File(dir, fileName))) {
				copy(data, fos);
			}
		}

		// Generate html
		final File file = new File(dir, fileName + ".htm");
		try (FileWriter out = new FileWriter(file);
			InputStream in = JsHexViewer.class.getResourceAsStream("/jsHexViewer/template.htm");
			BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("<!-- generated -->")) {
					if (line.trim().startsWith("var dataUrl =")) {
						out.write("var dataUrl = '");
						out.write(fileName + ".js");
						out.write("';");
					}
				} else {
					out.write(line);
				}
				out.write('\n');
			}
		}

		// Copy html libs
		if (copyLibs) {
			copyLibs(dir);
		}
	}

	public static void copyLibs(final File dir) throws URISyntaxException, IOException {
		final File libsSource = new File(JsHexViewer.class.getResource("/jsHexViewer/libs/").toURI());
		final File libsDestination = new File(dir, "libs");
		libsDestination.mkdir();
		final Path source = libsSource.toPath();
		final Path destination = libsDestination.toPath();

		walkFileTree(libsSource.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
				throws IOException {
				final Path targetPath = destination.resolve(source.relativize(dir));
				if (!exists(targetPath)) {
					createDirectory(targetPath);
				}
				return CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				copy(file, destination.resolve(source.relativize(file)), REPLACE_EXISTING);
				return CONTINUE;
			}
		});
	}

	private static String writeData(final Map<Long, LinkedList<Definition>> map) throws IOException {
	    final StringBuilder builder = new StringBuilder("[");
		for (final Long row : map.keySet()) {
			if (builder.length() > 1) {
				builder.append(",");
			}
			builder.append(map.get(row).toString());
		}
		return builder.append("]").toString();
	}

	private static void step(final ParseItem item, final Map<Long, LinkedList<Definition>> map) {
		if (!item.isGraph()) {
            if (item.getDefinition() instanceof Def || item.getDefinition() instanceof Until) {
				final ParseValue value = item.asValue();
				if(SLICE.apply(value).source instanceof ByteStreamSource) {
					getList(map, new Definition(value));
				}
			}
			return;
		}
		if (item.asGraph().head == null) {
			return;
		}
		step(item.asGraph().head, map);
		step(item.asGraph().tail, map);
	}

	private static void getList(final Map<Long, LinkedList<Definition>> map, final Definition definition) {
		final long row = definition._offset.divide(COLUMN_COUNT).longValue(); // TODO convert row to BigInteger?
		LinkedList<Definition> list = map.get(row);
		if (list == null) {
			list = new LinkedList<>();
			map.put(row, list);
		}
		list.addFirst(definition);
	}

	private static class Definition {
		private final String _name;
		private final BigInteger _offset;
		private final BigInteger _length;

		public Definition(final ParseValue value) {
			_name = value.name;
			_offset = SLICE.apply(value).offset;
			_length = SLICE.apply(value).length;
		}

		@Override
		public String toString() {
			return "[" + _offset + ", " + _length + ", '" + _name + "']";
		}
	}
}
