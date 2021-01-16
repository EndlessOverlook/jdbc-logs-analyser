package endless.overlook.jla.service.analyser.impl;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.beans.BusinessUnionTableEntity;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.service.analyser.IBusinessSqlEntityAnalyser;

import java.util.*;
import java.util.Map.Entry;

/**
 * Description:<b>业务表操作情况分析器</b>
 *
 * @author Ralph
 * @since 2018年10月12日下午3:54:24
 */
public class BusinessTableAnalyser implements IBusinessSqlEntityAnalyser {

    /** 业务表名实体映射 **/
    private Map<String, BusinessUnionTableEntity> tableName2EntityMap = new HashMap<String, BusinessUnionTableEntity>();

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月12日 下午4:04:32
     */
    public BusinessTableAnalyser() {
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
        //业务表操作次数
        for (BusinessSqlEntity businessSqlEntity : businessSqlEntityList) {
            BusinessUnionTableEntity businessTableEntity = new BusinessUnionTableEntity(
                    businessSqlEntity.getTableName(),
                    businessSqlEntity.getParserType());
            if (!tableName2EntityMap
                    .containsKey(businessSqlEntity.getTableName())) {
                tableName2EntityMap.put(businessSqlEntity.getTableName(),
                        businessTableEntity);
            } else {
                tableName2EntityMap.get(businessSqlEntity.getTableName())
                        .increaseCount();
            }
        }

        List<BusinessUnionTableEntity> tableEntityList = new ArrayList<BusinessUnionTableEntity>();
        Iterator<Entry<String, BusinessUnionTableEntity>> tableNameIterator = tableName2EntityMap
                .entrySet().iterator();
        while (tableNameIterator.hasNext()) {
            Entry<String, BusinessUnionTableEntity> entry = (Entry<String, BusinessUnionTableEntity>) tableNameIterator
                    .next();
            tableEntityList.add(entry.getValue());
        }
        Collections.sort(tableEntityList,
                new Comparator<BusinessUnionTableEntity>() {
                    @Override
                    public int compare(BusinessUnionTableEntity tableEntity1,
                            BusinessUnionTableEntity tableEntity2) {
                        Integer count1 = tableEntity1.getTableHitCount();
                        Integer count2 = tableEntity2.getTableHitCount();
                        return count2 - count1;
                    }
                });
        analysedResult.put(JlaConstants.C_KEY_BUSINESS_UNIONTABLEENTITY,
                tableEntityList);
        return analysedResult;
    }
}
