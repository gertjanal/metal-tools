/**
 * Copyright 2016 Gertjan Al
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.gertjanal.metaltools.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import nl.gertjanal.metaltools.jshexviewer.JsHexViewer;

@Configuration
@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter {

	private ObjectMapper _mapper;

	public static void main(final String[] args) throws Exception {
		final File root = new File(JsHexViewer.class.getResource("/").toURI());
		JsHexViewer.copyLibs(new File(root, "static"));
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		if (_mapper == null) {
			_mapper = new ObjectMapper();
			final SimpleModule module = new SimpleModule("HanskenModule", new Version(1, 0, 0, null, null, null));
			module.addDeserializer(BigInteger.class, new JsonDeserializer<BigInteger>() {

				@Override
				public BigInteger deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
					return new BigInteger(jp.readValueAs(String.class), 16);
				}
			});
			module.addSerializer(BigInteger.class, new JsonSerializer<BigInteger>() {

				@Override
				public void serialize(final BigInteger value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
					jgen.writeString(value.toString(16));
				}
			});
			_mapper.registerModule(module);
		}
		return _mapper;
	}

	private MappingJackson2HttpMessageConverter getConverter() {
		final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(_mapper);
		return converter;
	}

	@Override
	public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
		converters.add(getConverter());
	}
}