package endless.overlook.jla.service.analyser.impl;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaNumberConstants;
import endless.overlook.jla.service.analyser.IBusinessSqlEntityAnalyser;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Description:<b>模糊查询分析器</b>
 *
 * @author Ralph
 * @since 2018年10月12日下午3:50:47
 */
public class FuzzyQueryAnalyser implements IBusinessSqlEntityAnalyser {

    /** 模糊匹配数量映射 **/
    private Map<String, Integer> fuzzyQuerySql2EntityMap = new TreeMap<String, Integer>();

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月12日 下午4:04:32
     */
    public FuzzyQueryAnalyser() {
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
        //模糊查询操作情况
        for (BusinessSqlEntity businessSqlEntity : businessSqlEntityList) {
            if (StringUtils.containsIgnoreCase(businessSqlEntity.getPlainSql(),
                    "LIKE '%")) {
                if (!fuzzyQuerySql2EntityMap
                        .containsKey(businessSqlEntity.getPlainSql())) {
                    fuzzyQuerySql2EntityMap.put(businessSqlEntity.getPlainSql(),
                            JlaNumberConstants.N_ONE);
                } else {
                    Integer fuzzyQuerySqlCount =
                            (Integer) (fuzzyQuerySql2EntityMap
                                    .get(businessSqlEntity.getPlainSql()))
                                    + JlaNumberConstants.N_ONE;
                    fuzzyQuerySql2EntityMap.put(businessSqlEntity.getPlainSql(),
                            fuzzyQuerySqlCount);
                }
            }
        }

        List<Map.Entry<String, Integer>> fuzzyQueryCountMappingsList = new ArrayList<Map.Entry<String, Integer>>(
                fuzzyQuerySql2EntityMap.entrySet());
        Collections.sort(fuzzyQueryCountMappingsList,
                new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> mappings1,
                            Map.Entry<String, Integer> mappings2) {
                        return mappings2.getValue() - mappings1.getValue();
                    }
                });
        analysedResult.put(JlaConstants.C_KEY_BUSINESS_FUZZYQUERYSQLLIST,
                fuzzyQueryCountMappingsList);
        return analysedResult;
    }

}
