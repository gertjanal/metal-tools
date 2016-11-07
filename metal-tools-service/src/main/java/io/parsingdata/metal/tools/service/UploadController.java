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

package io.parsingdata.metal.tools.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.parsingdata.metal.data.ByteStream;
import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.tools.service.json.CreateFile;
import io.parsingdata.metal.tools.service.json.Data;
import io.parsingdata.metal.tools.service.json.Slice;
import nl.gertjanal.metaltools.jshexviewer.JsHexViewer;

@RestController
public class UploadController {
	private final Map<Slice, byte[]> _queue = new HashMap<>();
	private final Map<UUID, CreateFile> _files = new HashMap<>();

	@Autowired
	private SimpMessagingTemplate _messagingTemplate;

	/**
	 * Initiate a new data upload.
	 *
	 * @param request Request with file name and size
	 * @return a upload session uuid
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public UUID start(@RequestBody final CreateFile request) {
		final UUID id = UUID.randomUUID();
		_files.put(id, request);
		System.out.println(id + " created for " + request.getName());
		return id;
	}

	/**
	 * Start the parsing of a upload session, let this controller call the client for bytes.
	 *
	 * @param id the upload uuid
	 */
	@MessageMapping("/data/{id}/start")
	public void ready(@DestinationVariable final UUID id) {
		final Thread t = new Thread(new Runnable() {
			private long _read = 0;

			@Override
			public void run() {
				final ByteStream stream = new ByteStream() {

					@Override
					public int read(final long offset, final byte[] buffer) throws IOException {
						final Slice part = new Slice(BigInteger.valueOf(offset), buffer.length, id);
						byte[] data = _queue.remove(part);
						if (data == null) {
							_messagingTemplate.convertAndSend("/topic/data/" + id + "/request", part);
						}

						do {
							try {
								Thread.sleep(10);
							} catch (final InterruptedException e) {
								e.printStackTrace();
								return 0;
							}
						} while ((data = _queue.remove(part)) == null);
						System.arraycopy(data, 0, buffer, 0, data.length);
						_read += data.length;
						return data.length;
					}
				};

				final Environment env = new Environment(stream);

				try {
					final ParseResult result = Tokens.SUPPORTED.parse(env, new Encoding());
					System.out.println("Read " + _read + " bytes of " + _files.get(id).getSize());
					if (result.succeeded) {
						System.out.println("generating for " + id);
						final File root = new File(JsHexViewer.class.getResource("/").toURI());
						JsHexViewer.generate(result.environment.order, id.toString(), new File(root, "static"), false);
					}
					_messagingTemplate.convertAndSend("/topic/data/" + id + "/done", result.succeeded);
				} catch (final Throwable t) {
					t.printStackTrace();
					_messagingTemplate.convertAndSend("/topic/data/" + id + "/done", false);
				}
			}
		});
		t.start();
	}

	/**
	 * Receive buffer response from client.
	 *
	 * @param id The id of the processing upload
	 * @param data The requested buffer
	 */
	@MessageMapping("/data/{id}/response")
	public void greeting(@DestinationVariable final UUID id, final Data data) {
		_queue.put(new Slice(data, id), data.getBuffer());
	}
}