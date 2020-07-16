package main;

import net.sf.json.JSONObject;
import org.jing.core.lang.Carrier;
import org.jing.core.lang.ExceptionHandler;
import org.jing.core.lang.JingException;
import org.jing.core.logger.JingLogger;
import org.jing.core.util.CarrierUtil;
import org.jing.core.util.FileUtil;
import org.jing.core.util.GenericUtil;
import org.jing.core.util.StringUtil;
import org.jing.decrypter.skeleton2Json.Arknights;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.Exception;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-07-15 <br>
 */
public class OpenFileChooser4Skel2Json {
    private static final JingLogger LOGGER = JingLogger.getLogger(OpenFileChooser4Skel2Json.class);

    private static final String CACHE_FILE_NAME = "cache.xml";
    
    private static final String CAHCE_DIR_ARKNIGHTS = "last-directory-path-arknights";

    private OpenFileChooser4Skel2Json() throws JingException {
        try {
            LOGGER.imp("Please choose function:");
            LOGGER.imp("1: Arknights");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String choose = br.readLine();
            br.close();
            if ("1".equals(choose)) {
                operation4Arknights();
            }
            System.exit(1);
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
        }
    }

    private void operation4Arknights() throws Exception {
        File cacheFile = new File(CACHE_FILE_NAME);
        Carrier cacheCarrier;
        if (!cacheFile.exists() || cacheFile.isDirectory()) {
            cacheCarrier = new Carrier();
            LOGGER.imp("No cache file found, create a new cache Carrier.");
        }
        else {
            cacheCarrier = CarrierUtil.string2Carrier(FileUtil.readFile(cacheFile.getAbsolutePath()));
            LOGGER.imp("A cache file found.");
        }
        String doing = cacheCarrier.getString("doing", "N");
        if ("Y".equalsIgnoreCase(doing)) {
            LOGGER.imp("Duplicate program running, try to close old one or edit the {} file, make [doing] to 'N'",
                cacheFile);
            return;
        }
        String lastDirectoryPath = cacheCarrier.getString(CAHCE_DIR_ARKNIGHTS, "");
        LOGGER.imp("[lastDirectoryPath: {}]", lastDirectoryPath);
        File directory = new File(lastDirectoryPath);
        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        JFileChooser fileChooser = new JFileChooser(directory);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileFilter() {
            @Override public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".skel") || f.getName().endsWith(".skel.txt");
            }
            @Override public String getDescription() {
                return null;
            }
        });
        if (StringUtil.isNotEmpty(lastDirectoryPath)) {
            fileChooser.setSelectedFile(directory);
        }
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
            File[] files = fileChooser.getSelectedFiles();
            int count = GenericUtil.countArray(files);
            if (0 == count) {
                LOGGER.imp("No file selected.");
            }
            else {
                String outPutDir = 1 == count ? files[0].getAbsolutePath() : files[0].getParentFile().getAbsolutePath();
                cacheCarrier.setValueByKey(CAHCE_DIR_ARKNIGHTS, outPutDir);
                updateCacheCarrier(cacheCarrier);
                for (int i$ = 0; i$ < count; i$++) {
                    operate4Each(files[i$], outPutDir);
                }
            }
        }
    }

    private void updateCacheCarrier(Carrier cacheCarrier) throws JingException {
        writeFile(CACHE_FILE_NAME, cacheCarrier.asXML());
    }

    private void writeFile(String filePath, String content) throws JingException {
        BufferedWriter bw = null;
        try {
            LOGGER.imp("Try to output to file: {}", filePath);
            File outPutFile = new File(filePath);
            if (!outPutFile.exists() || !outPutFile.isFile()) {
                File outPutDir = outPutFile.getParentFile();
                if (null != outPutDir) {
                    LOGGER.imp("Try to check output directory: {}", outPutDir.getAbsolutePath());
                    LOGGER.imp("[exists: {}]", outPutDir.exists());
                    LOGGER.imp("[isDirectory: {}]", outPutDir.isDirectory());
                    LOGGER.imp("[createDirectory: {}]", outPutDir.mkdirs());
                }
                LOGGER.imp("[createNewFile: {}]", outPutFile
                    .createNewFile());
            }
            bw = new BufferedWriter(new FileWriter(outPutFile));
            bw.write(content);
            bw.flush();
        }
        catch (Exception e) {
            ExceptionHandler.publish(e);
        }
        finally {
            if (null != bw) {
                try {
                    bw.close();
                }
                catch (Exception e) {
                    ExceptionHandler.publish(e);
                }
                finally {
                    bw = null;
                }
            }
        }
    }

    private void operate4Each(File file, String outPutDir) throws JingException {
        if (null == file) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i$ = 0, count = GenericUtil.countArray(files); i$ < count; i$++) {
                if (files[i$].getName().endsWith(".skel") || files[i$].getName().endsWith(".skel.txt")) {
                    operate4Each(files[i$], outPutDir);
                }
            }
        }
        else {
            LOGGER.imp("Operating file: {}", file.getAbsolutePath());
            try {
                Arknights decrypter = new Arknights(file);
                decrypter.decrypter();
                JSONObject resultJson = decrypter.getRetJson();
                String fileName = file.getName();
                fileName = fileName.replace(".skel.txt", ".json");
                fileName = fileName.replace(".skel", ".json");
                writeFile(outPutDir + File.separator + "output" + File.separator + fileName, resultJson.toString());
                LOGGER.imp("Success to decrypter file: {}", file.getName());
            }
            catch (Exception e) {
                LOGGER.error(StringUtil.getErrorStack(e));
                LOGGER.imp("Failed to decrypter file: {}", file.getName());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new OpenFileChooser4Skel2Json();
    }
}
