package endless.overlook.jla.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import endless.overlook.jla.config.ConfigLoader;
import endless.overlook.jla.constants.JlaConfigConstants;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaNumberConstants;
import endless.overlook.jla.threads.JlaMainTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:<b>JLA启动器</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:21:39
 */
public class JdbcLogsAnalyser {

    /** 日志记录对象 **/
    private static final Logger logger = LoggerFactory
            .getLogger(JdbcLogsAnalyser.class);

    /** JDBC日志分析线程池 **/
    private final CompletionService<Boolean> analyserCompletionService = new ExecutorCompletionService<Boolean>(
            Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
                        + JlaNumberConstants.N_ONE,
                new BasicThreadFactory.Builder()
                        .namingPattern("JlaMainThread-%d").build()));

    /**
     * Description:<b>JLA启动器</b>
     * @author Ralph
     * @since 上午9:49:35
     * @param args
     *              运行时参数
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        JdbcLogsAnalyser jdbcLogFilesAnalyser = new JdbcLogsAnalyser();
        jdbcLogFilesAnalyser.analyseJdbcLogFile();
    }

    /**
     * Description:<b>开始分析Jdbc日志文件</b>
     * @author Ralph
     * @since 2018年10月16日 下午3:46:32
     * @throws IOException
     *              IOException
     */
    private void analyseJdbcLogFile() throws IOException {
        logger.info("分析开始...");
        long startTime = System.currentTimeMillis();
        ConfigLoader configLoader = new ConfigLoader();

        String jdbcLogFilesPath = configLoader
                .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_DIRECTORYPATH);
        if (StringUtils.isBlank(jdbcLogFilesPath)) {
            logger.error("目标Jdbc日志文件夹路径为空！请检查config.properties文件中的jdbclogfiles.directoryPath配置项！");
            return;
        }
        File jdbcFile = new File(jdbcLogFilesPath);
        if (!jdbcFile.exists()) {
            logger.error(
                "目标Jdbc日志文件夹{}不存在！请检查config.properties文件中的jdbclogfiles.directoryPath配置项！",
                jdbcFile.getName());
            return;
        }
        List<File> analysingDirectoryList = new ArrayList<File>();
        File[] jdbcLogFiles = jdbcFile.listFiles();
        for (File jdbcLogFile : jdbcLogFiles) {
            if (jdbcLogFile.isDirectory()) {
                continue;
            }
            if (!StringUtils.equalsIgnoreCase(
                FilenameUtils.getExtension(jdbcLogFile.getAbsolutePath()),
                JlaConstants.C_SUFFIX_FILE_LOG)) {
                continue;
            }
            if (!StringUtils.containsIgnoreCase(
                FilenameUtils.getBaseName(jdbcLogFile.getAbsolutePath()),
                JlaConstants.C_SUFFIX_FILE_JDBC)
                    && !StringUtils.containsIgnoreCase(FilenameUtils
                            .getBaseName(jdbcLogFile.getAbsolutePath()),
                        JlaConstants.C_SUFFIX_FILE_SQLONLYFILE)) {
                continue;
            }
            File analysingDirectory = generateAnalysingDirectory(jdbcLogFile);
            //没被过滤、待分析的JDBC日志文件生成的Analysing目录集合
            analysingDirectoryList.add(analysingDirectory);
            analyserCompletionService.submit(new JlaMainTask(
                    analysingDirectory, jdbcLogFile, configLoader));
        }
        Long processTimeout = 1800L;
        String processTimeoutConfigValue = configLoader
                .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_PROCESSTIMEOUT);
        if (StringUtils.isNotBlank(processTimeoutConfigValue)) {
            processTimeout = Long.valueOf(processTimeoutConfigValue);
        }
        for (int i = JlaNumberConstants.N_ZERO; i < analysingDirectoryList
                .size(); i++) {
            try {
                Future<Boolean> future = analyserCompletionService.take();
                future.get(processTimeout,
                    TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("[JdbcAnalyser]获取线程结果出错......", e);
            } catch (ExecutionException e) {
                logger.error("[JdbcAnalyser]获取线程结果出错......", e);
            } catch (TimeoutException e) {
                logger.error("[JdbcAnalyser]获取线程结果超时......", e);
            }
        }
        generateAnalysedDirectory(analysingDirectoryList);
        
        long endTime = System.currentTimeMillis();
        logger.info("分析结束,本次共分析了{}个文件,耗时{}ms({}s,{}min)...",
            analysingDirectoryList.size(),
            (endTime - startTime), new BigDecimal((endTime - startTime)
                    / JlaNumberConstants.N_THUSAND)
                    .setScale(JlaNumberConstants.N_TWO), new BigDecimal(
                    (endTime - startTime)
                            / JlaNumberConstants.N_SIXTY_THUSAND)
                    .setScale(JlaNumberConstants.N_TWO));
    }

    /**
     * Description:<b>生成Analysed报告目录</b>
     * 
     * @author Ralph
     * @since 2018年10月17日 上午11:25:46
     * @param analysingDirectoryList
     *              Analysing报告目录
     */
    private void generateAnalysedDirectory(List<File> analysingDirectoryList) {
        for (File analysingDirectory : analysingDirectoryList) {
            String analysedFileSuffix = StringUtils.substringAfter(
                analysingDirectory.getName(),
                JlaConstants.C_PREFIX_REPORT_ANALYSING);
            File analysedDirectory = new File(
                    analysingDirectory.getParentFile(),
                JlaConstants.C_PREFIX_REPORT_ANALYSED + analysedFileSuffix);
            if (analysedDirectory.exists()) {
                try {
                    FileUtils.forceDelete(analysedDirectory);
                } catch (IOException e) {
                    logger.error("删除已存在的分析报告{}失败......",
                        analysingDirectory.getName(), e);
                }
            }
            try {
                FileUtils.moveDirectory(analysingDirectory, analysedDirectory);
            } catch (IOException e) {
                logger.error("重命名报告文件夹{}失败......", analysingDirectory.getName(),
                    e);
            }
        }
    }

    /**
     * Description:<b>生成Analysing报告目录</b>
     * @author Ralph
     * @since 2018-4-26 下午5:35:53
     * @param jdbcLogFile
     *              目标JDBC日志文件
     * @return
     *              Analysing报告目录对象
     * @throws IOException
     *              IOException
     */
    private File generateAnalysingDirectory(
            File jdbcLogFile) throws IOException {
        StringBuffer analysingDirectoryName = new StringBuffer(
                JlaConstants.C_PREFIX_REPORT_ANALYSING);
        analysingDirectoryName.append(
            FilenameUtils.getBaseName(jdbcLogFile.getAbsolutePath()));
        File analysingDirectory = new File(jdbcLogFile.getParentFile(),
                analysingDirectoryName.toString());
        FileUtils.deleteQuietly(analysingDirectory);
        FileUtils.forceMkdir(analysingDirectory);
        return analysingDirectory;
    }
}
