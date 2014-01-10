package tool;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class Main {

	private static final String ACCESS_KEY = "xxxxx";
	private static final String SECRET_KEY = "xxxxx";
	private static Regions RESION = Regions.AP_NORTHEAST_1; //東京リージョン

	/**
	 * 全テーブルの中から指定の接頭辞のテーブルに対して、スループットを下げる
	 */
	public static void main(String[] args) throws Exception {
		Service service = new Service(createClient());
		
		// ここで対象テーブルとスループットを指定
		String tablePrefix = "dev-";
		long readCapacityUnits = 10L;
		long writeCapacityUnits = 5L;
		
		List<String> tableNames = service.getAllTableNames();
		int count = 0;
		for (String name :tableNames) {
			if (name.startsWith(tablePrefix)) {
				service.updateThroughput(name, readCapacityUnits, writeCapacityUnits);
				count++;
			}
		}
		
		System.out.printf("%d tables updated!%n", count);
	}

	/**
	 * 環境設定
	 */
	private static AmazonDynamoDBClient createClient() throws Exception {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Region tokyo = Region.getRegion(RESION);
		client.setRegion(tokyo);
		return client;
	}

}
