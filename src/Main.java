import java.util.List;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class Main {

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
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
		Region tokyo = Region.getRegion(Regions.AP_NORTHEAST_1);
		client.setRegion(tokyo);
		return client;
	}

}