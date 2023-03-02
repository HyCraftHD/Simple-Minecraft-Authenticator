package net.hycrafthd.simple_minecraft_authenticator.result;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.Authenticator.Builder;

public class FileAuthenticationResult implements AuthenticationResult {
	
	private final AuthenticationFile file;
	private final String clientId;
	private final String redirectUrl;
	private final String clientSecret;
	
	public FileAuthenticationResult(AuthenticationFile file) {
		this(file, null, null, null);
	}
	
	public FileAuthenticationResult(AuthenticationFile file, String clientId, String redirectUrl, String clientSecret) {
		this.file = file;
		this.clientId = clientId;
		this.redirectUrl = redirectUrl;
		this.clientSecret = clientSecret;
	}
	
	@Override
	public AuthenticationFile getFile() {
		return file;
	}
	
	@Override
	public Authenticator buildAuthenticator(boolean xBoxProfile) {
		final Builder builder = Authenticator.of(file);
		
		if (clientId != null && redirectUrl != null) {
			builder.customAzureApplication(clientId, redirectUrl, clientSecret);
		}
		
		builder.shouldAuthenticate();
		
		if (xBoxProfile) {
			builder.shouldRetrieveXBoxProfile();
		}
		
		return builder.build();
	}
	
}
