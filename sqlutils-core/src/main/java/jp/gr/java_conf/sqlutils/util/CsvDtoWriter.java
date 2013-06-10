package jp.gr.java_conf.sqlutils.util;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.gr.java_conf.sqlutils.core.dto.IDto;

public class CsvDtoWriter<T extends IDto> extends CsvWriter {


	private List<IStringProvider> providers;


	public CsvDtoWriter(Writer sb) {
		super(sb);
	}

	public CsvDtoWriter(Writer sb, String delimiter, String newline) {
		super(sb, delimiter, newline);
	}


	public CsvDtoWriter<T> setProviders(IStringProvider...p) {
		providers = new ArrayList<IStringProvider>();
		providers.addAll(Arrays.asList(p));
		return this;
	}

	public CsvDtoWriter<T> addProvider(IStringProvider p) {
		if (providers == null)
			providers = new ArrayList<IStringProvider>();
		providers.add(p);
		return this;
	}


	public CsvDtoWriter<T> writeHeaders() {
		for (int i = 0; i < providers.size(); i++) {
			String h = providers.get(i).getHeaderString();
			if (i != (providers.size() - 1))
				write(h, true);
			else
				write(h, false);
		}
		newline();
		return this;
	}

	public CsvDtoWriter<T> writeCols(T dto) {
		for (int i = 0; i < providers.size(); i++) {
			IStringProvider col = providers.get(i);
			String v = col.getValueString(dto);
			if (i != (providers.size() - 1))
				write(v, true);
			else
				write(v, false);
		}
		newline();
		return this;
	}
}
