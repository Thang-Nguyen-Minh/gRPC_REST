package org.example.REST.BaiLa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class CheckSumReplay {
	static String msv="b22dcvt525";
	static String qc="0uvMqwUG";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/header";
	static String getUrl=url+"?studentCode="+msv+"&qCode="+qc;
	public static void main(String[] args) throws Exception{
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest getReq = HttpRequest.newBuilder().uri(URI.create(getUrl)).GET().build();

		HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
		Optional<String> checkSumHeader=getRes.headers().firstValue("X-Checksum");
		String checkSumValue=checkSumHeader.orElse("");
		String jsonRes = getRes.body();
		System.out.println("GET Response: " + jsonRes);
		ObjectMapper mapper=new ObjectMapper();
		JsonNode rootNode=mapper.readTree(jsonRes);
		String reqId=rootNode.get("requestId").asText();
		JsonNode dataNode=rootNode.get("data");
		// --- d & e. Cấu hình URL cho Phase 2 kết hợp Path và Query Parameter ---
		// Cấu trúc mong muốn: /api/rest/path/{productId}?studentCode=...&qCode=...&requestId=...&currency=USD
		String jsonBody=String.format(
				"{\"studentCode\":\"%s\",\"qCode\":\"%s\",\"requestId\":\"%s\"}",
				msv,qc,reqId
		);
		String putUrl=url+"/submit";
		// Gửi GET Request cho Phase 2 POST
		HttpRequest putReq=HttpRequest.newBuilder().uri(URI.create(putUrl)).header("X-Checksum",checkSumValue).
				POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
		HttpResponse<String> putRes= client.send(putReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(putRes.statusCode()+" "+putRes.body());
	}
}
