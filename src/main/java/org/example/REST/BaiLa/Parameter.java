package org.example.REST.BaiLa;
import java.net.http.*;
import java.net.*;
import java.util.Optional;

import com.fasterxml.jackson.databind.*;
public class Parameter {
	static String msv="b22dcvt525";
	static String qc="nAHoJrtL";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/path";
	static String getUrl=url+"?studentCode="+msv+"&qCode="+qc;
	public static void main(String[] args) throws Exception{
		HttpClient client = HttpClient.newHttpClient();

		// --- a. Gửi GET Request để nhận task ---
		HttpRequest getReq = HttpRequest.newBuilder()
				.uri(URI.create(getUrl))
				.GET()
				.build();

		HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
		String jsonRes = getRes.body();
		System.out.println("GET Response: " + jsonRes);
		ObjectMapper mapper=new ObjectMapper();
		JsonNode rootNode=mapper.readTree(jsonRes);
		String reqId=rootNode.get("requestId").asText();
		JsonNode dataNode=rootNode.get("data");
		String selectId="";
		if(dataNode.isArray() && dataNode.size()>0){
			selectId=dataNode.get(0).get("id").asText();
		}
		// --- d & e. Cấu hình URL cho Phase 2 kết hợp Path và Query Parameter ---
		// Cấu trúc mong muốn: /api/rest/path/{productId}?studentCode=...&qCode=...&requestId=...&currency=USD
		String jsonBody=String.format(
				"%s/%s?studentCode=%s&qCode=%s&requestId=%s&currency=USD",
				url,selectId,msv,qc,reqId
		);
		// Gửi GET Request cho Phase 2 (Bài này phase 2 vẫn là GET chứ không phải POST submit)
		HttpRequest putReq=HttpRequest.newBuilder().uri(URI.create(jsonBody))
				.GET().build();
		HttpResponse<String> putRes= client.send(putReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(putRes.statusCode()+" "+putRes.body());
	}
}
