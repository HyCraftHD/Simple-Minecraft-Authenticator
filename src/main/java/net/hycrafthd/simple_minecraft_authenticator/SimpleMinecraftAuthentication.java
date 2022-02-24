package net.hycrafthd.simple_minecraft_authenticator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.hycrafthd.simple_minecraft_authenticator.console.ConsoleAuthentication;
import net.hycrafthd.simple_minecraft_authenticator.creator.SimpleAuthenticationMethodCreator;
import net.hycrafthd.simple_minecraft_authenticator.web.WebAuthentication;

public final class SimpleMinecraftAuthentication {
	
	private static final ExecutorService executor;
	private static final Map<String, AuthenticationMethodBuilderCreator> methods = new HashMap<>();
	
	static {
		executor = Executors.newCachedThreadPool(new ThreadFactory() {
			
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			
			@Override
			public Thread newThread(Runnable runnable) {
				final Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				thread.setName("Simple Minecraft Authentication Worker " + threadNumber.getAndIncrement());
				return thread;
			}
		});
		addMethod(new SimpleAuthenticationMethodCreator("console", ConsoleAuthentication::new));
		addMethod(new SimpleAuthenticationMethodCreator("web", WebAuthentication::new));
	}
	
	private SimpleMinecraftAuthentication() {
	}
	
	public static final synchronized void addMethod(AuthenticationMethodBuilderCreator method) {
		methods.putIfAbsent(method.name(), method);
		method.setExecutor(executor);
	}
	
	public static final Optional<AuthenticationMethodBuilderCreator> getMethod(String name) {
		return Optional.ofNullable(methods.get(name));
	}
	
	public static final Set<String> getAvailableMethods() {
		return Collections.unmodifiableSet(methods.keySet());
	}
	
	public static final AuthenticationMethodBuilderCreator getDefaultMethod() {
		return getMethod("console").get();
	}
	
}
