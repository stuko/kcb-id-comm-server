package com.kcb.id.comm.carrier.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.common.StringUtils;
import com.kcb.id.comm.carrier.core.Carrier;
import com.kcb.id.comm.carrier.loader.ErrorInfoLoader;
import com.kcb.id.comm.carrier.loader.HeaderInfoLoader;
import com.kcb.id.comm.carrier.loader.MessageInfoLoader;

@Configuration
@Component
public class CarrierImpl implements Carrier {

	static Logger logger = LoggerFactory.getLogger(CarrierImpl.class);
	/*
	 * XML로 부터 여러 전문을 읽어서 MessageInfo 라는 객체에 채워주는 클래스 
	 */
	@Autowired
	MessageInfoLoader messageInfoLoader;
	@Autowired
	HeaderInfoLoader headerInfoLoader;
	@Autowired
	ErrorInfoLoader errorInfoLoader;
	
	
	/*
	 * MessageInfo XML 파일을 읽어올 경로 application.yml에 정의됨
	 */
	@Value( "${carrier.path.message}" )
	private String messagePath;
	@Value( "${carrier.path.header}" )
	private String headerPath;
	@Value( "${carrier.path.error}" )
	private String errorPath;

	/*
	 * 스프링의 어플리케이션 컨텍스트, 빈들을 참조하기 위한 용도
	 */
	@Autowired
	private ApplicationContext context;
	
	public String getMessagePath() {
		return messagePath;
	}
	public void setMessagePath(String messagePath) {
		this.messagePath = messagePath;
	}
	public HeaderInfoLoader getHeaderInfoLoader() {
		return headerInfoLoader;
	}
	public void setHeaderInfoLoader(HeaderInfoLoader headerInfoLoader) {
		this.headerInfoLoader = headerInfoLoader;
	}
	public ErrorInfoLoader getErrorInfoLoader() {
		return errorInfoLoader;
	}
	public void setErrorInfoLoader(ErrorInfoLoader errorInfoLoader) {
		this.errorInfoLoader = errorInfoLoader;
	}
	public String getHeaderPath() {
		return headerPath;
	}
	public void setHeaderPath(String headerPath) {
		this.headerPath = headerPath;
	}
	public String getErrorPath() {
		return errorPath;
	}
	public void setErrorPath(String errorPath) {
		this.errorPath = errorPath;
	}
	public MessageInfoLoader getMessageInfoLoader() {
		return messageInfoLoader;
	}
	public void setMessageInfoLoader(MessageInfoLoader messageInfoLoader) {
		this.messageInfoLoader = messageInfoLoader;
	}

	public void startAll() throws Exception {
		if(StringUtils.chkNull(this.getMessagePath())) {
			try {
				logger.debug("Message xml path exists... {}", this.getMessagePath());
				this.getMessageInfoLoader().setLoaderFilePath(this.getMessagePath());
			} catch (Exception e) {
				logger.error(e.toString(),e);
			}
			this.getMessageInfoLoader().load();
		}else throw new Exception("XMLMessageNotExistException");
		if(StringUtils.chkNull(this.getHeaderPath())) {
			try {
				logger.debug("Header xml path exists... {}", this.getHeaderPath());
				this.getHeaderInfoLoader().setLoaderFilePath(this.getHeaderPath());
			} catch (Exception e) {
				logger.error(e.toString(),e);
			}
			this.getHeaderInfoLoader().load();
		}else throw new Exception("XMLHeaderNotExistException");
		if(StringUtils.chkNull(this.getErrorPath())) {
			try {
				logger.debug("Error xml path exists... {}", this.getErrorPath());
				this.getErrorInfoLoader().setLoaderFilePath(this.getErrorPath());
			} catch (Exception e) {
				logger.error(e.toString(),e);
			}
			this.getErrorInfoLoader().load();
		}else throw new Exception("XMLErrorNotExistException");		
	}

	
}
