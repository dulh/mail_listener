package com.vng.esb.action;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.DirectChannel;

import com.vng.esb.consts.Constants;
import com.vng.esb.service.EmailReceiverService;


public class MailListenerAction extends AbstractActionLifecycle {
    protected static final Logger logger = LogManager.getLogger(MailListenerAction.class);
    protected ConfigTree config;
    private static Boolean isTriggered = false;
    
    public MailListenerAction(ConfigTree config) {
        this.config = config;
    }
    
    public Message process(Message message) {
        final String method = "============== ProcessPMIssue =================";
        logger.info(method + Constants.BEGIN_METHOD);
        
        try {
            synchronized (isTriggered) {
                if(isTriggered == false) {
                    final ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-beans.xml");
                    
                    // mail for IS
                    DirectChannel isReceiverChannel = (DirectChannel) ctx.getBean("isRecieveEmailChannel");
                    EmailReceiverService isMailService = (EmailReceiverService) ctx.getBean("isEmailReceiverService");
                    
                    if (isReceiverChannel != null) {
                        isReceiverChannel.subscribe(isMailService);
                    }
                    
                    isTriggered = true;
                } else {
                    logger.info("This service is running !");
                }
            }
        } catch (Exception e) {
            logger.error("Errors when trigger mail listener ", e);
        } finally {
            logger.info(method + Constants.END_METHOD);
        }
        
        return message;
    }
}
