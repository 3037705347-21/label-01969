# 测试指南

## 1. 概述

本文档说明如何运行宠物领养系统的测试用例。

## 2. 测试文件结构

```
backend/
├── src/
│   ├── main/          # 主源码
│   └── test/          # 测试源码
│       └── java/
│           └── com/petadopt/service/
│               ├── UserServiceTest.java       # 用户服务测试
│               ├── PetServiceTest.java        # 宠物服务测试
│               └── AdoptionServiceTest.java   # 领养服务测试
docs/
├── test_cases.md      # 测试用例文档
└── test_guide.md      # 本文档
```

## 3. 前置条件

- JDK 17+
- Maven 3.6+
- IDE（推荐 IntelliJ IDEA 或 Eclipse）

## 4. 运行测试

### ⚠️ 重要提示：推荐使用 IDE 运行测试

由于 Lombok 注解处理器的配置问题，**强烈建议使用 IntelliJ IDEA 或 Eclipse 等 IDE 来运行测试**，这是最可靠的方式。

### 4.1 在 IDE 中运行（推荐）

**IntelliJ IDEA:**
1. 用 IDEA 打开项目根目录
2. 等待 IDEA 索引完成（右下角有进度条）
3. 确保已安装 Lombok 插件（Settings → Plugins → 搜索 Lombok 并安装）
4. 启用注解处理（Settings → Build, Execution, Deployment → Compiler → Annotation Processors → 勾选 "Enable annotation processing"）
5. 右键点击测试类（如 `UserServiceTest.java`）或测试方法
6. 选择 "Run 'xxxTest'" 或 "Debug 'xxxTest'"

**Eclipse:**
1. 安装 Lombok 插件（下载 lombok.jar，运行 java -jar lombok.jar，选择 Eclipse 安装目录）
2. 重启 Eclipse
3. 右键点击测试类
4. 选择 "Run As" → "JUnit Test"

### 4.2 使用 Maven 命令行运行（备选）

如果需要使用命令行，先确保环境配置正确：

```bash
# 配置 Java 17 和 Maven 路径（macOS）
export PATH="/opt/homebrew/opt/openjdk@17/bin:/opt/homebrew/bin:$PATH"

# 验证版本
java -version
mvn -version

# 进入 backend 目录
cd backend

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 运行特定测试方法
mvn test -Dtest=UserServiceTest#testLogin_Success

# 生成测试报告
mvn surefire:report
```

## 5. 测试框架说明

### 5.1 技术栈

- **JUnit 5**：测试框架
- **Mockito**：Mock 框架，用于模拟依赖
- **AssertJ**：断言库（可选）

### 5.2 常用注解

| 注解 | 说明 |
|------|------|
| `@Test` | 标记测试方法 |
| `@DisplayName` | 自定义测试显示名称 |
| `@BeforeEach` | 每个测试方法前执行 |
| `@AfterEach` | 每个测试方法后执行 |
| `@BeforeAll` | 所有测试前执行一次（静态方法） |
| `@AfterAll` | 所有测试后执行一次（静态方法） |
| `@Mock` | 模拟依赖对象 |
| `@InjectMocks` | 注入 Mock 对象到被测试类 |
| `@ExtendWith(MockitoExtension.class)` | 启用 Mockito |

## 6. 测试覆盖范围

当前已实现的单元测试覆盖以下核心功能：

### 6.1 用户服务测试 (UserServiceTest)
- ✅ 用户登录成功
- ✅ 用户登录-用户不存在
- ✅ 用户登录-密码错误
- ✅ 用户登录-账号已禁用
- ✅ 用户登录-用户在黑名单
- ✅ 用户注册成功
- ✅ 用户注册-用户名已存在
- ✅ 用户注册-手机号已注册

### 6.2 宠物服务测试 (PetServiceTest)
- ✅ 救助方发布宠物成功
- ✅ 管理员发布宠物成功
- ✅ 领养人尝试发布宠物-权限不足
- ✅ 获取宠物详情成功
- ✅ 获取宠物详情-宠物不存在
- ✅ 救助方更新自己的宠物成功
- ✅ 更新宠物-宠物不存在
- ✅ 删除宠物-权限不足

### 6.3 领养服务测试 (AdoptionServiceTest)
- ✅ 领养人提交申请成功
- ✅ 提交申请-只有领养人可以申请
- ✅ 提交申请-宠物不存在
- ✅ 提交申请-宠物不可领养
- ✅ 提交申请-重复申请
- ✅ 救助方初审通过
- ✅ 救助方初审拒绝
- ✅ 救助方审核-无权审核

## 7. 扩展测试

### 7.1 添加新的测试类

1. 在 `backend/src/test/java/com/petadopt/service/` 下创建新的测试类
2. 使用 `@ExtendWith(MockitoExtension.class)` 注解
3. 使用 `@Mock` 模拟依赖，`@InjectMocks` 注入被测试类
4. 编写测试方法，使用 `@Test` 注解

示例：

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("我的服务测试")
class MyServiceTest {

    @Mock
    private SomeMapper someMapper;

    @InjectMocks
    private MyServiceImpl myService;

    @Test
    @DisplayName("测试方法")
    void testMyMethod() {
        // 准备数据
        when(someMapper.selectById(1L)).thenReturn(testEntity);

        // 执行测试
        var result = myService.myMethod(1L);

        // 验证结果
        assertNotNull(result);
        verify(someMapper, times(1)).selectById(1L);
    }
}
```

### 7.2 添加集成测试

对于需要数据库连接的集成测试，可以使用 `@SpringBootTest` 注解：

```java
@SpringBootTest
@Transactional
class PetServiceIntegrationTest {

    @Autowired
    private PetService petService;

    @Test
    @DisplayName("集成测试-创建宠物")
    void testCreatePet_Integration() {
        // 测试代码
    }
}
```

## 8. 测试报告

### 8.1 Maven Surefire 报告

运行测试后，报告生成在：
`backend/target/surefire-reports/`

### 8.2 JaCoCo 代码覆盖率（可选）

在 pom.xml 中添加 JaCoCo 插件：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

运行：
```bash
mvn clean test jacoco:report
```

覆盖率报告位置：
`backend/target/site/jacoco/index.html`

## 9. 常见问题

### 9.1 Mock 静态方法

使用 `MockedStatic`：

```java
try (MockedStatic<MyStaticClass> mocked = mockStatic(MyStaticClass.class)) {
    mocked.when(() -> MyStaticClass.staticMethod()).thenReturn("mocked");
    // 测试代码
}
```

### 9.2 测试私有方法

不建议直接测试私有方法，应通过测试公共方法间接测试。

### 9.3 如何提高测试覆盖率

- 为每个 Service 类编写对应的 Test 类
- 覆盖正常流程、异常流程、边界条件
- 使用 JaCoCo 查看覆盖率报告

## 10. 参考资料

- [JUnit 5 官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 官方文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
