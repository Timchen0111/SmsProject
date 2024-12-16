package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.util.HashMap;
import java.util.Map;

public class MyService extends Service {

    private Map<String, Integer> carriages = new HashMap<>();
    private Map<String,String> passengers = new HashMap<>(); //紀錄使用者和其所在車廂
    private Map<String,String> suggest = new HashMap<>(); //記錄使用者和建議前往車廂
    @Override

    public void onCreate() {
        super.onCreate();
        // 初始化每個車廂的人數
        carriages.put("Carriage 1", 0);
        carriages.put("Carriage 2", 0);
        carriages.put("Carriage 3", 0);
        carriages.put("Carriage 4", 0);
        carriages.put("Carriage 5", 0);
        carriages.put("Carriage 6", 0);
    }

    // 用於發送短信的方法
    public void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        //Toast.makeText(this, "SMS sent: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            // 根據 intent 的 action 來決定呼叫哪個方法
            String sender = intent.getStringExtra("number"); // 接收發送者的號碼
            String carriage = intent.getStringExtra("carriage"); // 接收車廂名稱
            System.out.println(carriage);
            if (carriage != null) {
                carriage = "Carriage " + carriage;
            }
            if ("Arrive".equals(intent.getAction())) {
                // 實作部分（處理新乘客）
                handleCustomerEnteringCarriage(sender,carriage);
            }
            if ("Departure".equals(intent.getAction())) {
                // 實作部分（處理離開的乘客）
                handleCustomerExit(sender);
            }
            if ("Response".equals(intent.getAction())) {
                // 乘客回應已到達指定車廂
                String NewCarriage = suggest.get(sender);
                String OldCarriage = passengers.get(sender);
                passengers.put(sender,NewCarriage);
                // 更改車廂人數紀錄
                Integer CurrentCount = carriages.getOrDefault(NewCarriage, 0);
                carriages.put(NewCarriage,CurrentCount+1);
                CurrentCount = carriages.getOrDefault(OldCarriage, 0);
                carriages.put(OldCarriage,CurrentCount-1);
                //回應使用者
                String number = intent.getStringExtra("number");
                String message = "系統已收到通知！";
                sendSMS(number,message);
            }
        }
        return START_STICKY; // 返回該服務的啟動方式
    }

    private void handleCustomerEnteringCarriage(String sender, String carriage) {
        Integer currentCount = carriages.getOrDefault(carriage, 0);
        if (currentCount != null) {
            carriages.put(carriage, currentCount + 1);
        }
        // tell the arriving passenger which carriage has the least number of passengers
        String leastFullCarriage = getLeastFullCarriage(carriage);
        // 印出測試資訊
        System.out.println("Passenger entered carriage: " + carriage);
        //System.out.println("Updated carriage count: " + carriages);
        //System.out.println("Carriage with minimum passengers: " + leastFullCarriage);
        passengers.put(sender,carriage);
        suggest.put(sender,leastFullCarriage);
        //System.out.println("Passenger INFO: " + passengers);
        //System.out.println("Suggest INFO: " + suggest);
        char c = leastFullCarriage.charAt(leastFullCarriage.length() - 1);
        sendSMS(sender, "建議前往第" + c + "車廂。");
    }

    private void handleCustomerExit(String sender) {
        // passenger leaves a carriage
        String carriage = passengers.get(sender);
        Integer currentCount = carriages.get(carriage);
        if (currentCount != null && currentCount > 0) {
            carriages.put(carriage, currentCount - 1);
        }
        passengers.remove(sender);
        suggest.remove(sender);
        // 印出測試資訊
        //System.out.println("Passenger left carriage: " + carriage);
        //System.out.println("Updated carriage count: " + carriages);
        // send SMS when passenger leaves
        sendSMS(sender, "已離開列車");
    }

    // 修改 func() 方法來接收參數(search for the carriage with the least passengers)
    private String getLeastFullCarriage(String carriage) {
        String leastFullCarriage = null;
        Integer CurrentNum =  carriages.getOrDefault(carriage, 0);
        int minCount = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : carriages.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                leastFullCarriage = entry.getKey();
            }
        }
        //程式邏輯：當人數皆相同時，讓使用者留在原本的車廂即可。

        if (CurrentNum - minCount < 2){
            if (carriage != null) {
                leastFullCarriage = carriage;
            }
        }
        return leastFullCarriage;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 如果不需要綁定服務，返回 null
    }
}