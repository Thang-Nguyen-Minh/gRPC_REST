package org.example.REST.BaiLa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PhanTrang {
	static String msv="b22dcvt525";
	static String qc="aETSGKEx";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/path";
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

		double maxAmount=Double.MIN_VALUE;
		int page=0;
		String id="";
		for(JsonNode js : dataNode){
			if(js.get("status").asText().equals("OVERDUE")){
				double amount=js.get("overdueAmount").asDouble();
				if(amount>maxAmount){
					maxAmount=amount;
					page=js.get("page").asInt();
					id=js.get("customerId").asText();
				}
			}
		}

		// --- d & e. Cấu hình URL cho Phase 2 kết hợp Path và Query Parameter ---
		// Cấu trúc mong muốn: /api/rest/path/{productId}?studentCode=...&qCode=...&requestId=...&currency=USD
		String jsonBody=String.format(
				"%s/%s?studentCode=%s&qCode=%s&requestId=%s&status=OVERDUE&page=%s",
				url,id,msv,qc,reqId,page
		);
		// Gửi GET Request cho Phase 2 (Bài này phase 2 vẫn là GET chứ không phải POST submit)
		HttpRequest putReq=HttpRequest.newBuilder().uri(URI.create(jsonBody))
				.GET().build();
		HttpResponse<String> putRes= client.send(putReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(putRes.statusCode()+" "+putRes.body());
	}
}
