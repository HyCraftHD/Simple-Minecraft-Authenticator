package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.simple_minecraft_authenticator.result.AuthenticationResult;
import net.hycrafthd.simple_minecraft_authenticator.result.FileAuthenticationResult;
import net.hycrafthd.simple_minecraft_authenticator.util.UnclosableInputStream;

public class ConsoleAuthentication extends AbstractAuthenticationMethod {
	
	private final BufferedReader reader;
	
	public ConsoleAuthentication(PrintStream out, InputStream in, ExecutorService executor) {
		super(out, executor);
		reader = new BufferedReader(new InputStreamReader(new UnclosableInputStream(in), StandardCharsets.UTF_8));
	}
	
	@Override
	public AuthenticationResult existingAuthentication(AuthenticationFile file) {
		return new FileAuthenticationResult(file);
	}
	
	@Override
	protected AuthenticationResult runInitalAuthentication() throws IOException, AuthenticationException {
		final URL loginUrl = Authenticator.microsoftLogin();
		loginUrlCallback.accept(loginUrl);
		
		out.println("Open the following link and log into your microsoft account. Paste the code parameter of the returned url.");
		out.println("Code should look like this: M.R3_BL2.00000000-0000-0000-0000-000000000000");
		out.println(loginUrl);
		
		long startTime = System.currentTimeMillis();
		while ((System.currentTimeMillis() - startTime) < timeout * 1000 && !reader.ready()) {
			synchronized (this) {
				try {
					wait(10);
				} catch (InterruptedException ex) {
					return null;
				}
			}
		}
		
		if (reader.ready()) {
			final String authorizationCode = reader.readLine();
			
			final Authenticator authenticator = Authenticator.ofMicrosoft(authorizationCode) //
					.build();
			
			authenticator.run();
			
			return new FileAuthenticationResult(authenticator.getResultFile());
		}
		return null;
	}
	
	@Override
	protected void finishInitalAuthentication() throws Exception {
		reader.close();
	}
}
