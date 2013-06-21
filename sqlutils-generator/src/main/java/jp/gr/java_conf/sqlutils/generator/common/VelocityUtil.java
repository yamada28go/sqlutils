package jp.gr.java_conf.sqlutils.generator.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class VelocityUtil {


	public static void initVelocity() throws Exception {
		Properties p = new Properties();
//      p.setProperty("resource.loader", "FILE");
//      p.setProperty("FILE.resource.loader.class",
//          "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
////      p.setProperty("FILE.resource.loader.path", currentPath);

		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
			"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		p.setProperty("input.encoding", "UTF-8");
		p.setProperty("output.encoding", "UTF-8");

		p.setProperty("runtime.references.strict", "true");
		Velocity.init(p);
	}

	public static Template getTemplate(Object baseClass, String file) {
		String path = baseClass.getClass().getPackage().getName().replace('.', '/');
		path = path + "/" + file;
		return Velocity.getTemplate(path);
	}

	public static void writeToFile(Template template, VelocityContext context,
			File outputPath, String className) throws IOException {

		File file = new File(outputPath, className + ".java");
		FileOutputStream fo = new FileOutputStream(file);
		OutputStreamWriter w = new OutputStreamWriter(fo, "UTF-8");
		BufferedWriter bw = new BufferedWriter(w);
		template.merge(context, bw);
		bw.close();
	}
}
