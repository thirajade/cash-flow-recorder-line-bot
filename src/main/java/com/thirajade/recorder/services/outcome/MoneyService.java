package com.thirajade.recorder.services.outcome;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoneyService {

    public Double stripCommaAndUnit(String amount) {
        String amountInString = amount;
        if (amountInString.contains("บ.")) {
            amountInString = amountInString.substring(0, amountInString.length() - 2);
        } else if (amountInString.contains("บ")) {
            amountInString = amountInString.substring(0, amountInString.length() - 1);
        }
        amountInString = this.removeCharFromString(amountInString, ",");
        return Double.parseDouble(amountInString);
    }

    private String removeCharFromString(String input, String charToBeRemoved) {
        List<String> arrayList = Arrays.asList(input.split(""));
        return arrayList.stream().filter(inputChar -> !inputChar.equals(charToBeRemoved)).collect(Collectors.joining());
    }
}
