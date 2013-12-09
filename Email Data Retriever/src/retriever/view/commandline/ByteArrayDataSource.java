package retriever.view.commandline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

public class ByteArrayDataSource implements DataSource {
	private byte[] data; // data
	private final String type; // content-type

	/* Create a DataSource from an input stream */
	public ByteArrayDataSource(InputStream is, String type) {
		this.type = type;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int ch;

			while ((ch = is.read()) != -1)
				os.write(ch);
			data = os.toByteArray();

		} catch (IOException ioex) {
		}
	}

	/* Create a DataSource from a byte array */
	public ByteArrayDataSource(byte[] data, String type) {
		this.data = data;
		this.type = type;
	}

	/* Create a DataSource from a String */
	public ByteArrayDataSource(String data, String type) {
		try {
// Assumption that the string contains only ASCII
// characters! Otherwise just pass a charset into this
// constructor and use it in getBytes()
			this.data = data.getBytes("iso-8859-1");
		} catch (UnsupportedEncodingException uex) {
		}
		this.type = type;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (data == null)
			throw new IOException("no data");
		return new ByteArrayInputStream(data);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("cannot do this");
	}

	@Override
	public String getContentType() {
		return type;
	}

	@Override
	public String getName() {
		return "dummy";
	}
}