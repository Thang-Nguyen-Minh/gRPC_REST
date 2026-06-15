package org.example.REST.Rest_Object;
import com.fasterxml.jackson.databind.*;
import com.google.api.Http;

import java.util.*;
import java.net.http.*;
import java.net.*;

public class SLA {
	static String msv="b22dcvt525";
	static String qc="MbyevW6w";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/object";
	static String getUrl=url+"?studentCode="+msv+"&qCode="+qc;
	public static void main(String[] args) throws Exception{
		HttpClient client=HttpClient.newHttpClient();
		HttpRequest getReq=HttpRequest.newBuilder().uri(URI.create(getUrl)).GET().build();
		HttpResponse<String> getRes= client.send(getReq,HttpResponse.BodyHandlers.ofString());
		String jsonRes=getRes.body();
		System.out.println(jsonRes);
		ObjectMapper mapper=new ObjectMapper();
		JsonNode rootNode=mapper.readTree(jsonRes);
		String reqId=rootNode.get("requestId").asText();
		JsonNode dataNode=rootNode.get("data");
		double weightKg=dataNode.get("weightKg").asDouble();
		int maxEtaDays=dataNode.get("maxEtaDays").asInt();
		JsonNode quoteNode=dataNode.get("quotes");
		//Init : khác 1 tí, ở đây khởi tạo đúng những biến phải tìm và 1 vòng lặp xong luôn cmm
		String bestCarri="";
		double minFee=Double.MAX_VALUE;
		double bestReli=0.0;
		int bestEtaDays=0;

		if(quoteNode.isArray()){
			for(JsonNode carrierNode:quoteNode){
				int etaDays=carrierNode.get("etaDays").asInt();
				if(etaDays<=maxEtaDays){
					double totalFee=carrierNode.get("baseFee").asDouble()+weightKg*carrierNode.get("perKgFee").asDouble();
					totalFee=Math.round(totalFee*100)/100.0;
					double reli=carrierNode.get("reliability").asDouble();
					//1 so sánh cho cả 2 trường hợp minFee(min) và reli(max) cho nhanh
					if (totalFee<minFee || (totalFee==minFee && reli>bestReli)){
						minFee=totalFee;
						bestReli=reli;
						bestCarri=carrierNode.get("carrier").asText();
						bestEtaDays=etaDays;
					}
				}
			}
		}
		String getUrl=url+"/submit";
		String jsonBody=String.format(
				"{\"studentCode\":\"%s\",\"qCode\":\"%s\",\"requestId\":\"%s\",\"answer\":{\"carrier\":\"%s\"," +
						"\"totalFee\":\"%s\"," +
						"\"etaDays\":\"%s\"}}",
				msv,qc,reqId,bestCarri,minFee,bestEtaDays
		);
		HttpRequest postReq=
				HttpRequest.newBuilder().uri(URI.create(getUrl)).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
		HttpResponse<String> postRes= client.send(postReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(postRes.statusCode()+" "+postRes.body());
	}
}
