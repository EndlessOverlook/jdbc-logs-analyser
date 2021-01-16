package endless.overlook.jla.threads;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.service.analyser.IBusinessSqlEntityAnalyser;

/**
 * Description:<b>日志分析线程</b>
 *
 * @author Ralph
 * @since 2018年10月15日下午6:14:17
 */
public class JlaAnalysingTask implements Callable<Boolean> {

    /** 日志分析器 **/
    private IBusinessSqlEntityAnalyser sqlEntityAnalyser;

    /** SQL实体集合 **/
    private List<BusinessSqlEntity> businessSqlEntityList;

    /** 分析结果集合 **/
    private Map<String, Object> analysedResult;

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月15日 下午6:18:46
     * @param sqlEntityAnalyser
     *              SQL分析器
     * @param businessSqlEntityList
     *              SQL实体集合
     * @param analysedResult
     *              SQL分析结果集合
     */
    public JlaAnalysingTask(IBusinessSqlEntityAnalyser sqlEntityAnalyser,
            List<BusinessSqlEntity> businessSqlEntityList,
            Map<String, Object> analysedResult) {
        super();
        this.sqlEntityAnalyser = sqlEntityAnalyser;
        this.businessSqlEntityList = businessSqlEntityList;
        this.analysedResult = analysedResult;
    }

    /**
     * Description:<b>分析SQL实体集合</b>
     * @author Ralph
     * @since 2018年10月15日 下午6:15:04
     * @return
     *              执行结果
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        sqlEntityAnalyser.analyseBusinessSqlEntity(businessSqlEntityList,
            analysedResult);
        return true;
    }

}
