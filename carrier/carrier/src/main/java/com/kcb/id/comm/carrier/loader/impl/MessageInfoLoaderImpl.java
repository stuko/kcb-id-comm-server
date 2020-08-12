package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.MessageInfoLoader;
import com.kcb.id.comm.carrier.parser.MessageInfoParser;

@Component
@Scope("prototype")
public class MessageInfoLoaderImpl extends LoaderImpl  implements MessageInfoLoader {

	static Logger logger = LoggerFactory.getLogger(MessageInfoLoaderImpl.class);
	@Autowired
	MessageInfoParser parser;
	Map<String, MessageInfo> messageRepository = new HashMap<>();

	public MessageInfoParser getParser() {
		return parser;
	}

	public void setParser(MessageInfoParser parser) {
		this.parser = parser;
	}

	public Map<String, MessageInfo> getMessageRepository() {
		return messageRepository;
	}

	public void setMessageRepository(Map<String, MessageInfo> messageRepository) {
		this.messageRepository = messageRepository;
	}

	@Override
	public void parseMe(NodeList nodeList){
		try {
			List<MessageInfo> list = this.getParser().parse(nodeList);
			if (list != null && list.size() > 0) {
				list.forEach(message->{
					logger.debug(" #### Message's forward : {} " , message.getForward());
					this.getMessageRepository().put(message.getName(),message);
				});
			} 
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}

}
