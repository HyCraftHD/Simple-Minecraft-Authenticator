package net.hycrafthd.simple_minecraft_authenticator;

public class Main {
	
	public static void main(String[] args) {
		final AuthenticationMethod authenticationMethod = SimpleMinecraftAuthentication.getMethod("web").get().create();
		authenticationMethod.setTimeout(5);
		
		System.out.println(authenticationMethod.initalAuthenticationFile());
		
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
