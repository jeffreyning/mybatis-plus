package com.nh.micro.mybatis.plugin;



import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author ninghao
 *
 */

@Intercepts({
	@Signature(type = Executor.class, method = "query", args = {
			MappedStatement.class, Object.class, RowBounds.class,
			ResultHandler.class }),
	@Signature(type = Executor.class, method = "query", args = {
			MappedStatement.class, Object.class, RowBounds.class,
			ResultHandler.class, CacheKey.class, BoundSql.class })
	})
public class MicroPageInterceptor implements Interceptor {

	private String dialect = null;

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		Object parameter = args[1];
		RowBounds rowBounds = (RowBounds) args[2];
		ResultHandler resultHandler = (ResultHandler) args[3];
		Executor executor = (Executor) invocation.getTarget();

		CacheKey cacheKey;
		BoundSql boundSql;
		boolean pageFlag = false;
		if (rowBounds instanceof PageInfo) {
			pageFlag = true;
		}
		if (pageFlag == false) {
			return invocation.proceed();
		}
		PageInfo pageInfo = (PageInfo) rowBounds;
		if(pageInfo.getOffset()<1){
			throw new RuntimeException("pageNo < 1");
		}
		if(pageInfo.getLimit()<1){
			throw new RuntimeException("pageRows < 1");
		}		
		if (args.length == 4) {
			// 4 个参数时
			boundSql = ms.getBoundSql(parameter);
			cacheKey = executor.createCacheKey(ms, parameter, rowBounds,
					boundSql);
		} else {
			// 6 个参数时
			cacheKey = (CacheKey) args[4];
			boundSql = (BoundSql) args[5];
		}
		String oriSql = boundSql.getSql();
		String countSql = getCountSql(oriSql);
		String pageSql = getPageSql(pageInfo, oriSql);

		Configuration configuration = ms.getConfiguration();
		BoundSql countBoundSql = new BoundSql(configuration, countSql,
				boundSql.getParameterMappings(), parameter);
		CacheKey countCacheKey = executor.createCacheKey(ms, parameter,
				RowBounds.DEFAULT, countBoundSql);

		List countList = executor.query(ms, parameter, RowBounds.DEFAULT, null,
				countCacheKey, countBoundSql);
		Map tempMap = (Map) countList.get(0);
		Object[] tempArray = tempMap.entrySet().toArray();
		Entry totalCount = (Entry) tempArray[0];
		pageInfo.setTotal((Long) totalCount.getValue());

		String orderStr=pageInfo.getOrderStr();
		if(orderStr!=null && !"".equals(orderStr)){
			pageSql=pageSql+" order by "+orderStr;
		}
		BoundSql pageBoundSql = new BoundSql(configuration, pageSql,
				boundSql.getParameterMappings(), parameter);
		List resultList = executor.query(ms, parameter, RowBounds.DEFAULT,
				resultHandler, cacheKey, pageBoundSql);
		return resultList;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		dialect = properties.getProperty("dialect");

	}

	private String getCountSql(String sql) {
		int index = sql.toLowerCase().indexOf("from");
		return "select count(1) " + sql.substring(index);
	}

	private String getPageSql(PageInfo pageInfo, String sql) {
		String orderStr=pageInfo.getOrderStr();
		if(orderStr!=null && !"".equals(orderStr)){
			sql=sql+" order by "+orderStr+" ";
		}
		
		StringBuffer sqlBuffer = new StringBuffer(sql);
		if ("mysql".equalsIgnoreCase(dialect) || dialect == null
				|| "".equals(dialect)) {
			return getMysqlPageSql(pageInfo, sqlBuffer);
		} else if ("oracle".equalsIgnoreCase(dialect)) {
			return getOraclePageSql(pageInfo, sqlBuffer);
		}
		return sqlBuffer.toString();
	}

	private String getMysqlPageSql(PageInfo pageInfo, StringBuffer sqlBuffer) {
		int pageNo=pageInfo.getOffset();
		int pageRows=pageInfo.getLimit();
		int offset = (pageNo-1)*pageRows;
		int limit = pageInfo.getLimit();
		
		sqlBuffer.append(" limit ").append(offset).append(",").append(limit);
		return sqlBuffer.toString();

	}

	private String getOraclePageSql(PageInfo pageInfo, StringBuffer sqlBuffer) {
		// rownum start 1
		int pageNo=pageInfo.getOffset();
		int pageRows=pageInfo.getLimit();		
		int offset = (pageNo-1)*pageRows + 1;
		int limit = pageInfo.getLimit();
		
		sqlBuffer.insert(0, "select NHPAGE_TEMP.*, rownum NHPAGE_RN from (")
				.append(") NHPAGE_TEMP where rownum < ").append(offset + limit);
		sqlBuffer.insert(0, "select * from (").append(") where NHPAGE_RN >= ")
				.append(offset);
		return sqlBuffer.toString();
	}

}
