/**
 * Description:<b></b>
 *
 * @author Ralph
 * @since 2018年10月15日 下午6:21:40
 */
package endless.overlook.jla.threads;

import java.util.Map;
import java.util.concurrent.Callable;

import endless.overlook.jla.service.printer.AbstractAnalysedReportPrinter;

/**
 * Description:<b>报告输出线程</b>
 *
 * @author Ralph
 * @since 2018年10月15日下午6:21:40
 */
/**
 * Description:<b>报告输出线程</b>
 *
 * @author Ralph
 * @since 2018年10月15日 下午6:21:40
 */
public class JlaPrintingTask implements Callable<Boolean> {

    /** 分析结果集合 **/
    private Map<String, Object> resultMap;

    /** 报告输出器 **/
    private AbstractAnalysedReportPrinter analysedReportPrinter;

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月15日 下午6:27:40
     * @param resultMap
     *              分析结果集集合
     * @param analysedReportPrinter
     *              报告输出器
     */
    public JlaPrintingTask(Map<String, Object> resultMap,
            AbstractAnalysedReportPrinter analysedReportPrinter) {
        super();
        this.resultMap = resultMap;
        this.analysedReportPrinter = analysedReportPrinter;
    }

    /**
     * Description:<b>输出分析报告</b>
     * @author Ralph
     * @since 2018年10月15日 下午6:22:08
     * @return
     *              执行结果
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        analysedReportPrinter.printReport(resultMap);
        return true;
    }
}
