package ru.selsup.testtask;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final Timer timer = new Timer("Timer");
    private int requestLimit;
    private long intervalInMillis;
    private AtomicInteger count;
    private Semaphore sem;

    private CrptApi() {
    }

    //constructor
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.count = new AtomicInteger(requestLimit);
        this.requestLimit = requestLimit;
        this.intervalInMillis = timeUnit.toMillis(1);
        sem = new Semaphore(requestLimit, true);
        createTimer();
    }

    //main method
    public void createDoc(Document document, String sign) throws InterruptedException {
        synchronized (this) {
            try {

                sem.acquire();

                if (count.get() == 0) {
                    wait(intervalInMillis);
                }

                docProcessing(document, sign);

                count.decrementAndGet();

            } finally {
                sem.release();
            }
        }
    }

    //work with documents (save etc.)
    private void docProcessing(Document document, String sign) {
    }

    //set timer and reset count (requestLimit)
    private void createTimer() {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                count.set(requestLimit);
            }
        };
        timer.scheduleAtFixedRate(repeatedTask, 0, intervalInMillis);
    }

    //dto
    private static class Document {

        public ArrayList<Product> products;

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }

        public Description description;

        public static class Description {
            public String participantInn;
        }

        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public String reg_date;
        public String reg_number;
    }

    public static void main(String[] args) {
        //если я правильно понял задание, то контроллер и рест слои не нужны.
        //проверял на этом тесте, меняя реквест лимит, количество тредов и счетчик (:
        CrptApi test = new CrptApi(TimeUnit.MINUTES, 10);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 20; i++) {
            executorService.execute(() -> {
                try {
                    test.createDoc(new Document(), "1");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
