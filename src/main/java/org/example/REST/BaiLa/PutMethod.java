package org.example.REST.BaiLa;
import java.net.http.*;
import java.net.*;
import com.fasterxml.jackson.databind.*;
public class PutMethod {
	static String msv="b22dcvt525";
	static String qc="epTUgFTK";
	static int port=2240;
	static String ip="36.50.135.242";
	static String url="http://"+ip+":2230/api/rest/method";
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
		// --- c. Định hình URL cho Phase 2 (Truyền requestId vào URL) ---
		// URL cấu trúc: http://<Exam_IP>:2230/api/rest/method/{requestId}
		String putUrl=url+"/"+reqId;
		// --- d. Tạo Body JSON bằng String.format ---
		// Yêu cầu: answer là 1 object có trường status bằng "done"
		String jsonBody=String.format(
				"{\"studentCode\":\"%s\",\"qCode\":\"%s\",\"answer\":{\"status\":\"done\"}}"
				,msv,qc
		);
		// --- c & e. Gửi bằng đúng phương thức PUT ---
		HttpRequest putReq=HttpRequest.newBuilder().uri(URI.create(putUrl))
				.header("Content-Type","application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
		HttpResponse<String> putRes= client.send(putReq,HttpResponse.BodyHandlers.ofString());
		System.out.println(putRes.statusCode()+" "+putRes.body());
	}
}
