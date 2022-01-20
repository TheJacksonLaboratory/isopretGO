package org.jax.isopret.core.html;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Deprecated
public class HtmlTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlTemplate.class);
    /** Map of data that will be used for the FreeMark template. */
    private final Map<String, Object> templateData;
    /** FreeMarker configuration object. */
    private final Configuration cfg;

    protected static final String EMPTY_STRING="";

    private final String outpath;

    /**
     *
     * @param data data for freemarker template.
     * @param outFileName Name of output file (without .html file ending)
     */
    public HtmlTemplate(Map<String, Object> data, String outFileName) {
        this.templateData = data;
        this.outpath = outFileName + ".html";
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
            Template template = cfg.getTemplate("main/resources/isopret.ftl");
            template.process(templateData, out);
        } catch (TemplateException | IOException te) {
            LOGGER.error(te.getMessage());
        }
    }
}
