dynamodb-tools
==============

DynamoDB の全テーブルのスループット下げたかったのでつくった

### 使い方: Mainクラス修正して実行

```java
public class Main {

    private static final String ACCESS_KEY = "xxxxx"; //アクセスキー
    private static final String SECRET_KEY = "xxxxx"; //シークレットキー
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

}
```

### 主なメソッド1: テーブル名全取得

```java
public List<String> getAllTableNames() {
    List<String> list = new ArrayList<>();
    boolean hasNext = true;
    String lastEvaluatedTableName = null;
    
    while (hasNext) {
        ListTablesRequest request = new ListTablesRequest().withExclusiveStartTableName(lastEvaluatedTableName);
        ListTablesResult result = dynamoDB.listTables(request);
        list.addAll(result.getTableNames());
        
        lastEvaluatedTableName = result.getLastEvaluatedTableName();
        if (lastEvaluatedTableName == null) {
                hasNext = false;
        }
    }
    
    return list;
}
```
### 主なメソッド2: スループット更新

```java
public void updateThroughput(String tableName, long read, long write) {
    if (isNeedChange(tableName, read, write)) {
        ProvisionedThroughput throughput = new ProvisionedThroughput(read, write);
        UpdateTableRequest request = new UpdateTableRequest()
                .withTableName(tableName)
                .withProvisionedThroughput(throughput);
        try {
                TableDescription description = dynamoDB.updateTable(request).getTableDescription();
                waitActive(description);
                System.out.println(String.format("[%-35s] done! -> read:%d,write:%d", tableName, read, write));
        } catch (Exception e) {
                System.out.println(String.format("[%-35s] skip=%s", tableName, "Error: " + e.getMessage()));
        }
    }
}
```
