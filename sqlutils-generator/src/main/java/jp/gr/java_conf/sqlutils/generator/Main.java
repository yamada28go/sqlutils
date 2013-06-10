package jp.gr.java_conf.sqlutils.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import jp.gr.java_conf.sqlutils.generator.common.NameResolver.Camelize;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.CamelizeAndCapitalize;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.ReverseCamelizeToLower;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.ReverseCamelizeToUpper;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.Specified;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.ToLower;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.ToUpper;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.Void;
import jp.gr.java_conf.sqlutils.generator.dto.DtoGenerator;
import jp.gr.java_conf.sqlutils.generator.dto.config.Config;
import jp.gr.java_conf.sqlutils.generator.valueenum.EnumGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);


	private static final String DEFAULT_PROPERTIES_NAME = "generator.properties.xml";

	/**
	 * @param args 0:properties-xml-fils's path
	 * @throws FileNotFoundException if specified by argument
	 */
	public static void main(String[] args) throws FileNotFoundException {

		if (args.length > 1) {
			throw new RuntimeException("arguments error.");
		}

		// 設定ファイル
		InputStream properties;
		if (args.length == 1) {
			properties = new FileInputStream(new File(args[0]));
		} else {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties = loader.getResourceAsStream(DEFAULT_PROPERTIES_NAME);
		}

//		// 設定ファイルは、引数で指定するか、指定が無い場合はクラスパス上の"generator.properties.xml"を読み込む
//		String path = (args.length == 1) ? args[0] : "D:\\works\\sqlutils-generator\\src\\test\\resources\\dtogenerator.properties.xml";
//		//String path = (args.length == 1) ? args[0] : "generator.properties.xml";
//
//		ClassLoader loader = Thread.currentThread().getContextClassLoader();
//		InputStream is = loader.getResourceAsStream("generator.properties.xml");
//
//		File configFile = new File(path);

//		if (!configFile.exists()) {
//			logger.error("missing config-file : " + path);
//		} else {
			try {
				// JAXBでXML→Object変換
				JAXBContext jc = JAXBContext.newInstance(
						Config.class,
						Void.class,
						Specified.class,
						Camelize.class,
						CamelizeAndCapitalize.class,
						ToUpper.class,
						ToLower.class,
						ReverseCamelizeToUpper.class,
						ReverseCamelizeToLower.class
						);
//				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//				documentBuilderFactory.setNamespaceAware(true);
//				DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
//			    Document doc = db.parse(new File(path));
			    Unmarshaller um = jc.createUnmarshaller();
			    Config config = (Config)um.unmarshal(properties);
//				Config config = JAXB.unmarshal(new File(path), Config.class);

				// 設定内容の妥当性をチェック
				config.validate();


				if (config.enumGenerator != null)
					new EnumGenerator(config).generate();

				if (config.dtoGenerator != null)
					new DtoGenerator(config).generate();

				logger.debug("OK : ");

			} catch (Exception e) {
				logger.error("fail to parse config-file:" +
					((args.length == 1) ? args[0] : DEFAULT_PROPERTIES_NAME), e);
			}
//		}
	}


}
