package org.jax.isopret.html;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


public class HtmlTemplate {
    /** Map of data that will be used for the FreeMark template. */
    protected final Map<String, Object> templateData;
    /** FreeMarker configuration object. */
    protected final Configuration cfg;

    protected static final String EMPTY_STRING="";

    private final String outpath = "prositometry.html";

    public HtmlTemplate(Map<String, Object> data) {
        this.templateData = data;
        this.cfg = new Configuration(new Version("2.3.23"));
        cfg.setDefaultEncoding("UTF-8");
        ClassLoader classLoader = HtmlTemplate.class.getClassLoader();
        cfg.setClassLoaderForTemplateLoading(classLoader, "");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String date = dtf.format(now);
        this.templateData.put("analysis_date", date);
    }


    public void outputFile() {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(this.outpath))) {
            Template template = cfg.getTemplate("prositometry.ftl");
            template.process(templateData, out);
        } catch (TemplateException | IOException te) {
            te.printStackTrace();
        }
    }
}
