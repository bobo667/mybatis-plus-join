# mybatis-plus-join

mybatis-plus-join是mybatis plus的一个多表插件，上手简单，十分钟不到就能学会全部使用方式，只要会用mp就会用这个插件，仅仅依赖了lombok，而且是扩展mp的构造器并非更改原本的构造器，不会对原有项目产生一点点影响，相信大多数项目都有这俩插件，四舍五入就是没依赖。

mybatis-plus-join示例：**
**gitee: https://gitee.com/mhb0409/mybatis-plus-join-example**
**github: https://github.com/bobo667/mybatis-plus-join-example**



### 关于该插件的一点问题

1. 出现了bug怎么办，不是mybatis plus官方的会不会不稳定啊？ 这个大可以放心，这个插件我已经在生产环境跑了半年多了，没出过什么问题，如果遇到问题可以在 Issues 上提出，我看见就会解决，上午提的，不忙的话下午就能打包新版本，忙的话大概就需要晚上就差不多了
2. 关于维护到啥时候？mybatis plus不倒我不倒（当然，如果长期没有star，哪怕是我得先倒了，还是那，您的star就是作者更新的动力，手动ღ( ´･ᴗ･` )比心）
3. 有什么有想法的新功能啊，或者改善啊，可以在Issues 上提出
4. 如果想联系作者，可以在wx上搜索小程序 **马汇博的博客 **在关于我中有微信号，欢迎来扰



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
    <version>1.0.8</version>
 </dependency>
```



## 版本对应关系（此处只显示对应的最新版本）

> 标注：*号代表，从起始版本之后都是可以使用的

| Mybatis-plus | Mybatis-plus-join                  |
| ------------ | ---------------------------------- |
| 3.2.0        | 1.2.0                              |
| 3.3.1 - 3.42 | 1.0.2                              |
| 3.4.3.4 - *  | 1.0.3 、1.0.4、1.0.5、1.0.6、1.0.8 |





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

1.增加了多对多映射
2.去掉了fastJSON依赖
3.更改serviceImpl动态返回类型的处理方式，采用更优的插件式注入方式
这次终于去掉了总是说的fastJSON依赖，现在采用动态注入resultMap方式，来构建普通多表，一对一，多对多查询，采用插件式懒加载 + 缓存机制，启动时间无影响，使用加载一下就可以直接从缓存调用，保证不影响使用中的效率。



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

4.注入mp自定义方法，主要是继承JoinDefaultSqlInjector

```java
package com.fk.zws.app.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.fk.zws.toolkit.mp.injector.CustomizeSqlInjector;
import com.fk.zws.toolkit.mp.plugIn.injector.JoinDefaultSqlInjector;
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
  		.select(UserAge::getAgeName);
// 最后一步 需要使用end方法结束
wrapper.end();
  

// 完整的就是
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId)
  	.eq(UserAge::getAgeName,"95")
  	.select(UserAge::getAgeName)
  	.end();

usersService.joinList(wrapper,UsersVo.class);

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
               .add(UsersAge::getId, "ageId", UsersAge::getId);
         }).end();

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
           cb.add(Users::getUserName, Users::getUserId, Users::getCreateTime);
         }).end();
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
wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId);
// 然后可以设置多表中的查询条件，这一步和mp一致
wrapper.eq(UserAge::getAgeName,"95")
  		.select(UserAge::getAgeName);
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
wrapper.leftJoin(UsersAge.class,UsersAge::getId,Users::getAgeId);
// 然后可以设置多表中的查询条件，这一步和mp一致
wrapper.eq(UserAge::getAgeName,"95")
  		.select(UserAge::getAgeName);
// 最后一步 需要使用end方法结束
wrapper.end();

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



### joinAnd() join添加条件

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
