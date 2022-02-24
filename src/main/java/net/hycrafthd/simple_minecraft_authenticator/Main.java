package net.hycrafthd.simple_minecraft_authenticator;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.simple_minecraft_authenticator.method.AuthenticationMethod;
import net.hycrafthd.simple_minecraft_authenticator.result.AuthenticationResult;

public class Main {
	
	public static void main(String[] args) {
		final AuthenticationMethod authenticationMethod = SimpleMinecraftAuthentication.getMethod("web").get().create();
		authenticationMethod.setTimeout(60);
		authenticationMethod.registerLoginUrlCallback(url -> {
		});
		
		try {
			final AuthenticationResult result = authenticationMethod.initalAuthentication();
			
			System.out.println(result.getFile());
			
			final Authenticator authenticator = result.buildAuthenticator(true);
			
			authenticator.run();
			
			System.out.println(authenticator.getUser().get());
			System.out.println(authenticator.getXBoxProfile().get());
			System.out.println(authenticator.getResultFile());
			
		} catch (AuthenticationException e1) {
			e1.printStackTrace();
		}
	}
}
