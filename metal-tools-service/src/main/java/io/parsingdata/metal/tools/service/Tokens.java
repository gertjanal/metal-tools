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

package io.parsingdata.metal.tools.service;

import static io.parsingdata.metal.Shorthand.cho;

import io.parsingdata.metal.format.JPEG;
import io.parsingdata.metal.format.PNG;
import io.parsingdata.metal.format.ZIP;
import io.parsingdata.metal.token.Token;
import nl.gertjanal.metaltools.formats.vhdx.VHDX;

/**
 * Collection of supported tokens.
 *
 * @author Netherlands Forensic Institute.
 */
public class Tokens {

    public static final Token SUPPORTED = cho(
            PNG.FORMAT,
            ZIP.FORMAT,
            JPEG.FORMAT,
            VHDX.FORMAT);
}
