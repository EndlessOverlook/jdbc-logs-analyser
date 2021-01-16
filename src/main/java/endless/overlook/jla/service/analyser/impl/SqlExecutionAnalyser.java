package endless.overlook.jla.service.analyser.impl;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.service.analyser.IBusinessSqlEntityAnalyser;

import java.util.*;
import java.util.Map.Entry;

/**
 * Description:<b>SQL执行情况分析器</b>
 *
 * @author Ralph
 * @since 2018年10月12日下午2:10:00
 */
public class SqlExecutionAnalyser implements IBusinessSqlEntityAnalyser {

    /** SQL数量映射 **/
    private Map<String, BusinessSqlEntity> plainSql2EntityMap = new TreeMap<String, BusinessSqlEntity>();

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月12日 下午4:04:32
     */
    public SqlExecutionAnalyser() {
    }

    /**
     * Description:<b>分析SQL实体</b>
     * @author Ralph
     * @since 2018年10月12日 下午2:10:06
     * @param businessSqlEntityList
     *               <b>业务SQL实体</b>
     * @param analysedResult
     *               <b>分析结果</b>
     * @return
     *              <b>分析结果集</b>
     */
    @Override
    public Object analyseBusinessSqlEntity(
            List<BusinessSqlEntity> businessSqlEntityList,
            Map<String, Object> analysedResult) {
        for (BusinessSqlEntity businessSqlEntity : businessSqlEntityList) {
            if (!plainSql2EntityMap
                    .containsKey(businessSqlEntity.getPlainSql())) {
                plainSql2EntityMap.put(businessSqlEntity.getPlainSql(),
                        businessSqlEntity);
            } else {
                plainSql2EntityMap.get(businessSqlEntity.getPlainSql())
                        .increaseCount();
            }
        }

        List<BusinessSqlEntity> sqlEntityList = new ArrayList<BusinessSqlEntity>();
        Iterator<Entry<String, BusinessSqlEntity>> sqlEntityIterator = plainSql2EntityMap
                .entrySet().iterator();
        while (sqlEntityIterator.hasNext()) {
            Entry<String, BusinessSqlEntity> entry = (Entry<String, BusinessSqlEntity>) sqlEntityIterator
                    .next();
            sqlEntityList.add(entry.getValue());
        }
        Collections.sort(sqlEntityList, new Comparator<BusinessSqlEntity>() {
            @Override
            public int compare(BusinessSqlEntity sqlEntity1,
                    BusinessSqlEntity sqlEntity2) {
                Integer count1 = sqlEntity1.getSqlCount();
                Integer count2 = sqlEntity2.getSqlCount();
                return count2 - count1;
            }
        });
        analysedResult
                .put(JlaConstants.C_KEY_BUSINESS_SQLEXECUTION, sqlEntityList);
        return analysedResult;
    }
}
