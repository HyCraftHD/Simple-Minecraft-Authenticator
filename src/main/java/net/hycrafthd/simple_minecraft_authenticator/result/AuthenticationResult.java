package net.hycrafthd.simple_minecraft_authenticator.result;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;

public interface AuthenticationResult {
	
	AuthenticationFile getFile();
	
	default Authenticator buildAuthenticator() {
		return buildAuthenticator(false);
	}
	
	Authenticator buildAuthenticator(boolean xBoxProfile);
	
}
