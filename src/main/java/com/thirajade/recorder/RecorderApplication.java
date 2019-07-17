package com.thirajade.recorder;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.thirajade.recorder.config.ApplicationContextProvider;
import com.thirajade.recorder.entities.Record;
import com.thirajade.recorder.services.record.RecordService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class RecorderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecorderApplication.class, args);
	}


    @EventMapping
    public Message handleTextMessage(MessageEvent<TextMessageContent> event) {
        RecordService recordService = this.getRecordService();
        Record record = recordService.createRecord(event);

        TextMessageContent message = new TextMessageContent(event.getSource().getSenderId(), "บันทึก transaction " + record.getTitle() + event.getMessage().getId() + " เรียบร้อย");
        return new TextMessage(message.getText());
    }

    @EventMapping
    public Message handleDefaultMessageEvent(Event event) {
	    RecordService recordService = this.getRecordService();
        recordService.createRecord(event);

        TextMessageContent message = new TextMessageContent(event.getSource().getSenderId(), "บันทึก transaction image เรียบร้อย");
        return new TextMessage(message.getText());
    }

    public RecordService getRecordService() {
        return ApplicationContextProvider.getApplicationContext().getBean(RecordService.class);
    }
}
