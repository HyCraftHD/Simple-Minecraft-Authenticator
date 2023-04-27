package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
		final Future<AuthenticationResult> future = executor.submit(this::runInitalAuthentication);
		
		try {
			final AuthenticationResult result = future.get(timeout, TimeUnit.SECONDS);
			if (result == null) {
				throw new TimeoutException("Result was null as the authentication method did not complete correctly");
			}
			return result;
		} catch (final InterruptedException ex) {
			future.cancel(true);
			throw new AuthenticationTimeoutException("Authentication was canceled from outside", ex);
		} catch (final TimeoutException ex) {
			throw new AuthenticationTimeoutException("Authentication was not completed in " + timeout + " seconds", ex);
		} catch (final ExecutionException ex) {
			if (ex.getCause() instanceof AuthenticationException authenticationException) {
				throw authenticationException;
			} else {
				throw new AuthenticationException("An exception in the simple authenticator occured", ex);
			}
		} catch (final Exception ex) {
			throw new AuthenticationException("An unknown exception occured", ex);
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
