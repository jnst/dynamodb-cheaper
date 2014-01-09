import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;

public class Service {

	private AmazonDynamoDBClient dynamoDB;

	public Service(AmazonDynamoDBClient dynamoDB) {
		this.dynamoDB = dynamoDB;
	}

	/**
	 * 全テーブル名を取得する
	 */
	public List<String> getAllTableNames() {
		List<String> list = new ArrayList<>();
		boolean hasNext = true;
		String lastEvaluatedTableName = null;
		
		while (hasNext) {
			ListTablesRequest listTablesRequest = new ListTablesRequest().withExclusiveStartTableName(lastEvaluatedTableName);
			ListTablesResult result = dynamoDB.listTables(listTablesRequest);
			list.addAll(result.getTableNames());
			
			lastEvaluatedTableName = result.getLastEvaluatedTableName();
			if (lastEvaluatedTableName == null) {
				hasNext = false;
			}
		}
		
		return list;
	}

	/**
	 * スループットを更新する
	 */
	public void updateThroughput(String tableName, long read, long write) {
		if (isNeedChange(tableName, read, write)) {
			ProvisionedThroughput throughput = new ProvisionedThroughput(read, write);
			UpdateTableRequest request = new UpdateTableRequest()
				.withTableName(tableName)
				.withProvisionedThroughput(throughput);
			try {
				TableDescription description = dynamoDB.updateTable(request).getTableDescription();
				waitActive(description);
				System.out.println(String.format("[%-35s] complete! read:%d,write:%d", tableName, read, write));
			} catch (Exception e) {
				System.out.println(String.format("[%-35s] skip=%s", tableName, "Error: " + e.getMessage()));
			}
		}
	}

	/**
	 * スループットの更新の必要有無を返す
	 */
	public boolean isNeedChange(String tableName, long read, long write) {
		DescribeTableRequest request = new DescribeTableRequest(tableName);
		TableDescription description = dynamoDB.describeTable(request).getTable();
		ProvisionedThroughputDescription throughput = description.getProvisionedThroughput();
		if ((read < throughput.getReadCapacityUnits()) || (write < throughput.getWriteCapacityUnits())) {
			return true;
		}
		
		System.out.printf("[%-35s] skip=AlreadyLower (read:%d, write:%d)%n", tableName, throughput.getReadCapacityUnits(), throughput.getWriteCapacityUnits());
		return false;
	}

	/**
	 * テーブルの更新が完了し、ステータスがACTIVEになるまで待機する
	 */
	public void waitActive(TableDescription description) {
		String tableName = description.getTableName();
		String status = description.getTableStatus();
		if (status.equals(TableStatus.ACTIVE)) {
			return;
		}
		
		System.out.printf("  status is %s...%n", status);
		
		while (true) {
			try {
				Thread.sleep(20000L);
			} catch (InterruptedException e) {
				// do nothing
			}
			
			if (isActive(tableName)) {
				return;
			}
		}
	}

	/**
	 * テーブルの更新中か否かを確認する
	 */
	public boolean isActive(String tableName) {
		DescribeTableRequest request = new DescribeTableRequest(tableName);
		TableDescription description = dynamoDB.describeTable(request).getTable();
		String status = description.getTableStatus();
		
		if (TableStatus.valueOf(status) == TableStatus.ACTIVE) {
			return true;
		}
		System.out.printf("  status is %s...%n", status);
		return false;
	}

}
