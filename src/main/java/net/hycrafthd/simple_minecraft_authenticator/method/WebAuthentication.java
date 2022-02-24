package net.hycrafthd.simple_minecraft_authenticator.method;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.util.FunctionWithIOException;

public class WebAuthentication extends AbstractAuthenticationMethod {
	
	private static final String BASE_URL = "http://localhost:{port}";
	
	private static final String LOGIN_PATH = "/ms-oauth";
	private static final String REDIRECT_PATH = "/ms-oauth/response";
	private static final String RESULT_PATH = "/ms-oauth/result";
	
	private static final String AZURE_CLIENT_ID = "78590d64-3549-4c5f-9ef5-add1e816fed1";
	
	private ServerSocket serverSocket;
	private String loginUrl;
	
	public WebAuthentication(PrintStream out, ExecutorService executor) {
		super(out, executor);
	}
	
	public String loginUrl() {
		return loginUrl;
	}
	
	@Override
	protected AuthenticationFile runInitalAuthenticationFile() throws IOException, AuthenticationException {
		serverSocket = new ServerSocket(0);
		serverSocket.setSoTimeout(timeout * 1000);
		
		final int port = serverSocket.getLocalPort();
		final String baseUrl = BASE_URL.replace("{port}", Integer.toString(port));
		
		loginUrl = baseUrl + LOGIN_PATH;
		
		out.println("Open the following link and log into your microsoft account.");
		out.println(loginUrl);
		
		// Handle login path to redirect to ms oauth login
		handleRequest(serverSocket, (socketData) -> {
			if (socketData.resource().equals(LOGIN_PATH)) {
				simpleResponse(socketData.writer(), "307 Moved Permanently", List.of("Location: " + Authenticator.microsoftLogin(AZURE_CLIENT_ID, baseUrl + REDIRECT_PATH)));
				return true;
			}
			return false;
		});
		
		final AtomicReference<String> authorizationCode = new AtomicReference<>();
		
		// Handle login redirect
		handleRequest(serverSocket, (socketData) -> {
			if (socketData.resource().startsWith(REDIRECT_PATH)) {
				
				String code = socketData.resource().substring((REDIRECT_PATH + "?code=").length());
				final int andCode = code.indexOf("&");
				if (andCode > 0) {
					code = code.substring(0, andCode);
				}
				authorizationCode.set(code);
				
				simpleResponse(socketData.writer(), "307 Moved Permanently", List.of("Location: " + baseUrl + RESULT_PATH));
				return true;
			}
			return false;
		});
		
		// Handle result
		handleRequest(serverSocket, (socketData) -> {
			if (socketData.resource().equals(RESULT_PATH)) {
				final String resultPage;
				
				try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
					WebAuthentication.class.getResourceAsStream("/html/result.html").transferTo(outputStream);
					resultPage = outputStream.toString(StandardCharsets.UTF_8);
				}
				
				simpleResponse(socketData.writer(), "200 OK", List.of("Content-Type: text/html"), resultPage);
			}
			return true;
		});
		
		final Authenticator authenticator = Authenticator.ofMicrosoft(authorizationCode.get()) //
				.customAzureApplication(AZURE_CLIENT_ID, baseUrl + REDIRECT_PATH) //
				.build();
		
		authenticator.run();
		
		return authenticator.getResultFile();
	}
	
	@Override
	protected void finishInitalAuthenticationFile() throws Exception {
		serverSocket.close();
	}
	
	private void handleRequest(ServerSocket serverSocket, FunctionWithIOException<SocketData, Boolean> handler) throws IOException {
		boolean rightRequest = false;
		
		while (!rightRequest) {
			final Socket socket = serverSocket.accept();
			try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); //
					final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
				final String[] firstHttpLine = bufferedReader.readLine().split(" ");
				
				final String requestType = firstHttpLine[0];
				final String resource = firstHttpLine[1];
				final String httpVersion = firstHttpLine[2];
				
				final SocketData socketData = new SocketData(requestType, URLDecoder.decode(resource, StandardCharsets.UTF_8), httpVersion, socket, bufferedReader, bufferedWriter);
				
				rightRequest = handler.apply(socketData);
			} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
				throw new IOException("Cannot parse request");
			}
		}
	}
	
	private record SocketData(String requestType, String resource, String httpVersion, Socket socket, BufferedReader reader, BufferedWriter writer) {
	}
	
	private void simpleResponse(BufferedWriter writer, String type, List<String> headers) throws IOException {
		simpleResponse(writer, type, headers, null);
	}
	
	private void simpleResponse(BufferedWriter writer, String type, List<String> headers, String content) throws IOException {
		writer.write("HTTP/1.1 " + type);
		writer.newLine();
		for (final String header : headers) {
			writer.write(header);
			writer.newLine();
		}
		
		writer.write("Server: Minecraft Authenticator");
		writer.newLine();
		
		writer.write("Connection: close");
		writer.newLine();
		
		if (content != null) {
			writer.write("Content-Length: " + content.getBytes(StandardCharsets.UTF_8).length);
			writer.newLine();
			
			writer.newLine();
			writer.write(content);
		}
	}
}
