package endless.overlook.jla.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import endless.overlook.jla.constants.JlaConfigConstants;

/**
 * Description:<b>配置加载器</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:22:51
 */
public class ConfigLoader {
    /** 日志对象 **/
    private static final Logger logger = LoggerFactory
            .getLogger(ConfigLoader.class);

    /** 默认配置映射 **/
    private Map<String, String> defaultPropertiesMappings = new HashMap<String, String>();

    /**
     * Description:<b>无参构造函数</b>
     * @author Ralph
     * @since 2018-4-7 09:55:49
     */
    public ConfigLoader() {
        this.initialize();
    }

    /**
     * Description:<b>初始化配置管理器</b>
     * @author Ralph
     * @since 2018年1月31日 下午8:07:54
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void initialize() {
        logger.info("配置加载[开始]......");
        long startTime = System.currentTimeMillis();
        File externalConfigFile = new File("props/config.properties");
        if (externalConfigFile.exists()) {
            logger.info("外部配置文件读取[开始]......");
            FileInputStream externalConfigInputStream = null;
            BufferedInputStream bufferedExternalConfigInputStream = null;
            try {
                Properties externalConfigProperties = new Properties();

                externalConfigInputStream = new FileInputStream(
                        externalConfigFile);
                bufferedExternalConfigInputStream = new BufferedInputStream(
                        externalConfigInputStream);
                externalConfigProperties
                        .load(bufferedExternalConfigInputStream);

                Iterator externalConfigIterator = externalConfigProperties
                        .entrySet().iterator();
                while (externalConfigIterator.hasNext()) {
                    Entry<String, String> externalConfigEntry = (Entry<String, String>) externalConfigIterator
                            .next();
                    defaultPropertiesMappings.put(externalConfigEntry.getKey(),
                        externalConfigEntry.getValue());
                }
            } catch (IOException e) {
                logger.error("读取外部配置文件时失败......", e);
            } finally {
                IOUtils.closeQuietly(externalConfigInputStream);
                IOUtils.closeQuietly(bufferedExternalConfigInputStream);
            }
            logger.info("外部配置文件读取[结束]......");
        } else {
            logger.info("默认配置文件读取[开始]......");
            //加载所有配置
            ResourceBundle defaultPropertiesBundle = ResourceBundle
                    .getBundle(JlaConfigConstants.C_NAME_JLA_PATH_CONFIG);
            Enumeration<String> defaultPropertiesKeys = defaultPropertiesBundle
                    .getKeys();
            while (defaultPropertiesKeys.hasMoreElements()) {
                String defaultPropertiesKey = defaultPropertiesKeys
                        .nextElement();
                defaultPropertiesMappings.put(defaultPropertiesKey,
                    defaultPropertiesBundle.getString(defaultPropertiesKey));
            }
            logger.info("默认配置文件读取[结束]......");
        }

        long endTime = System.currentTimeMillis();
        logger.info("配置加载[结束]......耗时[{}]ms......", endTime - startTime);
    }

    /**
     * Description:<b>根据配置键获取配置值</b>
     * @author Ralph
     * @since 2018年1月31日 下午8:08:08
     * @param configKey
     *              配置键
     * @return
     *              配置键对应的配置值
     */
    public String getConfig(String configKey) {
        return StringUtils
                .trimToEmpty(defaultPropertiesMappings.get(configKey));
    }

    /**
     * Description:<b>判断当前配置值是否有效</b>
     * @author Ralph
     * @since 2018年1月31日 下午9:55:39
     * @param configKey
     *              配置Key
     * @param destinationValue
     *              目标值
     * @return
     *              当前配置值是否有效
     */
    public boolean validateConfigValueValid(String configKey,
            String destinationValue) {
        return StringUtils.equalsIgnoreCase(getConfig(configKey),
            StringUtils.trimToEmpty(destinationValue));
    }
}
