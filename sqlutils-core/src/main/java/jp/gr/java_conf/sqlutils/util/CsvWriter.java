package jp.gr.java_conf.sqlutils.util;

import java.io.IOException;
import java.io.Writer;

public /*abstract*/ class CsvWriter {

	private String delimiter = ",";
	private String newline = "\n";


	private Writer writer;

	/**
	 * @param writer
	 * Fileの場合はBufferedWriterを、
	 * メモリ上で変数として扱いたい場合はStringWriterを、
	 * （Wicketを使ってるならorg.apache.wicket.util.io.StringBufferWriterがおすすめ）
	 *
	 */
	public CsvWriter(Writer writer) {
		this.writer = writer;
	}

	public CsvWriter(Writer sb, String delimiter, String newline) {
		this.writer = sb;
		this.delimiter = delimiter;
		this.newline = newline;
	}


	public CsvWriter writeLine(String... strs) {
		for (int i = 0; i < strs.length; i++) {
			if (i != (strs.length - 1))
				write(strs[i], true);
			else
				write(strs[i], false);
		}
		newline();
		return this;
	}


	public CsvWriter write(String val) {
		return write(val, true);
	}

	public CsvWriter write(String val, boolean addDelimiter) {
		try {
			writer.append(val);
			if (addDelimiter)
				writer.append(delimiter);
			return this;
		} catch (IOException e) {
			closeQuietly(writer);
			throw new RuntimeException(e);
		}
	}

	public CsvWriter newline() {
		try {
			writer.append(newline);
			return this;
		} catch (IOException e) {
			closeQuietly(writer);
			throw new RuntimeException(e);
		}
	}


	public void close() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			closeQuietly(writer);
			throw new RuntimeException(e);
		}
	}

    private void closeQuietly(Writer output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

}
