package net.hycrafthd.simple_minecraft_authenticator.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UnclosableInputStream extends FilterInputStream {
	
	public UnclosableInputStream(InputStream inputStream) {
		super(inputStream);
	}
	
	@Override
	public void close() throws IOException {
	}
	
}
