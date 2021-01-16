package endless.overlook.jla.threads;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.config.ConfigLoader;
import endless.overlook.jla.constants.JlaConfigConstants;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaNumberConstants;
import endless.overlook.jla.constants.JlaSymbolConstants;
import endless.overlook.jla.service.analyser.IBusinessSqlEntityAnalyser;
import endless.overlook.jla.service.printer.AbstractAnalysedReportPrinter;
import endless.overlook.jla.service.processor.IterationProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Description:<b>分析JDBC日志任务实现类</b>
 *
 * @author Ralph
 * @since 2018-4-7 下午3:43:07
 */
public class JlaMainTask implements Callable<Boolean> {

    /**
     * 日志记录对象
     **/
    private static final Logger logger = LoggerFactory
            .getLogger(JlaMainTask.class);

    /**
     * 配置文件中指定的字符编码集
     **/
    private static String _CHARSET;

    /**
     * 字符编码集对象
     **/
    private static Charset _PROCESS_CHARSET;

    /**
     * JDBC日志文件
     **/
    private File jdbcLogFile;

    /**
     * 分析结果文件夹
     **/
    private File analysingDirectory;

    /**
     * 配置加载器
     **/
    private ConfigLoader configLoader;

    //分析器类名称
    private List<String> analyserClassesList = new ArrayList<String>();

    //输出器类名称
    private List<String> printerClassesList = new ArrayList<String>();

    /** 日志分析线程池 **/
    private static final CompletionService<Boolean> analysingCompletionService = new ExecutorCompletionService<Boolean>(
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
                            + JlaNumberConstants.N_ONE,
                    new BasicThreadFactory.Builder()
                            .namingPattern("JlaAnalysingTask-%d").build()));

    /** 报告输出分析线程池 **/
    private static final CompletionService<Boolean> printingCompletionService = new ExecutorCompletionService<Boolean>(
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
                            + JlaNumberConstants.N_ONE,
                    new BasicThreadFactory.Builder()
                            .namingPattern("JlaPrintingTask-%d").build()));

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018-4-11 下午2:01:08
     * @param analysingDirectory 分析结果文件夹
     * @param jdbcLogFile         JDBC日志文件
     * @param configLoader        配置加载器
     */
    public JlaMainTask(File analysingDirectory, File jdbcLogFile,
            ConfigLoader configLoader) {
        super();
        this.jdbcLogFile = jdbcLogFile;
        this.configLoader = configLoader;
        this.analysingDirectory = analysingDirectory;
        String processCharSet = configLoader.getConfig(
                JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_PROCESSCHARSET);
        _CHARSET = "GB2312";
        if (StringUtils.isNotBlank(processCharSet)) {
            _CHARSET = processCharSet;
        }
        _PROCESS_CHARSET = Charset.forName(_CHARSET);

        String analysersString = configLoader
                .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_ANALYSERS);
        String[] analyserClassesArray = analysersString
                .split(JlaSymbolConstants.C_SYMBOL_SEPERATOR_COMMAORSEMICOLON);
        for (String analyserClass : analyserClassesArray) {
            if (StringUtils.isNotBlank(analyserClass)) {
                analyserClassesList.add(analyserClass);
            }
        }
        String printersString = configLoader
                .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_PRINTERS);
        String[] printerClassesArray = printersString
                .split(JlaSymbolConstants.C_SYMBOL_SEPERATOR_COMMAORSEMICOLON);
        for (String printerClass : printerClassesArray) {
            if (StringUtils.isNotBlank(printerClass)) {
                printerClassesList.add(printerClass);
            }
        }
    }

    /**
     * Description:<b>线程执行的业务逻辑</b>
     *
     * @return 目标JDBC日志文件是否解析成功
     * @throws Exception
     * @author Ralph
     * @since 2018-4-7 下午3:44:36
     */
    @Override
    public Boolean call() throws Exception {
        return analyseJdbcLogFiles(jdbcLogFile);
    }

    /**
     * Description:<b>分析JDBC日志文件</b>
     *
     * @param jdbcLogFile 目标JDBC日志文件
     * @author Ralph
     * @since 上午9:51:20
     */
    private boolean analyseJdbcLogFiles(File jdbcLogFile) {
        try {
            //遍历JDBC日志
            List<BusinessSqlEntity> businessSqlEntity = iteratingJdbcLogFile(
                    jdbcLogFile);
            //分析JDBC日志
            Map<String, Object> resultMap = analysingJdbcLogFile(
                    businessSqlEntity);
            //输出分析报告
            generateReportFiles(jdbcLogFile, resultMap, analysingDirectory);
        } catch (IOException e) {
            logger.error("读取目标Jdbc文件{}失败！", jdbcLogFile.getName(), e);
            return false;
        }
        return true;
    }

    /**
     * Description:<b>遍历目标JDBC日志文件</b>
     *
     * @param jdbcLogFile 目标JDBC日志文件
     * @return 分析结果集合
     * @throws IOException IOException
     * @author Ralph
     * @since 2018-4-26 下午5:36:20
     */
    private List<BusinessSqlEntity> iteratingJdbcLogFile(File jdbcLogFile)
            throws IOException {
        logger.info("开始----->遍历文件[{}]......", jdbcLogFile.getName());
        IterationProcessor iterationProcessor = new IterationProcessor(
                configLoader);
        LineIterator lineIterator = FileUtils
                .lineIterator(jdbcLogFile, _CHARSET);
        try {
            Integer currentLineNumber = JlaNumberConstants.N_ZERO;
            /** !!!文件最后需要至少保留两行空行，否则最后一个SqlEntity会丢失！！！ **/
            while (lineIterator.hasNext()) {
                String currentLine = lineIterator.nextLine();
                if (currentLineNumber > JlaNumberConstants.N_ZERO && (
                        currentLineNumber % JlaNumberConstants.N_TEN_THUSANDS
                                == JlaNumberConstants.N_ZERO)) {
                    logger.warn("Jdbc日志文件[{}]已遍历{}W行......",
                            jdbcLogFile.getName(), currentLineNumber
                                    / JlaNumberConstants.N_TEN_THUSANDS);
                }
                currentLineNumber++;
                if (!iterationProcessor.processLine(currentLine)) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error Occurred When Iterating Lines......", e);
        } finally {
            LineIterator.closeQuietly(lineIterator);
        }
        logger.info("结束----->遍历文件[{}]......", jdbcLogFile.getName());
        return iterationProcessor.getResult();
    }

    /**
     * Description:<b>分析JDBC日志中提取出来的SQL实体</b>
     *
     * @param businessSqlEntityList SQL实体集合
     * @return SQL实体分析结果集合
     * @author Ralph
     * @since 2018年10月15日 下午6:16:45
     */
    private Map<String, Object> analysingJdbcLogFile(
            List<BusinessSqlEntity> businessSqlEntityList) {
        logger.info("开始分析[{}]......", jdbcLogFile.getName());
        Map<String, Object> analysedResult = new ConcurrentHashMap<String, Object>();
        //初始化分析器集合
        for (String analyserClass : analyserClassesList) {
            try {
                Class<?> analyserClazz = Class.forName(
                        JlaConstants.C_PREFIX_ANALYSERS + analyserClass);
                Constructor<?> analyserConstructor = analyserClazz
                        .getConstructor();
                IBusinessSqlEntityAnalyser sqlEntityAnalyser = (IBusinessSqlEntityAnalyser) analyserConstructor
                        .newInstance();
                analysingCompletionService
                        .submit(new JlaAnalysingTask(sqlEntityAnalyser,
                                businessSqlEntityList, analysedResult));
            } catch (ClassNotFoundException e) {
                logger.error("实例化分析器失败......", e);
            } catch (InstantiationException e) {
                logger.error("实例化分析器失败......", e);
            } catch (IllegalAccessException e) {
                logger.error("实例化分析器失败......", e);
            } catch (IllegalArgumentException e) {
                logger.error("实例化分析器失败......", e);
            } catch (InvocationTargetException e) {
                logger.error("实例化分析器失败......", e);
            } catch (SecurityException e) {
                logger.error("实例化分析器失败......", e);
            } catch (NoSuchMethodException e) {
                logger.error("实例化分析器失败......", e);
            }
        }

        for (int i = JlaNumberConstants.N_ZERO;
             i < analyserClassesList.size(); i++) {
            try {
                Future<Boolean> future = analysingCompletionService.take();
                future.get();
            } catch (InterruptedException e) {
                logger.error("[JdbcAnalyser]获取线程结果出错......", e);
            } catch (ExecutionException e) {
                logger.error("[JdbcAnalyser]获取线程结果出错......", e);
            }
        }

        return analysedResult;
    }

    /**
     * Description:<b>输出分析报告</b>
     *
     * @author Ralph
     * @since 2018-4-26 下午5:35:24
     * @param jdbcLogFile
     * @param analysedMap           分析结果信息
     * @param analysingDirectory 报告目录对象
     * @param analysingDirectory    Analysing目录
     */
    private void generateReportFiles(File jdbcLogFile,
            Map<String, Object> analysedMap, File analysingDirectory) {
        logger.info("开始生成报告[{}]......", jdbcLogFile.getName());

        for (String printerClass : printerClassesList) {
            try {
                Class<?> printerClazz = Class
                        .forName(JlaConstants.C_PREFIX_PRINTERS + printerClass);
                Constructor<?> printerConstructor = printerClazz
                        .getConstructor(File.class, Charset.class, File.class,
                                ConfigLoader.class);
                AbstractAnalysedReportPrinter analysedReportPrinter = (AbstractAnalysedReportPrinter) printerConstructor
                        .newInstance(jdbcLogFile, _PROCESS_CHARSET,
                                analysingDirectory, configLoader);
                printingCompletionService
                        .submit(new JlaPrintingTask(analysedMap,
                                analysedReportPrinter));
            } catch (ClassNotFoundException e) {
                logger.error("实例化输出器失败......", e);
            } catch (InstantiationException e) {
                logger.error("实例化输出器失败......", e);
            } catch (IllegalAccessException e) {
                logger.error("实例化输出器失败......", e);
            } catch (IllegalArgumentException e) {
                logger.error("实例化输出器失败......", e);
            } catch (InvocationTargetException e) {
                logger.error("实例化输出器失败......", e);
            } catch (SecurityException e) {
                logger.error("实例化输出器失败......", e);
            } catch (NoSuchMethodException e) {
                logger.error("实例化输出器失败......", e);
            }
        }

        for (int i = JlaNumberConstants.N_ZERO;
             i < printerClassesList.size(); i++) {
            try {
                Future<Boolean> future = printingCompletionService.take();
                future.get();
            } catch (InterruptedException e) {
                logger.error("[JdbcAnalyser]获取线程结果出错......", e);
            } catch (ExecutionException e) {
                logger.error("[JdbcAnalyser]获取线程结果出错......", e);
            }
        }

        logger.info("JDBC日志文件{}分析完成......", jdbcLogFile.getName());
    }

}
