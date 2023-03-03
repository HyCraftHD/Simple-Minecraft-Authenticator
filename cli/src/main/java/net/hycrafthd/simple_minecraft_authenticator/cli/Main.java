package net.hycrafthd.simple_minecraft_authenticator.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.simple_minecraft_authenticator.cli.util.AuthenticationMethodCreatorValueConverter;
import net.hycrafthd.simple_minecraft_authenticator.creator.AuthenticationMethodCreator;
import net.hycrafthd.simple_minecraft_authenticator.util.SimpleAuthenticationFileUtil;
import net.hycrafthd.simple_minecraft_authenticator.util.SimpleAuthenticationFileUtil.AuthenticationData;

public class Main {
	
	private static final Logger LOGGER = LogManager.getLogger("Simple Minecraft Authenticator CLI");
	
	public static void main(String[] args) {
		final OptionParser parser = new OptionParser();
		
		// Default specs
		final OptionSpec<Void> helpSpec = parser.accepts("help", "Show the help menu").forHelp();
		final OptionSpec<AuthenticationMethodCreator> methodSpec = parser.accepts("method", "Authentication method that should be used when file does not exists. Method 'console' and 'web' are always available").withRequiredArg().withValuesConvertedBy(new AuthenticationMethodCreatorValueConverter());
		final OptionSpec<Path> fileSpec = parser.accepts("file", "Authentication file to read and update. If file does not exist, or is not usable, then the user will be prompted to login with the selected authentication method.").withRequiredArg().withValuesConvertedBy(new PathConverter());
		final OptionSpec<Void> headlessSpec = parser.accepts("headless", "Force the authentication method to use a headless mode");
		
		final OptionSpec<Path> userFileSpec = parser.accepts("user-file", "File with the minecraft user login information").withRequiredArg().withValuesConvertedBy(new PathConverter());
		final OptionSpec<Path> xBoxFileSpec = parser.accepts("xbox-file", "File with the xbox user settings").withRequiredArg().withValuesConvertedBy(new PathConverter());
		
		final OptionSet set = parser.parse(args);
		
		if (set.has(helpSpec) || !set.has(fileSpec)) {
			try (final OutputStream outputStream = IoBuilder.forLogger(LOGGER).setLevel(Level.ERROR).buildOutputStream()) {
				parser.printHelpOn(outputStream);
			} catch (final IOException ex) {
				LOGGER.error("Cannot print help on console", ex);
			}
			return;
		}
		
		final AuthenticationMethodCreator authenticationMethod = set.valueOf(methodSpec);
		final Path file = set.valueOf(fileSpec);
		final boolean headless = set.has(headlessSpec);
		
		final Path userFile = set.valueOf(userFileSpec);
		final Path xBoxFile = set.valueOf(xBoxFileSpec);
		
		if (headless) {
			LOGGER.info("Force headless authentication mode");
		}
		
		try (final PrintStream loggerStream = IoBuilder.forLogger(LOGGER).setLevel(Level.INFO).setAutoFlush(true).buildPrintStream()) {
			Authenticator selectedAuthenticator = null;
			AuthenticationMethodCreator selectedCreator = null;
			
			// Try to read file and use that for authentication
			if (Files.exists(file)) {
				if (!Files.isRegularFile(file) || !Files.isReadable(file) || !Files.isWritable(file)) {
					LOGGER.fatal("Cannot read and write to the supplied authentication file {}", file);
					return;
				}
				
				try {
					final byte[] bytes = Files.readAllBytes(file);
					final AuthenticationData authenticationData = SimpleAuthenticationFileUtil.read(bytes);
					
					LOGGER.info("Use an existing authentication file with method " + authenticationData.creator().name());
					
					selectedAuthenticator = authenticationData.creator().create(headless, loggerStream, System.in).existingAuthentication(authenticationData.file()).buildAuthenticator(xBoxFile != null);
					selectedCreator = authenticationData.creator();
				} catch (final IOException ex) {
					LOGGER.error("Could not use existing authentication file", ex);
				}
			}
			
			// Create new authentication
			if (selectedAuthenticator == null || selectedCreator == null) {
				LOGGER.info("Requested a new oauth authentication with method " + authenticationMethod.name());
				
				try {
					selectedAuthenticator = authenticationMethod.create(headless, loggerStream, System.in).initalAuthentication().buildAuthenticator(xBoxFile != null);
					selectedCreator = authenticationMethod;
				} catch (final AuthenticationException ex) {
					LOGGER.fatal("Inital authentication failed. Run the program again for an other try", ex);
					return;
				}
			}
			
			if (xBoxFile != null) {
				LOGGER.info("Run authentication for minecraft and xBox services");
			} else {
				LOGGER.info("Run authentication for minecraft services");
			}
			
			final Authenticator authenticator = selectedAuthenticator;
			final AuthenticationMethodCreator creator = selectedCreator;
			
			// Save lambda for authentication file
			final Predicate<AuthenticationFile> saveFile = resultFile -> {
				try {
					final byte[] bytes = SimpleAuthenticationFileUtil.write(new AuthenticationData(resultFile, creator));
					Files.write(file, bytes);
					return true;
				} catch (final IOException ex) {
					LOGGER.fatal("Cannot save authentication file", ex);
					return false;
				}
			};
			
			// Run authenticator and save authentication file
			try {
				authenticator.run();
			} catch (final AuthenticationException ex) {
				LOGGER.fatal("An error occured while authentication. Trying to save authentication file", ex);
				if (authenticator.getResultFile() != null) {
					if (!saveFile.test(authenticator.getResultFile())) {
						return;
					}
				}
				return;
			}
			
			if (!saveFile.test(authenticator.getResultFile())) {
				return;
			}
			
			// Write files
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			if (userFile != null) {
				LOGGER.info("Write user file");
				try {
					Files.writeString(userFile, gson.toJson(authenticator.getUser().get()), StandardCharsets.UTF_8);
				} catch (final IOException ex) {
					LOGGER.error("Could not write user file", ex);
				}
			}
			
			if (xBoxFile != null) {
				LOGGER.info("Write xBox profile file");
				try {
					Files.writeString(xBoxFile, gson.toJson(authenticator.getXBoxProfile().get()), StandardCharsets.UTF_8);
				} catch (final IOException ex) {
					LOGGER.error("Could not write xBox profile file", ex);
				}
			}
		}
	}
}
