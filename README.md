# mybatis-plus

mybatis分页插件MicroPageInterceptor

已经提交maven中央库version使用最新的

```
<dependency>
    <groupId>com.github.jeffreyning</groupId>
    <artifactId>mybatis-plus-page</artifactId>
    <version>1.0.2-RELEASE</version>
</dependency>
```


MicroPageInterceptor特点：

1，	支持mysql和oracle分页
2，	不必在xml编写统计count的sql
3，	使用RowBounds子类PageInfo作为分页信息和记录总数的载体，不必像其他分页插件那样要求输入输出参数必须继承特殊父类。
4，	可在PageInfo中填写自定义排序sql串，提高查询性能和排序灵活性


Spring中配置mybatis分页插件MicroPageInterceptor

com.minxin.micro.mybatis.plugin.MicroPageInterceptor
建议在sqlSessionFactory中配置，可以根据sqlSessionFactory是oracle还是mysql配置插件的dialect属性

```
    <!-- define the SqlSessionFactory -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dynamic_xa_dataSource"/>
        <property name="typeAliasesPackage" value="foo.model"/>
      <property name="plugins">          
        <list>              
           <bean class="com.minxin.micro.mybatis.plugin.MicroPageInterceptor">
           <property name="dialect" value="mysql" />
            </bean>          
        </list>      
      </property>         
    </bean>
```

Dao接口中设置

接口中添加PageInfo参数，表示需要分页处理

```
//分页查询，传入PageInfo参数
public List queryInfoByCondition(Map paramMap,PageInfo pageInfo);
//不分页，则不用传PageInfo参数
public List queryInfoByCondition(Map paramMap);	
```

PageInfo结构

PageInfo继承RowBounds可以透传到Mybatis内部，不影响正常的参数。
pageNo,页数从1开始
pageRows,每页条数
orderStr,自定义排序sql(非必填)
total,返回总记录数（返回值）

```
public class PageInfo extends RowBounds {
	public PageInfo(int pageNo, int pageRows) {
		super(pageNo, pageRows);
	}
	public PageInfo(int offset, int limit, String orderStr) {
		super(offset, limit);
		this.orderStr=orderStr;
	}
	private Long total = 0l;
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	private String orderStr = "";
	public String getOrderStr() {
		return orderStr;
	}
	public void setOrderStr(String orderStr) {
		this.orderStr = orderStr;
	}
}
```


分页或不分页sql在xml中是一样的不需要修改；sql不需要针对pageinfo做额外处理，比如手工添加limit或rownum；不需要分页的则不传PageInfo参数；不需要额外填写count统计sql；

```
<select id="queryInfoByCondition" parameterType="Map" resultType="java.util.Map">         
select * from  micro_test5 where condition=#{param}		
</select>
```

Java调用Dao

PageInfo创建实例时需填写pageNo（页数从1开始）和pageRows(每页条数)
如果xml中sql没有orderby语句可以，在pageInfo中设置pageInfo.setOrderStr()

```
//创建分页信息对象
PageInfo pageInfo=new PageInfo(1,10);//第1页，每页10条
//可以设置order串（非必须）
pageInfo.setOrderStr("id desc");
//调用dao并传入pageInfo
List list=testRep.queryInfoById(paramMap,pageInfo);
System.out.println(list);
//使用pageInfo.getTotal()获取总记录数
System.out.println(pageInfo.getTotal());
```

注意：

1，自定义排序

如果xml中sql没有写死排序sql，则pageInfo可以设置排序字符串。好处是查询记录个数时不必排序，查询记录时自动拼上排序sql，提高查询性能。也可以不修改xml中sql支持灵活排序。

2，总记录数处理

不必编写额外的查询count的sql；记录数会在插件中设置到pageInfo对象的total字段中带回。

