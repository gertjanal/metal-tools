# metal-tools
Tools for [parsingdata/metal](https://github.com/parsingdata/metal)

Live demo available at: [gertjanal.github.io/metal-tools](https://gertjanal.github.io/metal-tools)

## HexViewer
First tool added is a HexViewer, written in HTML.
This hexviewer highlights all Metal Def tokens. For example, in the test a byte array is added with a string;
`length + data`: `stream(7, 'G', 'e', 'r', 't', 'j', 'a', 'n');`

Using `JsHexViewer.generate(result.environment.order);`, the following hexviewer is generated:
![screenshot_data.png](https://raw.githubusercontent.com/gertjanal/metal-tools/master/metal-tools-jshexviewer/src/test/resources/screenshot_data.png)

Note that after opening the hexviewer webpage, you need to browse to the source data to view the bytes.

Below is a screenshot of the hexviewer with the `screenshot_data.png` from the previous example as input. As you can see this hexviewer is newer than the example above. It has more features, like bookmarking definitions.

![screenshot_png.png](https://raw.githubusercontent.com/gertjanal/metal-tools/master/metal-tools-jshexviewer/src/test/resources/screenshot_png.png)

### Usage
Simply create a test class to call the parse of the graph and generate a hexviewer page.

```java
@Test
public void generate() throws Exception {
    final Environment env = EnvironmentFactory.stream(getClass().getResource("/data.zip").toURI());
    final Encoding enc = EncodingFactory.le();
    final ParseResult result = ZIP.FORMAT.parse(env, enc);
    if (!result.succeeded) {
        fail("Parse failed");
    }
    JsHexViewer.generate(result.environment.order, "zip");
}
```