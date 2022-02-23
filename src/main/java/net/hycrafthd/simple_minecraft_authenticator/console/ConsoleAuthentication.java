package net.hycrafthd.simple_minecraft_authenticator.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.simple_minecraft_authenticator.AuthenticationMethod;

public class ConsoleAuthentication implements AuthenticationMethod {
	
	private final PrintStream out;
	private final InputStream in;
	
	public ConsoleAuthentication() {
		out = System.out;
		in = System.in;
	}
	
	public ConsoleAuthentication(PrintStream out, InputStream in) {
		this.out = out;
		this.in = in;
	}
	
	@Override
	public String name() {
		return "console";
	}
	
	@Override
	public AuthenticationFile executeMethod() {
		out.println("Open the following link and log into your microsoft account. Paste the code parameter of the returned url.");
		out.println("Code should look like this: M.R3_BL2.00000000-0000-0000-0000-000000000000");
		out.println(Authenticator.microsoftLogin());
		
		// Do not close because we do not want to close the input stream
		@SuppressWarnings("resource")
		final String authorizationCode = new Scanner(in).nextLine();
		final Authenticator authenticator = Authenticator.ofMicrosoft(authorizationCode) //
				.build();
		
		try {
			authenticator.run();
		} catch (final AuthenticationException ex) {
			out.println(ex.getMessage());
		}
		
		return authenticator.getResultFile();
	}
	
}
