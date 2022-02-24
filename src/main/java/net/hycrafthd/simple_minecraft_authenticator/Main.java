package net.hycrafthd.simple_minecraft_authenticator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.hycrafthd.minecraft_authenticator.login.XBoxProfile;
import net.hycrafthd.minecraft_authenticator.util.ConsumerWithIOException;
import net.hycrafthd.simple_minecraft_authenticator.creator.AuthenticationMethodCreator;
import net.hycrafthd.simple_minecraft_authenticator.util.AuthenticationMethodCreatorValueConverter;
import net.hycrafthd.simple_minecraft_authenticator.util.SimpleAuthenticationFileUtil;
import net.hycrafthd.simple_minecraft_authenticator.util.SimpleAuthenticationFileUtil.AuthenticationData;

public class Main {
	
	public static void main(String[] args) throws IOException, AuthenticationException {
		final OptionParser parser = new OptionParser();
		
		// Default specs
		final OptionSpec<Void> helpSpec = parser.accepts("help", "Show the help menu").forHelp();
		final OptionSpec<AuthenticationMethodCreator> authenticateSpec = parser.accepts("authenticate", "Authentication method that should be used when file does not exists. Method 'console' and 'web' are always available").withRequiredArg().withValuesConvertedBy(new AuthenticationMethodCreatorValueConverter());
		final OptionSpec<Path> fileSpec = parser.accepts("file", "Authentication file to read and update. If file does not exist, or is not usable, then the user will be prompted to login with the selected authentication method.").withRequiredArg().withValuesConvertedBy(new PathConverter());
		final OptionSpec<Void> headlessSpec = parser.accepts("headless", "Force the authentication method to use a headless mode");
		
		final OptionSpec<Path> userFileSpec = parser.accepts("user-file", "File with the minecraft user login information").withRequiredArg().withValuesConvertedBy(new PathConverter());
		final OptionSpec<Path> xBoxFileSpec = parser.accepts("xbox-file", "File with the xbox user settings").withRequiredArg().withValuesConvertedBy(new PathConverter());
		
		final OptionSet set = parser.parse(args);
		
		if (set.has(helpSpec) || !set.has(fileSpec)) {
			parser.printHelpOn(System.out);
			return;
		}
		
		final boolean authenticate = set.has(authenticateSpec);
		final AuthenticationMethodCreator authenticateMethod = set.valueOf(authenticateSpec);
		
		final Path file = set.valueOf(fileSpec);
		final boolean headless = set.has(headlessSpec);
		
		final Path userFile = set.valueOf(userFileSpec);
		final Path xBoxFile = set.valueOf(xBoxFileSpec);
		
		final Authenticator authenticator;
		final AuthenticationMethodCreator creator;
		
		if (headless) {
			System.out.println("Force headless authentication mode");
		}
		
		if (authenticate) {
			System.out.println("Requested a new oauth authentication with method " + authenticateMethod.name());
			authenticator = authenticateMethod.create(headless).initalAuthentication().buildAuthenticator(xBoxFile != null);
			creator = authenticateMethod;
		} else {
			final byte[] bytes = Files.readAllBytes(file);
			final AuthenticationData authenticationData = SimpleAuthenticationFileUtil.read(bytes);
			
			System.out.println("Use an existing authentication file with method " + authenticationData.creator().name());
			
			authenticator = authenticationData.creator().create(headless).existingAuthentication(authenticationData.file()).buildAuthenticator(xBoxFile != null);
			creator = authenticationData.creator();
		}
		
		if (xBoxFile != null) {
			System.out.println("Run authentication for minecraft and xBox services");
		} else {
			System.out.println("Run authentication for minecraft services");
		}
		
		final ConsumerWithIOException<AuthenticationFile> saveFile = resultFile -> {
			final byte[] bytes = SimpleAuthenticationFileUtil.write(new AuthenticationData(resultFile, creator));
			Files.write(file, bytes);
		};
		
		try {
			authenticator.run();
		} catch (final AuthenticationException ex) {
			System.out.println("An error occured. Save authentication file before");
			if (authenticator.getResultFile() != null) {
				saveFile.accept(authenticator.getResultFile());
			}
			throw ex;
		}
		
		saveFile.accept(authenticator.getResultFile());
		
		if (userFile != null) {
			System.out.println("Write user file");
			final User user = authenticator.getUser().get();
			
			Files.write(userFile, List.of(user.uuid(), user.name(), user.accessToken(), user.type(), user.xuid(), user.clientId()), StandardCharsets.UTF_8);
		}
		
		if (xBoxFile != null) {
			System.out.println("Write xBox profile settings");
			final XBoxProfile profile = authenticator.getXBoxProfile().get();
			
			final List<String> list = new ArrayList<>();
			list.add(profile.xuid());
			list.add(Boolean.toString(profile.isSponsoredUser()));
			profile.settings().forEach(setting -> list.add(setting.id() + ": " + setting.value()));
			
			Files.write(xBoxFile, list, StandardCharsets.UTF_8);
		}
	}
}
