package org.example.REST.BaiLa;
import java.net.http.*;
import java.net.*;
import java.util.Optional;

import com.fasterxml.jackson.databind.*;
public class CheckSum {
	static String msv="b22dcvt525";
	static String qc="BzVlZa1B";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/header";
	static String getUrl=url+"?studentCode="+msv+"&qCode="+qc;
	public static void main(String[] args) throws Exception{
		HttpClient client = HttpClient.newHttpClient();

		// --- a. Gửi GET Request để nhận task ---
		HttpRequest getReq = HttpRequest.newBuilder()
				.uri(URI.create(getUrl))
				.GET()
				.build();

		HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
		// --- b & c. Đọc đúng giá trị header X-Checksum ---
		//Sử dụng getRes.headers().firstValue() để lấy giá trị của header dạng Optional
		Optional<String> checksumHeader=getRes.headers().firstValue("X-Checksum");
		String checksumValue=checksumHeader.orElse("");
		String jsonRes = getRes.body();
		System.out.println("GET Response: " + jsonRes);
		ObjectMapper mapper=new ObjectMapper();
		JsonNode rootNode=mapper.readTree(jsonRes);
		String reqId=rootNode.get("requestId").asText();
		// --- d. Chuẩn bị chuỗi JSON Body để gửi POST ---
		// Đề bài không nói answer là object chứa trường gì, nhưng cấu trúc chuẩn chung thường là truyền reqId ngoài body.
		// Dựa vào các bài trước, ta đóng gói đầy đủ:
		String jsonBody=String.format(
				"{\"studentCode\":\"%s\",\"qCode\":\"%s\",\"requestId\":\"%s\"}"
				,msv,qc,reqId
		);
		String putUrl=url+"/submit";
		// --- d & e. Gửi POST Request kèm lại header X-Checksum ---
		HttpRequest putReq=HttpRequest.newBuilder().uri(URI.create(putUrl))
				.header("Content-Type","application/json")
				.headers("X-Checksum",checksumValue)
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
		HttpResponse<String> putRes= client.send(putReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(putRes.statusCode()+" "+putRes.body());
	}
}
