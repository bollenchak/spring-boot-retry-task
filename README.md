## Spring Boot Custom Retry Task 自定义重试任务

### Diff with @Retry 区别于Retry注解

基于延时队列实现异步重试机制，提供 DelayQueue 的持久化，避免内存中的重试任务丢失。

### Example 例子

在某系统中，需要将客户分派给对应员工，分派过程中需要调用外部依赖的系统。调用过程中可能出现超时等异常，造成派工失败。因此，通过重试机制确保能在对方短暂停止服务时依然能派工成功。

通过 business 包中的 CustomerAssignService 实现 RetryService 接口的 addRetryTask 和 failedRetry 方法来实现上述目的。