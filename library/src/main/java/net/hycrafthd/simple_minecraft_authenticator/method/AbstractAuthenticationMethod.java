package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.simple_minecraft_authenticator.result.AuthenticationResult;
import net.hycrafthd.simple_minecraft_authenticator.util.AuthenticationFutureUtil;

public abstract class AbstractAuthenticationMethod implements AuthenticationMethod {
	
	protected final PrintStream out;
	protected final ExecutorService executor;
	protected int timeout;
	protected Consumer<URL> loginUrlCallback;
	
	public AbstractAuthenticationMethod(PrintStream out, ExecutorService executor) {
		this.out = out;
		this.executor = executor;
		timeout = 300;
		loginUrlCallback = url -> {
		};
	}
	
	@Override
	public void setTimeout(int seconds) {
		timeout = seconds;
	}
	
	@Override
	public void registerLoginUrlCallback(Consumer<URL> loginUrlCallback) {
		this.loginUrlCallback = loginUrlCallback;
	}
	
	@Override
	public AuthenticationResult initalAuthentication() throws AuthenticationException {
		try {
			return AuthenticationFutureUtil.runAuthentication(executor, this::runInitalAuthentication, timeout, false);
		} finally {
			// Ignore exception of finish authentication as its only used for cleanup (closing streams, stopping web server)
			try {
				finishInitalAuthentication();
			} catch (final Exception ex) {
			}
		}
	}
	
	protected abstract AuthenticationResult runInitalAuthentication() throws Exception;
	
	protected abstract void finishInitalAuthentication() throws Exception;
	
}
