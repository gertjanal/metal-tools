package io.parsingdata.metal.tools.service;

import static io.parsingdata.metal.Shorthand.cho;

import io.parsingdata.metal.format.JPEG;
import io.parsingdata.metal.format.PNG;
import io.parsingdata.metal.format.ZIP;
import io.parsingdata.metal.formats.vhdx.VHDX;
import io.parsingdata.metal.token.Token;

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
