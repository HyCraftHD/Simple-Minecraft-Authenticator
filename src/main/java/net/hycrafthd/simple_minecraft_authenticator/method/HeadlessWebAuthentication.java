package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.microsoft.AzureApplication;
import net.hycrafthd.simple_minecraft_authenticator.result.AuthenticationResult;
import net.hycrafthd.simple_minecraft_authenticator.result.FileAuthenticationResult;
import net.hycrafthd.simple_minecraft_authenticator.util.UnclosableInputStream;

public class HeadlessWebAuthentication extends AbstractAuthenticationMethod {
	
	private final BufferedReader reader;
	private final AzureApplication azureApplication;
	
	public HeadlessWebAuthentication(PrintStream out, InputStream in, ExecutorService executor) {
		super(out, executor);
		reader = new BufferedReader(new InputStreamReader(new UnclosableInputStream(in), StandardCharsets.UTF_8));
		azureApplication = new AzureApplication(WebAuthentication.AZURE_CLIENT_ID, WebAuthentication.BASE_URL.replace("{port}", Integer.toString(9999) + WebAuthentication.REDIRECT_PATH));
	}
	
	@Override
	public AuthenticationResult existingAuthentication(AuthenticationFile file) {
		return new FileAuthenticationResult(file, Optional.of(azureApplication));
	}
	
	@Override
	protected AuthenticationResult runInitalAuthentication() throws IOException, AuthenticationException {
		final URL loginUrl = Authenticator.microsoftLogin(azureApplication.clientId(), azureApplication.redirectUrl());
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
					.customAzureApplication(azureApplication.clientId(), azureApplication.redirectUrl()) //
					.build();
			
			authenticator.run();
			
			return new FileAuthenticationResult(authenticator.getResultFile(), Optional.of(azureApplication));
		}
		return null;
	}
	
	@Override
	protected void finishInitalAuthentication() throws Exception {
		reader.close();
	}
}
