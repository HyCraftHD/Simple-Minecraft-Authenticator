package net.hycrafthd.simple_minecraft_authenticator.creator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

import net.hycrafthd.simple_minecraft_authenticator.method.AuthenticationMethod;

public interface AuthenticationMethodCreator {
	
	String name();
	
	void setExecutor(ExecutorService executor);
	
	default AuthenticationMethod create(boolean headless, PrintStream out, InputStream in) {
		return headless ? createHeadless(out, in) : create(out, in);
	}
	
	default AuthenticationMethod create(boolean headless) {
		return headless ? createHeadless() : create();
	}
	
	AuthenticationMethod create(PrintStream out, InputStream in);
	
	default AuthenticationMethod create() {
		return create(System.out, System.in);
	}
	
	AuthenticationMethod createHeadless(PrintStream out, InputStream in);
	
	default AuthenticationMethod createHeadless() {
		return createHeadless(System.out, System.in);
	}
	
}
