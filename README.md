dynamodb-tools
==============

DynamoDB の全テーブルのスループット下げたかったのでつくった

```java
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

}
```
