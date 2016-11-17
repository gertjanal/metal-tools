/**
 * Copyright 2016 Gertjan Al
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.gertjanal.metaltools.jstree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.parsingdata.metal.data.ParseGraph;
import io.parsingdata.metal.data.ParseItem;
import io.parsingdata.metal.token.Token;

/**
 * Generate a d3 tree of the {@link ParseGraph}.
 *
 * @author Netherlands Forensic Institute.
 */
public class JsTree {

	private static String generateJs(final ParseGraph graph) throws IOException {
		final Map<String, Object> root = new LinkedHashMap<>();
		step(graph, root, new HashMap<>());
		final ObjectMapper objectMapper = new ObjectMapper();
		if (root.containsKey("children")){
			return objectMapper.writeValueAsString(((List<?>) root.get("children")).get(0));
		}
		return "{}";
	}

	private static void step(final ParseItem item, final Map<String, Object> self, final Map<Token, LinkedList<Map<String, Object>>> collection) {
		final Token definition = item.getDefinition();
		self.put("name", definition.getClass().getSimpleName() + (definition.name.isEmpty() ? "" : ": " + definition.name));
		if (!item.isGraph() || item.asGraph().head == null) {
			return;
		}

		final LinkedList<Map<String, Object>> children;
		final Map<String, Object> head = new LinkedHashMap<>();
		final Map<String, Object> tail = new LinkedHashMap<>();

		if (collection.containsKey(definition)) {
			children = collection.get(definition);
		}
		else {
			children = new LinkedList<>();
			self.put("children", children);
			collection.put(definition, children);
		}

		if (item.asGraph().head.getDefinition() != item.getDefinition()) {
			children.push(head);
		}

		if (item.asGraph().tail.getDefinition() != item.getDefinition()) {
			children.push(tail);
		}

		step(item.asGraph().tail, tail, collection);
		step(item.asGraph().head, head, collection);
	}

	public static void generate(final ParseGraph graph) throws URISyntaxException, IOException {
		generate(graph, "jsTree");
	}

	public static void generate(final ParseGraph graph, final String fileName) throws URISyntaxException, IOException {
		generate(graph, null, fileName);
	}

	public static void generate(final ParseGraph graph, final InputStream data, final String fileName) throws URISyntaxException, IOException {
		final File root = new File(JsTree.class.getResource("/").toURI());
		generate(graph, data, fileName, root, true);
	}

	public static void generate(final ParseGraph graph, final InputStream data, final String fileName, final File dir, final boolean copyLibs) throws URISyntaxException, IOException {
		// Save content
		if (data != null) {
			try (FileOutputStream fos = new FileOutputStream(new File(dir, fileName))) {
				IOUtils.copy(data, fos);
			}
		}

		// Generate html
		final File file = new File(dir, fileName + ".htm");
		try (FileWriter out = new FileWriter(file);
			 InputStream in = JsTree.class.getResourceAsStream("/jsTree/template.htm");
			 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("/* generated */")) {
					if (line.trim().startsWith("loadData(")) {
						out.write("loadData(");
						out.write(generateJs(graph));
						out.write(");");
					}
				}
				else {
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
		final File libsSource = new File(JsTree.class.getResource("/jsTree/libs/").toURI());
		final File libsDestination = new File(dir, "libs");
		libsDestination.mkdir();
		final Path source = libsSource.toPath();
		final Path destination = libsDestination.toPath();

		Files.walkFileTree(libsSource.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
				final Path targetPath = destination.resolve(source.relativize(dir));
				if (!Files.exists(targetPath)) {
					Files.createDirectory(targetPath);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
