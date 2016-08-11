# metal-tools
Tools for parsingdata/metal

First tool added is a HexViewer, written in HTML.
This hexviewer highlights all Metal Def tokens. For example, in the test a byte array is added with a string:;
length + data:
`stream(7, 'G', 'e', 'r', 't', 'j', 'a', 'n');`

Using `JsHexViewer.generate(result.environment.order);`, the following hexviewer is generated:
![screenshot_data.png](https://raw.githubusercontent.com/gertjanal/metal-tools/master/src/test/resources/jsHexViewer/screenshot_data.png)

After opening the hexviewer, you need to browse to the source data to view the bytes.

To make things even more complicated, here's a hexviewer of the `screenshot_data.png` above, using the PNG metal token:
(The page buttons on the right are calculated based on the known highlights on offsets)

![screenshot_png.png](https://raw.githubusercontent.com/gertjanal/metal-tools/master/src/test/resources/jsHexViewer/screenshot_png.png)

