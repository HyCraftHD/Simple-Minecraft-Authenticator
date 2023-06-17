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

import net.hycrafthd.simple_minecraft_authenticator.creator.AuthenticationMethodCreator;
import net.hycrafthd.simple_minecraft_authenticator.creator.SimpleAuthenticationMethodCreator;
import net.hycrafthd.simple_minecraft_authenticator.method.ConsoleAuthentication;
import net.hycrafthd.simple_minecraft_authenticator.method.HeadlessWebAuthentication;
import net.hycrafthd.simple_minecraft_authenticator.method.WebAuthentication;

public final class SimpleMinecraftAuthentication {
	
	public static final String METHOD_EXTRA_PROPERTY = "method";
	
	private static final ExecutorService executor;
	private static final Map<String, AuthenticationMethodCreator> methods = new HashMap<>();
	
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
		addMethod(new SimpleAuthenticationMethodCreator("console", ConsoleAuthentication::new, ConsoleAuthentication::new));
		addMethod(new SimpleAuthenticationMethodCreator("web", WebAuthentication::new, HeadlessWebAuthentication::new));
	}
	
	private SimpleMinecraftAuthentication() {
	}
	
	public static final synchronized void addMethod(AuthenticationMethodCreator method) {
		methods.putIfAbsent(method.name(), method);
		method.setExecutor(executor);
	}
	
	public static final Optional<AuthenticationMethodCreator> getMethod(String name) {
		return Optional.ofNullable(methods.get(name));
	}
	
	public static final AuthenticationMethodCreator getMethodOrThrow(String name) throws IllegalArgumentException {
		return getMethod(name).orElseThrow(() -> new IllegalArgumentException("Authentication type " + name + " does not exist"));
	}
	
	public static final Set<String> getAvailableMethods() {
		return Collections.unmodifiableSet(methods.keySet());
	}
	
	public static final AuthenticationMethodCreator getDefaultMethod() {
		return getMethod("console").get();
	}
	
	public static ExecutorService getExecutor() {
		return executor;
	}
	
}
