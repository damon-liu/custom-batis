## 一、JDBC概念

JDBC（Java Database Connectivity）是Java语言中提供的访问关系型数据的接口。在Java编写的应用中，使用JDBC API可以执行SQL语句、检索SQL执行结果以及将数据更改写回到底层数据源。

JDBC操作数据库代码，jdbc-demo项目下的测试目录下

```java
public class JdbcDemoTest {

    @Test
    public void test() throws Exception {
        //数据库连接地址
        String url = "jdbc:mysql://localhost:3306/sharding_jdbc?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "123456";
        // 1.加载数据库驱动
        Class.forName("com.mysql.jdbc.Driver");
        // 2.获取数据库连接对象Connection
        Connection connection = DriverManager.getConnection(url, user, password);
        // 3.创建sql语句对象statement，填写sql语句
        PreparedStatement preparedStatement = connection.prepareStatement("select * from t_user where name=?;");
        // 4.传入参数
        preparedStatement.setString(1, "刘备【1】");
        // 5.执行sql
        ResultSet resultSet = preparedStatement.executeQuery();
        // 6.遍历结果集
        List<User> users = new ArrayList<User>();
        while (resultSet.next()) {
            User u = User.builder().id(resultSet.getInt("id"))
                    .name(resultSet.getString("name"))
                    .age(resultSet.getInt("age"))
                    .address(resultSet.getString("address")).build();
            users.add(u);
        }
        System.out.println("users:  " + users);
        //7.关闭连接，释放资源
        resultSet.close();//关闭结果集对象
        preparedStatement.close();//关闭Sql语句对象
        connection.close();//关闭数据库连接对象
    }
}
```

执行结果

![image-20230326230213872](https://damon-study.oss-cn-shenzhen.aliyuncs.com/%20typora/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8Bimage-20230326230213872.png)

## 二、手写mybatis

### 2.1 项目原理

![image-20230326230438426](https://damon-study.oss-cn-shenzhen.aliyuncs.com/%20typora/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8Bimage-20230326230438426.png)

步骤：

1.  自定义框架准备工作，SQL配置文件及其封装类，数据库信息配置文件及其封装类
2.  编写SqlSession接口类和实现类：负责提供常用的数据库操作API接口
3.  编写SqlSessionFactory类：负责创建SqlSession对象的实现类
4. 编写SqlSessionFactoryBuilder类-解析核心配置文件
5.  编写SqlSessionFactoryBuilder类-解析Sql配置文件
6.  安装自定义框架到Maven仓库

### 2.2 代码实现

#### 2.2.1 导入坐标

```xml
    <dependencies>
        <!--dom for java 读取xml文件-->
        <dependency>
            <groupId>dom4j</groupId>
            <artifactId>dom4j</artifactId>
            <version>1.6.1</version>
        </dependency>
        <!--xpath表达式，快速定位xml文件元素-->
        <dependency>
            <groupId>jaxen</groupId>
            <artifactId>jaxen</artifactId>
            <version>1.1.6</version>
        </dependency>
    </dependencies>
```

#### 2.2.2 配置文件

数据库配置信息，及Sql配置文件

##### SqlMapConfig.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <environments>
        <environment>
            <dataSource>
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/sharding_jdbc?serverTimezone=Asia/Shanghai" />
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="UserMapper.xml"/>
    </mappers>
</configuration>
```

##### UserMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<mapper namespace="com.lhx.frame.dao.UserMapper">
    <select id="findAll" resultType="com.lhx.frame.pojo.User">
        select * from t_user;
    </select>
</mapper>

```

### 2.2.3 Configuration

将数据库配置文件和Sql配置文件封到类中

```java
public class Configuration {
    private String driver;
    private String url;
    private String username;
    private String password;
    private Map<String, SqlSource> sqlSourceMap = new HashMap<>();
    //...省略get set方法
}
```

```java
public class SqlSource {
    private String sql;
    private String resultType;
    //...省略get set方法
}
```

### 2.2.4 SqlSession接口及其实现类

```java
public interface SqlSession {
    /**
     * 查询所有用户
     * T 代表泛型类型，T(type缩写)
     */
    <T> List<T> selectList(String sql) throws Exception;
}

public class SqlSessionImpl implements SqlSession{

    private Configuration configuration;

    public SqlSessionImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> List<T> selectList(String sql) throws Exception {
        Executor executor = new Executor(configuration);
        return executor.executeQuery(sql);
    }
}
```

### 2.2.5 编写Executor类

抽取JDBC公用代码到工具类

```java
public class Executor {

    private Configuration configuration;

    public Executor(Configuration configuration) {
        this.configuration = configuration;
    }

    public List executeQuery(String statement) throws Exception {
        /**
         * 数据库连接信息硬编码
         */
        String driver = configuration.getDriver();
        String url = configuration.getUrl();
        String username = configuration.getUsername();
        String password = configuration.getPassword();
        Map<String, SqlSource> map = configuration.getSqlSourceMap();
        // 获取sql信息
        SqlSource mapper = map.get(statement);
        String sqlStr = mapper.getSql();
        String resultType = mapper.getResultType();

        //1.注册MySQL驱动
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, username, password);
        //3.创建SQL语句对象Statement，填写SQL语句
        PreparedStatement ps = conn.prepareStatement(sqlStr);
        //4.执行查询SQL，返回结果集ResultSet
        ResultSet rs = ps.executeQuery();
        //5.解析结果集，获取查询用户list集合
        //获取结果集元数据
        ResultSetMetaData metaData = rs.getMetaData();
        //获取总列数5
        int columnCount = metaData.getColumnCount();
        //获取所有列的list集合
        List<String> columnNames = new ArrayList<String>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }
        List list = new ArrayList();
        while (rs.next()) {
            //在代码运行的时候，为创建指定类的对象，并且可以调用对象的方法，而且无视权限修饰符！
            //在汽车奔跑的时候，为汽车更换轮子！
            //通过反射获取类的字节码对象，传入的参数是类的全限定名称
            Class<?> clazz = Class.forName(resultType);
            //反射创建对象
            Object user = clazz.newInstance();
            //反射获取当前类的所有方法
            Method[] methods = clazz.getMethods();
            //循环 遍历所有列名
            for (String columnName : columnNames) {
                for (Method method : methods) {
                    String methodName = method.getName();
                    //判断方法的名称，与set+列名相等，那么就把列名对应的值设置到当前对象的set方法中
                    if (("set"+columnName).equalsIgnoreCase(methodName)){
                        //把列名column对应的值,设置到对象的set方法中,给属性赋值
                        method.invoke(user,rs.getObject(columnName));
                    }
                }
            }
            //将用户存入集合中
            list.add(user);
        }
        //关闭连接，释放资源
        rs.close();
        ps.close();
        conn.close();
        return list;
    }
}
```

### 2.2.6 编写SqlSessionFactory

SqlSession的工厂类，负责创建SqlSession接口实现类

```java
/**
 * Description:SqlSession工厂类，负责创建SqlSession接口实现类
 *
 * @author damon.liu
 * Date 2022-12-06 4:15
 */
public class SqlSessionFactory {

    private Configuration configuration;

    public SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 创建SqlSession会话
     */
    public SqlSession openSession(){
        return new SqlSessionImpl(configuration);
    }
}
```

### 2.2.7 编写SqlSessionFactoryBuilder

解析框架使用者传入的配置文件（SqlMapConfig.xml、UserMapper.xml），

```java
/**
 * Description:负责创建SqlSessionFactory
 *
 * @author damon.liu
 * Date 2022-12-06 6:17
 */
public class SqlSessionFactoryBuilder {

    /**
     * 构建工厂对象
     * 参数：SqlMapConfig.xml配置文件的输入流对象
     */
    public SqlSessionFactory build(InputStream inputStream) throws DocumentException {
        Configuration configuration = new Configuration();
        //解析配置文件
        loadXmlConfig(configuration,inputStream);
        return new SqlSessionFactory(configuration);
    }


    /**
     * 解析框架使用者传入的配置文件
     */
    private void loadXmlConfig(Configuration configuration,InputStream inputStream) throws DocumentException {
        //创建解析XML文件对象SAXReader
        SAXReader saxReader = new SAXReader();
        //读取SqlMapConfig.xml配置文件流资源，获取文档对象
        Document document = saxReader.read(inputStream);
        //获取SqlMapConfig.xml 配置文件内所有property标签元素
        List<Element> selectNodes = document.selectNodes("//property");
        //循环解析property标签内容，抽取配置信息
        for (Element element : selectNodes) {
            String name = element.attributeValue("name");
            if ("driver".equals(name)){//数据库驱动
                configuration.setDriver(element.attributeValue("value"));
            } else if ("url".equals(name)){//数据库地址
                configuration.setUrl(element.attributeValue("value"));
            } else if ("username".equals(name)){//用户名
                configuration.setUsername(element.attributeValue("value"));
            } else if ("password".equals(name)){//密码
                configuration.setPassword(element.attributeValue("value"));
            }
        }
        //解析SqlMapConfig.xml 映射器配置信息
        List<Element> list = document.selectNodes("//mapper");
        for (Element element : list) {
            //SQL映射配置文件路径
            String resource = element.attributeValue("resource");
            //解析SQL映射配置文件
            loadSqlConfig(resource,configuration);
        }
    }

    /**
     * 解析SQL配置文件
     */
    private void loadSqlConfig(String resource, Configuration configuration) throws DocumentException {
        //根据SQL映射配置文件路径，读取流资源。classpath路径下
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resource);
        //创建解析XML文件的对象SAXReader
        SAXReader saxReader = new SAXReader();
        //读取UserMapper.xml配置文件文档对象
        Document document = saxReader.read(inputStream);
        //获取文档对象根节点：<mapper namespace="test">
        Element rootElement = document.getRootElement();
        //取出根节点的命名空间
        String namespace = rootElement.attributeValue("namespace");
        //获取当前SQL映射文件所有查询语句标签
        List<Element> selectNodes = document.selectNodes("//select");
        //循环解析查询标签select，抽取SQL语句
        for (Element element : selectNodes) {
            //查询语句唯一标识
            String id = element.attributeValue("id");
            //当前查询语句返回结果集对象类型
            String resultType = element.attributeValue("resultType");
            //查询语句
            String sql = element.getText();
            //创建Mapper对象
            SqlSource mapper = new SqlSource();
            mapper.setSql(sql);
            mapper.setResultType(resultType);
            //在configuration中设置mapper类，key：(命名空间+.+SQL语句唯一标识符)
            configuration.getSqlSourceMap().put(namespace+"."+id,mapper);
        }
    }
}
```

### 2.2.8 测试代码

执行damonbatis-frame-demo项目下的测试类CustomFrameTest的test()

```java
    @Test
    public void test() throws Exception {
        //4.执行查询Sql语句
        List<User> users = sqlSession.selectList("com.lhx.frame.dao.UserMapper.findAll");
        //5.循环打印
        for (User u : users) {
            System.out.println(u);
        }
    }
```

### 2.2.9 执行结果

![image-20230326233855281](https://damon-study.oss-cn-shenzhen.aliyuncs.com/%20typora/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8Bimage-20230326233855281.png)

