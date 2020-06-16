package cn.tedu.store.controller.ex;

/**
 * 上传时文件状态异常，例如上传过程中源文件被移除，导致源文件不可被访问
 */
public class FileUploadStateException extends FileUploadException {

	public FileUploadStateException() {
		super();
	}

	public FileUploadStateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FileUploadStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileUploadStateException(String message) {
		super(message);
	}

	public FileUploadStateException(Throwable cause) {
		super(cause);
	}

}
