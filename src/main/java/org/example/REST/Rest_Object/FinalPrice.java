package org.example.REST.Rest_Object;
import com.fasterxml.jackson.databind.*;
import java.net.http.*;
import java.net.*;
import java.util.*;

public class FinalPrice {
	static String msv="b22dcvt525";
	static String qc="EluWHzfW";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/object";
	static String getUrl=url+"?studentCode="+msv+"&qCode="+qc;
	public static void main(String[] args) throws Exception{
		HttpClient client=HttpClient.newHttpClient();
		HttpRequest getReq=HttpRequest.newBuilder().uri(URI.create(getUrl)).GET().build();
		HttpResponse<String> getRes=client.send(getReq,HttpResponse.BodyHandlers.ofString());
		String jsonRes=getRes.body();
		ObjectMapper mapper=new ObjectMapper();
		JsonNode rootNode=mapper.readTree(jsonRes);
		//Phải lấy ra thằng object chứa nó là data trước rồi mới lấy được các thằng bên trong
		JsonNode dataNode=rootNode.get("data");
		double price=dataNode.get("price").asDouble();
		double taxRate=dataNode.get("taxRate").asDouble();
		double discount=dataNode.get("discount").asDouble();
		double finalPrice = price * (1 + taxRate / 100) * (1 - discount / 100);
		finalPrice=Math.round(finalPrice*100)/100.0;
		String answer=String.valueOf(finalPrice);
		String postUrl=url+"/submit";
		String reqId=rootNode.get("requestId").asText();
		//Gửi Object chứa finalPrice thì cái finalPrice chí là %s
		//answer:{finalPrice:%s}, nhớ có dấu /
		String jsonBody=String.format(
				"{\"studentCode\":\"%s\",\"qCode\":\"%s\",\"requestId\":\"%s\",\"answer\":{\"finalPrice\":\"%s\"}}",
				msv,qc,reqId,answer
		);
		HttpRequest postReq=HttpRequest.newBuilder().uri(URI.create(postUrl)).header("Content-Type","application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
		HttpResponse<String> postRes= client.send(postReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(postRes.statusCode()+" "+postRes.body());
	}
}
