package risk.engine.guishared;

import java.io.*;

public class SimplePrintStream extends OutputStream {

	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
	private StringWriter sw = null;

	private SimplePrintStream(StringWriter a) {

		sw = a;
	}
 
	public void write(int b) {

		outputStream.write(b);
	}
 
	public void flush() throws IOException {

		super.flush();

		sw.write( outputStream.toString() );

		outputStream.reset();

	}

	public static PrintStream getSimplePrintStream(StringWriter a) {

		return new PrintStream(new SimplePrintStream(a), true);

	}

}
