package io.parsingdata.metal.tools.service;

import static io.parsingdata.metal.Shorthand.cho;

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
import io.parsingdata.metal.format.PNG;
import io.parsingdata.metal.format.ZIP;
import io.parsingdata.metal.formats.vhdx.VHDX;
import io.parsingdata.metal.token.Token;
import io.parsingdata.metal.tools.jshexviewer.JsHexViewer;

@RestController
public class UploadController {
    private static final Token FILES = cho(PNG.FORMAT, ZIP.FORMAT, VHDX.VHDX);
    private final Map<Part, byte[]> _queue = new HashMap<>();
    private final Map<UUID, CreateFile> _files = new HashMap<>();

    @Autowired
    private SimpMessagingTemplate _messagingTemplate;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public UUID start(@RequestBody final CreateFile request) {
        final UUID id = UUID.randomUUID();
        _files.put(id, request);
        System.out.println(id + " created for " + request.getName());
        return id;
    }

    @MessageMapping("/data/{id}/start")
    public void ready(@DestinationVariable final UUID id) {
        final Thread t = new Thread(new Runnable() {
            private long _read = 0;

            @Override
            public void run() {
                final ByteStream stream = new ByteStream() {

                    @Override
                    public int read(final long offset, final byte[] buffer) throws IOException {
                        System.out.println("Requesting buffer " + offset + ", " + buffer.length);
                        final Part part = new Part(BigInteger.valueOf(offset), buffer.length, id);
                        byte[] data = _queue.remove(part);
                        if (data == null) {
                            _messagingTemplate.convertAndSend("/topic/data/" + id + "/request", part);
                        }

                        do {
                            try {
                                Thread.sleep(10);
                            }
                            catch (final InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while ((data = _queue.remove(part)) == null);
                        System.out.println("Received buffer " + offset + ", " + data.length);
                        System.arraycopy(data, 0, buffer, 0, data.length);
                        _read += data.length;
                        return data.length;
                    }
                };

                final Environment env = new Environment(stream);

                try {
                    final ParseResult result = FILES.parse(env, null);
                    System.out.println("Read " + _read + " bytes of " + _files.get(id).getSize());
                    if (result.succeeded) {
                        System.out.println("generating for " + id);
                        final File root = new File(JsHexViewer.class.getResource("/").toURI());
                        JsHexViewer.generate(result.environment.order, id.toString(), new File(root, "static"), false);
                    }
                    _messagingTemplate.convertAndSend("/topic/data/" + id + "/done", result.succeeded);
                }
                catch (final Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        t.start();
    }

    @MessageMapping("/data/{id}/response")
    public void greeting(@DestinationVariable final UUID id, final Data data) throws Exception {
        _queue.put(new Part(data, id), data.getBuffer());
    }
}