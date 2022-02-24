package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.simple_minecraft_authenticator.util.AuthenticationTimeoutException;

public abstract class AbstractAuthenticationMethod implements AuthenticationMethod {
	
	protected final PrintStream out;
	protected final ExecutorService executor;
	protected int timeout;
	
	public AbstractAuthenticationMethod(PrintStream out, ExecutorService executor) {
		this.out = out;
		this.executor = executor;
		timeout = 300;
	}
	
	@Override
	public void setTimeout(int seconds) {
		timeout = seconds;
	}
	
	@Override
	public AuthenticationFile initalAuthenticationFile() throws AuthenticationException {
		try {
			final AuthenticationFile file = executor.submit(this::runInitalAuthenticationFile).get(timeout, TimeUnit.SECONDS);
			finishInitalAuthenticationFile();
			return file;
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
	
	protected abstract AuthenticationFile runInitalAuthenticationFile() throws Exception;
	
	protected abstract void finishInitalAuthenticationFile() throws Exception;
	
}
