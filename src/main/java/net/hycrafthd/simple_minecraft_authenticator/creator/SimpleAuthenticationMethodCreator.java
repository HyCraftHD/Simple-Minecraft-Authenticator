package net.hycrafthd.simple_minecraft_authenticator.creator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

import net.hycrafthd.simple_minecraft_authenticator.AuthenticationMethod;
import net.hycrafthd.simple_minecraft_authenticator.AuthenticationMethodBuilderCreator;

public class SimpleAuthenticationMethodCreator implements AuthenticationMethodBuilderCreator {
	
	private final String name;
	private final FullCreator creator;
	private ExecutorService executor;
	
	public SimpleAuthenticationMethodCreator(String name, ReducedCreator creator) {
		this(name, (out, in, executor) -> creator.create(out, executor));
	}
	
	public SimpleAuthenticationMethodCreator(String name, FullCreator creator) {
		this.name = name;
		this.creator = creator;
	}
	
	@Override
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public AuthenticationMethod create(PrintStream out, InputStream in) {
		return creator.create(out, in, executor);
	}
	
	public static interface FullCreator {
		
		AuthenticationMethod create(PrintStream out, InputStream in, ExecutorService executor);
	}
	
	public static interface ReducedCreator {
		
		AuthenticationMethod create(PrintStream out, ExecutorService executor);
	}
	
}
