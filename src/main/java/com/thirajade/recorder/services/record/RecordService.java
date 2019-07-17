package com.thirajade.recorder.services.record;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.thirajade.recorder.entities.Record;
import com.thirajade.recorder.entities.Transaction;
import com.thirajade.recorder.repositories.RecordRepository;
import com.thirajade.recorder.services.outcome.MoneyService;
import com.thirajade.recorder.services.time.TimeService;
import com.thirajade.recorder.services.image.ImageService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordService {

    @Value("line.bot.channel-token")
    private String channelToken;

    private RecordRepository recordRepository;
    private ImageService imageService;
    private TimeService timeService;
    private MoneyService moneyService;

    public RecordService(RecordRepository recordRepository,
                         ImageService imageService,
                         TimeService timeService,
                         MoneyService moneyService) {
        this.recordRepository = recordRepository;
        this.imageService = imageService;
        this.timeService = timeService;
        this.moneyService = moneyService;
    }

    public Record createRecord(MessageEvent<TextMessageContent> event) {
        System.out.println("TextMessageContent = [" + event + "]");
        TextMessageContent textMessageContent = event.getMessage();
        String text = textMessageContent.getText();

        Record record = recordOfTransaction(text);
        if (record.getType().equals("enet")) {
            record.setTransactionDate(new Date(event.getTimestamp().toEpochMilli()));
        }

        recordRepository.save(record);
        System.out.println(record);

        return record;
    }

    public Record createRecord(Event event) {
        try {
            MessageEvent<ImageMessageContent> imageMessageContentMessageEvent = (MessageEvent<ImageMessageContent>) event;
            System.out.println("TextMessageContent = [" + imageMessageContentMessageEvent + "]");
            ImageMessageContent imageMessageContent = imageMessageContentMessageEvent.getMessage();

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://api.line.me/v2/bot/message/" + imageMessageContent.getId() + "/content")
                    .get()
                    .addHeader("Authorization", "Bearer " + channelToken)
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();
            System.out.println(response.body().contentLength());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Record recordOfTransaction(String text) {
        //do something here
        if (text.contains("ถอน/โอนเงิน")) {
            //ถอน/โอนเงิน 420.00บ บ/ชx055215 26/06@01:01 ใช้ได้5,910.40บ
            return withdrawTransaction(text);
        } else if (text.contains("ชำระเงิน")) {
            //ชำระเงิน 500 บ ผ่านENETจากบ/ช x055215 ใช้ได้ 1,354.62 บ
            return enetTransaction(text);
        } else if (text.contains("ขอบคุณที่ใช้จ่ายผ่านบัตร")) {
            //ขอบคุณที่ใช้จ่ายผ่านบัตร SCB x055215 ที่ AFTER YOU-101 THE TH จำนวน 635.00 บาท วันที่ 02/07
            return debitTransaction(text);
        } else if (text.contains("เงินโอน/เงินเดือน")) {
            //เงินโอน/เงินเดือน 37,282.42บ เข้าบ/ชx055215 25/04@03:39
            return salaryTransaction(text);
        } else if (text.contains("เช็คเข้า")) {
            //เช็คเข้า 8,730.00บ. เข้าบ/ช x055215 ผ่าน TELL ใช้ได้ 1,892.02บ.
            return checkInTransaction(text);
        } else if (text.contains("เงิน")) {
            //เงิน 8,730.00บ เข้าบ/ชx055215 17/06@13:02 ใช้ได้ 2,887.98บ
            return depositTransaction(text);
        } else {
            //17/07@12:42 100.00 จากBBLA/x076236เข้าx055215 ใช้ได้954.62บ
            return transferTransaction(text);
        }
    }

    public Record withdrawTransaction(String text) {

        Transaction transaction = new Transaction(text);

        Date transactionDate = timeService.convertDateWithFormat(transaction.getPart(3), "dd/MM@hh:mm");
        Double amount = moneyService.stripCommaAndUnit(transaction.getPart(1));

        return Record.builder()
                .bank("SCB")
                .type("withdraw/transfer")
                .title("SCB withdraw/transfer")
                .action(transaction.getPart(0))
                .outcome(amount)
                .description(text)
                .transactionDate(transactionDate)
                .createdAt(new Date())
                .build();
    }

    public Record enetTransaction(String text) {
        Transaction transaction = new Transaction(text);
        Double amount = moneyService.stripCommaAndUnit(transaction.getPart(1));
        return Record.builder()
                .bank("SCB")
                .type("enet")
                .title("SCB enet")
                .action(transaction.getPart(0))
                .outcome(amount)
                .description(text)
                .createdAt(new Date())
                .build();
    }

    public Record debitTransaction(String text) {
        //***special one cannot be locate each field within text.
        String[] parts = text.split(" ");

        //action
        String action = parts[0] + parts[1];

        //amount
        String amountInString = text.substring(text.lastIndexOf("จำนวน") + "จำนวน".length(), text.lastIndexOf("บาท"));
        amountInString = removeCharFromString(amountInString, ",");
        Double amount = moneyService.stripCommaAndUnit(amountInString);

        //transaction date
        String transactionDateInString = text.substring(text.lastIndexOf("วันที่") + "วันที่".length());
        Date transactionDate = timeService.convertDateWithFormat(transactionDateInString, "dd/MM");

        //payload
        String payload = text.substring(text.lastIndexOf(" ที่ ") + " ที่ ".length(), text.lastIndexOf(" จำนวน"));

        return Record.builder()
                .bank("SCB")
                .type("debit")
                .title("SCB debit")
                .action(action)
                .outcome(amount)
                .description(text)
                .payload(payload)
                .transactionDate(transactionDate)
                .createdAt(new Date())
                .build();
    }

    public Record salaryTransaction(String text) {
        Transaction transaction = new Transaction(text);

        Date transactionDate = timeService.convertDateWithFormat(transaction.getPart(3), "dd/MM@hh:mm");
        Double amount = moneyService.stripCommaAndUnit(transaction.getPart(1));

        return Record.builder()
                .bank("SCB")
                .type("income/salary")
                .title("SCB income/salary")
                .action(transaction.getPart(0))
                .income(amount)
                .description(text)
                .transactionDate(transactionDate)
                .createdAt(new Date())
                .build();
    }

    public Record checkInTransaction(String text) {
        Transaction transaction = new Transaction(text);

        Double amount = moneyService.stripCommaAndUnit(transaction.getPart(1));

        return Record.builder()
                .bank("SCB")
                .type("income/salary")
                .title("SCB income/salary")
                .action(transaction.getPart(0))
                .income(amount)
                .description(text)
                .payload(transaction.getPart(5))
                .createdAt(new Date())
                .build();
    }

    public Record depositTransaction(String text) {
        Transaction transaction = new Transaction(text);

        Date transactionDate = timeService.convertDateWithFormat(transaction.getPart(3), "dd/MM@hh:mm");
        Double amount = moneyService.stripCommaAndUnit(transaction.getPart(1));

        return Record.builder()
                .bank("SCB")
                .type("income/salary")
                .title("SCB income/salary")
                .action("deposit")
                .income(amount)
                .description(text)
                .transactionDate(transactionDate)
                .createdAt(new Date())
                .build();
    }

    public Record transferTransaction(String text) {
        Transaction transaction = new Transaction(text);

        Date transactionDate = timeService.convertDateWithFormat(transaction.getPart(0), "dd/MM@hh:mm");
        Double amount = moneyService.stripCommaAndUnit(transaction.getPart(1));

        return Record.builder()
                .bank("SCB")
                .type("income/salary")
                .title("SCB income/salary")
                .action("transferIn")
                .income(amount)
                .description(text)
                .transactionDate(transactionDate)
                .createdAt(new Date())
                .payload(transaction.getPart(2))
                .build();
    }

    public String removeCharFromString(String input, String charToBeRemoved) {
        List<String> arrayList = Arrays.asList(input.split(""));
        return arrayList.stream().filter(inputChar -> !inputChar.equals(charToBeRemoved)).collect(Collectors.joining());
    }
}
