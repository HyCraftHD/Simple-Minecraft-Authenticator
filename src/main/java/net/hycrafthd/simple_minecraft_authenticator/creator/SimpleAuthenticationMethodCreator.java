package net.hycrafthd.simple_minecraft_authenticator.creator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

import net.hycrafthd.simple_minecraft_authenticator.method.AuthenticationMethod;

public class SimpleAuthenticationMethodCreator implements AuthenticationMethodCreator {
	
	private final String name;
	private final FullCreator creator;
	private final FullCreator headlessCreator;
	private ExecutorService executor;
	
	public SimpleAuthenticationMethodCreator(String name, ReducedCreator creator, FullCreator headlessCreator) {
		this(name, (out, in, executor) -> creator.create(out, executor), headlessCreator);
	}
	
	public SimpleAuthenticationMethodCreator(String name, FullCreator creator, FullCreator headlessCreator) {
		this.name = name;
		this.creator = creator;
		this.headlessCreator = headlessCreator;
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
	
	@Override
	public AuthenticationMethod createHeadless(PrintStream out, InputStream in) {
		return headlessCreator.create(out, in, executor);
	}
	
	public static interface FullCreator {
		
		AuthenticationMethod create(PrintStream out, InputStream in, ExecutorService executor);
	}
	
	public static interface ReducedCreator {
		
		AuthenticationMethod create(PrintStream out, ExecutorService executor);
	}
	
}
