
package net.hycrafthd.simple_minecraft_authenticator.util;

import java.util.Locale;

import joptsimple.ValueConverter;
import net.hycrafthd.simple_minecraft_authenticator.SimpleMinecraftAuthentication;
import net.hycrafthd.simple_minecraft_authenticator.creator.AuthenticationMethodCreator;

public class AuthenticationMethodCreatorValueConverter implements ValueConverter<AuthenticationMethodCreator> {
	
	@Override
	public AuthenticationMethodCreator convert(String value) {
		return SimpleMinecraftAuthentication.getMethodOrThrow(value.toLowerCase(Locale.ROOT));
	}
	
	@Override
	public Class<? extends AuthenticationMethodCreator> valueType() {
		return AuthenticationMethodCreator.class;
	}
	
	@Override
	public String valuePattern() {
		return null;
	}
	
}
