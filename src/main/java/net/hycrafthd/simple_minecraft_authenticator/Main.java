package net.hycrafthd.simple_minecraft_authenticator;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;

public class Main {
	
	public static void main(String[] args) {
		final AuthenticationMethod authenticationMethod = SimpleMinecraftAuthentication.getMethod("console").get().create();
		authenticationMethod.setTimeout(5);
		
		try {
			System.out.println(authenticationMethod.initalAuthenticationFile());
		} catch (AuthenticationException e1) {
			e1.printStackTrace();
		}
		
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
