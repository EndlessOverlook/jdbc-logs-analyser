package endless.overlook.jla.service.analyser;

import java.util.List;
import java.util.Map;

import endless.overlook.jla.beans.BusinessSqlEntity;

/**
 * Description:<b>报告分析器接口</b>
 *
 * @author Ralph
 * @since 2018年10月12日下午12:03:45
 */
public interface IBusinessSqlEntityAnalyser {

    /**
     * Description:<b>分析SQL实体</b>
     * @author Ralph
     * @since 2018年10月12日 下午2:04:45
     * @param businessSqlEntityList
     * @param analysedResult
     * @return
     */
    public Object analyseBusinessSqlEntity(
            List<BusinessSqlEntity> businessSqlEntityList,
            Map<String, Object> analysedResult);
}
