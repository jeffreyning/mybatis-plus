# mybatis-plus

mybatis��ҳ���MicroPageInterceptor

�Ѿ��ύmaven�����versionʹ�����µ�

```
<dependency>
    <groupId>com.github.jeffreyning</groupId>
    <artifactId>mybatis-plus-page</artifactId>
    <version>1.0.2-RELEASE</version>
</dependency>
```


MicroPageInterceptor�ص㣺

1��	֧��mysql��oracle��ҳ
2��	������xml��дͳ��count��sql
3��	ʹ��RowBounds����PageInfo��Ϊ��ҳ��Ϣ�ͼ�¼���������壬������������ҳ�������Ҫ�����������������̳����⸸�ࡣ
4��	����PageInfo����д�Զ�������sql������߲�ѯ���ܺ����������


Spring������mybatis��ҳ���MicroPageInterceptor

com.minxin.micro.mybatis.plugin.MicroPageInterceptor
������sqlSessionFactory�����ã����Ը���sqlSessionFactory��oracle����mysql���ò����dialect����

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

Dao�ӿ�������

�ӿ������PageInfo��������ʾ��Ҫ��ҳ����

```
//��ҳ��ѯ������PageInfo����
public List queryInfoByCondition(Map paramMap,PageInfo pageInfo);
//����ҳ�����ô�PageInfo����
public List queryInfoByCondition(Map paramMap);	
```

PageInfo�ṹ

PageInfo�̳�RowBounds����͸����Mybatis�ڲ�����Ӱ�������Ĳ�����
pageNo,ҳ����1��ʼ
pageRows,ÿҳ����
orderStr,�Զ�������sql(�Ǳ���)
total,�����ܼ�¼��������ֵ��

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


��ҳ�򲻷�ҳsql��xml����һ���Ĳ���Ҫ�޸ģ�sql����Ҫ���pageinfo�����⴦�������ֹ����limit��rownum������Ҫ��ҳ���򲻴�PageInfo����������Ҫ������дcountͳ��sql��

```
<select id="queryInfoByCondition" parameterType="Map" resultType="java.util.Map">         
select * from  micro_test5 where condition=#{param}		
</select>
```

Java����Dao

PageInfo����ʵ��ʱ����дpageNo��ҳ����1��ʼ����pageRows(ÿҳ����)
���xml��sqlû��orderby�����ԣ���pageInfo������pageInfo.setOrderStr()

```
//������ҳ��Ϣ����
PageInfo pageInfo=new PageInfo(1,10);//��1ҳ��ÿҳ10��
//��������order�����Ǳ��룩
pageInfo.setOrderStr("id desc");
//����dao������pageInfo
List list=testRep.queryInfoById(paramMap,pageInfo);
System.out.println(list);
//ʹ��pageInfo.getTotal()��ȡ�ܼ�¼��
System.out.println(pageInfo.getTotal());
```

ע�⣺

1���Զ�������

���xml��sqlû��д������sql����pageInfo�������������ַ������ô��ǲ�ѯ��¼����ʱ�������򣬲�ѯ��¼ʱ�Զ�ƴ������sql����߲�ѯ���ܡ�Ҳ���Բ��޸�xml��sql֧���������

2���ܼ�¼������

���ر�д����Ĳ�ѯcount��sql����¼�����ڲ�������õ�pageInfo�����total�ֶ��д��ء�

