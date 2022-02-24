package net.hycrafthd.simple_minecraft_authenticator.util;

import java.io.IOException;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.simple_minecraft_authenticator.SimpleMinecraftAuthentication;
import net.hycrafthd.simple_minecraft_authenticator.creator.AuthenticationMethodCreator;

public class SimpleAuthenticationFileUtil {
	
	public static byte[] write(AuthenticationData data) throws IOException {
		final AuthenticationFile file = data.file();
		
		file.getExtraProperties().put(SimpleMinecraftAuthentication.METHOD_EXTRA_PROPERTY, data.creator().name());
		
		return file.writeCompressed();
	}
	
	public static AuthenticationData read(byte[] bytes) throws IOException {
		final AuthenticationFile file = AuthenticationFile.readCompressed(bytes);
		
		final String method = file.getExtraProperties().get(SimpleMinecraftAuthentication.METHOD_EXTRA_PROPERTY);
		
		final AuthenticationMethodCreator creator = SimpleMinecraftAuthentication.getMethod(method).orElseThrow(() -> new IOException("Cannot find unknown method '" + method + "' for authentication"));
		
		return new AuthenticationData(file, creator);
	}
	
	public static record AuthenticationData(AuthenticationFile file, AuthenticationMethodCreator creator) {
	}
	
}
