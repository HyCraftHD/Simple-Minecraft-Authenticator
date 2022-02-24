package net.hycrafthd.simple_minecraft_authenticator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

public interface AuthenticationMethodBuilderCreator {
	
	String name();
	
	AuthenticationMethod create(PrintStream out, InputStream in);
	
	void setExecutor(ExecutorService executor);
	
	default AuthenticationMethod create() {
		return create(System.out, System.in);
	}
	
}
