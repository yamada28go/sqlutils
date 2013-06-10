package jp.gr.java_conf.sqlutils.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.IDto;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

import org.apache.commons.lang.ArrayUtils;

public interface IStringProvider {

	String getHeaderString();
	String getValueString(IDto dto);


	public abstract static class AbstractProvider<T extends IDto> implements IStringProvider {

		protected String header;
		protected String nullValue;

		protected abstract String dto2String(T dto);

		public AbstractProvider() {
			this("", "");
		}

		public AbstractProvider(String header) {
			this(header, "");
		}

		public AbstractProvider(String header, String nullValue) {
			this.header = header;
			this.nullValue = nullValue;
		}

		@Override
		public String getHeaderString() {
			return header;
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getValueString(IDto dto) {
			String v = dto2String((T) dto);
			if (v != null)
				return v;
			else
				return nullValue;
		}
	}


	public static class DefaultProvider implements IStringProvider {

		protected String header;
		protected IColumn<?> col;

		public DefaultProvider(IColumn<?> col) {
			this(col, col.name());
		}

		public DefaultProvider(IColumn<?> col, String header) {
			this.col = col;
			this.header = header;
		}

		@Override
		public String getHeaderString() {
			return quoteString(header, String.class);
		}

		@Override
		public String getValueString(IDto dto) {
			return quoteString(dto2String(dto, col), col.getDataType());
		}

		protected String dto2String(IDto dto, IColumn<?> col) {
			try {
				Class<?> type = col.getDataType();
				Object value = DtoUtil.get(dto, col);

				if (value == null)
					return getNullString();

				if (type == Number.class)
					return new DecimalFormat(getNumberFormat()).format(value);

				if (type == java.util.Date.class)
					return new SimpleDateFormat(getDateTimeFormat()).format(value);

				if (type == java.sql.Timestamp.class)
					return new SimpleDateFormat(getDateTimeFormat()).format(value);

				if (type == java.sql.Date.class)
					return new SimpleDateFormat(getDateFormat()).format(value);

				if (type == java.sql.Time.class)
					return new SimpleDateFormat(getTimeFormat()).format(value);

				return value.toString();

			} catch (NoSuchColumnException e) {
				throw new RuntimeException(e);
			}
		}

		protected String quoteString(String val, Class<?> type) {
			if (ArrayUtils.contains(getQuotedTypes(), type)) {
				return getQuoteString() + val + getQuoteString();
			} else {
				return val;
			}
		}

		protected String getNumberFormat() {
			return "#,##0";
		}

		protected String getDateTimeFormat() {
			return "yyyy/MM/dd HH:mm:ss";
		}

		protected String getDateFormat() {
			return "yyyy/MM/dd";
		}

		protected String getTimeFormat() {
			return "HH:mm:ss";
		}

		protected String getNullString() {
			return "";
		}

		protected String getQuoteString() {
			return "\"";
		}

		protected Class<?>[] getQuotedTypes() {
			return new Class<?>[] {
				String.class,
				Number.class,
				java.util.Date.class,
				java.sql.Timestamp.class,
				java.sql.Date.class,
				java.sql.Time.class,
			};
		}
	}
}
