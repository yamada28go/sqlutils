package jp.gr.java_conf.sqlutils.generator.dto.config;

import javax.xml.bind.annotation.XmlAttribute;

public class SequenceRelation {

	@XmlAttribute(name="name")
	public String name;

	public void validate(String pos) {
		Config.CheckRequired(name, pos + "/sequenceRelation@name");
	}

	public String toString() {
		return name;
	}
}
