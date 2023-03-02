package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.simple_minecraft_authenticator.result.AuthenticationResult;
import net.hycrafthd.simple_minecraft_authenticator.util.AuthenticationTimeoutException;

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
			final AuthenticationResult result = executor.submit(this::runInitalAuthentication).get(timeout, TimeUnit.SECONDS);
			finishInitalAuthentication();
			if (result == null) {
				throw new TimeoutException("Result was null as the authentication method did not complete correctly");
			}
			return result;
		} catch (final InterruptedException | TimeoutException ex) {
			throw new AuthenticationTimeoutException("Authentication was not completed in " + timeout + " seconds", ex);
		} catch (final ExecutionException ex) {
			if (ex.getCause() instanceof AuthenticationException authenticationException) {
				throw authenticationException;
			} else {
				throw new AuthenticationException("An exception in the simple authenticator occured", ex);
			}
		} catch (final Exception ex) {
			throw new AuthenticationException("An unknown exception occured", ex);
		}
	}
	
	protected abstract AuthenticationResult runInitalAuthentication() throws Exception;
	
	protected abstract void finishInitalAuthentication() throws Exception;
	
}
