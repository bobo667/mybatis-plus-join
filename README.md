# mybatis-plus-join

mybatis-plus-join是mybatis plus的一个多表插件，上手简单，十分钟不到就能学会全部使用方式，只要会用mp就会用这个插件，仅仅依赖了lombok，而且是扩展mp的构造器并非更改原本的构造器，不会对原有项目产生一点点影响，相信大多数项目都有这插件，四舍五入就是没依赖。

mybatis-plus-join示例：

> gitee: https://gitee.com/mhb0409/mybatis-plus-join-example
> github: https://github.com/bobo667/mybatis-plus-join-example



### 关于该插件的一点问题

1. 出现了bug怎么办，不是mybatis plus官方的会不会不稳定啊？ 这个大可以放心，这个插件我已经在生产环境跑了一年多了，已经有许多开发者再用，没出过什么问题，如果遇到问题可以在 Issues 上提出，我看见就会解决，上午提的，不忙的话下午就能打包新版本，忙的话大概就需要晚上就差不多了
2. 关于维护到啥时候？mybatis plus不倒我不倒（当然，如果长期没有star，哪怕是我得先倒了，还是那，您的star就是作者更新的动力，手动ღ( ´･ᴗ･` )比心）
3. 有什么有想法的新功能啊，或者改善啊，可以在Issues 上提出
4. 如果想联系作者，可以在wx上搜索小程序 <u>马汇博的博客</u>在关于我中有微信号，欢迎来扰

如果需要加群，请加我微信我拉您进群

<img src="https://www.mhba.work/upload/2022/12/tmp_9c854beed43b4f9eaf4984f42eefa027-2fe7c9b96b9b451db7317ee7bac9c0e5.jpg" alt="tmp_9c854beed43b4f9eaf4984f42eefa027" style="zoom:25%;" />



**目前支持大部分mp常用版本**

maven坐标

mybatis plus：3.2.0版本依赖地址：

```xml
 <dependency>
   <groupId>icu.mhb</groupId>
   <artifactId>mybatis-plus-join</artifactId>
   <version>1.2.0</version>
</dependency>
```

最新版本依赖地址：

```xml
 <dependency>
   <groupId>icu.mhb</groupId>
   <artifactId>mybatis-plus-join</artifactId>
   <version>1.3.4</version>
</dependency>
```



## 版本对应关系（此处只显示对应的最新版本）

> 标注：*号代表，从起始版本之后都是可以使用的

| Mybatis-plus    | Mybatis-plus-join                                            |
| --------------- | ------------------------------------------------------------ |
| 3.2.0           | 1.2.0                                                        |
| 3.3.1 - 3.42    | 1.0.2、1.3.4.1                                               |
| 3.4.3.4 - 3.5.2 | 1.0.3 、1.0.4、1.0.5、1.0.6、1.0.8、1.0.9、1.1.1、1.1.2、1.1.3、1.1.4、1.1.5、1.1.6、1.3.1、1.3.2、1.3.3 |
| 3.5.3 - *       | 1.3.3.1、1.3.4                                               |



## 版本日志

### 1.0.1 版本

1.初始化项目 mybatis-plus-join项目诞生

### 1.0.2 版本

1.优化了selectAs()方法，支持函数简洁式写法

2.增加了缓存优化性能

### 1.0.3 版本

1.支持3.4.3.4版本

2.增加根据传入实体不为空的数据查询

3.优化了代码逻辑

4.增加notDefaultSelectAll() 不默认查询主表全部的字段



### 1.0.4 版本

1.支持查询单个参数时候返回单个参数，例如List<String> String

2.优化转换类型的方式



### 1.0.5 版本

1.修复在没有条件下order 排序失效的问题



### 1.0.6 版本

1.修复实体条件为主键ID的时候没有加别名问题

2.增加返回值支持一对一查询



### 1.0.8 版本

1. 增加了多对多映射

2. 去掉了fastJSON依赖

3. 更改serviceImpl动态返回类型的处理方式，采用更优的插件式注入方式



这次终于去掉了总是说的fastJSON依赖，现在采用动态注入resultMap方式，来构建普通多表，一对一，多对多查询，采用插件式懒加载 + 缓存机制，启动时间无影响，使用加载一下就可以直接从缓存调用，保证不影响使用中的效率。

### 1.0.9 版本

1. 更改默认表、字段别名关键字As 为 空格

2. 增加自定义表、字段别名关键字，在不同数据库中兼容



### 1.1.1 版本

1. 修复在添加逻辑删除的时候SQL报错问题

2. 返回类型支持Map类型

3. 增加主表和子表自定义别名，方便多个相同子表对应一个主表



这次更新解决了目前使用的一些特殊场景下的缺陷问题，使用的更灵活了

### 1.1.2 版本

1. 修复逻辑删除没有加别名的问题

2. 修复多个连表情况下，只查询主表的逻辑删除的问题

3. 修复在定义typeHandler不生效的问题



这次更新主要是修复的bug版本，目前作者没有什么特别多的思路去要写什么样的新功能，如果各位有可以提出来

### 1.1.3 版本

1. 更改逻辑删除出现的条件为join后，而不是where后

2. Merge pull request !2 from f_ms/N/A

3. Merge pull request !1 from f_ms/N/A



这次更新主要是修复的bug版本，目前作者没有什么特别多的思路去要写什么样的新功能，如果各位有可以提出来

### 1.1.4 版本

1. 修复逻辑删除值错误的bug  **gitee issues-I5UY2K**
2. typeHandler 增加子表支持  **gitee issues-I5SUV6**
3. 修复 在调用 and()方法的情况下，设置的表别名失效的问题
4. orderBy 排序增加顺序下标，可根据下标来调整对应的排序顺序
5. 增加 orderBySql 方法，可以手写排序SQL
6. selectAs 自定义查询字段方法增加重载参数 boolean isQuotes，来标识是否需要是字符，可以用这种方式写一些简单的函数
7. 增加distinct函数方法
8. 优化了代码结构

### 1.1.5 版本

1. MybatisPlusJoinConfig增加isUseMsCache方法，代表使用不使用MappedStatement的缓存，如果为true，就会更改他的id如果是使用mate的某些插件特效出现classNotFoud，因为更改了MappedStatement Id报错，可以尝试把这个改成false，就不会更改id内容
2. 增加方法JoinLambdaWrapper#changeQueryWrapper 转换查询条件

### 1.1.6版本

1. 修复一对一，多对多的情况下主表和字表字段名字重复，出现赋值错误的情况
2. 修复getTableFieldInfoByFieldName 获取不到tableInfo的情况下会报错的问题

### 1.3.1版本

1. 单纯的升级个版本跨过1.2.0

### 1.3.2版本

1. selectAs(cb -> {cb.add()})方式，自动增加表前缀，在一对一，多对多的场景下，相同字段名可以无需写别名即可映射

2. 修复gitee /issues/I64AZ0 分页参数传递的问题

3. 增加selectAs(SFunction<T, ?> column, SFunction<J, ?> alias) 和  selectAs#addFunAlias 支持用函数来表示别名

   （不建议使用，建议使用最新版）

### 1.3.2.1版本

该版本主要是修复1.3.2的版本bug

1.  selectAs(SFunction<T, ?> column, SFunction<J, ?> alias) 和  selectAs#addFunAlias 方法，生成的别名和属性名没有对应报错的bug

### 1.3.3 版本(重大更新)

1.  增加@JoinField 注解，定义映射关系，可在wrapper中用push**Join方法加入，即可查询并映射
2.  条件构造器增加基础join查询四件套可以用wrapper直接进行查询
3.  manyToManySelect和oneToOneSelect 增加可以不指定查询列，查询全部列并映射
4.  修复 JoinLambdaWrapper 和 JoinWrapper的  select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) 没有加别名的问题修复
5.  增加接口 JoinCompareFun，eq、ne..可以传入别的表的函数，实现两个表字段关联

### 1.3.3.1 版本

1.  支持了mybatis-plus 3.5.3版本往后

### 1.3.4 版本

1.  主表可使用masterLogicDelete方法配置是否开启逻辑删除
2.  查询字段增加子查询 selectSunQuery方法
3.  joinAnd 增加条件构造器，可以自由构造多条件join and
4.  项目异常更改为mpj异常
5.  合并 pr https://gitee.com/mhb0409/mybatis-plus-join/pulls/3

### 1.3.4.1 版本（兼容老版本）

1.  兼容 3.3.2 - 3.4.2 mp版本



### 其他版本

#### 1.2.0 版本

1.支持了3.2.0 版本



废话不多说，直接看怎么使用

```java
   
    /**
     * 查询列表
     *
     * @param wrapper 实体对象封装操作类
     * @param <E>     返回泛型（如果只查询一个字段可以传递String Int之类的类型）
     * @return 返回E 类型的列表
     */
    <EV, E> List<EV> joinList(Wrapper<E> wrapper, Class<EV> clz);

    /**
     * 查询单个对象
     *
     * @param wrapper 实体对象封装操作类
     * @param clz     返回对象 （如果只查询一个字段可以传递String Int之类的类型）
     * @param <E>     包装泛型类型
     * @param <EV>    返回类型泛型
     * @return EV
     */
    <E, EV> EV joinGetOne(Wrapper<E> wrapper, Class<EV> clz);


    /**
     * 查询count
     *
     * @param wrapper 实体对象封装操作类
     * @param <E>     返回泛型
     * @return 总数
     */
    <E> int joinCount(Wrapper<E> wrapper);


    /**
     * 翻页查询
     *
     * @param page    翻页对象
     * @param wrapper 实体对象封装操作类
     */
    <EV, E extends IPage<EV>, C> IPage<EV> joinPage(E page, Wrapper<C> wrapper, Class<EV> clz);


```

一共是四个方法，分别重写于mp的

`joinList -> list `

`joinGetOne -> getOne`

`joinCount -> count`

`joinPage -> page`

**注意：这几个方法，前面俩参数和mp的用法一致，最后一个class类型的是返回类型，这个主要是大多数多表操作都是需要有额外字段，所以需要额外定义，而Wrapper<E> wrapper中的这个需要填写在需要构建条件的实体，这个实体是任意的，不强制，创建条件构造器的时候定义的那个对象就是主表**



## 基本使用方法

1.mapper继承 JoinBaseMapper< T>

2.service继承 JoinIService< T>

3.impl 继承 JoinServiceImpl<M,T>

4.注入mp自定义方法，主要是继承JoinDefaultSqlInjector （因为版本更新，这里的参数可能有所变化，改一下传递一下就行了）

```java
package icu.mhb.mpj.example.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import icu.mhb.mybatisplus.plugln.injector.JoinDefaultSqlInjector;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MyBatisPlusConfig extends JoinDefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        // 自己的自定义方法
        return methodList;
    }

}

```

然后就可以愉快的使用了

## 自定义查询字段和表别名关键字

```java
// 为何要这个东西，因为在不同数据库之间，别名关键字不一样，例如Mysql表别名是 As 而oracle中 是 is 关键字所以需要

// 以oracle 关键字为例
  @Bean
    public MybatisPlusJoinConfig mybatisPlusJoinConfig() {
        return MybatisPlusJoinConfig.builder()
                // 查询字段别名关键字
                .columnAliasKeyword("is")
                // 表、left join、right join、inner join 表别名关键字
                .tableAliasKeyword("is")
                .build();
    }

// 运行的SQL
SELECT 1 is id
 FROM users is users
 LEFT JOIN users_age is users_age
 ON users_age.id = users.age_id

```



## 自定义是否使用MappedStatement缓存（如果有出现classNotFoud情况，可以尝试关闭）

```java
// 为何要这个东西，因为在不同数据库之间，别名关键字不一样，例如Mysql表别名是 As 而oracle中 是 is 关键字所以需要

  @Bean
    public MybatisPlusJoinConfig mybatisPlusJoinConfig() {
        return MybatisPlusJoinConfig.builder()
                /*
                  是否使用MappedStatement缓存，如果使用在JoinInterceptor中就会更改
                  MappedStatement的id，导致mybatis-plus-mate 的某些拦截器插件报错，
                  设置成false，代表不使用缓存则不会更改MappedStatement的id
                 */
                .isUseMsCache(false)
                .build();
    }

```



## 自定义函数关键字例如Distinct

```java
// 为何要这个东西，可能有些数据库关键字不一样，他有默认实现，一般情况下不需要传，除非你的数据库真是关键字不一样

// 第一步需要建个类，实现IFuncKeyWord
public class FuncKeyWordImpl implements IFuncKeyWord {
    @Override
    public String distinct() {
        return "distinct";
    }
}

// 第二部在构造器中set进去
JoinLambdaWrapper<Users> wrapper = joinLambdaQueryWrapper(Users.class)
                .setFuncKeyWord(new FuncKeyWordImpl())
  
// 后续会改进为增加全局注入，但是这种方式依旧会保留，避免你多数据源情况下两个数据库查询的关键字都不相同

```



下面来看构造器的使用：

```java
// 第一步new 一个JoinLambdaWrapper构造参数是主表的实体对象（如果在service中直接使用joinLambdaWrapper()方法即可获得）
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);

// 第二步 使用leftJoin方法创建一个左连接
/*
	有三个方法可以使用 
	leftJoin 左联
	rightJoin 右联
	innerJoin 内联
*/

// 这一部分一个参数是join中定义的连接的表，第二个参数是随意的表，但是是要出现构造器中的
wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId);
// 然后可以设置多表中的查询条件，这一步和mp一致
wrapper.eq(UserAge::getAgeName,"95")
  		.select(UserAge::getAgeName)
      // 最后一步 需要使用end方法结束
      .end();


// 完整的就是
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
  	.eq(UserAge::getAgeName,"95")
  	.select(UserAge::getAgeName)
  	.end();

usersService.joinList(wrapper,UsersVo.class);

// 或者如果你的类只有mapper继承了，那你可以
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
  	.eq(UserAge::getAgeName,"95")
  	.select(UserAge::getAgeName)
  	.joinList(UsersVo.class);

// 执行SQL 
select 
  users.user_id,
  users.user_name,
  users_age.age_name
from users users
  left join users_age users_age on users_age.id = users.age_id
where (
	users_age.age_name = '95'
)

```

是不是就很简单，就和mp的原生的比，就是增加了 join方法啥的

## 加料用法

OK，来点丝滑的加料用法

### 使用selectSunQuery构建子查询（1.3.4版本之后）

```java
// joinList
List<UsersVo> list = Joins.of(Users.class)
                .masterLogicDelete(false)
                .pushLeftJoin(UsersVo::getUsersAge, UsersAge.class)
                .selectSunQuery(UsersAge.class, w -> {
                    w.eq(UsersAge::getId, Users::getAgeId)
                            .eq(UsersAge::getId, 1)
                            .le(UsersAge::getCreateTime, new Date())
                      			// 需要注意的是这个查询字段只能有一个
                            .selectAs(cb -> {
//                                cb.add("count(1)", "counts", false);
                                cb.add(UsersAge::getId, "counts");
                              // 这里的话，他的关联表是需要在之前出现的，这个selectSunQuery 在主表也是一样字表也是一样的，但是需要放在后面，因为如果在前面可能关联表的别名被重写定义了，那么他就会出现SQL错误
                            }).leftJoin(Users.class, Users::getAgeId, UsersAge::getId, w2 -> {
                                w2.eq(Users::getUserId, 1);
                            });
                })
                .joinAnd(0, w -> w.eq(UsersAge::getId, Users::getAgeId)
                                            .ne(UsersAge::getId, 10))
                .isNotNull(UsersAge::getId).end().joinList(UsersVo.class);

 // 生成SQL
SELECT
	users.user_name,
	users.create_time,
	users.age_id,
	users.content_json,
	users.user_id,
	t1.age_doc AS t1_ageDoc,
	t1.age_name AS t1_ageName,
	t1.create_time AS t1_createTime,
	t1.content_json_age AS t1_contentJsonAge,
	t1.id AS t1_id,
	(
	SELECT
		t1.id AS counts 
	FROM
		users_age t1
		LEFT JOIN users AS users ON users.age_id = t1.id 
		AND users.age_id = 0 
	WHERE
		( users.user_id = 1 ) 
		AND ( t1.id = users.age_id AND t1.id = 1 AND t1.create_time <= '2023-01-20 16:11:14.38' ) 
	) AS counts 
FROM
	users AS users
	LEFT JOIN users_age AS t1 ON t1.id = users.age_id 
	AND ( t1.id = users.age_id AND t1.id <> 10 ) 
WHERE
	( t1.id IS NOT NULL );
    
```





### 使用构建器调用join*查询方法（1.3.3版本之后）

```java
// joinList
List<UsersVo> list = Joins.of(Users.class)
                .leftJoin(UsersAge.class,UsersAge::getAgeId,Users::getAgeId)
  							.eq(UsersAge::getAgeName,1).end()
                .joinList(UsersVo.class);

 // 生成SQL
SELECT
	users.user_name,
	users.create_time,
	users.age_id,
	users.content_json,
	users.user_id
FROM
	users AS users
	LEFT JOIN users_age AS users_age ON users_age.id = users.age_id 
WHERE
	users_age.age_name = '1'
    
 // joinGetOne
UsersVo userVo = Joins.of(Users.class)
                .leftJoin(UsersAge.class,UsersAge::getAgeId,Users::getAgeId)
  							.eq(UsersAge::getAgeName,1).end()
                .joinGetOne(UsersVo.class);

 // 生成SQL
SELECT
	users.user_name,
	users.create_time,
	users.age_id,
	users.content_json,
	users.user_id
FROM
	users AS users
	LEFT JOIN users_age AS users_age ON users_age.id = users.age_id 
WHERE
	users_age.age_name = '1'
    
  // joinCount
UsersVo userVo = Joins.of(Users.class)
                .leftJoin(UsersAge.class,UsersAge::getAgeId,Users::getAgeId)
  							.eq(UsersAge::getAgeName,1).end()
                .joinCount();

 // 生成SQL
SELECT
	count(*)
FROM
	users AS users
	LEFT JOIN users_age AS users_age ON users_age.id = users.age_id 
WHERE
	users_age.age_name = '1'
    
 // joinCount
Page<UsersVo> pageResult = Joins.of(Users.class)
                .leftJoin(UsersAge.class,UsersAge::getAgeId,Users::getAgeId)
  							.eq(UsersAge::getAgeName,1).end()
                .joinPage(page,UsersVo.class);

 // 生成SQL
SELECT
	users.user_name,
	users.create_time,
	users.age_id,
	users.content_json,
	users.user_id
FROM
	users AS users
	LEFT JOIN users_age AS users_age ON users_age.id = users.age_id 
WHERE
	users_age.age_name = '1'
limit 10
    
```





### eq、ne..等两个表字段关联（1.3.3版本之后）

```java
List<UsersVo> list = Joins.of(Users.class)
                .pushLeftJoin(UsersVo::getUsersAge)
                .eq(Users::getAgeId, UsersAge::getId)
                .le(Users::getAgeId, UsersAge::getId)
                .lt(Users::getAgeId, UsersAge::getId)
                .ge(Users::getAgeId, UsersAge::getId)
                .gt(Users::getAgeId, UsersAge::getId)
                .ne(Users::getAgeId, UsersAge::getId)
                .between(Users::getAgeId, UsersAge::getId, UsersAge::getAgeName)
                .notBetween(Users::getAgeId, UsersAge::getId, UsersAge::getAgeName)
                .joinList(UsersVo.class);


 // 生成SQL
SELECT
	users.user_name,
	users.create_time,
	users.age_id,
	users.content_json,
	users.user_id,
	t2.age_doc AS t2_ageDoc,
	t2.age_name AS t2_ageName,
	t2.create_time AS t2_createTime,
	t2.content_json_age AS t2_contentJsonAge,
	t2.id AS t2_id 
FROM
	users AS users
	LEFT JOIN users_age AS t2 ON t2.id = users.age_id 
WHERE
	(
		users.age_id = t2.id 
		AND users.age_id <= t2.id 
		AND users.age_id < t2.id 
		AND users.age_id >= t2.id 
		AND users.age_id > t2.id 
		AND users.age_id <> t2.id 
		AND users.age_id BETWEEN t2.id 
		AND t2.age_name 
	AND users.age_id NOT BETWEEN t2.id 
	AND t2.age_name)
```





### push*Join 和 @JoinField 方便构建join查询方法（1.3.3版本之后）

```java
// 第一步 在查询返回的模型中添加对象或者集合
// 注解参数
  // 主表对象class   
  masterModelClass();
  // 子表对象class
  sunModelClass();
  // 主表关联字段，注意不要写别名啥的，就写实体类中的属性名
  masterModelField();
  // 子表关联字段，注意不要写别名啥的，就写实体类中的属性名
  sunModelField();
  // 子表别名,如果你关联的对象中有两个相同的表，就需要显示填写一下别名，否则不用写
  sunAlias() default "";
  // 关联类型
  relevancyType();


@JoinField(masterModelClass = Users.class, masterModelField = "ageId",
            sunModelClass = UsersAge.class, sunModelField = "id", relevancyType = RelevancyType.ONT_TO_ONE,
            sunAlias = "t1")
private UsersAge usersAge;

@JoinField(masterModelClass = Users.class, masterModelField = "ageId",
            sunModelClass = UsersAge.class, sunModelField = "id", relevancyType = RelevancyType.MANY_TO_MANY,
            sunAlias = "t2")
private List<UsersAge> usersAges;

// 添加完注解之后，就可以用push*Join方法，添加进去
 pushLeftJoin(UsersVo::getUsersAge) 
 
 List<UsersVo> list = Joins.of(Users.class)
                .pushLeftJoin(UsersVo::getUsersAge)
                .joinList(UsersVo.class);
// 如果你添加完这个join之后，还需要再添加条件之类的
 List<UsersVo> list = Joins.of(Users.class)
   							// 就需要用该参数，指定一下构建的泛型class
                .pushLeftJoin(UsersVo::getUsersAge, UsersAge.class)
   							.eq(UsersAge::getId, Users::getAgeId).end()
                .joinList(UsersVo.class);
// 执行SQL
	SELECT 
  users.user_name,users.create_time,users.age_id,users.content_json,users.user_id, t1.age_doc as t1_ageDoc , t1.age_name as t1_ageName , t1.create_time as t1_createTime , t1.content_json_age as t1_contentJsonAge , t1.id as t1_id 
  FROM users as users 
  LEFT JOIN users_age as t1 ON t1.id = users.age_id
 
// 返回对象
UsersVo(....
        usersAge=UsersAge(id=1, ageDoc=90, ageName=90, createTime=Fri Dec 17 13:11:11 CST 2021, contentJsonAge=TestUserJson(name=456, content=呜呜呜)))
 
```



### changeQueryWrapper 转换条件构造器

```java
//  转换查询Wrapper 会把 查询条件，group，order by，having转换
//  注意该方法无法给 多个入参添加别名，例如 orderByDesc("id","id2") 这种情况下别名就会添加错误
 QueryWrapper<Users> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("user_id", 1)
                .and(w -> {
                    w.like("user_id", 2).or()
                            .le("user_id", 34);
                })
                .orderByDesc("user_id")
                .groupBy("user_id")
                .having("1={0}", 1);
 wrapper.changeQueryWrapper(wrapper1);

// SQL
SELECT users.create_time, users.content_json, users.user_id, u_age.age_doc, u_age.age_name, u_age.id, u_age.content_json_age, u_age.id AS ageTableId, '1' AS mpnb, sum(u_age.id) AS ageIds
 FROM users AS users
 LEFT JOIN users_age AS u_age
 ON u_age.id = users.age_id
WHERE (users.user_id = 1 AND (user_id LIKE '%2%' OR user_id <= 34)) GROUP BY users.user_id HAVING 1 = 1 ORDER BY users.user_id DESC;
```



### orderBy 顺序排列

```java
// 根据index下标进行排列排序顺序 
// @param condition 是否执行 @param isAsc     是否正序
// @param index     下标 @param column    列
orderBy(boolean condition, boolean isAsc, R column, int index) 

// 手写排序SQL @param condition 是否执行  @param sql      SQL
orderBySql(boolean condition, String sql, int index);

JoinLambdaWrapper<Users> wrapper = joinLambdaQueryWrapper(Users.class)
                .orderByDesc(Users::getAgeId) // 如果存在有下标的排序和无下标的排序，无下标的排序，会被存在于最前面
                .leftJoin(UsersAge.class, UsersAge::getId, Users::getAgeId)
                .orderByAsc(UsersAge::getId, 2)
                .orderBySql("users.user_id asc", 0) // 可以手写排序SQL，处理一些复杂的操作，这个orderBySql字表和主表中都可以存在
                .end()
                .orderBySql("users_age.age_name desc", 1);
 return super.joinList(wrapper, UsersVo.class);

// SQL
SELECT users.content_json, users_age.content_json_age as contentJsonAge
FROM users as users
LEFT JOIN users_age as users_age ON users_age.id = users.age_id 
ORDER BY users.age_id DESC,users.user_id asc,users_age.age_name desc,users_age.id ASC;
```



### distinct 去重

```java
// 根据index下标进行排列排序顺序 
// @param condition 是否执行 @param isAsc     是否正序
// @param index     下标 @param column    列
orderBy(boolean condition, boolean isAsc, R column, int index) 

// 手写排序SQL @param condition 是否执行  @param sql      SQL
orderBySql(boolean condition, String sql, int index);

JoinLambdaWrapper<Users> wrapper = joinLambdaQueryWrapper(Users.class)
  							.distinct() // 这个只能存在于主表
                .orderByDesc(Users::getAgeId) // 如果存在有下标的排序和无下标的排序，无下标的排序，会被存在于最前面
                .leftJoin(UsersAge.class, UsersAge::getId, Users::getAgeId)
                .orderByAsc(UsersAge::getId, 2)
                .orderBySql("users.user_id asc", 0) // 可以手写排序SQL，处理一些复杂的操作，这个orderBySql字表和主表中都可以存在
                .end()
                .orderBySql("users_age.age_name desc", 1);
 return super.joinList(wrapper, UsersVo.class);

// SQL
SELECT DISTINCT users.content_json, users_age.content_json_age as contentJsonAge
FROM users as users
LEFT JOIN users_age as users_age ON users_age.id = users.age_id 
ORDER BY users.age_id DESC,users.user_id asc,users_age.age_name desc,users_age.id ASC;
```



### 自定义别名和返回map类型

```java
// 两个参数代表自定义别名
JoinLambdaWrapper<Users> wrapper = joinLambdaQueryWrapper(Users.class, "userMaster");

wrapper
       .select(Users::getUserId, Users::getUserName)
  		// leftJoin innerJoin rightJoin 三个参数代表使用默认别名，四个参数代表使用自定义别名
       .leftJoin(UsersAge.class, UsersAge::getId, Users::getAgeId, "u_age")
       .select(UsersAge::getAgeDoc).end()
       .leftJoin(UsersAge.class, UsersAge::getId, Users::getAgeId, "u_a")
       .select(UsersAge::getAgeName).end();

// 需要注意的是当返回参数为map的时候是没有下划线转驼峰的，如果需要请自行配置mybatis的下划线转驼峰
List<Map> dataList = super.joinList(wrapper, Map.class);

// SQL
SELECT
	userMaster.user_id,
	userMaster.user_name,
	u_age.age_doc,
	u_a.age_name 
FROM
	users AS userMaster
	LEFT JOIN users_age AS u_age ON u_age.id = userMaster.age_id
	LEFT JOIN users_age AS u_a ON u_a.id = userMaster.age_id;
```



### 一对一查询映射

```java
// 很多时候连表返回的字段很多都相同，所以在每个vo里面都会出现，如果把这些重复性字段封装成一个类，会更好维护，所以说针对这个情况 版本 >= 1.0.6 即可使用oneToOneSelect 方法

 JoinLambdaWrapper<Users> wrapper = joinLambdaQueryWrapper(Users.class);

 wrapper.leftJoin(UsersAge.class, UsersAge::getId, Users::getAgeId)
   // oneToOneSelect 第一个参数需要映射的实体类字段，第二个参数则是查询函数
        .oneToOneSelect(UsersVo::getUsersAge, (cb) -> {
             cb.add(UsersAge::getAgeDoc, UsersAge::getAgeName)
               /* 
              当你出现两个实体类映射字段相同，例如 user实体中有个字段id，userAge表中也有个字段id，你									想要同时获取这两个字段，这时候则可以使用
               |column : 查询字段
               |alias  : 别名
							 |fieldName : 字段名称
               add(SFunction<T, ?> column, String alias, SFunction<F, ?> fieldName)
               */
               .add(UsersAge::getId, "ageId", UsersAge::getId)
          		// 在1.3.2版本后 属性名和映射vo的属性名相同的情况下，可以不必写别名，就可以完成自动映射
          	 .add(UsersAge::getId);
         })
   // 1.3.3版本之后可以 这样子查询这个类的所有查询字段并赋值到对象中
     .oneToOneSelect(UsersVo::getUsersAge,UsersAge.class)
   .end();


 return super.joinList(wrapper, UsersVo.class);

// 执行SQL 
SELECT users.user_name, users.create_time, users.age_id, users.user_id, users_age.age_doc
	, users_age.age_name, users_age.id AS ageId
FROM users users
	LEFT JOIN users_age users_age ON users_age.id = users.age_id
  
// 返回结果
[
  {
   "ageId":1,
   "createTime":1635416270000,
   "userId":1,
   "userName":"名字啊",
   "usersAge":{
     "ageDoc":"90",
     "ageName":"90",
     "id":1
   }
  }....
]



```





### 多对多查询映射

```java
JoinLambdaWrapper<UsersAge> wrapper = joinLambdaQueryWrapper(UsersAge.class);

wrapper.leftJoin(Users.class, Users::getAgeId, UsersAge::getId)
  			// manyToManySelect 多对多，对应的就是 mybatis中的resultMap中的collection标签
  			// 该方法第一个参数代表的是需要映射到的实体类字段
        // 第二个参数代表list中的实体类型 例如 List<Users> 这里的实体类型就是Users
  			// 第三个就是要查询的字段
        .manyToManySelect(UsersAgesVo::getUsersList, Users.class, (cb) -> {
          	// 在1.3.2版本后 属性名和映射vo的属性名相同的情况下，可以不必写别名，就可以完成自动映射
           cb.add(Users::getUserName, Users::getUserId, Users::getCreateTime);
         })
  			   // 1.3.3版本之后可以 这样子查询这个类的所有查询字段并赋值到集合对象中
    		.manyToManySelect(UsersAgesVo::getUsersList,Users.class)
  .end();
return super.joinList(wrapper, UsersAgesVo.class);

// 执行SQL
SELECT 		   
 users_age.age_doc,users_age.age_name,users_age.id,users.user_name,users.user_id,users.create_time
FROM users_age AS users_age
	LEFT JOIN users AS users ON users.age_id = users_age.id;

// 返回数据
[
  {"ageDoc":"90","ageName":"90","id":1,
   "usersList":[
     {"createTime":1635416270000,"userId":1,"userName":"名字啊"},
     {"createTime":1635416270000,"userId":2,"userName":"名字2"}
   ]
  }
]
```



### 返回基础类型数据

```java
// 当我们只需要查询一个字段，例如id列表，现在支持直接传递基础类型

JoinLambdaWrapper<Users> wrapper = joinLambdaQueryWrapper(Users.class)
                .select(Users::getUserId);

List<Integer> ids = super.joinList(wrapper, Integer.class);

System.out.println(JSON.toJSONString(ids));

// 输出结果：[1,2]

// 也支持返回单个数据类型

JoinLambdaWrapper<Users> wrapper = joinLambdaQueryWrapper(Users.class)
                .select(Users::getUserName)
                .eq(Users::getUserId, 1)
                .last("limit 1");

String userName = super.joinGetOne(wrapper, String.class);

System.out.println(userName);

// 输出结果："我是名字1"

```



### 根据实体不为空的数据查询

```java
// 如果需要根据实体查询可以采用这样的实例化
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(new Users().setUserName("name啊")
                                                                          .setUserId(1L));
// 或者可以采用这样的setEntity
// wrapper.setEntity(new Users().setUserName("name啊"));

// 这一部分一个参数是join中定义的连接的表，第二个参数是随意的表，但是是要出现构造器中的
wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
// 然后可以设置多表中的查询条件，这一步和mp一致
			.eq(UserAge::getAgeName,"95")
  		.select(UserAge::getAgeName)
// 最后一步 需要使用end方法结束
      wrapper.end();

// 执行查询
usersService.joinList(wrapper,UsersVo.class);

// 执行SQL 
select 
  users.user_id,
  users.user_name,
  users_age.age_name
from users users
  left join users_age users_age on users_age.id = users.age_id
where 
 users.user_id = 1
 and users.user_name = 'name啊'
 and users_age.age_name = '95'


```



### notDefaultSelectAll() 不默认查询主表全部的字段

```java
// 如果需要根据实体查询可以采用这样的实例化
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(new Users().setUserName("name啊")
        .setUserId(1L));

// 因为默认是查询主表所有查询字段，如果不需要查询主表全部字段就调用该方法
        wrapper.notDefaultSelectAll();

// 这一部分一个参数是join中定义的连接的表，第二个参数是随意的表，但是是要出现构造器中的
        wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
// 然后可以设置多表中的查询条件，这一步和mp一致
        .eq(UserAge::getAgeName,"95")
        .select(UserAge::getAgeName)
// 最后一步 需要使用end方法结束
        .end();

// 执行查询
        usersService.joinList(wrapper,UsersVo.class);

// 执行SQL 
        select
        users_age.age_name
        from users users
        left join users_age users_age on users_age.id = users.age_id
        where
        users.user_id = 1
        and users.user_name = 'name啊'
        and users_age.age_name = '95'



```



### selectAs() 查询添加别名

```java
/* 
  selectAs(List<As<T>> columns) 
  selectAs(SFunction<T, ?> column, String alias)
  查询并添加别名
*/
// 拿起来我们上面用的哪个实例。我现在需要给ageName给个别名 user_age_name
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
        wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
        .eq(UserAge::getAgeName,"95")
        .selectAs(UserAge::getAgeName,"user_age_name")
        // 在1.3.2版本后可以采用函数的方式写别名
        .selectAs(UserAge::getAgeName,UsersVo::getUserAgeName)
        .end();
// 执行查询
        usersService.joinList(wrapper,UsersVo.class);

// 执行SQL 
        select
        users.user_id,
        users.user_name,
        users_age.age_name as user_age_name
        from users users
        left join users_age users_age on users_age.id = users.age_id
        where (
        users_age.age_name = '95'
        )

// 现在来个高级需求，我需要查询出users_age表中的两个字段并且需要加一个固定值

        JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
        wrapper.join(UsersAge.class)
        .leftJoin(UsersAge::getId,Users::getAgeId)
        .eq(UserAge::getAgeName,"95")
        .selectAs((cb) -> {
        cb.add(UserAge::getAgeName,"user_age_name")
        .add(UserAge::getAgeDoc)
        .addFunAlias(UserAge::getAgeName,UsersVo::getUserAgeName) // 该方法在1.3.2版本后支持
        .add("mp永远滴神","mpnb")
        .add("sum(users_age.id)","ageIdSum",false); // 这个为false就是代表不是字符串，会原样查询 在1.3.1版本后支持
        }).end();
// 执行查询
        usersService.joinList(wrapper,UsersVo.class);

// 执行SQL 
        select
        users.user_id,
        users.user_name,
        users_age.age_name as user_age_name,
        users_age.age_doc,
        'mp永远滴神' as mpnb,
        sum(users_age.id) as ageIdSum
        from users users
        left join users_age users_age on users_age.id = users.age_id
        where (
        users_age.age_name = '95'
        )

 
/*
	这里需要注意啊，如果selectAs那个地方因为是函数接口，所以值是不可以改变的，如果是可变的那么可以采用
	selectAs(Arrays.asList(
			new As(UserAge::getAgeName,"user_age_name"),
			new As(UserAge::getAgeDoc)
	))
*/

```

### selectAll() 查询全部

```java
// selectAll()方法，查询出当前表所有的子段
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
        wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
        .eq(UserAge::getAgeName,"95")
        .selectAll().end();
// 执行查询
        usersService.joinList(wrapper,UsersVo.class);

// 执行SQL 
        select
        users.user_id,
        users.user_name,
        users_age.age_name,
        users_age.age_doc,
        users_age.id
        from users users
        left join users_age users_age on users_age.id = users.age_id
        where (
        users_age.age_name = '95'
        )
```



### joinAnd() join添加条件(1.3.4版本后可根据Wrapper设定条件)

```java

/*
		相信有很多情况需要限制join的表的限制条件那么就需要 
    joinAnd(SFunction<T, Object> field, Object val, int index)
*/

JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
        wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
        .joinAnd(UsersAge::getId,1,0) // 需要注意啊，这个最后一个下标是指的第几个join，因为有时候会出现多个连接，附表连接主表，附表的附表连接附表这样子
        .eq(UserAge::getAgeName,"95")
        .selectAs((cb) -> {
        cb.add(UserAge::getAgeName,"user_age_name")
        .add(UserAge::getAgeDoc)
        .add("mp永远滴神","mpnb");
        }).end();
// 执行查询
        usersService.joinList(wrapper,UsersVo.class);

// 执行SQL 
        select
        users.user_id,
        users.user_name,
        users_age.age_name as user_age_name,
        users_age.age_doc,
        'mp永远滴神' as mpnb
        from users users
        left join users_age users_age on users_age.id = users.age_id and users_age.id = 1
        where (
        users_age.age_name = '95'
        )

// 1.3.4版本后写法
        Joins.of(Users.class)
        .masterLogicDelete(false)
        .pushLeftJoin(UsersVo::getUsersAge, UsersAge.class)
        .joinAnd(0, w -> w.eq(UsersAge::getId, Users::getAgeId)
        .ne(UsersAge::getId, 10))
        .isNotNull(UsersAge::getId).end().joinList(UsersVo.class)

        // 执行SQL
        SELECT
        users.user_name,users.create_time,users.age_id,users.content_json,users.user_id, t1.age_doc as t1_ageDoc , t1.age_name as t1_ageName , t1.create_time as t1_createTime , t1.content_json_age as t1_contentJsonAge , t1.id as t1_id
        FROM users as users
        LEFT JOIN users_age as t1 ON t1.id = users.age_id and (t1.id = users.age_id AND t1.id <> 10) WHERE (t1.id IS NOT NULL)
```

### 同个接口返回任意实体

```java
// 这个就不得不说了，大多数情况下，一个接口是返回一个实体类型的，但是很多情况下，我们有不同的业务需求，所返回的对象也是不一样的，全部加在一个对象中又太臃肿不好维护，所以就需要这个返回任意定制类型
// 使用方法 在最后一个参数中增加上自己的实体类型就行了
List<UsersVo> usersVoList = usersService.joinList(wrapper,UsersVo.class);

```



### 自定义别名 TableAlias

```java
/*
	这个自定义别名是某些业务下，比如说在项目中构建了SQL啊，之类的，但是构建的SQL别名一般都是固定的达到通用，
	所以需要在实体中增加别名使用@TableAlias注解就行了，如果没有添加别名 就默认使用表名作为别名 
*/
@TableName("app_users")
@TableAlias("users")
public class Users implements Serializable {

}
```





## 用法注意

1.在使用 join service 一系列方法的时候，所有参数都不能传null

2.这个条件构造器啊，你在join的时候就相当于创建一个新的构造器，你要在这个新的构造器中实现你所有的操作，包括查询，和条件，排序之类的，这样的好处在于，维护好一些，毕竟都放在一起的话，到时候容易迷。

3.您的start是作者更新的动力，如果用的人多的话，可以留言，我会继续更新并适配mp其他版本，如果各位等不了呢，也可以把源码下载下来，放进你的项目中改一下里面的东西。
