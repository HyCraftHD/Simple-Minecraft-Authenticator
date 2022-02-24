package net.hycrafthd.simple_minecraft_authenticator;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;

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
	public AuthenticationFile initalAuthenticationFile() {
		try {
			final AuthenticationFile file = executor.submit(this::runInitalAuthenticationFile).get(timeout, TimeUnit.SECONDS);
			finishInitalAuthenticationFile();
			return file;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected abstract AuthenticationFile runInitalAuthenticationFile() throws Exception;
	
	protected abstract void finishInitalAuthenticationFile() throws Exception;
	
}
