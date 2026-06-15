package org.example.REST.BaiLa;
import com.fasterxml.jackson.databind.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.*;
import java.util.*;
public class HMAC {
	static String msv = "b22dcvt525";
	static String qc = "9ueyBUGt"; // Thay bằng qCode tương ứng khi làm bài
	static String ip = "36.50.135.242";

	static String url = "http://" + ip + ":2230/api/rest/header";
	static String getUrl = url + "?studentCode=" + msv + "&qCode=" + qc;

	public static void main(String[] args) throws Exception {
		HttpClient client = HttpClient.newHttpClient();

		// --- a. Gửi GET Request Phase 1 ---
		HttpRequest getReq = HttpRequest.newBuilder().uri(URI.create(getUrl)).GET().build();
		HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
		String jsonRes = getRes.body();
		System.out.println("GET Response: " + jsonRes);

		// --- b. Bóc tách dữ liệu từ JSON ---
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(jsonRes);
		String reqId = rootNode.get("requestId").asText();

		JsonNode dataNode = rootNode.get("data");
		String nonce = dataNode.get("nonce").asText();
		String signingKey = dataNode.get("signingKey").asText();

		// Trích xuất mảng events từ JSON thành danh sách List<String>
		JsonNode eventsNode = dataNode.get("events");
		List<String> eventsList = new ArrayList<>();
		if (eventsNode.isArray()) {
			for (JsonNode event : eventsNode) {
				eventsList.add(event.asText());
			}
		}

		// --- c. Tạo chuỗi Payload theo đúng cấu trúc đề bài ---
		// Nối các phần tử trong mảng events bằng dấu '|'
		String joinedEvents = String.join("|", eventsList);
		// Định dạng nonce:event1|event2:STUDENT_CODE_UPPER
		String payload = String.format("%s:%s:%s", nonce, joinedEvents, msv.toUpperCase());
		System.out.println("Chuỗi Payload được tạo: " + payload);

		// --- c. Tính mã hóa HMAC-SHA256 ---
		String signatureHex = hmacSha256(payload, signingKey);
		System.out.println("Chữ ký X-Signature (Hex): " + signatureHex);

		// --- d. Chuẩn bị JSON Body cho Phase 2 ---
		String jsonBody = String.format(
				"{\"studentCode\":\"%s\",\"qCode\":\"%s\",\"requestId\":\"%s\"}",
				msv, qc, reqId
		);

		// --- d & e. Gửi POST Request kèm cặp Header X-Signature ---
		String postUrl = url + "/submit";
		HttpRequest postReq = HttpRequest.newBuilder()
				.uri(URI.create(postUrl))
				.header("Content-Type", "application/json")
				.header("X-Signature", signatureHex) // Gửi chữ ký lên Header thay vì Body
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.build();

		// Nhận kết quả chấm bài từ Server
		HttpResponse<String> postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());
		System.out.println("\nKết quả từ Server:");
		System.out.println("Status Code: " + postRes.statusCode());
		System.out.println("Response Body: " + postRes.body());
	}

	/**
	 * Hàm tiện ích tính toán HMAC-SHA256 mã hóa mặc định trong Java
	 */
	private static String hmacSha256(String data, String key) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(secretKeySpec);
		byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

		// Chuyển đổi mảng byte sang chuỗi Hex
		StringBuilder hexString = new StringBuilder();
		for (byte b : rawHmac) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString(); // Trả về chuỗi hex viết thường
	}
}
