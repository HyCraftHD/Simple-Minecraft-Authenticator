package net.hycrafthd.simple_minecraft_authenticator.result;

import java.util.Optional;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.Authenticator.Builder;
import net.hycrafthd.minecraft_authenticator.microsoft.AzureApplication;

public class FileAuthenticationResult implements AuthenticationResult {
	
	private final AuthenticationFile file;
	private final Optional<AzureApplication> azureApplication;
	
	public FileAuthenticationResult(AuthenticationFile file) {
		this(file, Optional.empty());
	}
	
	public FileAuthenticationResult(AuthenticationFile file, Optional<AzureApplication> azureApplication) {
		this.file = file;
		this.azureApplication = azureApplication;
	}
	
	@Override
	public AuthenticationFile getFile() {
		return file;
	}
	
	@Override
	public Authenticator buildAuthenticator(boolean xBoxProfile) {
		final Builder builder = Authenticator.of(file);
		
		azureApplication.ifPresent(azure -> {
			builder.customAzureApplication(azure.clientId(), azure.redirectUrl(), azure.clientSecret());
		});
		
		builder.shouldAuthenticate();
		
		if (xBoxProfile) {
			builder.shouldRetrieveXBoxProfile();
		}
		
		return builder.build();
	}
	
}
