# mybatis-plus-join

mybatis-plus-join是mybatis plus的一个多表插件，上手简单，十分钟不到就能学会全部使用方式，只要会用mp就会用这个插件，仅仅依赖了lombok，而且是扩展mp的构造器并非更改原本的构造器，不会对原有项目产生一点点影响，相信大多数项目都有这插件，四舍五入就是没依赖。

mybatis-plus-join示例：

> gitee: https://gitee.com/mhb0409/mybatis-plus-join-example
> github: https://github.com/bobo667/mybatis-plus-join-example



### 关于该插件的一点问题

1. 出现了bug怎么办，不是mybatis plus官方的会不会不稳定啊？ 已经有许多开发者再用，没出过什么问题，如果遇到问题可以在 Issues 上提出，我看见就会解决，上午提的，不忙的话下午就能打包新版本，忙的话大概就需要晚上就差不多了
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
   <artifactId>mybatis-plus-join-boot-starter</artifactId>
   <version>2.0.4</version>
</dependency>
```

apt依赖地址：
```xml
 <dependency>
   <groupId>icu.mhb</groupId>
   <artifactId>mybatis-plus-join-processor</artifactId>
   <version>2.0.4</version>
</dependency>
```


## 版本对应关系（此处只显示对应的最新版本）

> 标注：*号代表，从起始版本之后都是可以使用的

| Mybatis-plus    | Mybatis-plus-join                                                                          |
| --------------- |--------------------------------------------------------------------------------------------|
| 3.2.0           | 1.2.0                                                                                      |
| 3.3.1 - 3.42    | 1.0.2、1.3.4.1                                                                              |
| 3.4.3.4 - 3.5.2 | 1.0.3 、1.0.4、1.0.5、1.0.6、1.0.8、1.0.9、1.1.1、1.1.2、1.1.3、1.1.4、1.1.5、1.1.6、1.3.1、1.3.2、1.3.3 |
| 3.5.3 - *       | 1.3.3.1、1.3.4、1.3.5、1.3.5.1、1.3.6、1.3.7、1.3.8、2.0.4                                        |


## 2.0 版本发布啦
在2.0版本中，支持apt进行代码生成，可使用chain方式调用，底层采用QueryWrapper实现，他几乎把字符串和lambda的优点都包含了，强烈推荐使用
### 使用预览
```java
        UsersVo users = new UsersVo();
        users.setUserName("setUserName");
        users.setAgeName("setAgeName");
        UsersChain usersChain = UsersChain.create();
        UsersAgeChain ageChain = UsersAgeChain.create();

        return Joins.chain(usersChain)
                .selectAs(() -> {
                    return usersChain.userId().userName().createTime()
                                        .to(ageChain)
                                        .ageDoc().ageName().id();
                            })
                .leftJoin(ageChain._id(), usersChain._ageId())
                .joinAnd(ageChain, (w) -> w.eq(ageChain._id(1)))
                .eqIfNull(() -> {
                    return usersChain.userName(users.getUserName())
                    .userId(users.getUserId())
                    .ageId(users.getAgeId())
                    .to(ageChain)
                    .ageName(users.getAgeName())
                    .ageDoc(users.getAgeDoc());
                }).joinList(UsersVo.class);
```

## 使用预览
```java
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
```

## 使用文档
https://www.mhb.icu
