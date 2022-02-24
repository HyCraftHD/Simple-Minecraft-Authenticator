package net.hycrafthd.simple_minecraft_authenticator.method;

import java.net.URL;
import java.util.function.Consumer;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.simple_minecraft_authenticator.result.AuthenticationResult;

public interface AuthenticationMethod {
	
	void setTimeout(int seconds);
	
	void registerLoginUrlCallback(Consumer<URL> loginUrl);
	
	AuthenticationResult existingAuthentication(AuthenticationFile file);
	
	AuthenticationResult initalAuthentication() throws AuthenticationException;
	
}
