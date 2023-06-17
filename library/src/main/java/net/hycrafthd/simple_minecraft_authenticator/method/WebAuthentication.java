package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.simple_minecraft_authenticator.result.AuthenticationResult;
import net.hycrafthd.simple_minecraft_authenticator.result.FileAuthenticationResult;

public class WebAuthentication extends AbstractAuthenticationMethod {
	
	protected static final String BASE_URL = "http://localhost:{port}";
	
	protected static final String FAVICON = "/favicon.ico";
	protected static final String BASE_PATH = "/ms-oauth";
	protected static final String LOGIN_PATH = BASE_PATH + "/login";
	protected static final String REDIRECT_PATH = BASE_PATH + "/response";
	protected static final String ERROR_PATH = BASE_PATH + "/error";
	protected static final String SUCCESS_PATH = BASE_PATH + "/success";
	
	protected static final String AZURE_CLIENT_ID = "78590d64-3549-4c5f-9ef5-add1e816fed1";
	
	protected static final String EXISTING_REDIRECT_URL = BASE_URL.replace("{port}", Integer.toString(9999) + REDIRECT_PATH);
	
	private HttpServer server;
	
	public WebAuthentication(PrintStream out, ExecutorService executor) {
		super(out, executor);
	}
	
	@Override
	public AuthenticationResult existingAuthentication(AuthenticationFile file) {
		return new FileAuthenticationResult(file, AZURE_CLIENT_ID, EXISTING_REDIRECT_URL, null);
	}
	
	@Override
	protected AuthenticationResult runInitalAuthentication() throws IOException, AuthenticationException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		
		final int port = server.getAddress().getPort();
		final String baseUrl = BASE_URL.replace("{port}", Integer.toString(port));
		
		final URL loginUrl = new URL(baseUrl + LOGIN_PATH);
		loginUrlCallback.accept(loginUrl);
		
		out.println("Open the following link and log into your microsoft account.");
		out.println(loginUrl);
		
		final String redirectUrl = baseUrl + REDIRECT_PATH;
		
		final Function<HttpHandler, HttpHandler> defaultHandler = handler -> exchange -> {
			try (exchange) {
				if (!"GET".equals(exchange.getRequestMethod())) {
					return;
				}
				exchange.getResponseHeaders().add("Server", "Minecraft Authenticator");
				handler.handle(exchange);
			}
		};
		
		// Favicon and error and success page
		final BiFunction<String, String, HttpHandler> fileHandler = (file, contentType) -> defaultHandler.apply(exchange -> {
			final byte[] bytes;
			try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				WebAuthentication.class.getResourceAsStream("/html/" + file).transferTo(outputStream);
				bytes = outputStream.toByteArray();
			}
			exchange.getResponseHeaders().add("Content-Type", contentType);
			exchange.sendResponseHeaders(200, bytes.length);
			exchange.getResponseBody().write(bytes);
		});
		
		server.createContext(FAVICON, fileHandler.apply("favicon.ico", "image/ico"));
		server.createContext(ERROR_PATH, fileHandler.apply("error.html", "text/html"));
		server.createContext(SUCCESS_PATH, fileHandler.apply("success.html", "text/html"));
		
		server.createContext(LOGIN_PATH, defaultHandler.apply(exchange -> {
			exchange.getResponseHeaders().add("Location", Authenticator.microsoftLogin(AZURE_CLIENT_ID, redirectUrl).toString());
			exchange.sendResponseHeaders(307, -1);
		}));
		
		// Handler method
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<AuthenticationFile> authenticationFileReference = new AtomicReference<>();
		
		server.createContext(REDIRECT_PATH, defaultHandler.apply(exchange -> {
			final var query = splitQuery(exchange.getRequestURI().getQuery());
			
			final List<String> codes = query.get("code");
			if (codes == null || codes.size() != 1) {
				exchange.getResponseHeaders().add("Location", ERROR_PATH);
				exchange.sendResponseHeaders(307, -1);
				return;
			}
			
			final Authenticator authenticator = Authenticator.ofMicrosoft(codes.get(0)) //
					.customAzureApplication(AZURE_CLIENT_ID, redirectUrl) //
					.build();
			
			try {
				authenticator.run();
			} catch (AuthenticationException ex) {
				exchange.getResponseHeaders().add("Location", ERROR_PATH);
				exchange.sendResponseHeaders(307, -1);
				return;
			}
			
			exchange.getResponseHeaders().add("Location", SUCCESS_PATH);
			exchange.sendResponseHeaders(307, -1);
			
			authenticationFileReference.set(authenticator.getResultFile());
			latch.countDown();
		}));
		
		server.start();
		
		try {
			latch.await();
			out.println("Login was sucessful. Wait 5 seconds for webpages to update");
			Thread.sleep(5000);
		} catch (final InterruptedException ex) {
			return null;
		}
		
		final AuthenticationFile authenticationFile = authenticationFileReference.get();
		if (authenticationFile == null) {
			return null;
		}
		
		return new FileAuthenticationResult(authenticationFile, AZURE_CLIENT_ID, redirectUrl, null);
	}
	
	@Override
	protected void finishInitalAuthentication() throws Exception {
		server.stop(0);
	}
	
	// https://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection/13592567#13592567
	public Map<String, List<String>> splitQuery(String query) {
		if (query == null || query.isBlank()) {
			return Collections.emptyMap();
		}
		return Arrays.stream(query.split("&")) //
				.map(this::splitQueryParameter) //
				.collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}
	
	public SimpleImmutableEntry<String, String> splitQueryParameter(String part) {
		final int indexOfEqual = part.indexOf("=");
		final String key = indexOfEqual > 0 ? part.substring(0, indexOfEqual) : part;
		final String value = indexOfEqual > 0 && part.length() > indexOfEqual + 1 ? part.substring(indexOfEqual + 1) : null;
		return new SimpleImmutableEntry<>(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
	}
}
