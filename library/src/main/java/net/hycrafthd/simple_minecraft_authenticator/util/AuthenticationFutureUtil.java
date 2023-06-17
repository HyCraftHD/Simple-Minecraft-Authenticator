package net.hycrafthd.simple_minecraft_authenticator.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;

public class AuthenticationFutureUtil {
	
	public static <T> T runAuthentication(ExecutorService executor, Callable<T> callable, int timeout, boolean allowNullResult) throws AuthenticationException {
		final Future<T> future = executor.submit(callable);
		
		try {
			final T result = future.get(timeout, TimeUnit.SECONDS);
			if (!allowNullResult && result == null) {
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
		}
	}
	
}
