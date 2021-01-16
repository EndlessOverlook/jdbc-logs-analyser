package endless.overlook.jla.service.analyser.impl;

import endless.overlook.jla.beans.BusinessPlainTableEntity;
import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaSymbolConstants;
import endless.overlook.jla.service.analyser.IBusinessSqlEntityAnalyser;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Description:<b>实体表操作情况分析器</b>
 *
 * @author Ralph
 * @since 2018年10月12日下午3:56:01
 */
public class PlainTableAnalyser implements IBusinessSqlEntityAnalyser {

    /** 实体表执行次数统计映射 **/
    private Map<String, BusinessPlainTableEntity> plainTableName2EntityMap = new TreeMap<String, BusinessPlainTableEntity>();

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月12日 下午4:04:32
     */
    public PlainTableAnalyser() {
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
        //业务表操作次数统计
        for (BusinessSqlEntity businessSqlEntity : businessSqlEntityList) {
            if (StringUtils.isNotBlank(businessSqlEntity.getTableName())) {
                String[] tableNameArray = businessSqlEntity.getTableName()
                        .split(JlaSymbolConstants.C_SYMBOL_COMMA);
                for (String tableName : tableNameArray) {
                    if (StringUtils.isNotBlank(tableName)) {
                        BusinessPlainTableEntity plainTableEntity;
                        if (!plainTableName2EntityMap.containsKey(tableName)) {
                            plainTableEntity = new BusinessPlainTableEntity(
                                    tableName);
                            plainTableName2EntityMap
                                    .put(tableName, plainTableEntity);
                        } else {
                            plainTableEntity = plainTableName2EntityMap
                                    .get(tableName);
                            plainTableEntity.increaseCount();
                        }
                        //分操作类型统计
                        if (StringUtils.startsWithIgnoreCase(
                                businessSqlEntity.getPlainSql(), "SELECT")
                                || StringUtils.startsWithIgnoreCase(
                                businessSqlEntity.getPlainSql(), "SET ROWCOUNT")
                                || StringUtils.startsWithIgnoreCase(
                                businessSqlEntity.getPlainSql(),
                                "SET TRANSACTION ISOLATION LEVEL")) {
                            plainTableEntity.increaseSelectCount();
                        } else if (StringUtils.startsWithIgnoreCase(
                                businessSqlEntity.getPlainSql(), "UPDATE")) {
                            plainTableEntity.increaseUpdateCount();
                        } else if (StringUtils.startsWithIgnoreCase(
                                businessSqlEntity.getPlainSql(), "DELETE")) {
                            plainTableEntity.increaseDeleteCount();
                        } else if (StringUtils.startsWithIgnoreCase(
                                businessSqlEntity.getPlainSql(), "INSERT")) {
                            plainTableEntity.increaseInsertCount();
                        }
                    }
                }
            }
        }

        List<Map.Entry<String, BusinessPlainTableEntity>> plainTableCountMappingsList = new ArrayList<Map.Entry<String, BusinessPlainTableEntity>>(
                plainTableName2EntityMap.entrySet());
        Collections.sort(plainTableCountMappingsList,
                new Comparator<Map.Entry<String, BusinessPlainTableEntity>>() {
                    @Override
                    public int compare(
                            Map.Entry<String, BusinessPlainTableEntity> mappings1,
                            Map.Entry<String, BusinessPlainTableEntity> mappings2) {
                        return (int) (mappings2.getValue().getHitCount()
                                - mappings1.getValue().getHitCount());
                    }
                });
        analysedResult.put(JlaConstants.C_KEY_BUSINESS_PLAINTALBECOUNTMAPPINGS,
                plainTableCountMappingsList);
        return analysedResult;
    }
}
