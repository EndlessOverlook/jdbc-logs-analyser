package endless.overlook.jla.logback;

import ch.qos.logback.core.PropertyDefinerBase;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * Description:<b>Logback日志文件存放根路径属性生成器</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:23:44
 */
public class LogbackBaseHomeProvider extends PropertyDefinerBase {

    /**
     * Description:<b>获取Logback日志文件存放根路径属性</b>
     * @author Ralph
     * @since 下午10:36:24
     * @return
     *              Logback日志文件存放根路径属性
     */
    @Override
    public String getPropertyValue() {
        File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
        return desktopDir.getAbsolutePath();
    }

}
