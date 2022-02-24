package net.hycrafthd.simple_minecraft_authenticator.method;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;

public interface AuthenticationMethod {
	
	void setTimeout(int seconds);
	
	AuthenticationFile initalAuthenticationFile() throws AuthenticationException;
	
}
