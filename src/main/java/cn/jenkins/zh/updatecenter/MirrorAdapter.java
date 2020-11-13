package cn.jenkins.zh.updatecenter;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.ClassParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Iterator;

public class MirrorAdapter {
    @Option(name = "-official-json", usage = "Official JSON file which comes from http://mirrors.jenkins.io/updates/update-center.json", required = true)
    public File officialJSON = null;

    @Option(name = "-connection-check-url", usage = "The URL which allows to check the connection.", required = true)
    public String connectionCheckURL;

    @Option(name = "-mirror-url", usage = "For example https://mirrors.tuna.tsinghua.edu.cn/jenkins/", required = true)
    public String mirrorURL;

    @Option(name = "-mirror-provider", usage = "The provider of mirror service")
    public String mirrorProvider;

    @Option(name = "-mirror-json", usage = "Official JSON file which comes from http://mirrors.jenkins.io/updates/update-center.json", required = true)
    public File mirrorJSON = null;

    @Option(name = "-tools", usage = "If sign the tools metadata data")
    public boolean signTools = false;

    private Signer signer = new Signer();

    private int run(String[] args) throws CmdLineException, IOException, GeneralSecurityException {
        CmdLineParser p = new CmdLineParser(this);
        new ClassParser().parse(signer, p);

        p.parseArgument(args);

        return signTools ? signTools() : signUpdateCenterJSON();
    }

    private int signTools() throws CmdLineException, GeneralSecurityException, IOException {
        // make sure the output directory exists
        File mirrorDir = mirrorJSON.getParentFile();
        if (!mirrorDir.isDirectory()) {
            boolean dirCreating = mirrorDir.mkdirs();
            if (!dirCreating) {
                System.err.println(String.format("cannot create directory: %s", mirrorDir.getAbsolutePath()));
                return -1;
            }
        }
        // check whether officialJSON is a regular file
        if (!officialJSON.isFile()) {
            System.err.println(String.format("official json file is not a regular file: %s", officialJSON.getAbsolutePath()));
            return -2;
        }

        try(InputStream input = new FileInputStream(officialJSON);
            FileOutputStream out = new FileOutputStream(mirrorJSON);) {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            IOUtils.copy(input, data);

            String jsonStr = data.toString("UTF-8");
            jsonStr = jsonStr.substring(toolJSONHeader.length());
            jsonStr = jsonStr.substring(0, jsonStr.length() - toolJSONFooter.length());
            JSONObject json = JSONObject.fromObject(jsonStr);

            json.remove("signature"); // to regenerate the new signature

            JSONObject result = signer.sign(json);
            out.write((toolJSONHeader + result.toString(2, 2) + toolJSONFooter).getBytes());
        }
        return 0;
    }

    private final String toolJSONHeader = "<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html;charset=UTF-8' /></head><body><script>window.onload = function () { window.parent.postMessage(JSON.stringify(\n";
    private final String toolJSONFooter = "\n),'*'); };</script></body></html>";

    private int signUpdateCenterJSON() throws IOException, GeneralSecurityException, CmdLineException {
        // make sure the output directory exists
        File mirrorDir = mirrorJSON.getParentFile();
        if (!mirrorDir.isDirectory()) {
            boolean dirCreating = mirrorDir.mkdirs();
            if (!dirCreating) {
                System.err.println(String.format("cannot create directory: %s", mirrorDir.getAbsolutePath()));
                return -1;
            }
        }
        // check whether officialJSON is a regular file
        if (!officialJSON.isFile()) {
            System.err.println(String.format("official json file is not a regular file: %s", officialJSON.getAbsolutePath()));
            return -2;
        }

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try(InputStream input = new FileInputStream(officialJSON);
            FileOutputStream out = new FileOutputStream(mirrorJSON);) {
            IOUtils.copy(input, data);

            String jsonStr = data.toString("UTF-8");
            jsonStr = jsonStr.substring(jsonHeader.length());
            jsonStr = jsonStr.substring(0, jsonStr.length() - jsonFooter.length());
            JSONObject json = JSONObject.fromObject(jsonStr);

            // replace coreWarUrl
            JSONObject core = json.getJSONObject("core");
            String coreUrl = core.getString("url");
            coreUrl = coreUrl.replaceAll("https://updates.jenkins.io/download/", mirrorURL);
            core.put("url", coreUrl);

            json.put("connectionCheckUrl", connectionCheckURL);
            JSONObject plugins = json.getJSONObject("plugins");
            json.remove("signature"); // to regenerate the new signature
            Iterator it = plugins.keySet().iterator();
            while(it.hasNext()) {
                JSONObject plugin = plugins.getJSONObject(it.next().toString());
                String url = plugin.getString("url");
                url = url.replaceAll("https://updates.jenkins.io/download/", "https://updates.jenkins-zh.cn/jenkins/");
                url = url + "?provider=" + mirrorProvider;
                plugin.put("url", url);
            }

            JSONObject result = signer.sign(json);
            out.write((jsonHeader + result.toString() + jsonFooter).getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("Error happened when parse json file from '" + officialJSON + "', data is: " + data.toString());
        }
        return 0;
    }

    private final String jsonHeader = "updateCenter.post(\n";
    private final String jsonFooter = "\n);";

    public static void main(String[] args) throws Exception {
        MirrorAdapter adapter = new MirrorAdapter();
        System.exit(adapter.run(args));
    }
}
