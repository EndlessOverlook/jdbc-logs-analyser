package endless.overlook.jla.service.printer;

import endless.overlook.jla.config.ConfigLoader;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Description:<b>报告结果输出器</b>
 *
 * @author Ralph
 * @since 2018-4-26 下午3:33:48
 */
public abstract class AbstractAnalysedReportPrinter {

    /** 目标JDBC日志文件 **/
    protected File jdbcLogFile;

    /** 目标JDBC日志文件编码集 **/
    protected Charset processCharset;

    /** 分析结果文件夹对象 **/
    protected File analysingDirectory;

    /** 配置加载器 **/
    protected ConfigLoader configLoader;

    public File getJdbcLogFile() {
        return jdbcLogFile;
    }

    public void setJdbcLogFile(File jdbcLogFile) {
        this.jdbcLogFile = jdbcLogFile;
    }

    public Charset getProcessCharset() {
        return processCharset;
    }

    public void setProcessCharset(Charset processCharset) {
        this.processCharset = processCharset;
    }

    public File getResultFileDirectory() {
        return analysingDirectory;
    }

    public void setResultFileDirectory(File analysingDirectory) {
        this.analysingDirectory = analysingDirectory;
    }

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018-4-26 下午3:40:24
     * @param jdbcLogFile
     *              目标JDBC日志文件
     * @param processCharset
     *              目标JDBC日志文件编码集对象
     * @param analysingDirectory
     *              分析结果文件目录
     * @param configLoader
     *              配置加载器
     */
    public AbstractAnalysedReportPrinter(File jdbcLogFile,
            Charset processCharset, File analysingDirectory,
            ConfigLoader configLoader) {
        super();
        this.jdbcLogFile = jdbcLogFile;
        this.processCharset = processCharset;
        this.analysingDirectory = analysingDirectory;
        this.configLoader = configLoader;
    }

    /**
     * Description:<b>输出报告</b>
     * @author Ralph
     * @since 2018-4-26 下午3:39:05
     * @param resultMap
     *              结果信息
     */
    public abstract void printReport(Map<String, Object> resultMap);
}
