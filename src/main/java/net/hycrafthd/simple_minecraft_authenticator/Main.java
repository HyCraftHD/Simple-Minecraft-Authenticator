package net.hycrafthd.simple_minecraft_authenticator;

import net.hycrafthd.simple_minecraft_authenticator.web.WebAuthentication;

public class Main {
	
	public static void main(String[] args) {
		System.out.println(new WebAuthentication().executeMethod());
	}
	
}
