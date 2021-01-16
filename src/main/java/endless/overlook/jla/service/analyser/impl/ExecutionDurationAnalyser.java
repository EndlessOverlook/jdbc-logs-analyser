package endless.overlook.jla.service.analyser.impl;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.service.analyser.IBusinessSqlEntityAnalyser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Description:<b>执行时间分析器</b>
 *
 * @author Ralph
 * @since 2018年10月12日下午3:49:08
 */
public class ExecutionDurationAnalyser implements IBusinessSqlEntityAnalyser {

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月12日 下午4:04:32
     */
    public ExecutionDurationAnalyser() {
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
        Collections.sort(businessSqlEntityList,
                new Comparator<BusinessSqlEntity>() {
                    @Override
                    public int compare(BusinessSqlEntity sqlEntity1,
                            BusinessSqlEntity sqlEntity2) {
                        Long count1 = sqlEntity1.getDurationTime();
                        Long count2 = sqlEntity2.getDurationTime();
                        return (int) (count2 - count1);
                    }
                });
        analysedResult.put(JlaConstants.C_KEY_BUSINESS_BUSINESSSQLENTITYLIST,
                businessSqlEntityList);
        return analysedResult;
    }

}
