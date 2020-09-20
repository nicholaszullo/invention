import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.util.Scanner;

public class Tester {
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter a stock ticker: ");
		String stock = scan.nextLine();

		HttpClient client = HttpClient.newHttpClient();

		// getNewAccessKey(client);
		JSON stockinfo = getTicker(stock, client);
		System.out.println(stockinfo.getValue("lastPrice"));
		JSON balance = getBalance(client);
		System.out.println(balance.getValue("accountValue"));
		scan.close();
	}

	private static JSON getBalance(HttpClient client) {
		StringBuilder auth = new StringBuilder();
		auth.append("Bearer ");
		BufferedReader access = null;
		BufferedReader accountNumber = null;
		try {
			access = new BufferedReader(new FileReader(new File("AccessToken.key")));
		} catch (FileNotFoundException e) {
			panic("Access Token does not exist!");
		}
		try {
			auth.append(access.readLine());
			access.close();
		} catch (IOException e) {
			panic("Error reading Access Token!" + e.toString());
		}
		StringBuilder url = new StringBuilder();
		url.append("https://api.tdameritrade.com/v1/accounts/");
		try {
			accountNumber = new BufferedReader(new FileReader(new File("AccountNumber.key")));
		} catch (FileNotFoundException e1) {
			panic("Account number not found!");
		}
		try {
			url.append(accountNumber.readLine());
		} catch (IOException e1) {
			panic("Unable to read Account Number!");
		}

		HttpRequest balance = HttpRequest.newBuilder(URI.create(url.toString()))
			.header("accept", "application/json")
			.header("authorization", auth.toString())
			.GET()
			.build();
		HttpResponse<String> response = null;
		try {
			response = client.send(balance, BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			panic("Unable to send balance request! " + e.toString());
		}
		return new JSON(response.body());

	}

	private static JSON getTicker(String ticker, HttpClient client) {
		BufferedReader api = null;
		try {
			api = new BufferedReader(new FileReader(new File("ApiKey.key")));
		} catch (FileNotFoundException e) {
			panic("Api Key does not exist!");
		}
		StringBuilder url = new StringBuilder();

		url.append("https://api.tdameritrade.com/v1/marketdata/");
		url.append(ticker);
		url.append("/quotes?apikey=");
		try {
			url.append(api.readLine());
			api.close();
		} catch (IOException e1) {
			panic("Failed to read API Key! " + e1.toString());
		}
		HttpRequest req = HttpRequest.newBuilder(URI.create(url.toString())).header("accept", "application/json").GET()
				.build();
		try {
			System.out.println("sending request...");
			HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
			System.out.println("Response recieved");
			return new JSON(resp.body());

		} catch (Exception e) {
			panic("Unable to receive response! " + e.toString());
			return null;	//panic will exit but compiler needs a return statement still
		}
	}

	private static void getNewAccessKey(HttpClient client) {
		BufferedWriter access = null;
		BufferedReader refresh = null;
		BufferedReader api = null;
		try {
			access = new BufferedWriter(new FileWriter(new File("AccessToken.key"), false));
		} catch (IOException e) {
			panic("Access key cannot be created!");
		}
		try {
			refresh = new BufferedReader(new FileReader(new File("RefreshToken.key")));
		} catch (FileNotFoundException e) {
			panic("Refresh Token does not exist!");
		}
		try {
			api = new BufferedReader(new FileReader(new File("ApiKey.key")));
		} catch (FileNotFoundException e) {
			panic("Api Key does not exist!");
		}

		HttpRequest newkey = null;
		try {
			StringBuilder postBody = new StringBuilder("grant_type=refresh_token&refresh_token=");
			postBody.append(refresh.readLine());
			postBody.append("&access_type=&code=&client_id=");
			postBody.append(api.readLine());
			postBody.append("&redirect_uri=");
			newkey = HttpRequest.newBuilder().uri(URI.create("https://api.tdameritrade.com/v1/oauth2/token"))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(BodyPublishers.ofString(postBody.toString()))
					.build();
			refresh.close();
		} catch (IOException e) {
			panic("Access Token request failed to build!");
		}
		HttpResponse<String> response = null;
		try {
			response = client.send(newkey, BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			panic("Access Token request failed!");
		}
		try {
			JSON resp = new JSON(response.body());
			access.write(resp.getValue("access_token"));
			access.close();
		} catch (IOException e) {
			panic("Failed to write response to file!");
		}
	
	}
	private static void panic(String s){
		System.out.println(s);
		System.exit(0);
	}
	
}