# mybatis-plus-join

mybatis plus的一个多表插件，上手简单，只要会用mp就会用这个插件，仅仅依赖了lombok和fastJson，而且是扩展mp的构造器并非更改原本的构造器，不会对原有项目产生一点点影响，相信大多数项目都有这俩插件，四舍五入就是没依赖。


**注意：目前当前版本只支持3.3.1 - 3.42 如果有特殊需求，请下载源码改动，需要改的东西并不多**

maven坐标.....还没有，等后面在更新上去，主要是我忘记账号密码了，然后我试密码的时候，他给我账号禁用了，所以过段时间吧。现在各位可以把这个安装一下，然后引入就行了
`mvn install` 安装到本地


废话不多说，直接看怎么使用

```java
    /**
     * 查询列表
     *
     * @param wrapper 实体对象封装操作类（可以为 null）
     * @param <E>     返回泛型
     * @return 返回E 类型的列表
     */
    <EV, E> List<EV> joinList(Wrapper<E> wrapper, Class<EV> clz);

    /**
     * 查询单个对象
     *
     * @param wrapper 实体对象封装操作类
     * @param clz     返回对象
     * @param <E>     包装泛型类型
     * @param <EV>    返回类型泛型
     * @return EV
     */
    <E, EV> EV joinGetOne(Wrapper<E> wrapper, Class<EV> clz);


    /**
     * 查询count
     *
     * @param wrapper 实体对象封装操作类（可以为 null）
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

1.mapper继承 JoinBaseMapper< T >

2.service继承 JoinIService< T >

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
// 第二步 使用join方法创建一个连接
wrapper.join(UsersAge.class);
/*
	然后有三个方法可以使用 
	leftJoin 左联
	rightJoin 右联
	join 内联
*/
// 这一部分一个参数是join中定义的连接的表，第二个参数是随意的表，但是是要出现构造器中的
wrapper.leftJoin(UsersAge::getId,Users::getAgeId);
// 然后可以设置多表中的查询条件，这一步和mp一致
wrapper.eq(UserAge::getAgeName,"95")
  		.select(UserAge::getAgeName);
// 最后一步 需要使用end方法结束
wrapper.end();

// 完整的就是
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
wrapper.join(UsersAge.class)
  	.leftJoin(UsersAge::getId,Users::getAgeId)
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

### selectAs() 查询添加别名

```java
/* 
  selectAs(List<ColumnsBuilder<T>> columns) 
  selectAs(SFunction<T, ?> column, String alias)
  查询并添加别名
*/
// 拿起来我们上面用的哪个实例。我现在需要给ageName给个别名 user_age_name
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
wrapper.join(UsersAge.class)
  	.leftJoin(UsersAge::getId,Users::getAgeId)
  	.eq(UserAge::getAgeName,"95")
  	.selectAs(UserAge::getAgeName,"user_age_name")
  	.end();
// 执行查询
usersService.joinList(wrapper);

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
  	.selectAs(Arrays.as(
            new ColumnsBuilder<>(UserAge::getAgeName,"user_age_name"),
            new ColumnsBuilder<>(UserAge::getAgeDoc),
            new ColumnsBuilder<>("mp永远滴神","mpnb"),
    )).end();
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

 
```

### selectAll() 查询全部

```java
// selectAll()方法，查询出当前表所有的子段
JoinLambdaWrapper<Users> wrapper = new JoinLambdaWrapper<>(Users.class);
wrapper.join(UsersAge.class)
  	.leftJoin(UsersAge::getId,Users::getAgeId)
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
wrapper.join(UsersAge.class)
  	.leftJoin(UsersAge::getId,Users::getAgeId)
  	.joinAnd(UsersAge::getId,1,0) // 需要注意啊，这个最后一个下标是指的第几个join，因为有时候会出现多个连接，附表连接主表，附表的附表连接附表这样子
  	.eq(UserAge::getAgeName,"95")
  	.selectAs(Arrays.as(
              new ColumnsBuilder<>(UserAge::getAgeName,"user_age_name"),
              new ColumnsBuilder<>(UserAge::getAgeDoc),
              new ColumnsBuilder<>("mp永远滴神","mpnb"),
    )).end();
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

