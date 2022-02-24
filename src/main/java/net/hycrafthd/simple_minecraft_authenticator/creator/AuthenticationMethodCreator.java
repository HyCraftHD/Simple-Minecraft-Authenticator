package net.hycrafthd.simple_minecraft_authenticator.creator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

import net.hycrafthd.simple_minecraft_authenticator.method.AuthenticationMethod;

public interface AuthenticationMethodCreator {
	
	String name();
	
	AuthenticationMethod create(PrintStream out, InputStream in);
	
	void setExecutor(ExecutorService executor);
	
	default AuthenticationMethod create() {
		return create(System.out, System.in);
	}
	
}
