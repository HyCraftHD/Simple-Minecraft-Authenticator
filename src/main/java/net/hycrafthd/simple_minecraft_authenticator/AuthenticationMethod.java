package net.hycrafthd.simple_minecraft_authenticator;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;

public interface AuthenticationMethod {
	
	AuthenticationFile executeMethod();
	
}
