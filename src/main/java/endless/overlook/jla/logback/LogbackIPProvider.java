package endless.overlook.jla.logback;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.PropertyDefinerBase;

import endless.overlook.jla.constants.JlaSymbolConstants;

/**
 * Description:<b>Logback日志文件IP属性生成器</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:23:54
 */
public class LogbackIPProvider extends PropertyDefinerBase {

    /** 日志记录对象 **/
    private static final Logger logger = LoggerFactory
            .getLogger(LogbackIPProvider.class);

    /** 默认值 **/
    private final static String DEFAULT_VALUE = "IP";

    /**
     * Description:<b>获取Logback日志文件IP属性</b>
     * @author Ralph
     * @since 下午10:37:56
     * @return
     *              Logback日志文件IP属性
     */
    @Override
    public String getPropertyValue() {
        Enumeration<NetworkInterface> netInterfaces = null;
        List<String> ipList = new ArrayList<String>();
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress inet = ips.nextElement();
                    if (inet instanceof Inet4Address
                            && (!inet.isLoopbackAddress())) {
                        ipList.add(inet.getHostAddress());
                    }
                }
            }
            if (CollectionUtils.isEmpty(ipList)) {
                InetAddress addr = InetAddress.getLocalHost();
                ipList.add(addr.getHostAddress());
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("读取到本机IP地址为【%s】",
                        addr.getHostAddress()));
                }
            }
            return StringUtils.join(ipList.toArray(),
                JlaSymbolConstants.C_SYMBOL_UNDERLINE);
        } catch (Exception e) {
            logger.error("获取本级网络IP地址出现异常......", e);
        }
        return DEFAULT_VALUE;
    }
}
