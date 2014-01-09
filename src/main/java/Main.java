package main.java;

import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class Main {

	private static final String ACCESS_KEY = "xxxxx";
	private static final String SECRET_KEY = "xxxxx";

	/**
	 * 全テーブルの中から指定の接頭辞のテーブルに対して、スループットを下げる
	 */
	public static void main(String[] args) throws Exception {
		AmazonDynamoDBClient client = createClient();
		Service service = new Service(client);
		
		List<String> tableNames = service.getAllTableNames();
		for (String name :tableNames) {
			if (name.startsWith("dev02-")) {
				service.updateThroughput(name, 10L, 5L);
			}
		}
	}

	/**
	 * 環境設定
	 */
	private static AmazonDynamoDBClient createClient() throws Exception {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Region tokyo = Region.getRegion(Regions.AP_NORTHEAST_1);
		client.setRegion(tokyo);
		return client;
	}

}