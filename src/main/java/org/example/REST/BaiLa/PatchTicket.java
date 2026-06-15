package org.example.REST.BaiLa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PatchTicket {
	static String msv="b22dcvt525";
	static String qc="BSgboKgF";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/method";
	static String getUrl=url+"?studentCode="+msv+"&qCode="+qc;
	public static void main(String[] args) throws Exception{
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest getReq = HttpRequest.newBuilder().uri(URI.create(getUrl)).GET().build();

		HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
		String jsonRes = getRes.body();
		System.out.println("GET Response: " + jsonRes);
		ObjectMapper mapper=new ObjectMapper();
		JsonNode rootNode=mapper.readTree(jsonRes);
		String reqId=rootNode.get("requestId").asText();
		JsonNode dataNode=rootNode.get("data");
		String etag=dataNode.get("etag").asText();
		// --- d & e. Cấu hình URL cho Phase 2 kết hợp Path và Query Parameter ---
		// Cấu trúc mong muốn: /api/rest/path/{productId}?studentCode=...&qCode=...&requestId=...&currency=USD
		String jsonBody=String.format(
				"{\"studentCode\":\"%s\",\"qCode\":\"%s\",\"answer\":{\"status\":\"RESOLVED\"}}",
				msv,qc
		);
		String putUrl=url+"/"+reqId;
		// Gửi GET Request cho Phase 2 PATCH
		HttpRequest putReq=HttpRequest.newBuilder().uri(URI.create(putUrl)).header("If-Match",etag).
			method("PATCH",HttpRequest.BodyPublishers.ofString(jsonBody)).build();
		HttpResponse<String> putRes= client.send(putReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(putRes.statusCode()+" "+putRes.body());
	}
}
