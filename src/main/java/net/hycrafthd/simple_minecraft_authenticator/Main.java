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
import net.hycrafthd.simple_minecraft_authenticator.creator.AuthenticationMethodCreator;
import net.hycrafthd.simple_minecraft_authenticator.util.AuthenticationMethodCreatorValueConverter;

public class Main {
	
	public static void main(String[] args) throws IOException, AuthenticationException {
		final OptionParser parser = new OptionParser();
		
		// Default specs
		final OptionSpec<Void> helpSpec = parser.accepts("help", "Show the help menu").forHelp();
		final OptionSpec<AuthenticationMethodCreator> authenticationMethodCreatorSpec = parser.accepts("method", "Authentication method that should be used. Method 'console' is always available").withRequiredArg().defaultsTo(SimpleMinecraftAuthentication.getAvailableMethods().toArray(String[]::new)).withValuesConvertedBy(new AuthenticationMethodCreatorValueConverter());
		final OptionSpec<Path> fileSpec = parser.accepts("file", "Authentication file to read and update. If file does not exist, or is not usable, then the user will be prompted to login with the selected authentication method.").withRequiredArg().withValuesConvertedBy(new PathConverter());
		
		final OptionSpec<Path> userFileSpec = parser.accepts("user-file", "File with the minecraft user login information").withRequiredArg().withValuesConvertedBy(new PathConverter());
		final OptionSpec<Path> xBoxFileSpec = parser.accepts("xbox-file", "File with the xbox user settings").withRequiredArg().withValuesConvertedBy(new PathConverter());
		
		final OptionSet set = parser.parse(args);
		
		if (set.has(helpSpec) || !set.has(authenticationMethodCreatorSpec) || !set.has(fileSpec)) {
			parser.printHelpOn(System.out);
			return;
		}
		
		final AuthenticationMethodCreator authenticationMethodCreator = set.valueOf(authenticationMethodCreatorSpec);
		final Path file = set.valueOf(fileSpec);
		
		final Path userFile = set.valueOf(userFileSpec);
		final Path xBoxFile = set.valueOf(xBoxFileSpec);
		
		Authenticator authenticator = null;
		
		if (Files.exists(file) && Files.isReadable(file)) {
			System.out.println("Read existing authentication file");
			try {
				final AuthenticationFile authFile = AuthenticationFile.readCompressed(Files.readAllBytes(file));
				final Authenticator existingAuthenticator = authenticationMethodCreator.create().existingAuthentication(authFile).buildAuthenticator(xBoxFile != null);
				existingAuthenticator.run();
				authenticator = existingAuthenticator;
			} catch (final IOException | AuthenticationException ex) {
				System.out.println("Cannot authenticate with existing authentication file because: " + ex.getMessage());
				System.out.println("Try with a fresh login");
			}
		}
		
		if (authenticator == null) {
			authenticator = authenticationMethodCreator.create().initalAuthentication().buildAuthenticator(xBoxFile != null);
			authenticator.run();
		}
		
		System.out.println("Finished authentication. Writing files");
		
		Files.write(file, authenticator.getResultFile().writeCompressed());
		
		if (userFile != null) {
			final User user = authenticator.getUser().get();
			
			Files.write(userFile, List.of(user.uuid(), user.name(), user.accessToken(), user.type(), user.xuid(), user.clientId()), StandardCharsets.UTF_8);
		}
		
		if (xBoxFile != null) {
			final XBoxProfile profile = authenticator.getXBoxProfile().get();
			
			final List<String> list = new ArrayList<>();
			list.add(profile.xuid());
			list.add(Boolean.toString(profile.isSponsoredUser()));
			profile.settings().forEach(setting -> list.add(setting.id() + ": " + setting.value()));
			
			Files.write(xBoxFile, list, StandardCharsets.UTF_8);
		}
	}
}
