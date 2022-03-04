package ca.bc.gov.open.cdds.test.services;

import com.eviware.soapui.tools.SoapUITestCaseRunner;
import java.io.*;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @Value("${test.username}")
    private String username;

    @Value("${test.password}")
    private String password;

    @Value("${test.api-host}")
    private String apiHost;

    public TestService() {}

    public void setAuthentication() throws IOException {
        InputStream template = getClass().getResourceAsStream("/CDDS-soapui-project-template.xml");
        Scanner scanner = new Scanner(template);

        File project = new File("./cdds-soapui-project.xml");
        if (project.exists()) {
            project.delete();
        }
        project.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter("./CDDS-soapui-project.xml"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("{AUTHENTICATION_USERNAME}")) {
                line = line.replaceAll("\\{AUTHENTICATION_USERNAME}", username);
            }
            if (line.contains("{AUTHENTICATION_PASSWORD}")) {
                line = line.replaceAll("\\{AUTHENTICATION_PASSWORD}", password);
            }
            if (line.contains("{API_HOST}")) {
                line = line.replaceAll("\\{API_HOST}", apiHost);
            }
            writer.append(line + "\n");
        }
        writer.flush();
        writer.close();
        scanner.close();
    }

    private File zipAndReturnErrors() throws IOException {
        File dir = new File(".");
        FileFilter fileFilter = new WildcardFileFilter("CDDSTestSuite*-FAILED.txt");
        File[] files = dir.listFiles(fileFilter);
        sanitizeErrorFiles(files);
        FileOutputStream fos = new FileOutputStream("TestErrors.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fOut = new File("TestErrors.zip");

        for (var fileToZip : files) {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            fileToZip.delete();
        }
        zipOut.close();
        fos.close();
        return fOut;
    }

    private void sanitizeErrorFiles(File[] files) throws IOException {
        for (File f : files) {
            String fileContent = FileUtils.readFileToString(f);
            fileContent = fileContent.replaceAll(username, "*".repeat(8));
            fileContent = fileContent.replaceAll(password, "*".repeat(8));
            FileUtils.write(f, fileContent);
        }
    }

    public File runAllTests() throws IOException {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile("CDDS-soapui-project.xml");
        try {
            runner.run();
            return null;
        } catch (Exception ignored) {
            return zipAndReturnErrors();
        }
    }
}
